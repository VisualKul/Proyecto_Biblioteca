package com.biblioteca.ms_favoritos_listas.service;

import java.util.List;

import com.biblioteca.ms_favoritos_listas.dto.ListaFavoritosRequestDTO;
import com.biblioteca.ms_favoritos_listas.dto.ListaFavoritosResponseDTO;

public interface ListaFavoritosService {
    ListaFavoritosResponseDTO crear(ListaFavoritosRequestDTO dto);
    List<ListaFavoritosResponseDTO> listar();
    List<ListaFavoritosResponseDTO> listarPorUsuario(String email);
    List<ListaFavoritosResponseDTO> listarPublicas();
    ListaFavoritosResponseDTO buscarPorId(Long id);
    ListaFavoritosResponseDTO actualizar(Long id, ListaFavoritosRequestDTO dto);
    void eliminar(Long id);
}
