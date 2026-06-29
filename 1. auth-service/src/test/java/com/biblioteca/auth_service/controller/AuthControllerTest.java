package com.biblioteca.auth_service.controller;

import com.biblioteca.auth_service.dto.LoginDTO;
import com.biblioteca.auth_service.exception.GlobalExceptionHandler;
import com.biblioteca.auth_service.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController - tests unitarios (MockMvc standalone)")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/auth/login devuelve 200 y el token")
    void loginJson_ok() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("diego@biblioteca.com");
        dto.setPassword("secret");
        when(authService.login("diego@biblioteca.com", "secret")).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    @DisplayName("POST /api/auth/login con email invalido devuelve 400")
    void loginJson_invalido() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("no-es-email");
        dto.setPassword("x"); // demasiado corta

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login con credenciales invalidas devuelve 401")
    void loginJson_credencialesInvalidas() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("diego@biblioteca.com");
        dto.setPassword("wrong");
        when(authService.login(anyString(), anyString()))
                .thenThrow(new RuntimeException("Credenciales invalidas"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login-form devuelve 200 y el token")
    void loginForm_ok() throws Exception {
        when(authService.login("diego@biblioteca.com", "secret")).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/login-form")
                        .param("email", "diego@biblioteca.com")
                        .param("password", "secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }
}
