package utec.proyectofinal.Proyecto.Final.UTEC.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.TablaGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ValoresGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.ValoresGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ValoresGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ValoresGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios para ValoresGermService")
class ValoresGermServiceTest {

    @Mock
    private ValoresGermRepository valoresGermRepository;

    @Mock
    private AnalisisService analisisService;

    @InjectMocks
    private ValoresGermService valoresGermService;

    private ValoresGerm valoresGerm;
    private TablaGerm tablaGerm;
    private Germinacion germinacion;
    private ValoresGermRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        germinacion = new Germinacion();
        germinacion.setAnalisisID(1L);

        tablaGerm = new TablaGerm();
        tablaGerm.setTablaGermID(1L);
        tablaGerm.setGerminacion(germinacion);

        valoresGerm = new ValoresGerm();
        valoresGerm.setValoresGermID(1L);
        valoresGerm.setInstituto(Instituto.INIA);
        valoresGerm.setNormales(new BigDecimal("80.00"));
        valoresGerm.setAnormales(new BigDecimal("10.00"));
        valoresGerm.setDuras(new BigDecimal("5.00"));
        valoresGerm.setFrescas(new BigDecimal("3.00"));
        valoresGerm.setMuertas(new BigDecimal("2.00"));
        valoresGerm.setGerminacion(new BigDecimal("90.00"));
        valoresGerm.setTablaGerm(tablaGerm);

        requestDTO = new ValoresGermRequestDTO();
        requestDTO.setNormales(new BigDecimal("75.00"));
        requestDTO.setAnormales(new BigDecimal("15.00"));
        requestDTO.setDuras(new BigDecimal("5.00"));
        requestDTO.setFrescas(new BigDecimal("3.00"));
        requestDTO.setMuertas(new BigDecimal("2.00"));
        requestDTO.setGerminacion(new BigDecimal("85.00"));
    }

    @Test
    @DisplayName("Debe obtener valores por ID exitosamente")
    void obtenerValoresPorId_conIdExistente_debeRetornarValores() {
        when(valoresGermRepository.findById(1L)).thenReturn(Optional.of(valoresGerm));

        ValoresGermDTO resultado = valoresGermService.obtenerValoresPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getValoresGermID());
        assertEquals(Instituto.INIA, resultado.getInstituto());
        assertEquals(new BigDecimal("80.00"), resultado.getNormales());
        assertEquals(1L, resultado.getTablaGermId());
        verify(valoresGermRepository).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el ID no existe")
    void obtenerValoresPorId_conIdInexistente_debeLanzarExcepcion() {
        when(valoresGermRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> valoresGermService.obtenerValoresPorId(999L));

        assertTrue(exception.getMessage().contains("no encontrados"));
        verify(valoresGermRepository).findById(999L);
    }

    @Test
    @DisplayName("Debe actualizar valores exitosamente")
    void actualizarValores_conDatosValidos_debeRetornarValoresActualizados() {
        when(valoresGermRepository.findById(1L)).thenReturn(Optional.of(valoresGerm));
        when(valoresGermRepository.save(any(ValoresGerm.class))).thenReturn(valoresGerm);
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any(Germinacion.class));

        ValoresGermDTO resultado = valoresGermService.actualizarValores(1L, requestDTO);

        assertNotNull(resultado);
        verify(analisisService).manejarEdicionAnalisisFinalizado(germinacion);
        verify(valoresGermRepository).findById(1L);
        verify(valoresGermRepository).save(any(ValoresGerm.class));
    }

    @Test
    @DisplayName("Debe validar que la suma no supere 100")
    void actualizarValores_conSumaSuperior100_debeLanzarExcepcion() {
        requestDTO.setNormales(new BigDecimal("50.00"));
        requestDTO.setAnormales(new BigDecimal("30.00"));
        requestDTO.setDuras(new BigDecimal("15.00"));
        requestDTO.setFrescas(new BigDecimal("10.00"));
        requestDTO.setMuertas(new BigDecimal("10.00")); 

        when(valoresGermRepository.findById(1L)).thenReturn(Optional.of(valoresGerm));

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> valoresGermService.actualizarValores(1L, requestDTO));

        assertTrue(exception.getMessage().contains("no puede superar 100"));
        verify(valoresGermRepository, never()).save(any(ValoresGerm.class));
    }

    @Test
    @DisplayName("Debe obtener valores por tabla e instituto")
    void obtenerValoresPorTablaEInstituto_conDatosValidos_debeRetornarValores() {
        when(valoresGermRepository.findByTablaGermIdAndInstituto(1L, Instituto.INIA))
            .thenReturn(Optional.of(valoresGerm));

        ValoresGermDTO resultado = valoresGermService.obtenerValoresPorTablaEInstituto(1L, Instituto.INIA);

        assertNotNull(resultado);
        assertEquals(Instituto.INIA, resultado.getInstituto());
        assertEquals(1L, resultado.getTablaGermId());
        verify(valoresGermRepository).findByTablaGermIdAndInstituto(1L, Instituto.INIA);
    }

    @Test
    @DisplayName("Debe obtener todos los valores de una tabla")
    void obtenerValoresPorTabla_debeRetornarListaDeValores() {
        ValoresGerm valoresInase = new ValoresGerm();
        valoresInase.setValoresGermID(2L);
        valoresInase.setInstituto(Instituto.INASE);
        valoresInase.setTablaGerm(tablaGerm);

        List<ValoresGerm> listaValores = Arrays.asList(valoresGerm, valoresInase);
        when(valoresGermRepository.findByTablaGermId(1L)).thenReturn(listaValores);

        List<ValoresGermDTO> resultado = valoresGermService.obtenerValoresPorTabla(1L);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(Instituto.INIA, resultado.get(0).getInstituto());
        assertEquals(Instituto.INASE, resultado.get(1).getInstituto());
        verify(valoresGermRepository).findByTablaGermId(1L);
    }

    @Test
    @DisplayName("Debe obtener valores de INIA por tabla")
    void obtenerValoresIniaPorTabla_debeRetornarValoresDeInia() {
        when(valoresGermRepository.findByTablaGermIdAndInstituto(1L, Instituto.INIA))
            .thenReturn(Optional.of(valoresGerm));

        ValoresGermDTO resultado = valoresGermService.obtenerValoresIniaPorTabla(1L);

        assertNotNull(resultado);
        assertEquals(Instituto.INIA, resultado.getInstituto());
        verify(valoresGermRepository).findByTablaGermIdAndInstituto(1L, Instituto.INIA);
    }

    @Test
    @DisplayName("Debe obtener valores de INASE por tabla")
    void obtenerValoresInasePorTabla_debeRetornarValoresDeInase() {
        valoresGerm.setInstituto(Instituto.INASE);
        when(valoresGermRepository.findByTablaGermIdAndInstituto(1L, Instituto.INASE))
            .thenReturn(Optional.of(valoresGerm));

        ValoresGermDTO resultado = valoresGermService.obtenerValoresInasePorTabla(1L);

        assertNotNull(resultado);
        assertEquals(Instituto.INASE, resultado.getInstituto());
        verify(valoresGermRepository).findByTablaGermIdAndInstituto(1L, Instituto.INASE);
    }

    @Test
    @DisplayName("Debe eliminar valores exitosamente")
    void eliminarValores_conIdExistente_debeEliminarValores() {
        when(valoresGermRepository.findById(1L)).thenReturn(Optional.of(valoresGerm));
        doNothing().when(valoresGermRepository).deleteById(1L);

        assertDoesNotThrow(() -> valoresGermService.eliminarValores(1L));

        verify(valoresGermRepository).findById(1L);
        verify(valoresGermRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar con ID inexistente")
    void eliminarValores_conIdInexistente_debeLanzarExcepcion() {
        when(valoresGermRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> valoresGermService.eliminarValores(999L));

        assertTrue(exception.getMessage().contains("no encontrados"));
        verify(valoresGermRepository).findById(999L);
        verify(valoresGermRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Debe aceptar suma de valores igual a 100")
    void actualizarValores_conSumaIgual100_debeActualizar() {
        requestDTO.setNormales(new BigDecimal("80.00"));
        requestDTO.setAnormales(new BigDecimal("10.00"));
        requestDTO.setDuras(new BigDecimal("5.00"));
        requestDTO.setFrescas(new BigDecimal("3.00"));
        requestDTO.setMuertas(new BigDecimal("2.00")); 

        when(valoresGermRepository.findById(1L)).thenReturn(Optional.of(valoresGerm));
        when(valoresGermRepository.save(any(ValoresGerm.class))).thenReturn(valoresGerm);
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any(Germinacion.class));

        assertDoesNotThrow(() -> valoresGermService.actualizarValores(1L, requestDTO));

        verify(valoresGermRepository).save(any(ValoresGerm.class));
    }

    @Test
    @DisplayName("Debe aceptar valores null en la validación")
    void actualizarValores_conValoresNull_debeActualizar() {
        requestDTO.setNormales(new BigDecimal("50.00"));
        requestDTO.setAnormales(null);
        requestDTO.setDuras(null);
        requestDTO.setFrescas(null);
        requestDTO.setMuertas(null);

        when(valoresGermRepository.findById(1L)).thenReturn(Optional.of(valoresGerm));
        when(valoresGermRepository.save(any(ValoresGerm.class))).thenReturn(valoresGerm);
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any(Germinacion.class));

        assertDoesNotThrow(() -> valoresGermService.actualizarValores(1L, requestDTO));

        verify(valoresGermRepository).save(any(ValoresGerm.class));
    }
}
