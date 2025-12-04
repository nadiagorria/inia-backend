package utec.proyectofinal.Proyecto.Final.UTEC.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.*;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.*;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.*;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final GerminacionRepository germinacionRepository;
    private final PurezaRepository purezaRepository;
    private final PmsRepository pmsRepository;
    private final TetrazolioRepository tetrazolioRepository;
    private final DosnRepository dosnRepository;

    public ReporteGeneralDTO obtenerReporteGeneral(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = fechaFin != null ? fechaFin.atTime(23, 59, 59) : null;

        List<Analisis> todosAnalisis = new ArrayList<>();
        todosAnalisis.addAll(obtenerAnalisisPorFecha(germinacionRepository.findAll(), inicio, fin));
        todosAnalisis.addAll(obtenerAnalisisPorFecha(purezaRepository.findAll(), inicio, fin));
        todosAnalisis.addAll(obtenerAnalisisPorFecha(pmsRepository.findAll(), inicio, fin));
        todosAnalisis.addAll(obtenerAnalisisPorFecha(tetrazolioRepository.findAll(), inicio, fin));
        todosAnalisis.addAll(obtenerAnalisisPorFecha(dosnRepository.findAll(), inicio, fin));

        Long totalAnalisis = (long) todosAnalisis.size();

        Map<String, Long> analisisPorPeriodo = calcularAnalisisPorPeriodo(todosAnalisis);
        Map<String, Long> analisisPorEstado = calcularAnalisisPorEstado(todosAnalisis);
        Map<String, Double> porcentajeCompletitud = calcularPorcentajeCompletitud(analisisPorEstado, totalAnalisis);
        Double tiempoMedioFinalizacion = calcularTiempoMedioFinalizacion(todosAnalisis);
        Map<String, Long> topAnalisisProblemas = calcularTopAnalisisProblemas(inicio, fin);

        return new ReporteGeneralDTO(
            totalAnalisis,
            analisisPorPeriodo,
            analisisPorEstado,
            porcentajeCompletitud,
            tiempoMedioFinalizacion,
            topAnalisisProblemas
        );
    }

    public ReporteGerminacionDTO obtenerReporteGerminacion(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = fechaFin != null ? fechaFin.atTime(23, 59, 59) : null;

        List<Germinacion> germinaciones = obtenerAnalisisPorFecha(germinacionRepository.findAll(), inicio, fin);

        Map<String, Double> mediaGerminacionPorEspecie = calcularMediaGerminacionPorEspecie(germinaciones);
        Map<String, Double> tiempoPromedioPrimerConteo = calcularTiempoPromedioPrimerConteo(germinaciones);
        Map<String, Double> tiempoPromedioUltimoConteo = calcularTiempoPromedioUltimoConteo(germinaciones);

        return new ReporteGerminacionDTO(
            mediaGerminacionPorEspecie,
            tiempoPromedioPrimerConteo,
            tiempoPromedioUltimoConteo,
            (long) germinaciones.size()
        );
    }

    public ReportePMSDTO obtenerReportePms(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = fechaFin != null ? fechaFin.atTime(23, 59, 59) : null;

        List<Pms> pmsList = obtenerAnalisisPorFecha(pmsRepository.findAll(), inicio, fin);

        long totalPms = pmsList.size();
        long muestrasConCVSuperado = pmsList.stream()
            .filter(pms -> pms.getCoefVariacion() != null && pms.getCoefVariacion().compareTo(BigDecimal.valueOf(6.0)) > 0)
            .count();

        double porcentaje = totalPms > 0 ? (muestrasConCVSuperado * 100.0 / totalPms) : 0.0;

        long muestrasConRepeticionesMaximas = pmsList.stream()
            .filter(pms -> pms.getNumRepeticionesEsperadas() != null && pms.getNumRepeticionesEsperadas() >= 16)
            .count();

        // Calcular PMS promedio por especie
        Map<String, Double> pmsPorEspecie = pmsList.stream()
            .filter(pms -> pms.getLote() != null && pms.getLote().getCultivar() != null && 
                          pms.getLote().getCultivar().getEspecie() != null)
            .filter(pms -> pms.getPmsconRedon() != null)
            .collect(Collectors.groupingBy(
                pms -> pms.getLote().getCultivar().getEspecie().getNombreComun(),
                Collectors.averagingDouble(pms -> pms.getPmsconRedon().doubleValue())
            ));

        return new ReportePMSDTO(
            totalPms,
            muestrasConCVSuperado,
            porcentaje,
            muestrasConRepeticionesMaximas,
            pmsPorEspecie
        );
    }

    public ReportePurezaDTO obtenerReportePureza(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = fechaFin != null ? fechaFin.atTime(23, 59, 59) : null;

        List<Pureza> purezas = obtenerAnalisisPorFecha(purezaRepository.findAll(), inicio, fin);

        Map<String, Long> contaminantesPorEspecie = new HashMap<>();
        Map<String, Double> porcentajeMalezas = new HashMap<>();
        Map<String, Double> porcentajeOtrasSemillas = new HashMap<>();
        Map<String, Double> porcentajeCumpleEstandar = new HashMap<>();

        Map<String, List<Pureza>> purezasPorEspecie = purezas.stream()
            .filter(p -> p.getLote() != null && p.getLote().getCultivar() != null && p.getLote().getCultivar().getEspecie() != null)
            .collect(Collectors.groupingBy(
                p -> p.getLote().getCultivar().getEspecie().getNombreComun(),
                Collectors.toList()
            ));

        for (Map.Entry<String, List<Pureza>> entry : purezasPorEspecie.entrySet()) {
            String especie = entry.getKey();
            List<Pureza> purezasEspecie = entry.getValue();

            double totalMalezas = purezasEspecie.stream()
                .filter(p -> p.getMalezas_g() != null && p.getPesoInicial_g() != null && p.getPesoInicial_g().compareTo(BigDecimal.ZERO) > 0)
                .mapToDouble(p -> p.getMalezas_g().divide(p.getPesoInicial_g(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue())
                .average()
                .orElse(0.0);

            double totalOtrasSemillas = purezasEspecie.stream()
                .filter(p -> p.getOtrosCultivos_g() != null && p.getPesoInicial_g() != null && p.getPesoInicial_g().compareTo(BigDecimal.ZERO) > 0)
                .mapToDouble(p -> p.getOtrosCultivos_g().divide(p.getPesoInicial_g(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue())
                .average()
                .orElse(0.0);

            long cumpleEstandar = purezasEspecie.stream()
                .filter(p -> p.getCumpleEstandar() != null && p.getCumpleEstandar())
                .count();

            double porcentajeCumple = purezasEspecie.size() > 0 ? (cumpleEstandar * 100.0 / purezasEspecie.size()) : 0.0;

            contaminantesPorEspecie.put(especie, (long) purezasEspecie.size());
            porcentajeMalezas.put(especie, totalMalezas);
            porcentajeOtrasSemillas.put(especie, totalOtrasSemillas);
            porcentajeCumpleEstandar.put(especie, porcentajeCumple);
        }

        return new ReportePurezaDTO(
            contaminantesPorEspecie,
            porcentajeMalezas,
            porcentajeOtrasSemillas,
            porcentajeCumpleEstandar,
            (long) purezas.size()
        );
    }

    public ReporteTetrazolioDTO obtenerReporteTetrazolio(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = fechaFin != null ? fechaFin.atTime(23, 59, 59) : null;

        List<Tetrazolio> tetrazolios = obtenerAnalisisPorFecha(tetrazolioRepository.findAll(), inicio, fin);

        // Viabilidad INIA (porcViablesRedondeo)
        Map<String, Double> viabilidadIniaPorEspecie = tetrazolios.stream()
            .filter(t -> t.getLote() != null && t.getLote().getCultivar() != null && t.getLote().getCultivar().getEspecie() != null)
            .filter(t -> t.getPorcViablesRedondeo() != null)
            .collect(Collectors.groupingBy(
                t -> t.getLote().getCultivar().getEspecie().getNombreComun(),
                Collectors.averagingDouble(t -> t.getPorcViablesRedondeo().doubleValue())
            ));

        // Viabilidad INASE (viabilidadInase)
        Map<String, Double> viabilidadInasePorEspecie = tetrazolios.stream()
            .filter(t -> t.getLote() != null && t.getLote().getCultivar() != null && t.getLote().getCultivar().getEspecie() != null)
            .filter(t -> t.getViabilidadInase() != null)
            .collect(Collectors.groupingBy(
                t -> t.getLote().getCultivar().getEspecie().getNombreComun(),
                Collectors.averagingDouble(t -> t.getViabilidadInase().doubleValue())
            ));

        return new ReporteTetrazolioDTO(
            viabilidadIniaPorEspecie,
            viabilidadInasePorEspecie,
            (long) tetrazolios.size()
        );
    }

    private <T extends Analisis> List<T> obtenerAnalisisPorFecha(List<T> analisis, LocalDateTime inicio, LocalDateTime fin) {
        return analisis.stream()
            .filter(a -> a.getActivo() != null && a.getActivo())
            .filter(a -> {
                if (inicio != null && a.getFechaInicio() != null && a.getFechaInicio().isBefore(inicio)) {
                    return false;
                }
                if (fin != null && a.getFechaInicio() != null && a.getFechaInicio().isAfter(fin)) {
                    return false;
                }
                return true;
            })
            .collect(Collectors.toList());
    }

    private Map<String, Long> calcularAnalisisPorPeriodo(List<Analisis> analisis) {
        Map<String, Long> resultado = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        analisis.stream()
            .filter(a -> a.getFechaInicio() != null)
            .collect(Collectors.groupingBy(
                a -> a.getFechaInicio().format(formatter),
                Collectors.counting()
            ))
            .entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> resultado.put(e.getKey(), e.getValue()));

        return resultado;
    }

    private Map<String, Long> calcularAnalisisPorEstado(List<Analisis> analisis) {
        return analisis.stream()
            .filter(a -> a.getEstado() != null)
            .collect(Collectors.groupingBy(
                a -> a.getEstado().name(),
                Collectors.counting()
            ));
    }

    private Map<String, Double> calcularPorcentajeCompletitud(Map<String, Long> analisisPorEstado, Long total) {
        Map<String, Double> resultado = new HashMap<>();
        if (total == 0) return resultado;

        for (Map.Entry<String, Long> entry : analisisPorEstado.entrySet()) {
            double porcentaje = (entry.getValue() * 100.0) / total;
            resultado.put(entry.getKey(), Math.round(porcentaje * 100.0) / 100.0);
        }

        return resultado;
    }

    private Double calcularTiempoMedioFinalizacion(List<Analisis> analisis) {
        List<Long> duraciones = analisis.stream()
            .filter(a -> a.getFechaInicio() != null && a.getFechaFin() != null)
            .map(a -> Duration.between(a.getFechaInicio(), a.getFechaFin()).toDays())
            .collect(Collectors.toList());

        if (duraciones.isEmpty()) return 0.0;

        return duraciones.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
    }

    private Map<String, Long> calcularTopAnalisisProblemas(LocalDateTime inicio, LocalDateTime fin) {
        Map<String, Long> problemas = new HashMap<>();

        long germinacionProblemas = obtenerAnalisisPorFecha(germinacionRepository.findAll(), inicio, fin).stream()
            .filter(g -> g.getEstado() == Estado.A_REPETIR)
            .count();

        long purezaProblemas = obtenerAnalisisPorFecha(purezaRepository.findAll(), inicio, fin).stream()
            .filter(p -> p.getEstado() == Estado.A_REPETIR)
            .count();

        long pmsProblemas = obtenerAnalisisPorFecha(pmsRepository.findAll(), inicio, fin).stream()
            .filter(p -> p.getEstado() == Estado.A_REPETIR)
            .count();

        long tetrazolioProblemas = obtenerAnalisisPorFecha(tetrazolioRepository.findAll(), inicio, fin).stream()
            .filter(t -> t.getEstado() == Estado.A_REPETIR)
            .count();

        long dosnProblemas = obtenerAnalisisPorFecha(dosnRepository.findAll(), inicio, fin).stream()
            .filter(d -> d.getEstado() == Estado.A_REPETIR)
            .count();

        problemas.put("GERMINACION", germinacionProblemas);
        problemas.put("PUREZA", purezaProblemas);
        problemas.put("PMS", pmsProblemas);
        problemas.put("TETRAZOLIO", tetrazolioProblemas);
        problemas.put("DOSN", dosnProblemas);

        return problemas.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    private Map<String, Double> calcularMediaGerminacionPorEspecie(List<Germinacion> germinaciones) {
        return germinaciones.stream()
            .filter(g -> g.getLote() != null && g.getLote().getCultivar() != null && g.getLote().getCultivar().getEspecie() != null)
            .filter(g -> g.getTablaGerm() != null && !g.getTablaGerm().isEmpty())
            .flatMap(g -> g.getTablaGerm().stream()
                .filter(t -> t.getPorcentajeNormalesConRedondeo() != null)
                .map(t -> new Object() {
                    String especie = g.getLote().getCultivar().getEspecie().getNombreComun();
                    Double porcentaje = t.getPorcentajeNormalesConRedondeo().doubleValue();
                }))
            .collect(Collectors.groupingBy(
                obj -> obj.especie,
                Collectors.averagingDouble(obj -> obj.porcentaje)
            ));
    }

    private Map<String, Double> calcularTiempoPromedioPrimerConteo(List<Germinacion> germinaciones) {
        return germinaciones.stream()
            .filter(g -> g.getLote() != null && g.getLote().getCultivar() != null && g.getLote().getCultivar().getEspecie() != null)
            .filter(g -> g.getTablaGerm() != null && !g.getTablaGerm().isEmpty())
            .flatMap(g -> g.getTablaGerm().stream()
                .filter(t -> t.getFechaConteos() != null && !t.getFechaConteos().isEmpty() && t.getFechaGerminacion() != null)
                .map(t -> new Object() {
                    String especie = g.getLote().getCultivar().getEspecie().getNombreComun();
                    Double dias = (double) java.time.temporal.ChronoUnit.DAYS.between(t.getFechaGerminacion(), t.getFechaConteos().get(0));
                }))
            .collect(Collectors.groupingBy(
                obj -> obj.especie,
                Collectors.averagingDouble(obj -> obj.dias)
            ));
    }

    private Map<String, Double> calcularTiempoPromedioUltimoConteo(List<Germinacion> germinaciones) {
        return germinaciones.stream()
            .filter(g -> g.getLote() != null && g.getLote().getCultivar() != null && g.getLote().getCultivar().getEspecie() != null)
            .filter(g -> g.getTablaGerm() != null && !g.getTablaGerm().isEmpty())
            .flatMap(g -> g.getTablaGerm().stream()
                .filter(t -> t.getFechaUltConteo() != null && t.getFechaGerminacion() != null)
                .map(t -> new Object() {
                    String especie = g.getLote().getCultivar().getEspecie().getNombreComun();
                    Double dias = (double) java.time.temporal.ChronoUnit.DAYS.between(t.getFechaGerminacion(), t.getFechaUltConteo());
                }))
            .collect(Collectors.groupingBy(
                obj -> obj.especie,
                Collectors.averagingDouble(obj -> obj.dias)
            ));
    }
    
    public ReportePurezaDTO obtenerReporteDosn(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = fechaFin != null ? fechaFin.atTime(23, 59, 59) : null;

        List<Dosn> dosns = obtenerAnalisisPorFecha(dosnRepository.findAll(), inicio, fin);

        Map<String, Long> contaminantesPorEspecie = new HashMap<>();
        Map<String, Double> porcentajeMalezas = new HashMap<>();
        Map<String, Double> porcentajeOtrasSemillas = new HashMap<>();
        Map<String, Double> porcentajeCumpleEstandar = new HashMap<>();

        Map<String, List<Dosn>> dosnsPorEspecie = dosns.stream()
            .filter(d -> d.getLote() != null && d.getLote().getCultivar() != null && d.getLote().getCultivar().getEspecie() != null)
            .collect(Collectors.groupingBy(
                d -> d.getLote().getCultivar().getEspecie().getNombreComun(),
                Collectors.toList()
            ));

        for (Map.Entry<String, List<Dosn>> entry : dosnsPorEspecie.entrySet()) {
            String especie = entry.getKey();
            List<Dosn> dosnsEspecie = entry.getValue();

            long cumpleEstandar = dosnsEspecie.stream()
                .filter(d -> d.getCumpleEstandar() != null && d.getCumpleEstandar())
                .count();

            double porcentajeCumple = dosnsEspecie.size() > 0 ? (cumpleEstandar * 100.0 / dosnsEspecie.size()) : 0.0;

            contaminantesPorEspecie.put(especie, (long) dosnsEspecie.size());
            porcentajeMalezas.put(especie, 0.0); 
            porcentajeOtrasSemillas.put(especie, 0.0); 
            porcentajeCumpleEstandar.put(especie, porcentajeCumple);
        }

        return new ReportePurezaDTO(
            contaminantesPorEspecie,
            porcentajeMalezas,
            porcentajeOtrasSemillas,
            porcentajeCumpleEstandar,
            (long) dosns.size()
        );
    }

    public Map<String, Double> obtenerContaminantesPorEspeciePureza(String especie, LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = fechaFin != null ? fechaFin.atTime(23, 59, 59) : null;

        List<Pureza> purezas = obtenerAnalisisPorFecha(purezaRepository.findAll(), inicio, fin).stream()
            .filter(p -> p.getLote() != null && p.getLote().getCultivar() != null && p.getLote().getCultivar().getEspecie() != null)
            .filter(p -> p.getLote().getCultivar().getEspecie().getNombreComun().equals(especie))
            .collect(Collectors.toList());

        Map<String, Double> contaminantes = new HashMap<>();
        
        // Agregar detalle de malezas específicas desde listados (catálogo)
        Map<String, Long> malezasDetalle = purezas.stream()
            .filter(p -> p.getListados() != null && !p.getListados().isEmpty())
            .flatMap(p -> p.getListados().stream())
            .filter(l -> l.getCatalogo() != null && l.getCatalogo().getNombreComun() != null)
            .collect(Collectors.groupingBy(
                l -> l.getCatalogo().getNombreComun(),
                Collectors.counting()
            ));
        
        // Agregar detalle de otros cultivos específicos desde listados (especie)
        Map<String, Long> cultivosDetalle = purezas.stream()
            .filter(p -> p.getListados() != null && !p.getListados().isEmpty())
            .flatMap(p -> p.getListados().stream())
            .filter(l -> l.getEspecie() != null && l.getEspecie().getNombreComun() != null)
            .collect(Collectors.groupingBy(
                l -> l.getEspecie().getNombreComun(),
                Collectors.counting()
            ));

        // Calcular totales sumando los detalles
        long totalMalezas = malezasDetalle.values().stream().mapToLong(Long::longValue).sum();
        long totalOtrosCultivos = cultivosDetalle.values().stream().mapToLong(Long::longValue).sum();
        
        if (totalMalezas > 0) {
            contaminantes.put("Total Malezas", (double) totalMalezas);
        }
        
        if (totalOtrosCultivos > 0) {
            contaminantes.put("Total Otros Cultivos", (double) totalOtrosCultivos);
        }

        // Agregar malezas individuales
        malezasDetalle.forEach((nombre, cantidad) -> {
            contaminantes.put("Maleza: " + nombre, (double) cantidad);
        });
        
        // Agregar cultivos individuales
        cultivosDetalle.forEach((nombre, cantidad) -> {
            contaminantes.put("Cultivo: " + nombre, (double) cantidad);
        });

        return contaminantes;
    }

    public Map<String, Double> obtenerContaminantesPorEspecieDosn(String especie, LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = fechaFin != null ? fechaFin.atTime(23, 59, 59) : null;

        List<Dosn> dosns = obtenerAnalisisPorFecha(dosnRepository.findAll(), inicio, fin).stream()
            .filter(d -> d.getLote() != null && d.getLote().getCultivar() != null && d.getLote().getCultivar().getEspecie() != null)
            .filter(d -> d.getLote().getCultivar().getEspecie().getNombreComun().equals(especie))
            .collect(Collectors.toList());

        Map<String, Double> contaminantes = new HashMap<>();
        
        // Agregar detalle de malezas específicas desde listados (catálogo)
        Map<String, Long> malezasDetalle = dosns.stream()
            .filter(d -> d.getListados() != null && !d.getListados().isEmpty())
            .flatMap(d -> d.getListados().stream())
            .filter(l -> l.getCatalogo() != null && l.getCatalogo().getNombreComun() != null)
            .collect(Collectors.groupingBy(
                l -> l.getCatalogo().getNombreComun(),
                Collectors.counting()
            ));
        
        // Agregar detalle de otros cultivos específicos desde listados (especie)
        Map<String, Long> cultivosDetalle = dosns.stream()
            .filter(d -> d.getListados() != null && !d.getListados().isEmpty())
            .flatMap(d -> d.getListados().stream())
            .filter(l -> l.getEspecie() != null && l.getEspecie().getNombreComun() != null)
            .collect(Collectors.groupingBy(
                l -> l.getEspecie().getNombreComun(),
                Collectors.counting()
            ));

        // Calcular totales sumando los detalles
        long totalMalezas = malezasDetalle.values().stream().mapToLong(Long::longValue).sum();
        long totalOtrosCultivos = cultivosDetalle.values().stream().mapToLong(Long::longValue).sum();
        
        // Contar registros de cuscuta
        long totalCuscuta = dosns.stream()
            .filter(d -> d.getCuscutaRegistros() != null)
            .flatMap(d -> d.getCuscutaRegistros().stream())
            .count();
        
        if (totalMalezas > 0) {
            contaminantes.put("Total Malezas", (double) totalMalezas);
        }
        
        if (totalOtrosCultivos > 0) {
            contaminantes.put("Total Otros Cultivos", (double) totalOtrosCultivos);
        }
        
        if (totalCuscuta > 0) {
            contaminantes.put("Total Registros de Cuscuta", (double) totalCuscuta);
        }

        // Agregar malezas individuales
        malezasDetalle.forEach((nombre, cantidad) -> {
            contaminantes.put("Maleza: " + nombre, (double) cantidad);
        });
        
        // Agregar cultivos individuales
        cultivosDetalle.forEach((nombre, cantidad) -> {
            contaminantes.put("Cultivo: " + nombre, (double) cantidad);
        });

        return contaminantes;
    }
}
