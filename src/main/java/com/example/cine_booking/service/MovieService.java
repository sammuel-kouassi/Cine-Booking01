package com.example.cine_booking.service;


import com.example.cine_booking.dto.MovieRequest;
import com.example.cine_booking.model.Movie;
import com.example.cine_booking.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    public Movie addMovie(MovieRequest request) {
        Movie movie = Movie.builder()
                .title(request.title())
                .description(request.description())
                .genre(request.genre())
                .durationMinutes(request.durationMinutes())
                .posterUrl(request.posterUrl())
                .build();
        return movieRepository.save(movie);
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }
}