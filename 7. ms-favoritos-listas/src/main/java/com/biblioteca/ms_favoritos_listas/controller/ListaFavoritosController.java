package com.biblioteca.ms_favoritos_listas.controller;

import com.biblioteca.ms_favoritos_listas.dto.ListaFavoritosRequestDTO;
import com.biblioteca.ms_favoritos_listas.dto.ListaFavoritosResponseDTO;
import com.biblioteca.ms_favoritos_listas.service.ListaFavoritosService;
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
        name = "Favoritos y Listas",
        description = "Endpoints para administrar listas de favoritos de los usuarios."
)
@RestController
@RequestMapping("/api/listas")
@RequiredArgsConstructor
@Slf4j
public class ListaFavoritosController {

    private final ListaFavoritosService service;

    @Operation(
            summary = "Crear una lista de favoritos",
            description = "Crea una nueva lista de favoritos para un usuario."
    )
    @PostMapping
    public ResponseEntity<ListaFavoritosResponseDTO> crear(
            @Valid @RequestBody ListaFavoritosRequestDTO dto) {

        log.info("POST /api/listas");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @Operation(
            summary = "Listar todas las listas",
            description = "Obtiene todas las listas de favoritos registradas."
    )
    @GetMapping
    public ResponseEntity<List<ListaFavoritosResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @Operation(
            summary = "Buscar lista por ID",
            description = "Obtiene una lista de favoritos utilizando su identificador."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ListaFavoritosResponseDTO> buscarPorId(
            @Parameter(description = "ID de la lista", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @Operation(
            summary = "Listar listas de un usuario",
            description = "Obtiene todas las listas de favoritos asociadas al correo de un usuario."
    )
    @GetMapping("/usuario/{email}")
    public ResponseEntity<List<ListaFavoritosResponseDTO>> listarPorUsuario(
            @Parameter(description = "Correo electrónico del usuario", example = "diego@biblioteca.com")
            @PathVariable String email) {

        return ResponseEntity.ok(service.listarPorUsuario(email));
    }

    @Operation(
            summary = "Listar listas públicas",
            description = "Obtiene todas las listas marcadas como públicas."
    )
    @GetMapping("/publicas")
    public ResponseEntity<List<ListaFavoritosResponseDTO>> listarPublicas() {
        return ResponseEntity.ok(service.listarPublicas());
    }

    @Operation(
            summary = "Actualizar una lista",
            description = "Actualiza la información de una lista de favoritos."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ListaFavoritosResponseDTO> actualizar(
            @Parameter(description = "ID de la lista", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ListaFavoritosRequestDTO dto) {

        return ResponseEntity.ok(service.actualizar(id, dto));
    }

    @Operation(
            summary = "Eliminar una lista",
            description = "Elimina una lista de favoritos."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la lista", example = "1")
            @PathVariable Long id) {

        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}