//---> PATH: src/main/java/ink/yowyob/auctions/infrastructure/security/CustomAuthenticationEntryPoint.java
package ink.yowyob.auctions.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class CustomAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Data
    @Builder
    private static class ErrorResponse {
        private int statusCode;
        private String error;
        private String message;
        private Instant timestamp;
        private String path;
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message("Authentication failed: " + ex.getMessage())
                .timestamp(Instant.now())
                .path(exchange.getRequest().getPath().value())
                .build();

        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = bufferFactory.wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}
