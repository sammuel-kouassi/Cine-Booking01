package com.example.cine_booking;


import com.example.cine_booking.dto.ScreeningRequest;
import com.example.cine_booking.dto.SeatDto;
import com.example.cine_booking.exception.BusinessException;
import com.example.cine_booking.model.CinemaHall;
import com.example.cine_booking.model.Movie;
import com.example.cine_booking.model.Screening;
import com.example.cine_booking.model.Seat;
import com.example.cine_booking.repository.*;
import com.example.cine_booking.service.ScreeningService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScreeningServiceTest {

    @Mock private ScreeningRepository screeningRepository;
    @Mock private MovieRepository movieRepository;
    @Mock private CinemaHallRepository hallRepository;

    @InjectMocks private ScreeningService screeningService;

    /**
     * Verifies that the {@code addScreening} method in {@link ScreeningService} throws
     * a {@link BusinessException} when attempting to add a screening that overlaps
     * with an existing screening in the same cinema hall.
     * The method simulates:
     * <ul>
     *     <li>A new screening request for a movie starting at a specific time.</li>
     *     <li>An existing screening in the database that overlaps with the new screening's time interval.</li>
     * </ul>
     *
     * The test ensures that:
     * <ul>
     *     <li>The {@link ScreeningRepository#findScreeningsInInterval} method identifies overlapping screenings.</li>
     *     <li>A {@link BusinessException} is thrown when an overlap is detected, containing a message indicating the conflict.</li>
     *     <li>The {@link ScreeningRepository#save} method is never invoked when conflicts are present.</li>
     * </ul>
     *
     * This test checks adherence to business rules preventing scheduling conflicts
     * for screenings within the same cinema hall.
     */
    @Test
    void should_ThrowException_When_ScreeningOverlaps() {
        // GIVEN
        LocalDateTime newStart = LocalDateTime.of(2025, 1, 1, 20, 0); // 20h00
        // Film de 2h (donc finit à 22h00)
        Movie newMovie = Movie.builder().id(1L).durationMinutes(120).build();

        ScreeningRequest request = new ScreeningRequest(1L, 10L, newStart, BigDecimal.TEN);

        /*
         Simulation DB : Il existe DÉJÀ une séance qui commence à 19h00
         Avec un film de 2h (donc finit à 21h00) → CONFLIT entre 20h et 21h
        */

        Movie existingMovie = Movie.builder().durationMinutes(120).build();
        Screening existingScreening = Screening.builder()
                .startTime(LocalDateTime.of(2025, 1, 1, 19, 0))
                .movie(existingMovie)
                .build();

        when(movieRepository.findById(1L)).thenReturn(Optional.of(newMovie));
        when(hallRepository.findById(10L)).thenReturn(Optional.of(new CinemaHall()));

        // Le repo renvoie la séance existante
        when(screeningRepository.findScreeningsInInterval(anyLong(), any(), any()))
                .thenReturn(List.of(existingScreening));

        // WHEN & THEN
        BusinessException ex = assertThrows(BusinessException.class, () ->
                screeningService.addScreening(request)
        );
        assertTrue(ex.getMessage().contains("chevauche"));
        verify(screeningRepository, never()).save(any());
    }

    @Mock private BookingSeatRepository bookingSeatRepository; // À ajouter
    @Mock private SeatRepository seatRepository; // À ajouter si pas déjà là

    @Test
    void should_GetScreeningSeats_WithCorrectStatus() {
        // --- GIVEN ---
        Long screeningId = 100L;
        Long hallId = 50L;

        // 1. Configuration de la séance et de la salle
        CinemaHall hall = CinemaHall.builder().id(hallId).build();
        Screening screening = Screening.builder().id(screeningId).cinemaHall(hall).build();

        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));

        // 2. Configuration des sièges physiques (La salle a 2 sièges)
        Seat seat1 = Seat.builder().id(10L).rowCode("A").number(1).build();
        Seat seat2 = Seat.builder().id(11L).rowCode("A").number(2).build();

        // Simuler la récupération des sièges de cette salle
        when(seatRepository.findAllByCinemaHallId(hallId)).thenReturn(List.of(seat1, seat2));

        // 3. Configuration des réservations (Le siège 10 est DÉJÀ pris)
        when(bookingSeatRepository.findReservedSeatIdsByScreeningId(screeningId))
                .thenReturn(List.of(10L));

        // --- WHEN ---
        List<SeatDto> result = screeningService.getScreeningSeats(screeningId);

        // --- THEN ---
        assertEquals(2, result.size());

        // Le siège 10 doit être BOOKED
        SeatDto dto1 = result.stream().filter(s -> s.id().equals(10L)).findFirst().orElseThrow();
        assertEquals("BOOKED", dto1.status());

        // Le siège 11 doit être AVAILABLE
        SeatDto dto2 = result.stream().filter(s -> s.id().equals(11L)).findFirst().orElseThrow();
        assertEquals("AVAILABLE", dto2.status());
    }
}
