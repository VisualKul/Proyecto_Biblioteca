package com.biblioteca.ms_inventario.service;

import com.biblioteca.ms_inventario.dto.LibroCreateDTO;
import com.biblioteca.ms_inventario.dto.LibroUpdateDTO;
import com.biblioteca.ms_inventario.model.Libro;
import com.biblioteca.ms_inventario.repository.LibroRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LibroService {

    private static final Logger log = LoggerFactory.getLogger(LibroService.class);

    @Autowired
    private LibroRepository repository;

    public List<Libro> listarTodo() {
        return repository.findAll();
    }

    public Libro buscarPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Libro crear(LibroCreateDTO dto) {
        if (repository.findByIsbn(dto.getIsbn()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un libro con ese ISBN");
        }
        Libro libro = new Libro();
        libro.setTitulo(dto.getTitulo());
        libro.setAutor(dto.getAutor());
        libro.setIsbn(dto.getIsbn());
        libro.setEditorial(dto.getEditorial());
        libro.setStock(dto.getStock());
        Libro creado = repository.save(libro);
        log.info("Libro creado id={} titulo={}", creado.getId(), creado.getTitulo());
        return creado;
    }

    public Libro actualizar(Long id, LibroUpdateDTO dto) {
        Libro existente = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado"));

        if (dto.getTitulo() != null) existente.setTitulo(dto.getTitulo());
        if (dto.getAutor() != null) existente.setAutor(dto.getAutor());
        if (dto.getIsbn() != null) existente.setIsbn(dto.getIsbn());
        if (dto.getEditorial() != null) existente.setEditorial(dto.getEditorial());
        if (dto.getStock() != null) existente.setStock(dto.getStock());

        Libro actualizado = repository.save(existente);
        log.info("Libro actualizado id={}", actualizado.getId());
        return actualizado;
    }

    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Libro no encontrado");
        }
        repository.deleteById(id);
        log.info("Libro eliminado id={}", id);
    }

    public List<Libro> buscarPorNombre(String nombre) {
        return repository.findByTituloContainingIgnoreCase(nombre);
    }

    public Libro buscarPorIsbn(String isbn) {
        return repository.findByIsbn(isbn).orElse(null);
    }

    public void descontarStock(Long id) {

        Libro libro = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado"));

        if (libro.getStock() <= 0) {
            throw new IllegalArgumentException("No hay stock disponible");
        }

        libro.setStock(libro.getStock() - 1);

        repository.save(libro);

        log.info("Stock descontado libro={} stock={}", id, libro.getStock());
    }

    public void devolverStock(Long id) {

        Libro libro = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado"));

        libro.setStock(libro.getStock() + 1);

        repository.save(libro);

        log.info("Stock incrementado libro={} stock={}", id, libro.getStock());
    }
}
