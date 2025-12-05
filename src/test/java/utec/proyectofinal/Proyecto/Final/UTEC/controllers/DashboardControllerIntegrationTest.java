package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.AnalisisPendienteDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.AnalisisPorAprobarDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.DashboardStatsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis;
import utec.proyectofinal.Proyecto.Final.UTEC.services.DashboardService;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests de integración para DashboardController")
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    private DashboardStatsDTO statsDTO;
    private AnalisisPendienteDTO analisisPendiente;
    private AnalisisPorAprobarDTO analisisPorAprobar;

    @BeforeEach
    void setUp() {
        statsDTO = new DashboardStatsDTO();
        statsDTO.setLotesActivos(100L);
        statsDTO.setAnalisisPendientes(10L);
        statsDTO.setCompletadosHoy(40L);
        statsDTO.setAnalisisPorAprobar(5L);

        analisisPendiente = new AnalisisPendienteDTO();
        analisisPendiente.setLoteID(1L);
        analisisPendiente.setTipoAnalisis(TipoAnalisis.GERMINACION);
        analisisPendiente.setNomLote("LOTE-001");
        analisisPendiente.setFicha("F-001");

        analisisPorAprobar = new AnalisisPorAprobarDTO();
        analisisPorAprobar.setAnalisisID(2L);
        analisisPorAprobar.setTipo(TipoAnalisis.PUREZA);
        analisisPorAprobar.setLoteID(2L);
        analisisPorAprobar.setNomLote("LOTE-002");
    }

    @Test
    @DisplayName("GET /api/dashboard/stats - Debe obtener estadísticas del dashboard")
    @WithMockUser(roles = "ADMIN")
    void obtenerEstadisticas_debeRetornarStats() throws Exception {
        when(dashboardService.obtenerEstadisticas()).thenReturn(statsDTO);

        mockMvc.perform(get("/api/dashboard/stats")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lotesActivos").value(100))
                .andExpect(jsonPath("$.analisisPendientes").value(10))
                .andExpect(jsonPath("$.completadosHoy").value(40))
                .andExpect(jsonPath("$.analisisPorAprobar").value(5));
    }

    @Test
    @DisplayName("GET /api/dashboard/stats - Debe manejar error interno")
    @WithMockUser(roles = "ADMIN")
    void obtenerEstadisticas_conError_debeRetornar500() throws Exception {
        when(dashboardService.obtenerEstadisticas()).thenThrow(new RuntimeException("Error interno"));

        mockMvc.perform(get("/api/dashboard/stats")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/dashboard/analisis-pendientes - Debe obtener análisis pendientes paginados")
    @WithMockUser(roles = "ANALISTA")
    void obtenerAnalisisPendientes_debeRetornarPaginacion() throws Exception {
        Page<AnalisisPendienteDTO> page = new PageImpl<>(Arrays.asList(analisisPendiente));
        when(dashboardService.listarAnalisisPendientesPaginados(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/dashboard/analisis-pendientes")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].loteID").value(1))
                .andExpect(jsonPath("$.content[0].tipoAnalisis").value("GERMINACION"));
    }

    @Test
    @DisplayName("GET /api/dashboard/analisis-por-aprobar - Debe obtener análisis por aprobar con rol ADMIN")
    @WithMockUser(roles = "ADMIN")
    void obtenerAnalisisPorAprobar_conRolAdmin_debeRetornarPaginacion() throws Exception {
        Page<AnalisisPorAprobarDTO> page = new PageImpl<>(Arrays.asList(analisisPorAprobar));
        when(dashboardService.listarAnalisisPorAprobarPaginados(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/dashboard/analisis-por-aprobar")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].analisisID").value(2))
                .andExpect(jsonPath("$.content[0].tipo").value("PUREZA"));
    }

    @Test
    @DisplayName("GET /api/dashboard/analisis-pendientes - Debe usar valores por defecto de paginación")
    @WithMockUser(roles = "ANALISTA")
    void obtenerAnalisisPendientes_sinParametros_debeUsarDefaults() throws Exception {
        Page<AnalisisPendienteDTO> page = new PageImpl<>(Arrays.asList(analisisPendiente));
        when(dashboardService.listarAnalisisPendientesPaginados(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/dashboard/analisis-pendientes")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/dashboard/analisis-pendientes - Debe manejar error en servicio")
    @WithMockUser(roles = "ANALISTA")
    void obtenerAnalisisPendientes_conError_debeRetornar500() throws Exception {
        when(dashboardService.listarAnalisisPendientesPaginados(any(Pageable.class)))
            .thenThrow(new RuntimeException("Error en servicio"));

        mockMvc.perform(get("/api/dashboard/analisis-pendientes")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /api/dashboard/analisis-por-aprobar - Debe manejar error en servicio")
    @WithMockUser(roles = "ADMIN")
    void obtenerAnalisisPorAprobar_conError_debeRetornar500() throws Exception {
        when(dashboardService.listarAnalisisPorAprobarPaginados(any(Pageable.class)))
            .thenThrow(new RuntimeException("Error en servicio"));

        mockMvc.perform(get("/api/dashboard/analisis-por-aprobar")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }
}
