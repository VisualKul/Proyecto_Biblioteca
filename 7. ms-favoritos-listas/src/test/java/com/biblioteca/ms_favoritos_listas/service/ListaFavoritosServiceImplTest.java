package com.biblioteca.ms_favoritos_listas.service;

import com.biblioteca.ms_favoritos_listas.client.UserClient;
import com.biblioteca.ms_favoritos_listas.dto.ListaFavoritosRequestDTO;
import com.biblioteca.ms_favoritos_listas.dto.ListaFavoritosResponseDTO;
import com.biblioteca.ms_favoritos_listas.dto.UsuarioDto;
import com.biblioteca.ms_favoritos_listas.exception.DuplicateResourceException;
import com.biblioteca.ms_favoritos_listas.exception.ResourceNotFoundException;
import com.biblioteca.ms_favoritos_listas.model.ListaFavoritos;
import com.biblioteca.ms_favoritos_listas.repository.ListaFavoritosRepository;
import com.biblioteca.ms_favoritos_listas.service.impl.ListaFavoritosServiceImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListaFavoritosServiceImpl - tests unitarios")
class ListaFavoritosServiceImplTest {

    @Mock private ListaFavoritosRepository repository;
    @Mock private UserClient userClient;

    @InjectMocks
    private ListaFavoritosServiceImpl service;

    private ListaFavoritosRequestDTO requestDTO;
    private ListaFavoritos lista;

    @BeforeEach
    void setUp() {
        requestDTO = new ListaFavoritosRequestDTO();
        requestDTO.setNombre("Favoritos 2026");
        requestDTO.setDescripcion("Mis libros");
        requestDTO.setEmailUsuario("diego@biblioteca.com");
        requestDTO.setPublica(true);

        lista = ListaFavoritos.builder()
                .id(1L).nombre("Favoritos 2026").descripcion("Mis libros")
                .emailUsuario("diego@biblioteca.com").publica(true).build();
    }

    private FeignException.NotFound notFound() {
        Request request = Request.create(Request.HttpMethod.GET, "/",
                Collections.emptyMap(), new byte[0], StandardCharsets.UTF_8, null);
        return new FeignException.NotFound("not found", request, null, null);
    }

    @Test
    @DisplayName("crear guarda la lista cuando el usuario existe y no esta duplicada")
    void crear_ok() {
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenReturn(new UsuarioDto());
        when(repository.findByNombreAndEmailUsuario("Favoritos 2026", "diego@biblioteca.com"))
                .thenReturn(Optional.empty());
        when(repository.save(any(ListaFavoritos.class))).thenReturn(lista);

        ListaFavoritosResponseDTO resp = service.crear(requestDTO);

        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getNombre()).isEqualTo("Favoritos 2026");
        verify(repository).save(any(ListaFavoritos.class));
    }

    @Test
    @DisplayName("crear lanza DuplicateResourceException si la lista ya existe")
    void crear_duplicada() {
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenReturn(new UsuarioDto());
        when(repository.findByNombreAndEmailUsuario("Favoritos 2026", "diego@biblioteca.com"))
                .thenReturn(Optional.of(lista));

        assertThatThrownBy(() -> service.crear(requestDTO))
                .isInstanceOf(DuplicateResourceException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("crear lanza ResourceNotFoundException si el usuario no existe (Feign 404)")
    void crear_usuarioNoExiste() {
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenThrow(notFound());

        assertThatThrownBy(() -> service.crear(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("listar mapea todas las entidades a DTO")
    void listar_ok() {
        when(repository.findAll()).thenReturn(List.of(lista));

        List<ListaFavoritosResponseDTO> resp = service.listar();

        assertThat(resp).hasSize(1);
        assertThat(resp.get(0).getEmailUsuario()).isEqualTo("diego@biblioteca.com");
    }

    @Test
    @DisplayName("buscarPorId devuelve el DTO cuando existe")
    void buscarPorId_ok() {
        when(repository.findById(1L)).thenReturn(Optional.of(lista));

        assertThat(service.buscarPorId(1L).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("buscarPorId lanza ResourceNotFoundException cuando no existe")
    void buscarPorId_noExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("actualizar modifica la lista existente")
    void actualizar_ok() {
        when(repository.findById(1L)).thenReturn(Optional.of(lista));
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenReturn(new UsuarioDto());
        when(repository.save(any(ListaFavoritos.class))).thenAnswer(inv -> inv.getArgument(0));

        requestDTO.setNombre("Favoritos editados");
        ListaFavoritosResponseDTO resp = service.actualizar(1L, requestDTO);

        assertThat(resp.getNombre()).isEqualTo("Favoritos editados");
    }

    @Test
    @DisplayName("actualizar lanza ResourceNotFoundException cuando no existe")
    void actualizar_noExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar(99L, requestDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("eliminar borra cuando la lista existe")
    void eliminar_ok() {
        when(repository.findById(1L)).thenReturn(Optional.of(lista));

        service.eliminar(1L);

        verify(repository).delete(lista);
    }

    @Test
    @DisplayName("eliminar lanza ResourceNotFoundException cuando no existe")
    void eliminar_noExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("listarPublicas solo devuelve listas publicas")
    void listarPublicas_ok() {
        when(repository.findByPublicaTrue()).thenReturn(List.of(lista));

        assertThat(service.listarPublicas()).hasSize(1);
    }
}
