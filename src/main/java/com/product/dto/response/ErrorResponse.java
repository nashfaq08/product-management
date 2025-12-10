package com.product.dto.response;

public record ErrorResponse(
        String error,
        String message
) {
    // 👇 This is the method your IDE says is missing
    public static ErrorResponse of(String error, String message) {
        return new ErrorResponse(
                error,
                message
        );
    }
}
