package com.yowyob.dev.utils;

import com.yowyob.dev.security.CustomJwtAuthenticationConverter;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

/**
 * Utilitaires pour extraire les informations du JWT dans un contexte réactif
 */
public class JwtUtils {

    /**
     * Extrait le nom d'utilisateur du contexte de sécurité actuel
     */
    public static Mono<String> getCurrentUsername() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .cast(JwtAuthenticationToken.class)
                .map(authToken -> CustomJwtAuthenticationConverter.extractUsername(authToken.getToken()))
                .switchIfEmpty(Mono.error(new RuntimeException("No authenticated user found")));
    }

    /**
     * Extrait l'ID utilisateur du contexte de sécurité actuel
     */
    public static Mono<String> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .cast(JwtAuthenticationToken.class)
                .map(authToken -> CustomJwtAuthenticationConverter.extractUserId(authToken.getToken()))
                .switchIfEmpty(Mono.error(new RuntimeException("No authenticated user found")));
    }

    /**
     * Extrait le nom complet de l'utilisateur du contexte de sécurité actuel
     */
    public static Mono<String> getCurrentUserFullName() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .cast(JwtAuthenticationToken.class)
                .map(authToken -> CustomJwtAuthenticationConverter.extractUserName(authToken.getToken()))
                .switchIfEmpty(Mono.error(new RuntimeException("No authenticated user found")));
    }

    /**
     * Extrait le rôle de l'utilisateur du contexte de sécurité actuel
     */
    public static Mono<String> getCurrentUserRole() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .cast(JwtAuthenticationToken.class)
                .map(authToken -> authToken.getToken().getClaimAsString("libelle"))
                .switchIfEmpty(Mono.error(new RuntimeException("No authenticated user found")));
    }

    /**
     * Vérifie si l'utilisateur actuel a un rôle spécifique
     */
    public static Mono<Boolean> hasRole(String role) {
        return getCurrentUserRole()
                .map(userRole -> role.equalsIgnoreCase(userRole))
                .onErrorReturn(false);
    }

    /**
     * Vérifie si l'utilisateur actuel est un administrateur
     */
    public static Mono<Boolean> isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Extrait toutes les informations utilisateur du contexte de sécurité
     */
    public static Mono<UserInfo> getCurrentUserInfo() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .cast(JwtAuthenticationToken.class)
                .map(authToken -> {
                    var jwt = authToken.getToken();
                    return UserInfo.builder()
                            .username(CustomJwtAuthenticationConverter.extractUsername(jwt))
                            .userId(CustomJwtAuthenticationConverter.extractUserId(jwt))
                            .fullName(CustomJwtAuthenticationConverter.extractUserName(jwt))
                            .email(jwt.getClaimAsString("email"))
                            .role(jwt.getClaimAsString("libelle"))
                            .build();
                })
                .switchIfEmpty(Mono.error(new RuntimeException("No authenticated user found")));
    }

    /**
     * Classe pour encapsuler les informations utilisateur
     */
    public static class UserInfo {
        private String username;
        private String userId;
        private String fullName;
        private String email;
        private String role;

        // Builder pattern
        public static UserInfoBuilder builder() {
            return new UserInfoBuilder();
        }

        public static class UserInfoBuilder {
            private String username;
            private String userId;
            private String fullName;
            private String email;
            private String role;

            public UserInfoBuilder username(String username) {
                this.username = username;
                return this;
            }

            public UserInfoBuilder userId(String userId) {
                this.userId = userId;
                return this;
            }

            public UserInfoBuilder fullName(String fullName) {
                this.fullName = fullName;
                return this;
            }

            public UserInfoBuilder email(String email) {
                this.email = email;
                return this;
            }

            public UserInfoBuilder role(String role) {
                this.role = role;
                return this;
            }

            public UserInfo build() {
                UserInfo userInfo = new UserInfo();
                userInfo.username = this.username;
                userInfo.userId = this.userId;
                userInfo.fullName = this.fullName;
                userInfo.email = this.email;
                userInfo.role = this.role;
                return userInfo;
            }
        }

        // Getters
        public String getUsername() { return username; }
        public String getUserId() { return userId; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getRole() { return role; }

        @Override
        public String toString() {
            return "UserInfo{" +
                    "username='" + username + '\'' +
                    ", userId='" + userId + '\'' +
                    ", fullName='" + fullName + '\'' +
                    ", email='" + email + '\'' +
                    ", role='" + role + '\'' +
                    '}';
        }
    }
}