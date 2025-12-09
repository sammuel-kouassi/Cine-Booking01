package com.example.cine_booking.repository;

import com.example.cine_booking.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findAllByCinemaHallId(Long hallId);
}