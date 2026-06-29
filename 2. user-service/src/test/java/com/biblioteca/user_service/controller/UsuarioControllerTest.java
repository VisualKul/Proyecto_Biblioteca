package com.biblioteca.user_service.controller;

import com.biblioteca.user_service.dto.UsuarioCreateDTO;
import com.biblioteca.user_service.exception.GlobalExceptionHandler;
import com.biblioteca.user_service.model.UsuarioModelo;
import com.biblioteca.user_service.service.UsuarioService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioController - tests unitarios (MockMvc standalone)")
class UsuarioControllerTest {

    @Mock
    private UsuarioService service;

    @InjectMocks
    private UsuarioController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private UsuarioModelo usuario;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        usuario = new UsuarioModelo();
        usuario.setId(1L);
        usuario.setNombre("Diego");
        usuario.setEmail("diego@biblioteca.com");
    }

    @Test
    @DisplayName("POST /api/users devuelve 201")
    void registrar_ok() throws Exception {
        UsuarioCreateDTO dto = new UsuarioCreateDTO();
        dto.setNombre("Ana");
        dto.setEmail("ana@biblioteca.com");
        dto.setPassword("1234");
        dto.setTelefono("999999");
        when(service.crearUsuario(any())).thenReturn(usuario);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/users con datos invalidos devuelve 400")
    void registrar_invalido() throws Exception {
        UsuarioCreateDTO dto = new UsuarioCreateDTO(); // todos los campos vacios

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
        verify(service, never()).crearUsuario(any());
    }

    @Test
    @DisplayName("GET /api/users devuelve 200 con la lista")
    void listar_ok() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(usuario));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("diego@biblioteca.com"));
    }

    @Test
    @DisplayName("GET /api/users/email/{email} devuelve 404 cuando no existe")
    void buscarPorEmail_noExiste() throws Exception {
        when(service.obtenerPorEmail("x@x.com")).thenReturn(null);

        mockMvc.perform(get("/api/users/email/x@x.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/users/id/{id} devuelve 200 cuando existe")
    void buscarPorId_ok() throws Exception {
        when(service.obtenerPorId(1L)).thenReturn(usuario);

        mockMvc.perform(get("/api/users/id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /api/users/{id} devuelve 200")
    void actualizar_ok() throws Exception {
        when(service.actualizarUsuario(eq(1L), any())).thenReturn(usuario);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nuevo\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} devuelve 200")
    void eliminar_ok() throws Exception {
        doNothing().when(service).eliminarUsuario(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk());
        verify(service).eliminarUsuario(1L);
    }
}
