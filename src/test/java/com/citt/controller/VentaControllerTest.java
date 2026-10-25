package com.citt.controller;

import com.citt.exceptions.VentaNotFoundException;
import com.citt.persistence.entity.Venta;
import com.citt.persistence.services.VentaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VentaController.class)
class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VentaService ventaService;

    @Test
    @DisplayName("POST debe responder 201 con el encabezado Location apuntando al ID generado")
    void crearVentaDebeDevolverLocationConElIdGenerado() throws Exception {
        Venta ventaGuardada = Venta.builder()
                .idVenta(7L)
                .direccionCompra("Calle Falsa 123")
                .valorCompra(1000)
                .fechaCompra(LocalDate.of(2025, 4, 14))
                .despachoGenerado(false)
                .build();
        when(ventaService.saveVenta(any(Venta.class))).thenReturn(ventaGuardada);

        mockMvc.perform(post("/api/v1/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"direccionCompra\":\"Calle Falsa 123\",\"valorCompra\":1000,"
                                + "\"fechaCompra\":\"2025-04-14\",\"despachoGenerado\":false}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/ventas/7")))
                .andExpect(jsonPath("$.idVenta").value(7));
    }

    @Test
    @DisplayName("GET debe responder 200 con la lista de ventas")
    void obtenerVentasDebeDevolverLaListaDeVentas() throws Exception {
        // Arrange
        Venta primeraVenta = Venta.builder()
                .idVenta(1L)
                .direccionCompra("Calle Falsa 123")
                .valorCompra(1000)
                .fechaCompra(LocalDate.of(2025, 4, 14))
                .despachoGenerado(false)
                .build();
        Venta segundaVenta = Venta.builder()
                .idVenta(2L)
                .direccionCompra("Avenida Siempre Viva 742")
                .valorCompra(5000)
                .fechaCompra(LocalDate.of(2025, 5, 20))
                .despachoGenerado(true)
                .build();
        when(ventaService.findAllVentas()).thenReturn(List.of(primeraVenta, segundaVenta));

        // Act
        var resultado = mockMvc.perform(get("/api/v1/ventas"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].idVenta").value(1))
                .andExpect(jsonPath("$[0].direccionCompra").value("Calle Falsa 123"))
                .andExpect(jsonPath("$[1].idVenta").value(2))
                .andExpect(jsonPath("$[1].valorCompra").value(5000));
        verify(ventaService, times(1)).findAllVentas();
    }

    @Test
    @DisplayName("GET por ID debe responder 200 con la venta encontrada")
    void obtenerVentaPorIdDebeDevolverLaVenta() throws Exception {
        // Arrange
        Venta ventaExistente = Venta.builder()
                .idVenta(5L)
                .direccionCompra("Calle Falsa 123")
                .valorCompra(1000)
                .fechaCompra(LocalDate.of(2025, 4, 14))
                .despachoGenerado(false)
                .build();
        when(ventaService.findById(5L)).thenReturn(ventaExistente);

        // Act
        var resultado = mockMvc.perform(get("/api/v1/ventas/5"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$.idVenta").value(5))
                .andExpect(jsonPath("$.direccionCompra").value("Calle Falsa 123"))
                .andExpect(jsonPath("$.valorCompra").value(1000))
                .andExpect(jsonPath("$.despachoGenerado").value(false));
        verify(ventaService, times(1)).findById(5L);
    }

    @Test
    @DisplayName("GET por ID inexistente debe responder 404")
    void obtenerVentaPorIdInexistenteDebeDevolverNotFound() throws Exception {
        // Arrange
        when(ventaService.findById(99L))
                .thenThrow(new VentaNotFoundException("Venta no encontrada con el ID: 99"));

        // Act
        var resultado = mockMvc.perform(get("/api/v1/ventas/99"));

        // Assert
        resultado.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("99")));
        verify(ventaService, times(1)).findById(99L);
    }

    @Test
    @DisplayName("PUT debe responder 200 con la venta actualizada")
    void actualizarVentaDebeDevolverLaVentaActualizada() throws Exception {
        // Arrange
        Venta ventaActualizada = Venta.builder()
                .idVenta(3L)
                .direccionCompra("Avenida Nueva 456")
                .valorCompra(7500)
                .fechaCompra(LocalDate.of(2025, 6, 1))
                .despachoGenerado(true)
                .build();
        when(ventaService.updateVenta(eq(3L), any(Venta.class))).thenReturn(ventaActualizada);

        // Act
        var resultado = mockMvc.perform(put("/api/v1/ventas/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"direccionCompra\":\"Avenida Nueva 456\",\"valorCompra\":7500,"
                        + "\"fechaCompra\":\"2025-06-01\",\"despachoGenerado\":true}"));

        // Assert
        resultado.andExpect(status().isOk())
                .andExpect(jsonPath("$.idVenta").value(3))
                .andExpect(jsonPath("$.direccionCompra").value("Avenida Nueva 456"))
                .andExpect(jsonPath("$.valorCompra").value(7500))
                .andExpect(jsonPath("$.despachoGenerado").value(true));
        verify(ventaService, times(1)).updateVenta(eq(3L), any(Venta.class));
    }

    @Test
    @DisplayName("PUT sobre un ID inexistente debe responder 404")
    void actualizarVentaInexistenteDebeDevolverNotFound() throws Exception {
        // Arrange
        when(ventaService.updateVenta(eq(99L), any(Venta.class)))
                .thenThrow(new VentaNotFoundException("!No es posible actualizar! No existe venta con ID: 99"));

        // Act
        var resultado = mockMvc.perform(put("/api/v1/ventas/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"direccionCompra\":\"Avenida Nueva 456\",\"valorCompra\":7500,"
                        + "\"fechaCompra\":\"2025-06-01\",\"despachoGenerado\":true}"));

        // Assert
        resultado.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("99")));
        verify(ventaService, times(1)).updateVenta(eq(99L), any(Venta.class));
    }

    @Test
    @DisplayName("DELETE debe responder 204 sin contenido")
    void eliminarVentaDebeDevolverNoContent() throws Exception {
        // Arrange
        Long idVenta = 4L;

        // Act
        var resultado = mockMvc.perform(delete("/api/v1/ventas/" + idVenta));

        // Assert
        resultado.andExpect(status().isNoContent());
        verify(ventaService, times(1)).deleteVenta(idVenta);
    }

    @Test
    @DisplayName("DELETE sobre un ID inexistente debe responder 404")
    void eliminarVentaInexistenteDebeDevolverNotFound() throws Exception {
        // Arrange
        doThrow(new VentaNotFoundException("¡No es posible eliminar! No existe venta con el ID: 99"))
                .when(ventaService).deleteVenta(99L);

        // Act
        var resultado = mockMvc.perform(delete("/api/v1/ventas/99"));

        // Assert
        resultado.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("99")));
        verify(ventaService, times(1)).deleteVenta(99L);
    }
}
