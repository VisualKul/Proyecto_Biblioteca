package com.biblioteca.ms_inventario.controller;

import com.biblioteca.ms_inventario.dto.LibroCreateDTO;
import com.biblioteca.ms_inventario.dto.LibroUpdateDTO;
import com.biblioteca.ms_inventario.model.Libro;
import com.biblioteca.ms_inventario.service.LibroService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/inventario")
public class LibroController {

    private static final Logger log = LoggerFactory.getLogger(LibroController.class);

    @Autowired
    private LibroService service;

    @GetMapping("/listar")
    public ResponseEntity<Object> listar() {
        List<Libro> libros = service.listarTodo();
        if (libros.isEmpty()) {
            return ResponseEntity.status(404).body("No hay libros en el inventario");
        }
        return ResponseEntity.ok(libros);
    }

    @GetMapping("/ver/{id}")
    public ResponseEntity<Object> verUno(@PathVariable Long id) {
        Libro libro = service.buscarPorId(id);
        if (libro == null) {
            return ResponseEntity.status(404).body("Libro no encontrado");
        }
        return ResponseEntity.ok(libro);
    }

    @PostMapping("/crear")
    public ResponseEntity<Object> crear(@Valid @RequestBody LibroCreateDTO dto) {
        log.info("POST /inventario/crear isbn={}", dto.getIsbn());
        Libro creado = service.crear(dto);
        return ResponseEntity.status(201).body(creado);
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<Object> actualizar(@PathVariable Long id,
                                             @Valid @RequestBody LibroUpdateDTO dto) {
        log.info("PUT /inventario/actualizar/{}", id);
        Libro actualizado = service.actualizar(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Object> eliminar(@PathVariable Long id) {
        log.info("DELETE /inventario/eliminar/{}", id);
        service.eliminar(id);
        return ResponseEntity.ok("Libro eliminado");
    }

    @GetMapping("/ver/nombre/{nombre}")
    public ResponseEntity<Object> verPorNombre(@PathVariable String nombre) {
        List<Libro> libros = service.buscarPorNombre(nombre);
        if (libros.isEmpty()) {
            return ResponseEntity.status(404).body("No se encontraron libros con ese nombre");
        }
        return ResponseEntity.ok(libros);
    }

    @GetMapping("/ver/isbn/{isbn}")
    public ResponseEntity<Object> verPorIsbn(@PathVariable String isbn) {
        Libro libro = service.buscarPorIsbn(isbn);
        if (libro == null) {
            return ResponseEntity.status(404).body("Libro con ese ISBN no encontrado");
        }
        return ResponseEntity.ok(libro);
    }

    @PutMapping("/descontar/{id}")
    public ResponseEntity<Object> descontarStock(@PathVariable Long id) {
    service.descontarStock(id);
    return ResponseEntity.ok("Stock actualizado");
    }

    @PutMapping("/devolver/{id}")
    public ResponseEntity<Object> devolverStock(@PathVariable Long id) {
    service.devolverStock(id);
    return ResponseEntity.ok("Stock actualizado");
    }
}
