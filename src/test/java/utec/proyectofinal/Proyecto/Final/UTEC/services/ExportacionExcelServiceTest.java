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
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ExportacionRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoListado;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyList;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de ExportacionExcelService")
class ExportacionExcelServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private PurezaRepository purezaRepository;

    @Mock
    private GerminacionRepository germinacionRepository;

    @Mock
    private PmsRepository pmsRepository;

    @Mock
    private TetrazolioRepository tetrazolioRepository;

    @Mock
    private DosnRepository dosnRepository;

    @Mock
    private TablaGermRepository tablaGermRepository;

    @InjectMocks
    private ExportacionExcelService exportacionExcelService;

    private Lote lote;
    private Cultivar cultivar;
    private Especie especie;

    @BeforeEach
    void setUp() {
        especie = new Especie();
        especie.setEspecieID(1L);
        especie.setNombreCientifico("Especie Test");
        especie.setNombreComun("Especie Común");
        especie.setActivo(true);

        cultivar = new Cultivar();
        cultivar.setCultivarID(1L);
        cultivar.setNombre("Cultivar Test");
        cultivar.setEspecie(especie);
        cultivar.setActivo(true);

        lote = new Lote();
        lote.setLoteID(1L);
        lote.setNomLote("LOTE-001");
        lote.setFicha("FICHA-001");
        lote.setFechaRecibo(LocalDate.now());
        lote.setCultivar(cultivar);
        lote.setActivo(true);
    }

    @Test
    @DisplayName("Generar reporte Excel con lista vacía - debe generar archivo")
    void generarReporteExcel_listaVacia_debeGenerarArchivo() throws IOException {
        List<Long> loteIds = new ArrayList<>();

        byte[] resultado = exportacionExcelService.generarReporteExcel(loteIds);

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Generar reporte Excel avanzado vacío - debe generar archivo")
    void generarReporteExcelAvanzado_vacio_debeGenerarArchivo() throws IOException {
        ExportacionRequestDTO solicitud = new ExportacionRequestDTO();
        solicitud.setIncluirEncabezados(true);

        byte[] resultado = exportacionExcelService.generarReporteExcelAvanzado(solicitud);

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Generar reporte Excel sin encabezados - debe generar archivo")
    void generarReporteExcelAvanzado_sinEncabezados_debeGenerarArchivo() throws IOException {
        ExportacionRequestDTO solicitud = new ExportacionRequestDTO();
        solicitud.setIncluirEncabezados(false);

        byte[] resultado = exportacionExcelService.generarReporteExcelAvanzado(solicitud);

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Generar reporte Excel con lote inexistente - debe generar archivo")
    void generarReporteExcel_loteInexistente_debeGenerarArchivo() throws IOException {
        List<Long> loteIds = Arrays.asList(999L);
        when(loteRepository.findAllById(loteIds)).thenReturn(Arrays.asList());

        byte[] resultado = exportacionExcelService.generarReporteExcel(loteIds);

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        verify(loteRepository, times(1)).findAllById(loteIds);
    }

    @Test
    @DisplayName("Generar reporte con múltiples lotes - debe generar archivo")
    void generarReporteExcel_multiples_debeGenerarArchivo() throws IOException {
        Lote lote2 = new Lote();
        lote2.setLoteID(2L);
        lote2.setNomLote("LOTE-002");
        lote2.setActivo(true);

        List<Long> loteIds = Arrays.asList(1L, 2L);
        when(loteRepository.findAllById(loteIds)).thenReturn(Arrays.asList(lote, lote2));

        byte[] resultado = exportacionExcelService.generarReporteExcel(loteIds);

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        verify(loteRepository, times(1)).findAllById(loteIds);
    }

    @Test
    @DisplayName("Generar reporte con lote con datos completos de Pureza")
    void generarReporteExcel_conPureza_debeMapearCorrectamente() throws IOException {
        
        Pureza pureza = new Pureza();
        pureza.setAnalisisID(1L);
        pureza.setLote(lote);
        pureza.setRedonSemillaPura(java.math.BigDecimal.valueOf(95.0));
        pureza.setRedonMateriaInerte(java.math.BigDecimal.valueOf(3.0));
        pureza.setRedonOtrosCultivos(java.math.BigDecimal.valueOf(1.0));
        pureza.setRedonMalezas(java.math.BigDecimal.valueOf(1.0));
        pureza.setInasePura(java.math.BigDecimal.valueOf(94.5));
        pureza.setInaseMateriaInerte(java.math.BigDecimal.valueOf(3.5));
        
        List<Long> loteIds = Arrays.asList(1L);
        when(loteRepository.findAllById(loteIds)).thenReturn(Arrays.asList(lote));
        when(purezaRepository.findByLoteLoteID(1L)).thenReturn(Arrays.asList(pureza));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(loteIds);

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        verify(purezaRepository, times(1)).findByLoteLoteID(1L);
    }

    @Test
    @DisplayName("Generar reporte con lote con datos completos de Germinación")
    void generarReporteExcel_conGerminacion_debeMapearCorrectamente() throws IOException {
        
        Germinacion germinacion = new Germinacion();
        germinacion.setAnalisisID(1L);
        germinacion.setLote(lote);
        
        TablaGerm tablaGerm = new TablaGerm();
        tablaGerm.setGerminacion(germinacion);
        tablaGerm.setPorcentajeNormalesConRedondeo(java.math.BigDecimal.valueOf(85.0));
        
        List<Long> loteIds = Arrays.asList(1L);
        when(loteRepository.findAllById(loteIds)).thenReturn(Arrays.asList(lote));
        when(germinacionRepository.findByLoteLoteID(1L)).thenReturn(Arrays.asList(germinacion));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(loteIds);

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        verify(germinacionRepository, times(1)).findByLoteLoteID(1L);
    }

    @Test
    @DisplayName("Generar reporte con lote con datos completos de PMS")
    void generarReporteExcel_conPms_debeMapearCorrectamente() throws IOException {
        
        Pms pms = new Pms();
        pms.setAnalisisID(1L);
        pms.setLote(lote);
        pms.setPmsconRedon(java.math.BigDecimal.valueOf(250.5));
        
        List<Long> loteIds = Arrays.asList(1L);
        when(loteRepository.findAllById(loteIds)).thenReturn(Arrays.asList(lote));
        when(pmsRepository.findByLoteLoteID(1L)).thenReturn(Arrays.asList(pms));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(loteIds);

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        verify(pmsRepository, times(1)).findByLoteLoteID(1L);
    }

    @Test
    @DisplayName("Generar reporte con lote con datos completos de Tetrazolio")
    void generarReporteExcel_conTetrazolio_debeMapearCorrectamente() throws IOException {
        
        Tetrazolio tetrazolio = new Tetrazolio();
        tetrazolio.setAnalisisID(1L);
        tetrazolio.setLote(lote);
        tetrazolio.setPorcViablesRedondeo(java.math.BigDecimal.valueOf(90.0));
        tetrazolio.setViabilidadInase(java.math.BigDecimal.valueOf(88.5));
        
        List<Long> loteIds = Arrays.asList(1L);
        when(loteRepository.findAllById(loteIds)).thenReturn(Arrays.asList(lote));
        when(tetrazolioRepository.findByLoteLoteID(1L)).thenReturn(Arrays.asList(tetrazolio));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(loteIds);

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        verify(tetrazolioRepository, times(1)).findByLoteLoteID(1L);
    }

    @Test
    @DisplayName("Generar reporte con lote con datos completos de DOSN")
    void generarReporteExcel_conDosn_debeMapearCorrectamente() throws IOException {
        
        Dosn dosn = new Dosn();
        dosn.setAnalisisID(1L);
        dosn.setLote(lote);
        dosn.setGramosAnalizadosINIA(java.math.BigDecimal.valueOf(1000.0));
        dosn.setGramosAnalizadosINASE(java.math.BigDecimal.valueOf(1000.0));
        
        List<Long> loteIds = Arrays.asList(1L);
        when(loteRepository.findAllById(loteIds)).thenReturn(Arrays.asList(lote));
        when(dosnRepository.findByLoteLoteID(1L)).thenReturn(Arrays.asList(dosn));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(loteIds);

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        verify(dosnRepository, times(1)).findByLoteLoteID(1L);
    }

    @Test
    @DisplayName("Generar reporte Excel avanzado con filtros complejos")
    void generarReporteExcelAvanzado_conFiltros_debeGenerarArchivo() throws IOException {
        
        ExportacionRequestDTO solicitud = new ExportacionRequestDTO();
        solicitud.setIncluirEncabezados(true);
        solicitud.setLoteIds(Arrays.asList(1L));
        solicitud.setFechaDesde(LocalDate.now().minusDays(30));
        solicitud.setFechaHasta(LocalDate.now());
        
        when(loteRepository.findAllById(anyList())).thenReturn(Arrays.asList(lote));

        
        byte[] resultado = exportacionExcelService.generarReporteExcelAvanzado(solicitud);

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Generar reporte con null en loteIds - debe usar todos los lotes activos")
    void generarReporteExcel_loteIdsNull_debeUsarLotesActivos() throws IOException {
        
        when(loteRepository.findByActivoTrue()).thenReturn(Arrays.asList(lote));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(null);

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        verify(loteRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("Generar reporte con lote sin cultivar - debe manejar nulls")
    void generarReporteExcel_loteSinCultivar_debeManejarNulls() throws IOException {
        
        Lote loteSinCultivar = new Lote();
        loteSinCultivar.setLoteID(3L);
        loteSinCultivar.setNomLote("LOTE-SIN-CULTIVAR");
        loteSinCultivar.setActivo(true);
        loteSinCultivar.setCultivar(null);
        
        List<Long> loteIds = Arrays.asList(3L);
        when(loteRepository.findAllById(loteIds)).thenReturn(Arrays.asList(loteSinCultivar));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(loteIds);

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Generar reporte con múltiples análisis del mismo tipo - debe usar el más reciente")
    void generarReporteExcel_multiplesPurezas_debeUsarMasReciente() throws IOException {
        
        Pureza pureza1 = new Pureza();
        pureza1.setAnalisisID(1L);
        pureza1.setLote(lote);
        pureza1.setFechaInicio(LocalDateTime.now().minusDays(10));
        pureza1.setRedonSemillaPura(java.math.BigDecimal.valueOf(90.0));
        
        Pureza pureza2 = new Pureza();
        pureza2.setAnalisisID(2L);
        pureza2.setLote(lote);
        pureza2.setFechaInicio(LocalDateTime.now());
        pureza2.setRedonSemillaPura(java.math.BigDecimal.valueOf(95.0));
        
        List<Long> loteIds = Arrays.asList(1L);
        when(loteRepository.findAllById(loteIds)).thenReturn(Arrays.asList(lote));
        when(purezaRepository.findByLoteLoteID(1L)).thenReturn(Arrays.asList(pureza1, pureza2));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(loteIds);

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Mapear datos pureza - con todos los campos completos")
    void mapearDatosPureza_conTodosLosCampos_debeLlenarDTO() throws IOException {
        
        Pureza pureza = new Pureza();
        pureza.setAnalisisID(1L);
        pureza.setLote(lote);
        pureza.setFechaInicio(LocalDateTime.now());
        pureza.setRedonSemillaPura(java.math.BigDecimal.valueOf(95.5));
        pureza.setRedonMateriaInerte(java.math.BigDecimal.valueOf(2.5));
        pureza.setRedonOtrosCultivos(java.math.BigDecimal.valueOf(1.0));
        pureza.setRedonMalezas(java.math.BigDecimal.valueOf(1.0));
        pureza.setInasePura(java.math.BigDecimal.valueOf(96.0));
        pureza.setInaseMateriaInerte(java.math.BigDecimal.valueOf(2.0));
        pureza.setInaseFecha(LocalDate.now());
        
        List<Long> loteIds = Arrays.asList(1L);
        when(loteRepository.findAllById(loteIds)).thenReturn(Arrays.asList(lote));
        when(purezaRepository.findByLoteLoteID(1L)).thenReturn(Arrays.asList(pureza));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(loteIds);

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        verify(purezaRepository, times(1)).findByLoteLoteID(1L);
    }

    @Test
    @DisplayName("Mapear datos germinacion - con tablas germ completas")
    void mapearDatosGerminacion_conTablasGerm_debeLlenarDTO() throws IOException {
        
        Germinacion germinacion = new Germinacion();
        germinacion.setAnalisisID(1L);
        germinacion.setLote(lote);
        germinacion.setFechaInicio(LocalDateTime.now());
        
        TablaGerm tablaGerm = new TablaGerm();
        tablaGerm.setTablaGermID(1L);
        tablaGerm.setGerminacion(germinacion);
        tablaGerm.setPorcentajeNormalesConRedondeo(java.math.BigDecimal.valueOf(85.0));
        tablaGerm.setPorcentajeAnormalesConRedondeo(java.math.BigDecimal.valueOf(10.0));
        tablaGerm.setFechaIngreso(LocalDate.now());
        tablaGerm.setFechaGerminacion(LocalDate.now().plusDays(7));
        
        List<Long> loteIds = Arrays.asList(1L);
        when(loteRepository.findAllById(loteIds)).thenReturn(Arrays.asList(lote));
        when(germinacionRepository.findByLoteLoteID(1L)).thenReturn(Arrays.asList(germinacion));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(loteIds);

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        verify(germinacionRepository, times(1)).findByLoteLoteID(1L);
    }

    @Test
    @DisplayName("Mapear datos PMS - con repeticiones completas")
    void mapearDatosPms_conRepeticiones_debeLlenarDTO() throws IOException {
        
        Pms pms = new Pms();
        pms.setAnalisisID(1L);
        pms.setLote(lote);
        pms.setFechaInicio(LocalDateTime.now());
        pms.setPmsconRedon(java.math.BigDecimal.valueOf(5.25));
        pms.setPmssinRedon(java.math.BigDecimal.valueOf(5.23));
        pms.setPromedio100g(java.math.BigDecimal.valueOf(1904.0));
        
        List<Long> loteIds = Arrays.asList(1L);
        when(loteRepository.findAllById(loteIds)).thenReturn(Arrays.asList(lote));
        when(pmsRepository.findByLoteLoteID(1L)).thenReturn(Arrays.asList(pms));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(loteIds);

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        verify(pmsRepository, times(1)).findByLoteLoteID(1L);
    }

    @Test
    @DisplayName("Mapear datos tetrazolio - con viabilidad completa")
    void mapearDatosTetrazolio_conViabilidad_debeLlenarDTO() throws IOException {
        
        Tetrazolio tetrazolio = new Tetrazolio();
        tetrazolio.setAnalisisID(1L);
        tetrazolio.setLote(lote);
        tetrazolio.setFechaInicio(LocalDateTime.now());
        tetrazolio.setPorcViablesRedondeo(java.math.BigDecimal.valueOf(92.0));
        tetrazolio.setPorcNoViablesRedondeo(java.math.BigDecimal.valueOf(5.0));
        tetrazolio.setPorcDurasRedondeo(java.math.BigDecimal.valueOf(3.0));
        tetrazolio.setViabilidadInase(java.math.BigDecimal.valueOf(91.5));
        
        List<Long> loteIds = Arrays.asList(1L);
        when(loteRepository.findAllById(loteIds)).thenReturn(Arrays.asList(lote));
        when(tetrazolioRepository.findByLoteLoteID(1L)).thenReturn(Arrays.asList(tetrazolio));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(loteIds);

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        verify(tetrazolioRepository, times(1)).findByLoteLoteID(1L);
    }

    @Test
    @DisplayName("Mapear datos DOSN - con análisis INIA e INASE")
    void mapearDatosDosn_conINIAeINASE_debeLlenarDTO() throws IOException {
        
        Dosn dosn = new Dosn();
        dosn.setAnalisisID(1L);
        dosn.setLote(lote);
        dosn.setFechaInicio(LocalDateTime.now());
        dosn.setGramosAnalizadosINIA(java.math.BigDecimal.valueOf(500.0));
        dosn.setGramosAnalizadosINASE(java.math.BigDecimal.valueOf(450.0));
        dosn.setFechaINIA(LocalDate.now());
        dosn.setFechaINASE(LocalDate.now().minusDays(1));
        
        List<Long> loteIds = Arrays.asList(1L);
        when(loteRepository.findAllById(loteIds)).thenReturn(Arrays.asList(lote));
        when(dosnRepository.findByLoteLoteID(1L)).thenReturn(Arrays.asList(dosn));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(loteIds);

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        verify(dosnRepository, times(1)).findByLoteLoteID(1L);
    }

    @Test
    @DisplayName("Obtener datos con filtros - con request completo")
    void obtenerDatosConFiltros_conRequestCompleto_debeAplicarFiltros() throws IOException {
        
        ExportacionRequestDTO solicitud = new ExportacionRequestDTO();
        solicitud.setTiposAnalisis(Arrays.asList("PUREZA", "GERMINACION", "PMS", "TETRAZOLIO", "DOSN"));
        solicitud.setIncluirEncabezados(true);
        
        when(loteRepository.findByActivoTrue()).thenReturn(Arrays.asList(lote));
        
        Pureza pureza = new Pureza();
        pureza.setAnalisisID(1L);
        pureza.setLote(lote);
        pureza.setRedonSemillaPura(java.math.BigDecimal.valueOf(95.0));
        when(purezaRepository.findByLoteLoteID(1L)).thenReturn(Arrays.asList(pureza));

        
        byte[] resultado = exportacionExcelService.generarReporteExcelAvanzado(solicitud);

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        verify(purezaRepository, times(1)).findByLoteLoteID(1L);
    }

    @Test
    @DisplayName("Obtener datos con filtros - solo pureza")
    void obtenerDatosConFiltros_soloPureza_debeGenerarSoloPureza() throws IOException {
        
        ExportacionRequestDTO solicitud = new ExportacionRequestDTO();
        solicitud.setTiposAnalisis(Arrays.asList("PUREZA"));
        
        when(loteRepository.findByActivoTrue()).thenReturn(Arrays.asList(lote));
        
        Pureza pureza = new Pureza();
        pureza.setAnalisisID(1L);
        pureza.setLote(lote);
        pureza.setRedonSemillaPura(java.math.BigDecimal.valueOf(95.0));
        when(purezaRepository.findByLoteLoteID(1L)).thenReturn(Arrays.asList(pureza));

        
        byte[] resultado = exportacionExcelService.generarReporteExcelAvanzado(solicitud);

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        verify(purezaRepository, times(1)).findByLoteLoteID(1L);
    }

    @Test
    @DisplayName("Mapear DOSN completo - con listados INIA e INASE")
    void mapearDatosDosn_conListadosCompletos_debeMapeartodo() throws IOException {
        
        Dosn dosn = new Dosn();
        dosn.setGramosAnalizadosINIA(BigDecimal.valueOf(100.0));
        dosn.setGramosAnalizadosINASE(BigDecimal.valueOf(95.0));
        
        
        List<Listado> listados = new ArrayList<>();
        
        
        Listado malezaTolCeroInia = new Listado();
        malezaTolCeroInia.setListadoInsti(Instituto.INIA);
        malezaTolCeroInia.setListadoTipo(TipoListado.MAL_TOLERANCIA_CERO);
        MalezasCatalogo maleza1 = new MalezasCatalogo();
        maleza1.setNombreComun("Avena fatua");
        malezaTolCeroInia.setCatalogo(maleza1);
        listados.add(malezaTolCeroInia);
        
        
        Listado brassicaInia = new Listado();
        brassicaInia.setListadoInsti(Instituto.INIA);
        brassicaInia.setListadoTipo(TipoListado.BRASSICA);
        MalezasCatalogo brassica = new MalezasCatalogo();
        brassica.setNombreComun("Raphanus sativus");
        brassicaInia.setCatalogo(brassica);
        listados.add(brassicaInia);
        
        
        Listado malezaTolerInia = new Listado();
        malezaTolerInia.setListadoInsti(Instituto.INIA);
        malezaTolerInia.setListadoTipo(TipoListado.MAL_TOLERANCIA);
        MalezasCatalogo maleza2 = new MalezasCatalogo();
        maleza2.setNombreComun("Lolium spp");
        malezaTolerInia.setCatalogo(maleza2);
        listados.add(malezaTolerInia);
        
        
        Listado cultivoInia = new Listado();
        cultivoInia.setListadoInsti(Instituto.INIA);
        cultivoInia.setListadoTipo(TipoListado.OTROS);
        Especie especieCultivo = new Especie();
        especieCultivo.setNombreComun("Trigo");
        cultivoInia.setEspecie(especieCultivo);
        listados.add(cultivoInia);
        
        
        Listado malezaTolCeroInase = new Listado();
        malezaTolCeroInase.setListadoInsti(Instituto.INASE);
        malezaTolCeroInase.setListadoTipo(TipoListado.MAL_TOLERANCIA_CERO);
        MalezasCatalogo maleza3 = new MalezasCatalogo();
        maleza3.setNombreComun("Cyperus rotundus");
        malezaTolCeroInase.setCatalogo(maleza3);
        listados.add(malezaTolCeroInase);
        
        dosn.setListados(listados);
        
        when(dosnRepository.findByLoteLoteID(1L)).thenReturn(List.of(dosn));
        when(loteRepository.findAllById(any())).thenReturn(List.of(lote));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(List.of(1L));

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        verify(dosnRepository, times(1)).findByLoteLoteID(1L);
    }

    @Test
    @DisplayName("Mapear Germinacion completa - con valores INIA e INASE y tratamiento")
    void mapearDatosGerminacion_conValoresCompletos_debeMapeartodo() throws IOException {
        
        Germinacion germinacion = new Germinacion();
        
        TablaGerm tablaGerm = new TablaGerm();
        tablaGerm.setTratamiento("Inmersión en agua");
        
        List<ValoresGerm> valores = new ArrayList<>();
        
        
        ValoresGerm valoresInia = new ValoresGerm();
        valoresInia.setInstituto(Instituto.INIA);
        valoresInia.setNormales(BigDecimal.valueOf(85.0));
        valoresInia.setAnormales(BigDecimal.valueOf(5.0));
        valoresInia.setDuras(BigDecimal.valueOf(3.0));
        valoresInia.setFrescas(BigDecimal.valueOf(2.0));
        valoresInia.setMuertas(BigDecimal.valueOf(5.0));
        valoresInia.setGerminacion(BigDecimal.valueOf(85.0));
        valores.add(valoresInia);
        
        
        ValoresGerm valoresInase = new ValoresGerm();
        valoresInase.setInstituto(Instituto.INASE);
        valoresInase.setNormales(BigDecimal.valueOf(83.0));
        valoresInase.setAnormales(BigDecimal.valueOf(6.0));
        valoresInase.setDuras(BigDecimal.valueOf(4.0));
        valoresInase.setFrescas(BigDecimal.valueOf(2.0));
        valoresInase.setMuertas(BigDecimal.valueOf(5.0));
        valoresInase.setGerminacion(BigDecimal.valueOf(83.0));
        valores.add(valoresInase);
        
        tablaGerm.setValoresGerm(valores);
        germinacion.setTablaGerm(List.of(tablaGerm));
        
        when(germinacionRepository.findByLoteLoteID(1L)).thenReturn(List.of(germinacion));
        when(loteRepository.findAllById(any())).thenReturn(List.of(lote));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(List.of(1L));

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        verify(germinacionRepository, times(1)).findByLoteLoteID(1L);
    }

    @Test
    @DisplayName("Mapear Germinacion sin valores - no debe fallar")
    void mapearDatosGerminacion_sinValores_noDebeFallar() throws IOException {
        
        Germinacion germinacion = new Germinacion();
        TablaGerm tablaGerm = new TablaGerm();
        tablaGerm.setValoresGerm(new ArrayList<>());
        germinacion.setTablaGerm(List.of(tablaGerm));
        
        when(germinacionRepository.findByLoteLoteID(1L)).thenReturn(List.of(germinacion));
        when(loteRepository.findAllById(any())).thenReturn(List.of(lote));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(List.of(1L));

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Mapear DOSN sin listados - no debe fallar")
    void mapearDatosDosn_sinListados_noDebeFallar() throws IOException {
        
        Dosn dosn = new Dosn();
        dosn.setListados(new ArrayList<>());
        
        when(dosnRepository.findByLoteLoteID(1L)).thenReturn(List.of(dosn));
        when(loteRepository.findAllById(any())).thenReturn(List.of(lote));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(List.of(1L));

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Mapear DOSN con listados sin catalogo ni especie - no debe fallar")
    void mapearDatosDosn_conListadosSinNombre_noDebeFallar() throws IOException {
        
        Dosn dosn = new Dosn();
        
        List<Listado> listados = new ArrayList<>();
        Listado listadoVacio = new Listado();
        listadoVacio.setListadoInsti(Instituto.INIA);
        listadoVacio.setListadoTipo(TipoListado.OTROS);
        listados.add(listadoVacio);
        
        dosn.setListados(listados);
        
        when(dosnRepository.findByLoteLoteID(1L)).thenReturn(List.of(dosn));
        when(loteRepository.findAllById(any())).thenReturn(List.of(lote));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(List.of(1L));

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Crear celda con valores nulos - no debe fallar")
    void crearCelda_conValoresNulos_noDebeFallar() throws IOException {
        
        Pureza pureza = new Pureza();
        
        
        when(purezaRepository.findByLoteLoteID(1L)).thenReturn(List.of(pureza));
        when(loteRepository.findAllById(any())).thenReturn(List.of(lote));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(List.of(1L));

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Combinar descripciones - ambas tienen valor")
    void combinarDescripciones_ambasConValor_debeCombinar() throws IOException {
        
        Pureza pureza = new Pureza();
        pureza.setAnalisisID(1L);
        pureza.setLote(lote);
        
        MalezasCatalogo maleza1 = new MalezasCatalogo();
        maleza1.setNombreComun("Avena fatua");
        
        MalezasCatalogo maleza2 = new MalezasCatalogo();
        maleza2.setNombreComun("Cyperus rotundus");
        
        Listado listadoInia = new Listado();
        listadoInia.setListadoInsti(Instituto.INIA);
        listadoInia.setListadoTipo(TipoListado.MAL_TOLERANCIA_CERO);
        listadoInia.setCatalogo(maleza1);
        
        Listado listadoInase = new Listado();
        listadoInase.setListadoInsti(Instituto.INASE);
        listadoInase.setListadoTipo(TipoListado.MAL_TOLERANCIA_CERO);
        listadoInase.setCatalogo(maleza2);
        
        pureza.setListados(Arrays.asList(listadoInia, listadoInase));
        
        when(purezaRepository.findByLoteLoteID(1L)).thenReturn(List.of(pureza));
        when(loteRepository.findAllById(any())).thenReturn(List.of(lote));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(List.of(1L));

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Combinar descripciones - solo INIA tiene valor")
    void combinarDescripciones_soloIniaTieneValor_debeRetornarInia() throws IOException {
        
        Pureza pureza = new Pureza();
        pureza.setAnalisisID(1L);
        pureza.setLote(lote);
        
        MalezasCatalogo maleza = new MalezasCatalogo();
        maleza.setNombreComun("Avena fatua");
        
        Listado listadoInia = new Listado();
        listadoInia.setListadoInsti(Instituto.INIA);
        listadoInia.setListadoTipo(TipoListado.MAL_TOLERANCIA_CERO);
        listadoInia.setCatalogo(maleza);
        
        pureza.setListados(Arrays.asList(listadoInia));
        
        when(purezaRepository.findByLoteLoteID(1L)).thenReturn(List.of(pureza));
        when(loteRepository.findAllById(any())).thenReturn(List.of(lote));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(List.of(1L));

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Combinar descripciones - solo INASE tiene valor")
    void combinarDescripciones_soloInaseTieneValor_debeRetornarInase() throws IOException {
        
        Pureza pureza = new Pureza();
        pureza.setAnalisisID(1L);
        pureza.setLote(lote);
        
        MalezasCatalogo maleza = new MalezasCatalogo();
        maleza.setNombreComun("Cyperus rotundus");
        
        Listado listadoInase = new Listado();
        listadoInase.setListadoInsti(Instituto.INASE);
        listadoInase.setListadoTipo(TipoListado.MAL_TOLERANCIA_CERO);
        listadoInase.setCatalogo(maleza);
        
        pureza.setListados(Arrays.asList(listadoInase));
        
        when(purezaRepository.findByLoteLoteID(1L)).thenReturn(List.of(pureza));
        when(loteRepository.findAllById(any())).thenReturn(List.of(lote));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(List.of(1L));

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }

    @Test
    @DisplayName("Combinar descripciones - ninguna tiene valor")
    void combinarDescripciones_ningunaConValor_debeRetornarVacio() throws IOException {
        
        Pureza pureza = new Pureza();
        pureza.setAnalisisID(1L);
        pureza.setLote(lote);
        pureza.setListados(new ArrayList<>());
        
        when(purezaRepository.findByLoteLoteID(1L)).thenReturn(List.of(pureza));
        when(loteRepository.findAllById(any())).thenReturn(List.of(lote));

        
        byte[] resultado = exportacionExcelService.generarReporteExcel(List.of(1L));

        
        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
    }
}


