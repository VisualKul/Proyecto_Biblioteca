package com.biblioteca.ms_inventario.repository;

import com.biblioteca.ms_inventario.model.Libro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("LibroRepository - tests de persistencia (@DataJpaTest + H2)")
class LibroRepositoryTest {

    @Autowired
    private LibroRepository repository;

    private Libro nuevo(String titulo, String isbn) {
        Libro l = new Libro();
        l.setTitulo(titulo);
        l.setAutor("Autor");
        l.setIsbn(isbn);
        l.setEditorial("Ed");
        l.setStock(1);
        return l;
    }

    @Test
    @DisplayName("save persiste y findByIsbn lo recupera")
    void guardarYBuscarPorIsbn() {
        repository.save(nuevo("Don Quijote", "1111111111"));

        Optional<Libro> encontrado = repository.findByIsbn("1111111111");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getTitulo()).isEqualTo("Don Quijote");
    }

    @Test
    @DisplayName("findByIsbn devuelve vacio cuando no existe")
    void buscarPorIsbnInexistente() {
        assertThat(repository.findByIsbn("0000000000")).isEmpty();
    }

    @Test
    @DisplayName("findByTituloContainingIgnoreCase ignora mayusculas/minusculas")
    void buscarPorTituloParcial() {
        repository.save(nuevo("El Quijote de la Mancha", "2222222222"));
        repository.save(nuevo("Cien Anos de Soledad", "3333333333"));

        List<Libro> resultado = repository.findByTituloContainingIgnoreCase("quijote");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIsbn()).isEqualTo("2222222222");
    }
}
