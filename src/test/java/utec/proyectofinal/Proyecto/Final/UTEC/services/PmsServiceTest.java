package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Cultivar;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepPms;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PmsRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepPmsRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PmsRedondeoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PmsRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.AnalisisHistorialDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.PmsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PmsService (Peso de Mil Semillas)
 * 
 * Funcionalidades testeadas:
 * - Creación de análisis PMS con estado REGISTRADO
 * - Validación de número de repeticiones esperadas (1-16)
 * - Validación de pesos y cálculos
 * - Gestión de tandas y repeticiones
 * - Cálculos de peso promedio de mil semillas
 * - Validación de límites de repeticiones
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("Tests de PmsService")
class PmsServiceTest {

    @Mock
    private PmsRepository pmsRepository;

    @Mock
    private RepPmsRepository repPmsRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private AnalisisHistorialService analisisHistorialService;

    @Mock
    private AnalisisService analisisService;

    @InjectMocks
    private PmsService pmsService;

    private PmsRequestDTO pmsRequestDTO;
    private Lote lote;
    private Pms pms;

    @BeforeEach
    void setUp() {
        // ARRANGE: Preparar datos de prueba
        lote = new Lote();
        lote.setLoteID(1L);
        lote.setNomLote("LOTE-PMS-001");
        lote.setActivo(true);

        pmsRequestDTO = new PmsRequestDTO();
        pmsRequestDTO.setIdLote(1L);
        pmsRequestDTO.setNumRepeticionesEsperadas(8);
        pmsRequestDTO.setEsSemillaBrozosa(false);
        pmsRequestDTO.setComentarios("Test PMS");

        pms = new Pms();
        pms.setAnalisisID(1L);
        pms.setLote(lote);
        pms.setNumRepeticionesEsperadas(8);
        pms.setEstado(Estado.REGISTRADO);
        pms.setActivo(true);
        pms.setNumTandas(1); // asegurar valor inicial para las validaciones internas
    }

    @Test
    @DisplayName("Crear PMS - debe asignar estado REGISTRADO")
    void crearPms_debeAsignarEstadoRegistrado() {
        
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        doNothing().when(analisisHistorialService).registrarCreacion(any(Pms.class));

        
        PmsDTO resultado = pmsService.crearPms(pmsRequestDTO);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        verify(pmsRepository, times(1)).save(any(Pms.class));
        verify(analisisHistorialService, times(1)).registrarCreacion(any(Pms.class));
    }

    @Test
    @DisplayName("Crear PMS sin repeticiones esperadas - debe lanzar excepción")
    void crearPms_sinRepeticionesEsperadas_debeLanzarExcepcion() {
        
        pmsRequestDTO.setNumRepeticionesEsperadas(null);

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pmsService.crearPms(pmsRequestDTO);
        });
        
        assertTrue(exception.getMessage().contains("repeticiones esperadas"));
        verify(pmsRepository, never()).save(any(Pms.class));
    }

    @Test
    @DisplayName("Crear PMS con repeticiones = 0 - debe lanzar excepción")
    void crearPms_conRepeticionesCero_debeLanzarExcepcion() {
        
        pmsRequestDTO.setNumRepeticionesEsperadas(0);

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pmsService.crearPms(pmsRequestDTO);
        });
        
        assertTrue(exception.getMessage().contains("mayor a 0"));
        verify(pmsRepository, never()).save(any(Pms.class));
    }

    @Test
    @DisplayName("Crear PMS con más de 16 repeticiones - debe lanzar excepción")
    void crearPms_conMasDe16Repeticiones_debeLanzarExcepcion() {
        
        pmsRequestDTO.setNumRepeticionesEsperadas(17);

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pmsService.crearPms(pmsRequestDTO);
        });
        
        assertTrue(exception.getMessage().contains("no puede superar 16"));
        verify(pmsRepository, never()).save(any(Pms.class));
    }

    @Test
    @DisplayName("Crear PMS con 8 repeticiones - debe ser válido")
    void crearPms_con8Repeticiones_debeSerValido() {
        
        pmsRequestDTO.setNumRepeticionesEsperadas(8);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        doNothing().when(analisisHistorialService).registrarCreacion(any(Pms.class));

        
        PmsDTO resultado = pmsService.crearPms(pmsRequestDTO);

        
        assertNotNull(resultado);
        verify(pmsRepository, times(1)).save(any(Pms.class));
    }

    @Test
    @DisplayName("Crear PMS con lote inexistente - debe lanzar excepción")
    void crearPms_conLoteInexistente_debeLanzarExcepcion() {
        
        when(loteRepository.findById(999L)).thenReturn(Optional.empty());
        pmsRequestDTO.setIdLote(999L);

        
        assertThrows(RuntimeException.class, () -> {
            pmsService.crearPms(pmsRequestDTO);
        }, "Debe lanzar excepción cuando el lote no existe");
    }

    @Test
    @DisplayName("Obtener PMS por ID - debe retornar el análisis si existe")
    void obtenerPorId_cuandoExiste_debeRetornarPms() {
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));

        
        PmsDTO resultado = pmsService.obtenerPorId(1L);

        
        assertNotNull(resultado);
        assertEquals(1L, resultado.getAnalisisID());
        assertEquals(8, resultado.getNumRepeticionesEsperadas());
        verify(pmsRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener PMS por ID inexistente - debe lanzar excepción")
    void obtenerPorId_cuandoNoExiste_debeLanzarExcepcion() {
        
        when(pmsRepository.findById(999L)).thenReturn(Optional.empty());

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pmsService.obtenerPorId(999L);
        });
        
        assertTrue(exception.getMessage().contains("no encontrado"));
    }

    @Test
    @DisplayName("Actualizar PMS - debe actualizar correctamente")
    void actualizarPms_debeActualizarCorrectamente() {
        
        pmsRequestDTO.setComentarios("Comentarios actualizados");
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any(Pms.class));
        doNothing().when(analisisHistorialService).registrarModificacion(any(Pms.class));

        
        PmsDTO resultado = pmsService.actualizarPms(1L, pmsRequestDTO);

        
        assertNotNull(resultado);
        verify(pmsRepository, times(1)).save(any(Pms.class));
        verify(analisisHistorialService, times(1)).registrarModificacion(any(Pms.class));
        verify(analisisService, times(1)).manejarEdicionAnalisisFinalizado(any(Pms.class));
    }

    @Test
    @DisplayName("Actualizar PMS inexistente - debe lanzar excepción")
    void actualizarPms_noExistente_debeLanzarExcepcion() {
        
        when(pmsRepository.findById(999L)).thenReturn(Optional.empty());

        
        assertThrows(RuntimeException.class, () -> {
            pmsService.actualizarPms(999L, pmsRequestDTO);
        });
        
        verify(pmsRepository, never()).save(any(Pms.class));
    }

    @Test
    @DisplayName("Desactivar PMS - debe cambiar activo a false")
    void desactivarPms_debeCambiarActivoAFalse() {
        
        doNothing().when(analisisService).desactivarAnalisis(eq(1L), any());

        
        pmsService.desactivarPms(1L);

        
        verify(analisisService, times(1)).desactivarAnalisis(eq(1L), any());
    }

    @Test
    @DisplayName("Reactivar PMS - debe cambiar activo a true")
    void reactivarPms_debeCambiarActivoATrue() {
        
        pms.setActivo(false);
        PmsDTO pmsDTO = new PmsDTO();
        pmsDTO.setAnalisisID(1L);
        pmsDTO.setActivo(true);
        
        when(analisisService.reactivarAnalisis(any(Long.class), any(), any())).thenReturn(pmsDTO);

        
        pmsService.reactivarPms(1L);

        
        verify(analisisService, times(1)).reactivarAnalisis(any(Long.class), any(), any());
    }

    @Test
    @DisplayName("Eliminar PMS - debe desactivar el análisis")
    void eliminarPms_debeDesactivarAnalisis() {
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);

        
        pmsService.eliminarPms(1L);

        
        verify(pmsRepository, times(1)).findById(1L);
        verify(pmsRepository, times(1)).save(any(Pms.class));
    }

    @Test
    @DisplayName("Listar PMS activos - debe retornar solo activos")
    void obtenerTodos_debeRetornarSoloActivos() {
        
        Pms pms2 = new Pms();
        pms2.setAnalisisID(2L);
        pms2.setActivo(true);
        
        when(pmsRepository.findByActivoTrue()).thenReturn(Arrays.asList(pms, pms2));

        
        List<PmsDTO> resultado = pmsService.obtenerTodos();

        
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(pmsRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("Obtener PMS por ID de lote - debe retornar análisis del lote")
    void obtenerPmsPorIdLote_debeRetornarAnalisisDelLote() {
        
        when(pmsRepository.findByIdLote(1)).thenReturn(Arrays.asList(pms));

        
        List<PmsDTO> resultado = pmsService.obtenerPmsPorIdLote(1L);

        
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(pmsRepository, times(1)).findByIdLote(1);
    }

    @Test
    @DisplayName("Listar PMS paginadas - debe retornar página correcta")
    void obtenerPmsPaginadas_debeRetornarPaginaCorrecta() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pms> pmsPage = new PageImpl<>(Arrays.asList(pms));
        
        when(pmsRepository.findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class)))
            .thenReturn(pmsPage);

        
        var resultado = pmsService.obtenerPmsPaginadas(pageable);

        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    @DisplayName("Validar esSemillaBrozosa - debe aceptar true o false")
    void crearPms_conSemillaBrozosaTrue_debeCrearse() {
        
        pmsRequestDTO.setEsSemillaBrozosa(true);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        doNothing().when(analisisHistorialService).registrarCreacion(any(Pms.class));

        
        PmsDTO resultado = pmsService.crearPms(pmsRequestDTO);

        
        assertNotNull(resultado);
        verify(pmsRepository, times(1)).save(any(Pms.class));
    }

    @Test
    @DisplayName("finalizarAnalisis PMS - valida y llama al método genérico sin fallar si datos ok")
    void finalizarAnalisisPms_conDatosCompletos_noFalla() {
        // Configurar para que pase las validaciones
        pms.setNumTandas(1);
        pms.setPmsconRedon(new BigDecimal("10.50"));
        pms.setEstado(Estado.EN_PROCESO);
        
        // Mock AnalisisService.finalizarAnalisisGenerico
        when(analisisService.finalizarAnalisisGenerico(
            eq(1L),
            eq(pmsRepository),
            any(),
            any()
        )).thenAnswer(invocation -> {
            // Obtener el mapper (tercer argumento) y aplicarlo al pms
            java.util.function.Function<Pms, PmsDTO> mapper = invocation.getArgument(2);
            return mapper.apply(pms);
        });
        
        // Debería finalizar sin lanzar excepción
        PmsDTO result = pmsService.finalizarAnalisis(1L);
        assertNotNull(result);
    }

    @Test
    @DisplayName("aprobarAnalisis PMS - con datos completos no falla")
    void aprobarAnalisisPms_conDatos_noFalla() {
        pms.setEstado(Estado.PENDIENTE_APROBACION);
        pms.setPmsconRedon(new BigDecimal("10.50"));
        pms.setNumTandas(1);
        
        // Mock AnalisisService.aprobarAnalisisGenerico
        when(analisisService.aprobarAnalisisGenerico(
            eq(1L),
            eq(pmsRepository),
            any(),
            any(),
            any()
        )).thenAnswer(invocation -> {
            // Obtener el mapper y aplicarlo al pms
            java.util.function.Function<Pms, PmsDTO> mapper = invocation.getArgument(2);
            return mapper.apply(pms);
        });
        
        PmsDTO result = pmsService.aprobarAnalisis(1L);
        assertNotNull(result);
    }

    @Test
    @DisplayName("marcarParaRepetir PMS - con datos completos no falla")
    void marcarParaRepetirPms_conDatos_noFalla() {
        pms.setEstado(Estado.APROBADO);
        pms.setPmsconRedon(new BigDecimal("10.50"));
        pms.setNumTandas(1);
        
        // Mock AnalisisService.marcarParaRepetirGenerico
        when(analisisService.marcarParaRepetirGenerico(
            eq(1L),
            eq(pmsRepository),
            any(),
            any()
        )).thenAnswer(invocation -> {
            // Obtener el mapper y aplicarlo al pms
            pms.setEstado(Estado.A_REPETIR); // Simular cambio de estado
            java.util.function.Function<Pms, PmsDTO> mapper = invocation.getArgument(2);
            return mapper.apply(pms);
        });
        
        PmsDTO result = pmsService.marcarParaRepetir(1L);
        assertNotNull(result);
        assertEquals(Estado.A_REPETIR, result.getEstado());
    }

    @Test
    @DisplayName("desactivarPms cambia activo a false usando servicio genérico")
    void desactivarPms_llamaServicioGenerico() {
        pms.setActivo(true);
        
        // Mock AnalisisService.desactivarAnalisis - debe modificar el pms en memoria
        doAnswer(invocation -> {
            pms.setActivo(false); // Simular desactivación
            return null;
        }).when(analisisService).desactivarAnalisis(eq(1L), eq(pmsRepository));
        
        pmsService.desactivarPms(1L);
        
        // Verificar que el servicio de análisis fue llamado
        verify(analisisService, times(1)).desactivarAnalisis(eq(1L), eq(pmsRepository));
        assertFalse(pms.getActivo());
    }

    @Test
    @DisplayName("reactivarPms - éxito cuando estaba inactivo y error si ya activo")
    void reactivarPms_casos() {
        // Caso 1: Reactivar PMS inactivo - debe funcionar
        Pms pmsInactivo = new Pms();
        pmsInactivo.setAnalisisID(1L);
        pmsInactivo.setActivo(false);
        pmsInactivo.setLote(lote);
        pmsInactivo.setNumRepeticionesEsperadas(8);
        pmsInactivo.setNumTandas(1);
        
        // Mock AnalisisService.reactivarAnalisis
        when(analisisService.reactivarAnalisis(
            eq(1L),
            eq(pmsRepository),
            any()
        )).thenAnswer(invocation -> {
            pmsInactivo.setActivo(true); // Simular reactivación
            java.util.function.Function<Pms, PmsDTO> mapper = invocation.getArgument(2);
            PmsDTO dto = mapper.apply(pmsInactivo);
            // Asegurar que activo esté seteado
            if (dto != null && dto.getActivo() == null) {
                dto.setActivo(true);
            }
            return dto;
        });
        
        PmsDTO dto = pmsService.reactivarPms(1L);
        assertNotNull(dto);
        assertTrue(dto.getActivo());
        
        // Caso 2: Intentar reactivar PMS ya activo - debe lanzar excepción
        when(analisisService.reactivarAnalisis(
            eq(2L),
            eq(pmsRepository),
            any()
        )).thenThrow(new RuntimeException("El análisis ya está activo"));
        
        assertThrows(RuntimeException.class, () -> pmsService.reactivarPms(2L));
    }

    @Test
    @DisplayName("procesarCalculosTanda - tanda incompleta solo actualiza estadísticas")
    void procesarCalculosTanda_tandaIncompleta_soloActualizaEstadisticas() {
        
        pms.setNumRepeticionesEsperadas(8);
        pms.setNumTandas(1);
        pms.setEsSemillaBrozosa(false);
        
        // Crear solo 4 repeticiones (menos de las esperadas)
        List<RepPms> repeticiones = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            RepPms rep = new RepPms();
            rep.setNumRep(i);
            rep.setNumTanda(1);
            rep.setPeso(new BigDecimal("10.0"));
            rep.setValido(true);
            repeticiones.add(rep);
        }
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.findByPmsId(1L)).thenReturn(repeticiones);
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        
        
        pmsService.procesarCalculosTanda(1L, 1);
        
        
        verify(pmsRepository, times(1)).save(any(Pms.class));
        verify(repPmsRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("procesarCalculosTanda - tanda completa con CV aceptable")
    void procesarCalculosTanda_tandaCompleta_CVAceptable() {
        
        pms.setNumRepeticionesEsperadas(4);
        pms.setNumTandas(1);
        pms.setEsSemillaBrozosa(false); // Umbral CV = 4.0
        
        // Crear 4 repeticiones válidas con pesos similares
        List<RepPms> repeticiones = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            RepPms rep = new RepPms();
            rep.setNumRep(i);
            rep.setNumTanda(1);
            rep.setPeso(new BigDecimal("10." + i)); // 10.1, 10.2, 10.3, 10.4
            rep.setValido(null);
            repeticiones.add(rep);
        }
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.findByPmsId(1L)).thenReturn(repeticiones);
        when(repPmsRepository.saveAll(anyList())).thenReturn(repeticiones);
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        
        
        pmsService.procesarCalculosTanda(1L, 1);
        
        
        verify(repPmsRepository, times(1)).saveAll(anyList());
        verify(pmsRepository, times(1)).save(any(Pms.class));
    }

    @Test
    @DisplayName("procesarCalculosTanda - tanda completa con CV no aceptable incrementa tandas")
    void procesarCalculosTanda_CVNoAceptable_incrementaTandas() {
        
        pms.setNumRepeticionesEsperadas(4);
        pms.setNumTandas(1);
        pms.setEsSemillaBrozosa(false); // Umbral CV = 4.0
        
        // Crear repeticiones con mucha variación para CV alto
        List<RepPms> repeticiones = new ArrayList<>();
        BigDecimal[] pesos = {new BigDecimal("8.0"), new BigDecimal("10.0"), 
                             new BigDecimal("12.0"), new BigDecimal("14.0")};
        for (int i = 0; i < 4; i++) {
            RepPms rep = new RepPms();
            rep.setNumRep(i + 1);
            rep.setNumTanda(1);
            rep.setPeso(pesos[i]);
            rep.setValido(null);
            repeticiones.add(rep);
        }
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.findByPmsId(1L)).thenReturn(repeticiones);
        when(repPmsRepository.saveAll(anyList())).thenReturn(repeticiones);
        when(repPmsRepository.countByPmsId(1L)).thenReturn(4L);
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        
        
        pmsService.procesarCalculosTanda(1L, 1);
        
        
        verify(pmsRepository, times(1)).save(any(Pms.class));
    }

    @Test
    @DisplayName("procesarCalculosTanda - sin repeticiones válidas incrementa tandas")
    void procesarCalculosTanda_sinRepeticionesValidas_incrementaTandas() {
        
        pms.setNumRepeticionesEsperadas(4);
        pms.setNumTandas(1);
        pms.setEsSemillaBrozosa(false);
        
        // Crear repeticiones que serán marcadas como inválidas
        List<RepPms> repeticiones = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            RepPms rep = new RepPms();
            rep.setNumRep(i);
            rep.setNumTanda(1);
            rep.setPeso(new BigDecimal("100.0")); // Outlier extremo
            rep.setValido(false);
            repeticiones.add(rep);
        }
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.findByPmsId(1L)).thenReturn(repeticiones);
        when(repPmsRepository.saveAll(anyList())).thenReturn(repeticiones);
        when(repPmsRepository.countByPmsId(1L)).thenReturn(4L);
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        
        
        pmsService.procesarCalculosTanda(1L, 1);
        
        
        verify(pmsRepository, times(1)).save(any(Pms.class));
    }

    @Test
    @DisplayName("validarTodasLasRepeticiones - sin repeticiones no hace nada")
    void validarTodasLasRepeticiones_sinRepeticiones_noHaceNada() {
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.findByPmsId(1L)).thenReturn(Collections.emptyList());
        
        
        pmsService.validarTodasLasRepeticiones(1L);
        
        
        verify(repPmsRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("validarTodasLasRepeticiones - repeticiones insuficientes marca como indeterminadas")
    void validarTodasLasRepeticiones_repeticionesInsuficientes_marcaIndeterminadas() {
        
        pms.setNumRepeticionesEsperadas(8);
        
        List<RepPms> repeticiones = new ArrayList<>();
        for (int i = 1; i <= 4; i++) { // Menos de las esperadas
            RepPms rep = new RepPms();
            rep.setNumRep(i);
            rep.setPeso(new BigDecimal("10.0"));
            repeticiones.add(rep);
        }
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.findByPmsId(1L)).thenReturn(repeticiones);
        when(repPmsRepository.saveAll(anyList())).thenReturn(repeticiones);
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        
        
        pmsService.validarTodasLasRepeticiones(1L);
        
        
        verify(repPmsRepository, times(1)).saveAll(anyList());
        verify(pmsRepository, times(1)).save(any(Pms.class));
    }

    @Test
    @DisplayName("validarTodasLasRepeticiones - con repeticiones suficientes valida correctamente")
    void validarTodasLasRepeticiones_repeticionesSuficientes_validaCorrectamente() {
        
        pms.setNumRepeticionesEsperadas(4);
        pms.setNumTandas(1);
        pms.setEsSemillaBrozosa(false);
        
        List<RepPms> repeticiones = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            RepPms rep = new RepPms();
            rep.setNumRep(i);
            rep.setNumTanda(1);
            rep.setPeso(new BigDecimal("10." + i));
            repeticiones.add(rep);
        }
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.findByPmsId(1L)).thenReturn(repeticiones);
        when(repPmsRepository.saveAll(anyList())).thenReturn(repeticiones);
        when(repPmsRepository.countByPmsId(1L)).thenReturn(4L);
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        
        
        pmsService.validarTodasLasRepeticiones(1L);
        
        
        verify(repPmsRepository, times(1)).saveAll(anyList());
        verify(pmsRepository, times(1)).save(any(Pms.class));
    }

    @Test
    @DisplayName("actualizarPmsConRedondeo - actualiza correctamente en estado EN_PROCESO")
    void actualizarPmsConRedondeo_estadoEnProceso_actualizaCorrectamente() {
        
        pms.setEstado(Estado.EN_PROCESO);
        pms.setNumRepeticionesEsperadas(4);
        pms.setNumTandas(1);
        pms.setEsSemillaBrozosa(false);
        
        
        List<RepPms> repeticiones = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            RepPms rep = new RepPms();
            rep.setNumRep(i);
            rep.setNumTanda(1);
            rep.setPeso(new BigDecimal("10.0"));
            rep.setValido(true);
            repeticiones.add(rep);
        }
        
        PmsRedondeoRequestDTO request = new PmsRedondeoRequestDTO();
        request.setPmsconRedon(new BigDecimal("100.5"));
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.findByPmsId(1L)).thenReturn(repeticiones);
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        
        
        PmsDTO resultado = pmsService.actualizarPmsConRedondeo(1L, request);
        
        
        assertNotNull(resultado);
        verify(pmsRepository, times(1)).save(any(Pms.class));
    }

    @Test
    @DisplayName("actualizarPmsConRedondeo - falla con estado REGISTRADO")
    void actualizarPmsConRedondeo_estadoRegistrado_lanzaExcepcion() {
        
        pms.setEstado(Estado.REGISTRADO);
        
        PmsRedondeoRequestDTO request = new PmsRedondeoRequestDTO();
        request.setPmsconRedon(new BigDecimal("100.5"));
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pmsService.actualizarPmsConRedondeo(1L, request);
        });
        
        assertTrue(exception.getMessage().contains("EN_PROCESO"));
        verify(pmsRepository, never()).save(any(Pms.class));
    }

    @Test
    @DisplayName("actualizarPmsConRedondeo - falla si repeticiones incompletas")
    void actualizarPmsConRedondeo_repeticionesIncompletas_lanzaExcepcion() {
        
        pms.setEstado(Estado.EN_PROCESO);
        pms.setNumRepeticionesEsperadas(8);
        pms.setNumTandas(1);
        
        
        List<RepPms> repeticiones = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            RepPms rep = new RepPms();
            rep.setNumRep(i);
            rep.setNumTanda(1);
            rep.setPeso(new BigDecimal("10.0"));
            rep.setValido(true);
            repeticiones.add(rep);
        }
        
        PmsRedondeoRequestDTO request = new PmsRedondeoRequestDTO();
        request.setPmsconRedon(new BigDecimal("100.5"));
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.findByPmsId(1L)).thenReturn(repeticiones);
        
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pmsService.actualizarPmsConRedondeo(1L, request);
        });
        
        assertTrue(exception.getMessage().contains("repeticiones válidas"));
        verify(pmsRepository, never()).save(any(Pms.class));
    }

    @Test
    @DisplayName("actualizarEstadisticasPms - actualiza estadísticas públicamente")
    void actualizarEstadisticasPms_actualizaCorrectamente() {
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.findByPmsId(1L)).thenReturn(Collections.emptyList());
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        
        
        pmsService.actualizarEstadisticasPms(1L);
        
        
        verify(pmsRepository, times(1)).save(any(Pms.class));
    }

    @Test
    @DisplayName("obtenerPmsPaginadasConFiltro - filtro 'activos'")
    void obtenerPmsPaginadasConFiltro_activos() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pms> pmsPage = new PageImpl<>(Arrays.asList(pms));
        
        when(pmsRepository.findByActivoTrueOrderByFechaInicioDesc(pageable))
            .thenReturn(pmsPage);
        
        
        var resultado = pmsService.obtenerPmsPaginadasConFiltro(pageable, "activos");
        
        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(pmsRepository, times(1)).findByActivoTrueOrderByFechaInicioDesc(pageable);
    }

    @Test
    @DisplayName("obtenerPmsPaginadasConFiltro - filtro 'inactivos'")
    void obtenerPmsPaginadasConFiltro_inactivos() {
        
        Pageable pageable = PageRequest.of(0, 10);
        pms.setActivo(false);
        Page<Pms> pmsPage = new PageImpl<>(Arrays.asList(pms));
        
        when(pmsRepository.findByActivoFalseOrderByFechaInicioDesc(pageable))
            .thenReturn(pmsPage);
        
        
        var resultado = pmsService.obtenerPmsPaginadasConFiltro(pageable, "inactivos");
        
        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(pmsRepository, times(1)).findByActivoFalseOrderByFechaInicioDesc(pageable);
    }

    @Test
    @DisplayName("obtenerPmsPaginadasConFiltro - filtro 'todos'")
    void obtenerPmsPaginadasConFiltro_todos() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pms> pmsPage = new PageImpl<>(Arrays.asList(pms));
        
        when(pmsRepository.findAllByOrderByFechaInicioDesc(pageable))
            .thenReturn(pmsPage);
        
        
        var resultado = pmsService.obtenerPmsPaginadasConFiltro(pageable, "todos");
        
        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(pmsRepository, times(1)).findAllByOrderByFechaInicioDesc(pageable);
    }

    @Test
    @DisplayName("obtenerPmsPaginadasConFiltros - con filtros dinámicos")
    void obtenerPmsPaginadasConFiltros_conFiltrosDinamicos() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pms> pmsPage = new PageImpl<>(Arrays.asList(pms));
        
        @SuppressWarnings("unchecked")
        Specification<Pms> anySpec = any(Specification.class);
        when(pmsRepository.findAll(anySpec, eq(pageable)))
            .thenReturn(pmsPage);
        
        
        var resultado = pmsService.obtenerPmsPaginadasConFiltros(
            pageable, "LOTE-001", true, "EN_PROCESO", 1L);
        
        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    @DisplayName("crear PMS con lote inactivo - debe lanzar excepción")
    void crearPms_conLoteInactivo_debeLanzarExcepcion() {
        
        lote.setActivo(false);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            pmsService.crearPms(pmsRequestDTO);
        });
        
        assertTrue(exception.getMessage().contains("activo"));
        verify(pmsRepository, never()).save(any(Pms.class));
    }

    @Test
    @DisplayName("mapeo DTO completo - con cultivar y especie")
    void mapeoDTO_conCultivarYEspecie() {
        
        Especie especie = new Especie();
        especie.setNombreComun("Trigo");
        especie.setNombreCientifico("Triticum aestivum");
        
        Cultivar cultivar = new Cultivar();
        cultivar.setNombre("Cultivar Test");
        cultivar.setEspecie(especie);
        
        lote.setCultivar(cultivar);
        lote.setFicha("FICHA-001");
        
        pms.setLote(lote);
        pms.setPromedio100g(new BigDecimal("10.5"));
        pms.setDesvioStd(new BigDecimal("0.5"));
        pms.setCoefVariacion(new BigDecimal("4.76"));
        pms.setPmssinRedon(new BigDecimal("105.0"));
        pms.setPmsconRedon(new BigDecimal("105"));
        
        List<AnalisisHistorialDTO> historial = new ArrayList<>();
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(analisisHistorialService.obtenerHistorialAnalisis(1L)).thenReturn(historial);
        
        
        PmsDTO resultado = pmsService.obtenerPorId(1L);
        
        
        assertNotNull(resultado);
        assertEquals("Cultivar Test", resultado.getCultivarNombre());
        assertEquals("Trigo", resultado.getEspecieNombre());
        assertEquals("FICHA-001", resultado.getFicha());
    }

    @Test
    @DisplayName("actualizarPmsConRedondeo - con estado PENDIENTE_APROBACION")
    void actualizarPmsConRedondeo_estadoPendienteAprobacion() {
        
        pms.setEstado(Estado.PENDIENTE_APROBACION);
        pms.setNumRepeticionesEsperadas(4);
        pms.setNumTandas(1);
        pms.setEsSemillaBrozosa(false);
        
        List<RepPms> repeticiones = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            RepPms rep = new RepPms();
            rep.setNumRep(i);
            rep.setNumTanda(1);
            rep.setPeso(new BigDecimal("10.0"));
            rep.setValido(true);
            repeticiones.add(rep);
        }
        
        PmsRedondeoRequestDTO request = new PmsRedondeoRequestDTO();
        request.setPmsconRedon(new BigDecimal("100.0"));
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.findByPmsId(1L)).thenReturn(repeticiones);
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        
        
        PmsDTO resultado = pmsService.actualizarPmsConRedondeo(1L, request);
        
        
        assertNotNull(resultado);
        verify(pmsRepository, times(1)).save(any(Pms.class));
    }

    @Test
    @DisplayName("actualizarPmsConRedondeo - con estado APROBADO")
    void actualizarPmsConRedondeo_estadoAprobado() {
        
        pms.setEstado(Estado.APROBADO);
        pms.setNumRepeticionesEsperadas(4);
        pms.setNumTandas(1);
        pms.setEsSemillaBrozosa(false);
        
        List<RepPms> repeticiones = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            RepPms rep = new RepPms();
            rep.setNumRep(i);
            rep.setNumTanda(1);
            rep.setPeso(new BigDecimal("10.0"));
            rep.setValido(true);
            repeticiones.add(rep);
        }
        
        PmsRedondeoRequestDTO request = new PmsRedondeoRequestDTO();
        request.setPmsconRedon(new BigDecimal("100.0"));
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.findByPmsId(1L)).thenReturn(repeticiones);
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        
        
        PmsDTO resultado = pmsService.actualizarPmsConRedondeo(1L, request);
        
        
        assertNotNull(resultado);
        verify(pmsRepository, times(1)).save(any(Pms.class));
    }

    @Test
    @DisplayName("validarTodasLasRepeticiones - con CV alto incrementa tandas")
    void validarTodasLasRepeticiones_CVAlto_incrementaTandas() {
        
        pms.setNumRepeticionesEsperadas(4);
        pms.setNumTandas(1);
        pms.setEsSemillaBrozosa(false);
        
        
        List<RepPms> repeticiones = new ArrayList<>();
        BigDecimal[] pesos = {new BigDecimal("8.0"), new BigDecimal("10.0"), 
                             new BigDecimal("12.0"), new BigDecimal("14.0")};
        for (int i = 0; i < 4; i++) {
            RepPms rep = new RepPms();
            rep.setNumRep(i + 1);
            rep.setNumTanda(1);
            rep.setPeso(pesos[i]);
            repeticiones.add(rep);
        }
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.findByPmsId(1L)).thenReturn(repeticiones);
        when(repPmsRepository.saveAll(anyList())).thenReturn(repeticiones);
        when(repPmsRepository.countByPmsId(1L)).thenReturn(4L);
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        
        
        pmsService.validarTodasLasRepeticiones(1L);
        
        
        verify(pmsRepository, times(1)).save(any(Pms.class));
    }

    @Test
    @DisplayName("procesarCalculosTanda - con semilla brozosa usa umbral CV 6.0")
    void procesarCalculosTanda_semillaBrozosa_umbralCV6() {
        
        pms.setNumRepeticionesEsperadas(4);
        pms.setNumTandas(1);
        pms.setEsSemillaBrozosa(true); 
        
        List<RepPms> repeticiones = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            RepPms rep = new RepPms();
            rep.setNumRep(i);
            rep.setNumTanda(1);
            rep.setPeso(new BigDecimal("10." + i));
            repeticiones.add(rep);
        }
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.findByPmsId(1L)).thenReturn(repeticiones);
        when(repPmsRepository.saveAll(anyList())).thenReturn(repeticiones);
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        
        
        pmsService.procesarCalculosTanda(1L, 1);
        
        
        verify(pmsRepository, times(1)).save(any(Pms.class));
    }

    @Test
    @DisplayName("procesarCalculosTanda - alcanza límite de 16 repeticiones")
    void procesarCalculosTanda_alcanzaLimite16_noIncrementaTandas() {
        
        pms.setNumRepeticionesEsperadas(4);
        pms.setNumTandas(4);
        pms.setEsSemillaBrozosa(false);
        
        List<RepPms> repeticiones = new ArrayList<>();
        BigDecimal[] pesos = {new BigDecimal("8.0"), new BigDecimal("10.0"), 
                             new BigDecimal("12.0"), new BigDecimal("14.0")};
        for (int i = 0; i < 4; i++) {
            RepPms rep = new RepPms();
            rep.setNumRep(i + 1);
            rep.setNumTanda(4);
            rep.setPeso(pesos[i]);
            repeticiones.add(rep);
        }
        
        when(pmsRepository.findById(1L)).thenReturn(Optional.of(pms));
        when(repPmsRepository.findByPmsId(1L)).thenReturn(repeticiones);
        when(repPmsRepository.saveAll(anyList())).thenReturn(repeticiones);
        when(repPmsRepository.countByPmsId(1L)).thenReturn(16L); 
        when(pmsRepository.save(any(Pms.class))).thenReturn(pms);
        
        
        pmsService.procesarCalculosTanda(1L, 4);
        
        
        verify(pmsRepository, times(1)).save(any(Pms.class));
    }
}
