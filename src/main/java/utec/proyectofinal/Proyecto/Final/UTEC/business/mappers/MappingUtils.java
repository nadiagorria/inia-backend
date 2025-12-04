package utec.proyectofinal.Proyecto.Final.UTEC.business.mappers;

import java.util.Optional;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Listado;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.MalezasCatalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.MalezasCatalogoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.EspecieRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ListadoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ListadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.MalezasCatalogoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.EspecieDTO;

public class MappingUtils {

    public static MalezasCatalogoDTO toCatalogoDTO(MalezasCatalogo MalezasCatalogo) {
        MalezasCatalogoDTO dto = new MalezasCatalogoDTO();
        dto.setCatalogoID(MalezasCatalogo.getCatalogoID());
        dto.setNombreComun(MalezasCatalogo.getNombreComun());
        dto.setNombreCientifico(MalezasCatalogo.getNombreCientifico());
        
        return dto;
    }

    public static EspecieDTO toEspecieDTO(Especie especie) {
        EspecieDTO dto = new EspecieDTO();
        dto.setEspecieID(especie.getEspecieID());
        dto.setNombreComun(especie.getNombreComun());
        dto.setNombreCientifico(especie.getNombreCientifico());
        dto.setActivo(especie.getActivo());
        
        return dto;
    }

    public static ListadoDTO toListadoDTO(Listado listado) {
        ListadoDTO dto = new ListadoDTO();
        dto.setListadoID(listado.getListadoID());
        dto.setListadoTipo(listado.getListadoTipo());
        dto.setListadoInsti(listado.getListadoInsti());
        dto.setListadoNum(listado.getListadoNum());

        if (listado.getCatalogo() != null) {
            dto.setCatalogo(toCatalogoDTO(listado.getCatalogo()));
        }
        
        if (listado.getEspecie() != null) {
            dto.setEspecie(toEspecieDTO(listado.getEspecie()));
        }
        
        return dto;
    }

    public static Listado fromListadoRequest(ListadoRequestDTO solicitud, MalezasCatalogoRepository catalogoRepository, EspecieRepository especieRepository) {
        Listado listado = new Listado();
        listado.setListadoTipo(solicitud.getListadoTipo());
        listado.setListadoInsti(solicitud.getListadoInsti());
        listado.setListadoNum(solicitud.getListadoNum());

        if (solicitud.getIdCatalogo() != null) {
            Optional<MalezasCatalogo> catalogoOpt = catalogoRepository.findById(solicitud.getIdCatalogo());
            if (catalogoOpt.isPresent()) {
                listado.setCatalogo(catalogoOpt.get());
            } else {
                throw new RuntimeException("Catálogo no encontrado con ID: " + solicitud.getIdCatalogo());
            }
        }
        
        if (solicitud.getIdEspecie() != null) {
            Optional<Especie> especieOpt = especieRepository.findById(solicitud.getIdEspecie());
            if (especieOpt.isPresent()) {
                listado.setEspecie(especieOpt.get());
            } else {
                throw new RuntimeException("Especie no encontrada con ID: " + solicitud.getIdEspecie());
            }
        }
        
        return listado;
    }
}
