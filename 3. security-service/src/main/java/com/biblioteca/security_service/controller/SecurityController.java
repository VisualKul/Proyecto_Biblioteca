package com.biblioteca.security_service.controller;

import com.biblioteca.security_service.dto.AsignarRolDTO;
import com.biblioteca.security_service.dto.RolCreateDTO;
import com.biblioteca.security_service.model.Rol;
import com.biblioteca.security_service.model.UsuarioRol;
import com.biblioteca.security_service.service.SecurityService;
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
        name = "Seguridad",
        description = "Endpoints para la administración de roles y asignaciones de usuarios."
)
@RestController
@RequestMapping("/api/security")
public class SecurityController {

    private static final Logger log = LoggerFactory.getLogger(SecurityController.class);

    @Autowired
    private SecurityService service;

    @Operation(
            summary = "Crear un rol",
            description = "Registra un nuevo rol en el sistema."
    )
    @PostMapping("/roles")
    public ResponseEntity<Object> crearRol(@Valid @RequestBody RolCreateDTO dto) {
        log.info("POST /api/security/roles nombre={}", dto.getNombre());
        Rol creado = service.crearRol(dto);
        return ResponseEntity.status(201).body(creado);
    }

    @Operation(
            summary = "Listar roles",
            description = "Obtiene todos los roles registrados."
    )
    @GetMapping("/roles")
    public ResponseEntity<List<Rol>> listarRoles() {
        return ResponseEntity.ok(service.listarRoles());
    }

    @Operation(
            summary = "Buscar rol por ID",
            description = "Obtiene un rol utilizando su identificador."
    )
    @GetMapping("/roles/buscar/{id}")
    public ResponseEntity<Object> verRol(
            @Parameter(description = "ID del rol", example = "1")
            @PathVariable Long id) {

        Rol rol = service.obtenerRolPorId(id);
        if (rol == null) {
            return ResponseEntity.status(404).body("Rol no encontrado");
        }
        return ResponseEntity.ok(rol);
    }

    @Operation(
            summary = "Actualizar un rol",
            description = "Actualiza la información de un rol existente."
    )
    @PutMapping("/roles/{id}")
    public ResponseEntity<Object> actualizarRol(
            @Parameter(description = "ID del rol", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody RolCreateDTO dto) {

        log.info("PUT /api/security/roles/{} nombre={}", id, dto.getNombre());
        Rol actualizado = service.actualizarRol(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    @Operation(
            summary = "Eliminar un rol",
            description = "Elimina un rol del sistema."
    )
    @DeleteMapping("/roles/{id}")
    public ResponseEntity<Object> eliminarRol(
            @Parameter(description = "ID del rol", example = "1")
            @PathVariable Long id) {

        log.info("DELETE /api/security/roles/{}", id);
        service.eliminarRol(id);
        return ResponseEntity.ok("Rol eliminado");
    }

    @Operation(
            summary = "Consultar roles de un usuario",
            description = "Obtiene todos los roles asignados a un usuario."
    )
    @GetMapping("/roles/usuario/{usuarioId}")
    public ResponseEntity<Object> verRolesDeUsuario(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Long usuarioId) {

        List<String> roles = service.obtenerRolesDeUsuario(usuarioId);
        if (roles.isEmpty() && !service.usuarioExiste(usuarioId)) {
            return ResponseEntity.status(404).body("Usuario no encontrado");
        }
        return ResponseEntity.ok(roles);
    }

    @Operation(
            summary = "Asignar un rol a un usuario",
            description = "Asigna un rol existente a un usuario."
    )
    @PostMapping("/asignar")
    public ResponseEntity<Object> asignarRol(@Valid @RequestBody AsignarRolDTO dto) {
        log.info("POST /api/security/asignar usuarioId={} rolId={}", dto.getUsuarioId(), dto.getRolId());
        UsuarioRol asignado = service.asignarRol(dto);
        return ResponseEntity.status(201).body(asignado);
    }

    @Operation(
            summary = "Actualizar una asignación de rol",
            description = "Modifica la asignación de un rol a un usuario."
    )
    @PutMapping("/asignar/{asignacionId}")
    public ResponseEntity<Object> actualizarAsignacion(
            @Parameter(description = "ID de la asignación", example = "1")
            @PathVariable Long asignacionId,
            @Valid @RequestBody AsignarRolDTO dto) {

        log.info("PUT /api/security/asignar/{}", asignacionId);
        UsuarioRol actualizada = service.actualizarAsignacion(asignacionId, dto);
        return ResponseEntity.ok(actualizada);
    }

    @Operation(
            summary = "Eliminar una asignación de rol",
            description = "Elimina la relación entre un usuario y un rol."
    )
    @DeleteMapping("/asignar/{usuarioRolId}")
    public ResponseEntity<Object> quitarRol(
            @Parameter(description = "ID de la asignación usuario-rol", example = "1")
            @PathVariable Long usuarioRolId) {

        log.info("DELETE /api/security/asignar/{}", usuarioRolId);
        service.quitarRol(usuarioRolId);
        return ResponseEntity.ok("Asignacion eliminada");
    }
}