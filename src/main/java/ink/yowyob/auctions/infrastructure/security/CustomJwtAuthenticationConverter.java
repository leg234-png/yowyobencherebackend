//---> PATH: src/main/java/ink/yowyob/auctions/infrastructure/security/CustomJwtAuthenticationConverter.java
package ink.yowyob.auctions.infrastructure.security;

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
import java.util.stream.Collectors;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private static final String AUTHORITIES_CLAIM_NAME = "authorities";
    private static final String AUTHORITY_PREFIX = "ROLE_";

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return Mono.just(new JwtAuthenticationToken(jwt, authorities));
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> authoritiesFromClaim = jwt.getClaimAsStringList(AUTHORITIES_CLAIM_NAME);

        if (authoritiesFromClaim == null || authoritiesFromClaim.isEmpty()) {
            return Collections.emptyList();
        }

        return authoritiesFromClaim.stream()
                .map(authority -> {
                    if (!authority.startsWith(AUTHORITY_PREFIX)) {
                        return new SimpleGrantedAuthority(AUTHORITY_PREFIX + authority);
                    }
                    return new SimpleGrantedAuthority(authority);
                })
                .collect(Collectors.toList());
    }
}