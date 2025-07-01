package com.yowyob.dev.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yowyob.dev.dto.responseDTO.UserDTO;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AuthService {

    private final WebClient webClient;

    @Value("${app.auth-service.base-url:https://gateway.yowyob.com/auth-service}")
    private String authServiceBaseUrl;

    @Value("${app.user-service.base-url:http://157.90.26.3:8032/api}")
    private String userServiceBaseUrl;

    public AuthService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB
                .build();
    }

    /**
     * Vérifie si un utilisateur existe par son username/email
     * Compatible avec votre endpoint
     */
    public Mono<Boolean> userExists(String username) {
        String url = authServiceBaseUrl + "/auth/username/" + username;

        return webClient.get()
                .uri(url)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .doOnSuccess(exists -> log.debug("User {} exists: {}", username, exists))
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> {
                    log.debug("User {} not found", username);
                    return Mono.just(false);
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("Error checking if user {} exists", username, ex);
                    return Mono.just(false);
                })
                .retryWhen(Retry.backoff(3, Duration.ofMillis(500))
                        .filter(throwable -> !(throwable instanceof WebClientResponseException.NotFound)))
                .timeout(Duration.ofSeconds(5));
    }

    /**
     * Vérifie si un utilisateur existe par son email
     * Compatible avec votre endpoint
     */
    public Mono<Boolean> userExistsByEmail(String email) {
        String url = authServiceBaseUrl + "/auth/email/" + email;

        return webClient.get()
                .uri(url)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .doOnSuccess(exists -> log.debug("User with email {} exists: {}", email, exists))
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> {
                    log.debug("User with email {} not found", email);
                    return Mono.just(false);
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("Error checking if user with email {} exists", email, ex);
                    return Mono.just(false);
                })
                .retryWhen(Retry.backoff(3, Duration.ofMillis(500))
                        .filter(throwable -> !(throwable instanceof WebClientResponseException.NotFound)))
                .timeout(Duration.ofSeconds(5));
    }

    /**
     * Récupère les détails d'un utilisateur par son username
     * Compatible avec votre service utilisateur
     */
    public Mono<UserDTO> getUserByUsername(String username) {
        String url = userServiceBaseUrl + "/user/" + username;

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .doOnSuccess(user -> log.debug("Retrieved user details for: {}", username))
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> {
                    log.warn("User details not found for username: {}", username);
                    return createEmptyUserDTO(username);
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("Error retrieving user details for username: {}", username, ex);
                    return createEmptyUserDTO(username);
                })
                .retryWhen(Retry.backoff(3, Duration.ofMillis(5000))
                        .filter(throwable -> !(throwable instanceof WebClientResponseException.NotFound)))
                .timeout(Duration.ofSeconds(1000));
    }

    /**
     * Récupère les détails d'un utilisateur par son ID
     * Compatible avec votre endpoint
     */
    public Mono<UserDTO> getUserById(String userId) {
        String url = userServiceBaseUrl + "/user/" + userId;

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .doOnSuccess(user -> log.debug("Retrieved user details for ID: {}", userId))
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> {
                    log.warn("User details not found for ID: {}", userId);
                    return createEmptyUserDTO("user_" + userId);
                })
                .onErrorResume(Exception.class, ex -> {
                    log.error("Error retrieving user details for ID: {}", userId, ex);
                    return createEmptyUserDTO("user_" + userId);
                })
                .retryWhen(Retry.backoff(3, Duration.ofMillis(500))
                        .filter(throwable -> !(throwable instanceof WebClientResponseException.NotFound)))
                .timeout(Duration.ofSeconds(5));
    }

    /**
     * Vérifie si une agence existe
     */
    public Mono<Boolean> agencyExists(String agencyId) {
       String url = userServiceBaseUrl + "/agencies/" + agencyId;

        WebClient webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                                .doOnConnected(conn -> {
                                    conn.addHandlerLast(new ReadTimeoutHandler(3, TimeUnit.SECONDS));
                                    conn.addHandlerLast(new WriteTimeoutHandler(3, TimeUnit.SECONDS));
                                })))
                .build();

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> System.out.println("Réponse reçue : " + response))
                .map(response -> {
                    try {
                        // Parse manuellement le champ "value"
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode jsonNode = mapper.readTree(response);
                        return "200".equals(jsonNode.get("value").asText());
                    } catch (Exception e) {
                        System.err.println("Erreur parsing JSON: " + e.getMessage());
                        return false;
                    }
                });
    }

    /**
     * Valide un token JWT en utilisant votre endpoint de test
     */
    public Mono<Boolean> validateToken(String token) {
        String url = authServiceBaseUrl + "/auth/test";

        return webClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .doOnSuccess(valid -> log.debug("Token validation result: {}", valid))
                .onErrorResume(Exception.class, ex -> {
                    log.error("Error validating token", ex);
                    return Mono.just(false);
                })
                .retryWhen(Retry.backoff(2, Duration.ofMillis(300)))
                .timeout(Duration.ofSeconds(3));
    }

    private Mono<UserDTO> createEmptyUserDTO(String username) {
        UserDTO emptyUser = new UserDTO();
        // Vous devrez adapter selon votre structure UserDTO
        return Mono.just(emptyUser);
    }
}