//---> PATH: src/main/java/ink/yowyob/auctions/infrastructure/webclient/ExternalServiceAdapter.java
package ink.yowyob.auctions.infrastructure.webclient;

import com.fasterxml.jackson.databind.JsonNode;
import ink.yowyob.auctions.application.port.out.ExternalServicePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
public class ExternalServiceAdapter implements ExternalServicePort {

    private final WebClient webClient;
    private final String userServiceBaseUrl;

    public ExternalServiceAdapter(WebClient.Builder webClientBuilder,
                                  @Value("${app.user-service.base-url}") String userServiceBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(userServiceBaseUrl).build();
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    @Override
    public Mono<Boolean> agencyExists(UUID agencyId) {
        return webClient.get()
                .uri("/agencies/{id}", agencyId)
                .retrieve()
                .bodyToMono(JsonNode.class) // Attendre une réponse JSON
                .map(jsonNode -> "200".equals(jsonNode.path("value").asText()))
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(exists -> log.debug("Check for agency {} existence: {}", agencyId, exists))
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                        log.warn("Agency with ID {} not found in external service.", agencyId);
                        return Mono.just(false);
                    }
                    log.error("Error checking agency existence for ID {}: status {}", agencyId, e.getStatusCode(), e);
                    return Mono.just(false); // Par défaut, considérer que l'agence n'existe pas en cas d'erreur
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("A non-HTTP error occurred while checking agency existence for ID {}", agencyId, e);
                    return Mono.just(false);
                })
                .retryWhen(Retry.backoff(2, Duration.ofMillis(300))
                        .filter(e -> !(e instanceof WebClientResponseException.NotFound)));
    }

    @Override
    public Mono<Boolean> userExists(String username) {
        // Supposons que votre service utilisateur a un endpoint pour vérifier par username
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/users/exists").queryParam("username", username).build())
                .retrieve()
                .bodyToMono(Boolean.class) // On s'attend à un simple booléen en réponse
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(exists -> log.debug("Check for user {} existence: {}", username, exists))
                .onErrorResume(e -> {
                    log.error("Error checking user existence for {}", username, e);
                    return Mono.just(false);
                })
                .retryWhen(Retry.backoff(2, Duration.ofMillis(300)));
    }
}