package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.*;
import utec.proyectofinal.Proyecto.Final.UTEC.services.ReporteService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReporteController.class)
@DisplayName("Tests de integración para ReporteController")
class ReporteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReporteService reporteService;

    private ReporteGeneralDTO reporteGeneralDTO;
    private ReporteGerminacionDTO reporteGerminacionDTO;
    private ReportePMSDTO reportePMSDTO;
    private ReportePurezaDTO reportePurezaDTO;
    private ReporteTetrazolioDTO reporteTetrazolioDTO;

    @BeforeEach
    void setUp() {
        Map<String, Long> analisisPorPeriodo = new HashMap<>();
        analisisPorPeriodo.put("2024-01", 10L);
        analisisPorPeriodo.put("2024-02", 15L);

        Map<String, Long> analisisPorEstado = new HashMap<>();
        analisisPorEstado.put("APROBADO", 20L);
        analisisPorEstado.put("EN_PROCESO", 5L);

        Map<String, Double> porcentajeCompletitud = new HashMap<>();
        porcentajeCompletitud.put("completitud", 80.0);

        Map<String, Long> topAnalisisProblemas = new HashMap<>();
        topAnalisisProblemas.put("Germinacion", 3L);
        topAnalisisProblemas.put("PMS", 2L);

        reporteGeneralDTO = new ReporteGeneralDTO(
            25L,
            analisisPorPeriodo,
            analisisPorEstado,
            porcentajeCompletitud,
            7.5,
            topAnalisisProblemas
        );

        Map<String, Double> mediaGerminacionPorEspecie = new HashMap<>();
        mediaGerminacionPorEspecie.put("Trigo", 85.5);
        mediaGerminacionPorEspecie.put("Cebada", 78.3);

        Map<String, Double> tiempoPromedioPrimerConteo = new HashMap<>();
        tiempoPromedioPrimerConteo.put("Trigo", 5.0);

        Map<String, Double> tiempoPromedioUltimoConteo = new HashMap<>();
        tiempoPromedioUltimoConteo.put("Trigo", 10.0);

        reporteGerminacionDTO = new ReporteGerminacionDTO(
            mediaGerminacionPorEspecie,
            tiempoPromedioPrimerConteo,
            tiempoPromedioUltimoConteo,
            15L
        );

        reportePMSDTO = new ReportePMSDTO(
            100L,
            10L,
            10.0,
            5L,
            new HashMap<>()
        );

        Map<String, Long> contaminantesPorEspecie = new HashMap<>();
        contaminantesPorEspecie.put("Trigo", 8L);

        Map<String, Double> porcentajeMalezas = new HashMap<>();
        porcentajeMalezas.put("Trigo", 2.5);

        Map<String, Double> porcentajeOtrasSemillas = new HashMap<>();
        porcentajeOtrasSemillas.put("Trigo", 1.5);

        Map<String, Double> porcentajeCumpleEstandar = new HashMap<>();
        porcentajeCumpleEstandar.put("Trigo", 90.0);

        reportePurezaDTO = new ReportePurezaDTO(
            contaminantesPorEspecie,
            porcentajeMalezas,
            porcentajeOtrasSemillas,
            porcentajeCumpleEstandar,
            100L
        );

        Map<String, Double> mediaViabilidadIniaPorEspecie = new HashMap<>();
        mediaViabilidadIniaPorEspecie.put("Trigo", 88.0);

        Map<String, Double> mediaViabilidadInasePorEspecie = new HashMap<>();
        mediaViabilidadInasePorEspecie.put("Trigo", 90.0);

        reporteTetrazolioDTO = new ReporteTetrazolioDTO(
            mediaViabilidadIniaPorEspecie,
            mediaViabilidadInasePorEspecie,
            20L
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/reportes/general - Obtener reporte general sin filtros")
    void obtenerReporteGeneral_sinFiltros_debeRetornarReporte() throws Exception {
        when(reporteService.obtenerReporteGeneral(null, null)).thenReturn(reporteGeneralDTO);

        mockMvc.perform(get("/api/reportes/general")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAnalisis").value(25))
                .andExpect(jsonPath("$.tiempoMedioFinalizacion").value(7.5))
                .andExpect(jsonPath("$.analisisPorEstado.APROBADO").value(20));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/reportes/general - Obtener reporte general con filtros de fecha")
    void obtenerReporteGeneral_conFiltrosFecha_debeRetornarReporte() throws Exception {
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);
        LocalDate fechaFin = LocalDate.of(2024, 12, 31);

        when(reporteService.obtenerReporteGeneral(fechaInicio, fechaFin)).thenReturn(reporteGeneralDTO);

        mockMvc.perform(get("/api/reportes/general")
                .param("fechaInicio", "2024-01-01")
                .param("fechaFin", "2024-12-31")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAnalisis").value(25));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/reportes/germinacion - Obtener reporte de germinación")
    void obtenerReporteGerminacion_debeRetornarReporte() throws Exception {
        when(reporteService.obtenerReporteGerminacion(null, null)).thenReturn(reporteGerminacionDTO);

        mockMvc.perform(get("/api/reportes/germinacion")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGerminaciones").value(15))
                .andExpect(jsonPath("$.mediaGerminacionPorEspecie.Trigo").value(85.5))
                .andExpect(jsonPath("$.mediaGerminacionPorEspecie.Cebada").value(78.3));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/reportes/germinacion - Obtener reporte de germinación con filtros")
    void obtenerReporteGerminacion_conFiltros_debeRetornarReporte() throws Exception {
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);
        LocalDate fechaFin = LocalDate.of(2024, 6, 30);

        when(reporteService.obtenerReporteGerminacion(fechaInicio, fechaFin)).thenReturn(reporteGerminacionDTO);

        mockMvc.perform(get("/api/reportes/germinacion")
                .param("fechaInicio", "2024-01-01")
                .param("fechaFin", "2024-06-30")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGerminaciones").value(15));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/reportes/pms - Obtener reporte de PMS")
    void obtenerReportePms_debeRetornarReporte() throws Exception {
        when(reporteService.obtenerReportePms(null, null)).thenReturn(reportePMSDTO);

        mockMvc.perform(get("/api/reportes/pms")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPms").value(100))
                .andExpect(jsonPath("$.muestrasConCVSuperado").value(10))
                .andExpect(jsonPath("$.porcentajeMuestrasConCVSuperado").value(10.0))
                .andExpect(jsonPath("$.muestrasConRepeticionesMaximas").value(5));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/reportes/pms - Obtener reporte de PMS con filtros")
    void obtenerReportePms_conFiltros_debeRetornarReporte() throws Exception {
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);
        LocalDate fechaFin = LocalDate.of(2024, 12, 31);

        when(reporteService.obtenerReportePms(fechaInicio, fechaFin)).thenReturn(reportePMSDTO);

        mockMvc.perform(get("/api/reportes/pms")
                .param("fechaInicio", "2024-01-01")
                .param("fechaFin", "2024-12-31")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPms").value(100));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/reportes/pureza - Obtener reporte de pureza")
    void obtenerReportePureza_debeRetornarReporte() throws Exception {
        when(reporteService.obtenerReportePureza(null, null)).thenReturn(reportePurezaDTO);

        mockMvc.perform(get("/api/reportes/pureza")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contaminantesPorEspecie.Trigo").value(8))
                .andExpect(jsonPath("$.porcentajeMalezas.Trigo").value(2.5))
                .andExpect(jsonPath("$.porcentajeOtrasSemillas.Trigo").value(1.5))
                .andExpect(jsonPath("$.porcentajeCumpleEstandar.Trigo").value(90.0));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/reportes/pureza - Obtener reporte de pureza con filtros")
    void obtenerReportePureza_conFiltros_debeRetornarReporte() throws Exception {
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);
        LocalDate fechaFin = LocalDate.of(2024, 12, 31);

        when(reporteService.obtenerReportePureza(fechaInicio, fechaFin)).thenReturn(reportePurezaDTO);

        mockMvc.perform(get("/api/reportes/pureza")
                .param("fechaInicio", "2024-01-01")
                .param("fechaFin", "2024-12-31")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contaminantesPorEspecie.Trigo").value(8));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/reportes/tetrazolio - Obtener reporte de tetrazolio")
    void obtenerReporteTetrazolio_debeRetornarReporte() throws Exception {
        when(reporteService.obtenerReporteTetrazolio(null, null)).thenReturn(reporteTetrazolioDTO);

        mockMvc.perform(get("/api/reportes/tetrazolio")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTetrazolios").value(20))
                .andExpect(jsonPath("$.viabilidadIniaPorEspecie.Trigo").value(88.0))
                .andExpect(jsonPath("$.viabilidadInasePorEspecie.Trigo").value(90.0));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/reportes/tetrazolio - Obtener reporte de tetrazolio con filtros")
    void obtenerReporteTetrazolio_conFiltros_debeRetornarReporte() throws Exception {
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);
        LocalDate fechaFin = LocalDate.of(2024, 12, 31);

        when(reporteService.obtenerReporteTetrazolio(fechaInicio, fechaFin)).thenReturn(reporteTetrazolioDTO);

        mockMvc.perform(get("/api/reportes/tetrazolio")
                .param("fechaInicio", "2024-01-01")
                .param("fechaFin", "2024-12-31")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTetrazolios").value(20));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/reportes/dosn - Obtener reporte de DOSN")
    void obtenerReporteDosn_debeRetornarReporte() throws Exception {
        when(reporteService.obtenerReporteDosn(null, null)).thenReturn(reportePurezaDTO);

        mockMvc.perform(get("/api/reportes/dosn")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contaminantesPorEspecie.Trigo").value(8));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/reportes/dosn - Obtener reporte de DOSN con filtros")
    void obtenerReporteDosn_conFiltros_debeRetornarReporte() throws Exception {
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);
        LocalDate fechaFin = LocalDate.of(2024, 12, 31);

        when(reporteService.obtenerReporteDosn(fechaInicio, fechaFin)).thenReturn(reportePurezaDTO);

        mockMvc.perform(get("/api/reportes/dosn")
                .param("fechaInicio", "2024-01-01")
                .param("fechaFin", "2024-12-31")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porcentajeMalezas.Trigo").value(2.5));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/reportes/pureza/contaminantes/{especie} - Obtener contaminantes de pureza por especie")
    void obtenerContaminantesPureza_debeRetornarMapa() throws Exception {
        Map<String, Double> contaminantes = new HashMap<>();
        contaminantes.put("Malezas", 2.5);
        contaminantes.put("OtrasSemillas", 1.5);

        when(reporteService.obtenerContaminantesPorEspeciePureza(eq("Trigo"), isNull(), isNull())).thenReturn(contaminantes);

        mockMvc.perform(get("/api/reportes/pureza/contaminantes/Trigo")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Malezas").value(2.5))
                .andExpect(jsonPath("$.OtrasSemillas").value(1.5));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/reportes/pureza/contaminantes/{especie} - Obtener contaminantes de pureza por especie con filtros")
    void obtenerContaminantesPureza_conFiltros_debeRetornarMapa() throws Exception {
        Map<String, Double> contaminantes = new HashMap<>();
        contaminantes.put("Malezas", 3.0);
        contaminantes.put("OtrasSemillas", 2.0);

        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);
        LocalDate fechaFin = LocalDate.of(2024, 12, 31);

        when(reporteService.obtenerContaminantesPorEspeciePureza(eq("Trigo"), eq(fechaInicio), eq(fechaFin))).thenReturn(contaminantes);

        mockMvc.perform(get("/api/reportes/pureza/contaminantes/Trigo")
                .param("fechaInicio", "2024-01-01")
                .param("fechaFin", "2024-12-31")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Malezas").value(3.0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/reportes/dosn/contaminantes/{especie} - Obtener contaminantes de DOSN por especie")
    void obtenerContaminantesDosn_debeRetornarMapa() throws Exception {
        Map<String, Double> contaminantes = new HashMap<>();
        contaminantes.put("Malezas", 1.8);
        contaminantes.put("OtrasSemillas", 1.2);

        when(reporteService.obtenerContaminantesPorEspecieDosn(eq("Trigo"), isNull(), isNull())).thenReturn(contaminantes);

        mockMvc.perform(get("/api/reportes/dosn/contaminantes/Trigo")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Malezas").value(1.8))
                .andExpect(jsonPath("$.OtrasSemillas").value(1.2));
    }

    @Test
    @WithMockUser(roles = "ANALISTA")
    @DisplayName("GET /api/reportes/dosn/contaminantes/{especie} - Obtener contaminantes de DOSN por especie con filtros")
    void obtenerContaminantesDosn_conFiltros_debeRetornarMapa() throws Exception {
        Map<String, Double> contaminantes = new HashMap<>();
        contaminantes.put("Malezas", 2.2);
        contaminantes.put("OtrasSemillas", 1.8);

        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);
        LocalDate fechaFin = LocalDate.of(2024, 12, 31);

        when(reporteService.obtenerContaminantesPorEspecieDosn(eq("Trigo"), eq(fechaInicio), eq(fechaFin))).thenReturn(contaminantes);

        mockMvc.perform(get("/api/reportes/dosn/contaminantes/Trigo")
                .param("fechaInicio", "2024-01-01")
                .param("fechaFin", "2024-12-31")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Malezas").value(2.2));
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/reportes/general - Observador puede ver reportes")
    void obtenerReporteGeneral_conRolObservador_debeRetornarOk() throws Exception {
        when(reporteService.obtenerReporteGeneral(null, null)).thenReturn(reporteGeneralDTO);

        mockMvc.perform(get("/api/reportes/general")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OBSERVADOR")
    @DisplayName("GET /api/reportes/germinacion - Observador puede ver reportes de germinación")
    void obtenerReporteGerminacion_conRolObservador_debeRetornarOk() throws Exception {
        when(reporteService.obtenerReporteGerminacion(null, null)).thenReturn(reporteGerminacionDTO);

        mockMvc.perform(get("/api/reportes/germinacion")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/reportes/general - Solo fechaInicio especificada")
    void obtenerReporteGeneral_soloFechaInicio_debeRetornarReporte() throws Exception {
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);

        when(reporteService.obtenerReporteGeneral(eq(fechaInicio), isNull())).thenReturn(reporteGeneralDTO);

        mockMvc.perform(get("/api/reportes/general")
                .param("fechaInicio", "2024-01-01")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAnalisis").value(25));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/reportes/general - Solo fechaFin especificada")
    void obtenerReporteGeneral_soloFechaFin_debeRetornarReporte() throws Exception {
        LocalDate fechaFin = LocalDate.of(2024, 12, 31);

        when(reporteService.obtenerReporteGeneral(isNull(), eq(fechaFin))).thenReturn(reporteGeneralDTO);

        mockMvc.perform(get("/api/reportes/general")
                .param("fechaFin", "2024-12-31")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAnalisis").value(25));
    }
}
