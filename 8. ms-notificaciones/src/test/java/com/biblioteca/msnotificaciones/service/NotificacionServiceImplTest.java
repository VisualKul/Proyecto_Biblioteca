package com.biblioteca.msnotificaciones.service;

import com.biblioteca.msnotificaciones.client.PrestamoClient;
import com.biblioteca.msnotificaciones.client.UserClient;
import com.biblioteca.msnotificaciones.dto.NotificacionRequestDTO;
import com.biblioteca.msnotificaciones.dto.NotificacionResponseDTO;
import com.biblioteca.msnotificaciones.dto.PrestamoDto;
import com.biblioteca.msnotificaciones.dto.UsuarioDto;
import com.biblioteca.msnotificaciones.entity.Notificacion;
import com.biblioteca.msnotificaciones.exception.ResourceNotFoundException;
import com.biblioteca.msnotificaciones.repository.NotificacionRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificacionServiceImpl - tests unitarios")
class NotificacionServiceImplTest {

    @Mock private NotificacionRepository repository;
    @Mock private UserClient userClient;
    @Mock private PrestamoClient prestamoClient;

    @InjectMocks
    private NotificacionServiceImpl service;

    private NotificacionRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new NotificacionRequestDTO();
        requestDTO.setEmailUsuario("diego@biblioteca.com");
        requestDTO.setMensaje("Mensaje de prueba");
        requestDTO.setTipo("GENERICO");
    }

    private FeignException.NotFound notFound() {
        Request request = Request.create(Request.HttpMethod.GET, "/",
                Collections.emptyMap(), new byte[0], StandardCharsets.UTF_8, null);
        return new FeignException.NotFound("not found", request, null, null);
    }

    @Test
    @DisplayName("crearNotificacion guarda con estado PENDIENTE cuando el usuario existe")
    void crear_ok() {
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenReturn(new UsuarioDto());
        when(repository.save(any(Notificacion.class))).thenAnswer(inv -> {
            Notificacion n = inv.getArgument(0);
            n.setId(1L);
            return n;
        });

        NotificacionResponseDTO resp = service.crearNotificacion(requestDTO);

        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getEstado()).isEqualTo("PENDIENTE");
        assertThat(resp.getTipo()).isEqualTo("GENERICO");
    }

    @Test
    @DisplayName("crearNotificacion lanza ResourceNotFound si el usuario no existe (Feign 404)")
    void crear_usuarioNoExiste() {
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenThrow(notFound());

        assertThatThrownBy(() -> service.crearNotificacion(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("notificarVencimiento genera notificacion VENCIMIENTO_PROXIMO a partir del prestamo")
    void notificarVencimiento_ok() {
        PrestamoDto prestamo = new PrestamoDto();
        prestamo.setEmailUsuario("diego@biblioteca.com");
        prestamo.setLibroId(5L);
        prestamo.setFechaDevolucion(LocalDate.now().plusDays(2));
        when(prestamoClient.obtenerPorId(10L)).thenReturn(prestamo);
        when(repository.save(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificacionResponseDTO resp = service.notificarVencimiento(10L);

        assertThat(resp.getTipo()).isEqualTo("VENCIMIENTO_PROXIMO");
        assertThat(resp.getEmailUsuario()).isEqualTo("diego@biblioteca.com");
        assertThat(resp.getMensaje()).contains("vence en");
        assertThat(resp.getRecursoId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("notificarVencimiento lanza ResourceNotFound si el prestamo no existe (Feign 404)")
    void notificarVencimiento_prestamoNoExiste() {
        when(prestamoClient.obtenerPorId(10L)).thenThrow(notFound());

        assertThatThrownBy(() -> service.notificarVencimiento(10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("notificarVencimiento lanza ResourceNotFound si el prestamo no tiene fecha de devolucion")
    void notificarVencimiento_sinFecha() {
        PrestamoDto prestamo = new PrestamoDto();
        prestamo.setEmailUsuario("diego@biblioteca.com");
        prestamo.setFechaDevolucion(null);
        when(prestamoClient.obtenerPorId(10L)).thenReturn(prestamo);

        assertThatThrownBy(() -> service.notificarVencimiento(10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("listarPorUsuario delega en el repositorio")
    void listarPorUsuario_ok() {
        Notificacion n = Notificacion.builder().id(1L).emailUsuario("diego@biblioteca.com")
                .mensaje("m").tipo("GENERICO").estado("PENDIENTE").build();
        when(repository.findByEmailUsuario("diego@biblioteca.com")).thenReturn(List.of(n));

        assertThat(service.listarPorUsuario("diego@biblioteca.com")).hasSize(1);
    }

    @Test
    @DisplayName("buscarPorId lanza ResourceNotFound cuando no existe")
    void buscarPorId_noExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("marcarEnviada cambia el estado a ENVIADA")
    void marcarEnviada_ok() {
        Notificacion n = Notificacion.builder().id(1L).emailUsuario("diego@biblioteca.com")
                .mensaje("m").tipo("GENERICO").estado("PENDIENTE").build();
        when(repository.findById(1L)).thenReturn(Optional.of(n));
        when(repository.save(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificacionResponseDTO resp = service.marcarEnviada(1L);

        assertThat(resp.getEstado()).isEqualTo("ENVIADA");
    }

    @Test
    @DisplayName("eliminarNotificacion borra cuando existe")
    void eliminar_ok() {
        Notificacion n = Notificacion.builder().id(1L).build();
        when(repository.findById(1L)).thenReturn(Optional.of(n));

        service.eliminarNotificacion(1L);

        verify(repository).delete(n);
    }

    @Test
    @DisplayName("eliminarNotificacion lanza ResourceNotFound cuando no existe")
    void eliminar_noExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarNotificacion(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
