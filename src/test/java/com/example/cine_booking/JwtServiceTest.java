package com.example.cine_booking;

import com.example.cine_booking.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(); // Pas de dépendances externes

    @Test
    void should_GenerateAndExtractUsername_Successfully() {
        // GIVEN
        UserDetails user = new User("test@email.com", "pass", Collections.emptyList());

        // WHEN
        String token = jwtService.generateToken(user);
        String extractedUsername = jwtService.extractUsername(token);

        // THEN
        assertNotNull(token);
        assertEquals("test@email.com", extractedUsername);
        assertTrue(jwtService.isTokenValid(token, user));
    }

}
