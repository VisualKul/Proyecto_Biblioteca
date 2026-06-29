package com.biblioteca.ms_multas.controller;

import com.biblioteca.ms_multas.dto.MultaAjusteDTO;
import com.biblioteca.ms_multas.model.Multa;
import com.biblioteca.ms_multas.service.MultaService;
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
import java.util.Map;

@Tag(
        name = "Multas",
        description = "Endpoints para la gestión, cálculo, pago y administración de multas."
)
@RestController
@RequestMapping("/multas")
public class MultaController {

    private static final Logger log = LoggerFactory.getLogger(MultaController.class);

    @Autowired
    private MultaService service;

    @Operation(
            summary = "Calcular multa",
            description = "Calcula una multa para un préstamo vencido."
    )
    @PostMapping("/calcular/{prestamoId}")
    public ResponseEntity<Object> calcular(
            @Parameter(description = "ID del préstamo", example = "1")
            @PathVariable Long prestamoId) {

        log.info("POST /multas/calcular/{}", prestamoId);

        try {
            Multa multa = service.calcularMulta(prestamoId);
            return ResponseEntity.status(201).body(multa);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(503).body(ex.getMessage());
        }
    }

    @Operation(
            summary = "Pagar multa",
            description = "Marca una multa como pagada."
    )
    @PostMapping("/pagar/{multaId}")
    public ResponseEntity<Object> pagar(
            @Parameter(description = "ID de la multa", example = "1")
            @PathVariable Long multaId) {

        log.info("POST /multas/pagar/{}", multaId);

        try {
            Multa multa = service.pagarMulta(multaId);
            return ResponseEntity.ok(multa);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        }
    }

    @Operation(
            summary = "Ajustar multa",
            description = "Permite modificar administrativamente los días de retraso o el monto de una multa."
    )
    @PutMapping("/ajustar/{multaId}")
    public ResponseEntity<Object> ajustar(
            @Parameter(description = "ID de la multa", example = "1")
            @PathVariable Long multaId,
            @Valid @RequestBody MultaAjusteDTO dto) {

        log.info("PUT /multas/ajustar/{}", multaId);

        try {
            Multa multa = service.ajustarMulta(multaId, dto.getDiasRetraso(), dto.getMonto());
            return ResponseEntity.ok(multa);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        }
    }

    @Operation(
            summary = "Anular multa",
            description = "Realiza una anulación lógica (soft delete) de una multa."
    )
    @PutMapping("/anular/{multaId}")
    public ResponseEntity<Object> anular(
            @Parameter(description = "ID de la multa", example = "1")
            @PathVariable Long multaId) {

        log.info("PUT /multas/anular/{}", multaId);

        try {
            return ResponseEntity.ok(service.anularMulta(multaId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        }
    }

    @Operation(
            summary = "Eliminar multa",
            description = "Elimina físicamente una multa del sistema."
    )
    @DeleteMapping("/{multaId}")
    public ResponseEntity<Object> eliminar(
            @Parameter(description = "ID de la multa", example = "1")
            @PathVariable Long multaId) {

        log.warn("DELETE /multas/{}", multaId);

        try {
            service.eliminarMulta(multaId);
            return ResponseEntity.ok("Multa eliminada");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        }
    }

    @Operation(
            summary = "Listar multas de un usuario",
            description = "Obtiene todas las multas asociadas a un correo electrónico."
    )
    @GetMapping("/usuario/{email}")
    public ResponseEntity<Object> listarPorUsuario(
            @Parameter(description = "Correo electrónico del usuario", example = "diego@biblioteca.com")
            @PathVariable String email) {

        List<Multa> multas = service.listarPorUsuario(email);

        if (multas.isEmpty()) {
            return ResponseEntity.status(404).body("El usuario no tiene multas registradas");
        }

        return ResponseEntity.ok(multas);
    }

    @Operation(
            summary = "Consultar multas pendientes",
            description = "Indica si un usuario tiene multas pendientes de pago y cuántas posee."
    )
    @GetMapping("/pendientes/{email}")
    public ResponseEntity<Map<String, Object>> tienePendientes(
            @Parameter(description = "Correo electrónico del usuario", example = "diego@biblioteca.com")
            @PathVariable String email) {

        boolean pendientes = service.tienePendientes(email);
        long cantidad = service.listarPendientesPorUsuario(email).size();

        return ResponseEntity.ok(Map.of(
                "email", email,
                "tienePendientes", pendientes,
                "cantidad", cantidad
        ));
    }

    @Operation(
            summary = "Buscar multa por ID",
            description = "Obtiene la información de una multa mediante su identificador."
    )
    @GetMapping("/ver/{id}")
    public ResponseEntity<Object> verUna(
            @Parameter(description = "ID de la multa", example = "1")
            @PathVariable Long id) {

        Multa multa = service.buscarPorId(id);

        if (multa == null) {
            return ResponseEntity.status(404).body("Multa no encontrada");
        }

        return ResponseEntity.ok(multa);
    }

    @Operation(
            summary = "Listar todas las multas",
            description = "Obtiene todas las multas registradas en el sistema."
    )
    @GetMapping("/listar")
    public ResponseEntity<Object> listarTodas() {

        List<Multa> multas = service.listarTodas();

        if (multas.isEmpty()) {
            return ResponseEntity.status(404).body("No hay multas registradas");
        }

        return ResponseEntity.ok(multas);
    }
}