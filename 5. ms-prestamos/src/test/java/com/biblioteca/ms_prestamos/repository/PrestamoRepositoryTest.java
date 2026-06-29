package com.biblioteca.ms_prestamos.repository;

import com.biblioteca.ms_prestamos.model.Prestamo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("PrestamoRepository - tests de persistencia (@DataJpaTest + H2)")
class PrestamoRepositoryTest {

    @Autowired
    private PrestamoRepository repository;

    private Prestamo nuevo(String email, Long libroId) {
        Prestamo p = new Prestamo();
        p.setEmailUsuario(email);
        p.setLibroId(libroId);
        p.setFechaPrestamo(LocalDate.now());
        p.setEstado("ACTIVO");
        return p;
    }

    @Test
    @DisplayName("findByEmailUsuario devuelve solo los prestamos de ese correo")
    void buscarPorEmail() {
        repository.save(nuevo("diego@biblioteca.com", 1L));
        repository.save(nuevo("diego@biblioteca.com", 2L));
        repository.save(nuevo("otro@biblioteca.com", 3L));

        List<Prestamo> resultado = repository.findByEmailUsuario("diego@biblioteca.com");

        assertThat(resultado).hasSize(2);
        assertThat(resultado).allMatch(p -> p.getEmailUsuario().equals("diego@biblioteca.com"));
    }

    @Test
    @DisplayName("findByEmailUsuario devuelve lista vacia cuando no hay prestamos")
    void buscarPorEmailSinResultados() {
        assertThat(repository.findByEmailUsuario("nadie@x.com")).isEmpty();
    }
}
