package com.biblioteca.ms_prestamos.service;

import com.biblioteca.ms_prestamos.client.LibroClient;
import com.biblioteca.ms_prestamos.client.MultaClient;
import com.biblioteca.ms_prestamos.client.UserClient;
import com.biblioteca.ms_prestamos.dto.*;
import com.biblioteca.ms_prestamos.exception.BusinessException;
import com.biblioteca.ms_prestamos.exception.ResourceNotFoundException;
import com.biblioteca.ms_prestamos.model.Prestamo;
import com.biblioteca.ms_prestamos.repository.PrestamoRepository;
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
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PrestamoService - tests unitarios")
class PrestamoServiceTest {

    @Mock private PrestamoRepository repository;
    @Mock private UserClient userClient;
    @Mock private LibroClient libroClient;
    @Mock private MultaClient multaClient;

    @InjectMocks
    private PrestamoService service;

    private PrestamoCreateDTO createDTO;
    private UsuarioDto usuario;
    private LibroDto libro;

    @BeforeEach
    void setUp() {
        createDTO = new PrestamoCreateDTO();
        createDTO.setEmailUsuario("diego@biblioteca.com");
        createDTO.setLibroId(1L);

        usuario = new UsuarioDto();
        usuario.setId(1L);
        usuario.setEmail("diego@biblioteca.com");

        libro = new LibroDto();
        libro.setId(1L);
        libro.setStock(5);
    }

    private FeignException.NotFound notFound() {
        Request request = Request.create(Request.HttpMethod.GET, "/",
                Collections.emptyMap(), new byte[0], StandardCharsets.UTF_8, null);
        return new FeignException.NotFound("not found", request, null, null);
    }

    @Test
    @DisplayName("registrarPrestamo crea el prestamo y descuenta stock en el flujo feliz")
    void registrar_ok() {
        MultasPendientesDto sinMultas = new MultasPendientesDto();
        sinMultas.setTienePendientes(false);
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenReturn(usuario);
        when(multaClient.consultarPendientes("diego@biblioteca.com")).thenReturn(sinMultas);
        when(libroClient.obtenerPorId(1L)).thenReturn(libro);
        when(repository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

        Prestamo resultado = service.registrarPrestamo(createDTO);

        assertThat(resultado.getEstado()).isEqualTo("ACTIVO");
        assertThat(resultado.getFechaPrestamo()).isEqualTo(LocalDate.now());
        verify(libroClient).descontarStock(1L);
        verify(repository).save(any(Prestamo.class));
    }

    @Test
    @DisplayName("registrarPrestamo lanza ResourceNotFound si el usuario no existe")
    void registrar_usuarioNoExiste() {
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenThrow(notFound());

        assertThatThrownBy(() -> service.registrarPrestamo(createDTO))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("registrarPrestamo bloquea si el usuario tiene multas pendientes")
    void registrar_conMultas() {
        MultasPendientesDto conMultas = new MultasPendientesDto();
        conMultas.setTienePendientes(true);
        conMultas.setCantidad(2L);
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenReturn(usuario);
        when(multaClient.consultarPendientes("diego@biblioteca.com")).thenReturn(conMultas);

        assertThatThrownBy(() -> service.registrarPrestamo(createDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("multa");
        verify(repository, never()).save(any());
        verify(libroClient, never()).descontarStock(anyLong());
    }

    @Test
    @DisplayName("registrarPrestamo lanza BusinessException si el libro no tiene stock")
    void registrar_sinStock() {
        libro.setStock(0);
        MultasPendientesDto sinMultas = new MultasPendientesDto();
        sinMultas.setTienePendientes(false);
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenReturn(usuario);
        when(multaClient.consultarPendientes("diego@biblioteca.com")).thenReturn(sinMultas);
        when(libroClient.obtenerPorId(1L)).thenReturn(libro);

        assertThatThrownBy(() -> service.registrarPrestamo(createDTO))
                .isInstanceOf(BusinessException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("registrarPrestamo lanza ResourceNotFound si el libro no existe")
    void registrar_libroNoExiste() {
        MultasPendientesDto sinMultas = new MultasPendientesDto();
        sinMultas.setTienePendientes(false);
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenReturn(usuario);
        when(multaClient.consultarPendientes("diego@biblioteca.com")).thenReturn(sinMultas);
        when(libroClient.obtenerPorId(1L)).thenThrow(notFound());

        assertThatThrownBy(() -> service.registrarPrestamo(createDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("actualizar a DEVUELTO restituye el stock del libro")
    void actualizar_devuelto() {
        Prestamo existente = new Prestamo();
        existente.setId(1L);
        existente.setLibroId(7L);
        existente.setEstado("ACTIVO");
        PrestamoUpdateDTO dto = new PrestamoUpdateDTO();
        dto.setEstado("DEVUELTO");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

        Prestamo actualizado = service.actualizar(1L, dto);

        assertThat(actualizado.getEstado()).isEqualTo("DEVUELTO");
        verify(libroClient).devolverStock(7L);
    }

    @Test
    @DisplayName("actualizar solo la fecha no toca el stock")
    void actualizar_soloFecha() {
        Prestamo existente = new Prestamo();
        existente.setId(1L);
        existente.setLibroId(7L);
        existente.setEstado("ACTIVO");
        PrestamoUpdateDTO dto = new PrestamoUpdateDTO();
        dto.setFechaDevolucion(LocalDate.now().plusDays(5));
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

        service.actualizar(1L, dto);

        verify(libroClient, never()).devolverStock(anyLong());
    }

    @Test
    @DisplayName("actualizar lanza excepcion cuando el prestamo no existe")
    void actualizar_noExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar(99L, new PrestamoUpdateDTO()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("eliminar borra cuando existe")
    void eliminar_ok() {
        when(repository.existsById(1L)).thenReturn(true);

        service.eliminar(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar lanza excepcion cuando no existe")
    void eliminar_noExiste() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("listarPorEmail delega en el repositorio")
    void listarPorEmail_ok() {
        Prestamo p = new Prestamo();
        when(repository.findByEmailUsuario("diego@biblioteca.com")).thenReturn(List.of(p));

        assertThat(service.listarPorEmail("diego@biblioteca.com")).containsExactly(p);
    }

    @Test
    @DisplayName("buscarPorId devuelve null cuando no existe")
    void buscarPorId_noExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThat(service.buscarPorId(99L)).isNull();
    }
}
