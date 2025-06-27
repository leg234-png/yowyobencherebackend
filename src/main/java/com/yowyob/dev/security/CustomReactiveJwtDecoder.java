package com.yowyob.dev.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CustomReactiveJwtDecoder implements ReactiveJwtDecoder {

    private final Key signingKey;

    public CustomReactiveJwtDecoder(String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public Mono<Jwt> decode(String token) throws JwtException {
        return Mono.fromCallable(() -> decodeToken(token))
                .onErrorMap(Exception.class, ex -> new JwtException("JWT decoding failed", ex));
    }

    private Jwt decodeToken(String token) {
        try {
            // Décoder le token avec la même méthode que votre service d'auth
            Claims claims = Jwts.parser()
                    .setSigningKey(this.signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Vérifier l'expiration
            Date expiration = claims.getExpiration();
            if (expiration != null && expiration.before(new Date())) {
                throw new JwtException("Token expired");
            }

            // Convertir les claims en format Spring Security JWT
            Map<String, Object> headers = new HashMap<>();
            headers.put("alg", "HS256");
            headers.put("typ", "JWT");

            Map<String, Object> jwtClaims = new HashMap<>();
            claims.forEach(jwtClaims::put);

            // Assurer que les claims standard sont présents
            if (!jwtClaims.containsKey("sub")) {
                jwtClaims.put("sub", claims.get("email")); // Utiliser email comme subject
            }
            if (!jwtClaims.containsKey("iat")) {
                jwtClaims.put("iat", Instant.now().getEpochSecond());
            }
            if (expiration != null) {
                jwtClaims.put("exp", expiration.toInstant().getEpochSecond());
            }

            // Ajouter des claims personnalisés de votre système
            jwtClaims.put("username", claims.get("email")); // Pour compatibilité
            jwtClaims.put("userId", claims.get("userId"));
            jwtClaims.put("role", claims.get("libelle"));

            Instant issuedAt = claims.getIssuedAt() != null
                    ? claims.getIssuedAt().toInstant()
                    : Instant.now();

            Instant expiresAt = expiration != null
                    ? expiration.toInstant()
                    : Instant.now().plusSeconds(3600); // 1 heure par défaut

            return new Jwt(token, issuedAt, expiresAt, headers, jwtClaims);

        } catch (Exception e) {
            throw new JwtException("Failed to decode JWT: " + e.getMessage(), e);
        }
    }
}