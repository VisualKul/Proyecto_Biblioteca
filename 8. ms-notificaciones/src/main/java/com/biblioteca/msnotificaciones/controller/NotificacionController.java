package com.biblioteca.msnotificaciones.controller;

import com.biblioteca.msnotificaciones.dto.NotificacionRequestDTO;
import com.biblioteca.msnotificaciones.dto.NotificacionResponseDTO;
import com.biblioteca.msnotificaciones.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Notificaciones",
        description = "Endpoints para la gestión de notificaciones del sistema."
)
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@Slf4j
public class NotificacionController {

    private final NotificacionService service;

    @Operation(
            summary = "Crear notificación",
            description = "Crea una nueva notificación manual en el sistema."
    )
    @PostMapping
    public ResponseEntity<NotificacionResponseDTO> crear(
            @Valid @RequestBody NotificacionRequestDTO dto) {

        log.info("POST /api/notificaciones");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearNotificacion(dto));
    }

    @Operation(
            summary = "Notificar vencimiento de préstamo",
            description = "Genera una notificación automática asociada al vencimiento de un préstamo."
    )
    @PostMapping("/vencimiento/{prestamoId}")
    public ResponseEntity<NotificacionResponseDTO> notificarVencimiento(
            @Parameter(description = "ID del préstamo", example = "10")
            @PathVariable Long prestamoId) {

        log.info("POST /api/notificaciones/vencimiento/{}", prestamoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.notificarVencimiento(prestamoId));
    }

    @Operation(
            summary = "Listar notificaciones",
            description = "Obtiene todas las notificaciones registradas en el sistema."
    )
    @GetMapping
    public ResponseEntity<List<NotificacionResponseDTO>> listar() {
        return ResponseEntity.ok(service.listarNotificaciones());
    }

    @Operation(
            summary = "Buscar notificación por ID",
            description = "Obtiene una notificación específica usando su identificador."
    )
    @GetMapping("/{id}")
    public ResponseEntity<NotificacionResponseDTO> buscarPorId(
            @Parameter(description = "ID de la notificación", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @Operation(
            summary = "Listar notificaciones por usuario",
            description = "Obtiene todas las notificaciones asociadas al correo de un usuario."
    )
    @GetMapping("/usuario/{email}")
    public ResponseEntity<List<NotificacionResponseDTO>> listarPorUsuario(
            @Parameter(description = "Correo electrónico del usuario", example = "diego@biblioteca.com")
            @PathVariable String email) {

        return ResponseEntity.ok(service.listarPorUsuario(email));
    }

    @Operation(
            summary = "Marcar notificación como enviada",
            description = "Actualiza el estado de una notificación a enviada."
    )
    @PutMapping("/{id}/enviar")
    public ResponseEntity<NotificacionResponseDTO> marcarEnviada(
            @Parameter(description = "ID de la notificación", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(service.marcarEnviada(id));
    }

    @Operation(
            summary = "Eliminar notificación",
            description = "Elimina una notificación del sistema."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la notificación", example = "1")
            @PathVariable Long id) {

        service.eliminarNotificacion(id);
        return ResponseEntity.noContent().build();
    }
}