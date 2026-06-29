package com.biblioteca.user_service.repository;

import com.biblioteca.user_service.model.UsuarioModelo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UsuarioRepository - tests de persistencia (@DataJpaTest + H2)")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository repository;

    private UsuarioModelo nuevo(String email) {
        UsuarioModelo u = new UsuarioModelo();
        u.setNombre("Test");
        u.setEmail(email);
        u.setPassword("hash");
        u.setTelefono("123456");
        return u;
    }

    @Test
    @DisplayName("save persiste y findByEmail lo recupera")
    void guardarYBuscarPorEmail() {
        repository.save(nuevo("diego@biblioteca.com"));

        Optional<UsuarioModelo> encontrado = repository.findByEmail("diego@biblioteca.com");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNombre()).isEqualTo("Test");
    }

    @Test
    @DisplayName("prePersist asigna la fecha de registro")
    void prePersistAsignaFecha() {
        UsuarioModelo guardado = repository.save(nuevo("fecha@biblioteca.com"));

        assertThat(guardado.getFechaRegistro()).isNotNull();
    }

    @Test
    @DisplayName("findByEmail devuelve vacio cuando no existe")
    void buscarPorEmailInexistente() {
        assertThat(repository.findByEmail("nada@x.com")).isEmpty();
    }
}
