package com.biblioteca.auth_service.controller;

import com.biblioteca.auth_service.dto.LoginDTO;
import com.biblioteca.auth_service.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;


@Tag(
    name = "Autenticación",
    description = "Endpoints para autenticación y generación de tokens JWT"
)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Operation(
        summary = "Iniciar sesión",
        description = "Autentica un usuario mediante un cuerpo JSON y devuelve un token JWT."
    )
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginJson(@Valid @RequestBody LoginDTO dto) {
        log.info("POST /api/auth/login (json) email={}", dto.getEmail());
        String token = authService.login(dto.getEmail(), dto.getPassword());
        return ResponseEntity.ok(Map.of("token", token));
    }
    @Operation(
        summary = "Iniciar sesión (formulario)",
        description = "Autentica un usuario mediante parámetros de formulario y devuelve un token JWT."
    )
    @PostMapping("/login-form")
    public ResponseEntity<Map<String, Object>> loginForm(@RequestParam String email,
                                                         @RequestParam String password) {
        log.info("POST /api/auth/login-form email={}", email);
        String token = authService.login(email, password);
        return ResponseEntity.ok(Map.of("token", token));
    }
}
