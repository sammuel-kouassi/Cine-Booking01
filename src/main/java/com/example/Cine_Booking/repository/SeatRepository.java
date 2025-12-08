package com.example.Cine_Booking.repository;

import com.example.Cine_Booking.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {}