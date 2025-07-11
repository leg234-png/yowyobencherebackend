//---> PATH: src/main/java/ink/yowyob/auctions/infrastructure/configuration/WebSocketConfig.java
package ink.yowyob.auctions.infrastructure.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import ink.yowyob.auctions.presentation.websocket.AuctionWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class WebSocketConfig {

    private final AuctionWebSocketHandler auctionWebSocketHandler;

    @Bean
    public HandlerMapping webSocketHandlerMapping() {
        // Mappe une URL à un handler WebSocket.
        Map<String, Object> map = new HashMap<>();
        // Les clients se connecteront à cette URL pour écouter les mises à jour d'une enchère.
        map.put("/ws/auctions/{auctionId}", auctionWebSocketHandler);

        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setOrder(Ordered.HIGHEST_PRECEDENCE); // Donner une priorité élevée au mapping WebSocket
        handlerMapping.setUrlMap(map);
        return handlerMapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        // Cet adaptateur est nécessaire pour que Spring puisse gérer les requêtes WebSocket.
        return new WebSocketHandlerAdapter();
    }
}