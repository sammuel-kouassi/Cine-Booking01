package com.example.cine_booking;


import com.example.cine_booking.dto.MovieRequest;
import com.example.cine_booking.model.Movie;
import com.example.cine_booking.repository.MovieRepository;
import com.example.cine_booking.service.MovieService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock private MovieRepository movieRepository;
    @InjectMocks private MovieService movieService;

    @Test
    void should_AddMovie_Successfully() {
        // GIVEN
        MovieRequest request = new MovieRequest("Inception", "Sci-Fi", "Action", 148, "url");

        when(movieRepository.save(any(Movie.class))).thenAnswer(i -> {
            Movie m = i.getArgument(0);
            m.setId(1L);
            return m;
        });

        // WHEN
        Movie result = movieService.addMovie(request);

        // THEN
        assertNotNull(result.getId());
        assertEquals("Inception", result.getTitle());
        verify(movieRepository).save(any(Movie.class));
    }
}
