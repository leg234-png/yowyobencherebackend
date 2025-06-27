package com.yowyob.dev.configuration;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/**
 * Un filtre pour WebClient qui propage le token JWT de la requête entrante
 * vers la requête sortante.
 */
public class TokenPropagationWebClientFilter implements ExchangeFilterFunction {

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        // Récupérer le contexte de sécurité réactif
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(context -> {
                    Authentication authentication = context.getAuthentication();
                    // Vérifier si l'utilisateur est authentifié et si c'est une authentification JWT
                    if (authentication instanceof JwtAuthenticationToken) {
                        return Mono.just(authentication);
                    }
                    return Mono.empty();
                })
                .flatMap(authentication -> {
                    // Extraire la valeur brute du token
                    String tokenValue = ((JwtAuthenticationToken) authentication).getToken().getTokenValue();
                    // Créer une nouvelle requête en ajoutant l'en-tête Authorization
                    ClientRequest newRequest = ClientRequest.from(request)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValue)
                            .build();
                    return next.exchange(newRequest);
                })
                // Si aucun contexte de sécurité n'est trouvé, continuer la requête sans token
                .switchIfEmpty(next.exchange(request));
    }
}