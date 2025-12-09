package com.example.cine_booking;


import com.example.cine_booking.dto.RevenueReportDto;
import com.example.cine_booking.model.*;
import com.example.cine_booking.model.enums.BookingStatus;
import com.example.cine_booking.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired private BookingRepository bookingRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private ScreeningRepository screeningRepository;
    @Autowired private CinemaHallRepository hallRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void should_CalculateRevenue_PerMovie() {
        // 1. Setup Data
        Movie avatar = movieRepository.save(Movie.builder().title("Avatar").description("Blue").durationMinutes(180).build());
        CinemaHall hall = hallRepository.save(CinemaHall.builder().name("A").build());
        User user = userRepository.save(User.builder().email("u@u.com").password("p").fullName("n").build());

        Screening s1 = screeningRepository.save(Screening.builder()
                .movie(avatar).cinemaHall(hall).startTime(LocalDateTime.now()).price(BigDecimal.TEN).build());

        // 2 réservations confirmées à 10€ = 20€
        createBooking(user, s1, BookingStatus.CONFIRMED);
        createBooking(user, s1, BookingStatus.CONFIRMED);
        // 1 annulée (ne doit pas compter)
        createBooking(user, s1, BookingStatus.CANCELLED);

        // 2. Execute Query
        List<RevenueReportDto> report = bookingRepository.getRevenueByMovie();

        // 3. Verify
        assertEquals(1, report.size());
        assertEquals("Avatar", report.getFirst().movieTitle());
        assertEquals(2L, report.getFirst().ticketsSold()); // 2 tickets valides
        assertEquals(new BigDecimal("20.00"), report.getFirst().totalRevenue());
    }

    private void createBooking(User u, Screening s, BookingStatus status) {
        bookingRepository.save(Booking.builder()
                .user(u).screening(s).status(status).totalAmount(BigDecimal.TEN).bookingTime(LocalDateTime.now()).build());
    }
}
