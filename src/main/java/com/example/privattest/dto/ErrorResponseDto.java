package com.example.privattest.dto;

public record ErrorResponseDto(
        int code,
        String reason
) {
}
