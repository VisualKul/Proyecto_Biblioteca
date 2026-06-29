package com.biblioteca.ms_favoritos_listas.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.biblioteca.ms_favoritos_listas.model.ListaFavoritos;

import java.util.List;
import java.util.Optional;

public interface ListaFavoritosRepository extends JpaRepository<ListaFavoritos, Long> {
    Optional<ListaFavoritos> findByNombreAndEmailUsuario(String nombre, String emailUsuario);
    List<ListaFavoritos> findByEmailUsuario(String emailUsuario);
    List<ListaFavoritos> findByPublicaTrue();
}
