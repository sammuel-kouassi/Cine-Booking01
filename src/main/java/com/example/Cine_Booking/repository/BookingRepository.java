package com.example.Cine_Booking.repository;

import com.example.Cine_Booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {}
