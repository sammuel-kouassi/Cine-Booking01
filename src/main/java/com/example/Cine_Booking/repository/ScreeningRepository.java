package com.example.Cine_Booking.repository;

import com.example.Cine_Booking.model.Screening;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {
    List<Screening> findByStartTimeAfter(LocalDateTime time);
}
