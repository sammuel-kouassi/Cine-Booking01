package com.example.cine_booking.service;

import com.example.cine_booking.dto.AuthResponse;
import com.example.cine_booking.dto.LoginRequest;
import com.example.cine_booking.dto.RegisterRequest;
import com.example.cine_booking.exception.BusinessException;
import com.example.cine_booking.model.User;
import com.example.cine_booking.model.enums.UserRole;
import com.example.cine_booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        /// 1. Validation de l'email unique
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessException("Cet email est déjà utilisé.");
        }

        /// 2. Création de l'utilisateur
        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password())) // HACHAGE IMPORTANT
                .roles(Set.of(UserRole.CUSTOMER)) // Rôle par défaut
                .build();

        /// 3. Sauvegarde
        userRepository.save(user);

        /// 4. Retour (On gérera le Token JWT à la prochaine étape)
        return new AuthResponse(
                null, // Pas de token pour l'instant
                user.getEmail(),
                UserRole.CUSTOMER.name()
        );
    }

    public AuthResponse login(LoginRequest request) {
        /// 1. Authentification via Spring Security (vérifie le mot de passe)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        /// 2. Si on arrive ici, c'est que le mot de passe est bon. On récupère l'user.
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Utilisateur inconnu"));

        /// 3. Génération du Token
        var jwtToken = jwtService.generateToken(user); // User doit implémenter UserDetails !

        return new AuthResponse(
                jwtToken,
                user.getEmail(),
                user.getRoles().iterator().next().name()
        );
    }
}
