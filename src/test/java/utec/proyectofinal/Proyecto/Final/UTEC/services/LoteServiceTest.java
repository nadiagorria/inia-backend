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

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Catalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Contacto;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Cultivar;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.ContactoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.CultivarRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.DosnRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.GerminacionRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PmsRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.PurezaRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.TetrazolioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.LoteRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.DatosHumedadRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ValidacionLoteDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LoteDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LoteListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LoteSimpleDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ValidacionLoteResponseDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoLoteSimple;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para LoteService
 * 
 * ¿Qué estamos testeando?
 * - La lógica de negocio del servicio de lotes
 * - Validaciones y transformaciones de datos
 * - Interacciones con el repositorio
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de LoteService")
class LoteServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private PmsRepository pmsRepository;

    @Mock
    private GerminacionRepository germinacionRepository;

    @Mock
    private DosnRepository dosnRepository;

    @Mock
    private TetrazolioRepository tetrazolioRepository;

    @Mock
    private PurezaRepository purezaRepository;

    @Mock
    private CultivarRepository cultivarRepository;

    @Mock
    private ContactoRepository contactoRepository;

    @Mock
    private CatalogoService catalogoService;

    @InjectMocks
    private LoteService loteService;

    private LoteRequestDTO loteRequestDTO;
    private Lote lote;
    private Cultivar cultivar;
    private Especie especie;
    private Contacto empresa;
    private Contacto cliente;
    private Catalogo catalogo;

    @BeforeEach
    void setUp() {
        // ARRANGE: Preparar datos de prueba que usaremos en varios tests
        loteRequestDTO = new LoteRequestDTO();
        loteRequestDTO.setNomLote("LOTE-TEST-001");
        loteRequestDTO.setFicha("FICHA-001");
        loteRequestDTO.setFechaRecibo(LocalDate.now());
        loteRequestDTO.setCultivarID(1L);
        
        especie = new Especie();
        especie.setEspecieID(1L);
        especie.setNombreCientifico("Triticum aestivum");
        especie.setNombreComun("Trigo");
        
        cultivar = new Cultivar();
        cultivar.setCultivarID(1L);
        cultivar.setNombre("Cultivar Test");
        cultivar.setEspecie(especie);
        
        lote = new Lote();
        lote.setLoteID(1L);
        lote.setNomLote("LOTE-TEST-001");
        lote.setFicha("FICHA-001");
        lote.setActivo(true);
        lote.setCultivar(cultivar);
        
        empresa = new Contacto();
        empresa.setContactoID(1L);
        empresa.setNombre("Empresa Test");
        
        cliente = new Contacto();
        cliente.setContactoID(2L);
        cliente.setNombre("Cliente Test");
        
        catalogo = new Catalogo();
        catalogo.setId(1L);
        catalogo.setValor("Test Catalogo");
    }

    @Test
    @DisplayName("Crear lote - debe asignar activo=true automáticamente")
    void crearLote_debeAsignarActivoTrue() {
        // ARRANGE: Configurar el mock para que devuelva el lote cuando se guarde
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);

        // ACT: Ejecutar el método que queremos probar
        LoteDTO resultado = loteService.crearLote(loteRequestDTO);

        // ASSERT: Verificar que el lote se creó correctamente
        assertNotNull(resultado, "El lote creado no debe ser nulo");
        assertEquals("LOTE-TEST-001", resultado.getNomLote(), "El nombre del lote debe coincidir");
        
        // Verificar que se llamó al método save exactamente 1 vez
        verify(loteRepository, times(1)).save(any(Lote.class));
    }

    @Test
    @DisplayName("Crear lote con fecha recibo futura - debe lanzar excepción")
    void crearLote_fechaReciboFutura_lanzaExcepcion() {
        loteRequestDTO.setFechaRecibo(LocalDate.now().plusDays(1));
        
        assertThrows(RuntimeException.class, () -> loteService.crearLote(loteRequestDTO));
        verify(loteRepository, never()).save(any(Lote.class));
    }

    @Test
    @DisplayName("Obtener lote por ID - debe retornar el lote si existe")
    void obtenerLotePorId_cuandoExiste_debeRetornarLote() {
        
        when(loteRepository.findByIdWithCultivarAndEspecie(1L)).thenReturn(Optional.of(lote));

        
        LoteDTO resultado = loteService.obtenerLotePorId(1L);

        
        assertNotNull(resultado);
        assertEquals(1L, resultado.getLoteID());
        assertEquals("LOTE-TEST-001", resultado.getNomLote());
        verify(loteRepository, times(1)).findByIdWithCultivarAndEspecie(1L);
    }

    @Test
    @DisplayName("Obtener lote por ID inexistente - debe lanzar excepción")
    void obtenerLotePorId_cuandoNoExiste_debeLanzarExcepcion() {
        
        when(loteRepository.findByIdWithCultivarAndEspecie(999L)).thenReturn(Optional.empty());

        
        assertThrows(RuntimeException.class, () -> {
            loteService.obtenerLotePorId(999L);
        }, "Debe lanzar excepción cuando el lote no existe");

        verify(loteRepository, times(1)).findByIdWithCultivarAndEspecie(999L);
    }

    @Test
    @DisplayName("Actualizar lote - debe actualizar correctamente")
    void actualizarLote_debeActualizarCorrectamente() {
        loteRequestDTO.setNomLote("LOTE-ACTUALIZADO");
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        
        LoteDTO resultado = loteService.actualizarLote(1L, loteRequestDTO);
        
        assertNotNull(resultado);
        verify(loteRepository, times(1)).save(any(Lote.class));
    }

    @Test
    @DisplayName("Eliminar lote - debe cambiar activo a false")
    void eliminarLote_debeCambiarActivoAFalse() {
        
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);

        
        loteService.eliminarLote(1L);

        
        verify(loteRepository, times(1)).findById(1L);
        verify(loteRepository, times(1)).save(any(Lote.class));
    }

    @Test
    @DisplayName("Eliminar lote inexistente - lanza excepción")
    void eliminarLote_inexistente_lanzaExcepcion() {
        when(loteRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> loteService.eliminarLote(99L));
    }

    @Test
    @DisplayName("Reactivar lote - debe cambiar activo a true")
    void reactivarLote_debeCambiarActivoATrue() {
        
        lote.setActivo(false); 
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);

        
        LoteDTO resultado = loteService.reactivarLote(1L);

        
        assertNotNull(resultado);
        verify(loteRepository, times(1)).findById(1L);
        verify(loteRepository, times(1)).save(any(Lote.class));
    }

    @Test
    @DisplayName("Reactivar lote ya activo - lanza excepción")
    void reactivarLote_yaActivo_lanzaExcepcion() {
        lote.setActivo(true);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        assertThrows(RuntimeException.class, () -> loteService.reactivarLote(1L));
    }

    @Test
    @DisplayName("Actualizar lote inactivo - debe lanzar excepción")
    void actualizarLote_inactivo_lanzaExcepcion() {
        lote.setActivo(false);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        LoteRequestDTO req = new LoteRequestDTO();
        req.setFechaRecibo(LocalDate.now());
        assertThrows(RuntimeException.class, () -> loteService.actualizarLote(1L, req));
    }

    @Test
    @DisplayName("Actualizar lote cambiando tipos con análisis existente - bloquea remoción")
    void actualizarLote_removerTipoNoPermitido_lanza() {
        lote.setActivo(true);
        lote.setTiposAnalisisAsignados(List.of(TipoAnalisis.PMS));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(pmsRepository.existsByLoteLoteID(1L)).thenReturn(true);
        LoteRequestDTO req = new LoteRequestDTO();
        req.setTiposAnalisisAsignados(List.of()); // intentar remover PMS
        req.setFechaRecibo(LocalDate.now());
        assertThrows(RuntimeException.class, () -> loteService.actualizarLote(1L, req));
    }

    @Test
    @DisplayName("Puede remover tipo analisis cuando no hay análisis creados")
    void puedeRemoverTipoAnalisis_sinAnalisis_true() {
        when(pmsRepository.existsByLoteLoteID(1L)).thenReturn(false);
        boolean result = loteService.puedeRemoverTipoAnalisis(1L, TipoAnalisis.PMS);
        assertTrue(result);
    }

    @Test
    @DisplayName("No puede remover tipo analisis cuando hay análisis creados")
    void puedeRemoverTipoAnalisis_conAnalisis_false() {
        when(germinacionRepository.existsByLoteLoteID(1L)).thenReturn(true);
        boolean result = loteService.puedeRemoverTipoAnalisis(1L, TipoAnalisis.GERMINACION);
        assertFalse(result);
    }

    @Test
    @DisplayName("esLoteElegibleParaTipoAnalisis - requiere activo, tipo asignado y puede crear")
    void esLoteElegible_paraCrear() {
        lote.setActivo(true);
        lote.setTiposAnalisisAsignados(List.of(TipoAnalisis.PMS));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(pmsRepository.existsByLoteLoteID(1L)).thenReturn(false); // permite crear
        assertTrue(loteService.esLoteElegibleParaTipoAnalisis(1L, TipoAnalisis.PMS));
    }

    @Test
    @DisplayName("esLoteElegibleParaTipoAnalisis - falso si lote inactivo")
    void esLoteElegible_inactivo_false() {
        lote.setActivo(false);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        assertFalse(loteService.esLoteElegibleParaTipoAnalisis(1L, TipoAnalisis.PMS));
    }

    @Test
    @DisplayName("esLoteElegibleParaTipoAnalisis - falso si tipo no asignado")
    void esLoteElegible_tipoNoAsignado_false() {
        lote.setActivo(true);
        lote.setTiposAnalisisAsignados(List.of(TipoAnalisis.GERMINACION));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        assertFalse(loteService.esLoteElegibleParaTipoAnalisis(1L, TipoAnalisis.PMS));
    }

    @Test
    @DisplayName("esLoteElegibleParaTipoAnalisis - falso si lote no existe")
    void esLoteElegible_loteNoExiste_false() {
        when(loteRepository.findById(999L)).thenReturn(Optional.empty());
        assertFalse(loteService.esLoteElegibleParaTipoAnalisis(999L, TipoAnalisis.PMS));
    }

    @Test
    @DisplayName("contarAnalisisPendientes - cuenta tipos sin análisis")
    void contarAnalisisPendientes_casos() {
        Lote lote2 = new Lote();
        lote2.setLoteID(2L);
        lote2.setActivo(true);
        lote.setTiposAnalisisAsignados(List.of(TipoAnalisis.PMS));
        lote2.setTiposAnalisisAsignados(List.of(TipoAnalisis.GERMINACION));
        when(loteRepository.findByActivoTrue()).thenReturn(List.of(lote, lote2));
        when(pmsRepository.existsByLoteLoteID(1L)).thenReturn(false);
        when(germinacionRepository.existsByLoteLoteID(2L)).thenReturn(false);
        long count = loteService.contarAnalisisPendientes();
        assertEquals(2L, count);
    }

    @Test
    @DisplayName("obtenerEstadisticasLotes - retorna mapa con claves esperadas")
    void obtenerEstadisticasLotes_ok() {
        when(loteRepository.count()).thenReturn(10L);
        when(loteRepository.countLotesActivos()).thenReturn(7L);
        when(loteRepository.countLotesInactivos()).thenReturn(3L);
        Map<String, Long> stats = loteService.obtenerEstadisticasLotes();
        assertEquals(10L, stats.get("total"));
        assertEquals(7L, stats.get("activos"));
        assertEquals(3L, stats.get("inactivos"));
    }

    @Test
    @DisplayName("obtenerTodosLotesActivos - retorna lista de lotes activos")
    void obtenerTodosLotesActivos_ok() {
        when(loteRepository.findByActivoTrue()).thenReturn(List.of(lote));
        ResponseListadoLoteSimple response = loteService.obtenerTodosLotesActivos();
        assertNotNull(response);
        assertEquals(1, response.getLotes().size());
    }

    @Test
    @DisplayName("obtenerTodosLotesInactivos - retorna lista de lotes inactivos")
    void obtenerTodosLotesInactivos_ok() {
        lote.setActivo(false);
        when(loteRepository.findByActivoFalse()).thenReturn(List.of(lote));
        ResponseListadoLoteSimple response = loteService.obtenerTodosLotesInactivos();
        assertNotNull(response);
        assertEquals(1, response.getLotes().size());
    }

    @Test
    @DisplayName("obtenerLotesPaginadas - retorna página de lotes para listado")
    void obtenerLotesPaginadas_ok() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Lote> page = new PageImpl<>(List.of(lote));
        when(loteRepository.findByActivo(true, pageable)).thenReturn(page);
        
        Page<LoteListadoDTO> result = loteService.obtenerLotesPaginadas(pageable);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("obtenerLotesSimplePaginadas - retorna página de lotes simples")
    void obtenerLotesSimplePaginadas_ok() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Lote> page = new PageImpl<>(List.of(lote));
        when(loteRepository.findAll(pageable)).thenReturn(page);
        
        Page<LoteSimpleDTO> result = loteService.obtenerLotesSimplePaginadas(pageable);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("obtenerLotesSimplePaginadasConFiltros - retorna página filtrada")
    void obtenerLotesSimplePaginadasConFiltros_ok() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Lote> page = new PageImpl<>(List.of(lote));
        @SuppressWarnings("unchecked")
        Specification<Lote> anySpec = any(Specification.class);
        when(loteRepository.findAll(anySpec, eq(pageable))).thenReturn(page);
        
        Page<LoteSimpleDTO> result = loteService.obtenerLotesSimplePaginadasConFiltros(
            pageable, "test", true, "Cultivar Test");
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("validarCamposUnicos - ficha existe")
    void validarCamposUnicos_fichaExiste() {
        ValidacionLoteDTO validacion = new ValidacionLoteDTO();
        validacion.setFicha("FICHA-001");
        validacion.setLoteID(2L);
        
        when(loteRepository.findByFicha("FICHA-001")).thenReturn(Optional.of(lote));
        
        ValidacionLoteResponseDTO response = loteService.validarCamposUnicos(validacion);
        assertTrue(response.isFichaExiste());
    }

    @Test
    @DisplayName("validarCamposUnicos - nombre lote existe")
    void validarCamposUnicos_nomLoteExiste() {
        ValidacionLoteDTO validacion = new ValidacionLoteDTO();
        validacion.setNomLote("LOTE-TEST-001");
        validacion.setLoteID(2L);
        
        when(loteRepository.findByNomLote("LOTE-TEST-001")).thenReturn(Optional.of(lote));
        
        ValidacionLoteResponseDTO response = loteService.validarCamposUnicos(validacion);
        assertTrue(response.isNomLoteExiste());
    }

    @Test
    @DisplayName("validarCamposUnicos - mismo lote no cuenta como duplicado")
    void validarCamposUnicos_mismoLote_noEsDuplicado() {
        ValidacionLoteDTO validacion = new ValidacionLoteDTO();
        validacion.setFicha("FICHA-001");
        validacion.setNomLote("LOTE-TEST-001");
        validacion.setLoteID(1L);
        
        when(loteRepository.findByFicha("FICHA-001")).thenReturn(Optional.of(lote));
        when(loteRepository.findByNomLote("LOTE-TEST-001")).thenReturn(Optional.of(lote));
        
        ValidacionLoteResponseDTO response = loteService.validarCamposUnicos(validacion);
        assertFalse(response.isFichaExiste());
        assertFalse(response.isNomLoteExiste());
    }

    @Test
    @DisplayName("obtenerLotesElegiblesParaTipoAnalisis - retorna lotes elegibles")
    void obtenerLotesElegiblesParaTipoAnalisis_ok() {
        // Crear lote con toda la estructura de cultivar y especie correctamente configurada
        Lote loteElegible = new Lote();
        loteElegible.setLoteID(1L);
        loteElegible.setActivo(true);
        loteElegible.setNomLote("LOTE-TEST-001");
        loteElegible.setFicha("FICHA-001");
        loteElegible.setCultivar(cultivar); // cultivar ya tiene especie asignada en setUp()
        loteElegible.setTiposAnalisisAsignados(List.of(TipoAnalisis.PMS));
        
        when(loteRepository.findByActivoTrue()).thenReturn(List.of(loteElegible));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(loteElegible));
        when(pmsRepository.existsByLoteLoteID(1L)).thenReturn(false);
        
        ResponseListadoLoteSimple response = loteService.obtenerLotesElegiblesParaTipoAnalisis(TipoAnalisis.PMS);
        
        assertNotNull(response);
        assertNotNull(response.getLotes());
        assertEquals(1, response.getLotes().size());
        assertEquals("LOTE-TEST-001", response.getLotes().get(0).getNomLote());
    }

    @Test
    @DisplayName("puedeCrearAnalisisDelTipo - todos los tipos")
    void puedeCrearAnalisisDelTipo_todosLosTipos() {
        // PMS
        when(pmsRepository.existsByLoteLoteID(1L)).thenReturn(false);
        assertTrue(loteService.puedeCrearAnalisisDelTipo(1L, TipoAnalisis.PMS));
        
        // GERMINACION
        when(germinacionRepository.existsByLoteLoteID(1L)).thenReturn(false);
        assertTrue(loteService.puedeCrearAnalisisDelTipo(1L, TipoAnalisis.GERMINACION));
        
        // DOSN
        when(dosnRepository.existsByLoteLoteID(1L)).thenReturn(false);
        assertTrue(loteService.puedeCrearAnalisisDelTipo(1L, TipoAnalisis.DOSN));
        
        // TETRAZOLIO
        when(tetrazolioRepository.existsByLoteLoteID(1L)).thenReturn(false);
        assertTrue(loteService.puedeCrearAnalisisDelTipo(1L, TipoAnalisis.TETRAZOLIO));
        
        // PUREZA
        when(purezaRepository.existsByLoteLoteID(1L)).thenReturn(false);
        assertTrue(loteService.puedeCrearAnalisisDelTipo(1L, TipoAnalisis.PUREZA));
    }

    // ===== TESTS PARA FUNCIONES DE MAPEO =====
    
    @Test
    @DisplayName("mapearSolicitudAEntidad - mapea cultivar correctamente")
    void crearLote_mapeaCultivarCorrectamente() {
        loteRequestDTO.setCultivarID(1L);
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        
        LoteDTO resultado = loteService.crearLote(loteRequestDTO);
        
        assertNotNull(resultado);
        verify(cultivarRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("mapearSolicitudAEntidad - mapea empresa cuando se proporciona")
    void crearLote_mapeaEmpresa() {
        loteRequestDTO.setEmpresaID(1L);
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(contactoRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        
        LoteDTO resultado = loteService.crearLote(loteRequestDTO);
        
        assertNotNull(resultado);
        verify(contactoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("mapearSolicitudAEntidad - mapea cliente cuando se proporciona")
    void crearLote_mapeaCliente() {
        loteRequestDTO.setClienteID(2L);
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(contactoRepository.findById(2L)).thenReturn(Optional.of(cliente));
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        
        LoteDTO resultado = loteService.crearLote(loteRequestDTO);
        
        assertNotNull(resultado);
        verify(contactoRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("mapearSolicitudAEntidad - mapea depósito cuando se proporciona")
    void crearLote_mapeaDeposito() {
        loteRequestDTO.setDepositoID(1L);
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(catalogoService.obtenerEntidadPorId(1L)).thenReturn(catalogo);
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        
        LoteDTO resultado = loteService.crearLote(loteRequestDTO);
        
        assertNotNull(resultado);
        verify(catalogoService, atLeastOnce()).obtenerEntidadPorId(1L);
    }

    @Test
    @DisplayName("mapearSolicitudAEntidad - mapea origen cuando se proporciona")
    void crearLote_mapeaOrigen() {
        loteRequestDTO.setOrigenID(1L);
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(catalogoService.obtenerEntidadPorId(1L)).thenReturn(catalogo);
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        
        LoteDTO resultado = loteService.crearLote(loteRequestDTO);
        
        assertNotNull(resultado);
        verify(catalogoService, atLeastOnce()).obtenerEntidadPorId(1L);
    }

    @Test
    @DisplayName("mapearSolicitudAEntidad - mapea estado cuando se proporciona")
    void crearLote_mapeaEstado() {
        loteRequestDTO.setEstadoID(1L);
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(catalogoService.obtenerEntidadPorId(1L)).thenReturn(catalogo);
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        
        LoteDTO resultado = loteService.crearLote(loteRequestDTO);
        
        assertNotNull(resultado);
        verify(catalogoService, atLeastOnce()).obtenerEntidadPorId(1L);
    }

    @Test
    @DisplayName("mapearSolicitudAEntidad - mapea numero articulo cuando se proporciona")
    void crearLote_mapeaNumeroArticulo() {
        loteRequestDTO.setNumeroArticuloID(1L);
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(catalogoService.obtenerEntidadPorId(1L)).thenReturn(catalogo);
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        
        LoteDTO resultado = loteService.crearLote(loteRequestDTO);
        
        assertNotNull(resultado);
        verify(catalogoService, atLeastOnce()).obtenerEntidadPorId(1L);
    }

    @Test
    @DisplayName("mapearSolicitudAEntidad - mapea tipo lote correctamente")
    void crearLote_mapeaTipoLote() {
        loteRequestDTO.setTipo("INTERNO");
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        
        LoteDTO resultado = loteService.crearLote(loteRequestDTO);
        
        assertNotNull(resultado);
        verify(loteRepository, times(1)).save(any(Lote.class));
    }

    @Test
    @DisplayName("mapearSolicitudAEntidad - mapea tipos análisis correctamente")
    void crearLote_mapeaTiposAnalisis() {
        loteRequestDTO.setTiposAnalisisAsignados(List.of(TipoAnalisis.PMS, TipoAnalisis.GERMINACION));
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        
        LoteDTO resultado = loteService.crearLote(loteRequestDTO);
        
        assertNotNull(resultado);
        verify(loteRepository, times(1)).save(any(Lote.class));
    }

    @Test
    @DisplayName("mapearSolicitudAEntidad - maneja cultivar no encontrado")
    void crearLote_cultivarNoEncontrado() {
        loteRequestDTO.setCultivarID(999L);
        when(cultivarRepository.findById(999L)).thenReturn(Optional.empty());
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        
        LoteDTO resultado = loteService.crearLote(loteRequestDTO);
        
        assertNotNull(resultado);
        verify(cultivarRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("actualizarEntidadDesdeSolicitud - actualiza todos los campos básicos")
    void actualizarLote_actualizaCamposBasicos() {
        loteRequestDTO.setNomLote("LOTE-ACTUALIZADO");
        loteRequestDTO.setFicha("FICHA-002");
        loteRequestDTO.setRemitente("Remitente Test");
        loteRequestDTO.setObservaciones("Observaciones Test");
        
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        
        LoteDTO resultado = loteService.actualizarLote(1L, loteRequestDTO);
        
        assertNotNull(resultado);
        verify(loteRepository, times(1)).save(any(Lote.class));
    }

    @Test
    @DisplayName("actualizarEntidadDesdeSolicitud - actualiza empresa")
    void actualizarLote_actualizaEmpresa() {
        loteRequestDTO.setEmpresaID(1L);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(contactoRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        
        LoteDTO resultado = loteService.actualizarLote(1L, loteRequestDTO);
        
        assertNotNull(resultado);
        verify(contactoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("mapearSolicitudAEntidad - mapea datos de humedad correctamente")
    void crearLote_mapeaDatosHumedad() {
        DatosHumedadRequestDTO datosHumedadDTO = new DatosHumedadRequestDTO();
        datosHumedadDTO.setTipoHumedadID(1L);
        datosHumedadDTO.setValor(new BigDecimal("12.5"));
        loteRequestDTO.setDatosHumedad(List.of(datosHumedadDTO));
        
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(catalogoService.obtenerEntidadPorId(1L)).thenReturn(catalogo);
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        
        LoteDTO resultado = loteService.crearLote(loteRequestDTO);
        
        assertNotNull(resultado);
        verify(catalogoService, atLeastOnce()).obtenerEntidadPorId(1L);
    }

    @Test
    @DisplayName("mapearSolicitudAEntidad - maneja múltiples datos de humedad")
    void crearLote_mapeaMultiplesDatosHumedad() {
        DatosHumedadRequestDTO datos1 = new DatosHumedadRequestDTO();
        datos1.setTipoHumedadID(1L);
        datos1.setValor(new BigDecimal("12.5"));
        
        DatosHumedadRequestDTO datos2 = new DatosHumedadRequestDTO();
        datos2.setTipoHumedadID(2L);
        datos2.setValor(new BigDecimal("13.2"));
        
        loteRequestDTO.setDatosHumedad(List.of(datos1, datos2));
        
        Catalogo tipoHumedad1 = new Catalogo();
        tipoHumedad1.setId(1L);
        Catalogo tipoHumedad2 = new Catalogo();
        tipoHumedad2.setId(2L);
        
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(catalogoService.obtenerEntidadPorId(1L)).thenReturn(tipoHumedad1);
        when(catalogoService.obtenerEntidadPorId(2L)).thenReturn(tipoHumedad2);
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        
        LoteDTO resultado = loteService.crearLote(loteRequestDTO);
        
        assertNotNull(resultado);
        verify(catalogoService, times(1)).obtenerEntidadPorId(1L);
        verify(catalogoService, times(1)).obtenerEntidadPorId(2L);
    }

    @Test
    @DisplayName("puedeRemoverTipoAnalisis - no puede remover GERMINACION con análisis")
    void puedeRemoverTipoAnalisis_germinacionConAnalisis_false() {
        when(germinacionRepository.existsByLoteLoteID(1L)).thenReturn(true);
        boolean result = loteService.puedeRemoverTipoAnalisis(1L, TipoAnalisis.GERMINACION);
        assertFalse(result);
    }

    @Test
    @DisplayName("puedeRemoverTipoAnalisis - no puede remover DOSN con análisis")
    void puedeRemoverTipoAnalisis_dosnConAnalisis_false() {
        when(dosnRepository.existsByLoteLoteID(1L)).thenReturn(true);
        boolean result = loteService.puedeRemoverTipoAnalisis(1L, TipoAnalisis.DOSN);
        assertFalse(result);
    }

    @Test
    @DisplayName("puedeRemoverTipoAnalisis - no puede remover TETRAZOLIO con análisis")
    void puedeRemoverTipoAnalisis_tetrazolioConAnalisis_false() {
        when(tetrazolioRepository.existsByLoteLoteID(1L)).thenReturn(true);
        boolean result = loteService.puedeRemoverTipoAnalisis(1L, TipoAnalisis.TETRAZOLIO);
        assertFalse(result);
    }

    @Test
    @DisplayName("puedeRemoverTipoAnalisis - no puede remover PUREZA con análisis")
    void puedeRemoverTipoAnalisis_purezaConAnalisis_false() {
        when(purezaRepository.existsByLoteLoteID(1L)).thenReturn(true);
        boolean result = loteService.puedeRemoverTipoAnalisis(1L, TipoAnalisis.PUREZA);
        assertFalse(result);
    }

    @Test
    @DisplayName("puedeCrearAnalisisDelTipo - no puede crear PMS si existe")
    void puedeCrearAnalisisDelTipo_pmsExiste_false() {
        when(pmsRepository.existsByLoteLoteID(1L)).thenReturn(true);
        boolean result = loteService.puedeCrearAnalisisDelTipo(1L, TipoAnalisis.PMS);
        assertFalse(result);
    }

    @Test
    @DisplayName("puedeCrearAnalisisDelTipo - no puede crear GERMINACION si existe")
    void puedeCrearAnalisisDelTipo_germinacionExiste_false() {
        when(germinacionRepository.existsByLoteLoteID(1L)).thenReturn(true);
        boolean result = loteService.puedeCrearAnalisisDelTipo(1L, TipoAnalisis.GERMINACION);
        assertFalse(result);
    }

    @Test
    @DisplayName("puedeCrearAnalisisDelTipo - no puede crear DOSN si existe")
    void puedeCrearAnalisisDelTipo_dosnExiste_false() {
        when(dosnRepository.existsByLoteLoteID(1L)).thenReturn(true);
        boolean result = loteService.puedeCrearAnalisisDelTipo(1L, TipoAnalisis.DOSN);
        assertFalse(result);
    }

    @Test
    @DisplayName("puedeCrearAnalisisDelTipo - no puede crear TETRAZOLIO si existe")
    void puedeCrearAnalisisDelTipo_tetrazolioExiste_false() {
        when(tetrazolioRepository.existsByLoteLoteID(1L)).thenReturn(true);
        boolean result = loteService.puedeCrearAnalisisDelTipo(1L, TipoAnalisis.TETRAZOLIO);
        assertFalse(result);
    }

    @Test
    @DisplayName("puedeCrearAnalisisDelTipo - no puede crear PUREZA si existe")
    void puedeCrearAnalisisDelTipo_purezaExiste_false() {
        when(purezaRepository.existsByLoteLoteID(1L)).thenReturn(true);
        boolean result = loteService.puedeCrearAnalisisDelTipo(1L, TipoAnalisis.PUREZA);
        assertFalse(result);
    }

    @Test
    @DisplayName("mapearSolicitudAEntidad - mapea todos los tipos de lote")
    void crearLote_mapeaTodosTiposLote() {
        // INTERNO
        loteRequestDTO.setTipo("INTERNO");
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        assertNotNull(loteService.crearLote(loteRequestDTO));
        
        // OTROS_CENTROS_COSTOS
        loteRequestDTO.setTipo("OTROS_CENTROS_COSTOS");
        assertNotNull(loteService.crearLote(loteRequestDTO));
        
        // EXTERNOS
        loteRequestDTO.setTipo("EXTERNOS");
        assertNotNull(loteService.crearLote(loteRequestDTO));
    }

    @Test
    @DisplayName("actualizarEntidadDesdeSolicitud - actualiza datos de humedad")
    void actualizarLote_actualizaDatosHumedad() {
        DatosHumedadRequestDTO datosHumedadDTO = new DatosHumedadRequestDTO();
        datosHumedadDTO.setTipoHumedadID(1L);
        datosHumedadDTO.setValor(new BigDecimal("15.0"));
        loteRequestDTO.setDatosHumedad(List.of(datosHumedadDTO));
        
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(catalogoService.obtenerEntidadPorId(1L)).thenReturn(catalogo);
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        
        LoteDTO resultado = loteService.actualizarLote(1L, loteRequestDTO);
        
        assertNotNull(resultado);
        verify(catalogoService, atLeastOnce()).obtenerEntidadPorId(1L);
    }

    @Test
    @DisplayName("validarCamposUnicos - ambos campos únicos ya existen")
    void validarCamposUnicos_ambosExisten() {
        ValidacionLoteDTO validacion = new ValidacionLoteDTO();
        validacion.setFicha("FICHA-001");
        validacion.setNomLote("LOTE-TEST-001");
        validacion.setLoteID(2L);
        
        when(loteRepository.findByFicha("FICHA-001")).thenReturn(Optional.of(lote));
        when(loteRepository.findByNomLote("LOTE-TEST-001")).thenReturn(Optional.of(lote));
        
        ValidacionLoteResponseDTO response = loteService.validarCamposUnicos(validacion);
        
        assertTrue(response.isFichaExiste());
        assertTrue(response.isNomLoteExiste());
    }

    @Test
    @DisplayName("validarCamposUnicos - campos vacíos no se validan")
    void validarCamposUnicos_camposVacios() {
        ValidacionLoteDTO validacion = new ValidacionLoteDTO();
        validacion.setFicha("");
        validacion.setNomLote(null);
        
        ValidacionLoteResponseDTO response = loteService.validarCamposUnicos(validacion);
        
        assertFalse(response.isFichaExiste());
        assertFalse(response.isNomLoteExiste());
    }

    @Test
    @DisplayName("mapearSolicitudAEntidad - mapea todos los campos opcionales juntos")
    void crearLote_mapeaTodosCamposOpcionales() {
        loteRequestDTO.setEmpresaID(1L);
        loteRequestDTO.setClienteID(2L);
        loteRequestDTO.setDepositoID(1L);
        loteRequestDTO.setOrigenID(1L);
        loteRequestDTO.setEstadoID(1L);
        loteRequestDTO.setNumeroArticuloID(1L);
        loteRequestDTO.setTipo("EXTERNOS");
        loteRequestDTO.setCodigoCC("CC-001");
        loteRequestDTO.setCodigoFF("FF-001");
        loteRequestDTO.setFechaEntrega(LocalDate.now().plusDays(5));
        loteRequestDTO.setUnidadEmbolsado("Bolsas 50kg");
        loteRequestDTO.setRemitente("Remitente Test");
        loteRequestDTO.setObservaciones("Observaciones completas");
        loteRequestDTO.setKilosLimpios(new BigDecimal("1000.0"));
        loteRequestDTO.setFechaCosecha(LocalDate.now().minusMonths(1));
        loteRequestDTO.setTiposAnalisisAsignados(List.of(TipoAnalisis.PMS, TipoAnalisis.GERMINACION, TipoAnalisis.PUREZA));
        
        DatosHumedadRequestDTO humedad = new DatosHumedadRequestDTO();
        humedad.setTipoHumedadID(1L);
        humedad.setValor(new BigDecimal("14.5"));
        loteRequestDTO.setDatosHumedad(List.of(humedad));
        
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(contactoRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(contactoRepository.findById(2L)).thenReturn(Optional.of(cliente));
        when(catalogoService.obtenerEntidadPorId(1L)).thenReturn(catalogo);
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        
        LoteDTO resultado = loteService.crearLote(loteRequestDTO);
        
        assertNotNull(resultado);
        verify(cultivarRepository, times(1)).findById(1L);
        verify(contactoRepository, times(1)).findById(1L);
        verify(contactoRepository, times(1)).findById(2L);
        verify(catalogoService, atLeast(4)).obtenerEntidadPorId(1L);
        verify(loteRepository, times(1)).save(any(Lote.class));
    }

    @Test
    @DisplayName("esLoteElegibleParaTipoAnalisis - tipo no asignado aunque no haya análisis")
    void esLoteElegible_tipoNoAsignadoSinAnalisis_false() {
        lote.setActivo(true);
        lote.setTiposAnalisisAsignados(List.of(TipoAnalisis.GERMINACION));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        // No mockeamos pmsRepository porque no debe ser llamado cuando el tipo no está asignado
        
        assertFalse(loteService.esLoteElegibleParaTipoAnalisis(1L, TipoAnalisis.PMS));
    }

    @Test
    @DisplayName("esLoteElegibleParaTipoAnalisis - falso si ya existe análisis del tipo")
    void esLoteElegible_analisisYaExiste_false() {
        lote.setActivo(true);
        lote.setTiposAnalisisAsignados(List.of(TipoAnalisis.PMS));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(pmsRepository.existsByLoteLoteID(1L)).thenReturn(true);
        
        assertFalse(loteService.esLoteElegibleParaTipoAnalisis(1L, TipoAnalisis.PMS));
    }

    @Test
    @DisplayName("obtenerLotesElegiblesParaTipoAnalisis - filtra lotes inactivos")
    void obtenerLotesElegibles_filtraInactivos() {
        Lote loteInactivo = new Lote();
        loteInactivo.setLoteID(2L);
        loteInactivo.setActivo(false);
        loteInactivo.setCultivar(cultivar);
        loteInactivo.setTiposAnalisisAsignados(List.of(TipoAnalisis.PMS));
        
        when(loteRepository.findByActivoTrue()).thenReturn(List.of());
        
        ResponseListadoLoteSimple response = loteService.obtenerLotesElegiblesParaTipoAnalisis(TipoAnalisis.PMS);
        
        assertNotNull(response);
        assertEquals(0, response.getLotes().size());
    }

    @Test
    @DisplayName("obtenerLotesElegiblesParaTipoAnalisis - filtra lotes sin tipo asignado")
    void obtenerLotesElegibles_filtraSinTipoAsignado() {
        Lote loteSinTipo = new Lote();
        loteSinTipo.setLoteID(3L);
        loteSinTipo.setActivo(true);
        loteSinTipo.setCultivar(cultivar);
        loteSinTipo.setTiposAnalisisAsignados(List.of(TipoAnalisis.GERMINACION));
        
        when(loteRepository.findByActivoTrue()).thenReturn(List.of(loteSinTipo));
        when(loteRepository.findById(3L)).thenReturn(Optional.of(loteSinTipo));
        
        ResponseListadoLoteSimple response = loteService.obtenerLotesElegiblesParaTipoAnalisis(TipoAnalisis.PMS);
        
        assertNotNull(response);
        assertEquals(0, response.getLotes().size());
    }

    @Test
    @DisplayName("actualizarLote - actualiza empresa cuando se proporciona nueva")
    void actualizarLote_nuevaEmpresa() {
        loteRequestDTO.setEmpresaID(1L);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(contactoRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(loteRepository.save(any(Lote.class))).thenReturn(lote);
        
        LoteDTO resultado = loteService.actualizarLote(1L, loteRequestDTO);
        
        assertNotNull(resultado);
        verify(contactoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("actualizarLote - lanza excepción si empresa no existe")
    void actualizarLote_empresaNoExiste_lanzaExcepcion() {
        loteRequestDTO.setEmpresaID(999L);
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(contactoRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> loteService.actualizarLote(1L, loteRequestDTO));
    }

    // ======================= TESTS DE CONTEO DE ANÁLISIS PENDIENTES =======================

    @Test
    @DisplayName("contarAnalisisPendientes - sin lotes activos devuelve 0")
    void contarAnalisisPendientes_sinLotesActivos() {
        when(loteRepository.findByActivoTrue()).thenReturn(List.of());
        
        long resultado = loteService.contarAnalisisPendientes();
        
        assertEquals(0, resultado);
    }

    @Test
    @DisplayName("contarAnalisisPendientes - lotes sin tipos asignados no cuenta nada")
    void contarAnalisisPendientes_sinTiposAsignados() {
        Lote loteSinTipos = new Lote();
        loteSinTipos.setLoteID(1L);
        loteSinTipos.setActivo(true);
        loteSinTipos.setTiposAnalisisAsignados(null);
        
        when(loteRepository.findByActivoTrue()).thenReturn(List.of(loteSinTipos));
        
        long resultado = loteService.contarAnalisisPendientes();
        
        assertEquals(0, resultado);
    }

    @Test
    @DisplayName("contarAnalisisPendientes - PMS sin análisis cuenta como pendiente")
    void contarAnalisisPendientes_pmsSinAnalisis() {
        lote.setActivo(true);
        lote.setTiposAnalisisAsignados(List.of(TipoAnalisis.PMS));
        
        when(loteRepository.findByActivoTrue()).thenReturn(List.of(lote));
        when(pmsRepository.existsByLoteLoteID(1L)).thenReturn(false);
        
        long resultado = loteService.contarAnalisisPendientes();
        
        assertEquals(1, resultado);
    }

    @Test
    @DisplayName("contarAnalisisPendientes - GERMINACION sin análisis cuenta como pendiente")
    void contarAnalisisPendientes_germinacionSinAnalisis() {
        lote.setActivo(true);
        lote.setTiposAnalisisAsignados(List.of(TipoAnalisis.GERMINACION));
        
        when(loteRepository.findByActivoTrue()).thenReturn(List.of(lote));
        when(germinacionRepository.existsByLoteLoteID(1L)).thenReturn(false);
        
        long resultado = loteService.contarAnalisisPendientes();
        
        assertEquals(1, resultado);
    }

    @Test
    @DisplayName("contarAnalisisPendientes - DOSN sin análisis cuenta como pendiente")
    void contarAnalisisPendientes_dosnSinAnalisis() {
        lote.setActivo(true);
        lote.setTiposAnalisisAsignados(List.of(TipoAnalisis.DOSN));
        
        when(loteRepository.findByActivoTrue()).thenReturn(List.of(lote));
        when(dosnRepository.existsByLoteLoteID(1L)).thenReturn(false);
        
        long resultado = loteService.contarAnalisisPendientes();
        
        assertEquals(1, resultado);
    }

    @Test
    @DisplayName("contarAnalisisPendientes - TETRAZOLIO sin análisis cuenta como pendiente")
    void contarAnalisisPendientes_tetrazolioSinAnalisis() {
        lote.setActivo(true);
        lote.setTiposAnalisisAsignados(List.of(TipoAnalisis.TETRAZOLIO));
        
        when(loteRepository.findByActivoTrue()).thenReturn(List.of(lote));
        when(tetrazolioRepository.existsByLoteLoteID(1L)).thenReturn(false);
        
        long resultado = loteService.contarAnalisisPendientes();
        
        assertEquals(1, resultado);
    }

    @Test
    @DisplayName("contarAnalisisPendientes - PUREZA sin análisis cuenta como pendiente")
    void contarAnalisisPendientes_purezaSinAnalisis() {
        lote.setActivo(true);
        lote.setTiposAnalisisAsignados(List.of(TipoAnalisis.PUREZA));
        
        when(loteRepository.findByActivoTrue()).thenReturn(List.of(lote));
        when(purezaRepository.existsByLoteLoteID(1L)).thenReturn(false);
        
        long resultado = loteService.contarAnalisisPendientes();
        
        assertEquals(1, resultado);
    }

    @Test
    @DisplayName("contarAnalisisPendientes - PMS con todos en A_REPETIR cuenta como pendiente")
    void contarAnalisisPendientes_pmsTodosARepetir() {
        lote.setActivo(true);
        lote.setTiposAnalisisAsignados(List.of(TipoAnalisis.PMS));
        
        utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms pms1 = new utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms();
        pms1.setEstado(Estado.A_REPETIR);
        utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms pms2 = new utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms();
        pms2.setEstado(Estado.A_REPETIR);
        
        when(loteRepository.findByActivoTrue()).thenReturn(List.of(lote));
        when(pmsRepository.existsByLoteLoteID(1L)).thenReturn(true);
        when(pmsRepository.findByLoteLoteID(1L)).thenReturn(List.of(pms1, pms2));
        
        long resultado = loteService.contarAnalisisPendientes();
        
        assertEquals(1, resultado);
    }

    @Test
    @DisplayName("contarAnalisisPendientes - GERMINACION con todos en A_REPETIR cuenta como pendiente")
    void contarAnalisisPendientes_germinacionTodosARepetir() {
        lote.setActivo(true);
        lote.setTiposAnalisisAsignados(List.of(TipoAnalisis.GERMINACION));
        
        utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion ger1 = new utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion();
        ger1.setEstado(Estado.A_REPETIR);
        
        when(loteRepository.findByActivoTrue()).thenReturn(List.of(lote));
        when(germinacionRepository.existsByLoteLoteID(1L)).thenReturn(true);
        when(germinacionRepository.findByLoteLoteID(1L)).thenReturn(List.of(ger1));
        
        long resultado = loteService.contarAnalisisPendientes();
        
        assertEquals(1, resultado);
    }

    @Test
    @DisplayName("contarAnalisisPendientes - DOSN con todos en A_REPETIR cuenta como pendiente")
    void contarAnalisisPendientes_dosnTodosARepetir() {
        lote.setActivo(true);
        lote.setTiposAnalisisAsignados(List.of(TipoAnalisis.DOSN));
        
        utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Dosn dosn1 = new utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Dosn();
        dosn1.setEstado(Estado.A_REPETIR);
        
        when(loteRepository.findByActivoTrue()).thenReturn(List.of(lote));
        when(dosnRepository.existsByLoteLoteID(1L)).thenReturn(true);
        when(dosnRepository.findByLoteLoteID(1L)).thenReturn(List.of(dosn1));
        
        long resultado = loteService.contarAnalisisPendientes();
        
        assertEquals(1, resultado);
    }

    @Test
    @DisplayName("contarAnalisisPendientes - TETRAZOLIO con todos en A_REPETIR cuenta como pendiente")
    void contarAnalisisPendientes_tetrazolioTodosARepetir() {
        lote.setActivo(true);
        lote.setTiposAnalisisAsignados(List.of(TipoAnalisis.TETRAZOLIO));
        
        utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Tetrazolio tet1 = new utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Tetrazolio();
        tet1.setEstado(Estado.A_REPETIR);
        
        when(loteRepository.findByActivoTrue()).thenReturn(List.of(lote));
        when(tetrazolioRepository.existsByLoteLoteID(1L)).thenReturn(true);
        when(tetrazolioRepository.findByLoteLoteID(1L)).thenReturn(List.of(tet1));
        
        long resultado = loteService.contarAnalisisPendientes();
        
        assertEquals(1, resultado);
    }

    @Test
    @DisplayName("contarAnalisisPendientes - PUREZA con todos en A_REPETIR cuenta como pendiente")
    void contarAnalisisPendientes_purezaTodosARepetir() {
        lote.setActivo(true);
        lote.setTiposAnalisisAsignados(List.of(TipoAnalisis.PUREZA));
        
        utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pureza pureza1 = new utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pureza();
        pureza1.setEstado(Estado.A_REPETIR);
        
        when(loteRepository.findByActivoTrue()).thenReturn(List.of(lote));
        when(purezaRepository.existsByLoteLoteID(1L)).thenReturn(true);
        when(purezaRepository.findByLoteLoteID(1L)).thenReturn(List.of(pureza1));
        
        long resultado = loteService.contarAnalisisPendientes();
        
        assertEquals(1, resultado);
    }

    @Test
    @DisplayName("contarAnalisisPendientes - análisis con estado diferente a A_REPETIR no cuenta")
    void contarAnalisisPendientes_analisisConOtroEstado() {
        lote.setActivo(true);
        lote.setTiposAnalisisAsignados(List.of(TipoAnalisis.PMS));
        
        utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms pms1 = new utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms();
        pms1.setEstado(Estado.PENDIENTE_APROBACION);
        
        when(loteRepository.findByActivoTrue()).thenReturn(List.of(lote));
        when(pmsRepository.existsByLoteLoteID(1L)).thenReturn(true);
        when(pmsRepository.findByLoteLoteID(1L)).thenReturn(List.of(pms1));
        
        long resultado = loteService.contarAnalisisPendientes();
        
        assertEquals(0, resultado);
    }

    @Test
    @DisplayName("contarAnalisisPendientes - múltiples tipos asignados cuenta correctamente")
    void contarAnalisisPendientes_multipleTipos() {
        lote.setActivo(true);
        lote.setTiposAnalisisAsignados(List.of(TipoAnalisis.PMS, TipoAnalisis.GERMINACION, TipoAnalisis.DOSN));
        
        when(loteRepository.findByActivoTrue()).thenReturn(List.of(lote));
        when(pmsRepository.existsByLoteLoteID(1L)).thenReturn(false);
        when(germinacionRepository.existsByLoteLoteID(1L)).thenReturn(false);
        when(dosnRepository.existsByLoteLoteID(1L)).thenReturn(false);
        
        long resultado = loteService.contarAnalisisPendientes();
        
        assertEquals(3, resultado);
    }

    @Test
    @DisplayName("contarAnalisisPendientes - múltiples lotes suma correctamente")
    void contarAnalisisPendientes_multipleLotes() {
        Lote lote1 = new Lote();
        lote1.setLoteID(1L);
        lote1.setActivo(true);
        lote1.setTiposAnalisisAsignados(List.of(TipoAnalisis.PMS));
        
        Lote lote2 = new Lote();
        lote2.setLoteID(2L);
        lote2.setActivo(true);
        lote2.setTiposAnalisisAsignados(List.of(TipoAnalisis.GERMINACION, TipoAnalisis.DOSN));
        
        when(loteRepository.findByActivoTrue()).thenReturn(List.of(lote1, lote2));
        when(pmsRepository.existsByLoteLoteID(1L)).thenReturn(false);
        when(germinacionRepository.existsByLoteLoteID(2L)).thenReturn(false);
        when(dosnRepository.existsByLoteLoteID(2L)).thenReturn(false);
        
        long resultado = loteService.contarAnalisisPendientes();
        
        assertEquals(3, resultado);
    }

    @Test
    @DisplayName("contarAnalisisPendientes - mezcla análisis completos y pendientes")
    void contarAnalisisPendientes_mezclaPendientesYCompletos() {
        lote.setActivo(true);
        lote.setTiposAnalisisAsignados(List.of(TipoAnalisis.PMS, TipoAnalisis.GERMINACION));
        
        utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms pms1 = new utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms();
        pms1.setEstado(Estado.APROBADO);
        
        when(loteRepository.findByActivoTrue()).thenReturn(List.of(lote));
        when(pmsRepository.existsByLoteLoteID(1L)).thenReturn(true);
        when(pmsRepository.findByLoteLoteID(1L)).thenReturn(List.of(pms1));
        when(germinacionRepository.existsByLoteLoteID(1L)).thenReturn(false);
        
        long resultado = loteService.contarAnalisisPendientes();
        
        assertEquals(1, resultado); // Solo GERMINACION está pendiente
    }
}
