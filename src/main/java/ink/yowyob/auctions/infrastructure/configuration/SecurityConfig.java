//---> PATH: src/main/java/ink/yowyob/auctions/infrastructure/configuration/SecurityConfig.java
package ink.yowyob.auctions.infrastructure.configuration;

import ink.yowyob.auctions.infrastructure.security.CustomAccessDeniedHandler;
import ink.yowyob.auctions.infrastructure.security.CustomAuthenticationEntryPoint;
import ink.yowyob.auctions.infrastructure.security.CustomJwtAuthenticationConverter;
import ink.yowyob.auctions.infrastructure.security.CustomReactiveJwtDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity // Active la sécurité au niveau des méthodes (ex: @PreAuthorize)
public class SecurityConfig {

    @Value("${app.jwt.secret-key}")
    private String jwtSecretKey;

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(CustomAuthenticationEntryPoint authenticationEntryPoint, CustomAccessDeniedHandler accessDeniedHandler) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance()) // API Stateless
                .authorizeExchange(exchanges -> exchanges
                        // Endpoints publics pour la consultation
                        .pathMatchers(HttpMethod.GET, "/api/auctions/{id}").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/agencies/{agencyId}/auctions").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        
                        // Endpoints publics pour les WebSockets et les fichiers statiques
                        .pathMatchers("/ws/auctions/**").permitAll()
                        .pathMatchers("/uploads/**").permitAll()
                        // Pour permettre les uploads aux utilisateurs connectés
                        .pathMatchers(HttpMethod.POST, "/api/uploads").authenticated() 
                        // Documentation API
                        .pathMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/webjars/**").permitAll()
                        
                        // Tout le reste nécessite une authentification
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtDecoder())
                                .jwtAuthenticationConverter(new CustomJwtAuthenticationConverter())
                        )
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        return new CustomReactiveJwtDecoder(jwtSecretKey);
    }
}