package com.biblioteca.ms_multas.controller;

import com.biblioteca.ms_multas.exception.GlobalExceptionHandler;
import com.biblioteca.ms_multas.model.Multa;
import com.biblioteca.ms_multas.service.MultaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MultaController - tests unitarios (MockMvc standalone)")
class MultaControllerTest {

    @Mock
    private MultaService service;

    @InjectMocks
    private MultaController controller;

    private MockMvc mockMvc;
    private Multa multa;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        multa = new Multa();
        multa.setId(1L);
        multa.setEmailUsuario("diego@biblioteca.com");
        multa.setEstado("PENDIENTE");
    }

    @Test
    @DisplayName("POST /multas/calcular/{id} devuelve 201")
    void calcular_ok() throws Exception {
        when(service.calcularMulta(1L)).thenReturn(multa);

        mockMvc.perform(post("/multas/calcular/1"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /multas/calcular/{id} devuelve 404 cuando el service lanza IllegalArgumentException")
    void calcular_noEncontrado() throws Exception {
        when(service.calcularMulta(1L)).thenThrow(new IllegalArgumentException("Prestamo no encontrado"));

        mockMvc.perform(post("/multas/calcular/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /multas/calcular/{id} devuelve 503 cuando hay error de comunicacion")
    void calcular_servicioCaido() throws Exception {
        when(service.calcularMulta(1L)).thenThrow(new IllegalStateException("Error comunicandose con ms-prestamos"));

        mockMvc.perform(post("/multas/calcular/1"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("POST /multas/pagar/{id} devuelve 200")
    void pagar_ok() throws Exception {
        when(service.pagarMulta(1L)).thenReturn(multa);

        mockMvc.perform(post("/multas/pagar/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /multas/anular/{id} devuelve 404 cuando no existe")
    void anular_noExiste() throws Exception {
        when(service.anularMulta(99L)).thenThrow(new IllegalArgumentException("Multa no encontrada"));

        mockMvc.perform(put("/multas/anular/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /multas/{id} devuelve 200")
    void eliminar_ok() throws Exception {
        doNothing().when(service).eliminarMulta(1L);

        mockMvc.perform(delete("/multas/1"))
                .andExpect(status().isOk());
        verify(service).eliminarMulta(1L);
    }

    @Test
    @DisplayName("GET /multas/usuario/{email} devuelve 404 cuando no tiene multas")
    void listarPorUsuario_vacio() throws Exception {
        when(service.listarPorUsuario("diego@biblioteca.com")).thenReturn(List.of());

        mockMvc.perform(get("/multas/usuario/diego@biblioteca.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /multas/pendientes/{email} devuelve el resumen de pendientes")
    void pendientes_ok() throws Exception {
        when(service.tienePendientes("diego@biblioteca.com")).thenReturn(true);
        when(service.listarPendientesPorUsuario("diego@biblioteca.com")).thenReturn(List.of(multa));

        mockMvc.perform(get("/multas/pendientes/diego@biblioteca.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tienePendientes").value(true))
                .andExpect(jsonPath("$.cantidad").value(1));
    }

    @Test
    @DisplayName("GET /multas/ver/{id} devuelve 404 cuando no existe")
    void verUna_noExiste() throws Exception {
        when(service.buscarPorId(99L)).thenReturn(null);

        mockMvc.perform(get("/multas/ver/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /multas/listar devuelve 200 con la lista")
    void listarTodas_ok() throws Exception {
        when(service.listarTodas()).thenReturn(List.of(multa));

        mockMvc.perform(get("/multas/listar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}
