package com.example.cine_booking.service;


import com.example.cine_booking.dto.ScreeningRequest;
import com.example.cine_booking.exception.BusinessException;
import com.example.cine_booking.model.CinemaHall;
import com.example.cine_booking.model.Movie;
import com.example.cine_booking.model.Screening;
import com.example.cine_booking.repository.CinemaHallRepository;
import com.example.cine_booking.repository.MovieRepository;
import com.example.cine_booking.repository.ScreeningRepository;
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
}
