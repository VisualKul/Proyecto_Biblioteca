package com.biblioteca.msnotificaciones.repository;

import com.biblioteca.msnotificaciones.entity.Notificacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("NotificacionRepository - tests de persistencia (@DataJpaTest + H2)")
class NotificacionRepositoryTest {

    @Autowired
    private NotificacionRepository repository;

    private Notificacion nueva(String email, String tipo) {
        return Notificacion.builder()
                .emailUsuario(email).mensaje("mensaje de prueba").tipo(tipo)
                .fechaEnvio(LocalDateTime.now()).estado("PENDIENTE").build();
    }

    @Test
    @DisplayName("findByEmailUsuario devuelve las notificaciones del usuario")
    void buscarPorEmail() {
        repository.save(nueva("diego@biblioteca.com", "GENERICO"));
        repository.save(nueva("diego@biblioteca.com", "MULTA_GENERADA"));
        repository.save(nueva("otro@biblioteca.com", "GENERICO"));

        List<Notificacion> resultado = repository.findByEmailUsuario("diego@biblioteca.com");

        assertThat(resultado).hasSize(2);
    }

    @Test
    @DisplayName("findByTipo filtra por tipo de notificacion")
    void buscarPorTipo() {
        repository.save(nueva("diego@biblioteca.com", "VENCIMIENTO_PROXIMO"));
        repository.save(nueva("diego@biblioteca.com", "GENERICO"));

        List<Notificacion> resultado = repository.findByTipo("VENCIMIENTO_PROXIMO");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipo()).isEqualTo("VENCIMIENTO_PROXIMO");
    }
}
