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

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.MalezasCatalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.MalezasCatalogoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.MalezasCatalogoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.MalezasCatalogoDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para MalezasCatalogoService
 * 
 * Funcionalidades testeadas:
 * - Obtener todas las malezas activas
 * - Obtener malezas inactivas
 * - Búsqueda por nombre común
 * - Búsqueda por nombre científico
 * - Obtener maleza por ID
 * - Crear nueva maleza
 * - Actualizar maleza existente
 * - Soft delete (eliminar)
 * - Reactivar maleza
 * - Obtener entidad por ID (para uso interno)
 * - Paginación simple
 * - Paginación con filtros dinámicos
 * - Mapeo de entidad a DTO
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de MalezasCatalogoService")
class MalezasCatalogoServiceTest {

    @Mock
    private MalezasCatalogoRepository repository;

    @InjectMocks
    private MalezasCatalogoService service;

    private MalezasCatalogo maleza1;
    private MalezasCatalogo maleza2;
    private MalezasCatalogo maleza3Inactiva;
    private MalezasCatalogoRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        // ARRANGE: Preparar malezas activas
        maleza1 = new MalezasCatalogo();
        maleza1.setCatalogoID(1L);
        maleza1.setNombreComun("Gramilla");
        maleza1.setNombreCientifico("Cynodon dactylon");
        maleza1.setActivo(true);

        maleza2 = new MalezasCatalogo();
        maleza2.setCatalogoID(2L);
        maleza2.setNombreComun("Yuyo Colorado");
        maleza2.setNombreCientifico("Amaranthus quitensis");
        maleza2.setActivo(true);

        // ARRANGE: Preparar maleza inactiva
        maleza3Inactiva = new MalezasCatalogo();
        maleza3Inactiva.setCatalogoID(3L);
        maleza3Inactiva.setNombreComun("Capín");
        maleza3Inactiva.setNombreCientifico("Echinochloa crus-galli");
        maleza3Inactiva.setActivo(false);

        // ARRANGE: Preparar request DTO
        requestDTO = new MalezasCatalogoRequestDTO();
        requestDTO.setNombreComun("Pasto Horqueta");
        requestDTO.setNombreCientifico("Paspalum notatum");
    }

    @Test
    @DisplayName("Obtener todos - debe retornar solo malezas activas")
    void obtenerTodos_debeRetornarSoloActivas() {
        
        List<MalezasCatalogo> malezasActivas = Arrays.asList(maleza1, maleza2);
        when(repository.findByActivoTrue()).thenReturn(malezasActivas);

        
        List<MalezasCatalogoDTO> resultado = service.obtenerTodos();

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(2, resultado.size(), "Debe retornar 2 malezas activas");
        assertEquals("Gramilla", resultado.get(0).getNombreComun(), "Primera maleza debe ser Gramilla");
        assertEquals("Yuyo Colorado", resultado.get(1).getNombreComun(), "Segunda maleza debe ser Yuyo Colorado");
        assertTrue(resultado.get(0).getActivo(), "Primera maleza debe estar activa");
        assertTrue(resultado.get(1).getActivo(), "Segunda maleza debe estar activa");
        
        verify(repository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("Obtener todos - debe retornar lista vacía si no hay malezas activas")
    void obtenerTodos_debeRetornarListaVacia() {
        
        when(repository.findByActivoTrue()).thenReturn(new ArrayList<>());

        
        List<MalezasCatalogoDTO> resultado = service.obtenerTodos();

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(0, resultado.size(), "La lista debe estar vacía");
    }

    @Test
    @DisplayName("Obtener inactivos - debe retornar solo malezas inactivas")
    void obtenerInactivos_debeRetornarSoloInactivas() {
        
        List<MalezasCatalogo> malezasInactivas = Arrays.asList(maleza3Inactiva);
        when(repository.findByActivoFalse()).thenReturn(malezasInactivas);

        
        List<MalezasCatalogoDTO> resultado = service.obtenerInactivos();

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(1, resultado.size(), "Debe retornar 1 maleza inactiva");
        assertEquals("Capín", resultado.get(0).getNombreComun(), "La maleza debe ser Capín");
        assertFalse(resultado.get(0).getActivo(), "La maleza debe estar inactiva");
        
        verify(repository, times(1)).findByActivoFalse();
    }

    @Test
    @DisplayName("Buscar por nombre común - debe encontrar malezas que coincidan")
    void buscarPorNombreComun_debeEncontrarCoincidencias() {
        
        List<MalezasCatalogo> malezasEncontradas = Arrays.asList(maleza1);
        when(repository.findByNombreComunContainingIgnoreCaseAndActivoTrue("Gram"))
                .thenReturn(malezasEncontradas);

        
        List<MalezasCatalogoDTO> resultado = service.buscarPorNombreComun("Gram");

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(1, resultado.size(), "Debe encontrar 1 maleza");
        assertEquals("Gramilla", resultado.get(0).getNombreComun(), "Debe encontrar Gramilla");
        
        verify(repository, times(1)).findByNombreComunContainingIgnoreCaseAndActivoTrue("Gram");
    }

    @Test
    @DisplayName("Buscar por nombre común - debe retornar lista vacía si no hay coincidencias")
    void buscarPorNombreComun_debeRetornarListaVaciaSinCoincidencias() {
        
        when(repository.findByNombreComunContainingIgnoreCaseAndActivoTrue("NoExiste"))
                .thenReturn(new ArrayList<>());

        
        List<MalezasCatalogoDTO> resultado = service.buscarPorNombreComun("NoExiste");

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(0, resultado.size(), "La lista debe estar vacía");
    }

    @Test
    @DisplayName("Buscar por nombre científico - debe encontrar malezas que coincidan")
    void buscarPorNombreCientifico_debeEncontrarCoincidencias() {
        
        List<MalezasCatalogo> malezasEncontradas = Arrays.asList(maleza2);
        when(repository.findByNombreCientificoContainingIgnoreCaseAndActivoTrue("Amaranthus"))
                .thenReturn(malezasEncontradas);

        
        List<MalezasCatalogoDTO> resultado = service.buscarPorNombreCientifico("Amaranthus");

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(1, resultado.size(), "Debe encontrar 1 maleza");
        assertEquals("Amaranthus quitensis", resultado.get(0).getNombreCientifico(), 
                "Debe encontrar Amaranthus quitensis");
        
        verify(repository, times(1)).findByNombreCientificoContainingIgnoreCaseAndActivoTrue("Amaranthus");
    }

    @Test
    @DisplayName("Buscar por nombre científico - debe ser case insensitive")
    void buscarPorNombreCientifico_debeSerCaseInsensitive() {
        
        List<MalezasCatalogo> malezasEncontradas = Arrays.asList(maleza1);
        when(repository.findByNombreCientificoContainingIgnoreCaseAndActivoTrue("CYNODON"))
                .thenReturn(malezasEncontradas);

        
        List<MalezasCatalogoDTO> resultado = service.buscarPorNombreCientifico("CYNODON");

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(1, resultado.size(), "Debe encontrar 1 maleza");
        assertEquals("Cynodon dactylon", resultado.get(0).getNombreCientifico(), 
                "Debe encontrar Cynodon dactylon");
    }

    @Test
    @DisplayName("Obtener por ID - debe retornar maleza existente")
    void obtenerPorId_debeRetornarMalezaExistente() {
        
        when(repository.findById(1L)).thenReturn(Optional.of(maleza1));

        
        MalezasCatalogoDTO resultado = service.obtenerPorId(1L);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(1L, resultado.getCatalogoID(), "El ID debe ser 1");
        assertEquals("Gramilla", resultado.getNombreComun(), "El nombre común debe ser Gramilla");
        assertEquals("Cynodon dactylon", resultado.getNombreCientifico(), 
                "El nombre científico debe ser Cynodon dactylon");
        
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener por ID - debe retornar null si no existe")
    void obtenerPorId_debeRetornarNullSiNoExiste() {
        
        when(repository.findById(999L)).thenReturn(Optional.empty());

        
        MalezasCatalogoDTO resultado = service.obtenerPorId(999L);

        
        assertNull(resultado, "El resultado debe ser nulo");
        verify(repository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Crear - debe crear nueva maleza con estado activo")
    void crear_debeCrearNuevaMaleza() {
        
        MalezasCatalogo malezaNueva = new MalezasCatalogo();
        malezaNueva.setCatalogoID(4L);
        malezaNueva.setNombreComun("Pasto Horqueta");
        malezaNueva.setNombreCientifico("Paspalum notatum");
        malezaNueva.setActivo(true);

        when(repository.save(any(MalezasCatalogo.class))).thenReturn(malezaNueva);

        
        MalezasCatalogoDTO resultado = service.crear(requestDTO);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(4L, resultado.getCatalogoID(), "El ID debe ser 4");
        assertEquals("Pasto Horqueta", resultado.getNombreComun(), "El nombre común debe coincidir");
        assertEquals("Paspalum notatum", resultado.getNombreCientifico(), "El nombre científico debe coincidir");
        assertTrue(resultado.getActivo(), "La maleza debe estar activa por defecto");
        
        verify(repository, times(1)).save(any(MalezasCatalogo.class));
    }

    @Test
    @DisplayName("Actualizar - debe actualizar maleza existente")
    void actualizar_debeActualizarMalezaExistente() {
        
        MalezasCatalogo malezaActualizada = new MalezasCatalogo();
        malezaActualizada.setCatalogoID(1L);
        malezaActualizada.setNombreComun("Pasto Horqueta");
        malezaActualizada.setNombreCientifico("Paspalum notatum");
        malezaActualizada.setActivo(true);

        when(repository.findById(1L)).thenReturn(Optional.of(maleza1));
        when(repository.save(any(MalezasCatalogo.class))).thenReturn(malezaActualizada);

        
        MalezasCatalogoDTO resultado = service.actualizar(1L, requestDTO);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(1L, resultado.getCatalogoID(), "El ID debe mantenerse");
        assertEquals("Pasto Horqueta", resultado.getNombreComun(), "El nombre común debe actualizarse");
        assertEquals("Paspalum notatum", resultado.getNombreCientifico(), "El nombre científico debe actualizarse");
        
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(MalezasCatalogo.class));
    }

    @Test
    @DisplayName("Actualizar - debe retornar null si la maleza no existe")
    void actualizar_debeRetornarNullSiNoExiste() {
        
        when(repository.findById(999L)).thenReturn(Optional.empty());

        
        MalezasCatalogoDTO resultado = service.actualizar(999L, requestDTO);

        
        assertNull(resultado, "El resultado debe ser nulo");
        verify(repository, times(1)).findById(999L);
        verify(repository, never()).save(any(MalezasCatalogo.class));
    }

    @Test
    @DisplayName("Eliminar - debe realizar soft delete (marcar como inactivo)")
    void eliminar_debeRealizarSoftDelete() {
        
        when(repository.findById(1L)).thenReturn(Optional.of(maleza1));
        when(repository.save(any(MalezasCatalogo.class))).thenAnswer(invocation -> {
            MalezasCatalogo maleza = invocation.getArgument(0);
            assertFalse(maleza.getActivo(), "La maleza debe marcarse como inactiva");
            return maleza;
        });

        
        service.eliminar(1L);

        
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(MalezasCatalogo.class));
    }

    @Test
    @DisplayName("Eliminar - no debe hacer nada si la maleza no existe")
    void eliminar_noDebeHacerNadaSiNoExiste() {
        
        when(repository.findById(999L)).thenReturn(Optional.empty());

        
        service.eliminar(999L);

        
        verify(repository, times(1)).findById(999L);
        verify(repository, never()).save(any(MalezasCatalogo.class));
    }

    @Test
    @DisplayName("Reactivar - debe marcar maleza como activa")
    void reactivar_debeMarcarComoActiva() {
        
        MalezasCatalogo malezaReactivada = new MalezasCatalogo();
        malezaReactivada.setCatalogoID(3L);
        malezaReactivada.setNombreComun("Capín");
        malezaReactivada.setNombreCientifico("Echinochloa crus-galli");
        malezaReactivada.setActivo(true);

        when(repository.findById(3L)).thenReturn(Optional.of(maleza3Inactiva));
        when(repository.save(any(MalezasCatalogo.class))).thenReturn(malezaReactivada);

        
        MalezasCatalogoDTO resultado = service.reactivar(3L);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(3L, resultado.getCatalogoID(), "El ID debe ser 3");
        assertTrue(resultado.getActivo(), "La maleza debe estar activa");
        
        verify(repository, times(1)).findById(3L);
        verify(repository, times(1)).save(any(MalezasCatalogo.class));
    }

    @Test
    @DisplayName("Reactivar - debe retornar null si la maleza no existe")
    void reactivar_debeRetornarNullSiNoExiste() {
        
        when(repository.findById(999L)).thenReturn(Optional.empty());

        
        MalezasCatalogoDTO resultado = service.reactivar(999L);

        
        assertNull(resultado, "El resultado debe ser nulo");
        verify(repository, times(1)).findById(999L);
        verify(repository, never()).save(any(MalezasCatalogo.class));
    }

    @Test
    @DisplayName("Obtener entidad por ID - debe retornar entidad completa")
    void obtenerEntidadPorId_debeRetornarEntidad() {
        
        when(repository.findById(1L)).thenReturn(Optional.of(maleza1));

        
        MalezasCatalogo resultado = service.obtenerEntidadPorId(1L);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(1L, resultado.getCatalogoID(), "El ID debe ser 1");
        assertEquals("Gramilla", resultado.getNombreComun(), "El nombre común debe ser Gramilla");
        assertTrue(resultado.getActivo(), "La maleza debe estar activa");
        
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener entidad por ID - debe retornar null si no existe")
    void obtenerEntidadPorId_debeRetornarNullSiNoExiste() {
        
        when(repository.findById(999L)).thenReturn(Optional.empty());

        
        MalezasCatalogo resultado = service.obtenerEntidadPorId(999L);

        
        assertNull(resultado, "El resultado debe ser nulo");
        verify(repository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Obtener malezas paginadas - debe retornar página de malezas activas")
    void obtenerMalezasPaginadas_debeRetornarPagina() {
        
        Pageable pageable = PageRequest.of(0, 10);
        List<MalezasCatalogo> malezas = Arrays.asList(maleza1, maleza2);
        Page<MalezasCatalogo> page = new PageImpl<>(malezas, pageable, malezas.size());
        
        when(repository.findByActivoTrueOrderByNombreComunAsc(pageable)).thenReturn(page);

        
        Page<MalezasCatalogoDTO> resultado = service.obtenerMalezasPaginadas(pageable);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(2, resultado.getTotalElements(), "Debe haber 2 elementos en total");
        assertEquals(2, resultado.getContent().size(), "Debe haber 2 elementos en la página");
        assertEquals("Gramilla", resultado.getContent().get(0).getNombreComun(), 
                "Primera maleza debe ser Gramilla");
        
        verify(repository, times(1)).findByActivoTrueOrderByNombreComunAsc(pageable);
    }

    @Test
    @DisplayName("Obtener malezas paginadas con filtros - sin filtros debe retornar todas ordenadas")
    void obtenerMalezasPaginadasConFiltros_sinFiltros() {
        
        Pageable pageable = PageRequest.of(0, 10);
        List<MalezasCatalogo> todasLasMalezas = Arrays.asList(maleza1, maleza2, maleza3Inactiva);
        Page<MalezasCatalogo> page = new PageImpl<>(todasLasMalezas, pageable, todasLasMalezas.size());
        
        when(repository.findAllByOrderByNombreComunAsc(pageable)).thenReturn(page);

        
        Page<MalezasCatalogoDTO> resultado = service.obtenerMalezasPaginadasConFiltros(pageable, null, null);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(3, resultado.getTotalElements(), "Debe haber 3 elementos en total");
        
        verify(repository, times(1)).findAllByOrderByNombreComunAsc(pageable);
    }

    @Test
    @DisplayName("Obtener malezas paginadas con filtros - solo activas")
    void obtenerMalezasPaginadasConFiltros_soloActivas() {
        
        Pageable pageable = PageRequest.of(0, 10);
        List<MalezasCatalogo> malezasActivas = Arrays.asList(maleza1, maleza2);
        Page<MalezasCatalogo> page = new PageImpl<>(malezasActivas, pageable, malezasActivas.size());
        
        when(repository.findByActivoTrueOrderByNombreComunAsc(pageable)).thenReturn(page);

        
        Page<MalezasCatalogoDTO> resultado = service.obtenerMalezasPaginadasConFiltros(pageable, null, true);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(2, resultado.getTotalElements(), "Debe haber 2 elementos activos");
        assertTrue(resultado.getContent().stream().allMatch(MalezasCatalogoDTO::getActivo), 
                "Todas deben estar activas");
        
        verify(repository, times(1)).findByActivoTrueOrderByNombreComunAsc(pageable);
    }

    @Test
    @DisplayName("Obtener malezas paginadas con filtros - solo inactivas")
    void obtenerMalezasPaginadasConFiltros_soloInactivas() {
        
        Pageable pageable = PageRequest.of(0, 10);
        List<MalezasCatalogo> malezasInactivas = Arrays.asList(maleza3Inactiva);
        Page<MalezasCatalogo> page = new PageImpl<>(malezasInactivas, pageable, malezasInactivas.size());
        
        when(repository.findByActivoFalseOrderByNombreComunAsc(pageable)).thenReturn(page);

        
        Page<MalezasCatalogoDTO> resultado = service.obtenerMalezasPaginadasConFiltros(pageable, null, false);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(1, resultado.getTotalElements(), "Debe haber 1 elemento inactivo");
        assertFalse(resultado.getContent().get(0).getActivo(), "Debe estar inactiva");
        
        verify(repository, times(1)).findByActivoFalseOrderByNombreComunAsc(pageable);
    }

    @Test
    @DisplayName("Obtener malezas paginadas con filtros - con búsqueda en activas")
    void obtenerMalezasPaginadasConFiltros_conBusquedaEnActivas() {
        
        Pageable pageable = PageRequest.of(0, 10);
        String searchTerm = "Gram";
        
        when(repository.findByNombreComunContainingIgnoreCaseAndActivoTrue(searchTerm))
                .thenReturn(Arrays.asList(maleza1));
        when(repository.findByNombreCientificoContainingIgnoreCaseAndActivoTrue(searchTerm))
                .thenReturn(new ArrayList<>());

        
        Page<MalezasCatalogoDTO> resultado = service.obtenerMalezasPaginadasConFiltros(pageable, searchTerm, true);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(1, resultado.getTotalElements(), "Debe encontrar 1 elemento");
        assertEquals("Gramilla", resultado.getContent().get(0).getNombreComun(), 
                "Debe encontrar Gramilla");
        
        verify(repository, times(1)).findByNombreComunContainingIgnoreCaseAndActivoTrue(searchTerm);
        verify(repository, times(1)).findByNombreCientificoContainingIgnoreCaseAndActivoTrue(searchTerm);
    }

    @Test
    @DisplayName("Obtener malezas paginadas con filtros - con búsqueda en inactivas")
    void obtenerMalezasPaginadasConFiltros_conBusquedaEnInactivas() {
        
        Pageable pageable = PageRequest.of(0, 10);
        String searchTerm = "Capín";
        
        List<MalezasCatalogo> todasLasMalezas = Arrays.asList(maleza1, maleza2, maleza3Inactiva);
        when(repository.findAll()).thenReturn(todasLasMalezas);

        
        Page<MalezasCatalogoDTO> resultado = service.obtenerMalezasPaginadasConFiltros(pageable, searchTerm, false);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(1, resultado.getTotalElements(), "Debe encontrar 1 elemento inactivo");
        assertEquals("Capín", resultado.getContent().get(0).getNombreComun(), 
                "Debe encontrar Capín");
        assertFalse(resultado.getContent().get(0).getActivo(), "Debe estar inactiva");
        
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Obtener malezas paginadas con filtros - con búsqueda sin filtro de activo")
    void obtenerMalezasPaginadasConFiltros_conBusquedaSinFiltroActivo() {
        
        Pageable pageable = PageRequest.of(0, 10);
        String searchTerm = "o"; // Busca la letra 'o' que está en varios nombres
        
        List<MalezasCatalogo> todasLasMalezas = Arrays.asList(maleza1, maleza2, maleza3Inactiva);
        when(repository.findAll()).thenReturn(todasLasMalezas);

        
        Page<MalezasCatalogoDTO> resultado = service.obtenerMalezasPaginadasConFiltros(pageable, searchTerm, null);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        // Yuyo Colorado y Cynodon dactylon contienen 'o'
        assertTrue(resultado.getTotalElements() >= 1, "Debe encontrar al menos 1 elemento");
        
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Obtener malezas paginadas con filtros - búsqueda con término vacío")
    void obtenerMalezasPaginadasConFiltros_busquedaTerminoVacio() {
        
        Pageable pageable = PageRequest.of(0, 10);
        String searchTerm = "   "; // Término vacío con espacios
        
        List<MalezasCatalogo> malezasActivas = Arrays.asList(maleza1, maleza2);
        Page<MalezasCatalogo> page = new PageImpl<>(malezasActivas, pageable, malezasActivas.size());
        
        when(repository.findByActivoTrueOrderByNombreComunAsc(pageable)).thenReturn(page);

        
        Page<MalezasCatalogoDTO> resultado = service.obtenerMalezasPaginadasConFiltros(pageable, searchTerm, true);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(2, resultado.getTotalElements(), "Debe retornar todas las activas");
        
        verify(repository, times(1)).findByActivoTrueOrderByNombreComunAsc(pageable);
    }

    @Test
    @DisplayName("Obtener malezas paginadas con filtros - búsqueda por nombre científico")
    void obtenerMalezasPaginadasConFiltros_busquedaPorNombreCientifico() {
        
        Pageable pageable = PageRequest.of(0, 10);
        String searchTerm = "dactylon";
        
        when(repository.findByNombreComunContainingIgnoreCaseAndActivoTrue(searchTerm))
                .thenReturn(new ArrayList<>());
        when(repository.findByNombreCientificoContainingIgnoreCaseAndActivoTrue(searchTerm))
                .thenReturn(Arrays.asList(maleza1));

        
        Page<MalezasCatalogoDTO> resultado = service.obtenerMalezasPaginadasConFiltros(pageable, searchTerm, true);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(1, resultado.getTotalElements(), "Debe encontrar 1 elemento");
        assertEquals("Cynodon dactylon", resultado.getContent().get(0).getNombreCientifico(), 
                "Debe encontrar Cynodon dactylon");
        
        verify(repository, times(1)).findByNombreComunContainingIgnoreCaseAndActivoTrue(searchTerm);
        verify(repository, times(1)).findByNombreCientificoContainingIgnoreCaseAndActivoTrue(searchTerm);
    }

    @Test
    @DisplayName("Obtener malezas paginadas con filtros - búsqueda que coincide en ambos campos")
    void obtenerMalezasPaginadasConFiltros_busquedaEnAmbosCampos() {
        
        Pageable pageable = PageRequest.of(0, 10);
        String searchTerm = "Yuyo";
        
        // Coincide en nombre común
        when(repository.findByNombreComunContainingIgnoreCaseAndActivoTrue(searchTerm))
                .thenReturn(Arrays.asList(maleza2));
        // No coincide en nombre científico
        when(repository.findByNombreCientificoContainingIgnoreCaseAndActivoTrue(searchTerm))
                .thenReturn(new ArrayList<>());

        
        Page<MalezasCatalogoDTO> resultado = service.obtenerMalezasPaginadasConFiltros(pageable, searchTerm, true);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(1, resultado.getTotalElements(), "Debe encontrar 1 elemento");
        assertEquals("Yuyo Colorado", resultado.getContent().get(0).getNombreComun(), 
                "Debe encontrar Yuyo Colorado");
    }

    @Test
    @DisplayName("Mapear entidad a DTO - debe mapear todos los campos correctamente")
    void mapearEntidadADTO_debeMapeaTodosLosCampos() {
        
        when(repository.findById(1L)).thenReturn(Optional.of(maleza1));

        
        MalezasCatalogoDTO resultado = service.obtenerPorId(1L);

        
        assertNotNull(resultado, "El DTO no debe ser nulo");
        assertEquals(maleza1.getCatalogoID(), resultado.getCatalogoID(), "El ID debe coincidir");
        assertEquals(maleza1.getNombreComun(), resultado.getNombreComun(), "El nombre común debe coincidir");
        assertEquals(maleza1.getNombreCientifico(), resultado.getNombreCientifico(), 
                "El nombre científico debe coincidir");
        assertEquals(maleza1.getActivo(), resultado.getActivo(), "El estado activo debe coincidir");
    }

    @Test
    @DisplayName("Paginación - debe respetar el tamaño de página")
    void paginacion_debeRespetarTamanoPagina() {
        
        Pageable pageable = PageRequest.of(0, 1); // Solo 1 elemento por página
        List<MalezasCatalogo> malezas = Arrays.asList(maleza1);
        Page<MalezasCatalogo> page = new PageImpl<>(malezas, pageable, 2); // 2 elementos totales
        
        when(repository.findByActivoTrueOrderByNombreComunAsc(pageable)).thenReturn(page);

        
        Page<MalezasCatalogoDTO> resultado = service.obtenerMalezasPaginadas(pageable);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(2, resultado.getTotalElements(), "Debe haber 2 elementos en total");
        assertEquals(1, resultado.getContent().size(), "Debe haber 1 elemento en la página");
        assertEquals(2, resultado.getTotalPages(), "Debe haber 2 páginas en total");
    }
}
