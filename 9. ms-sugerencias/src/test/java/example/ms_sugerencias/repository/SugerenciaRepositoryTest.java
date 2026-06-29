package example.ms_sugerencias.repository;

import example.ms_sugerencias.model.Sugerencia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("SugerenciaRepository - tests de persistencia (@DataJpaTest + H2)")
class SugerenciaRepositoryTest {

    @Autowired
    private SugerenciaRepository repository;

    private Sugerencia nueva(String isbn, String email, String estado) {
        return Sugerencia.builder()
                .titulo("Titulo").autor("Autor").isbn(isbn).comentario("c")
                .estado(estado).fechaSugerencia(LocalDateTime.now()).emailSocio(email).build();
    }

    @Test
    @DisplayName("findByIsbn recupera la sugerencia por su ISBN unico")
    void buscarPorIsbn() {
        repository.save(nueva("9780132350884", "diego@biblioteca.com", "PENDIENTE"));

        Optional<Sugerencia> encontrado = repository.findByIsbn("9780132350884");

        assertThat(encontrado).isPresent();
    }

    @Test
    @DisplayName("findByEmailSocio devuelve las sugerencias del socio")
    void buscarPorSocio() {
        repository.save(nueva("1111111111", "diego@biblioteca.com", "PENDIENTE"));
        repository.save(nueva("2222222222", "diego@biblioteca.com", "APROBADA"));
        repository.save(nueva("3333333333", "otro@biblioteca.com", "PENDIENTE"));

        List<Sugerencia> resultado = repository.findByEmailSocio("diego@biblioteca.com");

        assertThat(resultado).hasSize(2);
    }

    @Test
    @DisplayName("findByEstado filtra por estado")
    void buscarPorEstado() {
        repository.save(nueva("4444444444", "diego@biblioteca.com", "APROBADA"));
        repository.save(nueva("5555555555", "diego@biblioteca.com", "PENDIENTE"));

        List<Sugerencia> aprobadas = repository.findByEstado("APROBADA");

        assertThat(aprobadas).hasSize(1);
        assertThat(aprobadas.get(0).getIsbn()).isEqualTo("4444444444");
    }
}
