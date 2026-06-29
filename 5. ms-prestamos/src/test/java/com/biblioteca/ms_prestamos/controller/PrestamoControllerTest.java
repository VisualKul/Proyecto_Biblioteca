package com.biblioteca.ms_prestamos.controller;

import com.biblioteca.ms_prestamos.dto.PrestamoCreateDTO;
import com.biblioteca.ms_prestamos.exception.GlobalExceptionHandler;
import com.biblioteca.ms_prestamos.exception.ResourceNotFoundException;
import com.biblioteca.ms_prestamos.model.Prestamo;
import com.biblioteca.ms_prestamos.service.PrestamoService;
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
@DisplayName("PrestamoController - tests unitarios (MockMvc standalone)")
class PrestamoControllerTest {

    @Mock
    private PrestamoService service;

    @InjectMocks
    private PrestamoController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Prestamo prestamo;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        prestamo = new Prestamo();
        prestamo.setId(1L);
        prestamo.setEmailUsuario("diego@biblioteca.com");
        prestamo.setLibroId(1L);
        prestamo.setEstado("ACTIVO");
    }

    @Test
    @DisplayName("POST /prestamos/registrar devuelve 201")
    void registrar_ok() throws Exception {
        PrestamoCreateDTO dto = new PrestamoCreateDTO();
        dto.setEmailUsuario("diego@biblioteca.com");
        dto.setLibroId(1L);
        when(service.registrarPrestamo(any())).thenReturn(prestamo);

        mockMvc.perform(post("/prestamos/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /prestamos/registrar con email invalido devuelve 400")
    void registrar_invalido() throws Exception {
        PrestamoCreateDTO dto = new PrestamoCreateDTO();
        dto.setEmailUsuario("no-es-email");
        // libroId nulo -> invalido

        mockMvc.perform(post("/prestamos/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
        verify(service, never()).registrarPrestamo(any());
    }

    @Test
    @DisplayName("POST /prestamos/registrar propaga 404 cuando el recurso no existe")
    void registrar_recursoNoEncontrado() throws Exception {
        PrestamoCreateDTO dto = new PrestamoCreateDTO();
        dto.setEmailUsuario("diego@biblioteca.com");
        dto.setLibroId(1L);
        when(service.registrarPrestamo(any()))
                .thenThrow(new ResourceNotFoundException("Usuario no encontrado en user-service"));

        mockMvc.perform(post("/prestamos/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /prestamos/listar devuelve 200")
    void listar_ok() throws Exception {
        when(service.listarTodos()).thenReturn(List.of(prestamo));

        mockMvc.perform(get("/prestamos/listar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("GET /prestamos/usuario/{email} devuelve 404 cuando no tiene prestamos")
    void listarPorUsuario_vacio() throws Exception {
        when(service.listarPorEmail("diego@biblioteca.com")).thenReturn(List.of());

        mockMvc.perform(get("/prestamos/usuario/diego@biblioteca.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /prestamos/ver/{id} devuelve 404 cuando no existe")
    void verUno_noExiste() throws Exception {
        when(service.buscarPorId(99L)).thenReturn(null);

        mockMvc.perform(get("/prestamos/ver/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /prestamos/actualizar/{id} devuelve 200")
    void actualizar_ok() throws Exception {
        when(service.actualizar(eq(1L), any())).thenReturn(prestamo);

        mockMvc.perform(put("/prestamos/actualizar/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"DEVUELTO\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /prestamos/eliminar/{id} devuelve 200")
    void eliminar_ok() throws Exception {
        doNothing().when(service).eliminar(1L);

        mockMvc.perform(delete("/prestamos/eliminar/1"))
                .andExpect(status().isOk());
        verify(service).eliminar(1L);
    }
}
