package com.example.cine_booking.controller;


import com.example.cine_booking.dto.BookingResponse;
import com.example.cine_booking.dto.BookingSummaryDto;
import com.example.cine_booking.dto.CreateBookingRequest;
import com.example.cine_booking.model.Booking;
import com.example.cine_booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody CreateBookingRequest request) {

        // 1. Appel du Service (Logique métier)
        Booking booking = bookingService.createBooking(
                request.userId(),
                request.screeningId(),
                request.seatIds()
        );

        // 2. Mapping Entité → DTO
        // (Dans un vrai projet, on utiliserait MapStruct ou un Mapper dédié)
        BookingResponse response = new BookingResponse(
                booking.getId(),
                booking.getStatus().name(),
                booking.getTotalAmount(),
                booking.getSeats() != null ? booking.getSeats().size() : 0,
                booking.getBookingTime()
        );

        // 3. Retour de la réponse HTTP 201
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<BookingSummaryDto>> getMyBookings(java.security.Principal principal) {
        // principal.getName() retourne l'email extrait du Token JWT
        return ResponseEntity.ok(bookingService.getMyBookings(principal.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id, java.security.Principal principal) {
        bookingService.cancelBooking(id, principal.getName());
        return ResponseEntity.noContent().build(); // Retourne 204 No Content
    }
}