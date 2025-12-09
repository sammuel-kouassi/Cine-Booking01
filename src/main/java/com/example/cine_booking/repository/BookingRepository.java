package com.example.cine_booking.repository;

import com.example.cine_booking.dto.RevenueReportDto;
import com.example.cine_booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser_EmailOrderByBookingTimeDesc(String email);
    @Query("""
        SELECT new com.example.cine_booking.dto.RevenueReportDto(
            b.screening.movie.title,
            COUNT(b),
            SUM(b.totalAmount)
        )
        FROM Booking b
        WHERE b.status = 'CONFIRMED'
        GROUP BY b.screening.movie.title
        ORDER BY SUM(b.totalAmount) DESC
    """)
    List<RevenueReportDto> getRevenueByMovie();
}
