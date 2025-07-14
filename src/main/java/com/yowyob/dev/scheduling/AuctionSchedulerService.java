package com.yowyob.dev.scheduling;

import com.yowyob.dev.enumeration.AuctionStatus;
import com.yowyob.dev.repositories.AuctionRepository;
import com.yowyob.dev.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.scheduler.Scheduler;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionSchedulerService {

    private final AuctionRepository auctionRepository;
    private final Scheduler auctionJobScheduler;
    private final NotificationService notificationService; // Injection

    @Scheduled(fixedRate = 30000)
    public void checkAndCloseExpiredAuctions() {
        auctionRepository.findByEndDateBeforeAndStatus(LocalDateTime.now(), AuctionStatus.OPEN)
                .flatMap(auction -> {
                    log.info("Clôture de l'enchère: {} - {}", auction.getId(), auction.getTitle());
                    auction.setStatus(AuctionStatus.CLOSE);
                    auction.setUpdatedAt(LocalDateTime.now());
                    return auctionRepository.save(auction)
                            .doOnSuccess(notificationService::notifyWinnerPaymentDue);
                })
                .count()
                .publishOn(auctionJobScheduler) // <-- Exécute sur ton scheduler dédié
                .subscribe(count -> {
                    if (count > 0) {
                        log.info("Vérification terminée : {} enchère(s) clôturée(s).", count);
                    }
                }, error -> {
                    log.error("Erreur lors de la clôture des enchères expirées", error);
                });
    }
}
