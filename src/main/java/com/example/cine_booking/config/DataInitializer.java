package com.example.cine_booking.config;


import com.example.cine_booking.model.CinemaHall;
import com.example.cine_booking.model.Seat;
import com.example.cine_booking.model.User;
import com.example.cine_booking.model.enums.UserRole;
import com.example.cine_booking.repository.CinemaHallRepository;
import com.example.cine_booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CinemaHallRepository hallRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Créer l'ADMIN s'il n'existe pas
        if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {
            User admin = User.builder()
                    .fullName("Super Admin")
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("admin123")) // Mot de passe par défaut
                    .roles(Set.of(UserRole.ADMIN, UserRole.CUSTOMER))
                    .build();
            userRepository.save(admin);
            System.out.println(">>> ADMIN INITIAL CRÉÉ : admin@gmail.com / admin123");
        }

        // 2. Créer une Salle et ses Sièges (Infrastructure)
        if (hallRepository.count() == 0) {
            CinemaHall hallA = CinemaHall.builder().name("Salle IMAX").capacity(50).build();

            // Génération automatique de 5 rangées (A-E) de 10 sièges
            List<Seat> seats = new ArrayList<>();
            String[] rows = {"A", "B", "C", "D", "E"};
            for (String row : rows) {
                for (int i = 1; i <= 10; i++) {
                    seats.add(Seat.builder()
                            .rowCode(row)
                            .number(i)
                            .cinemaHall(hallA) // Lien parent
                            .build());
                }
            }
            hallA.setSeats(seats); // Le CascadeType.ALL va sauvegarder les sièges
            hallRepository.save(hallA);
            System.out.println(">>> SALLE 'IMAX' CRÉÉE avec 50 sièges.");
        }
    }
}