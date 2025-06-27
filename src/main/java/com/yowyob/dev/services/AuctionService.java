package com.yowyob.dev.services;

import com.yowyob.dev.dto.requestDTO.AuctionDTO;
import com.yowyob.dev.enumeration.AuctionStatus;
import com.yowyob.dev.exceptions.NotFoundException;
import com.yowyob.dev.mapper.AuctionMapper;
import com.yowyob.dev.models.Auction;
import com.yowyob.dev.repositories.AuctionRepository;
import com.yowyob.dev.repositories.BidRepository;
import com.yowyob.dev.repositories.CategoryRepository;
import com.yowyob.dev.security.CustomJwtAuthenticationConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final CategoryRepository categoryRepository;
    private final BidRepository bidRepository;
    private final AuthService authService;
    private final AuctionMapper auctionMapper;
    private final String uploadPath;
    private final String uploadBaseUrl;

    public AuctionService(AuctionRepository auctionRepository,
                          CategoryRepository categoryRepository,
                          BidRepository bidRepository,
                          AuthService authService,
                          AuctionMapper auctionMapper,
                          @Value("${app.upload.path:uploads/}") String uploadPath,
                          @Value("${app.upload.base-url:http://157.90.26.3:8031/api/uploads/}") String uploadBaseUrl) {
        this.auctionRepository = auctionRepository;
        this.categoryRepository = categoryRepository;
        this.bidRepository = bidRepository;
        this.authService = authService;
        this.auctionMapper = auctionMapper;
        this.uploadPath = uploadPath != null ? uploadPath : "uploads/";
        this.uploadBaseUrl = uploadBaseUrl != null ? uploadBaseUrl : "http://157.90.26.3:8031/api/uploads/";

        // Créer le répertoire d'upload s'il n'existe pas
        try {
            Files.createDirectories(Paths.get(this.uploadPath));
            log.info("Upload directory created/verified: {}", this.uploadPath);
        } catch (IOException e) {
            log.error("Failed to create upload directory: {}", this.uploadPath, e);
        }
    }

    @Transactional
    public Mono<Auction> createAuction(AuctionDTO auctionDTO, Flux<FilePart> imageFiles) {
        log.info("Creating auction: {}", auctionDTO.getTitle());

        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .cast(JwtAuthenticationToken.class)
                .flatMap(authToken -> {
                    String username = CustomJwtAuthenticationConverter.extractUsername(authToken.getToken());
                    String userId = CustomJwtAuthenticationConverter.extractUserId(authToken.getToken());

                    log.debug("Creating auction for user: {} with ID: {}", username, userId);

                    Auction auction = auctionMapper.toAuction(auctionDTO);
                    auction.setId(UUID.randomUUID());
                    auction.setStatus(AuctionStatus.OPEN);
                    auction.setCreatedAt(LocalDateTime.now());
                    auction.setUpdatedAt(LocalDateTime.now());

                    // Vérifier que l'agence existe
                    Mono<Boolean> agencyExists = authService.agencyExists(auction.getAgencyId().toString());
                    // Vérifier que la catégorie existe
                    Mono<Boolean> categoryExists = categoryRepository.existsById(auction.getCategoryId());

                    return Mono.zip(agencyExists, categoryExists)
                            .flatMap(tuple -> {
                                if (!tuple.getT1()) {
                                    log.warn("Agency not found: {}", auction.getAgencyId());
                                    return Mono.error(new NotFoundException("Agency not found"));
                                }
                                if (!tuple.getT2()) {
                                    log.warn("Category not found: {}", auction.getCategoryId());
                                    return Mono.error(new NotFoundException("Category not found"));
                                }

                                // Sauvegarder les images
                                return saveImages(imageFiles).collectList();
                            })
                            .flatMap(imageUrls -> {
                                auction.setImageUrls(imageUrls);
                                return auctionRepository.save(auction);
                            });
                })
                .switchIfEmpty(Mono.error(new NotFoundException("Authentication required")))
                .doOnSuccess(auction -> log.info("Auction created successfully: {}", auction.getId()))
                .doOnError(error -> log.error("Error creating auction: {}", error.getMessage()));
    }

    public Mono<Auction> getAuctionById(UUID id) {
        return auctionRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Auction not found")))
                .flatMap(this::enrichAuctionWithDetails);
    }

    private Mono<Auction> enrichAuctionWithDetails(Auction auction) {
        Mono<List<String>> participantsMono = bidRepository
                .findParticipantsByAuctionOrderByPriceDesc(auction.getId())
                .collectList();

        Mono<Auction> auctionMono = Mono.just(auction);

        return Mono.zip(auctionMono, participantsMono, (auc, participants) -> {
            auc.setParticipants(participants);
            return auc;
        });
    }

    public Flux<Auction> getAuctionsByAgencyId(UUID agencyId) {
        return authService.agencyExists(agencyId.toString())
                .flatMapMany(exists -> {
                    if (!exists) {
                        return Flux.error(new NotFoundException("Agency not found"));
                    }
                    return auctionRepository.findByAgencyId(agencyId);
                });
    }

    public Mono<Page<Auction>> findRecentAuctions(Pageable pageable, int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);

        Flux<Auction> auctions = auctionRepository.findRecentAuctions(
                cutoffDate,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        Mono<Long> total = auctionRepository.countRecentAuctions(cutoffDate);

        return Mono.zip(auctions.collectList(), total)
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    public Mono<Page<Auction>> findAuctionsEndingSoon(Pageable pageable, int hours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusHours(hours);

        Flux<Auction> auctions = auctionRepository.findEndingSoonAuctions(
                AuctionStatus.OPEN,
                now,
                endTime,
                pageable.getPageSize(),
                pageable.getOffset()
        );

        Mono<Long> total = auctionRepository.countEndingSoonAuctions(
                AuctionStatus.OPEN,
                now,
                endTime
        );

        return Mono.zip(auctions.collectList(), total)
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    /**
     * Sauvegarde les images uploadées et retourne les URLs
     */
    private Flux<String> saveImages(Flux<FilePart> fileParts) {
        return fileParts.flatMap(filePart -> {
            String fileName = UUID.randomUUID() + "_" + filePart.filename();
            Path targetFile = Paths.get(uploadPath).resolve(fileName);

            return filePart.transferTo(targetFile)
                    .then(Mono.just(uploadBaseUrl + fileName))
                    .doOnSuccess(url -> log.debug("Image saved: {}", url))
                    .onErrorResume(error -> {
                        log.error("Error saving image: {}", error.getMessage());
                        return Mono.empty(); // Ignorer cette image en cas d'erreur
                    });
        });
    }

    /**
     * Met à jour une enchère (seulement par le propriétaire)
     */
    public Mono<Auction> updateAuction(UUID auctionId, AuctionDTO updateDto) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .cast(JwtAuthenticationToken.class)
                .flatMap(authToken -> {
                    String username = CustomJwtAuthenticationConverter.extractUsername(authToken.getToken());

                    return auctionRepository.findById(auctionId)
                            .switchIfEmpty(Mono.error(new NotFoundException("Auction not found")))
                            .flatMap(existingAuction -> {
                                // Vérifier que l'utilisateur peut modifier cette enchère
                                // TODO: Implémenter la logique de vérification du propriétaire

                                // Mettre à jour les champs
                                if (updateDto.getTitle() != null) {
                                    existingAuction.setTitle(updateDto.getTitle());
                                }
                                if (updateDto.getDescription() != null) {
                                    existingAuction.setDescription(updateDto.getDescription());
                                }
                                if (updateDto.getEndDate() != null) {
                                    existingAuction.setEndDate(updateDto.getEndDate());
                                }
                                existingAuction.setUpdatedAt(LocalDateTime.now());

                                return auctionRepository.save(existingAuction);
                            });
                })
                .switchIfEmpty(Mono.error(new NotFoundException("Authentication required")));
    }

    /**
     * Supprime une enchère (seulement par le propriétaire)
     */
    public Mono<Void> deleteAuction(UUID auctionId) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .cast(JwtAuthenticationToken.class)
                .flatMap(authToken -> {
                    String username = CustomJwtAuthenticationConverter.extractUsername(authToken.getToken());
                    String role = authToken.getToken().getClaimAsString("libelle");

                    return auctionRepository.findById(auctionId)
                            .switchIfEmpty(Mono.error(new NotFoundException("Auction not found")))
                            .flatMap(auction -> {
                                // Vérifier que l'utilisateur peut supprimer cette enchère
                                // TODO: Implémenter la logique de vérification du propriétaire
                                // ou vérifier si c'est un admin
                                if (!"ADMIN".equalsIgnoreCase(role)) {
                                    // Logique additionnelle pour vérifier le propriétaire
                                }

                                return auctionRepository.delete(auction);
                            });
                })
                .switchIfEmpty(Mono.error(new NotFoundException("Authentication required")));
    }
}