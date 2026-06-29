package com.biblioteca.security_service.controller;

import com.biblioteca.security_service.dto.AsignarRolDTO;
import com.biblioteca.security_service.dto.RolCreateDTO;
import com.biblioteca.security_service.exception.GlobalExceptionHandler;
import com.biblioteca.security_service.model.Rol;
import com.biblioteca.security_service.model.UsuarioRol;
import com.biblioteca.security_service.service.SecurityService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityController - tests unitarios (MockMvc standalone)")
class SecurityControllerTest {

    @Mock
    private SecurityService service;

    @InjectMocks
    private SecurityController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Rol rol;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        rol = new Rol();
        rol.setId(1L);
        rol.setNombre("ROLE_ADMIN");
    }

    @Test
    @DisplayName("POST /api/security/roles devuelve 201")
    void crearRol_ok() throws Exception {
        RolCreateDTO dto = new RolCreateDTO();
        dto.setNombre("ROLE_SOCIO");
        when(service.crearRol(any())).thenReturn(rol);

        mockMvc.perform(post("/api/security/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/security/roles con nombre invalido devuelve 400")
    void crearRol_invalido() throws Exception {
        RolCreateDTO dto = new RolCreateDTO();
        dto.setNombre("admin"); // no cumple patron ROLE_XXX

        mockMvc.perform(post("/api/security/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/security/roles devuelve 200 con la lista")
    void listarRoles_ok() throws Exception {
        when(service.listarRoles()).thenReturn(List.of(rol));

        mockMvc.perform(get("/api/security/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("GET /api/security/roles/buscar/{id} devuelve 404 cuando no existe")
    void verRol_noExiste() throws Exception {
        when(service.obtenerRolPorId(99L)).thenReturn(null);

        mockMvc.perform(get("/api/security/roles/buscar/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/security/roles/{id} devuelve 200")
    void eliminarRol_ok() throws Exception {
        doNothing().when(service).eliminarRol(1L);

        mockMvc.perform(delete("/api/security/roles/1"))
                .andExpect(status().isOk());
        verify(service).eliminarRol(1L);
    }

    @Test
    @DisplayName("GET /api/security/roles/usuario/{id} devuelve 200 con los roles")
    void verRolesDeUsuario_ok() throws Exception {
        when(service.obtenerRolesDeUsuario(10L)).thenReturn(List.of("ROLE_ADMIN"));

        mockMvc.perform(get("/api/security/roles/usuario/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("GET /api/security/roles/usuario/{id} devuelve 404 si no hay roles y el usuario no existe")
    void verRolesDeUsuario_noExiste() throws Exception {
        when(service.obtenerRolesDeUsuario(99L)).thenReturn(List.of());
        when(service.usuarioExiste(99L)).thenReturn(false);

        mockMvc.perform(get("/api/security/roles/usuario/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/security/asignar devuelve 201")
    void asignarRol_ok() throws Exception {
        AsignarRolDTO dto = new AsignarRolDTO();
        dto.setUsuarioId(10L);
        dto.setRolId(1L);
        UsuarioRol asignado = new UsuarioRol();
        asignado.setId(5L);
        asignado.setUsuarioId(10L);
        asignado.setRol(rol);
        when(service.asignarRol(any())).thenReturn(asignado);

        mockMvc.perform(post("/api/security/asignar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("DELETE /api/security/asignar/{id} devuelve 200")
    void quitarRol_ok() throws Exception {
        doNothing().when(service).quitarRol(5L);

        mockMvc.perform(delete("/api/security/asignar/5"))
                .andExpect(status().isOk());
        verify(service).quitarRol(5L);
    }
}
