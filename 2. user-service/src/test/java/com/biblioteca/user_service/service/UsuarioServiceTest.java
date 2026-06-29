package com.biblioteca.user_service.service;

import com.biblioteca.user_service.dto.UsuarioCreateDTO;
import com.biblioteca.user_service.dto.UsuarioUpdateDTO;
import com.biblioteca.user_service.model.UsuarioModelo;
import com.biblioteca.user_service.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - tests unitarios")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @InjectMocks
    private UsuarioService service;

    private UsuarioModelo usuario;

    @BeforeEach
    void setUp() {
        usuario = new UsuarioModelo();
        usuario.setId(1L);
        usuario.setNombre("Diego");
        usuario.setEmail("diego@biblioteca.com");
        usuario.setPassword("hash");
        usuario.setTelefono("123456");
    }

    @Test
    @DisplayName("crearUsuario guarda cuando el email no existe")
    void crear_ok() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO();
        dto.setNombre("Ana");
        dto.setEmail("ana@biblioteca.com");
        dto.setPassword("1234");
        dto.setTelefono("999999");

        when(repository.findByEmail("ana@biblioteca.com")).thenReturn(Optional.empty());
        when(repository.save(any(UsuarioModelo.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioModelo creado = service.crearUsuario(dto);

        ArgumentCaptor<UsuarioModelo> captor = ArgumentCaptor.forClass(UsuarioModelo.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("ana@biblioteca.com");
        assertThat(creado.getNombre()).isEqualTo("Ana");
    }

    @Test
    @DisplayName("crearUsuario lanza excepcion con email duplicado")
    void crear_emailDuplicado() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO();
        dto.setEmail("diego@biblioteca.com");
        when(repository.findByEmail("diego@biblioteca.com")).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> service.crearUsuario(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("actualizarUsuario modifica solo campos no nulos")
    void actualizar_ok() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO();
        dto.setNombre("Diego Editado");

        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(repository.save(any(UsuarioModelo.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioModelo actualizado = service.actualizarUsuario(1L, dto);

        assertThat(actualizado.getNombre()).isEqualTo("Diego Editado");
        assertThat(actualizado.getEmail()).isEqualTo("diego@biblioteca.com");
    }

    @Test
    @DisplayName("actualizarUsuario lanza excepcion cuando no existe")
    void actualizar_noExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarUsuario(99L, new UsuarioUpdateDTO()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("eliminarUsuario borra cuando existe")
    void eliminar_ok() {
        when(repository.existsById(1L)).thenReturn(true);

        service.eliminarUsuario(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminarUsuario lanza excepcion cuando no existe")
    void eliminar_noExiste() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.eliminarUsuario(99L))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("obtenerPorEmail devuelve null cuando no existe")
    void obtenerPorEmail_noExiste() {
        when(repository.findByEmail("x@x.com")).thenReturn(Optional.empty());

        assertThat(service.obtenerPorEmail("x@x.com")).isNull();
    }

    @Test
    @DisplayName("obtenerPorId devuelve el usuario cuando existe")
    void obtenerPorId_ok() {
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThat(service.obtenerPorId(1L)).isEqualTo(usuario);
    }

    @Test
    @DisplayName("listarTodos delega en el repositorio")
    void listar_ok() {
        when(repository.findAll()).thenReturn(List.of(usuario));

        assertThat(service.listarTodos()).containsExactly(usuario);
    }
}
