package com.biblioteca.ms_prestamos.client;

import com.biblioteca.ms_prestamos.dto.LibroDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * Feign Client para consultar libros en ms-inventario.
 * Se usa para validar que el libro existe (y opcionalmente tiene stock)
 * antes de registrar un prestamo.
 */
@FeignClient(name = "ms-inventario")
public interface LibroClient {

    @GetMapping("/inventario/ver/{id}")
    LibroDto obtenerPorId(@PathVariable("id") Long id);

    @PutMapping("/inventario/descontar/{id}")
    void descontarStock(@PathVariable Long id);

    @PutMapping("/inventario/devolver/{id}")
    void devolverStock(@PathVariable Long id);
}
