package com.biblioteca.ms_multas.service;

import com.biblioteca.ms_multas.client.PrestamoClient;
import com.biblioteca.ms_multas.client.UserClient;
import com.biblioteca.ms_multas.dto.PrestamoDto;
import com.biblioteca.ms_multas.dto.UsuarioDto;
import com.biblioteca.ms_multas.model.Multa;
import com.biblioteca.ms_multas.repository.MultaRepository;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MultaService - tests unitarios")
class MultaServiceTest {

    @Mock private MultaRepository repository;
    @Mock private UserClient userClient;
    @Mock private PrestamoClient prestamoClient;

    @InjectMocks
    private MultaService service;

    @BeforeEach
    void setUp() {
        // @Value no lo inyecta Mockito; lo fijamos manualmente
        ReflectionTestUtils.setField(service, "tarifaPorDia", new BigDecimal("500"));
    }

    private FeignException.NotFound notFound() {
        Request request = Request.create(Request.HttpMethod.GET, "/",
                Collections.emptyMap(), new byte[0], StandardCharsets.UTF_8, null);
        return new FeignException.NotFound("not found", request, null, null);
    }

    private PrestamoDto prestamoVencido(int diasAtraso) {
        PrestamoDto p = new PrestamoDto();
        p.setId(1L);
        p.setEmailUsuario("diego@biblioteca.com");
        p.setFechaDevolucion(LocalDate.now().minusDays(diasAtraso));
        return p;
    }

    @Test
    @DisplayName("calcularMulta genera la multa con monto = tarifa * dias de retraso")
    void calcular_ok() {
        when(prestamoClient.obtenerPorId(1L)).thenReturn(prestamoVencido(3));
        when(repository.existsByPrestamoId(1L)).thenReturn(false);
        when(userClient.obtenerPorEmail("diego@biblioteca.com")).thenReturn(new UsuarioDto());
        when(repository.save(any(Multa.class))).thenAnswer(inv -> inv.getArgument(0));

        Multa multa = service.calcularMulta(1L);

        assertThat(multa.getDiasRetraso()).isEqualTo(3);
        assertThat(multa.getMonto()).isEqualByComparingTo("1500");
        assertThat(multa.getEstado()).isEqualTo("PENDIENTE");
    }

    @Test
    @DisplayName("calcularMulta lanza excepcion si el prestamo no existe (Feign 404)")
    void calcular_prestamoNoExiste() {
        when(prestamoClient.obtenerPorId(1L)).thenThrow(notFound());

        assertThatThrownBy(() -> service.calcularMulta(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Prestamo no encontrado");
    }

    @Test
    @DisplayName("calcularMulta lanza excepcion si el prestamo aun no esta vencido")
    void calcular_noVencido() {
        PrestamoDto vigente = new PrestamoDto();
        vigente.setEmailUsuario("diego@biblioteca.com");
        vigente.setFechaDevolucion(LocalDate.now().plusDays(5));
        when(prestamoClient.obtenerPorId(1L)).thenReturn(vigente);

        assertThatThrownBy(() -> service.calcularMulta(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no esta vencido");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("calcularMulta lanza excepcion si ya existe una multa para el prestamo")
    void calcular_duplicada() {
        when(prestamoClient.obtenerPorId(1L)).thenReturn(prestamoVencido(2));
        when(repository.existsByPrestamoId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.calcularMulta(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe una multa");
    }

    @Test
    @DisplayName("pagarMulta marca como PAGADA y registra fecha de pago")
    void pagar_ok() {
        Multa multa = new Multa();
        multa.setId(1L);
        multa.setEstado("PENDIENTE");
        when(repository.findById(1L)).thenReturn(Optional.of(multa));
        when(repository.save(any(Multa.class))).thenAnswer(inv -> inv.getArgument(0));

        Multa pagada = service.pagarMulta(1L);

        assertThat(pagada.getEstado()).isEqualTo("PAGADA");
        assertThat(pagada.getFechaPago()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("pagarMulta lanza excepcion si la multa ya esta pagada")
    void pagar_yaPagada() {
        Multa multa = new Multa();
        multa.setEstado("PAGADA");
        when(repository.findById(1L)).thenReturn(Optional.of(multa));

        assertThatThrownBy(() -> service.pagarMulta(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya esta pagada");
    }

    @Test
    @DisplayName("pagarMulta lanza excepcion si la multa no existe")
    void pagar_noExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.pagarMulta(99L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("ajustarMulta recalcula el monto al cambiar los dias de retraso")
    void ajustar_dias() {
        Multa multa = new Multa();
        multa.setId(1L);
        multa.setEstado("PENDIENTE");
        when(repository.findById(1L)).thenReturn(Optional.of(multa));
        when(repository.save(any(Multa.class))).thenAnswer(inv -> inv.getArgument(0));

        Multa ajustada = service.ajustarMulta(1L, 4, null);

        assertThat(ajustada.getDiasRetraso()).isEqualTo(4);
        assertThat(ajustada.getMonto()).isEqualByComparingTo("2000");
    }

    @Test
    @DisplayName("ajustarMulta rechaza ajustes sobre multas que no estan PENDIENTE")
    void ajustar_noPendiente() {
        Multa multa = new Multa();
        multa.setEstado("PAGADA");
        when(repository.findById(1L)).thenReturn(Optional.of(multa));

        assertThatThrownBy(() -> service.ajustarMulta(1L, 4, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PENDIENTE");
    }

    @Test
    @DisplayName("anularMulta cambia el estado a ANULADA")
    void anular_ok() {
        Multa multa = new Multa();
        multa.setEstado("PENDIENTE");
        when(repository.findById(1L)).thenReturn(Optional.of(multa));
        when(repository.save(any(Multa.class))).thenAnswer(inv -> inv.getArgument(0));

        Multa anulada = service.anularMulta(1L);

        assertThat(anulada.getEstado()).isEqualTo("ANULADA");
    }

    @Test
    @DisplayName("anularMulta lanza excepcion si ya estaba anulada")
    void anular_yaAnulada() {
        Multa multa = new Multa();
        multa.setEstado("ANULADA");
        when(repository.findById(1L)).thenReturn(Optional.of(multa));

        assertThatThrownBy(() -> service.anularMulta(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("eliminarMulta borra cuando existe")
    void eliminar_ok() {
        when(repository.existsById(1L)).thenReturn(true);

        service.eliminarMulta(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("tienePendientes delega en el repositorio")
    void tienePendientes_ok() {
        when(repository.existsByEmailUsuarioAndEstado("diego@biblioteca.com", "PENDIENTE")).thenReturn(true);

        assertThat(service.tienePendientes("diego@biblioteca.com")).isTrue();
    }

    @Test
    @DisplayName("listarPorUsuario delega en el repositorio")
    void listarPorUsuario_ok() {
        Multa m = new Multa();
        when(repository.findByEmailUsuario("diego@biblioteca.com")).thenReturn(List.of(m));

        assertThat(service.listarPorUsuario("diego@biblioteca.com")).containsExactly(m);
    }
}
