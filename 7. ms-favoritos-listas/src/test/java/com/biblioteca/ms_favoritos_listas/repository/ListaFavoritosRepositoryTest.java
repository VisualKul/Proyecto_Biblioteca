package com.biblioteca.ms_favoritos_listas.repository;

import com.biblioteca.ms_favoritos_listas.model.ListaFavoritos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("ListaFavoritosRepository - tests de persistencia (@DataJpaTest + H2)")
class ListaFavoritosRepositoryTest {

    @Autowired
    private ListaFavoritosRepository repository;

    private ListaFavoritos nueva(String nombre, String email, boolean publica) {
        return ListaFavoritos.builder()
                .nombre(nombre).descripcion("desc").emailUsuario(email).publica(publica).build();
    }

    @Test
    @DisplayName("findByNombreAndEmailUsuario recupera la lista exacta")
    void buscarPorNombreYEmail() {
        repository.save(nueva("Favoritos", "diego@biblioteca.com", true));

        Optional<ListaFavoritos> encontrado =
                repository.findByNombreAndEmailUsuario("Favoritos", "diego@biblioteca.com");

        assertThat(encontrado).isPresent();
    }

    @Test
    @DisplayName("findByEmailUsuario devuelve las listas del usuario")
    void buscarPorEmail() {
        repository.save(nueva("L1", "diego@biblioteca.com", true));
        repository.save(nueva("L2", "diego@biblioteca.com", false));
        repository.save(nueva("L3", "otro@biblioteca.com", true));

        List<ListaFavoritos> resultado = repository.findByEmailUsuario("diego@biblioteca.com");

        assertThat(resultado).hasSize(2);
    }

    @Test
    @DisplayName("findByPublicaTrue devuelve solo listas publicas")
    void buscarPublicas() {
        repository.save(nueva("Publica", "diego@biblioteca.com", true));
        repository.save(nueva("Privada", "diego@biblioteca.com", false));

        List<ListaFavoritos> publicas = repository.findByPublicaTrue();

        assertThat(publicas).hasSize(1);
        assertThat(publicas.get(0).getNombre()).isEqualTo("Publica");
    }
}
