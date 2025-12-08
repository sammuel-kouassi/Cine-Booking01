package com.example.cine_booking.dto;

public record AuthResponse(
        String token,
        String email,
        String role
) {}
