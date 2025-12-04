package utec.proyectofinal.Proyecto.Final.UTEC.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Legado;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LegadoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.specifications.LegadoSpecification;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LegadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LegadoListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LegadoSimpleDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LoteDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LegadoService {

    private final LegadoRepository legadoRepository;
    private final LoteService loteService;

    
    @Transactional(readOnly = true)
    public List<LegadoSimpleDTO> obtenerTodosSimple() {
        return legadoRepository.findByActivoTrue().stream()
                .map(this::convertirASimpleDTO)
                .collect(Collectors.toList());
    }

    
    @Transactional(readOnly = true)
    public LegadoDTO obtenerPorId(Long id) {
        Legado legado = legadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Legado no encontrado con ID: " + id));
        
        return convertirADTO(legado);
    }

    
    @Transactional(readOnly = true)
    public List<LegadoSimpleDTO> obtenerPorArchivo(String archivoOrigen) {
        return legadoRepository.findByArchivoOrigen(archivoOrigen).stream()
                .map(this::convertirASimpleDTO)
                .collect(Collectors.toList());
    }

    
    @Transactional(readOnly = true)
    public List<LegadoSimpleDTO> obtenerPorFicha(String ficha) {
        return legadoRepository.findByFicha(ficha).stream()
                .map(this::convertirASimpleDTO)
                .collect(Collectors.toList());
    }

    
    @Transactional
    public void desactivar(Long id) {
        Legado legado = legadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Legado no encontrado con ID: " + id));
        
        legado.setActivo(false);
        legadoRepository.save(legado);
    }

    
    @Transactional(readOnly = true)
    public Page<LegadoListadoDTO> obtenerLegadosPaginadas(
            Pageable pageable,
            String searchTerm,
            String especie,
            LocalDate fechaReciboInicio,
            LocalDate fechaReciboFin) {
        
        Specification<Legado> spec = LegadoSpecification.conFiltros(
            searchTerm, especie, fechaReciboInicio, fechaReciboFin);
        Page<Legado> legadoPage = legadoRepository.findAll(spec, pageable);
        return legadoPage.map(this::convertirAListadoDTO);
    }

    
    @Transactional(readOnly = true)
    public List<String> obtenerEspeciesUnicas() {
        return legadoRepository.findByActivoTrue().stream()
                .map(legado -> legado.getLote() != null && legado.getLote().getCultivar() != null 
                    ? legado.getLote().getCultivar().getNombre() 
                    : null)
                .filter(especie -> especie != null && !especie.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    
    private LegadoListadoDTO convertirAListadoDTO(Legado legado) {
        LegadoListadoDTO dto = new LegadoListadoDTO();
        dto.setLegadoID(legado.getLegadoID());
        
        // Obtener datos del lote
        if (legado.getLote() != null) {
            dto.setFicha(legado.getLote().getFicha());
            dto.setFechaRecibo(legado.getLote().getFechaRecibo());
            
            // Obtener nombre del cultivar (especie)
            if (legado.getLote().getCultivar() != null) {
                dto.setEspecie(legado.getLote().getCultivar().getNombre());
            }
        }
        
        // Datos de germinación y pureza
        dto.setGermC(legado.getGermC());
        dto.setGermSC(legado.getGermSC());
        dto.setPeso1000(legado.getPeso1000());
        dto.setPura(legado.getPura());
        dto.setPuraI(legado.getPuraI());
        
        return dto;
    }

    
    private LegadoSimpleDTO convertirASimpleDTO(Legado legado) {
        LegadoSimpleDTO dto = new LegadoSimpleDTO();
        dto.setLegadoID(legado.getLegadoID());
        dto.setNomLote(legado.getLote() != null ? legado.getLote().getNomLote() : null);
        dto.setFicha(legado.getLote() != null ? legado.getLote().getFicha() : null);
        dto.setCodDoc(legado.getCodDoc());
        dto.setNomDoc(legado.getNomDoc());
        dto.setFamilia(legado.getFamilia());
        dto.setActivo(legado.getActivo());
        return dto;
    }

    
    private LegadoDTO convertirADTO(Legado legado) {
        LegadoDTO dto = new LegadoDTO();
        dto.setLegadoID(legado.getLegadoID());
        
        // Obtener información completa del lote
        if (legado.getLote() != null) {
            LoteDTO loteDTO = loteService.obtenerLotePorId(legado.getLote().getLoteID());
            dto.setLote(loteDTO);
        }
        
        // Datos del documento
        dto.setCodDoc(legado.getCodDoc());
        dto.setNomDoc(legado.getNomDoc());
        dto.setNroDoc(legado.getNroDoc());
        dto.setFechaDoc(legado.getFechaDoc());
        dto.setFamilia(legado.getFamilia());
        
        // Tipo de semilla y tratamiento
        dto.setTipoSemilla(legado.getTipoSemilla());
        dto.setTipoTratGerm(legado.getTipoTratGerm());

        // Datos de germinación
        dto.setGermC(legado.getGermC());
        dto.setGermSC(legado.getGermSC());
        dto.setPeso1000(legado.getPeso1000());
        
        // Datos de pureza
        dto.setPura(legado.getPura());
        dto.setOc(legado.getOc());
        dto.setPorcOC(legado.getPorcOC());
        dto.setMaleza(legado.getMaleza());
        dto.setMalezaTol(legado.getMalezaTol());
        dto.setMatInerte(legado.getMatInerte());
        
        // Datos de pureza inicial
        dto.setPuraI(legado.getPuraI());
        dto.setOcI(legado.getOcI());
        dto.setMalezaI(legado.getMalezaI());
        dto.setMalezaTolI(legado.getMalezaTolI());
        dto.setMatInerteI(legado.getMatInerteI());
        
        // Otros datos
        dto.setPesoHEC(legado.getPesoHEC());
        dto.setNroTrans(legado.getNroTrans());
        dto.setCtaMov(legado.getCtaMov());
        dto.setStk(legado.getStk());
        
        // Fechas adicionales
        dto.setFechaSC_I(legado.getFechaSC_I());
        dto.setFechaC_I(legado.getFechaC_I());
        dto.setGermTotalSC_I(legado.getGermTotalSC_I());
        dto.setGermTotalC_I(legado.getGermTotalC_I());
        
        // Observaciones
        dto.setOtrasSemillasObser(legado.getOtrasSemillasObser());
        dto.setSemillaPura(legado.getSemillaPura());
        dto.setSemillaOtrosCultivos(legado.getSemillaOtrosCultivos());
        dto.setSemillaMalezas(legado.getSemillaMalezas());
        dto.setSemillaMalezasToleradas(legado.getSemillaMalezasToleradas());
        dto.setMateriaInerte(legado.getMateriaInerte());
        
        // Metadatos
        dto.setFechaImportacion(legado.getFechaImportacion());
        dto.setArchivoOrigen(legado.getArchivoOrigen());
        dto.setFilaExcel(legado.getFilaExcel());
        dto.setActivo(legado.getActivo());
        
        return dto;
    }
}
