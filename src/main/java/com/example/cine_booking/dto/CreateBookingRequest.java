package com.example.cine_booking.dto;

import java.util.List;

public record CreateBookingRequest(
        Long userId,
        Long screeningId,
        List<Long> seatIds
) {}