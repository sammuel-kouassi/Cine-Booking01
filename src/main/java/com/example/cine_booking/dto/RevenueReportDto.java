package com.example.cine_booking.dto;

import java.math.BigDecimal;

// Une "Projection" qui servira à stocker le résultat de la requête SQL
public record RevenueReportDto(
        String movieTitle,
        Long ticketsSold,
        BigDecimal totalRevenue
) {}
