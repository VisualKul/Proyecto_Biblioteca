package com.biblioteca.msvaloraciones.service;

import com.biblioteca.msvaloraciones.client.LibroClient;
import com.biblioteca.msvaloraciones.client.UserClient;
import com.biblioteca.msvaloraciones.dto.LibroDto;
import com.biblioteca.msvaloraciones.dto.UsuarioDto;
import com.biblioteca.msvaloraciones.dto.ValoracionRequestDTO;
import com.biblioteca.msvaloraciones.dto.ValoracionResponseDTO;
import com.biblioteca.msvaloraciones.entity.Valoracion;
import com.biblioteca.msvaloraciones.exception.ResourceNotFoundException;
import com.biblioteca.msvaloraciones.repository.ValoracionRepository;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValoracionServiceImpl - tests unitarios")
class ValoracionServiceImplTest {

    @Mock private ValoracionRepository repository;
    @Mock private UserClient userClient;
    @Mock private LibroClient libroClient;

    @InjectMocks
    private ValoracionServiceImpl service;

    private ValoracionRequestDTO requestDTO;
    private Valoracion valoracion;

    @BeforeEach
    void setUp() {
        requestDTO = new ValoracionRequestDTO();
        requestDTO.setLibroId(10L);
        requestDTO.setEmailUsuario("diego@biblioteca.com");
        requestDTO.setPuntuacion(5);
        requestDTO.setComentario("Excelente libro");

        valoracion = Valoracion.builder()
                .id(1L).libroId(10L).emailUsuario("diego@biblioteca.com")
                .puntuacion(5).comentario("Excelente libro").build();
    }

    private FeignException.NotFound notFound() {
        Request request = Request.create(Request.HttpMethod.GET, "/",
                Collections.emptyMap(), new byte[0], StandardCharsets.UTF_8, null);
        return new FeignException.NotFound("not found", request, null, null);
    }

    @Test
    @DisplayName("crearValoracion guarda cuando usuario y libro existen y no hay valoracion previa")
    void crear_ok() {
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenReturn(new UsuarioDto());
        when(libroClient.obtenerPorId(10L)).thenReturn(new LibroDto());
        when(repository.existsByLibroIdAndEmailUsuario(10L, "diego@biblioteca.com")).thenReturn(false);
        when(repository.save(any(Valoracion.class))).thenReturn(valoracion);

        ValoracionResponseDTO resp = service.crearValoracion(requestDTO);

        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getPuntuacion()).isEqualTo(5);
        verify(repository).save(any(Valoracion.class));
    }

    @Test
    @DisplayName("crearValoracion lanza IllegalArgument si el usuario ya valoro el libro")
    void crear_duplicada() {
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenReturn(new UsuarioDto());
        when(libroClient.obtenerPorId(10L)).thenReturn(new LibroDto());
        when(repository.existsByLibroIdAndEmailUsuario(10L, "diego@biblioteca.com")).thenReturn(true);

        assertThatThrownBy(() -> service.crearValoracion(requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya valoraste");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("crearValoracion lanza ResourceNotFound si el usuario no existe (Feign 404)")
    void crear_usuarioNoExiste() {
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenThrow(notFound());

        assertThatThrownBy(() -> service.crearValoracion(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(libroClient, never()).obtenerPorId(any());
    }

    @Test
    @DisplayName("crearValoracion lanza ResourceNotFound si el libro no existe (Feign 404)")
    void crear_libroNoExiste() {
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenReturn(new UsuarioDto());
        when(libroClient.obtenerPorId(10L)).thenThrow(notFound());

        assertThatThrownBy(() -> service.crearValoracion(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("promedioPorLibro devuelve promedio y cantidad")
    void promedio_ok() {
        when(repository.promedioPorLibro(10L)).thenReturn(4.5);
        when(repository.findByLibroId(10L)).thenReturn(List.of(valoracion, valoracion));

        Map<String, Object> result = service.promedioPorLibro(10L);

        assertThat(result.get("libroId")).isEqualTo(10L);
        assertThat(result.get("promedio")).isEqualTo(4.5);
        assertThat(result.get("cantidad")).isEqualTo(2L);
    }

    @Test
    @DisplayName("promedioPorLibro devuelve 0.0 cuando no hay valoraciones")
    void promedio_sinValoraciones() {
        when(repository.promedioPorLibro(10L)).thenReturn(null);
        when(repository.findByLibroId(10L)).thenReturn(List.of());

        Map<String, Object> result = service.promedioPorLibro(10L);

        assertThat(result.get("promedio")).isEqualTo(0.0);
        assertThat(result.get("cantidad")).isEqualTo(0L);
    }

    @Test
    @DisplayName("buscarPorId lanza ResourceNotFound cuando no existe")
    void buscarPorId_noExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("actualizarValoracion modifica la valoracion existente")
    void actualizar_ok() {
        when(repository.findById(1L)).thenReturn(Optional.of(valoracion));
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenReturn(new UsuarioDto());
        when(libroClient.obtenerPorId(10L)).thenReturn(new LibroDto());
        when(repository.save(any(Valoracion.class))).thenAnswer(inv -> inv.getArgument(0));

        requestDTO.setPuntuacion(3);
        ValoracionResponseDTO resp = service.actualizarValoracion(1L, requestDTO);

        assertThat(resp.getPuntuacion()).isEqualTo(3);
    }

    @Test
    @DisplayName("actualizarValoracion lanza ResourceNotFound cuando no existe")
    void actualizar_noExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarValoracion(99L, requestDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("eliminarValoracion borra cuando existe")
    void eliminar_ok() {
        when(repository.findById(1L)).thenReturn(Optional.of(valoracion));

        service.eliminarValoracion(1L);

        verify(repository).delete(valoracion);
    }

    @Test
    @DisplayName("listarPorLibro delega en el repositorio")
    void listarPorLibro_ok() {
        when(repository.findByLibroId(10L)).thenReturn(List.of(valoracion));

        assertThat(service.listarPorLibro(10L)).hasSize(1);
    }
}
