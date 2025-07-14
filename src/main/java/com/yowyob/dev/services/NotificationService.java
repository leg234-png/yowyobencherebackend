package com.yowyob.dev.services;

import com.yowyob.dev.models.Auction;
import com.yowyob.dev.models.Bid;
import com.yowyob.dev.repositories.BidRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Comparator;

@Service
@Slf4j
public class NotificationService {

    private final BidRepository bidRepository;

    public NotificationService(BidRepository bidRepository) {
        this.bidRepository = bidRepository;
    }

    public Mono<Void> notifyWinnerPaymentDue(Auction auction) {
        // 1. Trouver l'offre la plus élevée (le gagnant)
        return bidRepository.findByAuctionId(auction.getId())
                .sort(Comparator.comparing(Bid::getPrice).reversed())
                .next() // Prend le premier, qui est le plus élevé
                .flatMap(winnerBid -> {
                    // 2. Simuler l'envoi de la notification
                    log.info("--- NOTIFICATION ---");
                    log.info("À: {}", winnerBid.getUsername());
                    log.info("Sujet: Vous avez remporté l'enchère '{}' !", auction.getTitle());
                    log.info("Message: Félicitations ! Veuillez procéder au paiement de {:.2f} €.", winnerBid.getPrice());
                    log.info("Lien de paiement: /payment/auction/{}", auction.getId());
                    log.info("--- FIN NOTIFICATION ---");
                    return Mono.empty();
                })
                .doOnSuccess(v -> log.info("Notification de paiement envoyée pour l'enchère {}", auction.getId()))
                .then();
    }
}