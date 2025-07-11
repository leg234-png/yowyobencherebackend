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

    @Override
    public Mono<Long> closeExpiredAuctions() {
        log.info("Running job to close expired auctions at {}", LocalDateTime.now());
        // Cette méthode devra être ajoutée au port et à l'adaptateur.
        // Elle est complexe avec Cassandra et nécessite souvent un service tiers ou une table de lookup par "time bucket".
        // Pour une version simplifiée, nous supposerons que l'adaptateur peut le faire, même si c'est inefficace.
        return auctionRepository.findOpenAuctionsEndingBefore(LocalDateTime.now())
                .flatMap(auction -> {
                    log.info("Closing auction: {}", auction.getId());
                    auction.setStatus(AuctionStatus.CLOSED);
                    auction.setUpdatedAt(LocalDateTime.now());
                    return auctionRepository.update(auction)
                            .then(notificationPort.notifyAuctionClosed(auction.getId()));
                })
                .count()
                .doOnSuccess(count -> {
                    if (count > 0) {
                        log.info("Successfully closed {} auctions.", count);
                    }
                });
    }
}