package com.example.cine_booking.dto;

public record MovieRequest(
        String title,
        String description,
        String genre,
        Integer durationMinutes,
        String posterUrl
) {}