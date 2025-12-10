package com.product.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class AuthEntryPointJWT implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        log.warn("Unauthorized request: {} {}", request.getMethod(), request.getRequestURI());

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", 401);
        errorDetails.put("error", "Unauthorized");
        errorDetails.put("message", getErrorMessage(authException));

        new ObjectMapper().writeValue(response.getOutputStream(), errorDetails);
    }

    private String getErrorMessage(AuthenticationException ex) {
        String msg = ex.getMessage();

        if (msg.contains("JWT expired") || msg.contains("expired")) {
            return "Access token has expired";
        }
        if (msg.contains("invalid") || msg.contains("malformed")) {
            return "Invalid access token";
        }
        if (msg.contains("Full authentication is required")) {
            return "Authentication token missing";
        }

        return "Unauthorized: " + msg;
    }
}