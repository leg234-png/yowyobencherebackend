//---> PATH: src/main/java/ink/yowyob/auctions/infrastructure/messaging/WebSocketNotificationAdapter.java
package ink.yowyob.auctions.infrastructure.messaging;

import ink.yowyob.auctions.application.port.out.NotificationPort;
import ink.yowyob.auctions.domain.model.Bid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketNotificationAdapter implements NotificationPort {

    // Un "Sink" (un canal de publication) est créé pour chaque enchère active.
    // Il permet de diffuser des messages à plusieurs abonnés (multicast).
    private final Map<UUID, Sinks.Many<Object>> auctionSinks = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> notifyNewBid(UUID auctionId, Bid bid) {
        return Mono.fromRunnable(() -> {
            log.debug("Notifying new bid for auction {}", auctionId);
            Sinks.Many<Object> sink = auctionSinks.get(auctionId);
            if (sink != null) {
                // Émettre l'objet Bid dans le sink. Tous les clients connectés le recevront.
                Sinks.EmitResult result = sink.tryEmitNext(bid);
                if (result.isFailure()) {
                    log.warn("Failed to emit new bid notification for auction {}: {}", auctionId, result);
                }
            } else {
                log.warn("No active sink for auction {}. Cannot notify new bid.", auctionId);
            }
        });
    }

    @Override
    public Mono<Void> notifyAuctionClosed(UUID auctionId) {
        return Mono.fromRunnable(() -> {
            log.debug("Notifying auction closed for auction {}", auctionId);
            Sinks.Many<Object> sink = auctionSinks.get(auctionId);
            if (sink != null) {
                // Émettre un message spécial pour indiquer la fin
                 Sinks.EmitResult result = sink.tryEmitNext("AUCTION_CLOSED");
                 if (result.isFailure()) {
                    log.warn("Failed to emit auction closed notification for auction {}: {}", auctionId, result);
                }
                 // Émettre un signal de complétion pour fermer le flux côté client.
                sink.tryEmitComplete();
            } else {
                log.warn("No active sink for auction {}. Cannot notify auction closed.", auctionId);
            }
        });
    }

    /**
     * Méthode utilisée par le WebSocketHandler pour s'abonner aux mises à jour d'une enchère.
     * @param auctionId L'ID de l'enchère à écouter.
     * @return Un Flux qui émettra les nouveaux objets (Bid, messages, etc.).
     */
    public Flux<Object> getAuctionUpdates(UUID auctionId) {
        // Crée un nouveau sink s'il n'existe pas, ou retourne le sink existant.
        // onBackpressureBuffer() stocke les événements si les clients ne peuvent pas suivre.
        return auctionSinks.computeIfAbsent(auctionId, id -> {
            log.info("Creating new sink for auction {}", id);
            return Sinks.many().multicast().onBackpressureBuffer();
        }).asFlux();
    }

    /**
     * Méthode pour nettoyer le sink lorsqu'une session est terminée ou que l'enchère est finie.
     * @param auctionId L'ID de l'enchère à nettoyer.
     */
    public void completeAuctionStream(UUID auctionId) {
        Sinks.Many<Object> sink = auctionSinks.remove(auctionId);
        if (sink != null) {
            log.info("Completing and removing sink for auction {}", auctionId);
            sink.tryEmitComplete();
        }
    }
}