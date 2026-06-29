package com.biblioteca.ms_inventario.service;

import com.biblioteca.ms_inventario.dto.LibroCreateDTO;
import com.biblioteca.ms_inventario.dto.LibroUpdateDTO;
import com.biblioteca.ms_inventario.model.Libro;
import com.biblioteca.ms_inventario.repository.LibroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LibroService - tests unitarios")
class LibroServiceTest {

    @Mock
    private LibroRepository repository;

    @InjectMocks
    private LibroService service;

    private Libro libro;

    @BeforeEach
    void setUp() {
        libro = new Libro();
        libro.setId(1L);
        libro.setTitulo("Don Quijote");
        libro.setAutor("Cervantes");
        libro.setIsbn("1234567890");
        libro.setEditorial("Planeta");
        libro.setStock(5);
    }

    @Test
    @DisplayName("listarTodo devuelve la lista del repositorio")
    void listarTodo_devuelveLista() {
        when(repository.findAll()).thenReturn(List.of(libro));

        List<Libro> resultado = service.listarTodo();

        assertThat(resultado).hasSize(1).containsExactly(libro);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("buscarPorId devuelve el libro cuando existe")
    void buscarPorId_existe() {
        when(repository.findById(1L)).thenReturn(Optional.of(libro));

        assertThat(service.buscarPorId(1L)).isEqualTo(libro);
    }

    @Test
    @DisplayName("buscarPorId devuelve null cuando no existe")
    void buscarPorId_noExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThat(service.buscarPorId(99L)).isNull();
    }

    @Test
    @DisplayName("crear guarda un libro nuevo cuando el ISBN no existe")
    void crear_ok() {
        LibroCreateDTO dto = new LibroCreateDTO();
        dto.setTitulo("Nuevo");
        dto.setAutor("Autor");
        dto.setIsbn("9999999999");
        dto.setEditorial("Ed");
        dto.setStock(3);

        when(repository.findByIsbn("9999999999")).thenReturn(Optional.empty());
        when(repository.save(any(Libro.class))).thenAnswer(inv -> inv.getArgument(0));

        Libro creado = service.crear(dto);

        ArgumentCaptor<Libro> captor = ArgumentCaptor.forClass(Libro.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getTitulo()).isEqualTo("Nuevo");
        assertThat(captor.getValue().getStock()).isEqualTo(3);
        assertThat(creado.getIsbn()).isEqualTo("9999999999");
    }

    @Test
    @DisplayName("crear lanza excepcion cuando el ISBN ya existe")
    void crear_isbnDuplicado() {
        LibroCreateDTO dto = new LibroCreateDTO();
        dto.setIsbn("1234567890");
        when(repository.findByIsbn("1234567890")).thenReturn(Optional.of(libro));

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ISBN");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("actualizar modifica solo los campos no nulos")
    void actualizar_ok() {
        LibroUpdateDTO dto = new LibroUpdateDTO();
        dto.setTitulo("Editado");
        dto.setStock(10);

        when(repository.findById(1L)).thenReturn(Optional.of(libro));
        when(repository.save(any(Libro.class))).thenAnswer(inv -> inv.getArgument(0));

        Libro actualizado = service.actualizar(1L, dto);

        assertThat(actualizado.getTitulo()).isEqualTo("Editado");
        assertThat(actualizado.getStock()).isEqualTo(10);
        assertThat(actualizado.getAutor()).isEqualTo("Cervantes"); // sin cambios
    }

    @Test
    @DisplayName("actualizar lanza excepcion cuando el libro no existe")
    void actualizar_noExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar(99L, new LibroUpdateDTO()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("eliminar borra cuando el libro existe")
    void eliminar_ok() {
        when(repository.existsById(1L)).thenReturn(true);

        service.eliminar(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar lanza excepcion cuando no existe")
    void eliminar_noExiste() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("buscarPorNombre delega en el repositorio")
    void buscarPorNombre_ok() {
        when(repository.findByTituloContainingIgnoreCase("qui")).thenReturn(List.of(libro));

        assertThat(service.buscarPorNombre("qui")).containsExactly(libro);
    }

    @Test
    @DisplayName("buscarPorIsbn devuelve null cuando no existe")
    void buscarPorIsbn_noExiste() {
        when(repository.findByIsbn("000")).thenReturn(Optional.empty());

        assertThat(service.buscarPorIsbn("000")).isNull();
    }

    @Test
    @DisplayName("descontarStock reduce en uno el stock disponible")
    void descontarStock_ok() {
        when(repository.findById(1L)).thenReturn(Optional.of(libro));

        service.descontarStock(1L);

        assertThat(libro.getStock()).isEqualTo(4);
        verify(repository).save(libro);
    }

    @Test
    @DisplayName("descontarStock lanza excepcion cuando no hay stock")
    void descontarStock_sinStock() {
        libro.setStock(0);
        when(repository.findById(1L)).thenReturn(Optional.of(libro));

        assertThatThrownBy(() -> service.descontarStock(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stock");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("devolverStock incrementa en uno el stock")
    void devolverStock_ok() {
        when(repository.findById(1L)).thenReturn(Optional.of(libro));

        service.devolverStock(1L);

        assertThat(libro.getStock()).isEqualTo(6);
        verify(repository).save(libro);
    }

    @Test
    @DisplayName("devolverStock lanza excepcion cuando no existe")
    void devolverStock_noExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.devolverStock(99L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
