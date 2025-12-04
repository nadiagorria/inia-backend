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

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Cultivar;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.CultivarRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.CultivarRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.CultivarDTO;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de CultivarService")
class CultivarServiceTest {

    @Mock
    private CultivarRepository cultivarRepository;

    @Mock
    private EspecieService especieService;

    @InjectMocks
    private CultivarService cultivarService;

    private CultivarRequestDTO cultivarRequestDTO;
    private Cultivar cultivar;
    private Especie especie;

    @BeforeEach
    void setUp() {
        especie = new Especie();
        especie.setEspecieID(1L);
        especie.setNombreComun("Trigo");

        cultivarRequestDTO = new CultivarRequestDTO();
        cultivarRequestDTO.setNombre("Cultivar Test");
        cultivarRequestDTO.setEspecieID(1L);

        cultivar = new Cultivar();
        cultivar.setCultivarID(1L);
        cultivar.setNombre("Cultivar Test");
        cultivar.setEspecie(especie);
        cultivar.setActivo(true);
    }

    @Test
    @DisplayName("Obtener todos los cultivares activos - debe retornar lista")
    void obtenerTodos_debeRetornarListaActivos() {
        when(cultivarRepository.findByActivoTrue()).thenReturn(Arrays.asList(cultivar));

        List<CultivarDTO> resultado = cultivarService.obtenerTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(cultivarRepository, times(1)).findByActivoTrue();
    }

    @Test
    @DisplayName("Obtener cultivares inactivos - debe retornar solo inactivos")
    void obtenerInactivos_debeRetornarSoloInactivos() {
        cultivar.setActivo(false);
        when(cultivarRepository.findByActivoFalse()).thenReturn(Arrays.asList(cultivar));

        List<CultivarDTO> resultado = cultivarService.obtenerInactivos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(cultivarRepository, times(1)).findByActivoFalse();
    }

    @Test
    @DisplayName("Obtener todos con filtro null - debe retornar todos")
    void obtenerTodos_conFiltroNull_debeRetornarTodos() {
        when(cultivarRepository.findAll()).thenReturn(Arrays.asList(cultivar));

        List<CultivarDTO> resultado = cultivarService.obtenerTodos(null);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(cultivarRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Obtener cultivares por especie - debe retornar cultivares de la especie")
    void obtenerPorEspecie_debeRetornarCultivaresDeLaEspecie() {
        when(cultivarRepository.findByEspecieEspecieIDAndActivoTrue(1L)).thenReturn(Arrays.asList(cultivar));

        List<CultivarDTO> resultado = cultivarService.obtenerPorEspecie(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getEspecieID());
        verify(cultivarRepository, times(1)).findByEspecieEspecieIDAndActivoTrue(1L);
    }

    @Test
    @DisplayName("Buscar por nombre - debe retornar coincidencias")
    void buscarPorNombre_debeRetornarCoincidencias() {
        when(cultivarRepository.findByNombreContainingIgnoreCaseAndActivoTrue("Test")).thenReturn(Arrays.asList(cultivar));

        List<CultivarDTO> resultado = cultivarService.buscarPorNombre("Test");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getNombre().contains("Test"));
        verify(cultivarRepository, times(1)).findByNombreContainingIgnoreCaseAndActivoTrue("Test");
    }

    @Test
    @DisplayName("Obtener por ID - debe retornar cultivar existente")
    void obtenerPorId_cultivarExistente_debeRetornarCultivar() {
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));

        CultivarDTO resultado = cultivarService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getCultivarID());
        assertEquals("Cultivar Test", resultado.getNombre());
        verify(cultivarRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener por ID inexistente - debe retornar null")
    void obtenerPorId_cultivarInexistente_debeRetornarNull() {
        when(cultivarRepository.findById(999L)).thenReturn(Optional.empty());

        CultivarDTO resultado = cultivarService.obtenerPorId(999L);

        assertNull(resultado);
        verify(cultivarRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Crear cultivar válido - debe guardar y retornar DTO")
    void crear_cultivarValido_debeGuardarYRetornarDTO() {
        when(especieService.obtenerEntidadPorId(1L)).thenReturn(especie);
        when(cultivarRepository.save(any(Cultivar.class))).thenReturn(cultivar);

        CultivarDTO resultado = cultivarService.crear(cultivarRequestDTO);

        assertNotNull(resultado);
        assertEquals("Cultivar Test", resultado.getNombre());
        verify(especieService, times(1)).obtenerEntidadPorId(1L);
        verify(cultivarRepository, times(1)).save(any(Cultivar.class));
    }

    @Test
    @DisplayName("Crear cultivar con especie inexistente - debe lanzar excepción")
    void crear_especieInexistente_debeLanzarExcepcion() {
        when(especieService.obtenerEntidadPorId(999L)).thenReturn(null);
        cultivarRequestDTO.setEspecieID(999L);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cultivarService.crear(cultivarRequestDTO);
        });

        assertTrue(exception.getMessage().contains("no encontrada"));
        verify(cultivarRepository, never()).save(any(Cultivar.class));
    }

    @Test
    @DisplayName("Actualizar cultivar - debe actualizar y retornar DTO")
    void actualizar_debeActualizarYRetornarDTO() {
        cultivarRequestDTO.setNombre("Cultivar Actualizado");
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(especieService.obtenerEntidadPorId(1L)).thenReturn(especie);
        when(cultivarRepository.save(any(Cultivar.class))).thenReturn(cultivar);

        CultivarDTO resultado = cultivarService.actualizar(1L, cultivarRequestDTO);

        assertNotNull(resultado);
        verify(cultivarRepository, times(1)).save(any(Cultivar.class));
    }

    @Test
    @DisplayName("Actualizar cultivar inexistente - debe retornar null")
    void actualizar_cultivarInexistente_debeRetornarNull() {
        when(cultivarRepository.findById(999L)).thenReturn(Optional.empty());

        CultivarDTO resultado = cultivarService.actualizar(999L, cultivarRequestDTO);

        assertNull(resultado);
        verify(cultivarRepository, never()).save(any(Cultivar.class));
    }

    @Test
    @DisplayName("Eliminar cultivar - debe cambiar activo a false")
    void eliminar_debeCambiarActivoAFalse() {
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(cultivarRepository.save(any(Cultivar.class))).thenReturn(cultivar);

        cultivarService.eliminar(1L);

        verify(cultivarRepository, times(1)).findById(1L);
        verify(cultivarRepository, times(1)).save(any(Cultivar.class));
    }

    @Test
    @DisplayName("Reactivar cultivar - debe cambiar activo a true")
    void reactivar_debeCambiarActivoATrue() {
        cultivar.setActivo(false);
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));
        when(cultivarRepository.save(any(Cultivar.class))).thenReturn(cultivar);

        CultivarDTO resultado = cultivarService.reactivar(1L);

        assertNotNull(resultado);
        verify(cultivarRepository, times(1)).save(any(Cultivar.class));
    }

    @Test
    @DisplayName("Reactivar cultivar ya activo - debe lanzar excepción")
    void reactivar_cultivarYaActivo_debeLanzarExcepcion() {
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cultivarService.reactivar(1L);
        });

        assertTrue(exception.getMessage().contains("ya está activo"));
        verify(cultivarRepository, never()).save(any(Cultivar.class));
    }

    @Test
    @DisplayName("Reactivar cultivar inexistente - debe retornar null")
    void reactivar_cultivarInexistente_debeRetornarNull() {
        when(cultivarRepository.findById(999L)).thenReturn(Optional.empty());

        CultivarDTO resultado = cultivarService.reactivar(999L);

        assertNull(resultado);
        verify(cultivarRepository, never()).save(any(Cultivar.class));
    }

    @Test
    @DisplayName("Obtener entidad por ID - debe retornar entidad")
    void obtenerEntidadPorId_debeRetornarEntidad() {
        when(cultivarRepository.findById(1L)).thenReturn(Optional.of(cultivar));

        Cultivar resultado = cultivarService.obtenerEntidadPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getCultivarID());
        verify(cultivarRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener entidad por ID inexistente - debe retornar null")
    void obtenerEntidadPorId_inexistente_debeRetornarNull() {
        when(cultivarRepository.findById(999L)).thenReturn(Optional.empty());

        Cultivar resultado = cultivarService.obtenerEntidadPorId(999L);

        assertNull(resultado);
        verify(cultivarRepository, times(1)).findById(999L);
    }

    // ========== Tests para obtenerTodos(Boolean activo) ==========

    @Test
    @DisplayName("obtenerTodos con activo=true - debe retornar solo activos")
    void obtenerTodos_activoTrue_debeRetornarSoloActivos() {
        
        when(cultivarRepository.findByActivoTrue()).thenReturn(Arrays.asList(cultivar));

        
        List<CultivarDTO> resultado = cultivarService.obtenerTodos(true);

        
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(cultivarRepository, times(1)).findByActivoTrue();
        verify(cultivarRepository, never()).findAll();
        verify(cultivarRepository, never()).findByActivoFalse();
    }

    @Test
    @DisplayName("obtenerTodos con activo=false - debe retornar solo inactivos")
    void obtenerTodos_activoFalse_debeRetornarSoloInactivos() {
        
        cultivar.setActivo(false);
        when(cultivarRepository.findByActivoFalse()).thenReturn(Arrays.asList(cultivar));

        
        List<CultivarDTO> resultado = cultivarService.obtenerTodos(false);

        
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(cultivarRepository, times(1)).findByActivoFalse();
        verify(cultivarRepository, never()).findAll();
        verify(cultivarRepository, never()).findByActivoTrue();
    }

    // ========== Tests para obtenerCultivaresPaginadosConFiltros ==========

    @Test
    @DisplayName("obtenerCultivaresPaginadosConFiltros - sin filtros, retorna todos ordenados")
    void obtenerCultivaresPaginadosConFiltros_sinFiltros_retornaTodos() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cultivar> cultivarPage = new PageImpl<>(Arrays.asList(cultivar), pageable, 1);
        
        when(cultivarRepository.findAllByOrderByNombreAsc(pageable)).thenReturn(cultivarPage);

        
        Page<CultivarDTO> resultado = cultivarService.obtenerCultivaresPaginadosConFiltros(pageable, null, null);

        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1, resultado.getContent().size());
        verify(cultivarRepository, times(1)).findAllByOrderByNombreAsc(pageable);
    }

    @Test
    @DisplayName("obtenerCultivaresPaginadosConFiltros - filtro activo=true, sin búsqueda")
    void obtenerCultivaresPaginadosConFiltros_soloActivos_retornaActivos() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cultivar> cultivarPage = new PageImpl<>(Arrays.asList(cultivar), pageable, 1);
        
        when(cultivarRepository.findByActivoTrueOrderByNombreAsc(pageable)).thenReturn(cultivarPage);

        
        Page<CultivarDTO> resultado = cultivarService.obtenerCultivaresPaginadosConFiltros(pageable, null, true);

        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(cultivarRepository, times(1)).findByActivoTrueOrderByNombreAsc(pageable);
    }

    @Test
    @DisplayName("obtenerCultivaresPaginadosConFiltros - filtro activo=false, sin búsqueda")
    void obtenerCultivaresPaginadosConFiltros_soloInactivos_retornaInactivos() {
        
        Pageable pageable = PageRequest.of(0, 10);
        cultivar.setActivo(false);
        Page<Cultivar> cultivarPage = new PageImpl<>(Arrays.asList(cultivar), pageable, 1);
        
        when(cultivarRepository.findByActivoFalseOrderByNombreAsc(pageable)).thenReturn(cultivarPage);

        
        Page<CultivarDTO> resultado = cultivarService.obtenerCultivaresPaginadosConFiltros(pageable, null, false);

        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(cultivarRepository, times(1)).findByActivoFalseOrderByNombreAsc(pageable);
    }

    @Test
    @DisplayName("obtenerCultivaresPaginadosConFiltros - con término de búsqueda y activo=null")
    void obtenerCultivaresPaginadosConFiltros_conBusquedaSinFiltroActivo_retornaTodosCoincidentes() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Cultivar cultivar2 = new Cultivar();
        cultivar2.setCultivarID(2L);
        cultivar2.setNombre("Test Cultivar");
        cultivar2.setEspecie(especie);
        cultivar2.setActivo(false);
        
        when(cultivarRepository.findAll()).thenReturn(Arrays.asList(cultivar, cultivar2));

        
        Page<CultivarDTO> resultado = cultivarService.obtenerCultivaresPaginadosConFiltros(pageable, "Test", null);

        
        assertNotNull(resultado);
        assertEquals(2, resultado.getTotalElements()); // Ambos contienen "Test"
        verify(cultivarRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("obtenerCultivaresPaginadosConFiltros - con término de búsqueda y activo=true")
    void obtenerCultivaresPaginadosConFiltros_conBusquedaYActivoTrue_retornaSoloActivosCoincidentes() {
        
        Pageable pageable = PageRequest.of(0, 10);
        
        when(cultivarRepository.findByNombreContainingIgnoreCaseAndActivoTrue("Test")).thenReturn(Arrays.asList(cultivar));

        
        Page<CultivarDTO> resultado = cultivarService.obtenerCultivaresPaginadosConFiltros(pageable, "Test", true);

        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertTrue(resultado.getContent().get(0).getActivo());
        verify(cultivarRepository, times(1)).findByNombreContainingIgnoreCaseAndActivoTrue("Test");
    }

    @Test
    @DisplayName("obtenerCultivaresPaginadosConFiltros - con término de búsqueda y activo=false")
    void obtenerCultivaresPaginadosConFiltros_conBusquedaYActivoFalse_retornaSoloInactivosCoincidentes() {
        
        Pageable pageable = PageRequest.of(0, 10);
        cultivar.setActivo(false);
        
        when(cultivarRepository.findByActivoFalse()).thenReturn(Arrays.asList(cultivar));

        
        Page<CultivarDTO> resultado = cultivarService.obtenerCultivaresPaginadosConFiltros(pageable, "Test", false);

        
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertFalse(resultado.getContent().get(0).getActivo());
        verify(cultivarRepository, times(1)).findByActivoFalse();
    }

    @Test
    @DisplayName("obtenerCultivaresPaginadosConFiltros - con búsqueda vacía, trata como sin filtro")
    void obtenerCultivaresPaginadosConFiltros_busquedaVacia_trataSinFiltro() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cultivar> cultivarPage = new PageImpl<>(Arrays.asList(cultivar), pageable, 1);
        
        when(cultivarRepository.findAllByOrderByNombreAsc(pageable)).thenReturn(cultivarPage);

        
        Page<CultivarDTO> resultado = cultivarService.obtenerCultivaresPaginadosConFiltros(pageable, "   ", null);

        
        assertNotNull(resultado);
        verify(cultivarRepository, times(1)).findAllByOrderByNombreAsc(pageable);
    }

    @Test
    @DisplayName("obtenerCultivaresPaginadosConFiltros - paginación correcta con búsqueda")
    void obtenerCultivaresPaginadosConFiltros_paginacionCorrecta() {
        
        Pageable pageable = PageRequest.of(0, 1); // Solo 1 elemento por página
        
        Cultivar cultivar2 = new Cultivar();
        cultivar2.setCultivarID(2L);
        cultivar2.setNombre("Test 2");
        cultivar2.setEspecie(especie);
        cultivar2.setActivo(true);
        
        when(cultivarRepository.findByNombreContainingIgnoreCaseAndActivoTrue("Test"))
            .thenReturn(Arrays.asList(cultivar, cultivar2));

        
        Page<CultivarDTO> resultado = cultivarService.obtenerCultivaresPaginadosConFiltros(pageable, "Test", true);

        
        assertNotNull(resultado);
        assertEquals(2, resultado.getTotalElements()); // Total de elementos
        assertEquals(1, resultado.getContent().size()); // Solo 1 en la primera página
        assertEquals(2, resultado.getTotalPages()); // 2 páginas en total
    }
}
