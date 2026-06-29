package com.biblioteca.ms_multas.repository;

import com.biblioteca.ms_multas.model.Multa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("MultaRepository - tests de persistencia (@DataJpaTest + H2)")
class MultaRepositoryTest {

    @Autowired
    private MultaRepository repository;

    private Multa nueva(String email, Long prestamoId, String estado) {
        Multa m = new Multa();
        m.setEmailUsuario(email);
        m.setPrestamoId(prestamoId);
        m.setDiasRetraso(3);
        m.setMonto(new BigDecimal("1500"));
        m.setFechaGeneracion(LocalDate.now());
        m.setEstado(estado);
        return m;
    }

    @Test
    @DisplayName("findByEmailUsuarioAndEstado filtra por correo y estado")
    void buscarPorEmailYEstado() {
        repository.save(nueva("diego@biblioteca.com", 1L, "PENDIENTE"));
        repository.save(nueva("diego@biblioteca.com", 2L, "PAGADA"));

        List<Multa> pendientes = repository.findByEmailUsuarioAndEstado("diego@biblioteca.com", "PENDIENTE");

        assertThat(pendientes).hasSize(1);
        assertThat(pendientes.get(0).getPrestamoId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("existsByEmailUsuarioAndEstado devuelve true cuando hay pendientes")
    void existePendiente() {
        repository.save(nueva("diego@biblioteca.com", 1L, "PENDIENTE"));

        assertThat(repository.existsByEmailUsuarioAndEstado("diego@biblioteca.com", "PENDIENTE")).isTrue();
        assertThat(repository.existsByEmailUsuarioAndEstado("diego@biblioteca.com", "PAGADA")).isFalse();
    }

    @Test
    @DisplayName("existsByPrestamoId evita multas duplicadas por prestamo")
    void existePorPrestamo() {
        repository.save(nueva("diego@biblioteca.com", 50L, "PENDIENTE"));

        assertThat(repository.existsByPrestamoId(50L)).isTrue();
        assertThat(repository.existsByPrestamoId(999L)).isFalse();
    }
}
