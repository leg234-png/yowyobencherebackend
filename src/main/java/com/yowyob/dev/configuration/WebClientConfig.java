package com.yowyob.dev.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        System.out.println("### CONFIGURING WEBCLIENT WITH CUSTOM JWT PROPAGATION FILTER ###");
        return WebClient.builder()
                // Appliquer notre filtre personnalisé à chaque requête sortante
                .filter(new TokenPropagationWebClientFilter());
    }
}