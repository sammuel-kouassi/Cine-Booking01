package com.example.cine_booking.dto;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingSummaryDto(
        Long bookingId,
        String movieTitle,
        String posterUrl,
        String cinemaName,     // Ex: "Salle A"
        LocalDateTime screeningTime,
        List<String> seats,    // Ex: ["A1", "A2"]
        BigDecimal totalPrice,
        String status
) {}
