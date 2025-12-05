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

import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.AnalisisPendienteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.AnalisisPorAprobarRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.AnalisisRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.projections.AnalisisPendienteProjection;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.projections.AnalisisPorAprobarProjection;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.AnalisisPendienteDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.AnalisisPorAprobarDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.DashboardStatsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de DashboardService")
class DashboardServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private AnalisisRepository analisisRepository;

    @Mock
    private AnalisisPendienteRepository analisisPendienteRepository;

    @Mock
    private AnalisisPorAprobarRepository analisisPorAprobarRepository;

    @Mock
    private LoteService loteService;

    @InjectMocks
    private DashboardService dashboardService;

    private AnalisisPendienteProjection analisisPendienteProjection;
    private AnalisisPorAprobarProjection analisisPorAprobarProjection;

    @BeforeEach
    void setUp() {
        // ARRANGE: Preparar datos de prueba
        analisisPendienteProjection = new AnalisisPendienteProjection() {
            @Override
            public Long getLoteID() { return 1L; }
            
            @Override
            public String getNomLote() { return "LOTE-001"; }
            
            @Override
            public String getFicha() { return "F-001"; }
            
            @Override
            public String getEspecieNombre() { return "Trigo"; }
            
            @Override
            public String getCultivarNombre() { return "Cultivar 1"; }
            
            @Override
            public String getTipoAnalisis() { return "PUREZA"; }
        };

        analisisPorAprobarProjection = new AnalisisPorAprobarProjection() {
            @Override
            public Long getAnalisisID() { return 1L; }
            
            @Override
            public String getTipoAnalisis() { return "GERMINACION"; }
            
            @Override
            public Long getLoteID() { return 1L; }
            
            @Override
            public String getNomLote() { return "LOTE-001"; }
            
            @Override
            public String getFicha() { return "F-001"; }
            
            @Override
            public String getFechaInicio() { return "2024-01-01 10:00:00"; }
            
            @Override
            public String getFechaFin() { return "2024-01-08 10:00:00"; }
            
            @Override
            public String getEspecieNombre() { return "Trigo"; }
            
            @Override
            public String getCultivarNombre() { return "Cultivar 1"; }
        };
    }

    @Test
    @DisplayName("Obtener estadísticas - debe retornar todas las métricas del dashboard")
    void obtenerEstadisticas_debeRetornarTodasLasMetricas() {
        
        when(loteRepository.countLotesActivos()).thenReturn(25L);
        when(loteService.contarAnalisisPendientes()).thenReturn(12L);
        when(analisisRepository.countCompletadosEnFecha(any(LocalDate.class), eq(Estado.APROBADO)))
            .thenReturn(5L);
        when(analisisRepository.countByEstado(Estado.PENDIENTE_APROBACION)).thenReturn(8L);

        
        DashboardStatsDTO resultado = dashboardService.obtenerEstadisticas();

        
        assertNotNull(resultado);
        assertEquals(25L, resultado.getLotesActivos());
        assertEquals(12L, resultado.getAnalisisPendientes());
        assertEquals(5L, resultado.getCompletadosHoy());
        assertEquals(8L, resultado.getAnalisisPorAprobar());
        
        verify(loteRepository, times(1)).countLotesActivos();
        verify(loteService, times(1)).contarAnalisisPendientes();
        verify(analisisRepository, times(1)).countCompletadosEnFecha(any(LocalDate.class), eq(Estado.APROBADO));
        verify(analisisRepository, times(1)).countByEstado(Estado.PENDIENTE_APROBACION);
    }

    @Test
    @DisplayName("Obtener estadísticas sin datos - debe retornar ceros")
    void obtenerEstadisticas_sinDatos_debeRetornarCeros() {
        
        when(loteRepository.countLotesActivos()).thenReturn(0L);
        when(loteService.contarAnalisisPendientes()).thenReturn(0L);
        when(analisisRepository.countCompletadosEnFecha(any(LocalDate.class), eq(Estado.APROBADO)))
            .thenReturn(0L);
        when(analisisRepository.countByEstado(Estado.PENDIENTE_APROBACION)).thenReturn(0L);

        
        DashboardStatsDTO resultado = dashboardService.obtenerEstadisticas();

        
        assertNotNull(resultado);
        assertEquals(0L, resultado.getLotesActivos());
        assertEquals(0L, resultado.getAnalisisPendientes());
        assertEquals(0L, resultado.getCompletadosHoy());
        assertEquals(0L, resultado.getAnalisisPorAprobar());
    }

    @Test
    @DisplayName("Listar análisis pendientes paginados - debe retornar página correcta")
    void listarAnalisisPendientesPaginados_debeRetornarPaginaCorrecta() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<AnalisisPendienteProjection> proyeccionesPage = 
            new PageImpl<>(Arrays.asList(analisisPendienteProjection));
        
        when(analisisPendienteRepository.findAllPaginado(any(Pageable.class)))
            .thenReturn(proyeccionesPage);

        
        Page<AnalisisPendienteDTO> resultado = dashboardService.listarAnalisisPendientesPaginados(pageable);

        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
        
        AnalisisPendienteDTO dto = resultado.getContent().get(0);
        assertEquals(1L, dto.getLoteID());
        assertEquals("LOTE-001", dto.getNomLote());
        assertEquals("F-001", dto.getFicha());
        assertEquals("Trigo", dto.getEspecieNombre());
        assertEquals("Cultivar 1", dto.getCultivarNombre());
        assertEquals(TipoAnalisis.PUREZA, dto.getTipoAnalisis());
        
        verify(analisisPendienteRepository, times(1)).findAllPaginado(pageable);
    }

    @Test
    @DisplayName("Listar análisis pendientes vacío - debe retornar página vacía")
    void listarAnalisisPendientesPaginados_vacio_debeRetornarPaginaVacia() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<AnalisisPendienteProjection> proyeccionesPage = Page.empty();
        
        when(analisisPendienteRepository.findAllPaginado(any(Pageable.class)))
            .thenReturn(proyeccionesPage);

        
        Page<AnalisisPendienteDTO> resultado = dashboardService.listarAnalisisPendientesPaginados(pageable);

        
        assertNotNull(resultado);
        assertEquals(0, resultado.getTotalElements());
        assertTrue(resultado.getContent().isEmpty());
    }

    @Test
    @DisplayName("Listar análisis por aprobar paginados - debe retornar página correcta")
    void listarAnalisisPorAprobarPaginados_debeRetornarPaginaCorrecta() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<AnalisisPorAprobarProjection> proyeccionesPage = 
            new PageImpl<>(Arrays.asList(analisisPorAprobarProjection));
        
        when(analisisPorAprobarRepository.findAllPaginado(any(Pageable.class)))
            .thenReturn(proyeccionesPage);

        
        Page<AnalisisPorAprobarDTO> resultado = dashboardService.listarAnalisisPorAprobarPaginados(pageable);

        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
        
        AnalisisPorAprobarDTO dto = resultado.getContent().get(0);
        assertEquals(1L, dto.getAnalisisID());
        assertEquals(TipoAnalisis.GERMINACION, dto.getTipo());
        assertEquals(1L, dto.getLoteID());
        assertEquals("LOTE-001", dto.getNomLote());
        assertEquals("F-001", dto.getFicha());
        
        verify(analisisPorAprobarRepository, times(1)).findAllPaginado(pageable);
    }

    @Test
    @DisplayName("Listar análisis por aprobar vacío - debe retornar página vacía")
    void listarAnalisisPorAprobarPaginados_vacio_debeRetornarPaginaVacia() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<AnalisisPorAprobarProjection> proyeccionesPage = Page.empty();
        
        when(analisisPorAprobarRepository.findAllPaginado(any(Pageable.class)))
            .thenReturn(proyeccionesPage);

        
        Page<AnalisisPorAprobarDTO> resultado = dashboardService.listarAnalisisPorAprobarPaginados(pageable);

        
        assertNotNull(resultado);
        assertEquals(0, resultado.getTotalElements());
        assertTrue(resultado.getContent().isEmpty());
    }

    @Test
    @DisplayName("Obtener estadísticas debe calcular correctamente completados hoy")
    void obtenerEstadisticas_debeCalcularCompletadosHoyCorrectamente() {
        
        LocalDate hoy = LocalDate.now();
        when(loteRepository.countLotesActivos()).thenReturn(10L);
        when(loteService.contarAnalisisPendientes()).thenReturn(5L);
        when(analisisRepository.countCompletadosEnFecha(hoy, Estado.APROBADO)).thenReturn(3L);
        when(analisisRepository.countByEstado(Estado.PENDIENTE_APROBACION)).thenReturn(2L);

        
        DashboardStatsDTO resultado = dashboardService.obtenerEstadisticas();

        
        assertEquals(3L, resultado.getCompletadosHoy());
        verify(analisisRepository, times(1)).countCompletadosEnFecha(hoy, Estado.APROBADO);
    }

    @Test
    @DisplayName("Análisis pendientes debe incluir todos los tipos de análisis")
    void listarAnalisisPendientes_debeIncluirTodosLosTipos() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<AnalisisPendienteProjection> proyeccionesPage = 
            new PageImpl<>(Arrays.asList(analisisPendienteProjection));
        
        when(analisisPendienteRepository.findAllPaginado(pageable))
            .thenReturn(proyeccionesPage);

        
        Page<AnalisisPendienteDTO> resultado = dashboardService.listarAnalisisPendientesPaginados(pageable);

        
        assertNotNull(resultado);
        assertFalse(resultado.getContent().isEmpty());
        // Verificar que el tipo de análisis se mapea correctamente
        assertEquals(TipoAnalisis.PUREZA, resultado.getContent().get(0).getTipoAnalisis());
    }
}
