package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.TablaGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.TablaGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepGermDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de RepGermService")
class RepGermServiceTest {

    @Mock
    private RepGermRepository repGermRepository;

    @Mock
    private TablaGermRepository tablaGermRepository;

    @Mock
    private AnalisisService analisisService;

    @InjectMocks
    private RepGermService repGermService;

    private TablaGerm tablaGerm;
    private Germinacion germinacion;
    private RepGermRequestDTO repGermRequestDTO;
    private RepGerm repGerm;

    @BeforeEach
    void setUp() {
        
        germinacion = new Germinacion();
        germinacion.setAnalisisID(1L);
        germinacion.setActivo(true);

        
        tablaGerm = new TablaGerm();
        tablaGerm.setTablaGermID(1L);
        tablaGerm.setGerminacion(germinacion);
        tablaGerm.setNumeroRepeticiones(4);
        tablaGerm.setNumeroConteos(3);
        tablaGerm.setNumSemillasPRep(100);
        tablaGerm.setFinalizada(false);

        
        repGermRequestDTO = new RepGermRequestDTO();
        repGermRequestDTO.setNormales(Arrays.asList(25, 30, 20)); 
        repGermRequestDTO.setAnormales(10);
        repGermRequestDTO.setDuras(5);
        repGermRequestDTO.setFrescas(3);
        repGermRequestDTO.setMuertas(7);

        
        repGerm = new RepGerm();
        repGerm.setRepGermID(1L);
        repGerm.setNumRep(1);
        repGerm.setNormales(Arrays.asList(25, 30, 20));
        repGerm.setAnormales(10);
        repGerm.setDuras(5);
        repGerm.setFrescas(3);
        repGerm.setMuertas(7);
        repGerm.setTotal(100);
        repGerm.setTablaGerm(tablaGerm);
    }

    @Test
    @DisplayName("Crear repetición - debe crear con todos los datos correctos")
    void crearRepGerm_debeCrearConTodosLosDatos() {
        
        when(tablaGermRepository.findById(1L)).thenReturn(Optional.of(tablaGerm));
        when(repGermRepository.countByTablaGermId(1L)).thenReturn(0L);
        when(repGermRepository.save(any(RepGerm.class))).thenReturn(repGerm);
        when(repGermRepository.findByTablaGermId(1L)).thenReturn(Arrays.asList(repGerm));
        when(tablaGermRepository.save(any(TablaGerm.class))).thenReturn(tablaGerm);

        
        RepGermDTO resultado = repGermService.crearRepGerm(1L, repGermRequestDTO);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(1L, resultado.getRepGermID(), "El ID debe ser 1");
        assertEquals(1, resultado.getNumRep(), "El número de repetición debe ser 1");
        assertEquals(Arrays.asList(25, 30, 20), resultado.getNormales(), "Los valores normales deben coincidir");
        assertEquals(10, resultado.getAnormales(), "Las anormales deben ser 10");
        assertEquals(5, resultado.getDuras(), "Las duras deben ser 5");
        assertEquals(3, resultado.getFrescas(), "Las frescas deben ser 3");
        assertEquals(7, resultado.getMuertas(), "Las muertas deben ser 7");
        assertEquals(100, resultado.getTotal(), "El total debe ser 100");
        assertEquals(1L, resultado.getTablaGermId(), "El ID de tabla debe ser 1");

        
        verify(repGermRepository, times(1)).save(any(RepGerm.class));
        
        verify(tablaGermRepository, times(1)).save(any(TablaGerm.class));
    }

    @Test
    @DisplayName("Crear repetición - debe generar numRep automáticamente")
    void crearRepGerm_debeGenerarNumRepAutomaticamente() {
        
        when(tablaGermRepository.findById(1L)).thenReturn(Optional.of(tablaGerm));
        when(repGermRepository.countByTablaGermId(1L)).thenReturn(2L); 
        
        RepGerm repGerm3 = new RepGerm();
        repGerm3.setRepGermID(3L);
        repGerm3.setNumRep(3); 
        repGerm3.setTablaGerm(tablaGerm);
        repGerm3.setNormales(Arrays.asList(25, 30, 20));
        repGerm3.setTotal(100);
        
        when(repGermRepository.save(any(RepGerm.class))).thenReturn(repGerm3);
        when(repGermRepository.findByTablaGermId(1L)).thenReturn(Arrays.asList(repGerm3));
        when(tablaGermRepository.save(any(TablaGerm.class))).thenReturn(tablaGerm);

        
        RepGermDTO resultado = repGermService.crearRepGerm(1L, repGermRequestDTO);

        
        assertEquals(3, resultado.getNumRep(), "El numRep debe ser 3 (siguiente disponible)");
        
        verify(repGermRepository, times(2)).countByTablaGermId(1L);
    }

    @Test
    @DisplayName("Crear repetición - debe validar número máximo de repeticiones")
    void crearRepGerm_debeValidarNumeroMaximoRepeticiones() {
        
        when(tablaGermRepository.findById(1L)).thenReturn(Optional.of(tablaGerm));
        when(repGermRepository.countByTablaGermId(1L)).thenReturn(4L); 

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repGermService.crearRepGerm(1L, repGermRequestDTO);
        });

        assertTrue(exception.getMessage().contains("No se pueden agregar más repeticiones"),
                "Debe lanzar excepción por exceder número máximo de repeticiones");
        verify(repGermRepository, never()).save(any(RepGerm.class));
    }

    @Test
    @DisplayName("Crear repetición - debe validar tabla no encontrada")
    void crearRepGerm_debeValidarTablaNoEncontrada() {
        
        when(tablaGermRepository.findById(999L)).thenReturn(Optional.empty());

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repGermService.crearRepGerm(999L, repGermRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Tabla no encontrada"),
                "Debe lanzar excepción cuando la tabla no existe");
        verify(repGermRepository, never()).save(any(RepGerm.class));
    }

    @Test
    @DisplayName("Crear repetición - debe validar que el total no exceda el límite máximo (105% tolerancia)")
    void crearRepGerm_debeValidarLimiteMaximo() {
        
        when(tablaGermRepository.findById(1L)).thenReturn(Optional.of(tablaGerm));
        when(repGermRepository.countByTablaGermId(1L)).thenReturn(0L);

        
        RepGermRequestDTO requestExcesivo = new RepGermRequestDTO();
        requestExcesivo.setNormales(Arrays.asList(50, 40, 20)); 
        requestExcesivo.setAnormales(0);
        requestExcesivo.setDuras(0);
        requestExcesivo.setFrescas(0);
        requestExcesivo.setMuertas(0);

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repGermService.crearRepGerm(1L, requestExcesivo);
        });

        assertTrue(exception.getMessage().contains("excede el límite máximo permitido"),
                "Debe lanzar excepción cuando el total excede el límite máximo");
    }

    @Test
    @DisplayName("Crear repetición - debe validar valores negativos en normales")
    void crearRepGerm_debeValidarValoresNegativosEnNormales() {
        
        when(tablaGermRepository.findById(1L)).thenReturn(Optional.of(tablaGerm));
        when(repGermRepository.countByTablaGermId(1L)).thenReturn(0L);

        RepGermRequestDTO requestNegativo = new RepGermRequestDTO();
        requestNegativo.setNormales(Arrays.asList(25, -10, 20)); 
        requestNegativo.setAnormales(10);

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repGermService.crearRepGerm(1L, requestNegativo);
        });

        assertTrue(exception.getMessage().contains("no puede ser negativo"),
                "Debe lanzar excepción cuando hay valores negativos en normales");
    }

    @Test
    @DisplayName("Crear repetición - debe validar valores negativos en anormales")
    void crearRepGerm_debeValidarValoresNegativosEnAnormales() {
        
        when(tablaGermRepository.findById(1L)).thenReturn(Optional.of(tablaGerm));
        when(repGermRepository.countByTablaGermId(1L)).thenReturn(0L);

        RepGermRequestDTO requestNegativo = new RepGermRequestDTO();
        requestNegativo.setNormales(Arrays.asList(25, 30, 20));
        requestNegativo.setAnormales(-5); 

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repGermService.crearRepGerm(1L, requestNegativo);
        });

        assertTrue(exception.getMessage().contains("anormales no puede ser negativo"),
                "Debe lanzar excepción cuando anormales es negativo");
    }

    @Test
    @DisplayName("Crear repetición - debe validar valores que exceden numSemillasPRep")
    void crearRepGerm_debeValidarValoresQueExcedenNumSemillasPRep() {
        
        when(tablaGermRepository.findById(1L)).thenReturn(Optional.of(tablaGerm));
        when(repGermRepository.countByTablaGermId(1L)).thenReturn(0L);

        RepGermRequestDTO requestExcesivo = new RepGermRequestDTO();
        requestExcesivo.setNormales(Arrays.asList(25, 30, 20));
        requestExcesivo.setAnormales(150); 

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repGermService.crearRepGerm(1L, requestExcesivo);
        });

        assertTrue(exception.getMessage().contains("no puede exceder"),
                "Debe lanzar excepción cuando un valor individual excede numSemillasPRep");
    }

    @Test
    @DisplayName("Crear repetición - debe rechazar total cero")
    void crearRepGerm_debeRechazarTotalCero() {
        
        when(tablaGermRepository.findById(1L)).thenReturn(Optional.of(tablaGerm));
        when(repGermRepository.countByTablaGermId(1L)).thenReturn(0L);

        RepGermRequestDTO requestVacio = new RepGermRequestDTO();
        requestVacio.setNormales(Arrays.asList(0, 0, 0));
        requestVacio.setAnormales(0);
        requestVacio.setDuras(0);
        requestVacio.setFrescas(0);
        requestVacio.setMuertas(0);

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repGermService.crearRepGerm(1L, requestVacio);
        });

        assertTrue(exception.getMessage().contains("Debe ingresar al menos un valor"),
                "Debe lanzar excepción cuando el total es cero");
    }

    @Test
    @DisplayName("Actualizar repetición - debe actualizar correctamente todos los campos")
    void actualizarRepGerm_debeActualizarCorrectamente() {
        
        when(repGermRepository.findById(1L)).thenReturn(Optional.of(repGerm));
        
        RepGermRequestDTO requestActualizado = new RepGermRequestDTO();
        requestActualizado.setNormales(Arrays.asList(30, 35, 25)); 
        requestActualizado.setAnormales(5);
        requestActualizado.setDuras(2);
        requestActualizado.setFrescas(1);
        requestActualizado.setMuertas(2);
        
        RepGerm repGermActualizada = new RepGerm();
        repGermActualizada.setRepGermID(1L);
        repGermActualizada.setNumRep(1);
        repGermActualizada.setNormales(Arrays.asList(30, 35, 25));
        repGermActualizada.setAnormales(5);
        repGermActualizada.setDuras(2);
        repGermActualizada.setFrescas(1);
        repGermActualizada.setMuertas(2);
        repGermActualizada.setTotal(100); 
        repGermActualizada.setTablaGerm(tablaGerm);
        
        when(repGermRepository.save(any(RepGerm.class))).thenReturn(repGermActualizada);
        when(repGermRepository.findByTablaGermId(1L)).thenReturn(Arrays.asList(repGermActualizada));
        when(tablaGermRepository.save(any(TablaGerm.class))).thenReturn(tablaGerm);
        doNothing().when(analisisService).manejarEdicionAnalisisFinalizado(any(Germinacion.class));

        
        RepGermDTO resultado = repGermService.actualizarRepGerm(1L, requestActualizado);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(Arrays.asList(30, 35, 25), resultado.getNormales(), "Los valores normales deben actualizarse");
        assertEquals(5, resultado.getAnormales(), "Las anormales deben actualizarse");
        assertEquals(2, resultado.getDuras(), "Las duras deben actualizarse");
        assertEquals(1, resultado.getFrescas(), "Las frescas deben actualizarse");
        assertEquals(2, resultado.getMuertas(), "Las muertas deben actualizarse");
        assertEquals(100, resultado.getTotal(), "El total debe recalcularse correctamente");

        verify(repGermRepository, times(1)).save(any(RepGerm.class));
        verify(tablaGermRepository, times(1)).save(any(TablaGerm.class));
        verify(analisisService, times(1)).manejarEdicionAnalisisFinalizado(any(Germinacion.class));
    }

    @Test
    @DisplayName("Actualizar repetición - debe validar repetición no encontrada")
    void actualizarRepGerm_debeValidarRepeticionNoEncontrada() {
        
        when(repGermRepository.findById(999L)).thenReturn(Optional.empty());

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repGermService.actualizarRepGerm(999L, repGermRequestDTO);
        });

        assertTrue(exception.getMessage().contains("Repetición no encontrada"),
                "Debe lanzar excepción cuando la repetición no existe");
        verify(repGermRepository, never()).save(any(RepGerm.class));
    }

    @Test
    @DisplayName("Eliminar repetición - debe eliminar correctamente")
    void eliminarRepGerm_debeEliminarCorrectamente() {
        
        when(repGermRepository.findById(1L)).thenReturn(Optional.of(repGerm));
        doNothing().when(repGermRepository).deleteById(1L);

        
        repGermService.eliminarRepGerm(1L);

        
        verify(repGermRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Eliminar repetición - debe validar repetición no encontrada")
    void eliminarRepGerm_debeValidarRepeticionNoEncontrada() {
        
        when(repGermRepository.findById(999L)).thenReturn(Optional.empty());

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repGermService.eliminarRepGerm(999L);
        });

        assertTrue(exception.getMessage().contains("Repetición no encontrada"),
                "Debe lanzar excepción cuando la repetición no existe");
        verify(repGermRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Obtener repetición por ID - debe retornar repetición existente")
    void obtenerRepGermPorId_debeRetornarRepeticionExistente() {
        
        when(repGermRepository.findById(1L)).thenReturn(Optional.of(repGerm));

        
        RepGermDTO resultado = repGermService.obtenerRepGermPorId(1L);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(1L, resultado.getRepGermID(), "El ID debe coincidir");
        assertEquals(1, resultado.getNumRep(), "El numRep debe coincidir");
        verify(repGermRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Obtener repetición por ID - debe validar repetición no encontrada")
    void obtenerRepGermPorId_debeValidarRepeticionNoEncontrada() {
        
        when(repGermRepository.findById(999L)).thenReturn(Optional.empty());

        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            repGermService.obtenerRepGermPorId(999L);
        });

        assertTrue(exception.getMessage().contains("Repetición no encontrada"),
                "Debe lanzar excepción cuando la repetición no existe");
    }

    @Test
    @DisplayName("Obtener repeticiones por tabla - debe retornar lista de repeticiones")
    void obtenerRepeticionesPorTabla_debeRetornarListaRepeticiones() {
        
        RepGerm rep2 = new RepGerm();
        rep2.setRepGermID(2L);
        rep2.setNumRep(2);
        rep2.setNormales(Arrays.asList(28, 32, 22));
        rep2.setAnormales(8);
        rep2.setDuras(4);
        rep2.setFrescas(2);
        rep2.setMuertas(4);
        rep2.setTotal(100);
        rep2.setTablaGerm(tablaGerm);

        List<RepGerm> repeticiones = Arrays.asList(repGerm, rep2);
        when(repGermRepository.findByTablaGermId(1L)).thenReturn(repeticiones);

        
        List<RepGermDTO> resultado = repGermService.obtenerRepeticionesPorTabla(1L);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(2, resultado.size(), "Debe retornar 2 repeticiones");
        assertEquals(1, resultado.get(0).getNumRep(), "La primera debe ser numRep 1");
        assertEquals(2, resultado.get(1).getNumRep(), "La segunda debe ser numRep 2");
        verify(repGermRepository, times(1)).findByTablaGermId(1L);
    }

    @Test
    @DisplayName("Obtener repeticiones por tabla - debe retornar lista vacía si no hay repeticiones")
    void obtenerRepeticionesPorTabla_debeRetornarListaVacia() {
        
        when(repGermRepository.findByTablaGermId(1L)).thenReturn(new ArrayList<>());

        
        List<RepGermDTO> resultado = repGermService.obtenerRepeticionesPorTabla(1L);

        
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(0, resultado.size(), "La lista debe estar vacía");
    }

    @Test
    @DisplayName("Contar repeticiones por tabla - debe retornar conteo correcto")
    void contarRepeticionesPorTabla_debeRetornarConteoCorrecto() {
        
        when(repGermRepository.countByTablaGermId(1L)).thenReturn(3L);

        
        Long resultado = repGermService.contarRepeticionesPorTabla(1L);

        
        assertEquals(3L, resultado, "Debe retornar el conteo correcto");
        verify(repGermRepository, times(1)).countByTablaGermId(1L);
    }

    @Test
    @DisplayName("Actualizar totales tabla - debe calcular total como suma de repeticiones")
    void actualizarTotalesTablaGerm_debeCalcularTotalCorrectamente() {
        
        RepGerm rep1 = new RepGerm();
        rep1.setTotal(100);
        rep1.setNormales(Arrays.asList(25, 30, 20));
        rep1.setAnormales(10);
        rep1.setDuras(5);
        rep1.setFrescas(3);
        rep1.setMuertas(7);
        rep1.setTablaGerm(tablaGerm);

        RepGerm rep2 = new RepGerm();
        rep2.setTotal(98);
        rep2.setNormales(Arrays.asList(28, 32, 22));
        rep2.setAnormales(8);
        rep2.setDuras(3);
        rep2.setFrescas(1);
        rep2.setMuertas(4);
        rep2.setTablaGerm(tablaGerm);

        when(tablaGermRepository.findById(1L)).thenReturn(Optional.of(tablaGerm));
        when(repGermRepository.countByTablaGermId(1L)).thenReturn(0L);
        when(repGermRepository.save(any(RepGerm.class))).thenReturn(rep1);
        when(repGermRepository.findByTablaGermId(1L)).thenReturn(Arrays.asList(rep1, rep2));
        when(tablaGermRepository.save(any(TablaGerm.class))).thenAnswer(invocation -> {
            TablaGerm tabla = invocation.getArgument(0);
            
            assertEquals(198, tabla.getTotal(), "El total debe ser la suma de las repeticiones");
            return tabla;
        });

        
        repGermService.crearRepGerm(1L, repGermRequestDTO);

        
        verify(tablaGermRepository, times(1)).save(any(TablaGerm.class));
    }

    @Test
    @DisplayName("Calcular promedios sin redondeo - debe calcular 5 promedios correctamente")
    void calcularPromediosSinRedondeo_debeCalcularCorrectamente() {
        
        tablaGerm.setNumeroRepeticiones(4);
        
        
        RepGerm rep1 = crearRepeticion(1, Arrays.asList(25, 30, 20), 10, 5, 3, 7);
        RepGerm rep2 = crearRepeticion(2, Arrays.asList(28, 32, 22), 8, 4, 2, 4);
        RepGerm rep3 = crearRepeticion(3, Arrays.asList(26, 31, 21), 9, 5, 2, 6);
        RepGerm rep4 = crearRepeticion(4, Arrays.asList(27, 29, 23), 11, 3, 4, 3);

        when(tablaGermRepository.findById(1L)).thenReturn(Optional.of(tablaGerm));
        when(repGermRepository.countByTablaGermId(1L)).thenReturn(3L);
        when(repGermRepository.save(any(RepGerm.class))).thenReturn(rep4);
        when(repGermRepository.findByTablaGermId(1L)).thenReturn(Arrays.asList(rep1, rep2, rep3, rep4));
        when(tablaGermRepository.save(any(TablaGerm.class))).thenAnswer(invocation -> {
            TablaGerm tabla = invocation.getArgument(0);
            
            
            assertNotNull(tabla.getPromedioSinRedondeo(), "Los promedios no deben ser nulos");
            assertEquals(5, tabla.getPromedioSinRedondeo().size(), "Debe haber 5 promedios");
            
            
            assertEquals(new BigDecimal("78.50"), tabla.getPromedioSinRedondeo().get(0), 
                    "Promedio de normales debe ser 78.50");
            
            
            assertEquals(new BigDecimal("9.50"), tabla.getPromedioSinRedondeo().get(1), 
                    "Promedio de anormales debe ser 9.50");
            
            
            assertEquals(new BigDecimal("4.25"), tabla.getPromedioSinRedondeo().get(2), 
                    "Promedio de duras debe ser 4.25");
            
            
            assertEquals(new BigDecimal("2.75"), tabla.getPromedioSinRedondeo().get(3), 
                    "Promedio de frescas debe ser 2.75");
            
            
            assertEquals(new BigDecimal("5.00"), tabla.getPromedioSinRedondeo().get(4), 
                    "Promedio de muertas debe ser 5.00");
            
            return tabla;
        });

        
        repGermService.crearRepGerm(1L, repGermRequestDTO);

        
        verify(tablaGermRepository, times(1)).save(any(TablaGerm.class));
    }

    @Test
    @DisplayName("Calcular promedios por conteo - debe calcular promedios por cada conteo individual")
    void calcularPromediosPorConteo_debeCalcularCorrectamente() {
        
        tablaGerm.setNumeroRepeticiones(4);
        tablaGerm.setNumeroConteos(3);
        
        
        RepGerm rep1 = crearRepeticion(1, Arrays.asList(25, 30, 20), 10, 5, 3, 7);
        RepGerm rep2 = crearRepeticion(2, Arrays.asList(28, 32, 22), 8, 4, 2, 4);
        RepGerm rep3 = crearRepeticion(3, Arrays.asList(26, 31, 21), 9, 5, 2, 6);
        RepGerm rep4 = crearRepeticion(4, Arrays.asList(27, 29, 23), 11, 3, 4, 3);

        when(tablaGermRepository.findById(1L)).thenReturn(Optional.of(tablaGerm));
        when(repGermRepository.countByTablaGermId(1L)).thenReturn(3L);
        when(repGermRepository.save(any(RepGerm.class))).thenReturn(rep4);
        when(repGermRepository.findByTablaGermId(1L)).thenReturn(Arrays.asList(rep1, rep2, rep3, rep4));
        when(tablaGermRepository.save(any(TablaGerm.class))).thenAnswer(invocation -> {
            TablaGerm tabla = invocation.getArgument(0);
            
            
            assertNotNull(tabla.getPromediosSinRedPorConteo(), "Los promedios por conteo no deben ser nulos");
            assertEquals(7, tabla.getPromediosSinRedPorConteo().size(), "Debe haber 7 promedios (3 conteos + 4 campos)");
            
            
            assertEquals(new BigDecimal("26.50"), tabla.getPromediosSinRedPorConteo().get(0), 
                    "Promedio del conteo 1 debe ser 26.50");
            
            
            assertEquals(new BigDecimal("30.50"), tabla.getPromediosSinRedPorConteo().get(1), 
                    "Promedio del conteo 2 debe ser 30.50");
            
            
            assertEquals(new BigDecimal("21.50"), tabla.getPromediosSinRedPorConteo().get(2), 
                    "Promedio del conteo 3 debe ser 21.50");
            
            
            assertEquals(new BigDecimal("9.50"), tabla.getPromediosSinRedPorConteo().get(3), 
                    "Promedio de anormales debe ser 9.50");
            
            
            assertEquals(new BigDecimal("4.25"), tabla.getPromediosSinRedPorConteo().get(4), 
                    "Promedio de duras debe ser 4.25");
            
            
            assertEquals(new BigDecimal("2.75"), tabla.getPromediosSinRedPorConteo().get(5), 
                    "Promedio de frescas debe ser 2.75");
            
            
            assertEquals(new BigDecimal("5.00"), tabla.getPromediosSinRedPorConteo().get(6), 
                    "Promedio de muertas debe ser 5.00");
            
            return tabla;
        });

        
        repGermService.crearRepGerm(1L, repGermRequestDTO);

        
        verify(tablaGermRepository, times(1)).save(any(TablaGerm.class));
    }

    @Test
    @DisplayName("Verificar todas las repeticiones completas - debe retornar true cuando están completas")
    void todasLasRepeticionesCompletas_debeRetornarTrueCuandoCompletas() {
        
        tablaGerm.setNumeroRepeticiones(2);
        
        RepGerm rep1 = crearRepeticion(1, Arrays.asList(25, 30, 20), 10, 5, 3, 7);
        RepGerm rep2 = crearRepeticion(2, Arrays.asList(28, 32, 22), 8, 4, 2, 4);

        when(tablaGermRepository.findById(1L)).thenReturn(Optional.of(tablaGerm));
        when(repGermRepository.countByTablaGermId(1L)).thenReturn(1L);
        when(repGermRepository.save(any(RepGerm.class))).thenReturn(rep2);
        when(repGermRepository.findByTablaGermId(1L)).thenReturn(Arrays.asList(rep1, rep2));
        when(tablaGermRepository.save(any(TablaGerm.class))).thenAnswer(invocation -> {
            TablaGerm tabla = invocation.getArgument(0);
            
            
            assertNotNull(tabla.getPromedioSinRedondeo(), "Los promedios deben calcularse cuando todas están completas");
            assertEquals(5, tabla.getPromedioSinRedondeo().size(), "Debe haber 5 promedios");
            
            return tabla;
        });

        
        repGermService.crearRepGerm(1L, repGermRequestDTO);

        
        verify(tablaGermRepository, times(1)).save(any(TablaGerm.class));
    }

    @Test
    @DisplayName("Verificar todas las repeticiones completas - debe retornar false cuando faltan repeticiones")
    void todasLasRepeticionesCompletas_debeRetornarFalseCuandoFaltan() {
        
        tablaGerm.setNumeroRepeticiones(4); 
        
        RepGerm rep1 = crearRepeticion(1, Arrays.asList(25, 30, 20), 10, 5, 3, 7);

        when(tablaGermRepository.findById(1L)).thenReturn(Optional.of(tablaGerm));
        when(repGermRepository.countByTablaGermId(1L)).thenReturn(0L);
        when(repGermRepository.save(any(RepGerm.class))).thenReturn(rep1);
        when(repGermRepository.findByTablaGermId(1L)).thenReturn(Arrays.asList(rep1)); 
        when(tablaGermRepository.save(any(TablaGerm.class))).thenAnswer(invocation -> {
            TablaGerm tabla = invocation.getArgument(0);
            
            
            assertTrue(tabla.getPromedioSinRedondeo() == null || tabla.getPromedioSinRedondeo().isEmpty(),
                    "No deben calcularse promedios cuando faltan repeticiones");
            
            return tabla;
        });

        
        repGermService.crearRepGerm(1L, repGermRequestDTO);

        
        verify(tablaGermRepository, times(1)).save(any(TablaGerm.class));
    }

    @Test
    @DisplayName("Mapear entidad a DTO - debe mapear todos los campos correctamente")
    void mapearEntidadADTO_debeMapeaTodosLosCampos() {
        
        when(repGermRepository.findById(1L)).thenReturn(Optional.of(repGerm));

        
        RepGermDTO resultado = repGermService.obtenerRepGermPorId(1L);

        
        assertNotNull(resultado, "El DTO no debe ser nulo");
        assertEquals(repGerm.getRepGermID(), resultado.getRepGermID(), "El ID debe coincidir");
        assertEquals(repGerm.getNumRep(), resultado.getNumRep(), "El numRep debe coincidir");
        assertEquals(repGerm.getNormales(), resultado.getNormales(), "Los normales deben coincidir");
        assertEquals(repGerm.getAnormales(), resultado.getAnormales(), "Las anormales deben coincidir");
        assertEquals(repGerm.getDuras(), resultado.getDuras(), "Las duras deben coincidir");
        assertEquals(repGerm.getFrescas(), resultado.getFrescas(), "Las frescas deben coincidir");
        assertEquals(repGerm.getMuertas(), resultado.getMuertas(), "Las muertas deben coincidir");
        assertEquals(repGerm.getTotal(), resultado.getTotal(), "El total debe coincidir");
        assertEquals(repGerm.getTablaGerm().getTablaGermID(), resultado.getTablaGermId(), 
                "El ID de tabla debe coincidir");
    }

    @Test
    @DisplayName("Crear repetición con valores null - debe asignar valores por defecto")
    void crearRepGerm_conValoresNull_debeAsignarDefecto() {
        
        RepGermRequestDTO requestConNulls = new RepGermRequestDTO();
        requestConNulls.setNormales(Arrays.asList(75, null, null)); 
        requestConNulls.setAnormales(null);
        requestConNulls.setDuras(null);
        requestConNulls.setFrescas(null);
        requestConNulls.setMuertas(null);

        RepGerm repGermConDefectos = new RepGerm();
        repGermConDefectos.setRepGermID(1L);
        repGermConDefectos.setNumRep(1);
        repGermConDefectos.setNormales(Arrays.asList(75, 0, 0)); 
        repGermConDefectos.setAnormales(0);
        repGermConDefectos.setDuras(0);
        repGermConDefectos.setFrescas(0);
        repGermConDefectos.setMuertas(0);
        repGermConDefectos.setTotal(75);
        repGermConDefectos.setTablaGerm(tablaGerm);

        when(tablaGermRepository.findById(1L)).thenReturn(Optional.of(tablaGerm));
        when(repGermRepository.countByTablaGermId(1L)).thenReturn(0L);
        when(repGermRepository.save(any(RepGerm.class))).thenReturn(repGermConDefectos);
        when(repGermRepository.findByTablaGermId(1L)).thenReturn(Arrays.asList(repGermConDefectos));
        when(tablaGermRepository.save(any(TablaGerm.class))).thenReturn(tablaGerm);

        
        RepGermDTO resultado = repGermService.crearRepGerm(1L, requestConNulls);

        
        assertEquals(Arrays.asList(75, 0, 0), resultado.getNormales(), 
                "Debe completar con 0 los valores null en normales");
        assertEquals(0, resultado.getAnormales(), "Debe asignar 0 cuando anormales es null");
        assertEquals(0, resultado.getDuras(), "Debe asignar 0 cuando duras es null");
        assertEquals(0, resultado.getFrescas(), "Debe asignar 0 cuando frescas es null");
        assertEquals(0, resultado.getMuertas(), "Debe asignar 0 cuando muertas es null");
        assertEquals(75, resultado.getTotal(), "El total debe calcularse correctamente");
    }

    
    private RepGerm crearRepeticion(int numRep, List<Integer> normales, int anormales, 
                                    int duras, int frescas, int muertas) {
        RepGerm rep = new RepGerm();
        rep.setRepGermID((long) numRep);
        rep.setNumRep(numRep);
        rep.setNormales(normales);
        rep.setAnormales(anormales);
        rep.setDuras(duras);
        rep.setFrescas(frescas);
        rep.setMuertas(muertas);
        
        int total = normales.stream().mapToInt(Integer::intValue).sum() + 
                   anormales + duras + frescas + muertas;
        rep.setTotal(total);
        rep.setTablaGerm(tablaGerm);
        
        return rep;
    }
}
