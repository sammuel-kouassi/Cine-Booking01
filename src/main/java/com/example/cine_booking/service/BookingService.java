package com.example.cine_booking.service;



import com.example.cine_booking.dto.BookingSummaryDto;
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

    // ... imports et méthode createBooking existante

    // Lecture des réservations
    public List<BookingSummaryDto> getMyBookings(String userEmail) {
        return bookingRepository.findByUser_EmailOrderByBookingTimeDesc(userEmail)
                .stream()
                .map(this::convertToSummaryDto)
                .toList();
    }

    // Annulation
    @Transactional
    public void cancelBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Réservation introuvable"));

        // 1. Vérification de la propriété (Sécurité)
        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException("Vous n'êtes pas autorisé à annuler cette réservation");
        }

        // 2. Vérification du statut actuel
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Cette réservation est déjà annulée");
        }

        // 3. Vérification de l'heure (Règle Métier)
        if (booking.getScreening().getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Il est trop tard pour annuler, la séance a commencé/est passée");
        }

        // 4. Action
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Note : Grâce à la logique dans getScreeningSeats(),
        // les sièges de cette réservation redeviendront automatiquement "AVAILABLE"
        // car la requête SQL filtre sur status != CANCELLED.
    }

    // Mapper utilitaire (Entity -> DTO)
    private BookingSummaryDto convertToSummaryDto(Booking booking) {
        // Transformation de la liste des objets Seats en liste de Strings "A1", "A2"
        List<String> seatLabels = booking.getSeats().stream()
                .map(bs -> bs.getSeat().getRowCode() + bs.getSeat().getNumber())
                .sorted()
                .toList();

        return new BookingSummaryDto(
                booking.getId(),
                booking.getScreening().getMovie().getTitle(),
                booking.getScreening().getMovie().getPosterUrl(),
                booking.getScreening().getCinemaHall().getName(),
                booking.getScreening().getStartTime(),
                seatLabels,
                booking.getTotalAmount(),
                booking.getStatus().name()
        );
    }
}
