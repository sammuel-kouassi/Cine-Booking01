package com.example.cine_booking.controller;


import com.example.cine_booking.dto.SeatDto;
import com.example.cine_booking.service.ScreeningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screenings")
@RequiredArgsConstructor
public class ScreeningController {

    private final ScreeningService screeningService;

    /**
     * GET /api/screenings/{id}/seats
     * Accessible par tout le monde (authentifié ou non, selon votre choix business)
     * Ici, nous supposons qu'il faut être authentifié (défaut SecurityConfig)
     */
    @GetMapping("/{id}/seats")
    public ResponseEntity<List<SeatDto>> getScreeningSeats(@PathVariable Long id) {
        return ResponseEntity.ok(screeningService.getScreeningSeats(id));
    }
}