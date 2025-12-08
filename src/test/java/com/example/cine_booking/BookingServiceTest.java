package com.example.cine_booking;


import com.example.cine_booking.exception.BusinessException;
import com.example.cine_booking.model.Booking;
import com.example.cine_booking.model.Screening;
import com.example.cine_booking.model.Seat;
import com.example.cine_booking.model.User;
import com.example.cine_booking.model.enums.BookingStatus;
import com.example.cine_booking.repository.*;
import com.example.cine_booking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private ScreeningRepository screeningRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private UserRepository userRepository;
    @Mock private BookingSeatRepository bookingSeatRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void should_CreateBooking_When_SeatsAreAvailable() {
        /// --- GIVEN (Préparation des données) ---
        Long userId = 1L;
        Long screeningId = 100L;
        List<Long> seatIds = Arrays.asList(10L, 11L);
        BigDecimal pricePerSeat = new BigDecimal("15.00");

        /// Mock User
        User user = User.builder().id(userId).email("client@test.com").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        /// Mock Screening
        Screening screening = Screening.builder()
                .id(screeningId)
                .price(pricePerSeat)
                .startTime(LocalDateTime.now().plusDays(1))
                .build();
        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(screening));

        /// Mock Seats (Les sièges existent physiquement)
        Seat seat1 = Seat.builder().id(10L).build();
        Seat seat2 = Seat.builder().id(11L).build();
        when(seatRepository.findAllById(seatIds)).thenReturn(Arrays.asList(seat1, seat2));

        /// Mock Availability (CRITIQUE : La liste des sièges occupés est vide)
        when(bookingSeatRepository.findReservedSeatIdsByScreeningId(screeningId))
                .thenReturn(List.of()); // Aucun siège n'est pris.

        /// Mock Save (Retourne l'objet qu'on lui donne)
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(555L); // Simule l'ID généré par la DB
            return b;
        });

        /// --- WHEN (Action) ---
        Booking result = bookingService.createBooking(userId, screeningId, seatIds);

        /// --- THEN (Vérifications) ---
        assertNotNull(result);
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
        assertEquals(2, result.getSeats().size()); // Vérifie qu'il y a 2 BookingSeats

        // Vérification du calcul du prix : 15.00 * 2 = 30.00
        assertEquals(new BigDecimal("30.00"), result.getTotalAmount());

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void should_ThrowException_When_SeatIsTaken() {
        // --- GIVEN (Préparation du scénario d'échec) ---
        Long userId = 1L;
        Long screeningId = 100L;
        Long seatIdDejaPris = 10L;
        Long seatIdLibre = 11L;
        List<Long> requestedSeatIds = Arrays.asList(seatIdDejaPris, seatIdLibre);

        // 1. Mock User & Screening (« Ils doivent exister pour arriver à l'étape de vérification)
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(screeningRepository.findById(screeningId)).thenReturn(Optional.of(new Screening()));

        // 2. Mock Availability (C'est ici qu'on piège le service)
        // On simule que la base de données renvoie l'ID 10 comme étant déjà occupé
        when(bookingSeatRepository.findReservedSeatIdsByScreeningId(screeningId))
                .thenReturn(List.of(seatIdDejaPris));

        // --- WHEN & THEN (Action & Vérification de l'exception) ---

        // On s'attend à ce que le code lance une BusinessException
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            bookingService.createBooking(userId, screeningId, requestedSeatIds);
        });

        // Vérification du message d'erreur (optionnel, mais recommandé)
        assertTrue(exception.getMessage().contains("déjà réservé"));

        // --- VERIFICATIONS CRITIQUES ---

        // On vérifie que le service n'a JAMAIS appelé la sauvegarde.
        // C'est vital : on ne veut pas de réservation corrompue en base.
        verify(bookingRepository, never()).save(any());
    }
}
