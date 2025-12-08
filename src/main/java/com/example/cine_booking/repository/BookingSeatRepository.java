package com.example.cine_booking.repository;

import com.example.cine_booking.model.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {


    @Query("SELECT bs.seat.id FROM BookingSeat bs " +
            "WHERE bs.booking.screening.id = :screeningId " +
            "AND bs.booking.status <> 'CANCELLED'")
    List<Long> findReservedSeatIdsByScreeningId(@Param("screeningId") Long screeningId);
}