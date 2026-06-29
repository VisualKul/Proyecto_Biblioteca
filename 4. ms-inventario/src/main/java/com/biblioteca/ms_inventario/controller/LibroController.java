package com.biblioteca.ms_inventario.controller;

import com.biblioteca.ms_inventario.dto.LibroCreateDTO;
import com.biblioteca.ms_inventario.dto.LibroUpdateDTO;
import com.biblioteca.ms_inventario.model.Libro;
import com.biblioteca.ms_inventario.service.LibroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Inventario",
        description = "Endpoints para la administración del catálogo e inventario de libros."
)
@RestController
@RequestMapping("/inventario")
public class LibroController {

    private static final Logger log = LoggerFactory.getLogger(LibroController.class);

    @Autowired
    private LibroService service;

    @Operation(
            summary = "Listar libros",
            description = "Obtiene todos los libros registrados en el inventario."
    )
    @GetMapping("/listar")
    public ResponseEntity<Object> listar() {
        List<Libro> libros = service.listarTodo();
        if (libros.isEmpty()) {
            return ResponseEntity.status(404).body("No hay libros en el inventario");
        }
        return ResponseEntity.ok(libros);
    }

    @Operation(
            summary = "Buscar libro por ID",
            description = "Obtiene un libro utilizando su identificador."
    )
    @GetMapping("/ver/{id}")
    public ResponseEntity<Object> verUno(
            @Parameter(description = "ID del libro", example = "1")
            @PathVariable Long id) {

        Libro libro = service.buscarPorId(id);
        if (libro == null) {
            return ResponseEntity.status(404).body("Libro no encontrado");
        }
        return ResponseEntity.ok(libro);
    }

    @Operation(
            summary = "Registrar un libro",
            description = "Agrega un nuevo libro al inventario."
    )
    @PostMapping("/crear")
    public ResponseEntity<Object> crear(@Valid @RequestBody LibroCreateDTO dto) {
        log.info("POST /inventario/crear isbn={}", dto.getIsbn());
        Libro creado = service.crear(dto);
        return ResponseEntity.status(201).body(creado);
    }

    @Operation(
            summary = "Actualizar un libro",
            description = "Actualiza la información de un libro existente."
    )
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<Object> actualizar(
            @Parameter(description = "ID del libro", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody LibroUpdateDTO dto) {

        log.info("PUT /inventario/actualizar/{}", id);
        Libro actualizado = service.actualizar(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    @Operation(
            summary = "Eliminar un libro",
            description = "Elimina un libro del inventario."
    )
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Object> eliminar(
            @Parameter(description = "ID del libro", example = "1")
            @PathVariable Long id) {

        log.info("DELETE /inventario/eliminar/{}", id);
        service.eliminar(id);
        return ResponseEntity.ok("Libro eliminado");
    }

    @Operation(
            summary = "Buscar libros por nombre",
            description = "Busca libros cuyo título coincida con el nombre indicado."
    )
    @GetMapping("/ver/nombre/{nombre}")
    public ResponseEntity<Object> verPorNombre(
            @Parameter(description = "Título o parte del título del libro", example = "Don Quijote")
            @PathVariable String nombre) {

        List<Libro> libros = service.buscarPorNombre(nombre);
        if (libros.isEmpty()) {
            return ResponseEntity.status(404).body("No se encontraron libros con ese nombre");
        }
        return ResponseEntity.ok(libros);
    }

    @Operation(
            summary = "Buscar libro por ISBN",
            description = "Obtiene un libro utilizando su código ISBN."
    )
    @GetMapping("/ver/isbn/{isbn}")
    public ResponseEntity<Object> verPorIsbn(
            @Parameter(description = "ISBN del libro", example = "9789561234567")
            @PathVariable String isbn) {

        Libro libro = service.buscarPorIsbn(isbn);
        if (libro == null) {
            return ResponseEntity.status(404).body("Libro con ese ISBN no encontrado");
        }
        return ResponseEntity.ok(libro);
    }

    @Operation(
            summary = "Descontar stock",
            description = "Reduce en una unidad el stock disponible de un libro cuando se registra un préstamo."
    )
    @PutMapping("/descontar/{id}")
    public ResponseEntity<Object> descontarStock(
            @Parameter(description = "ID del libro", example = "1")
            @PathVariable Long id) {

        service.descontarStock(id);
        return ResponseEntity.ok("Stock actualizado");
    }

    @Operation(
            summary = "Restituir stock",
            description = "Incrementa en una unidad el stock disponible de un libro cuando se devuelve un préstamo."
    )
    @PutMapping("/devolver/{id}")
    public ResponseEntity<Object> devolverStock(
            @Parameter(description = "ID del libro", example = "1")
            @PathVariable Long id) {

        service.devolverStock(id);
        return ResponseEntity.ok("Stock actualizado");
    }
}