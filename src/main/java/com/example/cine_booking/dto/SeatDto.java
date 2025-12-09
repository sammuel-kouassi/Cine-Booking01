package com.example.cine_booking.dto;

public record SeatDto(
        Long id,
        String row,
        Integer number,
        String status // "AVAILABLE" ou "BOOKED"
) {}