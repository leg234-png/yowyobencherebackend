//---> PATH: src/main/java/ink/yowyob/auctions/infrastructure/security/CustomReactiveJwtDecoder.java
package ink.yowyob.auctions.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CustomReactiveJwtDecoder implements ReactiveJwtDecoder {

    private final SecretKey secretKey;

    public CustomReactiveJwtDecoder(String secret) {
        // La clé doit être suffisamment longue pour l'algorithme HMAC-SHA (ex: 256 bits pour HS256)
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public Mono<Jwt> decode(String token) {
        return Mono.fromCallable(() -> {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(this.secretKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                // Validation manuelle de l'expiration (bien que JJWT le fasse aussi)
                Date expiration = claims.getExpiration();
                if (expiration != null && expiration.toInstant().isBefore(Instant.now())) {
                    throw new JwtException("Token expired at " + expiration);
                }

                // Construire l'objet Jwt de Spring Security à partir des claims
                Map<String, Object> headers = new HashMap<>();
                headers.put("alg", "HS256");
                headers.put("typ", "JWT");

                return new Jwt(
                        token,
                        claims.getIssuedAt().toInstant(),
                        claims.getExpiration().toInstant(),
                        headers,
                        claims
                );
            } catch (Exception e) {
                // Envelopper toute exception dans une JwtException pour que Spring la gère correctement
                throw new JwtException("Failed to decode JWT: " + e.getMessage(), e);
            }
        });
    }
}