//---> PATH: src/main/java/ink/yowyob/auctions/application/service/BiddingService.java
package ink.yowyob.auctions.application.service;

import ink.yowyob.auctions.application.port.in.PlaceBidUseCase;
import ink.yowyob.auctions.application.port.out.AuctionRepositoryPort;
import ink.yowyob.auctions.application.port.out.BidRepositoryPort;
import ink.yowyob.auctions.application.port.out.ExternalServicePort;
import ink.yowyob.auctions.application.port.out.NotificationPort;
import ink.yowyob.auctions.domain.enumeration.AuctionStatus;
import ink.yowyob.auctions.domain.model.Bid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class BiddingService implements PlaceBidUseCase {

    private final BidRepositoryPort bidRepository;
    private final AuctionRepositoryPort auctionRepository;
    private final NotificationPort notificationPort;
    private final ExternalServicePort externalServicePort;

    // Un verrou simple par enchère pour éviter les race conditions lors de l'enchérissement.
    // Pour une application multi-instance, un verrou distribué (ex: Redis) serait nécessaire.
    private final ConcurrentHashMap<UUID, Object> auctionLocks = new ConcurrentHashMap<>();

    @Override
    @Transactional // Assure que la mise à jour de l'enchère et la sauvegarde de l'offre sont atomiques
    public Mono<Bid> placeBid(PlaceBidCommand command) {
        // Le synchronized ici est un verrou local. Il empêche deux threads sur la MÊME instance
        // de traiter une offre pour la même enchère en même temps.
        return Mono.defer(() -> {
            synchronized (auctionLocks.computeIfAbsent(command.getAuctionId(), k -> new Object())) {
                return this.doPlaceBid(command);
            }
        }).doFinally(signalType -> auctionLocks.remove(command.getAuctionId()));
    }

    private Mono<Bid> doPlaceBid(PlaceBidCommand command) {
        log.info("Attempting to place bid for auction {} by user {}", command.getAuctionId(), command.getBidderUsername());

        Mono<Boolean> userExists = externalServicePort.userExists(command.getBidderUsername());

        return auctionRepository.findById(command.getAuctionId())
                .zipWith(userExists)
                .flatMap(tuple -> {
                    var auction = tuple.getT1();
                    var userIsValid = tuple.getT2();

                    if (!userIsValid) {
                        return Mono.error(new IllegalArgumentException("Bidder " + command.getBidderUsername() + " does not exist."));
                    }
                    if (auction.getStatus() != AuctionStatus.OPEN) {
                        return Mono.error(new IllegalStateException("Auction is not open for bidding."));
                    }
                    if (command.getPrice().compareTo(auction.getCurrentPrice()) <= 0) {
                        return Mono.error(new IllegalArgumentException("Bid price must be higher than the current price."));
                    }
                    if (auction.getAgencyId().toString().equals(command.getBidderUsername())) { // Simplification, à adapter
                        return Mono.error(new IllegalStateException("Seller cannot bid on their own auction."));
                    }

                    Bid newBid = Bid.builder()
                            .id(UUID.randomUUID())
                            .auctionId(command.getAuctionId())
                            .username(command.getBidderUsername())
                            .price(command.getPrice())
                            .createdAt(LocalDateTime.now())
                            .build();

                    auction.setCurrentPrice(newBid.getPrice());
                    auction.setUpdatedAt(LocalDateTime.now());
                    
                    // L'opérateur transactional gère le commit/rollback
                    return bidRepository.save(newBid)
                            .then(auctionRepository.update(auction)) 
                            .thenReturn(newBid);
                })
                .flatMap(savedBid -> notificationPort.notifyNewBid(savedBid.getAuctionId(), savedBid)
                        .thenReturn(savedBid)
                )
                .doOnError(e -> log.error("Failed to place bid for auction {}: {}", command.getAuctionId(), e.getMessage()));
    }
}
