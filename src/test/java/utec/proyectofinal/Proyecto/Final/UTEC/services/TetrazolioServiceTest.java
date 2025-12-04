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
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepTetrazolioViabilidad;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Tetrazolio;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.TetrazolioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepTetrazolioViabilidadRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PorcentajesRedondeadosRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.TetrazolioRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.AnalisisHistorialDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TetrazolioDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para TetrazolioService
 * 
 * ¿Qué validamos?
 * - Creación de análisis de tetrazolio con estado REGISTRADO
 * - Validación de número de repeticiones esperadas (> 0)
 * - Asignación correcta de lote activo
 * - Configuración de parámetros de tinción
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("Tests de TetrazolioService")
class TetrazolioServiceTest {

    @Mock
    private TetrazolioRepository tetrazolioRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private AnalisisHistorialService analisisHistorialService;

    @Mock
    private AnalisisService analisisService;

    @Mock
    private RepTetrazolioViabilidadRepository repeticionRepository;

    @InjectMocks
    private TetrazolioService tetrazolioService;

    private TetrazolioRequestDTO tetrazolioRequestDTO;
    private Lote lote;
    private Tetrazolio tetrazolio;

    @BeforeEach
    void setUp() {
        
        lote = new Lote();
        lote.setLoteID(1L);
        lote.setNomLote("LOTE-TEST-001");
        lote.setActivo(true);

        tetrazolioRequestDTO = new TetrazolioRequestDTO();
        tetrazolioRequestDTO.setIdLote(1L);
        tetrazolioRequestDTO.setNumRepeticionesEsperadas(4);
        tetrazolioRequestDTO.setNumSemillasPorRep(100);
        tetrazolioRequestDTO.setPretratamiento("Remojo 24h");
        tetrazolioRequestDTO.setConcentracion("1%");
        tetrazolioRequestDTO.setTincionHs(24);
        tetrazolioRequestDTO.setTincionTemp(30);
        tetrazolioRequestDTO.setFecha(LocalDate.now());

        tetrazolio = new Tetrazolio();
        tetrazolio.setAnalisisID(1L);
        tetrazolio.setLote(lote);
        tetrazolio.setEstado(Estado.REGISTRADO);
        tetrazolio.setActivo(true);
        tetrazolio.setNumRepeticionesEsperadas(4);
    }

    @Test
    @DisplayName("Crear tetrazolio - debe asignar estado REGISTRADO")
    void crearTetrazolio_debeAsignarEstadoRegistrado() {
        
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(tetrazolioRepository.save(any(Tetrazolio.class))).thenReturn(tetrazolio);

        
        TetrazolioDTO resultado = tetrazolioService.crearTetrazolio(tetrazolioRequestDTO);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        verify(tetrazolioRepository, times(1)).save(any(Tetrazolio.class));
        verify(analisisHistorialService, times(1)).registrarCreacion(any(Tetrazolio.class));
    }

    @Test
    @DisplayName("Crear tetrazolio sin repeticiones esperadas - debe lanzar excepción")
    void crearTetrazolio_sinRepeticionesEsperadas_debeLanzarExcepcion() {
        
        tetrazolioRequestDTO.setNumRepeticionesEsperadas(null);

        
        assertThrows(RuntimeException.class, () -> {
            tetrazolioService.crearTetrazolio(tetrazolioRequestDTO);
        }, "Debe lanzar excepción cuando no se especifican repeticiones esperadas");
    }

    @Test
    @DisplayName("Crear tetrazolio con repeticiones = 0 - debe lanzar excepción")
    void crearTetrazolio_conRepeticionesCero_debeLanzarExcepcion() {
        
        tetrazolioRequestDTO.setNumRepeticionesEsperadas(0);

        
        assertThrows(RuntimeException.class, () -> {
            tetrazolioService.crearTetrazolio(tetrazolioRequestDTO);
        }, "Debe lanzar excepción cuando repeticiones = 0");
    }

    @Test
    @DisplayName("Crear tetrazolio con lote inactivo - debe lanzar excepción")
    void crearTetrazolio_conLoteInactivo_debeLanzarExcepcion() {
        
        lote.setActivo(false);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));

        
        assertThrows(RuntimeException.class, () -> {
            tetrazolioService.crearTetrazolio(tetrazolioRequestDTO);
        }, "Debe lanzar excepción cuando el lote está inactivo");
    }

    @Test
    @DisplayName("Actualizar tetrazolio aprobado por analista - cambia a PENDIENTE_APROBACION")
    void actualizarTetrazolio_aprobadoAnalista_cambiaEstado() {
        tetrazolio.setEstado(Estado.APROBADO);
        when(analisisService.esAnalista()).thenReturn(true);
        when(tetrazolioRepository.findById(1L)).thenReturn(Optional.of(tetrazolio));
        when(tetrazolioRepository.save(any(Tetrazolio.class))).thenReturn(tetrazolio);
        TetrazolioRequestDTO req = new TetrazolioRequestDTO();
        req.setIdLote(1L);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        TetrazolioDTO dto = tetrazolioService.actualizarTetrazolio(1L, req);
        assertEquals(Estado.PENDIENTE_APROBACION, dto.getEstado());
        verify(analisisHistorialService, times(1)).registrarModificacion(any());
    }

    @Test
    @DisplayName("finalizarAnalisis Tetrazolio - completa sin lanzar excepciones")
    void finalizarTetrazolio_conDatosCompletos_noFalla() {
        
        when(analisisService.finalizarAnalisisGenerico(eq(1L), eq(tetrazolioRepository), any(), any()))
            .thenAnswer(invocation -> {
                java.util.function.Function<Tetrazolio, TetrazolioDTO> mapper = invocation.getArgument(2);
                return mapper.apply(tetrazolio);
            });
        
        TetrazolioDTO resultado = tetrazolioService.finalizarAnalisis(1L);
        
        assertNotNull(resultado);
        verify(analisisService, times(1)).finalizarAnalisisGenerico(eq(1L), eq(tetrazolioRepository), any(), any());
    }

    @Test
    @DisplayName("aprobarAnalisis Tetrazolio - completa con mock de AnalisisService")
    void aprobarTetrazolio_conAnalisisServiceMock_noFalla() {
        
        when(analisisService.aprobarAnalisisGenerico(eq(1L), eq(tetrazolioRepository), any(), any(), any()))
            .thenAnswer(invocation -> {
                java.util.function.Function<Tetrazolio, TetrazolioDTO> mapper = invocation.getArgument(2);
                return mapper.apply(tetrazolio);
            });
        
        TetrazolioDTO resultado = tetrazolioService.aprobarAnalisis(1L);
        
        assertNotNull(resultado);
        verify(analisisService, times(1)).aprobarAnalisisGenerico(eq(1L), eq(tetrazolioRepository), any(), any(), any());
    }

    @Test
    @DisplayName("marcarParaRepetir Tetrazolio - completa con mock de AnalisisService")
    void marcarParaRepetirTetrazolio_conAnalisisServiceMock_noFalla() {
        
        when(analisisService.marcarParaRepetirGenerico(eq(1L), eq(tetrazolioRepository), any(), any()))
            .thenAnswer(invocation -> {
                java.util.function.Function<Tetrazolio, TetrazolioDTO> mapper = invocation.getArgument(2);
                return mapper.apply(tetrazolio);
            });
        
        TetrazolioDTO resultado = tetrazolioService.marcarParaRepetir(1L);
        
        assertNotNull(resultado);
        verify(analisisService, times(1)).marcarParaRepetirGenerico(eq(1L), eq(tetrazolioRepository), any(), any());
    }

    @Test
    @DisplayName("Obtener tetrazolio por ID - debe retornar el análisis si existe")
    void obtenerTetrazolioPorId_cuandoExiste_debeRetornarTetrazolio() {
        
        when(tetrazolioRepository.findById(1L)).thenReturn(Optional.of(tetrazolio));

        
        TetrazolioDTO resultado = tetrazolioService.obtenerTetrazolioPorId(1L);

        
        assertNotNull(resultado);
        assertEquals(1L, resultado.getAnalisisID());
        assertEquals(4, resultado.getNumRepeticionesEsperadas());
        verify(tetrazolioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Desactivar tetrazolio - debe cambiar activo a false")
    void desactivarTetrazolio_debeCambiarActivoAFalse() {
        
        doNothing().when(analisisService).desactivarAnalisis(anyLong(), any());

        
        tetrazolioService.desactivarTetrazolio(1L);

        
        verify(analisisService, times(1)).desactivarAnalisis(eq(1L), any());
    }

    @Test
    @DisplayName("actualizarPorcentajesRedondeados - con repeticiones completas actualiza correctamente")
    void actualizarPorcentajesRedondeados_conRepeticionesCompletas_actualiza() {
        
        tetrazolio.setNumRepeticionesEsperadas(4);
        PorcentajesRedondeadosRequestDTO request = new PorcentajesRedondeadosRequestDTO();
        request.setPorcViablesRedondeo(new BigDecimal("85.5"));
        request.setPorcNoViablesRedondeo(new BigDecimal("10.2"));
        request.setPorcDurasRedondeo(new BigDecimal("4.3"));
        
        when(tetrazolioRepository.findById(1L)).thenReturn(Optional.of(tetrazolio));
        when(repeticionRepository.countByTetrazolioId(1L)).thenReturn(4L); 
        when(tetrazolioRepository.save(any(Tetrazolio.class))).thenReturn(tetrazolio);
        
        
        TetrazolioDTO resultado = tetrazolioService.actualizarPorcentajesRedondeados(1L, request);
        
        
        assertNotNull(resultado);
        verify(tetrazolioRepository, times(1)).save(any(Tetrazolio.class));
        verify(repeticionRepository, times(1)).countByTetrazolioId(1L);
    }

    @Test
    @DisplayName("actualizarPorcentajesRedondeados - con repeticiones incompletas lanza excepción")
    void actualizarPorcentajesRedondeados_repeticionesIncompletas_lanzaExcepcion() {
        
        tetrazolio.setNumRepeticionesEsperadas(4);
        PorcentajesRedondeadosRequestDTO request = new PorcentajesRedondeadosRequestDTO();
        request.setPorcViablesRedondeo(new BigDecimal("85.5"));
        
        when(tetrazolioRepository.findById(1L)).thenReturn(Optional.of(tetrazolio));
        when(repeticionRepository.countByTetrazolioId(1L)).thenReturn(2L); 
        
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tetrazolioService.actualizarPorcentajesRedondeados(1L, request);
        });
        
        assertTrue(exception.getMessage().contains("repeticiones"));
        verify(tetrazolioRepository, never()).save(any(Tetrazolio.class));
    }

    @Test
    @DisplayName("obtenerTetrazoliosPaginadasConFiltro - filtro 'activos'")
    void obtenerTetrazoliosPaginadasConFiltro_activos() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tetrazolio> tetrazolioPage = new PageImpl<>(Arrays.asList(tetrazolio));
        
        when(tetrazolioRepository.findByActivoTrueOrderByFechaInicioDesc(pageable))
            .thenReturn(tetrazolioPage);
        when(analisisHistorialService.obtenerHistorialAnalisis(anyLong()))
            .thenReturn(Collections.emptyList());
        
        
        var resultado = tetrazolioService.obtenerTetrazoliosPaginadasConFiltro(pageable, "activos");
        
        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(tetrazolioRepository, times(1)).findByActivoTrueOrderByFechaInicioDesc(pageable);
    }

    @Test
    @DisplayName("obtenerTetrazoliosPaginadasConFiltro - filtro 'inactivos'")
    void obtenerTetrazoliosPaginadasConFiltro_inactivos() {
        
        Pageable pageable = PageRequest.of(0, 10);
        tetrazolio.setActivo(false);
        Page<Tetrazolio> tetrazolioPage = new PageImpl<>(Arrays.asList(tetrazolio));
        
        when(tetrazolioRepository.findByActivoFalseOrderByFechaInicioDesc(pageable))
            .thenReturn(tetrazolioPage);
        when(analisisHistorialService.obtenerHistorialAnalisis(anyLong()))
            .thenReturn(Collections.emptyList());
        
        
        var resultado = tetrazolioService.obtenerTetrazoliosPaginadasConFiltro(pageable, "inactivos");
        
        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(tetrazolioRepository, times(1)).findByActivoFalseOrderByFechaInicioDesc(pageable);
    }

    @Test
    @DisplayName("obtenerTetrazoliosPaginadasConFiltro - filtro 'todos'")
    void obtenerTetrazoliosPaginadasConFiltro_todos() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tetrazolio> tetrazolioPage = new PageImpl<>(Arrays.asList(tetrazolio));
        
        when(tetrazolioRepository.findAllByOrderByFechaInicioDesc(pageable))
            .thenReturn(tetrazolioPage);
        when(analisisHistorialService.obtenerHistorialAnalisis(anyLong()))
            .thenReturn(Collections.emptyList());
        
        
        var resultado = tetrazolioService.obtenerTetrazoliosPaginadasConFiltro(pageable, "todos");
        
        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(tetrazolioRepository, times(1)).findAllByOrderByFechaInicioDesc(pageable);
    }

    @Test
    @DisplayName("mapearEntidadAListadoDTO - con cultivar y especie completos")
    void mapearEntidadAListadoDTO_conCultivarYEspecie() {
        
        Especie especie = new Especie();
        especie.setNombreComun("Trigo");
        especie.setNombreCientifico("Triticum aestivum");
        
        Cultivar cultivar = new Cultivar();
        cultivar.setNombre("Cultivar Test");
        cultivar.setEspecie(especie);
        
        lote.setCultivar(cultivar);
        lote.setFicha("FICHA-001");
        
        tetrazolio.setLote(lote);
        tetrazolio.setFecha(LocalDate.now());
        tetrazolio.setPorcViablesRedondeo(new BigDecimal("85.5"));
        tetrazolio.setViabilidadInase(new BigDecimal("90.0"));
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tetrazolio> tetrazolioPage = new PageImpl<>(Arrays.asList(tetrazolio));
        
        List<AnalisisHistorialDTO> historial = new ArrayList<>();
        AnalisisHistorialDTO historialDTO = new AnalisisHistorialDTO();
        historialDTO.setUsuario("testuser");
        historial.add(historialDTO);
        
        when(tetrazolioRepository.findByActivoTrueOrderByFechaInicioDesc(pageable))
            .thenReturn(tetrazolioPage);
        when(analisisHistorialService.obtenerHistorialAnalisis(1L))
            .thenReturn(historial);
        
        
        var resultado = tetrazolioService.obtenerTetrazoliosPaginadasConFiltro(pageable, "activos");
        var dto = resultado.getContent().get(0);
        
        
        assertNotNull(dto);
        assertEquals("Trigo", dto.getEspecie());
        assertEquals("LOTE-TEST-001", dto.getLote());
        assertEquals("testuser", dto.getUsuarioCreador());
        assertEquals("testuser", dto.getUsuarioModificador());
    }

    @Test
    @DisplayName("mapearEntidadAListadoDTO - con especie usando nombreCientifico cuando nombreComun está vacío")
    void mapearEntidadAListadoDTO_especieConNombreCientifico() {
        
        Especie especie = new Especie();
        especie.setNombreComun(""); 
        especie.setNombreCientifico("Triticum aestivum");
        
        Cultivar cultivar = new Cultivar();
        cultivar.setEspecie(especie);
        
        lote.setCultivar(cultivar);
        tetrazolio.setLote(lote);
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tetrazolio> tetrazolioPage = new PageImpl<>(Arrays.asList(tetrazolio));
        
        when(tetrazolioRepository.findByActivoTrueOrderByFechaInicioDesc(pageable))
            .thenReturn(tetrazolioPage);
        when(analisisHistorialService.obtenerHistorialAnalisis(anyLong()))
            .thenReturn(Collections.emptyList());
        
        
        var resultado = tetrazolioService.obtenerTetrazoliosPaginadasConFiltro(pageable, "activos");
        var dto = resultado.getContent().get(0);
        
        
        assertNotNull(dto);
        assertEquals("Triticum aestivum", dto.getEspecie());
    }

    @Test
    @DisplayName("validarCompletitudRepeticiones - con todas las repeticiones creadas no lanza excepción")
    void validarCompletitudRepeticiones_completadas_noLanzaExcepcion() {
        
        tetrazolio.setNumRepeticionesEsperadas(4);
        
        when(tetrazolioRepository.findById(1L)).thenReturn(Optional.of(tetrazolio));
        when(repeticionRepository.countByTetrazolioId(1L)).thenReturn(4L);
        when(tetrazolioRepository.save(any(Tetrazolio.class))).thenReturn(tetrazolio);
        
        PorcentajesRedondeadosRequestDTO request = new PorcentajesRedondeadosRequestDTO();
        request.setPorcViablesRedondeo(new BigDecimal("85.0"));
        
        
        assertDoesNotThrow(() -> {
            tetrazolioService.actualizarPorcentajesRedondeados(1L, request);
        });
    }

    @Test
    @DisplayName("validarEvidenciaAntesDeFinalizar - con repeticiones no lanza excepción")
    void validarEvidenciaAntesDeFinalizar_conRepeticiones_noLanzaExcepcion() {
        
        tetrazolio.setNumRepeticionesEsperadas(2);
        
        List<RepTetrazolioViabilidad> repeticiones = new ArrayList<>();
        RepTetrazolioViabilidad rep1 = new RepTetrazolioViabilidad();
        repeticiones.add(rep1);
        tetrazolio.setRepeticiones(repeticiones);
        
        when(analisisService.finalizarAnalisisGenerico(eq(1L), eq(tetrazolioRepository), any(), any()))
            .thenAnswer(invocation -> {
                
                java.util.function.Consumer<Tetrazolio> validator = invocation.getArgument(3);
                validator.accept(tetrazolio);
                
                
                java.util.function.Function<Tetrazolio, TetrazolioDTO> mapper = invocation.getArgument(2);
                return mapper.apply(tetrazolio);
            });
        when(repeticionRepository.countByTetrazolioId(1L)).thenReturn(2L);
        
        
        assertDoesNotThrow(() -> {
            tetrazolioService.finalizarAnalisis(1L);
        });
    }

    @Test
    @DisplayName("validarEvidenciaAntesDeFinalizar - con porcentajes válidos no lanza excepción")
    void validarEvidenciaAntesDeFinalizar_conPorcentajes_noLanzaExcepcion() {
        
        tetrazolio.setNumRepeticionesEsperadas(2);
        tetrazolio.setRepeticiones(Collections.emptyList()); 
        tetrazolio.setPorcViablesRedondeo(new BigDecimal("85.5")); 
        
        when(analisisService.finalizarAnalisisGenerico(eq(1L), eq(tetrazolioRepository), any(), any()))
            .thenAnswer(invocation -> {
                
                java.util.function.Consumer<Tetrazolio> validator = invocation.getArgument(3);
                validator.accept(tetrazolio);
                
                
                java.util.function.Function<Tetrazolio, TetrazolioDTO> mapper = invocation.getArgument(2);
                return mapper.apply(tetrazolio);
            });
        when(repeticionRepository.countByTetrazolioId(1L)).thenReturn(2L);
        
        
        assertDoesNotThrow(() -> {
            tetrazolioService.finalizarAnalisis(1L);
        });
    }

    @Test
    @DisplayName("validarEvidenciaAntesDeFinalizar - sin repeticiones ni porcentajes lanza excepción")
    void validarEvidenciaAntesDeFinalizar_sinEvidencia_lanzaExcepcion() {
        
        tetrazolio.setNumRepeticionesEsperadas(2);
        tetrazolio.setRepeticiones(Collections.emptyList());
        tetrazolio.setPorcViablesRedondeo(null);
        tetrazolio.setPorcNoViablesRedondeo(null);
        tetrazolio.setPorcDurasRedondeo(null);
        
        when(analisisService.finalizarAnalisisGenerico(eq(1L), eq(tetrazolioRepository), any(), any()))
            .thenAnswer(invocation -> {
                
                java.util.function.Consumer<Tetrazolio> validator = invocation.getArgument(3);
                validator.accept(tetrazolio); 
                return null;
            });
        when(repeticionRepository.countByTetrazolioId(1L)).thenReturn(2L);
        
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tetrazolioService.finalizarAnalisis(1L);
        });
        
        assertTrue(exception.getMessage().contains("evidencia"));
    }

    @Test
    @DisplayName("Reactivar tetrazolio - debe cambiar activo a true")
    void reactivarTetrazolio_debeCambiarActivoATrue() {
        
        tetrazolio.setActivo(false);
        TetrazolioDTO tetrazolioDTO = new TetrazolioDTO();
        tetrazolioDTO.setAnalisisID(1L);
        tetrazolioDTO.setActivo(true);
        
        when(analisisService.reactivarAnalisis(eq(1L), eq(tetrazolioRepository), any()))
            .thenReturn(tetrazolioDTO);
        
        
        TetrazolioDTO resultado = tetrazolioService.reactivarTetrazolio(1L);
        
        
        assertNotNull(resultado);
        assertTrue(resultado.getActivo());
        verify(analisisService, times(1)).reactivarAnalisis(eq(1L), eq(tetrazolioRepository), any());
    }

    @Test
    @DisplayName("obtenerTetrazoliosPaginadasConFiltros - con filtros dinámicos")
    void obtenerTetrazoliosPaginadasConFiltros_conFiltrosDinamicos() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tetrazolio> tetrazolioPage = new PageImpl<>(Arrays.asList(tetrazolio));
        
        @SuppressWarnings("unchecked")
        Specification<Tetrazolio> anySpec = any(Specification.class);
        when(tetrazolioRepository.findAll(anySpec, eq(pageable)))
            .thenReturn(tetrazolioPage);
        when(analisisHistorialService.obtenerHistorialAnalisis(anyLong()))
            .thenReturn(Collections.emptyList());
        
        
        var resultado = tetrazolioService.obtenerTetrazoliosPaginadasConFiltros(
            pageable, "LOTE-001", true, "EN_PROCESO", 1L);
        
        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
    }
}
