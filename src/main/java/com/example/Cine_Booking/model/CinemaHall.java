package com.example.Cine_Booking.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "HALLS")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CinemaHall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private Integer capacity;

    // Relation inverse : Une salle a plusieurs sièges
    // Cascade ALL : si on supprime la salle, on supprime les sièges
    @OneToMany(mappedBy = "cinemaHall", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seat> seats = new ArrayList<>();
}