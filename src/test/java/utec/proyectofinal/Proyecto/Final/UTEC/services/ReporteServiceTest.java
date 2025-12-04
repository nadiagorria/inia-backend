package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.*;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.*;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.*;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de ReporteService")
class ReporteServiceTest {

    @Mock
    private GerminacionRepository germinacionRepository;

    @Mock
    private PurezaRepository purezaRepository;

    @Mock
    private PmsRepository pmsRepository;

    @Mock
    private TetrazolioRepository tetrazolioRepository;

    @Mock
    private DosnRepository dosnRepository;

    @InjectMocks
    private ReporteService reporteService;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Especie especieMock;
    private Cultivar cultivarMock;
    private Lote loteMock;

    @BeforeEach
    void setUp() {
        fechaInicio = LocalDate.of(2024, 1, 1);
        fechaFin = LocalDate.of(2024, 12, 31);

        
        especieMock = new Especie();
        especieMock.setNombreComun("Soja");

        cultivarMock = new Cultivar();
        cultivarMock.setEspecie(especieMock);

        loteMock = new Lote();
        loteMock.setCultivar(cultivarMock);
    }

    @Test
    @DisplayName("Obtener reporte general - sin análisis debe retornar reporte vacío")
    void obtenerReporteGeneral_sinAnalisis_debeRetornarReporteVacio() {
        
        when(germinacionRepository.findAll()).thenReturn(Collections.emptyList());
        when(purezaRepository.findAll()).thenReturn(Collections.emptyList());
        when(pmsRepository.findAll()).thenReturn(Collections.emptyList());
        when(tetrazolioRepository.findAll()).thenReturn(Collections.emptyList());
        when(dosnRepository.findAll()).thenReturn(Collections.emptyList());

        
        ReporteGeneralDTO reporte = reporteService.obtenerReporteGeneral(fechaInicio, fechaFin);

        
        assertNotNull(reporte);
        assertEquals(0L, reporte.getTotalAnalisis());
        assertTrue(reporte.getAnalisisPorPeriodo().isEmpty());
        assertTrue(reporte.getAnalisisPorEstado().isEmpty());
        
        verify(germinacionRepository, atLeastOnce()).findAll();
        verify(purezaRepository, atLeastOnce()).findAll();
    }

    @Test
    @DisplayName("Obtener reporte general - con análisis debe calcular métricas correctamente")
    void obtenerReporteGeneral_conAnalisis_debeCalcularMetricas() {
        
        Germinacion germ = crearGerminacion(Estado.APROBADO, LocalDateTime.of(2024, 6, 15, 10, 0));
        Pureza pureza = crearPureza(Estado.EN_PROCESO, LocalDateTime.of(2024, 6, 20, 14, 0));

        when(germinacionRepository.findAll()).thenReturn(Collections.singletonList(germ));
        when(purezaRepository.findAll()).thenReturn(Collections.singletonList(pureza));
        when(pmsRepository.findAll()).thenReturn(Collections.emptyList());
        when(tetrazolioRepository.findAll()).thenReturn(Collections.emptyList());
        when(dosnRepository.findAll()).thenReturn(Collections.emptyList());

        
        ReporteGeneralDTO reporte = reporteService.obtenerReporteGeneral(fechaInicio, fechaFin);

        
        assertNotNull(reporte);
        assertEquals(2L, reporte.getTotalAnalisis());
        assertFalse(reporte.getAnalisisPorPeriodo().isEmpty());
        assertFalse(reporte.getAnalisisPorEstado().isEmpty());
        assertTrue(reporte.getAnalisisPorEstado().containsKey("APROBADO"));
        assertTrue(reporte.getAnalisisPorEstado().containsKey("EN_PROCESO"));
    }

    @Test
    @DisplayName("Obtener reporte germinación - debe calcular media por especie")
    void obtenerReporteGerminacion_debeCalcularMediaPorEspecie() {
        
        Germinacion germ = crearGerminacion(Estado.APROBADO, LocalDateTime.of(2024, 6, 15, 10, 0));
        
        TablaGerm tablaGerm = new TablaGerm();
        tablaGerm.setPorcentajeNormalesConRedondeo(BigDecimal.valueOf(85.5));
        tablaGerm.setFechaGerminacion(LocalDate.of(2024, 6, 10));
        tablaGerm.setFechaConteos(Arrays.asList(
            LocalDate.of(2024, 6, 12),
            LocalDate.of(2024, 6, 14)
        ));
        tablaGerm.setFechaUltConteo(LocalDate.of(2024, 6, 14));
        
        germ.setTablaGerm(Collections.singletonList(tablaGerm));

        when(germinacionRepository.findAll()).thenReturn(Collections.singletonList(germ));

        
        ReporteGerminacionDTO reporte = reporteService.obtenerReporteGerminacion(fechaInicio, fechaFin);

        
        assertNotNull(reporte);
        assertEquals(1L, reporte.getTotalGerminaciones());
        assertFalse(reporte.getMediaGerminacionPorEspecie().isEmpty());
        assertTrue(reporte.getMediaGerminacionPorEspecie().containsKey("Soja"));
    }

    @Test
    @DisplayName("Obtener reporte PMS - debe calcular muestras con CV superado")
    void obtenerReportePms_debeCalcularMuestrasConCVSuperado() {
        
        Pms pms1 = crearPms(BigDecimal.valueOf(7.5), 10); 
        Pms pms2 = crearPms(BigDecimal.valueOf(4.2), 8);  
        Pms pms3 = crearPms(BigDecimal.valueOf(8.1), 16); 

        when(pmsRepository.findAll()).thenReturn(Arrays.asList(pms1, pms2, pms3));

        
        ReportePMSDTO reporte = reporteService.obtenerReportePms(fechaInicio, fechaFin);

        
        assertNotNull(reporte);
        assertEquals(3L, reporte.getTotalPms());
        assertEquals(2L, reporte.getMuestrasConCVSuperado());
        assertTrue(reporte.getPorcentajeMuestrasConCVSuperado() > 66.0);
        assertTrue(reporte.getPorcentajeMuestrasConCVSuperado() < 67.0);
        assertEquals(1L, reporte.getMuestrasConRepeticionesMaximas());
    }

    @Test
    @DisplayName("Obtener reporte Pureza - debe calcular porcentajes por especie")
    void obtenerReportePureza_debeCalcularPorcentajesPorEspecie() {
        
        Pureza pureza1 = crearPureza(
            BigDecimal.valueOf(100.0),
            BigDecimal.valueOf(2.5),
            BigDecimal.valueOf(1.2),
            true
        );
        Pureza pureza2 = crearPureza(
            BigDecimal.valueOf(100.0),
            BigDecimal.valueOf(3.0),
            BigDecimal.valueOf(0.8),
            false
        );

        when(purezaRepository.findAll()).thenReturn(Arrays.asList(pureza1, pureza2));

        
        ReportePurezaDTO reporte = reporteService.obtenerReportePureza(fechaInicio, fechaFin);

        
        assertNotNull(reporte);
        assertEquals(2L, reporte.getTotalPurezas());
        assertFalse(reporte.getContaminantesPorEspecie().isEmpty());
        assertTrue(reporte.getContaminantesPorEspecie().containsKey("Soja"));
        assertTrue(reporte.getPorcentajeCumpleEstandar().containsKey("Soja"));
        assertEquals(50.0, reporte.getPorcentajeCumpleEstandar().get("Soja"));
    }

    @Test
    @DisplayName("Obtener reporte Tetrazolio - debe calcular viabilidad por especie")
    void obtenerReporteTetrazolio_debeCalcularViabilidadPorEspecie() {
        
        Tetrazolio tetra1 = crearTetrazolio(BigDecimal.valueOf(92.5));
        Tetrazolio tetra2 = crearTetrazolio(BigDecimal.valueOf(88.3));

        when(tetrazolioRepository.findAll()).thenReturn(Arrays.asList(tetra1, tetra2));

        
        ReporteTetrazolioDTO reporte = reporteService.obtenerReporteTetrazolio(fechaInicio, fechaFin);

        
        assertNotNull(reporte);
        assertEquals(2L, reporte.getTotalTetrazolios());
        assertFalse(reporte.getViabilidadIniaPorEspecie().isEmpty());
        assertTrue(reporte.getViabilidadIniaPorEspecie().containsKey("Soja"));
        assertTrue(reporte.getViabilidadIniaPorEspecie().get("Soja") > 90.0);
        assertFalse(reporte.getViabilidadInasePorEspecie().isEmpty());
        assertTrue(reporte.getViabilidadInasePorEspecie().containsKey("Soja"));
        assertTrue(reporte.getViabilidadInasePorEspecie().get("Soja") > 90.0);
    }

    @Test
    @DisplayName("Obtener reporte DOSN - debe retornar estructura correcta")
    void obtenerReporteDosn_debeRetornarEstructuraCorrecta() {
        
        Dosn dosn = crearDosn(true);

        when(dosnRepository.findAll()).thenReturn(Collections.singletonList(dosn));

        
        ReportePurezaDTO reporte = reporteService.obtenerReporteDosn(fechaInicio, fechaFin);

        
        assertNotNull(reporte);
        assertEquals(1L, reporte.getTotalPurezas());
        assertFalse(reporte.getContaminantesPorEspecie().isEmpty());
        assertTrue(reporte.getPorcentajeCumpleEstandar().containsKey("Soja"));
    }

    @Test
    @DisplayName("Obtener contaminantes por especie Pureza - debe listar detalle de contaminantes")
    void obtenerContaminantesPorEspeciePureza_debeListarDetalle() {
        
        Pureza pureza = crearPurezaConListados();

        when(purezaRepository.findAll()).thenReturn(Collections.singletonList(pureza));

        
        Map<String, Double> contaminantes = reporteService.obtenerContaminantesPorEspeciePureza("Soja", fechaInicio, fechaFin);

        
        assertNotNull(contaminantes);
        
        
    }

    @Test
    @DisplayName("Obtener contaminantes por especie DOSN - debe listar detalle de contaminantes")
    void obtenerContaminantesPorEspecieDosn_debeListarDetalle() {
        
        Dosn dosn = crearDosnConListados();

        when(dosnRepository.findAll()).thenReturn(Collections.singletonList(dosn));

        
        Map<String, Double> contaminantes = reporteService.obtenerContaminantesPorEspecieDosn("Soja", fechaInicio, fechaFin);

        
        assertNotNull(contaminantes);
        
        
    }

    @Test
    @DisplayName("Filtrado por fechas - debe excluir análisis fuera del rango")
    void filtradoPorFechas_debeExcluirAnalisisFueraDelRango() {
        
        Germinacion germDentro = crearGerminacion(Estado.APROBADO, LocalDateTime.of(2024, 6, 15, 10, 0));
        Germinacion germFuera = crearGerminacion(Estado.APROBADO, LocalDateTime.of(2023, 12, 31, 10, 0));

        when(germinacionRepository.findAll()).thenReturn(Arrays.asList(germDentro, germFuera));
        when(purezaRepository.findAll()).thenReturn(Collections.emptyList());
        when(pmsRepository.findAll()).thenReturn(Collections.emptyList());
        when(tetrazolioRepository.findAll()).thenReturn(Collections.emptyList());
        when(dosnRepository.findAll()).thenReturn(Collections.emptyList());

        
        ReporteGeneralDTO reporte = reporteService.obtenerReporteGeneral(fechaInicio, fechaFin);

        
        assertNotNull(reporte);
        assertEquals(1L, reporte.getTotalAnalisis()); 
    }

    @Test
    @DisplayName("Análisis inactivos - no deben incluirse en reportes")
    void analisisInactivos_noDebenIncluirse() {
        
        Pureza purezaActiva = crearPureza(Estado.APROBADO, LocalDateTime.of(2024, 6, 15, 10, 0));
        purezaActiva.setActivo(true);
        
        Pureza purezaInactiva = crearPureza(Estado.APROBADO, LocalDateTime.of(2024, 6, 16, 10, 0));
        purezaInactiva.setActivo(false);

        when(germinacionRepository.findAll()).thenReturn(Collections.emptyList());
        when(purezaRepository.findAll()).thenReturn(Arrays.asList(purezaActiva, purezaInactiva));
        when(pmsRepository.findAll()).thenReturn(Collections.emptyList());
        when(tetrazolioRepository.findAll()).thenReturn(Collections.emptyList());
        when(dosnRepository.findAll()).thenReturn(Collections.emptyList());

        
        ReporteGeneralDTO reporte = reporteService.obtenerReporteGeneral(fechaInicio, fechaFin);

        
        assertNotNull(reporte);
        assertEquals(1L, reporte.getTotalAnalisis()); 
    }

    @Test
    @DisplayName("Reporte con fechas nulas - debe incluir todos los análisis")
    void reporteConFechasNulas_debeIncluirTodos() {
        
        Germinacion germ = crearGerminacion(Estado.APROBADO, LocalDateTime.of(2020, 1, 1, 10, 0));

        when(germinacionRepository.findAll()).thenReturn(Collections.singletonList(germ));
        when(purezaRepository.findAll()).thenReturn(Collections.emptyList());
        when(pmsRepository.findAll()).thenReturn(Collections.emptyList());
        when(tetrazolioRepository.findAll()).thenReturn(Collections.emptyList());
        when(dosnRepository.findAll()).thenReturn(Collections.emptyList());

        
        ReporteGeneralDTO reporte = reporteService.obtenerReporteGeneral(null, null);

        
        assertNotNull(reporte);
        assertEquals(1L, reporte.getTotalAnalisis());
    }

    @Test
    @DisplayName("Top análisis con problemas - debe ordenar por cantidad")
    void topAnalisisConProblemas_debeOrdenarPorCantidad() {
        
        Germinacion germ1 = crearGerminacion(Estado.A_REPETIR, LocalDateTime.of(2024, 6, 15, 10, 0));
        Germinacion germ2 = crearGerminacion(Estado.A_REPETIR, LocalDateTime.of(2024, 6, 16, 10, 0));
        Pureza pureza = crearPureza(Estado.A_REPETIR, LocalDateTime.of(2024, 6, 17, 10, 0));

        when(germinacionRepository.findAll()).thenReturn(Arrays.asList(germ1, germ2));
        when(purezaRepository.findAll()).thenReturn(Collections.singletonList(pureza));
        when(pmsRepository.findAll()).thenReturn(Collections.emptyList());
        when(tetrazolioRepository.findAll()).thenReturn(Collections.emptyList());
        when(dosnRepository.findAll()).thenReturn(Collections.emptyList());

        
        ReporteGeneralDTO reporte = reporteService.obtenerReporteGeneral(fechaInicio, fechaFin);

        
        assertNotNull(reporte);
        assertNotNull(reporte.getTopAnalisisProblemas());
        assertTrue(reporte.getTopAnalisisProblemas().containsKey("GERMINACION"));
    }

    

    private Germinacion crearGerminacion(Estado estado, LocalDateTime fechaInicio) {
        Germinacion germ = new Germinacion();
        germ.setEstado(estado);
        germ.setFechaInicio(fechaInicio);
        germ.setActivo(true);
        germ.setLote(loteMock);
        return germ;
    }

    private Pureza crearPureza(Estado estado, LocalDateTime fechaInicio) {
        Pureza pureza = new Pureza();
        pureza.setEstado(estado);
        pureza.setFechaInicio(fechaInicio);
        pureza.setActivo(true);
        pureza.setLote(loteMock);
        return pureza;
    }

    private Pureza crearPureza(BigDecimal pesoInicial, BigDecimal malezas, BigDecimal otrosCultivos, boolean cumpleEstandar) {
        Pureza pureza = new Pureza();
        pureza.setEstado(Estado.APROBADO);
        pureza.setFechaInicio(LocalDateTime.of(2024, 6, 15, 10, 0));
        pureza.setActivo(true);
        pureza.setLote(loteMock);
        pureza.setPesoInicial_g(pesoInicial);
        pureza.setMalezas_g(malezas);
        pureza.setOtrosCultivos_g(otrosCultivos);
        pureza.setCumpleEstandar(cumpleEstandar);
        return pureza;
    }

    private Pureza crearPurezaConListados() {
        Pureza pureza = crearPureza(Estado.APROBADO, LocalDateTime.of(2024, 6, 15, 10, 0));
        pureza.setListados(Collections.emptyList());
        return pureza;
    }

    private Pms crearPms(BigDecimal coefVariacion, int numRepeticiones) {
        Pms pms = new Pms();
        pms.setEstado(Estado.APROBADO);
        pms.setFechaInicio(LocalDateTime.of(2024, 6, 15, 10, 0));
        pms.setActivo(true);
        pms.setLote(loteMock);
        pms.setCoefVariacion(coefVariacion);
        pms.setNumRepeticionesEsperadas(numRepeticiones);
        return pms;
    }

    private Tetrazolio crearTetrazolio(BigDecimal viabilidad) {
        Tetrazolio tetra = new Tetrazolio();
        tetra.setEstado(Estado.APROBADO);
        tetra.setFechaInicio(LocalDateTime.of(2024, 6, 15, 10, 0));
        tetra.setActivo(true);
        tetra.setLote(loteMock);
        tetra.setPorcViablesRedondeo(viabilidad);
        tetra.setViabilidadInase(viabilidad);
        return tetra;
    }

    private Dosn crearDosn(boolean cumpleEstandar) {
        Dosn dosn = new Dosn();
        dosn.setEstado(Estado.APROBADO);
        dosn.setFechaInicio(LocalDateTime.of(2024, 6, 15, 10, 0));
        dosn.setActivo(true);
        dosn.setLote(loteMock);
        dosn.setCumpleEstandar(cumpleEstandar);
        return dosn;
    }

    private Dosn crearDosnConListados() {
        Dosn dosn = crearDosn(true);
        dosn.setListados(Collections.emptyList());
        dosn.setCuscutaRegistros(Collections.emptyList());
        return dosn;
    }
}
