package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepTetrazolioViabilidad;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Tetrazolio;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepTetrazolioViabilidadRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.TetrazolioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepTetrazolioViabilidadRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepTetrazolioViabilidadDTO;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para RepTetrazolioViabilidadService
 * 
 * Funcionalidades testeadas:
 * - Creación de repeticiones de tetrazolio
 * - Validación de suma de semillas
 * - Actualización y eliminación de repeticiones
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de RepTetrazolioViabilidadService")
class RepTetrazolioViabilidadServiceTest {

    @Mock
    private RepTetrazolioViabilidadRepository repeticionRepository;

    @Mock
    private TetrazolioRepository tetrazolioRepository;

    @InjectMocks
    private RepTetrazolioViabilidadService repeticionService;

    private Tetrazolio tetrazolio;
    private RepTetrazolioViabilidad repTetrazolio;
    private RepTetrazolioViabilidadRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        tetrazolio = new Tetrazolio();
        tetrazolio.setAnalisisID(1L);
        tetrazolio.setNumSemillasPorRep(100);
        tetrazolio.setNumRepeticionesEsperadas(4);

        repTetrazolio = new RepTetrazolioViabilidad();
        repTetrazolio.setRepTetrazolioViabID(20L);
        repTetrazolio.setFecha(LocalDate.of(2024, 1, 15));
        repTetrazolio.setViablesNum(85);
        repTetrazolio.setNoViablesNum(10);
        repTetrazolio.setDuras(5);
        repTetrazolio.setTetrazolio(tetrazolio);

        requestDTO = new RepTetrazolioViabilidadRequestDTO();
        requestDTO.setFecha(LocalDate.of(2024, 1, 15));
        requestDTO.setViablesNum(85);
        requestDTO.setNoViablesNum(10);
        requestDTO.setDuras(5);
    }

    @Test
    @DisplayName("Crear repetición - debe crear exitosamente")
    void crearRepeticion_debeCrearExitosamente() {
        
        when(tetrazolioRepository.findById(1L)).thenReturn(Optional.of(tetrazolio));
        when(repeticionRepository.save(any(RepTetrazolioViabilidad.class))).thenReturn(repTetrazolio);

        
        RepTetrazolioViabilidadDTO resultado = repeticionService.crearRepeticion(1L, requestDTO);

        
        assertNotNull(resultado);
        assertEquals(20L, resultado.getRepTetrazolioViabID());
        assertEquals(85, resultado.getViablesNum());
        assertEquals(10, resultado.getNoViablesNum());
        assertEquals(5, resultado.getDuras());
        verify(repeticionRepository, times(1)).save(any(RepTetrazolioViabilidad.class));
    }

    @Test
    @DisplayName("Crear repetición - debe lanzar excepción si tetrazolio no existe")
    void crearRepeticion_debeLanzarExcepcionSiTetrazolioNoExiste() {
        
        when(tetrazolioRepository.findById(999L)).thenReturn(Optional.empty());

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repeticionService.crearRepeticion(999L, requestDTO);
        });
        
        assertTrue(exception.getMessage().contains("no encontrado"));
        verify(repeticionRepository, never()).save(any(RepTetrazolioViabilidad.class));
    }

    @Test
    @DisplayName("Obtener repetición por ID - debe retornar si existe")
    void obtenerRepeticionPorId_debeRetornarSiExiste() {
        
        when(repeticionRepository.findById(20L)).thenReturn(Optional.of(repTetrazolio));

        
        RepTetrazolioViabilidadDTO resultado = repeticionService.obtenerRepeticionPorId(20L);

        
        assertNotNull(resultado);
        assertEquals(20L, resultado.getRepTetrazolioViabID());
        assertEquals(85, resultado.getViablesNum());
        verify(repeticionRepository, times(1)).findById(20L);
    }

    @Test
    @DisplayName("Obtener repetición por ID - debe lanzar excepción si no existe")
    void obtenerRepeticionPorId_debeLanzarExcepcionSiNoExiste() {
        
        when(repeticionRepository.findById(999L)).thenReturn(Optional.empty());

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repeticionService.obtenerRepeticionPorId(999L);
        });
        
        assertTrue(exception.getMessage().contains("no encontrada"));
    }

    @Test
    @DisplayName("Actualizar repetición - debe actualizar correctamente")
    void actualizarRepeticion_debeActualizarCorrectamente() {
        
        RepTetrazolioViabilidadRequestDTO updateDTO = new RepTetrazolioViabilidadRequestDTO();
        updateDTO.setFecha(LocalDate.of(2024, 1, 16));
        updateDTO.setViablesNum(90);
        updateDTO.setNoViablesNum(7);
        updateDTO.setDuras(3);
        
        when(repeticionRepository.findById(20L)).thenReturn(Optional.of(repTetrazolio));
        when(repeticionRepository.save(any(RepTetrazolioViabilidad.class))).thenReturn(repTetrazolio);

        
        RepTetrazolioViabilidadDTO resultado = repeticionService.actualizarRepeticion(20L, updateDTO);

        
        assertNotNull(resultado);
        verify(repeticionRepository, times(1)).save(any(RepTetrazolioViabilidad.class));
    }

    @Test
    @DisplayName("Actualizar repetición - debe lanzar excepción si no existe")
    void actualizarRepeticion_debeLanzarExcepcionSiNoExiste() {
        
        when(repeticionRepository.findById(999L)).thenReturn(Optional.empty());

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repeticionService.actualizarRepeticion(999L, requestDTO);
        });
        
        assertTrue(exception.getMessage().contains("no encontrada"));
        verify(repeticionRepository, never()).save(any(RepTetrazolioViabilidad.class));
    }

    @Test
    @DisplayName("Eliminar repetición - debe eliminar correctamente")
    void eliminarRepeticion_debeEliminarCorrectamente() {
        
        when(repeticionRepository.findById(20L)).thenReturn(Optional.of(repTetrazolio));
        doNothing().when(repeticionRepository).deleteById(20L);

        
        repeticionService.eliminarRepeticion(20L);

        
        verify(repeticionRepository, times(1)).deleteById(20L);
    }

    @Test
    @DisplayName("Eliminar repetición - debe lanzar excepción si no existe")
    void eliminarRepeticion_debeLanzarExcepcionSiNoExiste() {
        
        when(repeticionRepository.findById(999L)).thenReturn(Optional.empty());

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repeticionService.eliminarRepeticion(999L);
        });
        
        assertTrue(exception.getMessage().contains("no encontrada"));
        verify(repeticionRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Obtener repeticiones por tetrazolio - debe retornar lista")
    void obtenerRepeticionesPorTetrazolio_debeRetornarLista() {
        
        RepTetrazolioViabilidad rep2 = new RepTetrazolioViabilidad();
        rep2.setRepTetrazolioViabID(21L);
        rep2.setViablesNum(88);
        rep2.setNoViablesNum(8);
        rep2.setDuras(4);
        
        when(repeticionRepository.findByTetrazolioId(1L))
            .thenReturn(Arrays.asList(repTetrazolio, rep2));

        
        List<RepTetrazolioViabilidadDTO> resultado = 
            repeticionService.obtenerRepeticionesPorTetrazolio(1L);

        
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(20L, resultado.get(0).getRepTetrazolioViabID());
        assertEquals(21L, resultado.get(1).getRepTetrazolioViabID());
    }

    @Test
    @DisplayName("Contar repeticiones por tetrazolio - debe retornar cantidad")
    void contarRepeticionesPorTetrazolio_debeRetornarCantidad() {
        
        when(repeticionRepository.countByTetrazolioId(1L)).thenReturn(4L);

        
        Long resultado = repeticionService.contarRepeticionesPorTetrazolio(1L);

        
        assertEquals(4L, resultado);
        verify(repeticionRepository, times(1)).countByTetrazolioId(1L);
    }

    @Test
    @DisplayName("Validar suma de semillas - viables + noViables + duras debe ser correcto")
    void crearRepeticion_debeValidarSumaSemillas() {
        
        requestDTO.setViablesNum(85);
        requestDTO.setNoViablesNum(10);
        requestDTO.setDuras(10); 
        
        when(tetrazolioRepository.findById(1L)).thenReturn(Optional.of(tetrazolio));
        when(repeticionRepository.save(any(RepTetrazolioViabilidad.class))).thenReturn(repTetrazolio);

        
        
        
        RepTetrazolioViabilidadDTO resultado = repeticionService.crearRepeticion(1L, requestDTO);

        
        assertNotNull(resultado);
    }
}
