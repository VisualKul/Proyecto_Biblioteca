package com.biblioteca.auth_service.repository;

import com.biblioteca.auth_service.model.LoginRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("LoginRecordRepository - tests de persistencia (@DataJpaTest + H2)")
class LoginRecordRepositoryTest {

    @Autowired
    private LoginRecordRepository repository;

    @Test
    @DisplayName("save persiste el registro de login y genera id")
    void guardarYRecuperar() {
        LoginRecord record = new LoginRecord();
        record.setEmail("diego@biblioteca.com");
        record.setLoginTime(LocalDateTime.now());
        record.setExitoso(true);

        LoginRecord guardado = repository.save(record);

        assertThat(guardado.getId()).isNotNull();
        Optional<LoginRecord> encontrado = repository.findById(guardado.getId());
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().isExitoso()).isTrue();
        assertThat(encontrado.get().getEmail()).isEqualTo("diego@biblioteca.com");
    }

    @Test
    @DisplayName("findAll devuelve todos los registros guardados")
    void contarRegistros() {
        LoginRecord r1 = new LoginRecord();
        r1.setEmail("a@x.com");
        r1.setExitoso(false);
        LoginRecord r2 = new LoginRecord();
        r2.setEmail("b@x.com");
        r2.setExitoso(true);
        repository.save(r1);
        repository.save(r2);

        assertThat(repository.findAll()).hasSize(2);
    }
}
