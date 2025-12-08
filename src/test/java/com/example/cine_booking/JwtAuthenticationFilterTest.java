package com.example.cine_booking;


import com.example.cine_booking.config.JwtAuthenticationFilter;
import com.example.cine_booking.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private UserDetailsService userDetailsService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter; // On va créer cette classe

    @AfterEach
    void clearSecurityContext() {
        //// Ensure no cross-test contamination of the SecurityContext
        SecurityContextHolder.clearContext();
    }

    @Test
    void should_DoFilter_When_TokenIsValid() throws Exception {
        // GIVEN
        String token = "valid-token";
        String email = "test@test.com";
        UserDetails userDetails = new User(email, "pass", Collections.emptyList());

        // Simulation des headers HTTP
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        // Simulation du service JWT
        when(jwtService.extractUsername(token)).thenReturn(email);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

        // Simulation du chargement de l'utilisateur
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

        // WHEN
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // THEN
        // Vérifie qu'on continue la chaîne de filtres
        verify(filterChain).doFilter(request, response);
        // Vérifie qu'on a bien chargé l'utilisateur
        verify(userDetailsService).loadUserByUsername(email);
    }

    @Test
    void should_NotAuthenticate_When_NoToken() throws Exception {
        // GIVEN: Pas de header Authorization
        when(request.getHeader("Authorization")).thenReturn(null);

        // WHEN
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // THEN
        verify(jwtService, never()).extractUsername(any());
        verify(filterChain).doFilter(request, response);
        // Le contexte de sécurité doit rester vide
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
