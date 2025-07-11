//---> PATH: src/main/java/ink/yowyob/auctions/presentation/websocket/AuctionWebSocketHandler.java
package ink.yowyob.auctions.presentation.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ink.yowyob.auctions.domain.model.Bid;
import ink.yowyob.auctions.infrastructure.messaging.WebSocketNotificationAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionWebSocketHandler implements WebSocketHandler {

    private final WebSocketNotificationAdapter notificationAdapter;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        UUID auctionId;
        try {
            // Extrait l'ID de l'enchère depuis l'URL, ex: /ws/auctions/xxxx-xxxx-xxxx-xxxx
            String path = session.getHandshakeInfo().getUri().getPath();
            auctionId = UUID.fromString(path.substring(path.lastIndexOf('/') + 1));
        } catch (Exception e) {
            log.error("Invalid auction ID in WebSocket URI.", e);
            return session.close(org.springframework.web.reactive.socket.CloseStatus.BAD_DATA.withReason("Invalid Auction ID format"));
        }

        log.info("Client [{}] connected to auction stream [{}].", session.getId(), auctionId);

        // Le client écoute les messages entrants (non utilisé ici, mais pourrait servir pour des commandes)
        Mono<Void> input = session.receive()
                .doOnNext(message -> log.trace("Received message from client [{}]: {}", session.getId(), message.getPayloadAsText()))
                .then();

        // Le serveur envoie les mises à jour au client
        Mono<Void> output = session.send(notificationAdapter.getAuctionUpdates(auctionId)
                .flatMap(payload -> {
                    try {
                        // Sérialise l'objet (Bid, String, etc.) en JSON
                        byte[] bytes = objectMapper.writeValueAsBytes(payload);
                        return Mono.just(session.textMessage(new String(bytes)));
                    } catch (JsonProcessingException e) {
                        log.error("Failed to serialize WebSocket message payload.", e);
                        return Mono.error(e);
                    }
                })
        );
        
        // Gère la déconnexion
        return Mono.zip(input, output)
                   .then()
                   .doFinally(signalType -> {
                       log.info("Client [{}] disconnected from auction stream [{}].", session.getId(), auctionId);
                       // Potentiellement, nettoyer le sink si plus personne n'écoute.
                       // notificationAdapter.cleanupSinkIfEmpty(auctionId);
                   });
    }
}