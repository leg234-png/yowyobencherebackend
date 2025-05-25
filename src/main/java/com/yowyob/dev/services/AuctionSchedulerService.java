package com.yowyob.dev.services;



import com.yowyob.dev.dto.responseDTO.UserDTO;
import com.yowyob.dev.enumeration.AuctionStatus;
import com.yowyob.dev.models.Auction;
import com.yowyob.dev.repositories.AuctionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionSchedulerService {

    private final AuctionRepository auctionRepository;


    private final RestTemplate restTemplate;

    /**
     * Vérifie toutes les 30 secondes les enchères à clôturer
     */
    //@Scheduled(fixedRate = 30000)
    public void checkAndCloseExpiredAuctions() {
        log.info("Vérification des enchères expirées...");

        List<Auction> expiredAuctions = auctionRepository.findByEndDateBeforeAndStatus(
                LocalDateTime.now(),
                AuctionStatus.OPEN
        );

        for (Auction auction : expiredAuctions) {
            auction.setStatus(AuctionStatus.CLOSE);


            // TODO: ici tu peux déclencher un service de notification ou traitement post-vente
            log.info("Gagnant de l'enchère : {}", auction.getParticipants().getFirst());
            log.info("Montant de l'enchère : {}", auction.getBids().getFirst());

            auctionRepository.save(auction);
        }

        log.info("Vérification terminée : {} enchère(s) clôturée(s).", expiredAuctions.size());
    }
}
