package com.yowyob.dev.services;

import com.yowyob.dev.dto.requestDTO.BidDTO;
import com.yowyob.dev.dto.responseDTO.UserDTO;
import com.yowyob.dev.exceptions.BadRequestException;
import com.yowyob.dev.exceptions.NotFoundException;
import com.yowyob.dev.mapper.BidMapper;
import com.yowyob.dev.models.Auction;
import com.yowyob.dev.models.Bid;
import com.yowyob.dev.repositories.AuctionRepository;
import com.yowyob.dev.repositories.BidRepository;
import com.yowyob.dev.security.CustomJwtAuthenticationConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;

@Service
@Slf4j
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final AuthService authService;
    private final BidMapper bidMapper;
    private final TransactionalOperator transactionalOperator;

    public BidService(BidRepository bidRepository,
                      AuctionRepository auctionRepository,
                      AuthService authService,
                      BidMapper bidMapper,
                      TransactionalOperator transactionalOperator) {
        this.bidRepository = bidRepository;
        this.auctionRepository = auctionRepository;
        this.authService = authService;
        this.bidMapper = bidMapper;
        this.transactionalOperator = transactionalOperator;
    }

    /**
     * Crée une nouvelle offre. Cette opération est transactionnelle pour garantir la cohérence des données
     * même en cas d'accès concurrents.
     */
    public Mono<Bid> createBid(BidDTO dto) {
        log.info("Creating bid for auction {} by user {} with price {}",
                dto.getAuctionId(), dto.getUsername(), dto.getPrice());

        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .cast(JwtAuthenticationToken.class)
                .flatMap(authToken -> {
                    // Extraire les informations de l'utilisateur du token JWT
                    String tokenUsername = CustomJwtAuthenticationConverter.extractUsername(authToken.getToken());
                    String userId = CustomJwtAuthenticationConverter.extractUserId(authToken.getToken());

                    // Vérifier que l'utilisateur peut faire une offre pour lui-même
                    if (!dto.getUsername().equals(tokenUsername)) {
                        return Mono.error(new BadRequestException("You can only bid for yourself"));
                    }

                    return createBidInternal(dto, tokenUsername, userId);
                })
                .switchIfEmpty(Mono.error(new BadRequestException("Authentication required")));
    }

    private Mono<Bid> createBidInternal(BidDTO dto, String authenticatedUsername, String userId) {
        // 1. Récupérer l'enchère et le dernier prix proposé
        Mono<Auction> auctionMono = auctionRepository.findById(dto.getAuctionId())
                .switchIfEmpty(Mono.error(new NotFoundException("Auction not found")));

        Mono<Double> highestPriceMono = bidRepository.findByAuctionId(dto.getAuctionId())
                .map(Bid::getPrice)
                .defaultIfEmpty(0.0)
                .sort(Comparator.reverseOrder())
                .next(); // Prend le plus élevé

        // 2. Vérifier que l'utilisateur existe (optionnel si on fait confiance au JWT)
        Mono<Boolean> userExistsMono = authService.userExists(dto.getUsername());

        // 3. Combiner toutes les validations
        return Mono.zip(auctionMono, highestPriceMono, userExistsMono)
                .flatMap(tuple -> {
                    Auction auction = tuple.getT1();
                    Double highestPrice = tuple.getT2();
                    boolean userExists = tuple.getT3();

                    // Validations métier
                    if (!userExists) {
                        return Mono.error(new NotFoundException("User '" + dto.getUsername() + "' not found"));
                    }

                    if (dto.getPrice() <= Math.max(auction.getStartingPrice(), highestPrice)) {
                        return Mono.error(new BadRequestException(
                                String.format("Bid amount (%.2f) must be higher than the current price (%.2f)",
                                        dto.getPrice(), Math.max(auction.getStartingPrice(), highestPrice))));
                    }

                    if (LocalDateTime.now().isAfter(auction.getEndDate())) {
                        return Mono.error(new BadRequestException("This auction is closed"));
                    }

                    // Vérifier que l'utilisateur ne fait pas d'offre sur sa propre enchère
                    String agencyOwnerUsername = extractUsernameFromAgencyId(auction.getAgencyId());
                    if (dto.getUsername().equals(agencyOwnerUsername)) {
                        return Mono.error(new BadRequestException("You cannot bid on your own auction"));
                    }

                    // 4. Créer et sauvegarder le Bid, et mettre à jour l'enchère
                    Bid newBid = bidMapper.toBid(dto);

                    newBid.setCreatedAt(LocalDateTime.now());
                    newBid.setUpdatedAt(LocalDateTime.now());

                    auction.setCurrentPrice(newBid.getPrice());
                    auction.setUpdatedAt(LocalDateTime.now());

                    newBid.setId(UUID.randomUUID());
                    // Envelopper la sauvegarde du bid et de l'auction dans une seule transaction
                    return bidRepository.save(newBid)
                            .then(auctionRepository.save(auction))
                            .thenReturn(newBid); // Retourner le bid créé
                })
                // Appliquer la transaction à toute la chaîne réactive ci-dessus
                .as(transactionalOperator::transactional)
                .doOnSuccess(bid -> log.info("Bid created successfully: {} for auction {}",
                        bid.getId(), bid.getAuctionId()))
                .doOnError(error -> log.error("Error creating bid for auction {}: {}",
                        dto.getAuctionId(), error.getMessage()));
    }

    public Mono<Bid> getBid(UUID id) {
        return bidRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Bid not found")));
    }

    public Flux<Bid> getAllBids() {
        return bidRepository.findAll();
    }

    public Flux<Bid> getAllBidsOfAuction(UUID auctionId) {
        return bidRepository.findByAuctionId(auctionId)
                .sort((bid1, bid2) -> bid2.getCreatedAt().compareTo(bid1.getCreatedAt())); // Plus récents en premier
    }

    public Flux<UserDTO> getParticipants(UUID auctionId) {
        return bidRepository.findParticipantsByAuctionOrderByPriceDesc(auctionId)
                .distinct() // On s'assure que chaque participant n'est listé qu'une fois
                .flatMap(authService::getUserByUsername) // Pour chaque username, on appelle le service externe
                .take(5); // On prend les 5 premiers (les plus offrants)
    }

    public Mono<Void> deleteBid(UUID id) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .cast(JwtAuthenticationToken.class)
                .flatMap(authToken -> {
                    String username = CustomJwtAuthenticationConverter.extractUsername(authToken.getToken());
                    String role = authToken.getToken().getClaimAsString("libelle");

                    return bidRepository.findById(id)
                            .switchIfEmpty(Mono.error(new NotFoundException("Bid not found")))
                            .flatMap(bid -> {
                                // Vérifier que l'utilisateur peut supprimer cette offre
                                if (!bid.getUsername().equals(username) && !"ADMIN".equalsIgnoreCase(role)) {
                                    return Mono.error(new BadRequestException("You can only delete your own bids"));
                                }
                                return bidRepository.delete(bid);
                            });
                })
                .switchIfEmpty(Mono.error(new BadRequestException("Authentication required")));
    }

    /**
     * Méthode utilitaire pour extraire le nom d'utilisateur du propriétaire d'une agence
     * Cette méthode devrait être adaptée selon votre logique métier
     */
    private String extractUsernameFromAgencyId(UUID agencyId) {
        // TODO: Implémenter la logique pour récupérer le propriétaire de l'agence
        // Pour l'instant, on retourne null pour désactiver cette vérification
        return null;
    }
}