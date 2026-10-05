package dev.lpcsontos.dashboard.config;

import dev.lpcsontos.dashboard.modules.auth.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<Object> build(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(
                Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "status", status.value(),
                        "error", error,
                        "message", message
                )
        );
    }

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<Object> handleEmailUsed(EmailAlreadyUsedException ex) {
        return build(HttpStatus.BAD_REQUEST, "EMAIL_ALREADY_USED", ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFound(UserNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Object> handleInvalidToken(InvalidTokenException ex) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", ex.getMessage());
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Object> handleExpiredToken(TokenExpiredException ex) {
        return build(HttpStatus.BAD_REQUEST, "TOKEN_EXPIRED", ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password");
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Object> handleDisabled(DisabledException ex) {
        return build(HttpStatus.FORBIDDEN, "ACCOUNT_NOT_VERIFIED", "Please verify your email address first");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);   // a részletek a szerver logjába mennek
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected error");
    }
}