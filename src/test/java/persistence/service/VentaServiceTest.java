package persistence.service;

import com.citt.exceptions.VentaNotFoundException;
import com.citt.persistence.entity.Venta;
import com.citt.persistence.repository.VentaRepository;
import com.citt.persistence.services.VentaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private Venta venta;

    @BeforeEach
    public void setUp(){
        venta = Venta.builder()
                .direccionCompra("Calle Falsa 123")
                .valorCompra(1000)
                .fechaCompra(LocalDate.of(2025,4,14))
                .despachoGenerado(false)
                .build();
    }

    @Test
    @DisplayName("Cuando se guarda una venta válida, entonces se persiste correctamente")
    public void whenSavingValidVenta_thenItIsPersistedCorrectly(){
        //Prepara la simulación
        when(ventaRepository.save(any(Venta.class))).thenReturn(venta);

        //Llama al servicio
        Venta savedVenta = ventaService.saveVenta(venta);

        //Verifica el resultado
        verify(ventaRepository, times(1)).save(venta);

        //Verifica que la venta guardada es la misma que la venta original
        assertNotNull(savedVenta);
        assertEquals(venta.getDireccionCompra(), savedVenta.getDireccionCompra());
        assertEquals(venta.getValorCompra(), savedVenta.getValorCompra());
        assertEquals(venta.getFechaCompra(), savedVenta.getFechaCompra());
        assertEquals(venta.getDespachoGenerado(), savedVenta.getDespachoGenerado());
    }

    @Test
    @DisplayName("Cuando se guarda una venta, entonces se asigna un ID")
    public void whenVentaIsSavedthenIdIsAssigned(){
        // Preparar
        Venta ventaToSave = Venta.builder()
                .direccionCompra("Calle Falsa 123")
                .valorCompra(1000)
                .fechaCompra(LocalDate.of(2025,4,14))
                .despachoGenerado(false)
                .build();

        Venta ventaWithId = Venta.builder()
                .idVenta(1L)
                .direccionCompra("Calle Falsa 123")
                .valorCompra(1000)
                .fechaCompra(LocalDate.of(2025,4,14))
                .despachoGenerado(false)
                .build();

        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaWithId);

        // Ejecutar
        Venta result = ventaService.saveVenta(ventaToSave);

        // Verificar
        verify(ventaRepository).save(ventaToSave);
        assertNotNull(result);
        assertEquals(1L, result.getIdVenta());
        assertEquals(ventaToSave.getDireccionCompra(), result.getDireccionCompra());
    }

    @Test
    @DisplayName("Cuando se piden todas las ventas, entonces se devuelve la lista del repositorio")
    public void whenFindingAllVentas_thenRepositoryListIsReturned(){
        // Arrange
        Venta otraVenta = Venta.builder()
                .idVenta(2L)
                .direccionCompra("Avenida Siempre Viva 742")
                .valorCompra(5000)
                .fechaCompra(LocalDate.of(2025, 5, 20))
                .despachoGenerado(true)
                .build();
        when(ventaRepository.findAll()).thenReturn(List.of(venta, otraVenta));

        // Act
        List<Venta> ventas = ventaService.findAllVentas();

        // Assert
        verify(ventaRepository, times(1)).findAll();
        assertNotNull(ventas);
        assertEquals(2, ventas.size());
        assertEquals("Calle Falsa 123", ventas.get(0).getDireccionCompra());
        assertEquals("Avenida Siempre Viva 742", ventas.get(1).getDireccionCompra());
    }

    @Test
    @DisplayName("Cuando no hay ventas registradas, entonces se devuelve una lista vacía")
    public void whenThereAreNoVentas_thenAnEmptyListIsReturned(){
        // Arrange
        when(ventaRepository.findAll()).thenReturn(List.of());

        // Act
        List<Venta> ventas = ventaService.findAllVentas();

        // Assert
        verify(ventaRepository, times(1)).findAll();
        assertNotNull(ventas);
        assertTrue(ventas.isEmpty());
    }

    @Test
    @DisplayName("Cuando se actualiza una venta con ID existente, entonces se modifican los campos y se guarda")
    public void whenUpdatingVentaWithExistingId_thenFieldsAreMergedAndSaved() throws VentaNotFoundException {
        // Arrange
        Venta ventaDB = Venta.builder()
                .idVenta(1L)
                .direccionCompra("Calle Falsa 123")
                .valorCompra(1000)
                .fechaCompra(LocalDate.of(2025, 4, 14))
                .despachoGenerado(false)
                .build();
        Venta cambios = Venta.builder()
                .direccionCompra("Avenida Nueva 456")
                .valorCompra(7500)
                .fechaCompra(LocalDate.of(2025, 6, 1))
                .despachoGenerado(true)
                .build();
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaDB));
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaDB);

        // Act
        Venta ventaActualizada = ventaService.updateVenta(1L, cambios);

        // Assert
        verify(ventaRepository, times(1)).findById(1L);
        verify(ventaRepository, times(1)).save(ventaDB);
        assertNotNull(ventaActualizada);
        assertEquals(1L, ventaActualizada.getIdVenta());
        assertEquals("Avenida Nueva 456", ventaActualizada.getDireccionCompra());
        assertEquals(7500, ventaActualizada.getValorCompra());
        assertEquals(LocalDate.of(2025, 6, 1), ventaActualizada.getFechaCompra());
        assertTrue(ventaActualizada.getDespachoGenerado());
    }

    @Test
    @DisplayName("Cuando se actualiza una venta con ID inexistente, entonces se lanza VentaNotFoundException")
    public void whenUpdatingVentaWithUnknownId_thenVentaNotFoundExceptionIsThrown(){
        // Arrange
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        VentaNotFoundException excepcion = assertThrows(VentaNotFoundException.class,
                () -> ventaService.updateVenta(99L, venta));

        // Assert
        verify(ventaRepository, times(1)).findById(99L);
        verify(ventaRepository, never()).save(any(Venta.class));
        assertTrue(excepcion.getMessage().contains("99"));
    }

    @Test
    @DisplayName("Cuando los campos enviados son nulos, entonces la actualización no pisa los valores existentes")
    public void whenUpdatingVentaWithNullFields_thenExistingValuesArePreserved() throws VentaNotFoundException {
        // Arrange
        Venta ventaDB = Venta.builder()
                .idVenta(1L)
                .direccionCompra("Calle Falsa 123")
                .valorCompra(1000)
                .fechaCompra(LocalDate.of(2025, 4, 14))
                .despachoGenerado(false)
                .build();
        Venta cambios = Venta.builder()
                .valorCompra(2000)
                .build();
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaDB));
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaDB);

        // Act
        Venta ventaActualizada = ventaService.updateVenta(1L, cambios);

        // Assert
        verify(ventaRepository, times(1)).save(ventaDB);
        assertEquals("Calle Falsa 123", ventaActualizada.getDireccionCompra());
        assertEquals(LocalDate.of(2025, 4, 14), ventaActualizada.getFechaCompra());
        assertFalse(ventaActualizada.getDespachoGenerado());
        assertEquals(2000, ventaActualizada.getValorCompra());
    }

    @Test
    @DisplayName("Cuando la dirección enviada viene en blanco, entonces se conserva la dirección original")
    public void whenUpdatingVentaWithBlankDireccion_thenOriginalDireccionIsKept() throws VentaNotFoundException {
        // Arrange
        Venta ventaDB = Venta.builder()
                .idVenta(1L)
                .direccionCompra("Calle Falsa 123")
                .valorCompra(1000)
                .fechaCompra(LocalDate.of(2025, 4, 14))
                .despachoGenerado(false)
                .build();
        Venta cambios = Venta.builder()
                .direccionCompra("   ")
                .valorCompra(2500)
                .build();
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaDB));
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaDB);

        // Act
        Venta ventaActualizada = ventaService.updateVenta(1L, cambios);

        // Assert
        verify(ventaRepository, times(1)).save(ventaDB);
        assertEquals("Calle Falsa 123", ventaActualizada.getDireccionCompra());
        assertEquals(2500, ventaActualizada.getValorCompra());
    }

    @Test
    @DisplayName("Cuando se elimina una venta con ID existente, entonces se invoca deleteById")
    public void whenDeletingVentaWithExistingId_thenDeleteByIdIsCalled() throws VentaNotFoundException {
        // Arrange
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        // Act
        ventaService.deleteVenta(1L);

        // Assert
        verify(ventaRepository, times(1)).findById(1L);
        verify(ventaRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Cuando se elimina una venta con ID inexistente, entonces se lanza VentaNotFoundException")
    public void whenDeletingVentaWithUnknownId_thenVentaNotFoundExceptionIsThrown(){
        // Arrange
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        VentaNotFoundException excepcion = assertThrows(VentaNotFoundException.class,
                () -> ventaService.deleteVenta(99L));

        // Assert
        verify(ventaRepository, never()).deleteById(anyLong());
        assertTrue(excepcion.getMessage().contains("99"));
    }

    @Test
    @DisplayName("Cuando se busca una venta por un ID existente, entonces se devuelve la venta")
    public void whenFindingVentaByExistingId_thenVentaIsReturned() throws VentaNotFoundException {
        // Arrange
        Venta ventaDB = Venta.builder()
                .idVenta(1L)
                .direccionCompra("Calle Falsa 123")
                .valorCompra(1000)
                .fechaCompra(LocalDate.of(2025, 4, 14))
                .despachoGenerado(false)
                .build();
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaDB));

        // Act
        Venta encontrada = ventaService.findById(1L);

        // Assert
        verify(ventaRepository, times(1)).findById(1L);
        assertNotNull(encontrada);
        assertEquals(1L, encontrada.getIdVenta());
        assertEquals("Calle Falsa 123", encontrada.getDireccionCompra());
    }

    @Test
    @DisplayName("Cuando se busca una venta por un ID inexistente, entonces se lanza VentaNotFoundException")
    public void whenFindingVentaByUnknownId_thenVentaNotFoundExceptionIsThrown(){
        // Arrange
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        VentaNotFoundException excepcion = assertThrows(VentaNotFoundException.class,
                () -> ventaService.findById(99L));

        // Assert
        verify(ventaRepository, times(1)).findById(99L);
        assertTrue(excepcion.getMessage().contains("99"));
    }
}
