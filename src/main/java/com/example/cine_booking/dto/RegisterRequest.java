package com.example.cine_booking.dto;

public record RegisterRequest(
        String fullName,
        String email,
        String password
) {}
