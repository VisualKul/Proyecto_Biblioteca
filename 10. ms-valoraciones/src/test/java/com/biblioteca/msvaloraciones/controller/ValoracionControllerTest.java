package com.biblioteca.msvaloraciones.controller;

import com.biblioteca.msvaloraciones.dto.ValoracionRequestDTO;
import com.biblioteca.msvaloraciones.dto.ValoracionResponseDTO;
import com.biblioteca.msvaloraciones.exception.GlobalExceptionHandler;
import com.biblioteca.msvaloraciones.exception.ResourceNotFoundException;
import com.biblioteca.msvaloraciones.service.ValoracionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValoracionController - tests unitarios (MockMvc standalone)")
class ValoracionControllerTest {

    @Mock
    private ValoracionService service;

    private ValoracionController controller;
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ValoracionResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        controller = new ValoracionController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        responseDTO = ValoracionResponseDTO.builder()
                .id(1L).libroId(10L).emailUsuario("diego@biblioteca.com")
                .puntuacion(5).comentario("Excelente libro").build();
    }

    private ValoracionRequestDTO validRequest() {
        ValoracionRequestDTO dto = new ValoracionRequestDTO();
        dto.setLibroId(10L);
        dto.setEmailUsuario("diego@biblioteca.com");
        dto.setPuntuacion(5);
        dto.setComentario("Excelente libro");
        return dto;
    }

    @Test
    @DisplayName("POST /api/valoraciones devuelve 201")
    void crear_ok() throws Exception {
        when(service.crearValoracion(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/valoraciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/valoraciones con puntuacion fuera de rango devuelve 400")
    void crear_invalido() throws Exception {
        ValoracionRequestDTO dto = validRequest();
        dto.setPuntuacion(9); // fuera de 1..5

        mockMvc.perform(post("/api/valoraciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
        verify(service, never()).crearValoracion(any());
    }

    @Test
    @DisplayName("GET /api/valoraciones devuelve 200 con la lista")
    void listar_ok() throws Exception {
        when(service.listarValoraciones()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/valoraciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("GET /api/valoraciones/{id} devuelve 404 cuando no existe")
    void buscarPorId_noExiste() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new ResourceNotFoundException("Valoracion no encontrada"));

        mockMvc.perform(get("/api/valoraciones/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/valoraciones/libro/{id}/promedio devuelve el promedio")
    void promedio_ok() throws Exception {
        when(service.promedioPorLibro(10L)).thenReturn(Map.of("libroId", 10L, "promedio", 4.5, "cantidad", 2L));

        mockMvc.perform(get("/api/valoraciones/libro/10/promedio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promedio").value(4.5));
    }

    @Test
    @DisplayName("DELETE /api/valoraciones/{id} devuelve 204")
    void eliminar_ok() throws Exception {
        doNothing().when(service).eliminarValoracion(1L);

        mockMvc.perform(delete("/api/valoraciones/1"))
                .andExpect(status().isNoContent());
        verify(service).eliminarValoracion(1L);
    }
}
