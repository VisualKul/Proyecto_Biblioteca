package com.biblioteca.ms_prestamos.controller;

import com.biblioteca.ms_prestamos.dto.PrestamoCreateDTO;
import com.biblioteca.ms_prestamos.dto.PrestamoUpdateDTO;
import com.biblioteca.ms_prestamos.model.Prestamo;
import com.biblioteca.ms_prestamos.service.PrestamoService;
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
        name = "Préstamos",
        description = "Endpoints para la gestión de préstamos de libros."
)
@RestController
@RequestMapping("/prestamos")
public class PrestamoController {

    private static final Logger log = LoggerFactory.getLogger(PrestamoController.class);

    @Autowired
    private PrestamoService service;

    @Operation(
            summary = "Registrar préstamo",
            description = "Registra un nuevo préstamo validando la existencia del usuario, la disponibilidad del libro y la ausencia de multas pendientes."
    )
    @PostMapping("/registrar")
    public ResponseEntity<Object> registrar(@Valid @RequestBody PrestamoCreateDTO dto) {
        log.info("POST /prestamos/registrar email={} libroId={}", dto.getEmailUsuario(), dto.getLibroId());
        Prestamo nuevo = service.registrarPrestamo(dto);
        return ResponseEntity.status(201).body(nuevo);
    }

    @Operation(
            summary = "Listar préstamos",
            description = "Obtiene todos los préstamos registrados."
    )
    @GetMapping("/listar")
    public ResponseEntity<List<Prestamo>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @Operation(
            summary = "Buscar préstamos por usuario",
            description = "Obtiene todos los préstamos asociados al correo electrónico de un usuario."
    )
    @GetMapping("/usuario/{email}")
    public ResponseEntity<Object> listarPorUsuario(
            @Parameter(
                    description = "Correo electrónico del usuario",
                    example = "diego@biblioteca.com")
            @PathVariable String email) {

        List<Prestamo> prestamos = service.listarPorEmail(email);

        if (prestamos.isEmpty()) {
            return ResponseEntity.status(404)
                    .body("El usuario con correo " + email + " no tiene prestamos registrados");
        }

        return ResponseEntity.ok(prestamos);
    }

    @Operation(
            summary = "Buscar préstamo por ID",
            description = "Obtiene la información de un préstamo utilizando su identificador."
    )
    @GetMapping("/ver/{id}")
    public ResponseEntity<Object> verUno(
            @Parameter(
                    description = "ID del préstamo",
                    example = "1")
            @PathVariable Long id) {

        Prestamo prestamo = service.buscarPorId(id);

        if (prestamo == null) {
            return ResponseEntity.status(404).body("Prestamo no encontrado");
        }

        return ResponseEntity.ok(prestamo);
    }

    @Operation(
            summary = "Actualizar préstamo",
            description = "Actualiza la fecha de devolución o el estado de un préstamo."
    )
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<Object> actualizar(
            @Parameter(
                    description = "ID del préstamo",
                    example = "1")
            @PathVariable Long id,
            @Valid @RequestBody PrestamoUpdateDTO dto) {

        log.info("PUT /prestamos/actualizar/{}", id);
        Prestamo actualizado = service.actualizar(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    @Operation(
            summary = "Eliminar préstamo",
            description = "Elimina un préstamo registrado."
    )
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Object> eliminar(
            @Parameter(
                    description = "ID del préstamo",
                    example = "1")
            @PathVariable Long id) {

        log.info("DELETE /prestamos/eliminar/{}", id);
        service.eliminar(id);
        return ResponseEntity.ok("Prestamo eliminado");
    }
}