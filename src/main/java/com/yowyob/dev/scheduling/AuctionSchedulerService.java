package com.yowyob.dev.scheduling;

import com.yowyob.dev.enumeration.AuctionStatus;
import com.yowyob.dev.repositories.AuctionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionSchedulerService {

    private final AuctionRepository auctionRepository;

    /**
     * Vérifie toutes les 30 secondes les enchères à clôturer.
     * Le traitement est entièrement réactif.
     */
    @Scheduled(fixedRate = 30000)
    public void checkAndCloseExpiredAuctions() {
        log.info("Vérification des enchères expirées...");

        auctionRepository.findByEndDateBeforeAndStatus(LocalDateTime.now(), AuctionStatus.OPEN)
                .flatMap(auction -> {
                    log.info("Clôture de l'enchère: {} - {}", auction.getId(), auction.getTitle());
                    auction.setStatus(AuctionStatus.CLOSE);
                    auction.setUpdatedAt(LocalDateTime.now());

                    // Ici, on pourrait ajouter une logique de notification pour le gagnant
                    if (auction.getCurrentPrice() != null) {
                        log.info("L'enchère s'est terminée avec un prix de : {}", auction.getCurrentPrice());
                    } else {
                        log.info("L'enchère s'est terminée sans aucune offre.");
                    }

                    return auctionRepository.save(auction);
                })
                .count() // On attend la fin du flux pour compter les enchères clôturées
                .subscribe(count -> {
                    if (count > 0) {
                        log.info("Vérification terminée : {} enchère(s) clôturée(s).", count);
                    }
                }, error -> {
                    log.error("Erreur lors de la clôture des enchères expirées", error);
                });
    }
}