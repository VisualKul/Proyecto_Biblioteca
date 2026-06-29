package example.ms_sugerencias.service;

import example.ms_sugerencias.client.LibroClient;
import example.ms_sugerencias.client.UserClient;
import example.ms_sugerencias.dto.*;
import example.ms_sugerencias.exception.BadRequestException;
import example.ms_sugerencias.exception.ResourceNotFoundException;
import example.ms_sugerencias.model.Sugerencia;
import example.ms_sugerencias.repository.SugerenciaRepository;
import example.ms_sugerencias.service.impl.SugerenciaServiceImpl;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SugerenciaServiceImpl - tests unitarios")
class SugerenciaServiceImplTest {

    @Mock private SugerenciaRepository repository;
    @Mock private UserClient userClient;
    @Mock private LibroClient libroClient;

    @InjectMocks
    private SugerenciaServiceImpl service;

    private SugerenciaRequestDTO requestDTO;
    private Sugerencia sugerencia;

    @BeforeEach
    void setUp() {
        requestDTO = new SugerenciaRequestDTO();
        requestDTO.setTitulo("Clean Code");
        requestDTO.setAutor("Robert Martin");
        requestDTO.setIsbn("9780132350884");
        requestDTO.setComentario("Imprescindible");
        requestDTO.setEmailSocio("diego@biblioteca.com");

        sugerencia = Sugerencia.builder()
                .id(1L).titulo("Clean Code").autor("Robert Martin").isbn("9780132350884")
                .comentario("Imprescindible").estado("PENDIENTE").emailSocio("diego@biblioteca.com")
                .build();
    }

    private FeignException.NotFound notFound() {
        Request request = Request.create(Request.HttpMethod.GET, "/",
                Collections.emptyMap(), new byte[0], StandardCharsets.UTF_8, null);
        return new FeignException.NotFound("not found", request, null, null);
    }

    @Test
    @DisplayName("crear guarda la sugerencia cuando el socio existe y el ISBN es nuevo")
    void crear_ok() {
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenReturn(new UsuarioDto());
        when(libroClient.buscarPorIsbn("9780132350884")).thenThrow(notFound());
        when(repository.findByIsbn("9780132350884")).thenReturn(Optional.empty());
        when(repository.save(any(Sugerencia.class))).thenReturn(sugerencia);

        SugerenciaResponseDTO resp = service.crear(requestDTO);

        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getEstado()).isEqualTo("PENDIENTE");
        verify(repository).save(any(Sugerencia.class));
    }

    @Test
    @DisplayName("crear lanza BadRequest si el ISBN ya esta en el catalogo de inventario")
    void crear_yaEnInventario() {
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenReturn(new UsuarioDto());
        when(libroClient.buscarPorIsbn("9780132350884")).thenReturn(new LibroDto());

        assertThatThrownBy(() -> service.crear(requestDTO))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("catalogo");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("crear lanza BadRequest si ya existe una sugerencia con ese ISBN")
    void crear_sugerenciaDuplicada() {
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenReturn(new UsuarioDto());
        when(libroClient.buscarPorIsbn("9780132350884")).thenThrow(notFound());
        when(repository.findByIsbn("9780132350884")).thenReturn(Optional.of(sugerencia));

        assertThatThrownBy(() -> service.crear(requestDTO))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Ya existe una sugerencia");
    }

    @Test
    @DisplayName("crear lanza ResourceNotFound si el socio no existe (Feign 404)")
    void crear_socioNoExiste() {
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenThrow(notFound());

        assertThatThrownBy(() -> service.crear(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("buscarPorId lanza ResourceNotFound cuando no existe")
    void buscarPorId_noExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("cambiarEstado actualiza el estado de la sugerencia")
    void cambiarEstado_ok() {
        SugerenciaEstadoDTO estadoDTO = new SugerenciaEstadoDTO();
        estadoDTO.setEstado("APROBADA");
        when(repository.findById(1L)).thenReturn(Optional.of(sugerencia));
        when(repository.save(any(Sugerencia.class))).thenAnswer(inv -> inv.getArgument(0));

        SugerenciaResponseDTO resp = service.cambiarEstado(1L, estadoDTO);

        assertThat(resp.getEstado()).isEqualTo("APROBADA");
    }

    @Test
    @DisplayName("actualizar modifica la sugerencia existente")
    void actualizar_ok() {
        when(repository.findById(1L)).thenReturn(Optional.of(sugerencia));
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenReturn(new UsuarioDto());
        when(repository.save(any(Sugerencia.class))).thenAnswer(inv -> inv.getArgument(0));

        requestDTO.setTitulo("Clean Code (2da ed)");
        SugerenciaResponseDTO resp = service.actualizar(1L, requestDTO);

        assertThat(resp.getTitulo()).isEqualTo("Clean Code (2da ed)");
    }

    @Test
    @DisplayName("actualizar lanza ResourceNotFound cuando no existe")
    void actualizar_noExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar(99L, requestDTO))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(userClient, never()).obtenerPorEmail(anyString());
    }

    @Test
    @DisplayName("eliminar borra cuando la sugerencia existe")
    void eliminar_ok() {
        when(repository.findById(1L)).thenReturn(Optional.of(sugerencia));

        service.eliminar(1L);

        verify(repository).delete(sugerencia);
    }

    @Test
    @DisplayName("listarPorEstado delega en el repositorio")
    void listarPorEstado_ok() {
        when(repository.findByEstado("PENDIENTE")).thenReturn(List.of(sugerencia));

        assertThat(service.listarPorEstado("PENDIENTE")).hasSize(1);
    }
}
