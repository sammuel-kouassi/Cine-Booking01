package com.example.cine_booking.service;


import com.example.cine_booking.dto.ScreeningRequest;
import com.example.cine_booking.dto.SeatDto;
import com.example.cine_booking.exception.BusinessException;
import com.example.cine_booking.model.CinemaHall;
import com.example.cine_booking.model.Movie;
import com.example.cine_booking.model.Screening;
import com.example.cine_booking.model.Seat;
import com.example.cine_booking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final CinemaHallRepository hallRepository;
    private final SeatRepository seatRepository;
    private final BookingSeatRepository bookingSeatRepository;

    @Transactional
    public Screening addScreening(ScreeningRequest request) {
        // 1. Récupérer les entités
        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> new BusinessException("Film introuvable"));

        CinemaHall hall = hallRepository.findById(request.hallId())
                .orElseThrow(() -> new BusinessException("Salle introuvable"));

        // 2. Calculer la fin de la nouvelle séance
        LocalDateTime newStart = request.startTime();
        LocalDateTime newEnd = newStart.plusMinutes(movie.getDurationMinutes());

        // 3. Vérifier les conflits (On cherche +/- 4 heures autour pour être sûr)
        // Note : C'est une vérification simple. Pour une prod, faire une requête SQL native précise.
        // Use the hallId from the request to avoid relying on a possibly uninitialized entity ID in tests
        List<Screening> potentialConflicts = screeningRepository.findScreeningsInInterval(
                request.hallId(),
                newStart.minusHours(4),
                newEnd.plusHours(4)
        );

        for (Screening s : potentialConflicts) {
            LocalDateTime existingStart = s.getStartTime();
            LocalDateTime existingEnd = existingStart.plusMinutes(s.getMovie().getDurationMinutes());

            // Logique de chevauchement : (StartA < EndB) and (EndA > StartB)
            if (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)) {
                throw new BusinessException("Cette séance chevauche une séance existante dans la salle " + hall.getName());
            }
        }

        // 4. Sauvegarder
        Screening screening = Screening.builder()
                .movie(movie)
                .cinemaHall(hall)
                .startTime(request.startTime())
                .price(request.price())
                .build();

        return screeningRepository.save(screening);
    }

    public List<SeatDto> getScreeningSeats(Long screeningId) {
        // 1. Récupérer la séance pour connaître la salle
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new BusinessException("Séance introuvable"));

        // 2. Récupérer TOUS les sièges de la salle (Structure physique)
        List<Seat> allSeats = seatRepository.findAllByCinemaHallId(screening.getCinemaHall().getId());

        // 3. Récupérer les IDs des sièges réservés (État actuel)
        List<Long> reservedSeatIds = bookingSeatRepository.findReservedSeatIdsByScreeningId(screeningId);

        // 4. Fusionner les deux listes (Mapping)
        return allSeats.stream()
                .map(seat -> {
                    boolean isReserved = reservedSeatIds.contains(seat.getId());
                    return new SeatDto(
                            seat.getId(),
                            seat.getRowCode(),
                            seat.getNumber(),
                            isReserved ? "BOOKED" : "AVAILABLE"
                    );
                })
                .toList();
    }

}
