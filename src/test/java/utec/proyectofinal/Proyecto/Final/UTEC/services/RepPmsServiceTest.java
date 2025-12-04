package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepPms;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PmsRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepPmsRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepPmsRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepPmsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de RepPmsService")
class RepPmsServiceTest {

    @Mock
    private RepPmsRepository repPmsRepository;

    @Mock
    private PmsRepository pmsRepository;

    @Mock
    private PmsService pmsService;

    @Mock
    private AnalisisService analisisService;

    @Mock
    private AnalisisHistorialService analisisHistorialService;

    @InjectMocks
    private RepPmsService repPmsService;

    private Pms pms;
    private RepPms repPms;
    private RepPmsRequestDTO repPmsRequestDTO;

    @BeforeEach
    void setUp() {
        pms = new Pms();
        pms.setAnalisisID(1L);
        pms.setEstado(Estado.REGISTRADO);
        pms.setNumRepeticionesEsperadas(8);
        pms.setNumTandas(1);
        pms.setEsSemillaBrozosa(false);
        pms.setActivo(true);

        repPms = new RepPms();
        repPms.setRepPMSID(10L);
        repPms.setNumRep(1);
        repPms.setNumTanda(1);
        repPms.setPeso(new BigDecimal("45.5"));
        repPms.setValido(null);
        repPms.setPms(pms);

        repPmsRequestDTO = new RepPmsRequestDTO();
        repPmsRequestDTO.setNumRep(1);
        repPmsRequestDTO.setPeso(new BigDecimal("45.5"));
    }

    @Test
    @DisplayName("Crear repetición PMS - debe crear exitosamente")
    void crearRepeticion_debeCrearExitosamente() {
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.countByPmsId(1L)).thenReturn(0L);
        when(repPmsRepository.findByPmsId(1L)).thenReturn(Arrays.asList());
        when(repPmsRepository.save(any(RepPms.class))).thenReturn(repPms);
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Pms.class));

        
        RepPmsDTO resultado = repPmsService.crearRepeticion(1L, repPmsRequestDTO);

        
        assertNotNull(resultado);
        assertEquals(10L, resultado.getRepPMSID());
        assertEquals(1, resultado.getNumRep());
        assertEquals(new BigDecimal("45.5"), resultado.getPeso());
        verify(repPmsRepository, times(1)).save(any(RepPms.class));
    }

    @Test
    @DisplayName("Crear repetición - debe cambiar estado a EN_PROCESO en primera repetición")
    void crearRepeticion_debeCambiarEstadoEnProceso() {
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.countByPmsId(1L)).thenReturn(0L);
        when(repPmsRepository.findByPmsId(1L)).thenReturn(Arrays.asList());
        when(repPmsRepository.save(any(RepPms.class))).thenReturn(repPms);
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Pms.class));

        
        repPmsService.crearRepeticion(1L, repPmsRequestDTO);

        
        verify(pmsRepository).save(argThat(p -> p.getEstado() == Estado.EN_PROCESO));
    }

    @Test
    @DisplayName("Crear repetición - debe lanzar excepción si PMS no existe")
    void crearRepeticion_debeLanzarExcepcionSiPmsNoExiste() {
        
        when(pmsRepository.findById(999L)).thenReturn(Optional.empty());

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repPmsService.crearRepeticion(999L, repPmsRequestDTO);
        });
        
        assertTrue(exception.getMessage().contains("no encontrado"));
        verify(repPmsRepository, never()).save(any(RepPms.class));
    }

    @Test
    @DisplayName("Crear repetición - debe lanzar excepción si PMS está finalizado")
    void crearRepeticion_debeLanzarExcepcionSiPmsEstFinalizado() {
        
        pms.setEstado(Estado.APROBADO);
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repPmsService.crearRepeticion(1L, repPmsRequestDTO);
        });
        
        assertTrue(exception.getMessage().contains("REGISTRADO o EN_PROCESO"));
        verify(repPmsRepository, never()).save(any(RepPms.class));
    }

    @Test
    @DisplayName("Crear repetición - debe lanzar excepción si se exceden 16 repeticiones")
    void crearRepeticion_debeLanzarExcepcionSiExcede16() {
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.countByPmsId(1L)).thenReturn(16L);

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repPmsService.crearRepeticion(1L, repPmsRequestDTO);
        });
        
        assertTrue(exception.getMessage().contains("más de 16 repeticiones"));
        verify(repPmsRepository, never()).save(any(RepPms.class));
    }

    @Test
    @DisplayName("Obtener repetición por ID - debe retornar repetición si existe")
    void obtenerPorId_debeRetornarRepeticionSiExiste() {
        
        when(repPmsRepository.findById(10L)).thenReturn(Optional.of(repPms));

        
        RepPmsDTO resultado = repPmsService.obtenerPorId(10L);

        
        assertNotNull(resultado);
        assertEquals(10L, resultado.getRepPMSID());
        assertEquals(new BigDecimal("45.5"), resultado.getPeso());
        verify(repPmsRepository, times(1)).findById(10L);
    }

    @Test
    @DisplayName("Obtener repetición por ID - debe lanzar excepción si no existe")
    void obtenerPorId_debeLanzarExcepcionSiNoExiste() {
        
        when(repPmsRepository.findById(999L)).thenReturn(Optional.empty());

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repPmsService.obtenerPorId(999L);
        });
        
        assertTrue(exception.getMessage().contains("no encontrada"));
    }

    @Test
    @DisplayName("Actualizar repetición - debe actualizar correctamente")
    void actualizarRepeticion_debeActualizarCorrectamente() {
        
        RepPmsRequestDTO updateDTO = new RepPmsRequestDTO();
        updateDTO.setNumRep(1);
        updateDTO.setPeso(new BigDecimal("47.3"));
        
        when(repPmsRepository.findById(10L)).thenReturn(Optional.of(repPms));
        when(repPmsRepository.save(any(RepPms.class))).thenReturn(repPms);
        when(repPmsRepository.countByPmsId(1L)).thenReturn(8L);
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any(Pms.class));
        doNothing().when(pmsService).validarTodasLasRepeticiones(1L);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Pms.class));

        
        RepPmsDTO resultado = repPmsService.actualizarRepeticion(10L, updateDTO);

        
        assertNotNull(resultado);
        verify(repPmsRepository, times(1)).save(any(RepPms.class));
        verify(analisisHistorialService, times(1)).registrarModificacion(any(Pms.class));
    }

    @Test
    @DisplayName("Eliminar repetición - debe eliminar correctamente")
    void eliminarRepeticion_debeEliminarCorrectamente() {
        
        when(repPmsRepository.findById(10L)).thenReturn(Optional.of(repPms));
        doNothing().when(repPmsRepository).deleteById(10L);
        when(repPmsRepository.countByPmsId(1L)).thenReturn(7L);
        when(repPmsRepository.findByPmsId(1L)).thenReturn(Arrays.asList());
        doNothing().when(pmsService).actualizarEstadisticasPms(1L);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Pms.class));

        
        repPmsService.eliminarRepeticion(10L);

        
        verify(repPmsRepository, times(1)).deleteById(10L);
        verify(analisisHistorialService, times(1)).registrarModificacion(any(Pms.class));
    }

    @Test
    @DisplayName("Eliminar repetición - debe lanzar excepción si no existe")
    void eliminarRepeticion_debeLanzarExcepcionSiNoExiste() {
        
        when(repPmsRepository.findById(999L)).thenReturn(Optional.empty());

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repPmsService.eliminarRepeticion(999L);
        });
        
        assertTrue(exception.getMessage().contains("no encontrada"));
        verify(repPmsRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Obtener repeticiones por PMS - debe retornar lista")
    void obtenerPorPms_debeRetornarLista() {
        
        RepPms rep2 = new RepPms();
        rep2.setRepPMSID(11L);
        rep2.setNumRep(2);
        rep2.setNumTanda(1);
        rep2.setPeso(new BigDecimal("46.2"));
        rep2.setPms(pms);
        
        when(repPmsRepository.findByPmsId(1L)).thenReturn(Arrays.asList(repPms, rep2));

        
        List<RepPmsDTO> resultado = repPmsService.obtenerPorPms(1L);

        
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(10L, resultado.get(0).getRepPMSID());
        assertEquals(11L, resultado.get(1).getRepPMSID());
    }

    @Test
    @DisplayName("Contar repeticiones por PMS - debe retornar cantidad correcta")
    void contarPorPms_debeRetornarCantidad() {
        
        when(repPmsRepository.countByPmsId(1L)).thenReturn(8L);

        
        Long resultado = repPmsService.contarPorPms(1L);

        
        assertEquals(8L, resultado);
        verify(repPmsRepository, times(1)).countByPmsId(1L);
    }

    
    
    

    @Test
    @DisplayName("determinarTandaActual - debe retornar 1 cuando no hay repeticiones")
    void determinarTandaActual_debeRetornar1CuandoNoHayRepeticiones() {
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.countByPmsId(1L)).thenReturn(0L);
        when(repPmsRepository.findByPmsId(1L)).thenReturn(Arrays.asList()); 
        when(repPmsRepository.save(any(RepPms.class))).thenReturn(repPms);
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Pms.class));

        
        RepPmsDTO resultado = repPmsService.crearRepeticion(1L, repPmsRequestDTO);

        
        assertNotNull(resultado);
        assertEquals(1, resultado.getNumTanda(), "Debe asignar tanda 1 cuando no hay repeticiones");
    }

    @Test
    @DisplayName("determinarTandaActual - debe retornar tanda incompleta con repeticiones válidas insuficientes")
    void determinarTandaActual_debeRetornarTandaIncompletaConValidasInsuficientes() {
        
        
        RepPms rep1 = crearRepeticionConValor(1L, 1, 1, new BigDecimal("45.0"), true);
        RepPms rep2 = crearRepeticionConValor(2L, 2, 1, new BigDecimal("45.5"), true);
        RepPms rep3 = crearRepeticionConValor(3L, 3, 1, new BigDecimal("46.0"), true);
        RepPms rep4 = crearRepeticionConValor(4L, 4, 1, new BigDecimal("45.8"), true);
        RepPms rep5 = crearRepeticionConValor(5L, 5, 1, new BigDecimal("45.3"), true);

        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.countByPmsId(1L)).thenReturn(5L);
        when(repPmsRepository.findByPmsId(1L)).thenReturn(Arrays.asList(rep1, rep2, rep3, rep4, rep5));
        when(repPmsRepository.save(any(RepPms.class))).thenReturn(repPms);
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Pms.class));

        
        RepPmsDTO resultado = repPmsService.crearRepeticion(1L, repPmsRequestDTO);

        
        assertNotNull(resultado);
        assertEquals(1, resultado.getNumTanda(), "Debe seguir en tanda 1 porque tiene menos de 8 válidas");
    }

    @Test
    @DisplayName("determinarTandaActual - debe retornar tanda incompleta aunque haya inválidas")
    void determinarTandaActual_debeRetornarTandaIncompletaConInvalidas() {
        
        
        RepPms rep1 = crearRepeticionConValor(1L, 1, 1, new BigDecimal("45.0"), true);
        RepPms rep2 = crearRepeticionConValor(2L, 2, 1, new BigDecimal("45.5"), true);
        RepPms rep3 = crearRepeticionConValor(3L, 3, 1, new BigDecimal("46.0"), true);
        RepPms rep4 = crearRepeticionConValor(4L, 4, 1, new BigDecimal("45.8"), true);
        RepPms rep5 = crearRepeticionConValor(5L, 5, 1, new BigDecimal("45.3"), true);
        RepPms rep6 = crearRepeticionConValor(6L, 6, 1, new BigDecimal("60.0"), false); 
        RepPms rep7 = crearRepeticionConValor(7L, 7, 1, new BigDecimal("30.0"), false); 

        RepPms octavaRep = crearRepeticionConValor(8L, 8, 1, new BigDecimal("45.6"), null);

        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.countByPmsId(1L)).thenReturn(7L);
        when(repPmsRepository.findByPmsId(1L)).thenReturn(Arrays.asList(rep1, rep2, rep3, rep4, rep5, rep6, rep7));
        when(repPmsRepository.save(any(RepPms.class))).thenReturn(octavaRep);
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        when(repPmsRepository.findById(8L)).thenReturn(Optional.of(octavaRep));
        doNothing().when(pmsService).validarTodasLasRepeticiones(1L);

        
        RepPmsDTO resultado = repPmsService.crearRepeticion(1L, repPmsRequestDTO);

        
        assertNotNull(resultado);
        assertEquals(1, resultado.getNumTanda(), "Debe seguir en tanda 1 porque solo tiene 5 válidas (necesita 8)");
    }

    @Test
    @DisplayName("determinarTandaActual - debe incrementar tanda cuando CV no es aceptable")
    void determinarTandaActual_debeIncrementarTandaCuandoCVNoAceptable() {
        
        
        RepPms rep1 = crearRepeticionConValor(1L, 1, 1, new BigDecimal("45.0"), true);
        RepPms rep2 = crearRepeticionConValor(2L, 2, 1, new BigDecimal("46.0"), true);
        RepPms rep3 = crearRepeticionConValor(3L, 3, 1, new BigDecimal("47.0"), true);
        RepPms rep4 = crearRepeticionConValor(4L, 4, 1, new BigDecimal("48.0"), true);
        RepPms rep5 = crearRepeticionConValor(5L, 5, 1, new BigDecimal("49.0"), true);
        RepPms rep6 = crearRepeticionConValor(6L, 6, 1, new BigDecimal("50.0"), true);
        RepPms rep7 = crearRepeticionConValor(7L, 7, 1, new BigDecimal("51.0"), true);
        RepPms rep8 = crearRepeticionConValor(8L, 8, 1, new BigDecimal("52.0"), true);

        pms.setNumTandas(1);

        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.countByPmsId(1L)).thenReturn(8L);
        when(repPmsRepository.findByPmsId(1L)).thenReturn(Arrays.asList(rep1, rep2, rep3, rep4, rep5, rep6, rep7, rep8));
        when(pmsRepository.save(any(Pms.class))).thenAnswer(invocation -> {
            Pms savedPms = invocation.getArgument(0);
            
            if (savedPms.getNumTandas() == 2) {
                pms.setNumTandas(2); 
            }
            return savedPms;
        });
        
        RepPms nuevaRep = crearRepeticionConValor(9L, 9, 2, new BigDecimal("45.5"), null);
        when(repPmsRepository.save(any(RepPms.class))).thenReturn(nuevaRep);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Pms.class));

        
        RepPmsDTO resultado = repPmsService.crearRepeticion(1L, repPmsRequestDTO);

        
        assertNotNull(resultado);
        assertEquals(2, resultado.getNumTanda(), "Debe asignar tanda 2 porque la tanda 1 está completa pero CV no es aceptable");
        verify(pmsRepository, atLeastOnce()).save(argThat(p -> p.getNumTandas() == 2));
    }

    @Test
    @DisplayName("determinarTandaActual - debe lanzar excepción cuando CV es aceptable")
    void determinarTandaActual_debeLanzarExcepcionCuandoCVAceptable() {
        
        
        RepPms rep1 = crearRepeticionConValor(1L, 1, 1, new BigDecimal("45.0"), true);
        RepPms rep2 = crearRepeticionConValor(2L, 2, 1, new BigDecimal("45.1"), true);
        RepPms rep3 = crearRepeticionConValor(3L, 3, 1, new BigDecimal("45.2"), true);
        RepPms rep4 = crearRepeticionConValor(4L, 4, 1, new BigDecimal("45.0"), true);
        RepPms rep5 = crearRepeticionConValor(5L, 5, 1, new BigDecimal("45.1"), true);
        RepPms rep6 = crearRepeticionConValor(6L, 6, 1, new BigDecimal("45.2"), true);
        RepPms rep7 = crearRepeticionConValor(7L, 7, 1, new BigDecimal("45.0"), true);
        RepPms rep8 = crearRepeticionConValor(8L, 8, 1, new BigDecimal("45.1"), true);

        pms.setNumTandas(1);

        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.countByPmsId(1L)).thenReturn(8L);
        when(repPmsRepository.findByPmsId(1L)).thenReturn(Arrays.asList(rep1, rep2, rep3, rep4, rep5, rep6, rep7, rep8));

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repPmsService.crearRepeticion(1L, repPmsRequestDTO);
        });

        assertTrue(exception.getMessage().contains("CV global") && exception.getMessage().contains("ya es aceptable"),
                "Debe lanzar excepción indicando que el CV ya es aceptable");
    }

    @Test
    @DisplayName("determinarTandaActual - debe lanzar excepción cuando se alcanza el límite de 16 repeticiones")
    void determinarTandaActual_debeLanzarExcepcionCuandoAlcanzaLimite16() {
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.countByPmsId(1L)).thenReturn(16L); 

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repPmsService.crearRepeticion(1L, repPmsRequestDTO);
        });

        assertTrue(exception.getMessage().contains("más de 16 repeticiones"),
                "Debe lanzar excepción por límite de 16 repeticiones");
    }

    @Test
    @DisplayName("determinarTandaActual - debe retornar tanda 2 si tanda 1 está completa con suficientes válidas")
    void determinarTandaActual_debeRetornarTanda2SiTanda1CompletaConValidas() {
        
        
        
        List<RepPms> repeticiones = Arrays.asList(
            crearRepeticionConValor(1L, 1, 1, new BigDecimal("45.0"), true),
            crearRepeticionConValor(2L, 2, 1, new BigDecimal("46.0"), true),
            crearRepeticionConValor(3L, 3, 1, new BigDecimal("47.0"), true),
            crearRepeticionConValor(4L, 4, 1, new BigDecimal("48.0"), true),
            crearRepeticionConValor(5L, 5, 1, new BigDecimal("49.0"), true),
            crearRepeticionConValor(6L, 6, 1, new BigDecimal("50.0"), true),
            crearRepeticionConValor(7L, 7, 1, new BigDecimal("51.0"), true),
            crearRepeticionConValor(8L, 8, 1, new BigDecimal("52.0"), true),
            crearRepeticionConValor(9L, 9, 2, new BigDecimal("46.1"), true),
            crearRepeticionConValor(10L, 10, 2, new BigDecimal("46.3"), true),
            crearRepeticionConValor(11L, 11, 2, new BigDecimal("46.5"), true)
        );

        pms.setNumTandas(2);

        RepPms nuevaRep = crearRepeticionConValor(12L, 12, 2, new BigDecimal("46.7"), null);

        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.countByPmsId(1L)).thenReturn(11L);
        when(repPmsRepository.findByPmsId(1L)).thenReturn(repeticiones);
        when(repPmsRepository.save(any(RepPms.class))).thenReturn(nuevaRep);
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Pms.class));

        
        RepPmsDTO resultado = repPmsService.crearRepeticion(1L, repPmsRequestDTO);

        
        assertNotNull(resultado);
        assertEquals(2, resultado.getNumTanda(), "Debe asignar tanda 2 porque la tanda 1 está completa");
    }

    
    
    

    @Test
    @DisplayName("crearRepeticion - debe validar todas las repeticiones cuando se completa la tanda")
    void crearRepeticion_debeValidarTodasCuandoCompletaTanda() {
        
        
        List<RepPms> repeticionesExistentes = Arrays.asList(
            crearRepeticionConValor(1L, 1, 1, new BigDecimal("45.0"), null),
            crearRepeticionConValor(2L, 2, 1, new BigDecimal("45.5"), null),
            crearRepeticionConValor(3L, 3, 1, new BigDecimal("46.0"), null),
            crearRepeticionConValor(4L, 4, 1, new BigDecimal("45.8"), null),
            crearRepeticionConValor(5L, 5, 1, new BigDecimal("45.3"), null),
            crearRepeticionConValor(6L, 6, 1, new BigDecimal("45.7"), null),
            crearRepeticionConValor(7L, 7, 1, new BigDecimal("45.2"), null)
        );

        
        RepPms octavaRepeticion = crearRepeticionConValor(8L, 8, 1, new BigDecimal("45.9"), true);

        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.countByPmsId(1L)).thenReturn(7L);
        when(repPmsRepository.findByPmsId(1L)).thenReturn(repeticionesExistentes);
        when(repPmsRepository.save(any(RepPms.class))).thenReturn(octavaRepeticion);
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        when(repPmsRepository.findById(8L)).thenReturn(Optional.of(octavaRepeticion));
        
        
        doNothing().when(pmsService).validarTodasLasRepeticiones(1L);

        
        RepPmsDTO resultado = repPmsService.crearRepeticion(1L, repPmsRequestDTO);

        
        assertNotNull(resultado);
        assertEquals(8L, resultado.getRepPMSID(), "Debe ser la octava repetición");
        
        
        verify(pmsService, times(1)).validarTodasLasRepeticiones(1L);
        
        
        verify(repPmsRepository, times(1)).findById(8L);
    }

    @Test
    @DisplayName("crearRepeticion - NO debe validar si no se completa la tanda")
    void crearRepeticion_noDebeValidarSiNoCompletaTanda() {
        
        
        List<RepPms> repeticionesExistentes = Arrays.asList(
            crearRepeticionConValor(1L, 1, 1, new BigDecimal("45.0"), null),
            crearRepeticionConValor(2L, 2, 1, new BigDecimal("45.5"), null),
            crearRepeticionConValor(3L, 3, 1, new BigDecimal("46.0"), null),
            crearRepeticionConValor(4L, 4, 1, new BigDecimal("45.8"), null),
            crearRepeticionConValor(5L, 5, 1, new BigDecimal("45.3"), null)
        );

        RepPms sextaRepeticion = crearRepeticionConValor(6L, 6, 1, new BigDecimal("45.7"), null);

        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.countByPmsId(1L)).thenReturn(5L);
        when(repPmsRepository.findByPmsId(1L)).thenReturn(repeticionesExistentes);
        when(repPmsRepository.save(any(RepPms.class))).thenReturn(sextaRepeticion);
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Pms.class));

        
        RepPmsDTO resultado = repPmsService.crearRepeticion(1L, repPmsRequestDTO);

        
        assertNotNull(resultado);
        
        
        verify(pmsService, never()).validarTodasLasRepeticiones(anyLong());
        
        
        verify(analisisHistorialService, times(1)).registrarModificacion(pms);
    }

    @Test
    @DisplayName("crearRepeticion - debe validar cuando se alcanza exactamente el número esperado")
    void crearRepeticion_debeValidarCuandoAlcanzaExactamenteNumeroEsperado() {
        
        pms.setNumRepeticionesEsperadas(4); 
        
        List<RepPms> repeticionesExistentes = Arrays.asList(
            crearRepeticionConValor(1L, 1, 1, new BigDecimal("45.0"), null),
            crearRepeticionConValor(2L, 2, 1, new BigDecimal("45.5"), null),
            crearRepeticionConValor(3L, 3, 1, new BigDecimal("46.0"), null)
        );

        RepPms cuartaRepeticion = crearRepeticionConValor(4L, 4, 1, new BigDecimal("45.8"), true);

        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.countByPmsId(1L)).thenReturn(3L);
        when(repPmsRepository.findByPmsId(1L)).thenReturn(repeticionesExistentes);
        when(repPmsRepository.save(any(RepPms.class))).thenReturn(cuartaRepeticion);
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        when(repPmsRepository.findById(4L)).thenReturn(Optional.of(cuartaRepeticion));
        
        doNothing().when(pmsService).validarTodasLasRepeticiones(1L);

        
        RepPmsDTO resultado = repPmsService.crearRepeticion(1L, repPmsRequestDTO);

        
        assertNotNull(resultado);
        
        
        verify(pmsService, times(1)).validarTodasLasRepeticiones(1L);
    }

    
    private RepPms crearRepeticionConValor(Long id, Integer numRep, Integer numTanda, 
                                           BigDecimal peso, Boolean valido) {
        RepPms rep = new RepPms();
        rep.setRepPMSID(id);
        rep.setNumRep(numRep);
        rep.setNumTanda(numTanda);
        rep.setPeso(peso);
        rep.setValido(valido);
        rep.setPms(pms);
        return rep;
    }
}
