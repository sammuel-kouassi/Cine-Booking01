package com.example.Cine_Booking.repository;

import com.example.Cine_Booking.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {}
