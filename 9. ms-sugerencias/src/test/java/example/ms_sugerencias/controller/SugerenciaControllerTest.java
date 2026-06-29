package example.ms_sugerencias.controller;

import example.ms_sugerencias.dto.SugerenciaEstadoDTO;
import example.ms_sugerencias.dto.SugerenciaRequestDTO;
import example.ms_sugerencias.dto.SugerenciaResponseDTO;
import example.ms_sugerencias.exception.GlobalExceptionHandler;
import example.ms_sugerencias.exception.ResourceNotFoundException;
import example.ms_sugerencias.service.SugerenciaService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SugerenciaController - tests unitarios (MockMvc standalone)")
class SugerenciaControllerTest {

    @Mock
    private SugerenciaService service;

    private SugerenciaController controller;
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private SugerenciaResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        controller = new SugerenciaController(service);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        responseDTO = SugerenciaResponseDTO.builder()
                .id(1L).titulo("Clean Code").autor("Robert Martin").isbn("9780132350884")
                .estado("PENDIENTE").emailSocio("diego@biblioteca.com").build();
    }

    private SugerenciaRequestDTO validRequest() {
        SugerenciaRequestDTO dto = new SugerenciaRequestDTO();
        dto.setTitulo("Clean Code");
        dto.setAutor("Robert Martin");
        dto.setIsbn("9780132350884");
        dto.setEmailSocio("diego@biblioteca.com");
        return dto;
    }

    @Test
    @DisplayName("POST /api/sugerencias devuelve 201")
    void crear_ok() throws Exception {
        when(service.crear(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/sugerencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/sugerencias con datos invalidos devuelve 400")
    void crear_invalido() throws Exception {
        SugerenciaRequestDTO dto = new SugerenciaRequestDTO(); // vacio

        mockMvc.perform(post("/api/sugerencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
        verify(service, never()).crear(any());
    }

    @Test
    @DisplayName("GET /api/sugerencias devuelve 200 con la lista")
    void listar_ok() throws Exception {
        when(service.listar()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/sugerencias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Clean Code"));
    }

    @Test
    @DisplayName("GET /api/sugerencias/{id} devuelve 404 cuando no existe")
    void buscarPorId_noExiste() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new ResourceNotFoundException("Sugerencia no encontrada"));

        mockMvc.perform(get("/api/sugerencias/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/sugerencias/{id}/estado devuelve 200")
    void cambiarEstado_ok() throws Exception {
        SugerenciaEstadoDTO estadoDTO = new SugerenciaEstadoDTO();
        estadoDTO.setEstado("APROBADA");
        when(service.cambiarEstado(eq(1L), any())).thenReturn(responseDTO);

        mockMvc.perform(put("/api/sugerencias/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estadoDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/sugerencias/{id}/estado con estado invalido devuelve 400")
    void cambiarEstado_invalido() throws Exception {
        SugerenciaEstadoDTO estadoDTO = new SugerenciaEstadoDTO();
        estadoDTO.setEstado("XXX");

        mockMvc.perform(put("/api/sugerencias/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estadoDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/sugerencias/{id} devuelve 204")
    void eliminar_ok() throws Exception {
        doNothing().when(service).eliminar(1L);

        mockMvc.perform(delete("/api/sugerencias/1"))
                .andExpect(status().isNoContent());
        verify(service).eliminar(1L);
    }
}
