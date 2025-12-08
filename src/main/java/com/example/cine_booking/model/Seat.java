package com.example.cine_booking.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "SEATS", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"hall_id", "row_code", "seat_number"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "row_code", nullable = false)
    private String rowCode; // Ex: "A"

    @Column(name = "seat_number", nullable = false)
    private Integer number; // Ex: 1

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private CinemaHall cinemaHall;
}
