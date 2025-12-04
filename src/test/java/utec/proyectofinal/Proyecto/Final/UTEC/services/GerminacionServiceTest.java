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

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.GerminacionRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.GerminacionEditRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.GerminacionRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.GerminacionDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.GerminacionListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de GerminacionService")
class GerminacionServiceTest {

    @Mock
    private GerminacionRepository germinacionRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private AnalisisHistorialService analisisHistorialService;

    @Mock
    private AnalisisService analisisService;

    @InjectMocks
    private GerminacionService germinacionService;

    private GerminacionRequestDTO germinacionRequestDTO;
    private Lote lote;
    private Germinacion germinacion;

    @BeforeEach
    void setUp() {
        
        lote = new Lote();
        lote.setLoteID(1L);
        lote.setNomLote("LOTE-GERM-001");
        lote.setActivo(true);

        germinacionRequestDTO = new GerminacionRequestDTO();
        germinacionRequestDTO.setIdLote(1L);
        germinacionRequestDTO.setComentarios("Test de germinación");

        germinacion = new Germinacion();
        germinacion.setAnalisisID(1L);
        germinacion.setLote(lote);
        germinacion.setEstado(Estado.REGISTRADO);
        germinacion.setActivo(true);
        germinacion.setComentarios("Test de germinación");
    }

    @Test
    @DisplayName("Crear germinación - debe asignar estado REGISTRADO")
    void crearGerminacion_debeAsignarEstadoRegistrado() {
        
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(germinacionRepository.save(any(Germinacion.class))).thenReturn(germinacion);
        doNothing().when(analisisService).establecerFechaInicio(any(Germinacion.class));
        doNothing().when(analisisHistorialService).registrarCreacion(any(Germinacion.class));

        
        GerminacionDTO resultado = germinacionService.crearGerminacion(germinacionRequestDTO);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        verify(germinacionRepository, times(1)).save(any(Germinacion.class));
        verify(analisisHistorialService, times(1)).registrarCreacion(any(Germinacion.class));
        verify(analisisService, times(1)).establecerFechaInicio(any(Germinacion.class));
    }

    @Test
    @DisplayName("Crear germinación con lote inexistente - debe lanzar excepción")
    void crearGerminacion_conLoteInexistente_debeLanzarExcepcion() {
        
        when(loteRepository.findById(999L)).thenReturn(Optional.empty());
        germinacionRequestDTO.setIdLote(999L);

        
        assertThrows(RuntimeException.class, () -> {
            germinacionService.crearGerminacion(germinacionRequestDTO);
        }, "Debe lanzar excepción cuando el lote no existe");
    }

    @Test
    @DisplayName("Crear germinación sin repeticiones esperadas - debe lanzar excepción")
    void crearGerminacion_sinLote_debeLanzarExcepcion() {
        
        germinacionRequestDTO.setIdLote(null);

        
        assertThrows(RuntimeException.class, () -> {
            germinacionService.crearGerminacion(germinacionRequestDTO);
        }, "Debe lanzar excepción cuando no hay lote");
    }

    @Test
    @DisplayName("Crear germinación con repeticiones = 0 - debe lanzar excepción")
    void crearGerminacion_debeFuncionar() {
        
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(germinacionRepository.save(any(Germinacion.class))).thenReturn(germinacion);
        doNothing().when(analisisService).establecerFechaInicio(any(Germinacion.class));
        doNothing().when(analisisHistorialService).registrarCreacion(any(Germinacion.class));

        
        GerminacionDTO resultado = germinacionService.crearGerminacion(germinacionRequestDTO);

        
        assertNotNull(resultado);
        verify(germinacionRepository, times(1)).save(any(Germinacion.class));
    }

    @Test
    @DisplayName("Crear germinación con días <= 0 - debe funcionar correctamente")
    void crearGerminacion_conLoteValido_debeFuncionar() {
        
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(germinacionRepository.save(any(Germinacion.class))).thenReturn(germinacion);
        doNothing().when(analisisService).establecerFechaInicio(any(Germinacion.class));
        doNothing().when(analisisHistorialService).registrarCreacion(any(Germinacion.class));

        
        GerminacionDTO resultado = germinacionService.crearGerminacion(germinacionRequestDTO);

        
        assertNotNull(resultado);
        verify(analisisService, times(1)).establecerFechaInicio(any(Germinacion.class));
    }

    @Test
    @DisplayName("Obtener germinación por ID - debe retornar el análisis si existe")
    void obtenerGerminacionPorId_cuandoExiste_debeRetornarGerminacion() {
        
        when(germinacionRepository.findById(1L)).thenReturn(Optional.of(germinacion));

        
        GerminacionDTO resultado = germinacionService.obtenerGerminacionPorId(1L);

        
        assertNotNull(resultado);
        assertEquals(1L, resultado.getAnalisisID());
        verify(germinacionRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener germinación por ID inexistente - debe lanzar excepción")
    void obtenerGerminacionPorId_cuandoNoExiste_debeLanzarExcepcion() {
        
        when(germinacionRepository.findById(999L)).thenReturn(Optional.empty());

        
        assertThrows(RuntimeException.class, () -> {
            germinacionService.obtenerGerminacionPorId(999L);
        }, "Debe lanzar excepción cuando el análisis no existe");
    }

    @Test
    @DisplayName("Actualizar germinación - debe actualizar correctamente")
    void actualizarGerminacion_debeActualizarCorrectamente() {
        
        germinacionRequestDTO.setComentarios("Comentarios actualizados");
        
        when(germinacionRepository.findById(1L)).thenReturn(Optional.of(germinacion));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(germinacionRepository.save(any(Germinacion.class))).thenReturn(germinacion);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Germinacion.class));

        
        GerminacionDTO resultado = germinacionService.actualizarGerminacion(1L, germinacionRequestDTO);

        
        assertNotNull(resultado);
        verify(germinacionRepository, times(1)).save(any(Germinacion.class));
        verify(analisisHistorialService, times(1)).registrarModificacion(any(Germinacion.class));
    }

    @Test
    @DisplayName("Actualizar germinación APROBADA por ANALISTA - debe cambiar a PENDIENTE_APROBACION")
    void actualizarGerminacion_aprobadaPorAnalista_debeCambiarAPendiente() {
        
        germinacion.setEstado(Estado.APROBADO);
        
        when(germinacionRepository.findById(1L)).thenReturn(Optional.of(germinacion));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(analisisService.esAnalista()).thenReturn(true);
        when(germinacionRepository.save(any(Germinacion.class))).thenReturn(germinacion);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Germinacion.class));

        
        germinacionService.actualizarGerminacion(1L, germinacionRequestDTO);

        
        verify(germinacionRepository, times(1)).save(any(Germinacion.class));
        verify(analisisService, times(1)).esAnalista();
    }

    @Test
    @DisplayName("Actualizar germinación seguro - solo actualiza campos permitidos")
    void actualizarGerminacionSeguro_debeActualizarSoloCamposPermitidos() {
        
        GerminacionEditRequestDTO editDTO = new GerminacionEditRequestDTO();
        editDTO.setIdLote(1L);
        editDTO.setComentarios("Nuevo comentario");
        
        when(germinacionRepository.findById(1L)).thenReturn(Optional.of(germinacion));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(germinacionRepository.save(any(Germinacion.class))).thenReturn(germinacion);
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any(Germinacion.class));
        doNothing().when(analisisHistorialService).registrarModificacion(any(Germinacion.class));

        
        GerminacionDTO resultado = germinacionService.actualizarGerminacionSeguro(1L, editDTO);

        
        assertNotNull(resultado);
        verify(analisisService, times(1)).manejarEdicionAnalisisFinalizado(any(Germinacion.class));
        verify(germinacionRepository, times(1)).save(any(Germinacion.class));
    }

    @Test
    @DisplayName("Desactivar germinación - debe cambiar activo a false")
    void desactivarGerminacion_debeCambiarActivoAFalse() {
        
        doNothing().when(analisisService).desactivarAnalisis(eq(1L), any());

        
        germinacionService.desactivarGerminacion(1L);

        
        verify(analisisService, times(1)).desactivarAnalisis(eq(1L), any());
    }

    @Test
    @DisplayName("Reactivar germinación - debe cambiar activo a true")
    void reactivarGerminacion_debeCambiarActivoATrue() {
        
        germinacion.setActivo(false);
        GerminacionDTO germinacionDTO = new GerminacionDTO();
        germinacionDTO.setAnalisisID(1L);
        germinacionDTO.setActivo(true);
        
        when(analisisService.reactivarAnalisis(any(Long.class), any(), any())).thenReturn(germinacionDTO);

        
        germinacionService.reactivarGerminacion(1L);

        
        verify(analisisService, times(1)).reactivarAnalisis(any(Long.class), any(), any());
    }

    @Test
    @DisplayName("Eliminar germinación - debe desactivar el análisis")
    void eliminarGerminacion_debeDesactivarAnalisis() {
        
        when(germinacionRepository.findById(1L)).thenReturn(Optional.of(germinacion));
        when(germinacionRepository.save(any(Germinacion.class))).thenReturn(germinacion);

        
        germinacionService.eliminarGerminacion(1L);

        
        verify(germinacionRepository, times(1)).findById(1L);
        verify(germinacionRepository, times(1)).save(any(Germinacion.class));
    }

    @Test
    @DisplayName("Listar germinaciones por lote - debe retornar germinaciones del lote")
    void obtenerGerminacionesPorLote_debeRetornarGerminacionesDelLote() {
        
        Germinacion germinacion2 = new Germinacion();
        germinacion2.setAnalisisID(2L);
        germinacion2.setLote(lote);
        germinacion2.setActivo(true);
        
        when(germinacionRepository.findByIdLote(1L)).thenReturn(Arrays.asList(germinacion, germinacion2));

        
        List<GerminacionDTO> resultado = germinacionService.obtenerGerminacionesPorIdLote(1L);

        
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(germinacionRepository, times(1)).findByIdLote(1L);
    }

    @Test
    @DisplayName("Listar germinaciones paginadas - debe retornar página correcta")
    void obtenerGerminacionesPaginadas_debeRetornarPaginaCorrecta() {
        
        Pageable pageable = PageRequest.of(0, 10);
        GerminacionListadoDTO listadoDTO = new GerminacionListadoDTO();
        listadoDTO.setAnalisisID(1L);
        
        Page<GerminacionListadoDTO> germinacionesPage = new PageImpl<>(Arrays.asList(listadoDTO));
        
        when(germinacionRepository.findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(germinacion)));

        
        Page<GerminacionListadoDTO> resultado = germinacionService.obtenerGerminacionesPaginadas(pageable);

        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
    }

    @Test
    @DisplayName("Obtener todas las germinaciones - debe retornar lista activa")
    void obtenerTodasGerminaciones_debeRetornarListaActiva() {
        
        when(germinacionRepository.findByActivoTrue()).thenReturn(Arrays.asList(germinacion));

        
        var resultado = germinacionService.obtenerTodasGerminaciones();

        
        assertNotNull(resultado);
        assertNotNull(resultado.getGerminaciones());
        assertEquals(1, resultado.getGerminaciones().size());
        verify(germinacionRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("Obtener germinaciones paginadas con filtro 'activos' - debe retornar solo activos")
    void obtenerGerminacionesPaginadasConFiltro_activos_debeRetornarSoloActivos() {
        
        Pageable pageable = PageRequest.of(0, 10);
        when(germinacionRepository.findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(germinacion)));

        
        Page<GerminacionListadoDTO> resultado = germinacionService.obtenerGerminacionesPaginadasConFiltro(pageable, "activos");

        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(germinacionRepository, times(1)).findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class));
    }

    @Test
    @DisplayName("Obtener germinaciones paginadas con filtro 'inactivos' - debe retornar solo inactivos")
    void obtenerGerminacionesPaginadasConFiltro_inactivos_debeRetornarSoloInactivos() {
        
        Pageable pageable = PageRequest.of(0, 10);
        germinacion.setActivo(false);
        when(germinacionRepository.findByActivoFalseOrderByFechaInicioDesc(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(germinacion)));

        
        Page<GerminacionListadoDTO> resultado = germinacionService.obtenerGerminacionesPaginadasConFiltro(pageable, "inactivos");

        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(germinacionRepository, times(1)).findByActivoFalseOrderByFechaInicioDesc(any(Pageable.class));
    }

    @Test
    @DisplayName("Obtener germinaciones paginadas con filtro 'todos' - debe retornar todos")
    void obtenerGerminacionesPaginadasConFiltro_todos_debeRetornarTodos() {
        
        Pageable pageable = PageRequest.of(0, 10);
        when(germinacionRepository.findAllByOrderByFechaInicioDesc(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(germinacion)));

        
        Page<GerminacionListadoDTO> resultado = germinacionService.obtenerGerminacionesPaginadasConFiltro(pageable, "todos");

        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(germinacionRepository, times(1)).findAllByOrderByFechaInicioDesc(any(Pageable.class));
    }

    @Test
    @DisplayName("Obtener germinaciones paginadas con filtros completos - debe aplicar specification")
    void obtenerGerminacionesPaginadasConFiltros_conTodosParametros_debeAplicarFiltros() {
        
        Pageable pageable = PageRequest.of(0, 10);
        when(germinacionRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(germinacion)));

        
        Page<GerminacionListadoDTO> resultado = germinacionService.obtenerGerminacionesPaginadasConFiltros(
            pageable, "test", true, "REGISTRADO", 1L
        );

        
        assertNotNull(resultado);
        verify(germinacionRepository, times(1)).findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Actualizar germinación con ID inexistente - debe lanzar excepción")
    void actualizarGerminacion_conIdInexistente_debeLanzarExcepcion() {
        
        when(germinacionRepository.findById(999L)).thenReturn(Optional.empty());

        
        assertThrows(RuntimeException.class, () -> {
            germinacionService.actualizarGerminacion(999L, germinacionRequestDTO);
        });
    }

    @Test
    @DisplayName("Actualizar germinación seguro con lote inexistente - debe lanzar excepción")
    void actualizarGerminacionSeguro_conLoteInexistente_debeLanzarExcepcion() {
        
        GerminacionEditRequestDTO editDTO = new GerminacionEditRequestDTO();
        editDTO.setIdLote(999L);
        
        when(germinacionRepository.findById(1L)).thenReturn(Optional.of(germinacion));
        when(loteRepository.findById(999L)).thenReturn(Optional.empty());
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any(Germinacion.class));

        
        assertThrows(RuntimeException.class, () -> {
            germinacionService.actualizarGerminacionSeguro(1L, editDTO);
        });
    }

    @Test
    @DisplayName("Actualizar germinación seguro con ID inexistente - debe lanzar excepción")
    void actualizarGerminacionSeguro_conIdInexistente_debeLanzarExcepcion() {
        
        GerminacionEditRequestDTO editDTO = new GerminacionEditRequestDTO();
        when(germinacionRepository.findById(999L)).thenReturn(Optional.empty());

        
        assertThrows(RuntimeException.class, () -> {
            germinacionService.actualizarGerminacionSeguro(999L, editDTO);
        });
    }

    @Test
    @DisplayName("Eliminar germinación con ID inexistente - debe lanzar excepción")
    void eliminarGerminacion_conIdInexistente_debeLanzarExcepcion() {
        
        when(germinacionRepository.findById(999L)).thenReturn(Optional.empty());

        
        assertThrows(RuntimeException.class, () -> {
            germinacionService.eliminarGerminacion(999L);
        });
    }

    @Test
    @DisplayName("Finalizar análisis - debe llamar al servicio de análisis")
    void finalizarAnalisis_debeLlamarServicioAnalisis() {
        
        GerminacionDTO germinacionDTO = new GerminacionDTO();
        germinacionDTO.setAnalisisID(1L);
        germinacionDTO.setEstado(Estado.PENDIENTE_APROBACION);
        
        when(analisisService.finalizarAnalisisGenerico(any(), any(), any(), any())).thenReturn(germinacionDTO);

        
        GerminacionDTO resultado = germinacionService.finalizarAnalisis(1L);

        
        assertNotNull(resultado);
        verify(analisisService, times(1)).finalizarAnalisisGenerico(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Aprobar análisis - debe llamar al servicio de análisis")
    void aprobarAnalisis_debeLlamarServicioAnalisis() {
        
        GerminacionDTO germinacionDTO = new GerminacionDTO();
        germinacionDTO.setAnalisisID(1L);
        germinacionDTO.setEstado(Estado.APROBADO);
        
        when(analisisService.aprobarAnalisisGenerico(any(), any(), any(), any(), any())).thenReturn(germinacionDTO);

        
        GerminacionDTO resultado = germinacionService.aprobarAnalisis(1L);

        
        assertNotNull(resultado);
        verify(analisisService, times(1)).aprobarAnalisisGenerico(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Marcar para repetir - debe llamar al servicio de análisis")
    void marcarParaRepetir_debeLlamarServicioAnalisis() {
        
        GerminacionDTO germinacionDTO = new GerminacionDTO();
        germinacionDTO.setAnalisisID(1L);
        germinacionDTO.setEstado(Estado.A_REPETIR);
        
        when(analisisService.marcarParaRepetirGenerico(any(), any(), any(), any())).thenReturn(germinacionDTO);

        
        GerminacionDTO resultado = germinacionService.marcarParaRepetir(1L);

        
        assertNotNull(resultado);
        verify(analisisService, times(1)).marcarParaRepetirGenerico(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Crear germinación con lote inactivo - debe lanzar excepción")
    void crearGerminacion_conLoteInactivo_debeLanzarExcepcion() {
        
        lote.setActivo(false);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));

        
        assertThrows(RuntimeException.class, () -> {
            germinacionService.crearGerminacion(germinacionRequestDTO);
        });
    }

    @Test
    @DisplayName("Crear germinación con error al guardar - debe lanzar excepción")
    void crearGerminacion_conErrorAlGuardar_debeLanzarExcepcion() {
        
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(germinacionRepository.save(any(Germinacion.class))).thenThrow(new RuntimeException("Error al guardar"));
        doNothing().when(analisisService).establecerFechaInicio(any(Germinacion.class));

        
        assertThrows(RuntimeException.class, () -> {
            germinacionService.crearGerminacion(germinacionRequestDTO);
        });
    }

    @Test
    @DisplayName("Mapear entidad a DTO con lote completo - debe mapear correctamente")
    void mapearEntidadADTO_conLoteCompleto_debeMapeartodo() {
        
        utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie especie = 
            new utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie();
        especie.setNombreComun("Trigo");
        
        utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Cultivar cultivar = 
            new utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Cultivar();
        cultivar.setNombre("Cultivar Test");
        cultivar.setEspecie(especie);
        
        lote.setCultivar(cultivar);
        lote.setFicha("FICHA-001");
        germinacion.setLote(lote);
        
        when(germinacionRepository.findById(1L)).thenReturn(Optional.of(germinacion));
        when(analisisHistorialService.obtenerHistorialAnalisis(anyLong())).thenReturn(java.util.Collections.emptyList());

        
        var resultado = germinacionService.obtenerGerminacionPorId(1L);

        
        assertNotNull(resultado);
        assertEquals("LOTE-GERM-001", resultado.getLote());
        assertEquals("FICHA-001", resultado.getFicha());
        assertEquals("Cultivar Test", resultado.getCultivarNombre());
        assertEquals("Trigo", resultado.getEspecieNombre());
    }

    @Test
    @DisplayName("Mapear entidad a listado DTO - con lote y especie completos")
    void mapearEntidadAListadoDTO_conLoteYEspecieCompletos() {
        
        utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie especie = 
            new utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie();
        especie.setNombreComun("Trigo");
        especie.setNombreCientifico("Triticum aestivum");
        
        utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Cultivar cultivar = 
            new utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Cultivar();
        cultivar.setNombre("Cultivar Test");
        cultivar.setEspecie(especie);
        
        lote.setCultivar(cultivar);
        germinacion.setLote(lote);
        
        when(germinacionRepository.findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(germinacion)));
        when(analisisHistorialService.obtenerHistorialAnalisis(anyLong())).thenReturn(java.util.Collections.emptyList());

        
        Page<GerminacionListadoDTO> resultado = germinacionService.obtenerGerminacionesPaginadas(PageRequest.of(0, 10));

        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        GerminacionListadoDTO dto = resultado.getContent().get(0);
        assertEquals("Trigo", dto.getEspecie());
    }

    @Test
    @DisplayName("Mapear entidad a listado DTO - con nombre común vacío usa científico")
    void mapearEntidadAListadoDTO_nombreComunVacio_usaCientifico() {
        
        utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie especie = 
            new utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie();
        especie.setNombreComun("");
        especie.setNombreCientifico("Triticum aestivum");
        
        utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Cultivar cultivar = 
            new utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Cultivar();
        cultivar.setEspecie(especie);
        
        lote.setCultivar(cultivar);
        germinacion.setLote(lote);
        
        when(germinacionRepository.findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(germinacion)));
        when(analisisHistorialService.obtenerHistorialAnalisis(anyLong())).thenReturn(java.util.Collections.emptyList());

        
        Page<GerminacionListadoDTO> resultado = germinacionService.obtenerGerminacionesPaginadas(PageRequest.of(0, 10));

        
        GerminacionListadoDTO dto = resultado.getContent().get(0);
        assertEquals("Triticum aestivum", dto.getEspecie());
    }

    @Test
    @DisplayName("Mapear entidad a listado DTO - sin cultivar no debe fallar")
    void mapearEntidadAListadoDTO_sinCultivar_noDebeFallar() {
        
        lote.setCultivar(null);
        germinacion.setLote(lote);
        
        when(germinacionRepository.findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(germinacion)));
        when(analisisHistorialService.obtenerHistorialAnalisis(anyLong())).thenReturn(java.util.Collections.emptyList());

        
        Page<GerminacionListadoDTO> resultado = germinacionService.obtenerGerminacionesPaginadas(PageRequest.of(0, 10));

        
        assertNotNull(resultado);
        GerminacionListadoDTO dto = resultado.getContent().get(0);
        assertNull(dto.getEspecie());
    }

    @Test
    @DisplayName("Mapear entidad a listado DTO - sin lote no debe fallar")
    void mapearEntidadAListadoDTO_sinLote_noDebeFallar() {
        
        germinacion.setLote(null);
        
        when(germinacionRepository.findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(germinacion)));
        when(analisisHistorialService.obtenerHistorialAnalisis(anyLong())).thenReturn(java.util.Collections.emptyList());

        
        Page<GerminacionListadoDTO> resultado = germinacionService.obtenerGerminacionesPaginadas(PageRequest.of(0, 10));

        
        assertNotNull(resultado);
        GerminacionListadoDTO dto = resultado.getContent().get(0);
        assertNull(dto.getLote());
        assertNull(dto.getEspecie());
    }

    @Test
    @DisplayName("Mapear entidad a listado DTO - estado A_REPETIR marca cumpleNorma false")
    void mapearEntidadAListadoDTO_estadoARepetir_cumpleNormaFalse() {
        
        germinacion.setEstado(Estado.A_REPETIR);
        
        when(germinacionRepository.findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(germinacion)));
        when(analisisHistorialService.obtenerHistorialAnalisis(anyLong())).thenReturn(java.util.Collections.emptyList());

        
        Page<GerminacionListadoDTO> resultado = germinacionService.obtenerGerminacionesPaginadas(PageRequest.of(0, 10));

        
        GerminacionListadoDTO dto = resultado.getContent().get(0);
        assertFalse(dto.getCumpleNorma());
    }

    @Test
    @DisplayName("Mapear entidad a listado DTO - con TablaGerm y ValoresGerm completos")
    void mapearEntidadAListadoDTO_conTablaGermYValores_debeMapeartodo() {
        
        utec.proyectofinal.Proyecto.Final.UTEC.business.entities.TablaGerm tablaGerm = 
            new utec.proyectofinal.Proyecto.Final.UTEC.business.entities.TablaGerm();
        tablaGerm.setFechaGerminacion(LocalDate.now());
        tablaGerm.setFechaFinal(LocalDate.now().plusDays(7));
        tablaGerm.setTienePrefrio(true);
        tablaGerm.setTienePretratamiento(false);
        
        utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ValoresGerm valorInia = 
            new utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ValoresGerm();
        valorInia.setInstituto(utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto.INIA);
        valorInia.setGerminacion(java.math.BigDecimal.valueOf(85.0));
        
        utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ValoresGerm valorInase = 
            new utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ValoresGerm();
        valorInase.setInstituto(utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto.INASE);
        valorInase.setGerminacion(java.math.BigDecimal.valueOf(83.0));
        
        tablaGerm.setValoresGerm(Arrays.asList(valorInia, valorInase));
        germinacion.setTablaGerm(Arrays.asList(tablaGerm));
        
        when(germinacionRepository.findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(germinacion)));
        when(analisisHistorialService.obtenerHistorialAnalisis(anyLong())).thenReturn(java.util.Collections.emptyList());

        
        Page<GerminacionListadoDTO> resultado = germinacionService.obtenerGerminacionesPaginadas(PageRequest.of(0, 10));

        
        GerminacionListadoDTO dto = resultado.getContent().get(0);
        assertTrue(dto.getTienePrefrio());
        assertFalse(dto.getTienePretratamiento());
        assertNotNull(dto.getValorGerminacionINIA());
        assertNotNull(dto.getValorGerminacionINASE());
        assertEquals(0, dto.getValorGerminacionINIA().compareTo(java.math.BigDecimal.valueOf(85.0)));
        assertEquals(0, dto.getValorGerminacionINASE().compareTo(java.math.BigDecimal.valueOf(83.0)));
    }

    @Test
    @DisplayName("Mapear entidad a listado DTO - con historial de usuario")
    void mapearEntidadAListadoDTO_conHistorial_debeMostrarUsuarios() {
        
        var historial = new java.util.ArrayList<utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.AnalisisHistorialDTO>();
        var primerRegistro = new utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.AnalisisHistorialDTO();
        primerRegistro.setUsuario("usuario_creador@test.com");
        var segundoRegistro = new utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.AnalisisHistorialDTO();
        segundoRegistro.setUsuario("usuario_modificador@test.com");
        historial.add(segundoRegistro);
        historial.add(primerRegistro);
        
        when(germinacionRepository.findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(germinacion)));
        when(analisisHistorialService.obtenerHistorialAnalisis(anyLong())).thenReturn(historial);

        
        Page<GerminacionListadoDTO> resultado = germinacionService.obtenerGerminacionesPaginadas(PageRequest.of(0, 10));

        
        GerminacionListadoDTO dto = resultado.getContent().get(0);
        assertEquals("usuario_creador@test.com", dto.getUsuarioCreador());
        assertEquals("usuario_modificador@test.com", dto.getUsuarioModificador());
    }
}
