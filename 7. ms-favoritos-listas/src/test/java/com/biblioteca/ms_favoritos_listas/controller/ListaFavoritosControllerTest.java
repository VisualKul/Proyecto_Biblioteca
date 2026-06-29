package com.biblioteca.ms_favoritos_listas.controller;

import com.biblioteca.ms_favoritos_listas.dto.ListaFavoritosRequestDTO;
import com.biblioteca.ms_favoritos_listas.dto.ListaFavoritosResponseDTO;
import com.biblioteca.ms_favoritos_listas.exception.GlobalExceptionHandler;
import com.biblioteca.ms_favoritos_listas.exception.ResourceNotFoundException;
import com.biblioteca.ms_favoritos_listas.service.ListaFavoritosService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListaFavoritosController - tests unitarios (MockMvc standalone)")
class ListaFavoritosControllerTest {

    @Mock
    private ListaFavoritosService service;

    private ListaFavoritosController controller;
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ListaFavoritosResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        controller = new ListaFavoritosController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        responseDTO = ListaFavoritosResponseDTO.builder()
                .id(1L).nombre("Favoritos 2026").emailUsuario("diego@biblioteca.com").publica(true).build();
    }

    private ListaFavoritosRequestDTO validRequest() {
        ListaFavoritosRequestDTO dto = new ListaFavoritosRequestDTO();
        dto.setNombre("Favoritos 2026");
        dto.setEmailUsuario("diego@biblioteca.com");
        dto.setPublica(true);
        return dto;
    }

    @Test
    @DisplayName("POST /api/listas devuelve 201")
    void crear_ok() throws Exception {
        when(service.crear(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/listas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/listas con datos invalidos devuelve 400")
    void crear_invalido() throws Exception {
        ListaFavoritosRequestDTO dto = new ListaFavoritosRequestDTO(); // vacio

        mockMvc.perform(post("/api/listas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
        verify(service, never()).crear(any());
    }

    @Test
    @DisplayName("GET /api/listas devuelve 200 con la lista")
    void listar_ok() throws Exception {
        when(service.listar()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/listas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Favoritos 2026"));
    }

    @Test
    @DisplayName("GET /api/listas/{id} devuelve 404 cuando no existe")
    void buscarPorId_noExiste() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new ResourceNotFoundException("Lista no encontrada"));

        mockMvc.perform(get("/api/listas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/listas/publicas devuelve 200")
    void listarPublicas_ok() throws Exception {
        when(service.listarPublicas()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/listas/publicas"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/listas/{id} devuelve 204")
    void eliminar_ok() throws Exception {
        doNothing().when(service).eliminar(1L);

        mockMvc.perform(delete("/api/listas/1"))
                .andExpect(status().isNoContent());
        verify(service).eliminar(1L);
    }
}
