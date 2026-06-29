package example.ms_sugerencias.controller;

import example.ms_sugerencias.dto.*;
import example.ms_sugerencias.service.SugerenciaService;
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
        name = "Sugerencias",
        description = "Endpoints para la gestión de sugerencias de los socios."
)
@RestController
@RequestMapping("/api/sugerencias")
@RequiredArgsConstructor
@Slf4j
public class SugerenciaController {

    private final SugerenciaService service;

    @Operation(
            summary = "Crear sugerencia",
            description = "Permite crear una nueva sugerencia de un socio."
    )
    @PostMapping
    public ResponseEntity<SugerenciaResponseDTO> crear(
            @Valid @RequestBody SugerenciaRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @Operation(
            summary = "Listar sugerencias",
            description = "Obtiene todas las sugerencias registradas en el sistema."
    )
    @GetMapping
    public ResponseEntity<List<SugerenciaResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @Operation(
            summary = "Buscar sugerencia por ID",
            description = "Obtiene una sugerencia específica mediante su identificador."
    )
    @GetMapping("/{id}")
    public ResponseEntity<SugerenciaResponseDTO> buscarPorId(
            @Parameter(description = "ID de la sugerencia", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @Operation(
            summary = "Listar sugerencias por socio",
            description = "Obtiene todas las sugerencias asociadas al correo de un socio."
    )
    @GetMapping("/socio/{email}")
    public ResponseEntity<List<SugerenciaResponseDTO>> listarPorSocio(
            @Parameter(description = "Correo del socio", example = "diego@biblioteca.com")
            @PathVariable String email) {

        return ResponseEntity.ok(service.listarPorSocio(email));
    }

    @Operation(
            summary = "Listar sugerencias por estado",
            description = "Obtiene todas las sugerencias filtradas por su estado."
    )
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<SugerenciaResponseDTO>> listarPorEstado(
            @Parameter(description = "Estado de la sugerencia", example = "PENDIENTE")
            @PathVariable String estado) {

        return ResponseEntity.ok(service.listarPorEstado(estado));
    }

    @Operation(
            summary = "Actualizar sugerencia",
            description = "Actualiza los datos de una sugerencia existente."
    )
    @PutMapping("/{id}")
    public ResponseEntity<SugerenciaResponseDTO> actualizar(
            @Parameter(description = "ID de la sugerencia", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody SugerenciaRequestDTO dto) {

        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @Operation(
            summary = "Cambiar estado de sugerencia",
            description = "Actualiza el estado de una sugerencia."
    )
    @PutMapping("/{id}/estado")
    public ResponseEntity<SugerenciaResponseDTO> cambiarEstado(
            @Parameter(description = "ID de la sugerencia", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody SugerenciaEstadoDTO dto) {

        return ResponseEntity.ok(service.cambiarEstado(id, dto));
    }

    @Operation(
            summary = "Eliminar sugerencia",
            description = "Elimina una sugerencia del sistema."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la sugerencia", example = "1")
            @PathVariable Long id) {

        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}