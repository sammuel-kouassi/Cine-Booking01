package com.example.cine_booking.repository;

import com.example.cine_booking.model.Screening;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScreeningRepository extends JpaRepository<Screening, Long> {
    List<Screening> findByStartTimeAfter(LocalDateTime time);
}
