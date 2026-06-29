package com.biblioteca.msvaloraciones.controller;

import com.biblioteca.msvaloraciones.dto.ValoracionRequestDTO;
import com.biblioteca.msvaloraciones.dto.ValoracionResponseDTO;
import com.biblioteca.msvaloraciones.service.ValoracionService;
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
import java.util.Map;

@Tag(
        name = "Valoraciones",
        description = "Endpoints para la gestión de valoraciones de libros por usuarios."
)
@RestController
@RequestMapping("/api/valoraciones")
@RequiredArgsConstructor
@Slf4j
public class ValoracionController {

    private final ValoracionService service;

    @Operation(
            summary = "Crear valoración",
            description = "Permite crear una nueva valoración de un libro."
    )
    @PostMapping
    public ResponseEntity<ValoracionResponseDTO> crear(
            @Valid @RequestBody ValoracionRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearValoracion(dto));
    }

    @Operation(
            summary = "Listar valoraciones",
            description = "Obtiene todas las valoraciones registradas en el sistema."
    )
    @GetMapping
    public ResponseEntity<List<ValoracionResponseDTO>> listar() {
        return ResponseEntity.ok(service.listarValoraciones());
    }

    @Operation(
            summary = "Buscar valoración por ID",
            description = "Obtiene una valoración específica mediante su identificador."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ValoracionResponseDTO> buscarPorId(
            @Parameter(description = "ID de la valoración", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @Operation(
            summary = "Listar valoraciones por libro",
            description = "Obtiene todas las valoraciones asociadas a un libro."
    )
    @GetMapping("/libro/{libroId}")
    public ResponseEntity<List<ValoracionResponseDTO>> listarPorLibro(
            @Parameter(description = "ID del libro", example = "10")
            @PathVariable Long libroId) {

        return ResponseEntity.ok(service.listarPorLibro(libroId));
    }

    @Operation(
            summary = "Listar valoraciones por usuario",
            description = "Obtiene todas las valoraciones realizadas por un usuario."
    )
    @GetMapping("/usuario/{email}")
    public ResponseEntity<List<ValoracionResponseDTO>> listarPorUsuario(
            @Parameter(description = "Correo del usuario", example = "diego@biblioteca.com")
            @PathVariable String email) {

        return ResponseEntity.ok(service.listarPorUsuario(email));
    }

    @Operation(
            summary = "Obtener promedio de valoraciones por libro",
            description = "Calcula el promedio de valoraciones de un libro específico."
    )
    @GetMapping("/libro/{libroId}/promedio")
    public ResponseEntity<Map<String, Object>> promedioPorLibro(
            @Parameter(description = "ID del libro", example = "10")
            @PathVariable Long libroId) {

        return ResponseEntity.ok(service.promedioPorLibro(libroId));
    }

    @Operation(
            summary = "Actualizar valoración",
            description = "Actualiza una valoración existente."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ValoracionResponseDTO> actualizar(
            @Parameter(description = "ID de la valoración", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ValoracionRequestDTO dto) {

        return ResponseEntity.ok(service.actualizarValoracion(id, dto));
    }

    @Operation(
            summary = "Eliminar valoración",
            description = "Elimina una valoración del sistema."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la valoración", example = "1")
            @PathVariable Long id) {

        service.eliminarValoracion(id);
        return ResponseEntity.noContent().build();
    }
}