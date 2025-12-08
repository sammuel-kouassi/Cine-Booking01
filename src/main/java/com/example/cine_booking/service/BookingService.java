package com.example.cine_booking.service;



import com.example.cine_booking.exception.BusinessException;
import com.example.cine_booking.model.*;
import com.example.cine_booking.model.enums.BookingStatus;
import com.example.cine_booking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ScreeningRepository screeningRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final BookingSeatRepository bookingSeatRepository;

    /**
     * Méthode principale transactionnelle.
     * Si une erreur survient (ex : siège pris), tout est annulé (Rollback).
     */
    @Transactional
    public Booking createBooking(Long userId, Long screeningId, List<Long> seatIds) {

        ///  1. Récupération des entités
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new BusinessException("Séance non trouvée"));

        ///   2. Vérification de la disponibilité (Logique Métier Critique)
        List<Long> occupiedSeatIds = bookingSeatRepository.findReservedSeatIdsByScreeningId(screeningId);

        for (Long requestedSeatId : seatIds) {
            if (occupiedSeatIds.contains(requestedSeatId)) {
                throw new BusinessException("Le siège " + requestedSeatId + " est déjà réservé.");
            }
        }

        ///  3. Récupération des objets Sièges
        List<Seat> seats = seatRepository.findAllById(seatIds);
        if (seats.size() != seatIds.size()) {
            throw new BusinessException("Certains IDs de sièges sont invalides");
        }

        ///  4. Création de la Réservation
        BigDecimal totalAmount = screening.getPrice().multiply(BigDecimal.valueOf(seats.size()));

        Booking booking = Booking.builder()
                .user(user)
                .screening(screening)
                .bookingTime(LocalDateTime.now())
                .status(BookingStatus.CONFIRMED)
                .totalAmount(totalAmount)
                .seats(new HashSet<>()) // On initialise la collection
                .build();

        ///  5. Création des liens BookingSeat (Places)
        for (Seat seat : seats) {
            BookingSeat bookingSeat = BookingSeat.builder()
                    .booking(booking)
                    .seat(seat)
                    .build();
            booking.addSeat(bookingSeat); // Méthode helper dans l'entité Booking
        }

        ///  6. Sauvegarde (Cascade va sauvegarder les BookingSeats aussi)
        return bookingRepository.save(booking);
    }
}
