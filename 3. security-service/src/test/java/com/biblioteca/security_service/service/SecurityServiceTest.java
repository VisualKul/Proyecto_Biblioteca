package com.biblioteca.security_service.service;

import com.biblioteca.security_service.client.UserClient;
import com.biblioteca.security_service.dto.AsignarRolDTO;
import com.biblioteca.security_service.dto.RolCreateDTO;
import com.biblioteca.security_service.dto.UsuarioDto;
import com.biblioteca.security_service.model.Rol;
import com.biblioteca.security_service.model.UsuarioRol;
import com.biblioteca.security_service.repository.RolRepository;
import com.biblioteca.security_service.repository.UsuarioRolRepository;
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
@DisplayName("SecurityService - tests unitarios")
class SecurityServiceTest {

    @Mock private UsuarioRolRepository usuarioRolRepository;
    @Mock private RolRepository rolRepository;
    @Mock private UserClient userClient;

    @InjectMocks
    private SecurityService service;

    private Rol rol;

    @BeforeEach
    void setUp() {
        rol = new Rol();
        rol.setId(1L);
        rol.setNombre("ROLE_ADMIN");
    }

    private FeignException.NotFound notFound() {
        Request request = Request.create(Request.HttpMethod.GET, "/",
                Collections.emptyMap(), new byte[0], StandardCharsets.UTF_8, null);
        return new FeignException.NotFound("not found", request, null, null);
    }

    @Test
    @DisplayName("crearRol guarda cuando el nombre no existe")
    void crearRol_ok() {
        RolCreateDTO dto = new RolCreateDTO();
        dto.setNombre("ROLE_SOCIO");
        when(rolRepository.findByNombre("ROLE_SOCIO")).thenReturn(Optional.empty());
        when(rolRepository.save(any(Rol.class))).thenAnswer(inv -> inv.getArgument(0));

        Rol creado = service.crearRol(dto);

        assertThat(creado.getNombre()).isEqualTo("ROLE_SOCIO");
        verify(rolRepository).save(any(Rol.class));
    }

    @Test
    @DisplayName("crearRol lanza excepcion con nombre duplicado")
    void crearRol_duplicado() {
        RolCreateDTO dto = new RolCreateDTO();
        dto.setNombre("ROLE_ADMIN");
        when(rolRepository.findByNombre("ROLE_ADMIN")).thenReturn(Optional.of(rol));

        assertThatThrownBy(() -> service.crearRol(dto))
                .isInstanceOf(IllegalArgumentException.class);
        verify(rolRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizarRol cambia el nombre cuando no choca con otro rol")
    void actualizarRol_ok() {
        RolCreateDTO dto = new RolCreateDTO();
        dto.setNombre("ROLE_NUEVO");
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(rolRepository.findByNombre("ROLE_NUEVO")).thenReturn(Optional.empty());
        when(rolRepository.save(any(Rol.class))).thenAnswer(inv -> inv.getArgument(0));

        Rol actualizado = service.actualizarRol(1L, dto);

        assertThat(actualizado.getNombre()).isEqualTo("ROLE_NUEVO");
    }

    @Test
    @DisplayName("actualizarRol lanza excepcion si el nombre pertenece a otro rol")
    void actualizarRol_nombreDeOtro() {
        Rol otro = new Rol();
        otro.setId(2L);
        otro.setNombre("ROLE_X");
        RolCreateDTO dto = new RolCreateDTO();
        dto.setNombre("ROLE_X");
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(rolRepository.findByNombre("ROLE_X")).thenReturn(Optional.of(otro));

        assertThatThrownBy(() -> service.actualizarRol(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("otro rol");
    }

    @Test
    @DisplayName("actualizarRol lanza excepcion cuando el rol no existe")
    void actualizarRol_noExiste() {
        when(rolRepository.findById(99L)).thenReturn(Optional.empty());
        RolCreateDTO dto = new RolCreateDTO();
        dto.setNombre("ROLE_Y");

        assertThatThrownBy(() -> service.actualizarRol(99L, dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("eliminarRol lanza excepcion cuando no existe")
    void eliminarRol_noExiste() {
        when(rolRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.eliminarRol(99L))
                .isInstanceOf(IllegalArgumentException.class);
        verify(rolRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("obtenerRolesDeUsuario devuelve los nombres de los roles")
    void obtenerRolesDeUsuario_ok() {
        UsuarioRol ur = new UsuarioRol();
        ur.setId(1L);
        ur.setUsuarioId(10L);
        ur.setRol(rol);
        when(usuarioRolRepository.findByUsuarioId(10L)).thenReturn(List.of(ur));

        List<String> roles = service.obtenerRolesDeUsuario(10L);

        assertThat(roles).containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("usuarioExiste devuelve true cuando user-service responde")
    void usuarioExiste_true() {
        when(userClient.obtenerPorId(10L)).thenReturn(new UsuarioDto());

        assertThat(service.usuarioExiste(10L)).isTrue();
    }

    @Test
    @DisplayName("usuarioExiste devuelve false cuando user-service responde 404")
    void usuarioExiste_false() {
        when(userClient.obtenerPorId(99L)).thenThrow(notFound());

        assertThat(service.usuarioExiste(99L)).isFalse();
    }

    @Test
    @DisplayName("asignarRol crea la asignacion cuando usuario y rol existen")
    void asignarRol_ok() {
        AsignarRolDTO dto = new AsignarRolDTO();
        dto.setUsuarioId(10L);
        dto.setRolId(1L);
        when(userClient.obtenerPorId(10L)).thenReturn(new UsuarioDto());
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(usuarioRolRepository.save(any(UsuarioRol.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioRol asignado = service.asignarRol(dto);

        assertThat(asignado.getUsuarioId()).isEqualTo(10L);
        assertThat(asignado.getRol()).isEqualTo(rol);
    }

    @Test
    @DisplayName("asignarRol lanza excepcion si el usuario no existe")
    void asignarRol_usuarioNoExiste() {
        AsignarRolDTO dto = new AsignarRolDTO();
        dto.setUsuarioId(99L);
        dto.setRolId(1L);
        when(userClient.obtenerPorId(99L)).thenThrow(notFound());

        assertThatThrownBy(() -> service.asignarRol(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuario no encontrado");
        verify(usuarioRolRepository, never()).save(any());
    }

    @Test
    @DisplayName("asignarRol lanza excepcion si el rol no existe")
    void asignarRol_rolNoExiste() {
        AsignarRolDTO dto = new AsignarRolDTO();
        dto.setUsuarioId(10L);
        dto.setRolId(99L);
        when(userClient.obtenerPorId(10L)).thenReturn(new UsuarioDto());
        when(rolRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.asignarRol(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rol no encontrado");
    }

    @Test
    @DisplayName("actualizarAsignacion lanza excepcion cuando la asignacion no existe")
    void actualizarAsignacion_noExiste() {
        when(usuarioRolRepository.findById(99L)).thenReturn(Optional.empty());
        AsignarRolDTO dto = new AsignarRolDTO();
        dto.setUsuarioId(10L);
        dto.setRolId(1L);

        assertThatThrownBy(() -> service.actualizarAsignacion(99L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Asignacion no encontrada");
    }

    @Test
    @DisplayName("quitarRol elimina cuando la asignacion existe")
    void quitarRol_ok() {
        when(usuarioRolRepository.existsById(1L)).thenReturn(true);

        service.quitarRol(1L);

        verify(usuarioRolRepository).deleteById(1L);
    }

    @Test
    @DisplayName("quitarRol lanza excepcion cuando no existe")
    void quitarRol_noExiste() {
        when(usuarioRolRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.quitarRol(99L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
