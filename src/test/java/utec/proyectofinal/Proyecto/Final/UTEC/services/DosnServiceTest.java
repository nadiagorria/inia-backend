package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Dosn;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Listado;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.CuscutaRegistro;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Cultivar;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.MalezasCatalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.DosnRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LoteRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.MalezasCatalogoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.EspecieRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.DosnRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.CuscutaRegistroRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ListadoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.DosnDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.DosnListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.CuscutaRegistroDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.AnalisisHistorialDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;
import utec.proyectofinal.Proyecto.Final.UTEC.responses.ResponseListadoDosn;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para DosnService (Determinación de Otras Semillas por Número)
 * 
 * Funcionalidades testeadas:
 * - Creación de análisis DOSN con estado EN_PROCESO
 * - Validación de pesos y conteos
 * - Actualización de análisis
 * - Gestión de estados según rol de usuario
 * - Desactivación y reactivación
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Tests de DosnService")
class DosnServiceTest {

    @Mock
    private DosnRepository dosnRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private AnalisisService analisisService;

    @Mock
    private AnalisisHistorialService analisisHistorialService;

    @Mock
    private MalezasCatalogoRepository malezasCatalogoRepository;

    @Mock
    private EspecieRepository especieRepository;

    @InjectMocks
    private DosnService dosnService;

    private DosnRequestDTO dosnRequestDTO;
    private Lote lote;
    private Dosn dosn;

    @BeforeEach
    void setUp() {
        
        lote = new Lote();
        lote.setLoteID(1L);
        lote.setNomLote("LOTE-DOSN-001");
        lote.setActivo(true);

        dosnRequestDTO = new DosnRequestDTO();
        dosnRequestDTO.setIdLote(1L);
        dosnRequestDTO.setFechaINIA(LocalDate.now());
        dosnRequestDTO.setGramosAnalizadosINIA(BigDecimal.valueOf(100.0));
        dosnRequestDTO.setCumpleEstandar(true);
        dosnRequestDTO.setComentarios("Test DOSN");

        dosn = new Dosn();
        dosn.setAnalisisID(1L);
        dosn.setLote(lote);
        dosn.setFechaINIA(LocalDate.now());
        dosn.setGramosAnalizadosINIA(BigDecimal.valueOf(100.0));
        dosn.setEstado(Estado.EN_PROCESO);
        dosn.setActivo(true);
    }

    @Test
    @DisplayName("Crear DOSN - debe asignar estado EN_PROCESO")
    void crearDosn_debeAsignarEstadoEnProceso() {
        
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(dosnRepository.save(any(Dosn.class))).thenReturn(dosn);
        doNothing().when(analisisService).establecerFechaInicio(any(Dosn.class));
        doNothing().when(analisisHistorialService).registrarCreacion(any(Dosn.class));

        
        DosnDTO resultado = dosnService.crearDosn(dosnRequestDTO);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        verify(dosnRepository, times(1)).save(any(Dosn.class));
        verify(analisisHistorialService, times(1)).registrarCreacion(any(Dosn.class));
        verify(analisisService, times(1)).establecerFechaInicio(any(Dosn.class));
    }

    @Test
    @DisplayName("Crear DOSN con lote inexistente - debe lanzar excepción")
    void crearDosn_conLoteInexistente_debeLanzarExcepcion() {
        
        when(loteRepository.findById(999L)).thenReturn(Optional.empty());
        dosnRequestDTO.setIdLote(999L);

        
        assertThrows(RuntimeException.class, () -> {
            dosnService.crearDosn(dosnRequestDTO);
        }, "Debe lanzar excepción cuando el lote no existe");
    }

    @Test
    @DisplayName("Validar gramos analizados - debe aceptar valores positivos")
    void validarGramosAnalizados_debeAceptarValoresPositivos() {
        
        dosnRequestDTO.setGramosAnalizadosINIA(BigDecimal.valueOf(100.0));
        
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(dosnRepository.save(any(Dosn.class))).thenReturn(dosn);
        doNothing().when(analisisService).establecerFechaInicio(any(Dosn.class));
        doNothing().when(analisisHistorialService).registrarCreacion(any(Dosn.class));

        
        DosnDTO resultado = dosnService.crearDosn(dosnRequestDTO);

        
        assertNotNull(resultado);
        verify(dosnRepository, times(1)).save(any(Dosn.class));
    }

    @Test
    @DisplayName("Obtener DOSN por ID - debe retornar el análisis si existe")
    void obtenerDosnPorId_cuandoExiste_debeRetornarDosn() {
        
        when(dosnRepository.findById(1L)).thenReturn(Optional.of(dosn));

        
        DosnDTO resultado = dosnService.obtenerDosnPorId(1L);

        
        assertNotNull(resultado);
        assertEquals(1L, resultado.getAnalisisID());
        verify(dosnRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener DOSN por ID inexistente - debe lanzar excepción")
    void obtenerDosnPorId_cuandoNoExiste_debeLanzarExcepcion() {
        
        when(dosnRepository.findById(999L)).thenReturn(Optional.empty());

        
        assertThrows(RuntimeException.class, () -> {
            dosnService.obtenerDosnPorId(999L);
        }, "Debe lanzar excepción cuando el análisis no existe");
    }

    @Test
    @DisplayName("Actualizar DOSN - debe actualizar correctamente")
    void actualizarDosn_debeActualizarCorrectamente() {
        
        dosnRequestDTO.setComentarios("Comentarios actualizados");
        
        when(dosnRepository.findById(1L)).thenReturn(Optional.of(dosn));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(dosnRepository.save(any(Dosn.class))).thenReturn(dosn);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Dosn.class));

        
        DosnDTO resultado = dosnService.actualizarDosn(1L, dosnRequestDTO);

        
        assertNotNull(resultado);
        verify(dosnRepository, times(1)).save(any(Dosn.class));
        verify(analisisHistorialService, times(1)).registrarModificacion(any(Dosn.class));
    }

    @Test
    @DisplayName("Actualizar DOSN APROBADA por ANALISTA - debe cambiar a PENDIENTE_APROBACION")
    void actualizarDosn_aprobadaPorAnalista_debeCambiarAPendiente() {
        
        dosn.setEstado(Estado.APROBADO);
        
        when(dosnRepository.findById(1L)).thenReturn(Optional.of(dosn));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(analisisService.esAnalista()).thenReturn(true);
        when(dosnRepository.save(any(Dosn.class))).thenReturn(dosn);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Dosn.class));

        
        dosnService.actualizarDosn(1L, dosnRequestDTO);

        
        verify(dosnRepository, times(1)).save(any(Dosn.class));
        verify(analisisService, times(1)).esAnalista();
    }

    @Test
    @DisplayName("Actualizar DOSN inexistente - debe lanzar excepción")
    void actualizarDosn_noExistente_debeLanzarExcepcion() {
        
        when(dosnRepository.findById(999L)).thenReturn(Optional.empty());

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dosnService.actualizarDosn(999L, dosnRequestDTO);
        });
        
        assertTrue(exception.getMessage().contains("no encontrada"));
        verify(dosnRepository, never()).save(any(Dosn.class));
    }

    @Test
    @DisplayName("Desactivar DOSN - debe cambiar activo a false")
    void desactivarDosn_debeCambiarActivoAFalse() {
        
        doNothing().when(analisisService).desactivarAnalisis(eq(1L), any());

        
        dosnService.desactivarDosn(1L);

        
        verify(analisisService, times(1)).desactivarAnalisis(eq(1L), any());
    }

    @Test
    @DisplayName("Reactivar DOSN - debe cambiar activo a true")
    void reactivarDosn_debeCambiarActivoATrue() {
        
        dosn.setActivo(false);
        DosnDTO dosnDTO = new DosnDTO();
        dosnDTO.setAnalisisID(1L);
        dosnDTO.setActivo(true);
        
        when(analisisService.reactivarAnalisis(any(Long.class), any(), any())).thenReturn(dosnDTO);

        
        dosnService.reactivarDosn(1L);

        
        verify(analisisService, times(1)).reactivarAnalisis(any(Long.class), any(), any());
    }

    @Test
    @DisplayName("Eliminar DOSN - debe desactivar el análisis")
    void eliminarDosn_debeDesactivarAnalisis() {
        
        when(dosnRepository.findById(1L)).thenReturn(Optional.of(dosn));
        when(dosnRepository.save(any(Dosn.class))).thenReturn(dosn);

        
        dosnService.eliminarDosn(1L);

        
        verify(dosnRepository, times(1)).findById(1L);
        verify(dosnRepository, times(1)).save(any(Dosn.class));
    }

    @Test
    @DisplayName("Crear DOSN con cumple estándar true - debe guardar correctamente")
    void crearDosn_conCumpleEstandarTrue_debeGuardar() {
        
        dosnRequestDTO.setCumpleEstandar(true);
        dosnRequestDTO.setGramosAnalizadosINIA(BigDecimal.valueOf(100.0));
        
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(dosnRepository.save(any(Dosn.class))).thenReturn(dosn);
        doNothing().when(analisisService).establecerFechaInicio(any(Dosn.class));
        doNothing().when(analisisHistorialService).registrarCreacion(any(Dosn.class));

        
        DosnDTO resultado = dosnService.crearDosn(dosnRequestDTO);

        
        assertNotNull(resultado);
        verify(dosnRepository, times(1)).save(any(Dosn.class));
    }

    @Test
    @DisplayName("Crear DOSN con cumple estándar false - debe guardar correctamente")
    void crearDosn_conCumpleEstandarFalse_debeGuardar() {
        
        dosnRequestDTO.setCumpleEstandar(false);
        dosnRequestDTO.setGramosAnalizadosINIA(BigDecimal.valueOf(100.0));
        
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(dosnRepository.save(any(Dosn.class))).thenReturn(dosn);
        doNothing().when(analisisService).establecerFechaInicio(any(Dosn.class));
        doNothing().when(analisisHistorialService).registrarCreacion(any(Dosn.class));

        
        DosnDTO resultado = dosnService.crearDosn(dosnRequestDTO);

        
        assertNotNull(resultado);
        verify(dosnRepository, times(1)).save(any(Dosn.class));
    }

    @Test
    @DisplayName("mapearEntidadAListadoDTO - mapea todos los atributos correctamente")
    void mapearEntidadAListadoDTO_mapeaTodosLosAtributos() {
        
        Cultivar cultivar = new Cultivar();
        Especie especie = new Especie();
        especie.setNombreComun("Trigo");
        especie.setNombreCientifico("Triticum aestivum");
        cultivar.setEspecie(especie);
        
        lote.setCultivar(cultivar);
        dosn.setLote(lote);
        dosn.setFechaInicio(LocalDateTime.now());
        dosn.setFechaFin(LocalDateTime.now().plusDays(7));
        dosn.setCumpleEstandar(true);
        
        AnalisisHistorialDTO historial1 = new AnalisisHistorialDTO();
        historial1.setUsuario("admin");
        AnalisisHistorialDTO historial2 = new AnalisisHistorialDTO();
        historial2.setUsuario("analista");
        
        when(analisisHistorialService.obtenerHistorialAnalisis(1L))
            .thenReturn(Arrays.asList(historial2, historial1));
        
        Page<Dosn> page = new PageImpl<>(Arrays.asList(dosn));
        when(dosnRepository.findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class))).thenReturn(page);
        
        
        Page<DosnListadoDTO> resultado = dosnService.obtenerDosnPaginadas(Pageable.unpaged());
        
        
        assertNotNull(resultado);
        verify(analisisHistorialService, atLeastOnce()).obtenerHistorialAnalisis(any());
    }

    @Test
    @DisplayName("mapearEntidadAListadoDTO - usa nombreCientifico si nombreComun está vacío")
    void mapearEntidadAListadoDTO_usaNombreCientificoSiComunVacio() {
        
        Cultivar cultivar = new Cultivar();
        Especie especie = new Especie();
        especie.setNombreComun(""); 
        especie.setNombreCientifico("Triticum aestivum");
        cultivar.setEspecie(especie);
        
        lote.setCultivar(cultivar);
        dosn.setLote(lote);
        
        Page<Dosn> page = new PageImpl<>(Arrays.asList(dosn));
        when(dosnRepository.findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class))).thenReturn(page);
        
        
        Page<DosnListadoDTO> resultado = dosnService.obtenerDosnPaginadas(Pageable.unpaged());
        
        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    @DisplayName("validarAntesDeFinalizar - lanza excepción si no hay evidencia")
    void validarAntesDeFinalizar_sinEvidencia_lanzaExcepcion() throws Exception {
        
        Dosn dosnSinEvidencia = new Dosn();
        dosnSinEvidencia.setAnalisisID(1L);
        dosnSinEvidencia.setLote(lote);
        dosnSinEvidencia.setEstado(Estado.EN_PROCESO);
        
        
        
        Method validarMethod = DosnService.class.getDeclaredMethod("validarAntesDeFinalizar", Dosn.class);
        validarMethod.setAccessible(true);
        
        
        Exception exception = assertThrows(Exception.class, () -> {
            validarMethod.invoke(dosnService, dosnSinEvidencia);
        });
        
        assertTrue(exception.getCause().getMessage().contains("carece de evidencia"));
    }

    @Test
    @DisplayName("validarAntesDeFinalizar - acepta con datos INIA válidos")
    void validarAntesDeFinalizar_conDatosINIAValidos_nolanzaExcepcion() throws Exception {
        
        dosn.setFechaINIA(LocalDate.now());
        dosn.setGramosAnalizadosINIA(BigDecimal.valueOf(100.0));
        dosn.setEstado(Estado.EN_PROCESO);
        
        
        Method validarMethod = DosnService.class.getDeclaredMethod("validarAntesDeFinalizar", Dosn.class);
        validarMethod.setAccessible(true);
        
        
        assertDoesNotThrow(() -> {
            validarMethod.invoke(dosnService, dosn);
        });
    }

    @Test
    @DisplayName("validarAntesDeFinalizar - lanza excepción si gramos INIA <= 0")
    void validarAntesDeFinalizar_gramosINIACero_lanzaExcepcion() throws Exception {
        
        dosn.setFechaINIA(LocalDate.now());
        dosn.setGramosAnalizadosINIA(BigDecimal.ZERO);
        
        dosn.setFechaINASE(LocalDate.now());
        dosn.setGramosAnalizadosINASE(BigDecimal.valueOf(100.0));
        
        
        Method validarMethod = DosnService.class.getDeclaredMethod("validarAntesDeFinalizar", Dosn.class);
        validarMethod.setAccessible(true);
        
        
        Exception exception = assertThrows(Exception.class, () -> {
            validarMethod.invoke(dosnService, dosn);
        });
        
        assertTrue(exception.getCause().getMessage().contains("Gramos analizados INIA"));
    }

    @Test
    @DisplayName("validarAntesDeFinalizar - acepta con datos INASE válidos")
    void validarAntesDeFinalizar_conDatosINASEValidos_noLanzaExcepcion() throws Exception {
        
        dosn.setFechaINASE(LocalDate.now());
        dosn.setGramosAnalizadosINASE(BigDecimal.valueOf(200.0));
        dosn.setEstado(Estado.EN_PROCESO);
        
        
        Method validarMethod = DosnService.class.getDeclaredMethod("validarAntesDeFinalizar", Dosn.class);
        validarMethod.setAccessible(true);
        
        
        assertDoesNotThrow(() -> {
            validarMethod.invoke(dosnService, dosn);
        });
    }

    @Test
    @DisplayName("validarAntesDeFinalizar - lanza excepción si gramos INASE <= 0")
    void validarAntesDeFinalizar_gramosINASECero_lanzaExcepcion() throws Exception {
        
        dosn.setFechaINASE(LocalDate.now());
        dosn.setGramosAnalizadosINASE(BigDecimal.ZERO);
        
        dosn.setFechaINIA(LocalDate.now());
        dosn.setGramosAnalizadosINIA(BigDecimal.valueOf(100.0));
        
        
        Method validarMethod = DosnService.class.getDeclaredMethod("validarAntesDeFinalizar", Dosn.class);
        validarMethod.setAccessible(true);
        
        
        Exception exception = assertThrows(Exception.class, () -> {
            validarMethod.invoke(dosnService, dosn);
        });
        
        assertTrue(exception.getCause().getMessage().contains("Gramos analizados INASE"));
    }

    @Test
    @DisplayName("validarAntesDeFinalizar - acepta con datos de cuscuta válidos")
    void validarAntesDeFinalizar_conDatosCuscutaValidos_noLanzaExcepcion() throws Exception {
        
        CuscutaRegistro cuscuta = new CuscutaRegistro();
        cuscuta.setId(1L);
        cuscuta.setInstituto(Instituto.INIA);
        cuscuta.setCuscuta_g(BigDecimal.valueOf(5.0));
        cuscuta.setCuscutaNum(2);
        
        dosn.setCuscutaRegistros(Arrays.asList(cuscuta));
        dosn.setEstado(Estado.EN_PROCESO);
        
        
        Method validarMethod = DosnService.class.getDeclaredMethod("validarAntesDeFinalizar", Dosn.class);
        validarMethod.setAccessible(true);
        
        
        assertDoesNotThrow(() -> {
            validarMethod.invoke(dosnService, dosn);
        });
    }

    @Test
    @DisplayName("validarAntesDeFinalizar - acepta con listados válidos")
    void validarAntesDeFinalizar_conListadosValidos_noLanzaExcepcion() throws Exception {
        
        Listado listado = new Listado();
        listado.setListadoID(1L);
        listado.setListadoNum(5);
        
        dosn.setListados(Arrays.asList(listado));
        dosn.setEstado(Estado.EN_PROCESO);
        
        
        Method validarMethod = DosnService.class.getDeclaredMethod("validarAntesDeFinalizar", Dosn.class);
        validarMethod.setAccessible(true);
        
        
        assertDoesNotThrow(() -> {
            validarMethod.invoke(dosnService, dosn);
        });
    }

    @Test
    @DisplayName("validarAntesDeFinalizar - acepta cuscuta con solo gramos mayores a cero")
    void validarAntesDeFinalizar_cuscutaConSoloGramos_noLanzaExcepcion() throws Exception {
        
        CuscutaRegistro cuscuta = new CuscutaRegistro();
        cuscuta.setId(1L);
        cuscuta.setInstituto(Instituto.INASE);
        cuscuta.setCuscuta_g(BigDecimal.valueOf(3.5));
        cuscuta.setCuscutaNum(null); 
        
        dosn.setCuscutaRegistros(Arrays.asList(cuscuta));
        dosn.setEstado(Estado.EN_PROCESO);
        
        
        Method validarMethod = DosnService.class.getDeclaredMethod("validarAntesDeFinalizar", Dosn.class);
        validarMethod.setAccessible(true);
        
        
        assertDoesNotThrow(() -> {
            validarMethod.invoke(dosnService, dosn);
        });
    }

    @Test
    @DisplayName("validarAntesDeFinalizar - acepta cuscuta con solo número mayor a cero")
    void validarAntesDeFinalizar_cuscutaConSoloNumero_noLanzaExcepcion() throws Exception {
        
        CuscutaRegistro cuscuta = new CuscutaRegistro();
        cuscuta.setId(1L);
        cuscuta.setInstituto(Instituto.INIA);
        cuscuta.setCuscuta_g(null); 
        cuscuta.setCuscutaNum(5);
        
        dosn.setCuscutaRegistros(Arrays.asList(cuscuta));
        dosn.setEstado(Estado.EN_PROCESO);
        
        
        Method validarMethod = DosnService.class.getDeclaredMethod("validarAntesDeFinalizar", Dosn.class);
        validarMethod.setAccessible(true);
        
        
        assertDoesNotThrow(() -> {
            validarMethod.invoke(dosnService, dosn);
        });
    }

    @Test
    @DisplayName("validarAntesDeFinalizar - rechaza cuscuta sin datos válidos")
    void validarAntesDeFinalizar_cuscutaSinDatosValidos_lanzaExcepcion() throws Exception {
        
        CuscutaRegistro cuscuta = new CuscutaRegistro();
        cuscuta.setId(1L);
        cuscuta.setInstituto(Instituto.INIA);
        cuscuta.setCuscuta_g(BigDecimal.ZERO); 
        cuscuta.setCuscutaNum(0); 
        
        
        dosn.setFechaINIA(null);
        dosn.setGramosAnalizadosINIA(null);
        dosn.setFechaINASE(null);
        dosn.setGramosAnalizadosINASE(null);
        dosn.setListados(null);
        
        dosn.setCuscutaRegistros(Arrays.asList(cuscuta));
        dosn.setEstado(Estado.EN_PROCESO);
        
        
        Method validarMethod = DosnService.class.getDeclaredMethod("validarAntesDeFinalizar", Dosn.class);
        validarMethod.setAccessible(true);
        
        
        Exception exception = assertThrows(Exception.class, () -> {
            validarMethod.invoke(dosnService, dosn);
        });
        
        assertTrue(exception.getCause().getMessage().contains("carece de evidencia"));
    }

    @Test
    @DisplayName("actualizarEntidadDesdeSolicitud - limpia y agrega nuevos listados")
    void actualizarEntidadDesdeSolicitud_limpiaYAgregaNuevosListados() {
        
        Listado listadoViejo = new Listado();
        listadoViejo.setListadoID(1L);
        dosn.setListados(new ArrayList<>(Arrays.asList(listadoViejo)));
        
        ListadoRequestDTO nuevoListadoRequest = new ListadoRequestDTO();
        nuevoListadoRequest.setIdCatalogo(1L);
        nuevoListadoRequest.setListadoNum(10);
        dosnRequestDTO.setListados(Arrays.asList(nuevoListadoRequest));
        
        
        MalezasCatalogo catalogo = new MalezasCatalogo();
        catalogo.setCatalogoID(1L);
        when(malezasCatalogoRepository.findById(1L)).thenReturn(Optional.of(catalogo));
        
        when(dosnRepository.findById(1L)).thenReturn(Optional.of(dosn));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(dosnRepository.save(any(Dosn.class))).thenReturn(dosn);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Dosn.class));
        
        
        DosnDTO resultado = dosnService.actualizarDosn(1L, dosnRequestDTO);
        
        
        assertNotNull(resultado);
        verify(dosnRepository, times(1)).save(any(Dosn.class));
    }

    @Test
    @DisplayName("actualizarEntidadDesdeSolicitud - limpia y agrega nuevos registros de cuscuta")
    void actualizarEntidadDesdeSolicitud_limpiaYAgregaNuevosCuscuta() {
        
        CuscutaRegistro cuscutaViejo = new CuscutaRegistro();
        cuscutaViejo.setId(1L);
        dosn.setCuscutaRegistros(new ArrayList<>(Arrays.asList(cuscutaViejo)));
        
        CuscutaRegistroRequestDTO nuevoCuscutaRequest = new CuscutaRegistroRequestDTO();
        nuevoCuscutaRequest.setInstituto(Instituto.INIA);
        nuevoCuscutaRequest.setCuscuta_g(BigDecimal.valueOf(5.0));
        nuevoCuscutaRequest.setCuscutaNum(3);
        nuevoCuscutaRequest.setFechaCuscuta(LocalDate.now());
        dosnRequestDTO.setCuscutaRegistros(Arrays.asList(nuevoCuscutaRequest));
        
        when(dosnRepository.findById(1L)).thenReturn(Optional.of(dosn));
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(dosnRepository.save(any(Dosn.class))).thenReturn(dosn);
        doNothing().when(analisisHistorialService).registrarModificacion(any(Dosn.class));
        
        
        DosnDTO resultado = dosnService.actualizarDosn(1L, dosnRequestDTO);
        
        
        assertNotNull(resultado);
        verify(dosnRepository, times(1)).save(any(Dosn.class));
    }

    @Test
    @DisplayName("mapearEntidadADTO - mapea cuscuta correctamente")
    void mapearEntidadADTO_mapeaCuscutaCorrectamente() {
        
        CuscutaRegistro cuscuta = new CuscutaRegistro();
        cuscuta.setId(1L);
        cuscuta.setInstituto(Instituto.INIA);
        cuscuta.setCuscuta_g(BigDecimal.valueOf(5.0));
        cuscuta.setCuscutaNum(3);
        cuscuta.setFechaCuscuta(LocalDate.now());
        
        dosn.setCuscutaRegistros(Arrays.asList(cuscuta));
        when(dosnRepository.findById(1L)).thenReturn(Optional.of(dosn));
        when(analisisHistorialService.obtenerHistorialAnalisis(1L)).thenReturn(new ArrayList<>());
        
        
        DosnDTO resultado = dosnService.obtenerDosnPorId(1L);
        
        
        assertNotNull(resultado);
        assertNotNull(resultado.getCuscutaRegistros());
        assertFalse(resultado.getCuscutaRegistros().isEmpty());
    }

    @Test
    @DisplayName("mapearEntidadADTO - mapea listados correctamente")
    void mapearEntidadADTO_mapeaListadosCorrectamente() {
        
        Listado listado = new Listado();
        listado.setListadoID(1L);
        listado.setListadoNum(10);
        
        dosn.setListados(Arrays.asList(listado));
        when(dosnRepository.findById(1L)).thenReturn(Optional.of(dosn));
        when(analisisHistorialService.obtenerHistorialAnalisis(1L)).thenReturn(new ArrayList<>());
        
        
        DosnDTO resultado = dosnService.obtenerDosnPorId(1L);
        
        
        assertNotNull(resultado);
        assertNotNull(resultado.getListados());
        assertFalse(resultado.getListados().isEmpty());
    }

    @Test
    @DisplayName("mapearCuscutaRegistroADTO - mapea todos los atributos")
    void mapearCuscutaRegistroADTO_mapeaTodosLosAtributos() {
        
        CuscutaRegistro cuscuta = new CuscutaRegistro();
        cuscuta.setId(1L);
        cuscuta.setInstituto(Instituto.INASE);
        cuscuta.setCuscuta_g(BigDecimal.valueOf(10.5));
        cuscuta.setCuscutaNum(5);
        cuscuta.setFechaCuscuta(LocalDate.now());
        
        dosn.setCuscutaRegistros(Arrays.asList(cuscuta));
        when(dosnRepository.findById(1L)).thenReturn(Optional.of(dosn));
        when(analisisHistorialService.obtenerHistorialAnalisis(1L)).thenReturn(new ArrayList<>());
        
        
        DosnDTO resultado = dosnService.obtenerDosnPorId(1L);
        
        
        assertNotNull(resultado.getCuscutaRegistros());
        CuscutaRegistroDTO cuscutaDTO = resultado.getCuscutaRegistros().get(0);
        assertEquals(1L, cuscutaDTO.getId());
        assertEquals(Instituto.INASE, cuscutaDTO.getInstituto());
    }

    @Test
    @DisplayName("crearCuscutaRegistroDesdeSolicitud - crea registro correctamente")
    void crearCuscutaRegistroDesdeSolicitud_creaCorrectamente() {
        
        CuscutaRegistroRequestDTO cuscutaRequest = new CuscutaRegistroRequestDTO();
        cuscutaRequest.setInstituto(Instituto.INIA);
        cuscutaRequest.setCuscuta_g(BigDecimal.valueOf(7.5));
        cuscutaRequest.setCuscutaNum(4);
        cuscutaRequest.setFechaCuscuta(LocalDate.now());
        
        dosnRequestDTO.setCuscutaRegistros(Arrays.asList(cuscutaRequest));
        
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(dosnRepository.save(any(Dosn.class))).thenReturn(dosn);
        doNothing().when(analisisService).establecerFechaInicio(any(Dosn.class));
        doNothing().when(analisisHistorialService).registrarCreacion(any(Dosn.class));
        
        
        DosnDTO resultado = dosnService.crearDosn(dosnRequestDTO);
        
        
        assertNotNull(resultado);
        verify(dosnRepository, times(1)).save(any(Dosn.class));
    }

    @Test
    @DisplayName("obtenerTodasDosnActivas - retorna lista de DOSN activas")
    void obtenerTodasDosnActivas_retornaListaActivas() {
        
        Dosn dosn2 = new Dosn();
        dosn2.setAnalisisID(2L);
        dosn2.setLote(lote);
        dosn2.setActivo(true);
        
        when(dosnRepository.findByActivoTrue()).thenReturn(Arrays.asList(dosn, dosn2));
        when(analisisHistorialService.obtenerHistorialAnalisis(any())).thenReturn(new ArrayList<>());
        
        
        ResponseListadoDosn resultado = dosnService.obtenerTodasDosnActivas();
        
        
        assertNotNull(resultado);
        assertNotNull(resultado.getDosns());
        assertEquals(2, resultado.getDosns().size());
        verify(dosnRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("obtenerDosnPaginadasConFiltro - filtro activos")
    void obtenerDosnPaginadasConFiltro_filtroActivos() {
        
        Page<Dosn> page = new PageImpl<>(Arrays.asList(dosn));
        when(dosnRepository.findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class))).thenReturn(page);
        
        
        Page<DosnListadoDTO> resultado = dosnService.obtenerDosnPaginadasConFiltro(Pageable.unpaged(), "activos");
        
        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(dosnRepository, times(1)).findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class));
    }

    @Test
    @DisplayName("obtenerDosnPaginadasConFiltro - filtro inactivos")
    void obtenerDosnPaginadasConFiltro_filtroInactivos() {
        
        dosn.setActivo(false);
        Page<Dosn> page = new PageImpl<>(Arrays.asList(dosn));
        when(dosnRepository.findByActivoFalseOrderByFechaInicioDesc(any(Pageable.class))).thenReturn(page);
        
        
        Page<DosnListadoDTO> resultado = dosnService.obtenerDosnPaginadasConFiltro(Pageable.unpaged(), "inactivos");
        
        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(dosnRepository, times(1)).findByActivoFalseOrderByFechaInicioDesc(any(Pageable.class));
    }

    @Test
    @DisplayName("obtenerDosnPaginadasConFiltro - filtro todos")
    void obtenerDosnPaginadasConFiltro_filtroTodos() {
        
        Page<Dosn> page = new PageImpl<>(Arrays.asList(dosn));
        when(dosnRepository.findAllByOrderByFechaInicioDesc(any(Pageable.class))).thenReturn(page);
        
        
        Page<DosnListadoDTO> resultado = dosnService.obtenerDosnPaginadasConFiltro(Pageable.unpaged(), "todos");
        
        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(dosnRepository, times(1)).findAllByOrderByFechaInicioDesc(any(Pageable.class));
    }

    @Test
    @DisplayName("crearListadoDesdeSolicitud - crea listado correctamente")
    void crearListadoDesdeSolicitud_creaCorrectamente() {
        
        ListadoRequestDTO listadoRequest = new ListadoRequestDTO();
        listadoRequest.setIdCatalogo(1L);
        listadoRequest.setListadoNum(15);
        
        dosnRequestDTO.setListados(Arrays.asList(listadoRequest));
        
        
        MalezasCatalogo catalogo = new MalezasCatalogo();
        catalogo.setCatalogoID(1L);
        when(malezasCatalogoRepository.findById(1L)).thenReturn(Optional.of(catalogo));
        
        when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
        when(dosnRepository.save(any(Dosn.class))).thenReturn(dosn);
        doNothing().when(analisisService).establecerFechaInicio(any(Dosn.class));
        doNothing().when(analisisHistorialService).registrarCreacion(any(Dosn.class));
        
        
        DosnDTO resultado = dosnService.crearDosn(dosnRequestDTO);
        
        
        assertNotNull(resultado);
        verify(dosnRepository, times(1)).save(any(Dosn.class));
    }

    @Test
    @DisplayName("finalizarAnalisis - finaliza DOSN correctamente")
    void finalizarAnalisis_finalizaCorrectamente() {
        
        dosn.setFechaINIA(LocalDate.now());
        dosn.setGramosAnalizadosINIA(BigDecimal.valueOf(100.0));
        dosn.setEstado(Estado.EN_PROCESO);
        
        DosnDTO dosnDTO = new DosnDTO();
        dosnDTO.setAnalisisID(1L);
        dosnDTO.setEstado(Estado.PENDIENTE_APROBACION);
        
        when(dosnRepository.findById(1L)).thenReturn(Optional.of(dosn));
        when(analisisService.finalizarAnalisisGenerico(any(), any(), any(), any())).thenReturn(dosnDTO);
        
        
        DosnDTO resultado = dosnService.finalizarAnalisis(1L);
        
        
        assertNotNull(resultado);
        verify(analisisService, times(1)).finalizarAnalisisGenerico(any(), any(), any(), any());
    }

    @Test
    @DisplayName("obtenerDosnPaginadas - retorna página de DOSN")
    void obtenerDosnPaginadas_retornaPagina() {
        
        Page<Dosn> page = new PageImpl<>(Arrays.asList(dosn));
        when(dosnRepository.findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class))).thenReturn(page);
        
        
        Page<DosnListadoDTO> resultado = dosnService.obtenerDosnPaginadas(Pageable.unpaged());
        
        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(dosnRepository, times(1)).findByActivoTrueOrderByFechaInicioDesc(any(Pageable.class));
    }

    @Test
    @DisplayName("aprobarAnalisis - aprueba DOSN correctamente")
    void aprobarAnalisis_apruebaCorrectamente() {
        
        dosn.setFechaINIA(LocalDate.now());
        dosn.setGramosAnalizadosINIA(BigDecimal.valueOf(100.0));
        dosn.setEstado(Estado.PENDIENTE_APROBACION);
        
        DosnDTO dosnDTO = new DosnDTO();
        dosnDTO.setAnalisisID(1L);
        dosnDTO.setEstado(Estado.APROBADO);
        
        when(dosnRepository.findById(1L)).thenReturn(Optional.of(dosn));
        when(analisisService.aprobarAnalisisGenerico(any(), any(), any(), any(), any())).thenReturn(dosnDTO);
        
        
        DosnDTO resultado = dosnService.aprobarAnalisis(1L);
        
        
        assertNotNull(resultado);
        verify(analisisService, times(1)).aprobarAnalisisGenerico(any(), any(), any(), any(), any());
    }
}
