package com.example.cine_booking.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "BOOKING_SEATS")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    /**
     *
     * NOTE IMPORTANTE SUR LA CONCURRENCE :
     * * Pour empêcher la surréservation au niveau de la base de données,
     * il est difficile de mettre une contrainte UNIQUE ici car "Screening"
     * est accessible via "Booking".
     * * Solution Robustesse :
     * La logique métier devra faire une requête :
     * "SELECT count(bs) FROM BookingSeat bs WHERE bs.seat.id = : seatId
     * AND bs.booking.screening.id = : screeningId AND bs.booking.status != CANCELLED"
     * * Si le count > 0, on bloque.
     **/
}
