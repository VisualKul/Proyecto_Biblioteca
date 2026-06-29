package com.biblioteca.security_service.repository;

import com.biblioteca.security_service.model.Rol;
import com.biblioteca.security_service.model.UsuarioRol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UsuarioRolRepository - tests de persistencia (@DataJpaTest + H2)")
class UsuarioRolRepositoryTest {

    @Autowired
    private UsuarioRolRepository usuarioRolRepository;

    @Autowired
    private RolRepository rolRepository;

    private UsuarioRol asignar(Long usuarioId, Rol rol) {
        UsuarioRol ur = new UsuarioRol();
        ur.setUsuarioId(usuarioId);
        ur.setRol(rol);
        return ur;
    }

    @Test
    @DisplayName("findByUsuarioId devuelve las asignaciones del usuario")
    void buscarPorUsuarioId() {
        Rol rol = new Rol();
        rol.setNombre("ROLE_ADMIN");
        rol = rolRepository.save(rol);

        usuarioRolRepository.save(asignar(10L, rol));
        usuarioRolRepository.save(asignar(10L, rol));
        usuarioRolRepository.save(asignar(20L, rol));

        List<UsuarioRol> delUsuario10 = usuarioRolRepository.findByUsuarioId(10L);

        assertThat(delUsuario10).hasSize(2);
        assertThat(delUsuario10).allMatch(ur -> ur.getUsuarioId().equals(10L));
    }

    @Test
    @DisplayName("existsByUsuarioId refleja si el usuario tiene asignaciones")
    void existePorUsuarioId() {
        Rol rol = new Rol();
        rol.setNombre("ROLE_SOCIO");
        rol = rolRepository.save(rol);
        usuarioRolRepository.save(asignar(30L, rol));

        assertThat(usuarioRolRepository.existsByUsuarioId(30L)).isTrue();
        assertThat(usuarioRolRepository.existsByUsuarioId(999L)).isFalse();
    }
}
