package com.biblioteca.ms_prestamos.service;

import com.biblioteca.ms_prestamos.client.LibroClient;
import com.biblioteca.ms_prestamos.client.MultaClient;
import com.biblioteca.ms_prestamos.client.UserClient;
import com.biblioteca.ms_prestamos.dto.LibroDto;
import com.biblioteca.ms_prestamos.dto.MultasPendientesDto;
import com.biblioteca.ms_prestamos.dto.PrestamoCreateDTO;
import com.biblioteca.ms_prestamos.dto.PrestamoUpdateDTO;
import com.biblioteca.ms_prestamos.dto.UsuarioDto;
import com.biblioteca.ms_prestamos.exception.BusinessException;
import com.biblioteca.ms_prestamos.exception.ResourceNotFoundException;
import com.biblioteca.ms_prestamos.model.Prestamo;
import com.biblioteca.ms_prestamos.repository.PrestamoRepository;
import com.biblioteca.ms_prestamos.exception.ServiceUnavailableException;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrestamoService {

    private static final Logger log = LoggerFactory.getLogger(PrestamoService.class);

    @Autowired
    private PrestamoRepository repository;

    @Autowired
    private UserClient userClient;

    @Autowired
    private LibroClient libroClient;

    @Autowired
    private MultaClient multaClient;

    public Prestamo registrarPrestamo(PrestamoCreateDTO dto) {
        // 1. Validar usuario via Feign hacia user-service
        UsuarioDto usuario;
        try {
            usuario = userClient.obtenerPorEmail(dto.getEmailUsuario());
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Usuario no encontrado en user-service");
        } catch (FeignException ex) {
            log.error("Feign user-service: {}", ex.getMessage());
            throw new IllegalStateException("Error comunicandose con user-service", ex);
        }
        if (usuario == null) {
            throw new ResourceNotFoundException("Usuario no encontrado en user-service");
        }

        // 2. Bloquear si tiene multas pendientes
        try {
            MultasPendientesDto pendientes = multaClient.consultarPendientes(dto.getEmailUsuario());
            if (pendientes != null && Boolean.TRUE.equals(pendientes.getTienePendientes())) {
                log.warn("Bloqueo por multas email={} cantidad={}", dto.getEmailUsuario(), pendientes.getCantidad());
                throw new IllegalArgumentException(
                "El usuario tiene multa(s) pendiente(s). No puede registrar nuevos prestamos hasta pagarlas");
            }
        } catch (FeignException.NotFound ex) {
            // ms-multas no encontro registros, se permite
        } catch (FeignException ex) {
            log.error("Feign ms-multas: {}", ex.getMessage());
            throw new ServiceUnavailableException("Error comunicandose con ms-multas");
        }

        // 3. Validar libro
        LibroDto libro;
        try {
            libro = libroClient.obtenerPorId(dto.getLibroId());
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Libro no encontrado en ms-inventario");
        } catch (FeignException ex) {
            log.error("Feign ms-inventario: {}", ex.getMessage());
            throw new IllegalStateException("Error comunicandose con ms-inventario", ex);
        }
        if (libro == null) {
            throw new ResourceNotFoundException("Libro no encontrado en ms-inventario");
        }
        if (libro.getStock() != null && libro.getStock() <= 0) {
            throw new BusinessException("El libro no tiene stock disponible");
        }

        // 4. Persistir
        Prestamo prestamo = new Prestamo();
        prestamo.setEmailUsuario(dto.getEmailUsuario());
        prestamo.setLibroId(dto.getLibroId());
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setFechaDevolucion(dto.getFechaDevolucion());
        prestamo.setEstado("ACTIVO");
        Prestamo guardado = repository.save(prestamo);
        libroClient.descontarStock(dto.getLibroId());
        log.info("Stock descontado del libro {}", dto.getLibroId());
        log.info("Prestamo registrado id={} email={} libroId={}",
                guardado.getId(), guardado.getEmailUsuario(), guardado.getLibroId());
        return guardado;
    }

    public Prestamo actualizar(Long id, PrestamoUpdateDTO dto) {

    Prestamo existente = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Prestamo no encontrado"));

    if (dto.getFechaDevolucion() != null) {
        existente.setFechaDevolucion(dto.getFechaDevolucion());
    }

    if (dto.getEstado() != null) {

        // Si antes NO estaba devuelto y ahora sí
        if (!"DEVUELTO".equals(existente.getEstado())
                && "DEVUELTO".equals(dto.getEstado())) {

            libroClient.devolverStock(existente.getLibroId());
            log.info("Stock devuelto del libro {}", existente.getLibroId());
            }

        existente.setEstado(dto.getEstado());
        }

        Prestamo actualizado = repository.save(existente);

        log.info("Prestamo actualizado id={} estado={}",
            actualizado.getId(),
            actualizado.getEstado());

        return actualizado;
    }

    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Prestamo no encontrado");
        }
        repository.deleteById(id);
        log.info("Prestamo eliminado id={}", id);
    }

    public List<Prestamo> listarPorEmail(String email) {
        return repository.findByEmailUsuario(email);
    }

    public Prestamo buscarPorId(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<Prestamo> listarTodos() {
        return repository.findAll();
    }
}
