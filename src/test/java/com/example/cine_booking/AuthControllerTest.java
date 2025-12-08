package com.example.cine_booking;


import com.example.cine_booking.controller.AuthController;
import com.example.cine_booking.dto.AuthResponse;
import com.example.cine_booking.dto.LoginRequest;
import com.example.cine_booking.dto.RegisterRequest;
import com.example.cine_booking.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private AuthService authService; // Utilisation de MockitoBean (Spring 3.4)
    @Autowired private ObjectMapper objectMapper;

    @Test
    void should_Register_Successfully() throws Exception {
        RegisterRequest request = new RegisterRequest("John", "j@test.com", "pass");
        AuthResponse response = new AuthResponse("token", "j@test.com", "CUSTOMER");

        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    void should_Login_Successfully() throws Exception {
        LoginRequest request = new LoginRequest("j@test.com", "pass");
        AuthResponse response = new AuthResponse("jwt-token-xyz", "j@test.com", "CUSTOMER");

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-xyz"));
    }
}
