package com.yowyob.dev.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        // Créer le token d'authentification avec les autorités extraites
        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(jwt, authorities);

        return Mono.just(authenticationToken);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        // Extraire le rôle de votre claim "libelle" ou "role"
        String role = jwt.getClaimAsString("libelle");
        if (role == null) {
            role = jwt.getClaimAsString("role");
        }

        if (role != null && !role.trim().isEmpty()) {
            // Ajouter le préfixe ROLE_ si ce n'est pas déjà présent
            String formattedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
            return List.of(new SimpleGrantedAuthority(formattedRole));
        }

        // Par défaut, donner le rôle USER si aucun rôle n'est trouvé
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /**
     * Méthode utilitaire pour extraire le nom d'utilisateur du JWT
     */
    public static String extractUsername(Jwt jwt) {
        // Essayer plusieurs claims pour le nom d'utilisateur
        String username = jwt.getClaimAsString("username");
        if (username == null) {
            username = jwt.getClaimAsString("email");
        }
        if (username == null) {
            username = jwt.getSubject();
        }
        return username;
    }

    /**
     * Méthode utilitaire pour extraire l'ID utilisateur du JWT
     */
    public static String extractUserId(Jwt jwt) {
        return jwt.getClaimAsString("userId");
    }

    /**
     * Méthode utilitaire pour extraire le nom complet de l'utilisateur
     */
    public static String extractUserName(Jwt jwt) {
        return jwt.getClaimAsString("name");
    }
}