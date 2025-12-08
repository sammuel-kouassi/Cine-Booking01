package com.example.cine_booking;


import com.example.cine_booking.dto.AuthResponse;
import com.example.cine_booking.dto.RegisterRequest;
import com.example.cine_booking.exception.BusinessException;
import com.example.cine_booking.model.User;
import com.example.cine_booking.repository.UserRepository;
import com.example.cine_booking.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder; // Nécessaire pour hasher

    @InjectMocks
    private AuthService authService; // N'existe pas encore

    @Test
    void should_RegisterUser_Successfully() {
        // --- GIVEN ---
        RegisterRequest request = new RegisterRequest("John Doe", "john@test.com", "password123");

        // Simuler que l'email n'existe pas encore
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        // Simuler le hash du mot de passe
        when(passwordEncoder.encode(request.password())).thenReturn("hashed_secret");

        // Simuler la sauvegarde (renvoie l'user sauvegardé)
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L); // Simule l'ID généré
            return u;
        });

        // --- WHEN ---
        AuthResponse response = authService.register(request);

        // --- THEN ---
        assertNotNull(response);
        assertEquals("john@test.com", response.email());

        // Vérification cruciale : on a bien appelé le password encoder
        verify(passwordEncoder).encode("password123");
        // Vérification cruciale : on a sauvegardé en base
        verify(userRepository).save(any(User.class));
    }

    @Test
    void should_ThrowException_When_EmailAlreadyExists() {
        // --- GIVEN ---
        RegisterRequest request = new RegisterRequest("John Doe", "exist@test.com", "pass");

        // Simuler que l'email existe DÉJÀ
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(new User()));

        // --- WHEN & THEN ---
        assertThrows(BusinessException.class, () -> authService.register(request));

        // On ne doit JAMAIS sauvegarder si l'email existe
        verify(userRepository, never()).save(any());
    }
}