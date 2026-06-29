package com.biblioteca.user_service.controller;

import com.biblioteca.user_service.dto.UsuarioCreateDTO;
import com.biblioteca.user_service.dto.UsuarioUpdateDTO;
import com.biblioteca.user_service.model.UsuarioModelo;
import com.biblioteca.user_service.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Usuarios",
        description = "Endpoints para la gestión de usuarios del sistema."
)
@RestController
@RequestMapping("/api/users")
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    @Autowired
    private UsuarioService service;

    @Operation(
            summary = "Registrar un nuevo usuario",
            description = "Registra un nuevo usuario en el sistema."
    )
    @PostMapping
    public ResponseEntity<Object> registrar(@Valid @RequestBody UsuarioCreateDTO dto) {
        log.info("POST /api/users email={}", dto.getEmail());
        UsuarioModelo creado = service.crearUsuario(dto);
        return ResponseEntity.status(201).body(creado);
    }

    @Operation(
            summary = "Listar todos los usuarios",
            description = "Obtiene la lista completa de usuarios registrados."
    )
    @GetMapping
    public ResponseEntity<List<UsuarioModelo>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @Operation(
            summary = "Buscar usuario por correo electrónico",
            description = "Obtiene la información de un usuario utilizando su correo electrónico."
    )
    @GetMapping("/email/{email}")
    public ResponseEntity<Object> buscarPorEmail(
            @Parameter(description = "Correo electrónico del usuario", example = "diego@biblioteca.com")
            @PathVariable String email) {

        UsuarioModelo usuario = service.obtenerPorEmail(email);
        if (usuario == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }
        return ResponseEntity.ok(usuario);
    }

    @Operation(
            summary = "Buscar usuario por ID",
            description = "Obtiene la información de un usuario utilizando su identificador."
    )
    @GetMapping("/id/{id}")
    public ResponseEntity<Object> buscarPorId(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Long id) {

        UsuarioModelo usuario = service.obtenerPorId(id);
        if (usuario == null) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }
        return ResponseEntity.ok(usuario);
    }

    @Operation(
            summary = "Actualizar un usuario",
            description = "Actualiza los datos de un usuario existente."
    )
    @PutMapping("/{id}")
    public ResponseEntity<Object> actualizar(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDTO dto) {

        log.info("PUT /api/users/{}", id);
        UsuarioModelo actualizado = service.actualizarUsuario(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    @Operation(
            summary = "Eliminar un usuario",
            description = "Elimina un usuario del sistema."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> eliminar(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Long id) {

        log.info("DELETE /api/users/{}", id);
        service.eliminarUsuario(id);
        return ResponseEntity.ok("Usuario eliminado");
    }
}