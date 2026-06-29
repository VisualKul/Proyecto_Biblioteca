package com.biblioteca.msvaloraciones.repository;

import com.biblioteca.msvaloraciones.entity.Valoracion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DataJpaTest
@DisplayName("ValoracionRepository - tests de persistencia (@DataJpaTest + H2)")
class ValoracionRepositoryTest {

    @Autowired
    private ValoracionRepository repository;

    private Valoracion nueva(Long libroId, String email, int puntuacion) {
        return Valoracion.builder()
                .libroId(libroId).emailUsuario(email).puntuacion(puntuacion)
                .comentario("comentario de prueba").fechaCreacion(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("findByLibroId devuelve las valoraciones del libro")
    void buscarPorLibro() {
        repository.save(nueva(10L, "diego@biblioteca.com", 5));
        repository.save(nueva(10L, "ana@biblioteca.com", 3));
        repository.save(nueva(20L, "diego@biblioteca.com", 4));

        List<Valoracion> resultado = repository.findByLibroId(10L);

        assertThat(resultado).hasSize(2);
    }

    @Test
    @DisplayName("existsByLibroIdAndEmailUsuario detecta valoraciones duplicadas")
    void existeValoracionUsuario() {
        repository.save(nueva(10L, "diego@biblioteca.com", 5));

        assertThat(repository.existsByLibroIdAndEmailUsuario(10L, "diego@biblioteca.com")).isTrue();
        assertThat(repository.existsByLibroIdAndEmailUsuario(10L, "otro@biblioteca.com")).isFalse();
    }

    @Test
    @DisplayName("promedioPorLibro calcula la media de puntuaciones")
    void promedio() {
        repository.save(nueva(10L, "diego@biblioteca.com", 5));
        repository.save(nueva(10L, "ana@biblioteca.com", 3));

        Double promedio = repository.promedioPorLibro(10L);

        assertThat(promedio).isCloseTo(4.0, within(0.001));
    }
}
