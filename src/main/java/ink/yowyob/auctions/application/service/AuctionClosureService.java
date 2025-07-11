//---> PATH: src/main/java/ink/yowyob/auctions/application/service/AuctionClosureService.java
package ink.yowyob.auctions.application.service;

import ink.yowyob.auctions.application.port.in.CloseExpiredAuctionsUseCase;
import ink.yowyob.auctions.application.port.out.AuctionRepositoryPort;
import ink.yowyob.auctions.application.port.out.NotificationPort;
import ink.yowyob.auctions.domain.enumeration.AuctionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionClosureService implements CloseExpiredAuctionsUseCase {

    private final AuctionRepositoryPort auctionRepository;
    private final NotificationPort notificationPort;

    /**
     * Tâche planifiée pour trouver et clôturer les enchères expirées.
     * Cette méthode est maintenant complète et correcte.
     */
    @Override
    public Mono<Long> closeExpiredAuctions() {
        log.info("Running job to close expired auctions at {}", LocalDateTime.now());

        // 1. On trouve les enchères expirées en utilisant notre table de lookup efficace.
        return auctionRepository.findOpenAuctionsEndingBefore(LocalDateTime.now())
                .flatMap(auction -> {
                    // 2. Pour chaque enchère trouvée, on met à jour son statut.
                    log.info("Closing auction: {}", auction.getId());
                    auction.setStatus(AuctionStatus.CLOSED);
                    auction.setUpdatedAt(LocalDateTime.now());

                    // 3. On appelle la nouvelle méthode qui met à jour l'enchère ET nettoie les lookups.
                    return auctionRepository.updateAndCleanupLookups(auction)
                            // 4. Après le nettoyage, on notifie les clients via WebSocket.
                            .then(notificationPort.notifyAuctionClosed(auction.getId()));
                })
                .count() // On compte combien d'enchères ont été traitées.
                .doOnSuccess(count -> {
                    if (count > 0) {
                        log.info("Successfully closed and cleaned up {} auctions.", count);
                    }
                });
    }
}