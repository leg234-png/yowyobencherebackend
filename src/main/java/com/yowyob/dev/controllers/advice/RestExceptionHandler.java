package com.yowyob.dev.controllers.advice;

import com.yowyob.dev.dto.responseDTO.ApiError;
import com.yowyob.dev.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.stream.Collectors;

import com.yowyob.dev.exceptions.InvalidRequestException;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public Mono<ResponseEntity<ApiError>> handleNotFoundException(NotFoundException ex) {
        ApiError error = ApiError.builder()
                .value(String.valueOf(HttpStatus.NOT_FOUND.value()))
                .text(ex.getMessage())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiError>> handleValidationException(WebExchangeBindException ex) {
        String errors = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));

        ApiError error = ApiError.builder()
                .value(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                .text("Validation failed: " + errors)
                .build();
        return Mono.just(ResponseEntity.badRequest().body(error));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiError>> handleGenericException(Exception ex) {
        ApiError error = ApiError.builder()
                .value(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .text("An unexpected error occurred: " + ex.getMessage())
                .build();
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public Mono<ApiError> handleInvalidRequest(InvalidRequestException ex) {
        log.warn("Invalid request: {}", ex.getMessage());
        return Mono.just(ApiError.builder()
                .value(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                .text(ex.getMessage())
                .build());
    }

    @ExceptionHandler(JsonProcessingException.class)
    public Mono<ApiError> handleJsonProcessing(JsonProcessingException ex) {
        log.warn("JSON parsing error: {}", ex.getMessage());
        return Mono.just(ApiError.builder()
                .value(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                .text("Invalid JSON format in request")
                .build());
    }
}
