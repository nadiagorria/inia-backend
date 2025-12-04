package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.TablaGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.RepGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ValoresGerm;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.TablaGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.RepGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.ValoresGermRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.GerminacionRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.TablaGermRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.PorcentajesRedondeoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.TablaGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.RepGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ValoresGermDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

@Service
public class TablaGermService {

    @Autowired
    private TablaGermRepository tablaGermRepository;

    @Autowired
    private RepGermRepository repGermRepository;

    @Autowired
    private ValoresGermRepository valoresGermRepository;

    @Autowired
    private GerminacionRepository germinacionRepository;

    @Autowired
    private AnalisisService analisisService;

    
    public TablaGermDTO crearTablaGerm(Long germinacionId, TablaGermRequestDTO solicitud) {
        try {
            Optional<Germinacion> germinacionOpt = germinacionRepository.findById(germinacionId);
            if (germinacionOpt.isEmpty()) {
                throw new RuntimeException("Germinación no encontrada con ID: " + germinacionId);
            }
            
            Germinacion germinacion = germinacionOpt.get();
            
            
            
            validarDatosTablaGerm(solicitud, germinacion);

            
            List<TablaGerm> tablasExistentes = tablaGermRepository.findByGerminacionId(germinacionId);
            if (!tablasExistentes.isEmpty()) {
                
                boolean algunaTablaNoFinalizada = tablasExistentes.stream()
                    .anyMatch(tabla -> tabla.getFinalizada() == null || !tabla.getFinalizada());
                
                if (algunaTablaNoFinalizada) {
                    throw new RuntimeException("No se puede crear una nueva tabla hasta que todas las tablas anteriores estén finalizadas");
                }
                
            
            } else {
                
                germinacion.setEstado(Estado.EN_PROCESO);
                germinacionRepository.save(germinacion);
            }

            TablaGerm tablaGerm = mapearSolicitudAEntidad(solicitud, germinacion);
            
            
            calcularYActualizarTotales(tablaGerm);
            
            TablaGerm tablaGermGuardada = tablaGermRepository.save(tablaGerm);

            
            crearValoresGermAutomaticos(tablaGermGuardada);

            return mapearEntidadADTO(tablaGermGuardada);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear tabla de germinación: " + e.getMessage(), e);
        }
    }
    
    
    
    
    private void validarDatosTablaGerm(TablaGermRequestDTO solicitud, Germinacion germinacion) {
        
        if (solicitud.getFechaFinal() == null) {
            throw new RuntimeException("La fecha final es obligatoria");
        }
        
        if (solicitud.getTratamiento() == null || solicitud.getTratamiento().trim().isEmpty()) {
            throw new RuntimeException("El tratamiento es obligatorio");
        }
        
        if (solicitud.getMetodo() == null || solicitud.getMetodo().trim().isEmpty()) {
            throw new RuntimeException("El método es obligatorio");
        }
        
        if (solicitud.getNumSemillasPRep() == null || solicitud.getNumSemillasPRep() <= 0) {
            throw new RuntimeException("El número de semillas por repetición es obligatorio y debe ser mayor a 0");
        }
        
        if (solicitud.getTemperatura() == null || solicitud.getTemperatura().trim().isEmpty()) {
            throw new RuntimeException("La temperatura es obligatoria");
        }
        
        
        if (solicitud.getFechaIngreso() == null) {
            throw new RuntimeException("La fecha de ingreso es obligatoria");
        }
        
        if (solicitud.getFechaGerminacion() == null) {
            throw new RuntimeException("La fecha de germinación es obligatoria");
        }
        
        if (solicitud.getFechaUltConteo() == null) {
            throw new RuntimeException("La fecha de último conteo es obligatoria");
        }
        
        
        if (!solicitud.getFechaGerminacion().isAfter(solicitud.getFechaIngreso())) {
            throw new RuntimeException("La fecha de germinación debe ser posterior a la fecha de ingreso");
        }
        
        
        int diasPrefrio = (solicitud.getDiasPrefrio() != null) ? solicitud.getDiasPrefrio() : 0;
        if (diasPrefrio > 0) {
            long diasEntreFechas = java.time.temporal.ChronoUnit.DAYS.between(
                solicitud.getFechaIngreso(), 
                solicitud.getFechaGerminacion()
            );
            if (diasEntreFechas < diasPrefrio) {
                throw new RuntimeException("Debe haber al menos " + diasPrefrio + 
                    " días entre la fecha de ingreso y la fecha de germinación (días de prefrío requeridos). " +
                    "Actualmente hay " + diasEntreFechas + " días.");
            }
        }
        
        
        if (!solicitud.getFechaUltConteo().isAfter(solicitud.getFechaGerminacion())) {
            throw new RuntimeException("La fecha de último conteo debe ser posterior a la fecha de germinación");
        }
        
        
        if (solicitud.getFechaFinal().isBefore(solicitud.getFechaGerminacion())) {
            throw new RuntimeException("La fecha final debe ser posterior o igual a la fecha de germinación");
        }
        
        if (solicitud.getFechaFinal().isBefore(solicitud.getFechaUltConteo())) {
            throw new RuntimeException("La fecha final debe ser igual o posterior a la fecha de último conteo");
        }
        
        
        if (solicitud.getNumeroRepeticiones() == null || solicitud.getNumeroRepeticiones() <= 0) {
            throw new RuntimeException("Debe especificar un número válido de repeticiones (mayor a 0)");
        }
        
        if (solicitud.getNumeroRepeticiones() < 1 || solicitud.getNumeroRepeticiones() > 20) {
            throw new RuntimeException("El número de repeticiones debe estar entre 1 y 20");
        }
        
        if (solicitud.getNumeroConteos() == null || solicitud.getNumeroConteos() <= 0) {
            throw new RuntimeException("Debe especificar un número válido de conteos (mayor a 0)");
        }
        
        if (solicitud.getNumeroConteos() < 1 || solicitud.getNumeroConteos() > 15) {
            throw new RuntimeException("El número de conteos debe estar entre 1 y 15");
        }
        
        
        if (solicitud.getFechaConteos() == null || solicitud.getFechaConteos().isEmpty()) {
            throw new RuntimeException("Debe especificar al menos una fecha de conteo");
        }
        
        if (solicitud.getFechaConteos().size() != solicitud.getNumeroConteos()) {
            throw new RuntimeException("El número de fechas de conteos debe coincidir con el número de conteos definido");
        }
        
        
        if (Boolean.TRUE.equals(solicitud.getTienePrefrio())) {
            if (solicitud.getDiasPrefrio() != null && solicitud.getDiasPrefrio() > 0) {
                if (diasPrefrio < 0) {
                    throw new RuntimeException("Los días de prefrío deben ser mayores o iguales a 0");
                }
            }
        }
        
        
        
        
        for (int i = 0; i < solicitud.getFechaConteos().size(); i++) {
            java.time.LocalDate fechaConteo = solicitud.getFechaConteos().get(i);
            if (fechaConteo == null) {
                throw new RuntimeException("La fecha de conteo " + (i + 1) + " no puede ser nula");
            }
            
            
            if (i == 0 && !fechaConteo.isAfter(solicitud.getFechaGerminacion())) {
                throw new RuntimeException("El primer conteo debe realizarse después de la fecha de germinación (fecha mínima: " + 
                    solicitud.getFechaGerminacion().plusDays(1) + ")");
            }
            
            
            if (fechaConteo.isBefore(solicitud.getFechaGerminacion()) || 
                fechaConteo.isAfter(solicitud.getFechaUltConteo())) {
                throw new RuntimeException("La fecha de conteo " + (i + 1) + 
                    " debe estar entre la fecha de germinación y la fecha de último conteo");
            }
            
            
            if (i > 0 && fechaConteo.isBefore(solicitud.getFechaConteos().get(i - 1))) {
                throw new RuntimeException("La fecha de conteo " + (i + 1) + 
                    " debe ser igual o posterior a la fecha de conteo " + i);
            }
        }
    }
    
    
    private void validarPorcentajes(PorcentajesRedondeoRequestDTO solicitud) {
        
        if (solicitud.getPorcentajeNormalesConRedondeo() != null) {
            validarRangoPorcentaje("Normales", solicitud.getPorcentajeNormalesConRedondeo());
        }
        
        if (solicitud.getPorcentajeAnormalesConRedondeo() != null) {
            validarRangoPorcentaje("Anormales", solicitud.getPorcentajeAnormalesConRedondeo());
        }
        
        if (solicitud.getPorcentajeDurasConRedondeo() != null) {
            validarRangoPorcentaje("Duras", solicitud.getPorcentajeDurasConRedondeo());
        }
        
        if (solicitud.getPorcentajeFrescasConRedondeo() != null) {
            validarRangoPorcentaje("Frescas", solicitud.getPorcentajeFrescasConRedondeo());
        }
        
        if (solicitud.getPorcentajeMuertasConRedondeo() != null) {
            validarRangoPorcentaje("Muertas", solicitud.getPorcentajeMuertasConRedondeo());
        }
        
        
        if (solicitud.getPorcentajeNormalesConRedondeo() != null &&
            solicitud.getPorcentajeAnormalesConRedondeo() != null &&
            solicitud.getPorcentajeDurasConRedondeo() != null &&
            solicitud.getPorcentajeFrescasConRedondeo() != null &&
            solicitud.getPorcentajeMuertasConRedondeo() != null) {
            
            double suma = solicitud.getPorcentajeNormalesConRedondeo().doubleValue() +
                         solicitud.getPorcentajeAnormalesConRedondeo().doubleValue() +
                         solicitud.getPorcentajeDurasConRedondeo().doubleValue() +
                         solicitud.getPorcentajeFrescasConRedondeo().doubleValue() +
                         solicitud.getPorcentajeMuertasConRedondeo().doubleValue();
            
            
            if (suma < 99.0 || suma > 101.0) {
                throw new RuntimeException("La suma de todos los porcentajes debe ser aproximadamente 100% (actual: " + suma + "%)");
            }
        }
    }
    
    
    private void validarRangoPorcentaje(String tipo, BigDecimal porcentaje) {
        if (porcentaje.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El porcentaje de " + tipo + " no puede ser negativo");
        }
        
        if (porcentaje.compareTo(new BigDecimal("100")) > 0) {
            throw new RuntimeException("El porcentaje de " + tipo + " no puede ser mayor a 100%");
        }
    }

    
    public TablaGermDTO obtenerTablaGermPorId(Long id) {
        Optional<TablaGerm> tablaGerm = tablaGermRepository.findById(id);
        if (tablaGerm.isPresent()) {
            return mapearEntidadADTO(tablaGerm.get());
        } else {
            throw new RuntimeException("Tabla de germinación no encontrada con ID: " + id);
        }
    }

    
    public TablaGermDTO actualizarTablaGerm(Long id, TablaGermRequestDTO solicitud) {
        Optional<TablaGerm> tablaGermExistente = tablaGermRepository.findById(id);
        
        if (tablaGermExistente.isPresent()) {
            TablaGerm tablaGerm = tablaGermExistente.get();
            
            System.out.println(" Actualizando tabla ID: " + id);
            System.out.println("  Fecha último conteo anterior: " + tablaGerm.getFechaUltConteo());
            System.out.println("  Fecha último conteo nueva: " + solicitud.getFechaUltConteo());
            
            
            boolean debeReiniciarCamposUltimoConteo = false;
            if (solicitud.getFechaUltConteo() != null && tablaGerm.getFechaUltConteo() != null) {
                java.time.LocalDate hoy = java.time.LocalDate.now();
                boolean fechaAnteriorEsPresenteOPasada = !tablaGerm.getFechaUltConteo().isAfter(hoy);
                boolean fechaNuevaEsFutura = solicitud.getFechaUltConteo().isAfter(hoy);
                
                System.out.println("  Fecha anterior es presente/pasada: " + fechaAnteriorEsPresenteOPasada);
                System.out.println("  Fecha nueva es futura: " + fechaNuevaEsFutura);
                
                if (fechaAnteriorEsPresenteOPasada && fechaNuevaEsFutura) {
                    debeReiniciarCamposUltimoConteo = true;
                    System.out.println("   Se detectó cambio de fecha último conteo a futuro - se reiniciarán campos");
                }
            }
            
            
            analisisService.manejarEdicionAnalisisFinalizado(tablaGerm.getGerminacion());
            
            
            validarDatosTablaGerm(solicitud, tablaGerm.getGerminacion());
            
            
            if (debeReiniciarCamposUltimoConteo) {
                reiniciarCamposUltimoConteo(tablaGerm);
            }
            
            actualizarEntidadDesdeSolicitud(tablaGerm, solicitud);
            
            
            calcularYActualizarTotales(tablaGerm);
            
            TablaGerm tablaGermActualizada = tablaGermRepository.save(tablaGerm);

            return mapearEntidadADTO(tablaGermActualizada);
        } else {
            throw new RuntimeException("Tabla de germinación no encontrada con ID: " + id);
        }
    }

    
    public void eliminarTablaGerm(Long id) {
        Optional<TablaGerm> tablaGermExistente = tablaGermRepository.findById(id);
        
        if (tablaGermExistente.isPresent()) {
            
            List<ValoresGerm> valores = valoresGermRepository.findByTablaGermId(id);
            valoresGermRepository.deleteAll(valores);
            
            tablaGermRepository.deleteById(id);
        } else {
            throw new RuntimeException("Tabla de germinación no encontrada con ID: " + id);
        }
    }

    
    public List<TablaGermDTO> obtenerTablasPorGerminacion(Long germinacionId) {
        List<TablaGerm> tablas = tablaGermRepository.findByGerminacionId(germinacionId);
        return tablas.stream().map(this::mapearEntidadADTO).collect(Collectors.toList());
    }

    
    public Long contarTablasPorGerminacion(Long germinacionId) {
        return tablaGermRepository.countByGerminacionId(germinacionId);
    }

    
    public TablaGermDTO actualizarPorcentajes(Long tablaId, PorcentajesRedondeoRequestDTO solicitud) {
        Optional<TablaGerm> tablaExistente = tablaGermRepository.findById(tablaId);
        
        if (tablaExistente.isPresent()) {
            TablaGerm tabla = tablaExistente.get();
            
            
            analisisService.manejarEdicionAnalisisFinalizado(tabla.getGerminacion());
            
            
            if (!puedeIngresarPorcentajes(tablaId)) {
                throw new RuntimeException("No se pueden ingresar porcentajes hasta completar todas las repeticiones");
            }
            
            
            validarPorcentajes(solicitud);
            
            
            tabla.setPorcentajeNormalesConRedondeo(solicitud.getPorcentajeNormalesConRedondeo());
            tabla.setPorcentajeAnormalesConRedondeo(solicitud.getPorcentajeAnormalesConRedondeo());
            tabla.setPorcentajeDurasConRedondeo(solicitud.getPorcentajeDurasConRedondeo());
            tabla.setPorcentajeFrescasConRedondeo(solicitud.getPorcentajeFrescasConRedondeo());
            tabla.setPorcentajeMuertasConRedondeo(solicitud.getPorcentajeMuertasConRedondeo());
            
            TablaGerm tablaActualizada = tablaGermRepository.save(tabla);
            
            
            actualizarValoresInia(tablaActualizada);
            
            return mapearEntidadADTO(tablaActualizada);
        } else {
            throw new RuntimeException("Tabla no encontrada con ID: " + tablaId);
        }
    }

    
    public TablaGermDTO finalizarTabla(Long tablaId) {
        Optional<TablaGerm> tablaExistente = tablaGermRepository.findById(tablaId);
        
        if (tablaExistente.isPresent()) {
            TablaGerm tabla = tablaExistente.get();
            
            
            if (tabla.getFinalizada() != null && tabla.getFinalizada()) {
                throw new RuntimeException("La tabla ya está finalizada");
            }
            
            
            if (!todasLasRepeticionesCompletas(tabla)) {
                throw new RuntimeException("No se puede finalizar la tabla. Faltan repeticiones por completar.");
            }
            
            
            validarRangoToleranciaRepeticiones(tabla);
            
            
            if (!camposPorcentajeCompletos(tabla)) {
                throw new RuntimeException("No se puede finalizar la tabla. Debe ingresar todos los porcentajes con redondeo.");
            }
            
            
            tabla.setFinalizada(true);
            TablaGerm tablaActualizada = tablaGermRepository.save(tabla);
            
            System.out.println("Tabla finalizada exitosamente con ID: " + tablaId);
            return mapearEntidadADTO(tablaActualizada);
        } else {
            throw new RuntimeException("Tabla no encontrada con ID: " + tablaId);
        }
    }
    
    
    private void validarRangoToleranciaRepeticiones(TablaGerm tabla) {
        if (tabla.getRepGerm() == null || tabla.getRepGerm().isEmpty()) {
            return;
        }
        
        Integer numSemillasPRep = tabla.getNumSemillasPRep();
        if (numSemillasPRep == null) {
            return;
        }
        
        int limiteMinimo = (int) Math.floor(numSemillasPRep * 0.95);
        int limiteMaximo = (int) Math.floor(numSemillasPRep * 1.05);
        
        List<String> repeticionesFueraDeRango = new ArrayList<>();
        
        for (RepGerm rep : tabla.getRepGerm()) {
            Integer total = rep.getTotal();
            if (total != null && (total < limiteMinimo || total > limiteMaximo)) {
                repeticionesFueraDeRango.add("Repetición " + rep.getNumRep() + " (total: " + total + ")");
            }
        }
        
        if (!repeticionesFueraDeRango.isEmpty()) {
            throw new RuntimeException("No se puede finalizar la tabla. Las siguientes repeticiones están fuera del rango de tolerancia del 5% (" + 
                limiteMinimo + "-" + limiteMaximo + " semillas): " + String.join(", ", repeticionesFueraDeRango) + 
                ". Se perdió más del 5% de las semillas o hay un exceso.");
        }
    }

    
    private boolean todasLasRepeticionesCompletas(TablaGerm tabla) {
        if (tabla == null || tabla.getNumeroRepeticiones() == null) {
            return false;
        }
        
        
        List<RepGerm> repeticiones = tabla.getRepGerm();
        if (repeticiones == null) {
            return false;
        }
        
        int repeticionesEsperadas = tabla.getNumeroRepeticiones();
        int repeticionesExistentes = repeticiones.size();
        
        
        if (repeticionesExistentes != repeticionesEsperadas) {
            return false;
        }
        
        
        Integer numeroConteos = tabla.getNumeroConteos();
        if (numeroConteos == null) {
            return false;
        }
        
        for (RepGerm repeticion : repeticiones) {
            if (repeticion.getNormales() == null || repeticion.getNormales().size() != numeroConteos) {
                return false;
            }
            
            
            
            for (Integer normal : repeticion.getNormales()) {
                if (normal == null) {
                    return false;
                }
            }
            
            
            boolean tieneValoresIngresados = repeticion.getNormales().stream()
                .anyMatch(valor -> valor != null && valor > 0);
            
            if (!tieneValoresIngresados) {
                return false; 
            }
        }
        
        return true;
    }

    
    private boolean camposPorcentajeCompletos(TablaGerm tabla) {
        return tabla.getPorcentajeNormalesConRedondeo() != null &&
               tabla.getPorcentajeAnormalesConRedondeo() != null &&
               tabla.getPorcentajeDurasConRedondeo() != null &&
               tabla.getPorcentajeFrescasConRedondeo() != null &&
               tabla.getPorcentajeMuertasConRedondeo() != null;
    }

    
    public boolean puedeIngresarPorcentajes(Long tablaId) {
        Optional<TablaGerm> tablaExistente = tablaGermRepository.findById(tablaId);
        
        if (tablaExistente.isPresent()) {
            TablaGerm tabla = tablaExistente.get();
            
            
            
            return todasLasRepeticionesCompletas(tabla);
        }
        
        return false;
    }

    
    private void crearValoresGermAutomaticos(TablaGerm tablaGerm) {
        
        ValoresGerm valoresInia = new ValoresGerm();
        valoresInia.setInstituto(Instituto.INIA);
        valoresInia.setTablaGerm(tablaGerm);
        inicializarValoresEnCero(valoresInia);
        valoresGermRepository.save(valoresInia);

        
        ValoresGerm valoresInase = new ValoresGerm();
        valoresInase.setInstituto(Instituto.INASE);
        valoresInase.setTablaGerm(tablaGerm);
        inicializarValoresEnCero(valoresInase);
        valoresGermRepository.save(valoresInase);
    }

    
    private void inicializarValoresEnCero(ValoresGerm valores) {
        valores.setNormales(BigDecimal.ZERO);
        valores.setAnormales(BigDecimal.ZERO);
        valores.setDuras(BigDecimal.ZERO);
        valores.setFrescas(BigDecimal.ZERO);
        valores.setMuertas(BigDecimal.ZERO);
        valores.setGerminacion(BigDecimal.ZERO);
    }



    
    private void actualizarValoresInia(TablaGerm tablaGerm) {
        Optional<ValoresGerm> valoresIniaOpt = valoresGermRepository.findByTablaGermIdAndInstituto(
            tablaGerm.getTablaGermID(), Instituto.INIA);
        
        if (valoresIniaOpt.isPresent()) {
            ValoresGerm valoresInia = valoresIniaOpt.get();
            
            
            if (tablaGerm.getPorcentajeNormalesConRedondeo() != null) {
                valoresInia.setNormales(tablaGerm.getPorcentajeNormalesConRedondeo());
            }
            if (tablaGerm.getPorcentajeAnormalesConRedondeo() != null) {
                valoresInia.setAnormales(tablaGerm.getPorcentajeAnormalesConRedondeo());
            }
            if (tablaGerm.getPorcentajeDurasConRedondeo() != null) {
                valoresInia.setDuras(tablaGerm.getPorcentajeDurasConRedondeo());
            }
            if (tablaGerm.getPorcentajeFrescasConRedondeo() != null) {
                valoresInia.setFrescas(tablaGerm.getPorcentajeFrescasConRedondeo());
            }
            if (tablaGerm.getPorcentajeMuertasConRedondeo() != null) {
                valoresInia.setMuertas(tablaGerm.getPorcentajeMuertasConRedondeo());
            }
            
            
            valoresInia.setGerminacion(tablaGerm.getPorcentajeNormalesConRedondeo() != null ? 
                tablaGerm.getPorcentajeNormalesConRedondeo() : BigDecimal.ZERO);
            
            valoresGermRepository.save(valoresInia);
        }
    }

    
    private TablaGerm mapearSolicitudAEntidad(TablaGermRequestDTO solicitud, Germinacion germinacion) {
        TablaGerm tablaGerm = new TablaGerm();
        
        tablaGerm.setGerminacion(germinacion);
        
        tablaGerm.setTotal(0);
        
        tablaGerm.setFechaFinal(solicitud.getFechaFinal());
        
        
        tablaGerm.setFinalizada(false);
        
        
        tablaGerm.setTratamiento(solicitud.getTratamiento());
        tablaGerm.setProductoYDosis(solicitud.getProductoYDosis());
        tablaGerm.setNumSemillasPRep(solicitud.getNumSemillasPRep());
        tablaGerm.setMetodo(solicitud.getMetodo());
        tablaGerm.setTemperatura(solicitud.getTemperatura());
        
        
        tablaGerm.setTienePrefrio(solicitud.getTienePrefrio());
        if (Boolean.TRUE.equals(solicitud.getTienePrefrio())) {
            tablaGerm.setDescripcionPrefrio(solicitud.getDescripcionPrefrio());
            tablaGerm.setDiasPrefrio(solicitud.getDiasPrefrio() != null ? solicitud.getDiasPrefrio() : 0);
        } else {
            tablaGerm.setDescripcionPrefrio(null);
            tablaGerm.setDiasPrefrio(0);
        }
        
        tablaGerm.setTienePretratamiento(solicitud.getTienePretratamiento());
        if (Boolean.TRUE.equals(solicitud.getTienePretratamiento())) {
            tablaGerm.setDescripcionPretratamiento(solicitud.getDescripcionPretratamiento());
        } else {
            tablaGerm.setDescripcionPretratamiento(null);
        }
        
        
        tablaGerm.setFechaIngreso(solicitud.getFechaIngreso());
        tablaGerm.setFechaGerminacion(solicitud.getFechaGerminacion());
        tablaGerm.setFechaConteos(solicitud.getFechaConteos());
        tablaGerm.setFechaUltConteo(solicitud.getFechaUltConteo());
        tablaGerm.setNumDias(solicitud.getNumDias());
        tablaGerm.setNumeroRepeticiones(solicitud.getNumeroRepeticiones());
        tablaGerm.setNumeroConteos(solicitud.getNumeroConteos());
        
        return tablaGerm;
    }

    
    private void actualizarEntidadDesdeSolicitud(TablaGerm tablaGerm, TablaGermRequestDTO solicitud) {
        
        
        tablaGerm.setFechaFinal(solicitud.getFechaFinal());
        
        
        tablaGerm.setTratamiento(solicitud.getTratamiento());
        tablaGerm.setProductoYDosis(solicitud.getProductoYDosis());
        tablaGerm.setNumSemillasPRep(solicitud.getNumSemillasPRep());
        tablaGerm.setMetodo(solicitud.getMetodo());
        tablaGerm.setTemperatura(solicitud.getTemperatura());
        
        
        tablaGerm.setTienePrefrio(solicitud.getTienePrefrio());
        if (Boolean.TRUE.equals(solicitud.getTienePrefrio())) {
            tablaGerm.setDescripcionPrefrio(solicitud.getDescripcionPrefrio());
            tablaGerm.setDiasPrefrio(solicitud.getDiasPrefrio() != null ? solicitud.getDiasPrefrio() : 0);
        } else {
            tablaGerm.setDescripcionPrefrio(null);
            tablaGerm.setDiasPrefrio(0);
        }
        
        tablaGerm.setTienePretratamiento(solicitud.getTienePretratamiento());
        if (Boolean.TRUE.equals(solicitud.getTienePretratamiento())) {
            tablaGerm.setDescripcionPretratamiento(solicitud.getDescripcionPretratamiento());
        } else {
            tablaGerm.setDescripcionPretratamiento(null);
        };
        
        
        if (solicitud.getNumeroConteos() != null && tablaGerm.getNumeroConteos() != null &&
            !solicitud.getNumeroConteos().equals(tablaGerm.getNumeroConteos())) {
            
            Integer conteosAnteriores = tablaGerm.getNumeroConteos();
            Integer conteosNuevos = solicitud.getNumeroConteos();
            
            System.out.println(" Cambio en número de conteos detectado: " + conteosAnteriores + " -> " + conteosNuevos);
            
            
            if (tablaGerm.getRepGerm() != null && !tablaGerm.getRepGerm().isEmpty()) {
                for (RepGerm rep : tablaGerm.getRepGerm()) {
                    List<Integer> normalesActuales = rep.getNormales() != null ? 
                        new ArrayList<>(rep.getNormales()) : new ArrayList<>();
                    
                    if (conteosNuevos > conteosAnteriores) {
                        
                        int conteosAAgregar = conteosNuevos - conteosAnteriores;
                        for (int i = 0; i < conteosAAgregar; i++) {
                            normalesActuales.add(0);
                        }
                        System.out.println("   Repetición " + rep.getNumRep() + ": agregados " + conteosAAgregar + " conteos al final");
                    } else {
                        
                        int conteosAEliminar = conteosAnteriores - conteosNuevos;
                        for (int i = 0; i < conteosAEliminar && !normalesActuales.isEmpty(); i++) {
                            normalesActuales.remove(normalesActuales.size() - 1);
                        }
                        System.out.println("   Repetición " + rep.getNumRep() + ": eliminados " + conteosAEliminar + " conteos desde el final");
                    }
                    
                    rep.setNormales(normalesActuales);
                    repGermRepository.save(rep);
                }
            }
            
            
            tablaGerm.setNumeroConteos(conteosNuevos);
        }
        
        
        if (solicitud.getNumeroRepeticiones() != null && tablaGerm.getNumeroRepeticiones() != null &&
            !solicitud.getNumeroRepeticiones().equals(tablaGerm.getNumeroRepeticiones())) {
            
            Integer repeticionesAnteriores = tablaGerm.getNumeroRepeticiones();
            Integer repeticionesNuevas = solicitud.getNumeroRepeticiones();
            
            System.out.println(" Cambio en número de repeticiones detectado: " + repeticionesAnteriores + " -> " + repeticionesNuevas);
            
            if (repeticionesNuevas < repeticionesAnteriores) {
                
                List<RepGerm> repeticiones = repGermRepository.findByTablaGermId(tablaGerm.getTablaGermID());
                repeticiones.sort((r1, r2) -> Integer.compare(r2.getNumRep(), r1.getNumRep())); 
                
                int repeticionesAEliminar = repeticionesAnteriores - repeticionesNuevas;
                for (int i = 0; i < repeticionesAEliminar && i < repeticiones.size(); i++) {
                    RepGerm repAEliminar = repeticiones.get(i);
                    System.out.println("   Eliminando repetición " + repAEliminar.getNumRep());
                    repGermRepository.delete(repAEliminar);
                }
            }
            
            
            
            tablaGerm.setNumeroRepeticiones(repeticionesNuevas);
        }
        
        
        if (solicitud.getFechaConteos() != null && !solicitud.getFechaConteos().isEmpty()) {
            
            boolean fechasCambiaron = false;
            if (tablaGerm.getFechaConteos() == null || 
                tablaGerm.getFechaConteos().size() != solicitud.getFechaConteos().size()) {
                fechasCambiaron = true;
            } else {
                for (int i = 0; i < solicitud.getFechaConteos().size(); i++) {
                    if (!solicitud.getFechaConteos().get(i).equals(tablaGerm.getFechaConteos().get(i))) {
                        fechasCambiaron = true;
                        
                        if (solicitud.getFechaConteos().get(i).isAfter(tablaGerm.getFechaConteos().get(i))) {
                            
                            reiniciarDatosConteo(tablaGerm, i);
                        }
                    }
                }
            }
            
            if (fechasCambiaron) {
                
                validarFechasConteosEnEdicion(solicitud, tablaGerm);
                tablaGerm.setFechaConteos(solicitud.getFechaConteos());
            }
        }
        
        
        if (solicitud.getFechaIngreso() != null) {
            tablaGerm.setFechaIngreso(solicitud.getFechaIngreso());
        }
        
        if (solicitud.getFechaGerminacion() != null) {
            tablaGerm.setFechaGerminacion(solicitud.getFechaGerminacion());
        }
        
        if (solicitud.getFechaUltConteo() != null) {
            tablaGerm.setFechaUltConteo(solicitud.getFechaUltConteo());
        }
        
        if (solicitud.getNumDias() != null) {
            tablaGerm.setNumDias(solicitud.getNumDias());
        }
        
        
        if (solicitud.getDiasPrefrio() != null) {
            tablaGerm.setDiasPrefrio(solicitud.getDiasPrefrio());
        }
    }
    
    
    private void reiniciarDatosConteo(TablaGerm tablaGerm, int indiceConteo) {
        if (tablaGerm.getRepGerm() != null) {
            for (RepGerm rep : tablaGerm.getRepGerm()) {
                if (rep.getNormales() != null && indiceConteo < rep.getNormales().size()) {
                    rep.getNormales().set(indiceConteo, 0);
                    repGermRepository.save(rep);
                }
            }
        }
    }
    
    
    private void reiniciarCamposUltimoConteo(TablaGerm tablaGerm) {
        System.out.println(" Reiniciando campos del último conteo para tabla ID: " + tablaGerm.getTablaGermID());
        
        
        List<RepGerm> repeticiones = repGermRepository.findByTablaGermId(tablaGerm.getTablaGermID());
        
        System.out.println("   Repeticiones encontradas: " + repeticiones.size());
        
        if (repeticiones != null && !repeticiones.isEmpty()) {
            for (RepGerm rep : repeticiones) {
                System.out.println("   Limpiando repetición " + rep.getNumRep() + 
                    " (ID: " + rep.getRepGermID() + ")" +
                    " - Valores anteriores: anormales=" + rep.getAnormales() + 
                    ", duras=" + rep.getDuras() + 
                    ", frescas=" + rep.getFrescas() + 
                    ", muertas=" + rep.getMuertas());
                
                
                rep.setAnormales(0);
                rep.setDuras(0);
                rep.setFrescas(0);
                rep.setMuertas(0);
                
                RepGerm repGuardada = repGermRepository.save(rep);
                System.out.println("   Repetición " + rep.getNumRep() + " limpiada - Nuevos valores: anormales=" + 
                    repGuardada.getAnormales() + ", duras=" + repGuardada.getDuras() + 
                    ", frescas=" + repGuardada.getFrescas() + ", muertas=" + repGuardada.getMuertas());
            }
        } else {
            System.out.println("   No se encontraron repeticiones para limpiar");
        }
        
        System.out.println(" Campos del último conteo reiniciados completamente");
    }
    
    
    private void validarFechasConteosEnEdicion(TablaGermRequestDTO solicitud, TablaGerm tablaExistente) {
        
        
        java.time.LocalDate fechaGerminacion = solicitud.getFechaGerminacion() != null ? 
            solicitud.getFechaGerminacion() : tablaExistente.getFechaGerminacion();
        
        for (int i = 0; i < solicitud.getFechaConteos().size(); i++) {
            java.time.LocalDate fechaConteo = solicitud.getFechaConteos().get(i);
            
            if (i == 0 && !fechaConteo.isAfter(fechaGerminacion)) {
                throw new RuntimeException("El primer conteo debe realizarse después de la fecha de germinación (fecha mínima: " + 
                    fechaGerminacion.plusDays(1) + ")");
            }
        }
    }

    
    private TablaGermDTO mapearEntidadADTO(TablaGerm tablaGerm) {
        TablaGermDTO dto = new TablaGermDTO();
        
        dto.setTablaGermID(tablaGerm.getTablaGermID());
        
        
        if (tablaGerm.getRepGerm() != null) {
            List<RepGermDTO> repGermDTOs = tablaGerm.getRepGerm().stream()
                .map(this::mapearRepGermADTO)
                .collect(Collectors.toList());
            dto.setRepGerm(repGermDTOs);
        }
        
        dto.setTotal(tablaGerm.getTotal());
        dto.setPromedioSinRedondeo(tablaGerm.getPromedioSinRedondeo());
        dto.setPromediosSinRedPorConteo(tablaGerm.getPromediosSinRedPorConteo());
        
        
        dto.setPorcentajeNormalesConRedondeo(tablaGerm.getPorcentajeNormalesConRedondeo());
        dto.setPorcentajeAnormalesConRedondeo(tablaGerm.getPorcentajeAnormalesConRedondeo());
        dto.setPorcentajeDurasConRedondeo(tablaGerm.getPorcentajeDurasConRedondeo());
        dto.setPorcentajeFrescasConRedondeo(tablaGerm.getPorcentajeFrescasConRedondeo());
        dto.setPorcentajeMuertasConRedondeo(tablaGerm.getPorcentajeMuertasConRedondeo());
        
        
        if (tablaGerm.getValoresGerm() != null) {
            List<ValoresGermDTO> valoresDTO = tablaGerm.getValoresGerm().stream()
                .map(this::mapearValoresGermADTO)
                .collect(Collectors.toList());
            dto.setValoresGerm(valoresDTO);
        }
        
        dto.setFechaFinal(tablaGerm.getFechaFinal());
        
        
        dto.setFinalizada(tablaGerm.getFinalizada());
        
        
        dto.setTratamiento(tablaGerm.getTratamiento());
        dto.setProductoYDosis(tablaGerm.getProductoYDosis());
        dto.setNumSemillasPRep(tablaGerm.getNumSemillasPRep());
        dto.setMetodo(tablaGerm.getMetodo());
        dto.setTemperatura(tablaGerm.getTemperatura());
        
        
        dto.setTienePrefrio(tablaGerm.getTienePrefrio());
        dto.setDescripcionPrefrio(tablaGerm.getDescripcionPrefrio());
        dto.setTienePretratamiento(tablaGerm.getTienePretratamiento());
        dto.setDescripcionPretratamiento(tablaGerm.getDescripcionPretratamiento());
        
        
        dto.setFechaIngreso(tablaGerm.getFechaIngreso());
        dto.setFechaGerminacion(tablaGerm.getFechaGerminacion());
        dto.setFechaConteos(tablaGerm.getFechaConteos());
        dto.setFechaUltConteo(tablaGerm.getFechaUltConteo());
        dto.setNumDias(tablaGerm.getNumDias());
        dto.setNumeroRepeticiones(tablaGerm.getNumeroRepeticiones());
        dto.setNumeroConteos(tablaGerm.getNumeroConteos());
        dto.setDiasPrefrio(tablaGerm.getDiasPrefrio());
        
        return dto;
    }
    
    
    private RepGermDTO mapearRepGermADTO(RepGerm repGerm) {
        RepGermDTO dto = new RepGermDTO();
        dto.setRepGermID(repGerm.getRepGermID());
        dto.setNumRep(repGerm.getNumRep());
        dto.setNormales(repGerm.getNormales());
        dto.setAnormales(repGerm.getAnormales());
        dto.setDuras(repGerm.getDuras());
        dto.setFrescas(repGerm.getFrescas());
        dto.setMuertas(repGerm.getMuertas());
        dto.setTotal(repGerm.getTotal());
        dto.setTablaGermId(repGerm.getTablaGerm().getTablaGermID());
        return dto;
    }

    
    private ValoresGermDTO mapearValoresGermADTO(ValoresGerm valores) {
        ValoresGermDTO dto = new ValoresGermDTO();
        dto.setValoresGermID(valores.getValoresGermID());
        dto.setInstituto(valores.getInstituto());
        dto.setNormales(valores.getNormales());
        dto.setAnormales(valores.getAnormales());
        dto.setDuras(valores.getDuras());
        dto.setFrescas(valores.getFrescas());
        dto.setMuertas(valores.getMuertas());
        dto.setGerminacion(valores.getGerminacion());
        dto.setTablaGermId(valores.getTablaGerm().getTablaGermID());
        return dto;
    }
    
    
    private void calcularYActualizarTotales(TablaGerm tablaGerm) {
        if (tablaGerm.getTablaGermID() == null) {
            
            tablaGerm.setTotal(0);
            return;
        }
        
        
        List<RepGerm> repeticiones = repGermRepository.findByTablaGermId(tablaGerm.getTablaGermID());
        
        
        int totalCalculado = repeticiones.stream()
            .mapToInt(rep -> rep.getTotal() != null ? rep.getTotal().intValue() : 0)
            .sum();
            
        tablaGerm.setTotal(totalCalculado);
    }

}