package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.TablaGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.TablaGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.RepGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepGermDTO;

@Service
public class RepGermService {

    @Autowired
    private RepGermRepository repGermRepository;
    
    @Autowired
    private TablaGermRepository tablaGermRepository;

    @Autowired
    private AnalisisService analisisService;

    
    public RepGermDTO crearRepGerm(Long tablaGermId, RepGermRequestDTO solicitud) {
        try {
            System.out.println("Creando repetición para tabla ID: " + tablaGermId);
            
            
            Optional<TablaGerm> tablaGermOpt = tablaGermRepository.findById(tablaGermId);
            if (tablaGermOpt.isEmpty()) {
                throw new RuntimeException("Tabla no encontrada con ID: " + tablaGermId);
            }
            
            TablaGerm tablaGerm = tablaGermOpt.get();
            
            
            if (tablaGerm != null && tablaGerm.getNumeroRepeticiones() != null) {
                Long repeticionesExistentes = repGermRepository.countByTablaGermId(tablaGermId);
                if (repeticionesExistentes >= tablaGerm.getNumeroRepeticiones()) {
                    throw new RuntimeException("No se pueden agregar más repeticiones. El número máximo de repeticiones permitidas es: " + 
                        tablaGerm.getNumeroRepeticiones());
                }
            }

            
            RepGerm repGerm = mapearSolicitudAEntidad(solicitud, tablaGerm);
            RepGerm repGermGuardada = repGermRepository.save(repGerm);
            
            
            actualizarTotalesTablaGerm(tablaGerm);
            
            System.out.println("Repetición creada exitosamente con ID: " + repGermGuardada.getRepGermID());
            return mapearEntidadADTO(repGermGuardada);
        } catch (Exception e) {
            System.err.println("Error al crear repetición: " + e.getMessage());
            throw new RuntimeException("Error al crear la repetición: " + e.getMessage());
        }
    }

    
    public RepGermDTO obtenerRepGermPorId(Long id) {
        Optional<RepGerm> repGerm = repGermRepository.findById(id);
        if (repGerm.isPresent()) {
            return mapearEntidadADTO(repGerm.get());
        } else {
            throw new RuntimeException("Repetición no encontrada con ID: " + id);
        }
    }

    
    public RepGermDTO actualizarRepGerm(Long id, RepGermRequestDTO solicitud) {
        Optional<RepGerm> repGermExistente = repGermRepository.findById(id);
        
        if (repGermExistente.isPresent()) {
            RepGerm repGerm = repGermExistente.get();
            
            
            analisisService.manejarEdicionAnalisisFinalizado(repGerm.getTablaGerm().getGerminacion());
        
            
            validarDatosRepeticion(solicitud, repGerm.getTablaGerm());
            
            actualizarEntidadDesdeSolicitud(repGerm, solicitud);
            RepGerm repGermActualizada = repGermRepository.save(repGerm);
            
            
            actualizarTotalesTablaGerm(repGermActualizada.getTablaGerm());
            
            return mapearEntidadADTO(repGermActualizada);
        } else {
            throw new RuntimeException("Repetición no encontrada con ID: " + id);
        }
    }

    
    public void eliminarRepGerm(Long id) {
        Optional<RepGerm> repGermExistente = repGermRepository.findById(id);
        
        if (repGermExistente.isPresent()) {
            repGermRepository.deleteById(id);
            
            System.out.println("Repetición eliminada con ID: " + id);
        } else {
            throw new RuntimeException("Repetición no encontrada con ID: " + id);
        }
    }

    
    public List<RepGermDTO> obtenerRepeticionesPorTabla(Long tablaGermId) {
        List<RepGerm> repeticiones = repGermRepository.findByTablaGermId(tablaGermId);
        return repeticiones.stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    
    public Long contarRepeticionesPorTabla(Long tablaGermId) {
        return repGermRepository.countByTablaGermId(tablaGermId);
    }

    
    private RepGerm mapearSolicitudAEntidad(RepGermRequestDTO solicitud, TablaGerm tablaGerm) {
        
        validarDatosRepeticion(solicitud, tablaGerm);
        
        RepGerm repGerm = new RepGerm();
        
        
        repGerm.setTablaGerm(tablaGerm);
        
        
        Long repeticionesExistentes = repGermRepository.countByTablaGermId(tablaGerm.getTablaGermID());
        repGerm.setNumRep(repeticionesExistentes.intValue() + 1);
        
        
        Integer numeroConteos = (tablaGerm != null && tablaGerm.getNumeroConteos() != null) 
            ? tablaGerm.getNumeroConteos() 
            : 1; 
            
        List<Integer> normalesInicializadas = new ArrayList<>(Collections.nCopies(numeroConteos, 0));
        
        
        if (solicitud.getNormales() != null && !solicitud.getNormales().isEmpty()) {
            for (int i = 0; i < Math.min(solicitud.getNormales().size(), numeroConteos); i++) {
                if (solicitud.getNormales().get(i) != null) {
                    normalesInicializadas.set(i, solicitud.getNormales().get(i));
                }
            }
        }
        
        repGerm.setNormales(normalesInicializadas);
        
        repGerm.setAnormales(solicitud.getAnormales() != null ? solicitud.getAnormales() : 0);
        repGerm.setDuras(solicitud.getDuras() != null ? solicitud.getDuras() : 0);
        repGerm.setFrescas(solicitud.getFrescas() != null ? solicitud.getFrescas() : 0);
        repGerm.setMuertas(solicitud.getMuertas() != null ? solicitud.getMuertas() : 0);
        
        
        Integer totalCalculado = calcularTotal(normalesInicializadas, repGerm.getAnormales(), 
                                             repGerm.getDuras(), repGerm.getFrescas(), repGerm.getMuertas());
        repGerm.setTotal(totalCalculado);
        
        repGerm.setTotal(totalCalculado);
        
        
        if (totalCalculado == 0) {
            throw new RuntimeException("Debe ingresar al menos un valor");
        }
        
        
        
        
        if (tablaGerm.getNumSemillasPRep() != null) {
            int limiteMaximo = (int) Math.floor(tablaGerm.getNumSemillasPRep() * 1.05);
            
            if (totalCalculado > limiteMaximo) {
                throw new RuntimeException("El total de la repetición (" + totalCalculado + 
                    ") excede el límite máximo permitido (" + limiteMaximo + 
                    " - con 5% de tolerancia sobre " + tablaGerm.getNumSemillasPRep() + " semillas)");
            }
        }
        
        return repGerm;
    }
    
    /**
     * Validar datos de la repetición
     */
    private void validarDatosRepeticion(RepGermRequestDTO solicitud, TablaGerm tablaGerm) {
        Integer numSemillasPRep = tablaGerm.getNumSemillasPRep();
        
        
        if (solicitud.getAnormales() != null) {
            if (solicitud.getAnormales() < 0) {
                throw new RuntimeException("El número de semillas anormales no puede ser negativo");
            }
            if (numSemillasPRep != null && solicitud.getAnormales() > numSemillasPRep) {
                throw new RuntimeException("El número de semillas anormales no puede exceder " + numSemillasPRep);
            }
        }
        
        if (solicitud.getDuras() != null) {
            if (solicitud.getDuras() < 0) {
                throw new RuntimeException("El número de semillas duras no puede ser negativo");
            }
            if (numSemillasPRep != null && solicitud.getDuras() > numSemillasPRep) {
                throw new RuntimeException("El número de semillas duras no puede exceder " + numSemillasPRep);
            }
        }
        
        if (solicitud.getFrescas() != null) {
            if (solicitud.getFrescas() < 0) {
                throw new RuntimeException("El número de semillas frescas no puede ser negativo");
            }
            if (numSemillasPRep != null && solicitud.getFrescas() > numSemillasPRep) {
                throw new RuntimeException("El número de semillas frescas no puede exceder " + numSemillasPRep);
            }
        }
        
        if (solicitud.getMuertas() != null) {
            if (solicitud.getMuertas() < 0) {
                throw new RuntimeException("El número de semillas muertas no puede ser negativo");
            }
            if (numSemillasPRep != null && solicitud.getMuertas() > numSemillasPRep) {
                throw new RuntimeException("El número de semillas muertas no puede exceder " + numSemillasPRep);
            }
        }
        
        
        if (solicitud.getNormales() != null) {
            for (int i = 0; i < solicitud.getNormales().size(); i++) {
                Integer valor = solicitud.getNormales().get(i);
                if (valor != null) {
                    if (valor < 0) {
                        throw new RuntimeException("El valor del conteo " + (i + 1) + " de normales no puede ser negativo");
                    }
                    if (numSemillasPRep != null && valor > numSemillasPRep) {
                        throw new RuntimeException("El valor del conteo " + (i + 1) + " de normales no puede exceder " + numSemillasPRep);
                    }
                }
            }
        }
    }
    
    
    private Integer calcularTotal(List<Integer> normales, Integer anormales, Integer duras, Integer frescas, Integer muertas) {
        int total = 0;
        
        
        if (normales != null) {
            for (Integer normal : normales) {
                if (normal != null) {
                    total += normal;
                }
            }
        }
        
        
        total += (anormales != null ? anormales : 0);
        total += (duras != null ? duras : 0);
        total += (frescas != null ? frescas : 0);
        total += (muertas != null ? muertas : 0);
        
        return total;
    }

    
    private void actualizarEntidadDesdeSolicitud(RepGerm repGerm, RepGermRequestDTO solicitud) {
        
        
        
        Integer numeroConteos = (repGerm.getTablaGerm() != null && 
                               repGerm.getTablaGerm().getNumeroConteos() != null) 
            ? repGerm.getTablaGerm().getNumeroConteos() 
            : 1;
        
        List<Integer> normalesActualizadas = new ArrayList<>(Collections.nCopies(numeroConteos, 0));
        
        
        if (solicitud.getNormales() != null && !solicitud.getNormales().isEmpty()) {
            for (int i = 0; i < Math.min(solicitud.getNormales().size(), numeroConteos); i++) {
                if (solicitud.getNormales().get(i) != null) {
                    normalesActualizadas.set(i, solicitud.getNormales().get(i));
                }
            }
        }
        
        repGerm.setNormales(normalesActualizadas);
        
        repGerm.setAnormales(solicitud.getAnormales() != null ? solicitud.getAnormales() : 0);
        repGerm.setDuras(solicitud.getDuras() != null ? solicitud.getDuras() : 0);
        repGerm.setFrescas(solicitud.getFrescas() != null ? solicitud.getFrescas() : 0);
        repGerm.setMuertas(solicitud.getMuertas() != null ? solicitud.getMuertas() : 0);
        
        
        Integer totalCalculado = calcularTotal(normalesActualizadas, repGerm.getAnormales(), 
                                             repGerm.getDuras(), repGerm.getFrescas(), repGerm.getMuertas());
        
        
        if (totalCalculado == 0) {
            throw new RuntimeException("Debe ingresar al menos un valor");
        }
        repGerm.setTotal(totalCalculado);
        
        
        
        
        if (repGerm.getTablaGerm().getNumSemillasPRep() != null) {
            int limiteMaximo = (int) Math.floor(repGerm.getTablaGerm().getNumSemillasPRep() * 1.05);
            
            if (totalCalculado > limiteMaximo) {
                throw new RuntimeException("El total de la repetición (" + totalCalculado + 
                    ") excede el límite máximo permitido (" + limiteMaximo + 
                    " - con 5% de tolerancia sobre " + repGerm.getTablaGerm().getNumSemillasPRep() + " semillas)");
            }
        }
        
        
        repGerm.setTotal(totalCalculado);
        
    }

    
    private RepGermDTO mapearEntidadADTO(RepGerm repGerm) {
        RepGermDTO dto = new RepGermDTO();
        dto.setRepGermID(repGerm.getRepGermID());
        dto.setNumRep(repGerm.getNumRep());
        dto.setNormales(repGerm.getNormales());
        dto.setAnormales(repGerm.getAnormales());
        dto.setDuras(repGerm.getDuras());
        dto.setFrescas(repGerm.getFrescas());
        dto.setMuertas(repGerm.getMuertas());
        dto.setTotal(repGerm.getTotal());
        
        
        if (repGerm.getTablaGerm() != null) {
            dto.setTablaGermId(repGerm.getTablaGerm().getTablaGermID());
        }
        
        return dto;
    }
    
    
    private void actualizarTotalesTablaGerm(TablaGerm tablaGerm) {
        
        List<RepGerm> repeticiones = repGermRepository.findByTablaGermId(tablaGerm.getTablaGermID());
        
        
        int totalCalculado = repeticiones.stream()
            .mapToInt(rep -> rep.getTotal() != null ? rep.getTotal() : 0)
            .sum();
            
        tablaGerm.setTotal(totalCalculado);
        
        
        if (todasLasRepeticionesCompletas(tablaGerm, repeticiones)) {
            calcularPromediosSinRedondeo(tablaGerm, repeticiones);
        }
        
        
        tablaGermRepository.save(tablaGerm);
    }
    
    
    private boolean todasLasRepeticionesCompletas(TablaGerm tablaGerm, List<RepGerm> repeticiones) {
        if (tablaGerm == null || tablaGerm.getNumeroRepeticiones() == null) {
            return false;
        }
        
        
        if (repeticiones.size() < tablaGerm.getNumeroRepeticiones()) {
            return false;
        }
        
        
        return repeticiones.stream().allMatch(rep -> 
            rep.getTotal() != null && rep.getTotal() > 0
        );
    }
    
    
    
    private void calcularPromediosSinRedondeo(TablaGerm tablaGerm, List<RepGerm> repeticiones) {
        if (repeticiones.isEmpty()) {
            tablaGerm.setPromedioSinRedondeo(new ArrayList<>());
            return;
        }
        
        int numRepeticiones = repeticiones.size();
        List<BigDecimal> promedios = new ArrayList<>();
        
        
        int sumaNormales = repeticiones.stream()
            .flatMapToInt(rep -> rep.getNormales() != null ? rep.getNormales().stream().mapToInt(Integer::intValue) : java.util.stream.IntStream.empty())
            .sum();
        BigDecimal promedioNormales = BigDecimal.valueOf(sumaNormales).divide(BigDecimal.valueOf(numRepeticiones), 2, RoundingMode.HALF_UP);
        promedios.add(promedioNormales);
        
        
        int sumaAnormales = repeticiones.stream()
            .mapToInt(rep -> rep.getAnormales() != null ? rep.getAnormales() : 0)
            .sum();
        BigDecimal promedioAnormales = BigDecimal.valueOf(sumaAnormales).divide(BigDecimal.valueOf(numRepeticiones), 2, RoundingMode.HALF_UP);
        promedios.add(promedioAnormales);
        
        
        int sumaDuras = repeticiones.stream()
            .mapToInt(rep -> rep.getDuras() != null ? rep.getDuras() : 0)
            .sum();
        BigDecimal promedioDuras = BigDecimal.valueOf(sumaDuras).divide(BigDecimal.valueOf(numRepeticiones), 2, RoundingMode.HALF_UP);
        promedios.add(promedioDuras);
        
        
        int sumaFrescas = repeticiones.stream()
            .mapToInt(rep -> rep.getFrescas() != null ? rep.getFrescas() : 0)
            .sum();
        BigDecimal promedioFrescas = BigDecimal.valueOf(sumaFrescas).divide(BigDecimal.valueOf(numRepeticiones), 2, RoundingMode.HALF_UP);
        promedios.add(promedioFrescas);
        
        
        int sumaMuertas = repeticiones.stream()
            .mapToInt(rep -> rep.getMuertas() != null ? rep.getMuertas() : 0)
            .sum();
        BigDecimal promedioMuertas = BigDecimal.valueOf(sumaMuertas).divide(BigDecimal.valueOf(numRepeticiones), 2, RoundingMode.HALF_UP);
        promedios.add(promedioMuertas);
        
        tablaGerm.setPromedioSinRedondeo(promedios);
        
        
        calcularPromediosPorConteo(tablaGerm, repeticiones);
    }
    
    
    private void calcularPromediosPorConteo(TablaGerm tablaGerm, List<RepGerm> repeticiones) {
        if (repeticiones.isEmpty()) {
            tablaGerm.setPromediosSinRedPorConteo(new ArrayList<>());
            return;
        }
        
        int numRepeticiones = repeticiones.size();
        List<BigDecimal> promediosPorConteo = new ArrayList<>();
        
        
        Integer numConteos = tablaGerm != null ? tablaGerm.getNumeroConteos() : null;
            
        if (numConteos != null && numConteos > 0) {
            
            for (int conteo = 0; conteo < numConteos; conteo++) {
                final int conteoIndex = conteo;
                int sumaNormalesConteo = repeticiones.stream()
                    .mapToInt(rep -> {
                        if (rep.getNormales() != null && rep.getNormales().size() > conteoIndex) {
                            return rep.getNormales().get(conteoIndex);
                        }
                        return 0;
                    })
                    .sum();
                BigDecimal promedioConteo = BigDecimal.valueOf(sumaNormalesConteo)
                    .divide(BigDecimal.valueOf(numRepeticiones), 2, RoundingMode.HALF_UP);
                promediosPorConteo.add(promedioConteo);
            }
        }
        
        
        
        int sumaAnormales = repeticiones.stream()
            .mapToInt(rep -> rep.getAnormales() != null ? rep.getAnormales() : 0)
            .sum();
        BigDecimal promedioAnormales = BigDecimal.valueOf(sumaAnormales).divide(BigDecimal.valueOf(numRepeticiones), 2, RoundingMode.HALF_UP);
        promediosPorConteo.add(promedioAnormales);
        
        
        int sumaDuras = repeticiones.stream()
            .mapToInt(rep -> rep.getDuras() != null ? rep.getDuras() : 0)
            .sum();
        BigDecimal promedioDuras = BigDecimal.valueOf(sumaDuras).divide(BigDecimal.valueOf(numRepeticiones), 2, RoundingMode.HALF_UP);
        promediosPorConteo.add(promedioDuras);
        
        
        int sumaFrescas = repeticiones.stream()
            .mapToInt(rep -> rep.getFrescas() != null ? rep.getFrescas() : 0)
            .sum();
        BigDecimal promedioFrescas = BigDecimal.valueOf(sumaFrescas).divide(BigDecimal.valueOf(numRepeticiones), 2, RoundingMode.HALF_UP);
        promediosPorConteo.add(promedioFrescas);
        
        
        int sumaMuertas = repeticiones.stream()
            .mapToInt(rep -> rep.getMuertas() != null ? rep.getMuertas() : 0)
            .sum();
        BigDecimal promedioMuertas = BigDecimal.valueOf(sumaMuertas).divide(BigDecimal.valueOf(numRepeticiones), 2, RoundingMode.HALF_UP);
        promediosPorConteo.add(promedioMuertas);
        
        tablaGerm.setPromediosSinRedPorConteo(promediosPorConteo);
    }
}