package com.citt.controller;

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

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}
