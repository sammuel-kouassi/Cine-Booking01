package com.example.cine_booking.controller;


import com.example.cine_booking.dto.MovieRequest;
import com.example.cine_booking.dto.RevenueReportDto;
import com.example.cine_booking.dto.ScreeningRequest;
import com.example.cine_booking.model.Movie;
import com.example.cine_booking.model.Screening;
import com.example.cine_booking.repository.BookingRepository;
import com.example.cine_booking.service.MovieService;
import com.example.cine_booking.service.ScreeningService;
import com.example.cine_booking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MovieService movieService;
    private final ScreeningService screeningService;
    private final UserService userService;
    private final BookingRepository bookingRepository;



    /// Seuls les ADMINS accèdent ici (défini dans SecurityConfig)

    @PostMapping("/movies")
    public ResponseEntity<Movie> addMovie(@RequestBody MovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movieService.addMovie(request));
    }

    @PostMapping("/screenings")
    public ResponseEntity<Screening> addScreening(@RequestBody ScreeningRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(screeningService.addScreening(request));
    }

    @PutMapping("/users/{id}/promote")
    public ResponseEntity<Void> promoteUser(@PathVariable Long id) {
        userService.promoteToAdmin(id);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/reports/revenue")
    public ResponseEntity<List<RevenueReportDto>> getRevenueReport() {
        return ResponseEntity.ok(bookingRepository.getRevenueByMovie());
    }
}