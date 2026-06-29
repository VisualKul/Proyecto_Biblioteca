package com.biblioteca.ms_favoritos_listas.service.impl;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.biblioteca.ms_favoritos_listas.client.UserClient;
import com.biblioteca.ms_favoritos_listas.dto.ListaFavoritosRequestDTO;
import com.biblioteca.ms_favoritos_listas.dto.ListaFavoritosResponseDTO;
import com.biblioteca.ms_favoritos_listas.dto.UsuarioDto;
import com.biblioteca.ms_favoritos_listas.exception.DuplicateResourceException;
import com.biblioteca.ms_favoritos_listas.exception.ResourceNotFoundException;
import com.biblioteca.ms_favoritos_listas.model.ListaFavoritos;
import com.biblioteca.ms_favoritos_listas.repository.ListaFavoritosRepository;
import com.biblioteca.ms_favoritos_listas.service.ListaFavoritosService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListaFavoritosServiceImpl implements ListaFavoritosService {

    private final ListaFavoritosRepository repository;
    private final UserClient userClient;

    private void validarUsuario(String email) {
        try {
            UsuarioDto u = userClient.obtenerPorEmail(email);
            if (u == null) throw new ResourceNotFoundException("Usuario no encontrado en user-service");
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Usuario no encontrado en user-service");
        } catch (FeignException ex) {
            log.error("Feign user-service: {}", ex.getMessage());
            throw new IllegalStateException("Error comunicandose con user-service");
        }
    }

    @Override
    public ListaFavoritosResponseDTO crear(ListaFavoritosRequestDTO dto) {
        log.info("Creando lista '{}' para email={}", dto.getNombre(), dto.getEmailUsuario());
        validarUsuario(dto.getEmailUsuario());

        repository.findByNombreAndEmailUsuario(dto.getNombre(), dto.getEmailUsuario())
                .ifPresent(l -> {
                    throw new DuplicateResourceException("La lista ya existe para este usuario");
                });

        ListaFavoritos lista = ListaFavoritos.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .emailUsuario(dto.getEmailUsuario())
                .publica(dto.getPublica())
                .build();
        ListaFavoritos saved = repository.save(lista);
        log.info("Lista creada id={}", saved.getId());
        return mapToDTO(saved);
    }

    @Override
    public List<ListaFavoritosResponseDTO> listar() {
        return repository.findAll().stream().map(this::mapToDTO).toList();
    }

    @Override
    public List<ListaFavoritosResponseDTO> listarPorUsuario(String email) {
        return repository.findByEmailUsuario(email).stream().map(this::mapToDTO).toList();
    }

    @Override
    public List<ListaFavoritosResponseDTO> listarPublicas() {
        return repository.findByPublicaTrue().stream().map(this::mapToDTO).toList();
    }

    @Override
    public ListaFavoritosResponseDTO buscarPorId(Long id) {
        ListaFavoritos lista = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lista no encontrada"));
        return mapToDTO(lista);
    }

    @Override
    public ListaFavoritosResponseDTO actualizar(Long id, ListaFavoritosRequestDTO dto) {
        log.info("Actualizando lista id={}", id);
        ListaFavoritos lista = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lista no encontrada"));
        validarUsuario(dto.getEmailUsuario());
        lista.setNombre(dto.getNombre());
        lista.setDescripcion(dto.getDescripcion());
        lista.setEmailUsuario(dto.getEmailUsuario());
        lista.setPublica(dto.getPublica());
        return mapToDTO(repository.save(lista));
    }

    @Override
    public void eliminar(Long id) {
        ListaFavoritos lista = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lista no encontrada"));
        repository.delete(lista);
        log.info("Lista eliminada id={}", id);
    }

    private ListaFavoritosResponseDTO mapToDTO(ListaFavoritos l) {
        return ListaFavoritosResponseDTO.builder()
                .id(l.getId()).nombre(l.getNombre()).descripcion(l.getDescripcion())
                .emailUsuario(l.getEmailUsuario()).publica(l.getPublica()).build();
    }
}
