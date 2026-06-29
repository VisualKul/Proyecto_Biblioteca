package com.biblioteca.msnotificaciones.controller;

import com.biblioteca.msnotificaciones.dto.NotificacionRequestDTO;
import com.biblioteca.msnotificaciones.dto.NotificacionResponseDTO;
import com.biblioteca.msnotificaciones.exception.GlobalExceptionHandler;
import com.biblioteca.msnotificaciones.exception.ResourceNotFoundException;
import com.biblioteca.msnotificaciones.service.NotificacionService;
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
@DisplayName("NotificacionController - tests unitarios (MockMvc standalone)")
class NotificacionControllerTest {

    @Mock
    private NotificacionService service;

    private NotificacionController controller;
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private NotificacionResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        controller = new NotificacionController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        responseDTO = NotificacionResponseDTO.builder()
                .id(1L).emailUsuario("diego@biblioteca.com").mensaje("Mensaje de prueba")
                .tipo("GENERICO").estado("PENDIENTE").build();
    }

    private NotificacionRequestDTO validRequest() {
        NotificacionRequestDTO dto = new NotificacionRequestDTO();
        dto.setEmailUsuario("diego@biblioteca.com");
        dto.setMensaje("Mensaje de prueba");
        dto.setTipo("GENERICO");
        return dto;
    }

    @Test
    @DisplayName("POST /api/notificaciones devuelve 201")
    void crear_ok() throws Exception {
        when(service.crearNotificacion(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/notificaciones con tipo invalido devuelve 400")
    void crear_invalido() throws Exception {
        NotificacionRequestDTO dto = validRequest();
        dto.setTipo("OTRO_TIPO"); // no cumple el patron

        mockMvc.perform(post("/api/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
        verify(service, never()).crearNotificacion(any());
    }

    @Test
    @DisplayName("POST /api/notificaciones/vencimiento/{id} devuelve 201")
    void notificarVencimiento_ok() throws Exception {
        when(service.notificarVencimiento(10L)).thenReturn(responseDTO);

        mockMvc.perform(post("/api/notificaciones/vencimiento/10"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /api/notificaciones devuelve 200 con la lista")
    void listar_ok() throws Exception {
        when(service.listarNotificaciones()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/notificaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("GET /api/notificaciones/{id} devuelve 404 cuando no existe")
    void buscarPorId_noExiste() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new ResourceNotFoundException("Notificacion no encontrada"));

        mockMvc.perform(get("/api/notificaciones/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/notificaciones/{id}/enviar devuelve 200")
    void marcarEnviada_ok() throws Exception {
        when(service.marcarEnviada(1L)).thenReturn(responseDTO);

        mockMvc.perform(put("/api/notificaciones/1/enviar"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/notificaciones/{id} devuelve 204")
    void eliminar_ok() throws Exception {
        doNothing().when(service).eliminarNotificacion(1L);

        mockMvc.perform(delete("/api/notificaciones/1"))
                .andExpect(status().isNoContent());
        verify(service).eliminarNotificacion(1L);
    }
}
