//---> PATH: src/main/java/ink/yowyob/auctions/utils/JwtUtils.java
package ink.yowyob.auctions.utils;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.List;

public class JwtUtils {

    // ... (les autres méthodes comme getCurrentUsername, etc. restent les mêmes)

    public static Mono<String> getCurrentUsername() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .cast(JwtAuthenticationToken.class)
                .map(authToken -> extractUsername(authToken.getToken()))
                .switchIfEmpty(Mono.error(new RuntimeException("No authenticated user found")));
    }
    
    private static String extractUsername(Jwt jwt) {
        return jwt.getClaimAsString("username");
    }

    /**
     * Vérifie si l'utilisateur authentifié est le propriétaire de l'agence.
     * Cette logique dépendra fortement de la structure de votre token JWT.
     * Hypothèse : le token contient une claim "agencies" (liste d'IDs) ou "agency_id".
     *
     * @param agencyId L'ID de l'agence à vérifier.
     * @param exchange L'échange web actuel pour récupérer le token.
     * @return Mono<Boolean>
     */
    public static Mono<Boolean> isOwnerOfAgency(UUID agencyId, ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .cast(JwtAuthenticationToken.class)
                .map(JwtAuthenticationToken::getToken)
                .map(jwt -> {
                    // Logique de vérification. Adaptez à votre token.
                    // Cas 1: une claim "agency_id"
                    String userAgencyId = jwt.getClaimAsString("agency_id");
                    if (userAgencyId != null) {
                        return userAgencyId.equals(agencyId.toString());
                    }

                    // Cas 2: une claim "agencies" (liste)
                    List<String> userAgencies = jwt.getClaimAsStringList("agencies");
                    if (userAgencies != null) {
                        return userAgencies.contains(agencyId.toString());
                    }

                    // Cas 3: Rôle Admin
                    List<String> roles = jwt.getClaimAsStringList("authorities");
                    if (roles != null && roles.contains("ROLE_ADMIN")) {
                        return true;
                    }
                    
                    return false;
                })
                .defaultIfEmpty(false);
    }
}