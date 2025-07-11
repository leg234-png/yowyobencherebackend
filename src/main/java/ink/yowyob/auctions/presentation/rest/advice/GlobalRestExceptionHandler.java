//---> PATH: src/main/java/ink/yowyob/auctions/presentation/rest/advice/GlobalRestExceptionHandler.java
package ink.yowyob.auctions.presentation.rest.advice;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalRestExceptionHandler {

    @Data
    @Builder
    private static class ErrorResponse {
        private int statusCode;
        private String error;
        private String message;
        private Instant timestamp;
        private String path;
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationException(WebExchangeBindException ex, org.springframework.web.server.ServerWebExchange exchange) {
        String errors = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", errors, ex);
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed", errors, exchange);
    }
    
    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleResponseStatusException(ResponseStatusException ex, org.springframework.web.server.ServerWebExchange exchange) {
        log.warn("Response status exception: {}", ex.getReason(), ex);
        return createErrorResponse((HttpStatus) ex.getStatusCode(), ex.getReason(), ex.getReason(), exchange);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAccessDeniedException(AccessDeniedException ex, org.springframework.web.server.ServerWebExchange exchange) {
        log.warn("Access denied: {}", ex.getMessage());
        return createErrorResponse(HttpStatus.FORBIDDEN, "Access Denied", "You do not have permission to access this resource.", exchange);
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(Exception ex, org.springframework.web.server.ServerWebExchange exchange) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred.", exchange);
    }
    
    private Mono<ResponseEntity<ErrorResponse>> createErrorResponse(HttpStatus status, String error, String message, org.springframework.web.server.ServerWebExchange exchange) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(status.value())
                .error(error)
                .message(message)
                .timestamp(Instant.now())
                .path(exchange.getRequest().getPath().value())
                .build();
        return Mono.just(ResponseEntity.status(status).body(errorResponse));
    }
}