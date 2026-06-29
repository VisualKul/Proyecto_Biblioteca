package com.biblioteca.security_service.repository;

import com.biblioteca.security_service.model.Rol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("RolRepository - tests de persistencia (@DataJpaTest + H2)")
class RolRepositoryTest {

    @Autowired
    private RolRepository repository;

    @Test
    @DisplayName("save persiste y findByNombre lo recupera")
    void guardarYBuscarPorNombre() {
        Rol rol = new Rol();
        rol.setNombre("ROLE_ADMIN");
        repository.save(rol);

        Optional<Rol> encontrado = repository.findByNombre("ROLE_ADMIN");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getId()).isNotNull();
    }

    @Test
    @DisplayName("findByNombre devuelve vacio cuando no existe")
    void buscarPorNombreInexistente() {
        assertThat(repository.findByNombre("ROLE_NADA")).isEmpty();
    }
}
