package com.biblioteca.ms_inventario.controller;

import com.biblioteca.ms_inventario.dto.LibroCreateDTO;
import com.biblioteca.ms_inventario.exception.GlobalExceptionHandler;
import com.biblioteca.ms_inventario.model.Libro;
import com.biblioteca.ms_inventario.service.LibroService;
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
@DisplayName("LibroController - tests unitarios (MockMvc standalone)")
class LibroControllerTest {

    @Mock
    private LibroService service;

    @InjectMocks
    private LibroController controller;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Libro libro;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        libro = new Libro();
        libro.setId(1L);
        libro.setTitulo("Don Quijote");
        libro.setIsbn("1234567890");
        libro.setStock(5);
    }

    @Test
    @DisplayName("GET /inventario/listar devuelve 200 con la lista")
    void listar_ok() throws Exception {
        when(service.listarTodo()).thenReturn(List.of(libro));

        mockMvc.perform(get("/inventario/listar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Don Quijote"));
    }

    @Test
    @DisplayName("GET /inventario/listar devuelve 404 cuando esta vacio")
    void listar_vacio() throws Exception {
        when(service.listarTodo()).thenReturn(List.of());

        mockMvc.perform(get("/inventario/listar"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /inventario/ver/{id} devuelve 200 cuando existe")
    void verUno_ok() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(libro);

        mockMvc.perform(get("/inventario/ver/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /inventario/ver/{id} devuelve 404 cuando no existe")
    void verUno_noExiste() throws Exception {
        when(service.buscarPorId(99L)).thenReturn(null);

        mockMvc.perform(get("/inventario/ver/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /inventario/crear devuelve 201")
    void crear_ok() throws Exception {
        LibroCreateDTO dto = new LibroCreateDTO();
        dto.setTitulo("Nuevo");
        dto.setAutor("Autor");
        dto.setIsbn("9999999999");
        dto.setStock(3);
        when(service.crear(any(LibroCreateDTO.class))).thenReturn(libro);

        mockMvc.perform(post("/inventario/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PUT /inventario/actualizar/{id} devuelve 200")
    void actualizar_ok() throws Exception {
        when(service.actualizar(eq(1L), any())).thenReturn(libro);

        mockMvc.perform(put("/inventario/actualizar/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Editado\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /inventario/eliminar/{id} devuelve 200")
    void eliminar_ok() throws Exception {
        doNothing().when(service).eliminar(1L);

        mockMvc.perform(delete("/inventario/eliminar/1"))
                .andExpect(status().isOk());
        verify(service).eliminar(1L);
    }

    @Test
    @DisplayName("GET /inventario/ver/nombre/{nombre} devuelve 404 cuando no hay resultados")
    void verPorNombre_vacio() throws Exception {
        when(service.buscarPorNombre("xxx")).thenReturn(List.of());

        mockMvc.perform(get("/inventario/ver/nombre/xxx"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /inventario/ver/isbn/{isbn} devuelve 200 cuando existe")
    void verPorIsbn_ok() throws Exception {
        when(service.buscarPorIsbn("1234567890")).thenReturn(libro);

        mockMvc.perform(get("/inventario/ver/isbn/1234567890"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /inventario/descontar/{id} devuelve 200")
    void descontar_ok() throws Exception {
        doNothing().when(service).descontarStock(1L);

        mockMvc.perform(put("/inventario/descontar/1"))
                .andExpect(status().isOk());
        verify(service).descontarStock(1L);
    }

    @Test
    @DisplayName("PUT /inventario/devolver/{id} devuelve 200")
    void devolver_ok() throws Exception {
        doNothing().when(service).devolverStock(1L);

        mockMvc.perform(put("/inventario/devolver/1"))
                .andExpect(status().isOk());
        verify(service).devolverStock(1L);
    }
}
