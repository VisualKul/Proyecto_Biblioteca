package com.biblioteca.auth_service.service;

import com.biblioteca.auth_service.client.SecurityClient;
import com.biblioteca.auth_service.client.UserClient;
import com.biblioteca.auth_service.dto.UsuarioDto;
import com.biblioteca.auth_service.model.LoginRecord;
import com.biblioteca.auth_service.repository.LoginRecordRepository;
import com.biblioteca.auth_service.security.JwtProvider;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - tests unitarios")
class AuthServiceTest {

    @Mock private UserClient userClient;
    @Mock private SecurityClient securityClient;
    @Mock private LoginRecordRepository loginRepository;
    @Mock private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService service;

    private UsuarioDto usuario;

    @BeforeEach
    void setUp() {
        usuario = new UsuarioDto();
        usuario.setId(1L);
        usuario.setEmail("diego@biblioteca.com");
        usuario.setPassword("secret");
    }

    private FeignException.NotFound notFound() {
        Request request = Request.create(Request.HttpMethod.GET, "/",
                Collections.emptyMap(), new byte[0], StandardCharsets.UTF_8, null);
        return new FeignException.NotFound("not found", request, null, null);
    }

    @Test
    @DisplayName("login exitoso devuelve token y registra el login como exitoso")
    void login_ok() {
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenReturn(usuario);
        when(securityClient.obtenerRoles(1L)).thenReturn(List.of("ADMIN"));
        when(jwtProvider.generarToken("diego@biblioteca.com", List.of("ADMIN"))).thenReturn("jwt-token");

        String token = service.login("diego@biblioteca.com", "secret");

        assertThat(token).isEqualTo("jwt-token");
        ArgumentCaptor<LoginRecord> captor = ArgumentCaptor.forClass(LoginRecord.class);
        verify(loginRepository).save(captor.capture());
        assertThat(captor.getValue().isExitoso()).isTrue();
        assertThat(captor.getValue().getEmail()).isEqualTo("diego@biblioteca.com");
    }

    @Test
    @DisplayName("login con password incorrecta lanza 'Credenciales invalidas' y registra fallo")
    void login_passwordIncorrecta() {
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenReturn(usuario);

        assertThatThrownBy(() -> service.login("diego@biblioteca.com", "wrong"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Credenciales invalidas");

        ArgumentCaptor<LoginRecord> captor = ArgumentCaptor.forClass(LoginRecord.class);
        verify(loginRepository).save(captor.capture());
        assertThat(captor.getValue().isExitoso()).isFalse();
        verify(jwtProvider, never()).generarToken(anyString(), anyList());
    }

    @Test
    @DisplayName("login con usuario inexistente (Feign 404) lanza 'Usuario no encontrado'")
    void login_usuarioNoEncontrado() {
        when(userClient.obtenerPorEmail("nadie@x.com")).thenThrow(notFound());

        assertThatThrownBy(() -> service.login("nadie@x.com", "secret"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");
        verify(loginRepository).save(any(LoginRecord.class));
    }

    @Test
    @DisplayName("login con usuario sin password lanza excepcion")
    void login_usuarioSinPassword() {
        usuario.setPassword(null);
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenReturn(usuario);

        assertThatThrownBy(() -> service.login("diego@biblioteca.com", "secret"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("invalido");
    }

    @Test
    @DisplayName("si security-service devuelve 404, el login continua con roles vacios")
    void login_sinRoles() {
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenReturn(usuario);
        when(securityClient.obtenerRoles(1L)).thenThrow(notFound());
        when(jwtProvider.generarToken("diego@biblioteca.com", Collections.emptyList())).thenReturn("jwt-sin-roles");

        String token = service.login("diego@biblioteca.com", "secret");

        assertThat(token).isEqualTo("jwt-sin-roles");
        verify(jwtProvider).generarToken("diego@biblioteca.com", Collections.emptyList());
    }
}
