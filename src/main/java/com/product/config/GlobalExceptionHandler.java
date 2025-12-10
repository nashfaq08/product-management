package com.product.config;

import com.product.exception.ResourceNotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.security.SignatureException;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            BadCredentialsException.class,
            AccountStatusException.class,
            AccessDeniedException.class,
            SignatureException.class,
            ExpiredJwtException.class
    })
    public ResponseEntity<Map<String, Object>> handleSecurityExceptions(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error("Security exception: {}", exception.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();

        String description;
        HttpStatus status;

        if (exception instanceof BadCredentialsException) {
            status = HttpStatus.UNAUTHORIZED;
            description = "The username or password is incorrect";
        } else if (exception instanceof AccountStatusException) {
            status = HttpStatus.FORBIDDEN;
            description = "The account is locked";
        } else if (exception instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
            description = "You are not authorized to access this resource";
        } else if (exception instanceof SignatureException) {
            status = HttpStatus.FORBIDDEN;
            description = "Invalid JWT signature";
        } else if (exception instanceof ExpiredJwtException) {
            status = HttpStatus.FORBIDDEN;
            description = "JWT token has expired";
        } else {
            status = HttpStatus.FORBIDDEN;
            description = "Security error";
        }

        body.put("detail", exception.getMessage());
        body.put("description", description);
        body.put("instance", request.getRequestURI());

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of(
                "error", "Not Found",
                "message", ex.getMessage()
        ));
    }
}