package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.*;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.*;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.DatosExportacionExcelDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ExportacionRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoListado;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExportacionExcelService {

    @Autowired
    private LoteRepository loteRepository;
    
    @Autowired
    private PurezaRepository purezaRepository;
    
    @Autowired
    private GerminacionRepository germinacionRepository;
    
    @Autowired
    private PmsRepository pmsRepository;
    
    @Autowired
    private TetrazolioRepository tetrazolioRepository;
    
    @Autowired
    private DosnRepository dosnRepository;

    
    public byte[] generarReporteExcel(List<Long> loteIds) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Análisis de Semillas");
        
        
        CellStyle headerStyle = crearEstiloEncabezado(workbook);
        CellStyle subHeaderStyle = crearEstiloSubEncabezado(workbook);
        CellStyle dataStyle = crearEstiloData(workbook);
        CellStyle yellowHeaderStyle = crearEstiloEncabezadoAmarillo(workbook);
        
        
        crearEncabezados(sheet, headerStyle, subHeaderStyle, yellowHeaderStyle);
        
        
        List<DatosExportacionExcelDTO> datos = obtenerDatosParaExportacion(loteIds);
        int filaActual = 2; 
        
        for (DatosExportacionExcelDTO dato : datos) {
            crearFilaDatos(sheet, filaActual, dato, dataStyle);
            filaActual++;
        }
        
        
        ajustarAnchoColumnas(sheet);
        
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }

    
    public byte[] generarReporteExcelAvanzado(ExportacionRequestDTO solicitud) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Análisis de Semillas");
        
        
        CellStyle headerStyle = crearEstiloEncabezado(workbook);
        CellStyle subHeaderStyle = crearEstiloSubEncabezado(workbook);
        CellStyle dataStyle = crearEstiloData(workbook);
        CellStyle yellowHeaderStyle = crearEstiloEncabezadoAmarillo(workbook);
        
        
        if (solicitud.getIncluirEncabezados()) {
            crearEncabezados(sheet, headerStyle, subHeaderStyle, yellowHeaderStyle);
        }
        
        
        List<DatosExportacionExcelDTO> datos = obtenerDatosConFiltros(solicitud);
        int filaActual = solicitud.getIncluirEncabezados() ? 2 : 0;
        
        for (DatosExportacionExcelDTO dato : datos) {
            crearFilaDatos(sheet, filaActual, dato, dataStyle);
            filaActual++;
        }
        
        
        ajustarAnchoColumnas(sheet);
        
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }

    private void crearEncabezados(Sheet sheet, CellStyle headerStyle, CellStyle subHeaderStyle, CellStyle yellowHeaderStyle) {
        
        Row row0 = sheet.createRow(0);
        
        
        for (int i = 0; i < 9; i++) {
            Cell cell = row0.createCell(i);
            cell.setCellValue("");
            cell.setCellStyle(headerStyle);
        }
        
        
        Cell purezaIniaCell = row0.createCell(9);
        purezaIniaCell.setCellValue("Pureza INIA");
        purezaIniaCell.setCellStyle(headerStyle);
        
        
        Cell purezaInaseCell = row0.createCell(15);
        purezaInaseCell.setCellValue("Pureza INASE");
        purezaInaseCell.setCellStyle(yellowHeaderStyle);
        
        
        Cell descripcionCell = row0.createCell(21);
        descripcionCell.setCellValue("Descripción");
        descripcionCell.setCellStyle(headerStyle);
        
        
        Cell dosnCell = row0.createCell(25);
        dosnCell.setCellValue("DOSN");
        dosnCell.setCellStyle(headerStyle);
        
        
        Cell dosniCell = row0.createCell(30);
        dosniCell.setCellValue("DOSN-I");
        dosniCell.setCellStyle(yellowHeaderStyle);
        
        
        Cell pmsCell = row0.createCell(35);
        pmsCell.setCellValue("PMS");
        pmsCell.setCellStyle(headerStyle);
        
        
        Cell fechaCell = row0.createCell(36);
        fechaCell.setCellValue("Fecha Análisis");
        fechaCell.setCellStyle(headerStyle);
        
        
        Cell tsCell = row0.createCell(37);
        tsCell.setCellValue("TS");
        tsCell.setCellStyle(headerStyle);
        
        
        Cell germinacionCell = row0.createCell(38);
        germinacionCell.setCellValue("Germinación");
        germinacionCell.setCellStyle(headerStyle);
        
        
        Cell germinacionICell = row0.createCell(44);
        germinacionICell.setCellValue("Germinación -I");
        germinacionICell.setCellStyle(yellowHeaderStyle);
        
        
        Cell vCell = row0.createCell(50);
        vCell.setCellValue("V%");
        vCell.setCellStyle(headerStyle);
        
        
        Cell viCell = row0.createCell(51);
        viCell.setCellValue("V-I%");
        viCell.setCellStyle(yellowHeaderStyle);
        
        
        Row row1 = sheet.createRow(1);
        String[] subEncabezados = {
            
            "Especie", "Variedad", "Ficha", "Deposito", "N° de articulo", "N° análisis", "Lote", "Kilos", "H%",
            
            "SP%", "MI%", "OC%", "M%", "MT.%", "M.T.C%",
            
            "SP-I%", "MI-I%", "OC-I%", "M%", "M.T-I%", "M.T.C%",
            
            "MI", "OC", "MT", "MTC",
            
            "MTC", "OC", "M", "MT", "DB",
            
            "MTC", "OC", "M", "MT", "DB",
            
            "",
            
            "",
            
            "",
            
            "PN%", "AN%", "D%", "F%", "M%", "G%",
            
            "PN-I%", "AN-I%", "D-I%", "F-I%", "M-I%", "G-I%",
            
            "", ""
        };
        
        for (int i = 0; i < subEncabezados.length; i++) {
            Cell cell = row1.createCell(i);
            cell.setCellValue(subEncabezados[i]);
            cell.setCellStyle(subHeaderStyle);
        }
        
        
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 9, 14));   
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 15, 20));  
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 21, 24));  
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 25, 29));  
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 30, 34));  
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 38, 43));  
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 44, 49));  
    }

    private void crearFilaDatos(Sheet sheet, int numeroFila, DatosExportacionExcelDTO datos, CellStyle style) {
        Row row = sheet.createRow(numeroFila);
        int col = 0;
        
        
        crearCelda(row, col++, datos.getEspecie(), style);
        crearCelda(row, col++, datos.getVariedad(), style);
        crearCelda(row, col++, datos.getLote(), style);
        crearCelda(row, col++, datos.getDeposito(), style);
        crearCelda(row, col++, datos.getNumeroArticulo(), style);
        crearCelda(row, col++, datos.getNumeroAnalisis(), style);
        crearCelda(row, col++, datos.getNombreLote(), style);  
        crearCelda(row, col++, datos.getKilos(), style);
        crearCelda(row, col++, datos.getHumedad(), style);
        
        
        crearCelda(row, col++, datos.getPurezaSemillaPura(), style);
        crearCelda(row, col++, datos.getPurezaMateriaInerte(), style);
        crearCelda(row, col++, datos.getPurezaOtrosCultivos(), style);
        crearCelda(row, col++, datos.getPurezaMalezas(), style);
        crearCelda(row, col++, datos.getPurezaMalezasToleradas(), style);
        crearCelda(row, col++, datos.getPurezaMalezasToleranciaC(), style);
        
        
        crearCelda(row, col++, datos.getPurezaInaseSemillaPura(), style);
        crearCelda(row, col++, datos.getPurezaInaseMateriaInerte(), style);
        crearCelda(row, col++, datos.getPurezaInaseOtrosCultivos(), style);
        crearCelda(row, col++, datos.getPurezaInaseMalezas(), style);
        crearCelda(row, col++, datos.getPurezaInaseMalezasToleradas(), style);
        crearCelda(row, col++, datos.getPurezaInaseMalezasToleranciaC(), style);
        
        
        crearCelda(row, col++, datos.getDescripcionMalezas(), style);
        crearCelda(row, col++, datos.getDescripcionOtrosCultivos(), style);
        crearCelda(row, col++, datos.getDescripcionMalezasToleradas(), style);
        crearCelda(row, col++, datos.getDescripcionMalezasToleranciaC(), style);
        
        
        crearCelda(row, col++, datos.getDosnMalezasToleranciaC(), style);  
        crearCelda(row, col++, datos.getDosnOtrosCultivos(), style);        
        crearCelda(row, col++, datos.getDosnMalezas(), style);              
        crearCelda(row, col++, datos.getDosnMalezasToleradas(), style);     
        crearCelda(row, col++, datos.getDosnBrassica(), style);             
        
        
        crearCelda(row, col++, datos.getDosnInaseMalezasToleranciaC(), style);  
        crearCelda(row, col++, datos.getDosnInaseOtrosCultivos(), style);        
        crearCelda(row, col++, datos.getDosnInaseMalezas(), style);              
        crearCelda(row, col++, datos.getDosnInaseMalezasToleradas(), style);     
        crearCelda(row, col++, datos.getDosnInaseBrassica(), style);             
        
        
        crearCelda(row, col++, datos.getPms(), style);
        
        
        crearCelda(row, col++, datos.getFechaAnalisis() != null ? datos.getFechaAnalisis().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "", style);
        
        
        crearCelda(row, col++, datos.getTratamientoSemillas(), style);
        
        
        crearCelda(row, col++, datos.getGerminacionPlantulasNormales(), style);
        crearCelda(row, col++, datos.getGerminacionPlantulasAnormales(), style);
        crearCelda(row, col++, datos.getGerminacionSemillasDeterioras(), style);
        crearCelda(row, col++, datos.getGerminacionSemillasFrescas(), style);
        crearCelda(row, col++, datos.getGerminacionSemillasMuertas(), style);
        crearCelda(row, col++, datos.getGerminacionTotal(), style);
        
        
        crearCelda(row, col++, datos.getGerminacionInasePlantulasNormales(), style);
        crearCelda(row, col++, datos.getGerminacionInasePlantulasAnormales(), style);
        crearCelda(row, col++, datos.getGerminacionInaseSemillasDeterioras(), style);
        crearCelda(row, col++, datos.getGerminacionInaseSemillasFrescas(), style);
        crearCelda(row, col++, datos.getGerminacionInaseSemillasMuertas(), style);
        crearCelda(row, col++, datos.getGerminacionInaseTotal(), style);
        
        
        crearCelda(row, col++, datos.getViabilidadPorcentaje(), style);
        crearCelda(row, col++, datos.getViabilidadInasePorcentaje(), style);
    }

    private void crearCelda(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        
        if (value != null) {
            if (value instanceof String) {
                cell.setCellValue((String) value);
            } else if (value instanceof BigDecimal) {
                cell.setCellValue(((BigDecimal) value).doubleValue());
            } else if (value instanceof Integer) {
                cell.setCellValue((Integer) value);
            } else if (value instanceof Double) {
                cell.setCellValue((Double) value);
            } else {
                cell.setCellValue(value.toString());
            }
        }
        
        cell.setCellStyle(style);
    }

    private String combinarDescripciones(String descripcionInia, String descripcionInase) {
        boolean tieneInia = descripcionInia != null && !descripcionInia.trim().isEmpty();
        boolean tieneInase = descripcionInase != null && !descripcionInase.trim().isEmpty();
        
        if (tieneInia && tieneInase) {
            return descripcionInia + " | " + descripcionInase;
        } else if (tieneInia) {
            return descripcionInia;
        } else if (tieneInase) {
            return descripcionInase;
        } else {
            return "";
        }
    }

    private List<DatosExportacionExcelDTO> obtenerDatosParaExportacion(List<Long> loteIds) {
        List<DatosExportacionExcelDTO> datos = new ArrayList<>();
        
        List<Lote> lotes;
        if (loteIds == null || loteIds.isEmpty()) {
            lotes = loteRepository.findByActivoTrue();
        } else {
            lotes = loteRepository.findAllById(loteIds);
        }
        
        System.out.println("Exportando datos para " + lotes.size() + " lotes");
        
        for (Lote lote : lotes) {
            System.out.println("Procesando lote ID: " + lote.getLoteID() + ", Ficha: " + lote.getFicha());
            
            DatosExportacionExcelDTO dto = new DatosExportacionExcelDTO();
            
            
            mapearDatosBasicosLote(dto, lote);
            System.out.println("Datos básicos mapeados - Especie: " + dto.getEspecie() + ", Variedad: " + dto.getVariedad());
            
            
            mapearDatosPureza(dto, lote);
            System.out.println("Datos pureza mapeados - SP: " + dto.getPurezaSemillaPura() + ", MI: " + dto.getPurezaMateriaInerte());
            
            
            mapearDatosGerminacion(dto, lote);
            System.out.println("Datos germinación mapeados - PN: " + dto.getGerminacionPlantulasNormales());
            
            
            mapearDatosPms(dto, lote);
            System.out.println("Datos PMS mapeados - PMS: " + dto.getPms());
            
            
            mapearDatosTetrazolio(dto, lote);
            System.out.println("Datos tetrazolio mapeados - V%: " + dto.getViabilidadPorcentaje());
            
            
            mapearDatosDosn(dto, lote);
            System.out.println("Datos DOSN mapeados");
            
            datos.add(dto);
        }
        
        return datos;
    }

    private List<DatosExportacionExcelDTO> obtenerDatosConFiltros(ExportacionRequestDTO solicitud) {
        List<DatosExportacionExcelDTO> datos = new ArrayList<>();
        
        List<Lote> lotes;
        
        
        if (solicitud.getLoteIds() != null && !solicitud.getLoteIds().isEmpty()) {
            lotes = loteRepository.findAllById(solicitud.getLoteIds());
        } else {
            
            if (solicitud.getIncluirInactivos()) {
                lotes = loteRepository.findAll();
            } else {
                lotes = loteRepository.findByActivoTrue();
            }
        }
        
        
        if (solicitud.getFechaDesde() != null || solicitud.getFechaHasta() != null) {
            lotes = lotes.stream()
                .filter(lote -> {
                    LocalDate fechaLote = lote.getFechaRecibo(); 
                    if (fechaLote == null) return false;
                    
                    boolean cumpleFechaDesde = solicitud.getFechaDesde() == null || 
                        !fechaLote.isBefore(solicitud.getFechaDesde());
                    boolean cumpleFechaHasta = solicitud.getFechaHasta() == null || 
                        !fechaLote.isAfter(solicitud.getFechaHasta());
                    
                    return cumpleFechaDesde && cumpleFechaHasta;
                })
                .collect(Collectors.toList());
        }
        
        
        if (solicitud.getEspecieIds() != null && !solicitud.getEspecieIds().isEmpty()) {
            lotes = lotes.stream()
                .filter(lote -> lote.getCultivar() != null && 
                               lote.getCultivar().getEspecie() != null &&
                               solicitud.getEspecieIds().contains(lote.getCultivar().getEspecie().getEspecieID()))
                .collect(Collectors.toList());
        }
        
        
        if (solicitud.getCultivarIds() != null && !solicitud.getCultivarIds().isEmpty()) {
            lotes = lotes.stream()
                .filter(lote -> lote.getCultivar() != null &&
                               solicitud.getCultivarIds().contains(lote.getCultivar().getCultivarID()))
                .collect(Collectors.toList());
        }
        
        
        for (Lote lote : lotes) {
            DatosExportacionExcelDTO dto = new DatosExportacionExcelDTO();
            
            
            mapearDatosBasicosLote(dto, lote);
            
            
            List<String> tiposAnalisis = solicitud.getTiposAnalisis();
            if (tiposAnalisis == null || tiposAnalisis.isEmpty() || tiposAnalisis.contains("PUREZA")) {
                mapearDatosPureza(dto, lote);
            }
            if (tiposAnalisis == null || tiposAnalisis.isEmpty() || tiposAnalisis.contains("GERMINACION")) {
                mapearDatosGerminacion(dto, lote);
            }
            if (tiposAnalisis == null || tiposAnalisis.isEmpty() || tiposAnalisis.contains("PMS")) {
                mapearDatosPms(dto, lote);
            }
            if (tiposAnalisis == null || tiposAnalisis.isEmpty() || tiposAnalisis.contains("TETRAZOLIO")) {
                mapearDatosTetrazolio(dto, lote);
            }
            if (tiposAnalisis == null || tiposAnalisis.isEmpty() || tiposAnalisis.contains("DOSN")) {
                mapearDatosDosn(dto, lote);
            }
            
            datos.add(dto);
        }
        
        return datos;
    }

    private void mapearDatosBasicosLote(DatosExportacionExcelDTO dto, Lote lote) {
        
        if (lote.getCultivar() != null) {
            dto.setVariedad(lote.getCultivar().getNombre());
            if (lote.getCultivar().getEspecie() != null) {
                dto.setEspecie(lote.getCultivar().getEspecie().getNombreComun());
            }
        }
        
        
        dto.setLote(lote.getFicha());
        dto.setNombreLote(lote.getNomLote());  
        dto.setKilos(lote.getKilosLimpios() != null ? lote.getKilosLimpios().toString() : "");
        
        
        dto.setNumeroAnalisis(lote.getLoteID() != null ? lote.getLoteID().toString() : "");
        
        
        if (lote.getDeposito() != null) {
            dto.setDeposito(lote.getDeposito().getValor());
        }
        
        
        if (lote.getNumeroArticulo() != null) {
            dto.setNumeroArticulo(lote.getNumeroArticulo().getValor());
        }
        
        
        if (lote.getDatosHumedad() != null && !lote.getDatosHumedad().isEmpty()) {
            dto.setHumedad(lote.getDatosHumedad().get(0).getValor());
        }
    }

    private void mapearDatosPureza(DatosExportacionExcelDTO dto, Lote lote) {
        List<Pureza> analisisPureza = purezaRepository.findByLoteLoteID(lote.getLoteID());
        
        System.out.println("Buscando análisis de pureza para lote ID: " + lote.getLoteID());
        System.out.println("Análisis de pureza encontrados: " + analisisPureza.size());
        
        if (!analisisPureza.isEmpty()) {
            Pureza pureza = analisisPureza.get(0); 
            
            System.out.println("Datos INIA - SP: " + pureza.getRedonSemillaPura() + ", MI: " + pureza.getRedonMateriaInerte());
            System.out.println("Datos INASE - SP: " + pureza.getInasePura() + ", MI: " + pureza.getInaseMateriaInerte());
            
            
            dto.setPurezaSemillaPura(pureza.getRedonSemillaPura());
            dto.setPurezaMateriaInerte(pureza.getRedonMateriaInerte());
            dto.setPurezaOtrosCultivos(pureza.getRedonOtrosCultivos());
            dto.setPurezaMalezas(pureza.getRedonMalezas());
            dto.setPurezaMalezasToleradas(pureza.getRedonMalezasToleradas());
            dto.setPurezaMalezasToleranciaC(pureza.getRedonMalezasTolCero());
            
            
            dto.setPurezaInaseSemillaPura(pureza.getInasePura());
            dto.setPurezaInaseMateriaInerte(pureza.getInaseMateriaInerte());
            dto.setPurezaInaseOtrosCultivos(pureza.getInaseOtrosCultivos());
            dto.setPurezaInaseMalezas(pureza.getInaseMalezas());
            dto.setPurezaInaseMalezasToleradas(pureza.getInaseMalezasToleradas());
            dto.setPurezaInaseMalezasToleranciaC(pureza.getInaseMalezasTolCero());
            
            if (pureza.getFecha() != null) {
                dto.setFechaAnalisis(pureza.getFecha());
            }
            
            
            if (pureza.getListados() != null && !pureza.getListados().isEmpty()) {
                System.out.println("Listados encontrados: " + pureza.getListados().size());
                
                
                StringBuilder descripcionMalezas = new StringBuilder();
                StringBuilder descripcionOtrosCultivos = new StringBuilder();
                StringBuilder descripcionMalezasToleradas = new StringBuilder();
                StringBuilder descripcionMalezasToleranciaC = new StringBuilder();
                
                
                StringBuilder descripcionInaseMalezas = new StringBuilder();
                StringBuilder descripcionInaseOtrosCultivos = new StringBuilder();
                StringBuilder descripcionInaseMalezasToleradas = new StringBuilder();
                StringBuilder descripcionInaseMalezasToleranciaC = new StringBuilder();
                
                for (Listado listado : pureza.getListados()) {
                    String nombre = null;
                    boolean esMaleza = false;
                    boolean esCultivo = false;
                    
                    
                    if (listado.getCatalogo() != null) {
                        nombre = listado.getCatalogo().getNombreComun();
                        esMaleza = true;
                    } else if (listado.getEspecie() != null) {
                        nombre = listado.getEspecie().getNombreComun();
                        esCultivo = true;
                    }
                    
                    if (nombre != null) {
                        TipoListado tipoListado = listado.getListadoTipo();
                        Instituto instituto = listado.getListadoInsti();
                        
                        System.out.println("Procesando listado - Nombre: " + nombre + ", Tipo: " + tipoListado + ", Instituto: " + instituto);
                        
                        
                        if (instituto == Instituto.INIA) {
                            
                            if (tipoListado == TipoListado.MAL_TOLERANCIA_CERO) {
                                if (descripcionMalezasToleranciaC.length() > 0) descripcionMalezasToleranciaC.append(", ");
                                descripcionMalezasToleranciaC.append(nombre);
                            } else if (esMaleza) {
                                if (tipoListado == TipoListado.MAL_TOLERANCIA) {
                                    if (descripcionMalezasToleradas.length() > 0) descripcionMalezasToleradas.append(", ");
                                    descripcionMalezasToleradas.append(nombre);
                                } else {
                                    if (descripcionMalezas.length() > 0) descripcionMalezas.append(", ");
                                    descripcionMalezas.append(nombre);
                                }
                            } else if (esCultivo) {
                                if (descripcionOtrosCultivos.length() > 0) descripcionOtrosCultivos.append(", ");
                                descripcionOtrosCultivos.append(nombre);
                            }
                        } else if (instituto == Instituto.INASE) {
                            
                            if (tipoListado == TipoListado.MAL_TOLERANCIA_CERO) {
                                if (descripcionInaseMalezasToleranciaC.length() > 0) descripcionInaseMalezasToleranciaC.append(", ");
                                descripcionInaseMalezasToleranciaC.append(nombre);
                            } else if (esMaleza) {
                                if (tipoListado == TipoListado.MAL_TOLERANCIA) {
                                    if (descripcionInaseMalezasToleradas.length() > 0) descripcionInaseMalezasToleradas.append(", ");
                                    descripcionInaseMalezasToleradas.append(nombre);
                                } else {
                                    if (descripcionInaseMalezas.length() > 0) descripcionInaseMalezas.append(", ");
                                    descripcionInaseMalezas.append(nombre);
                                }
                            } else if (esCultivo) {
                                if (descripcionInaseOtrosCultivos.length() > 0) descripcionInaseOtrosCultivos.append(", ");
                                descripcionInaseOtrosCultivos.append(nombre);
                            }
                        }
                    }
                }
                
                
                
                
                String malezasFinales = combinarDescripciones(descripcionMalezas.toString(), descripcionInaseMalezas.toString());
                String cultivosFinales = combinarDescripciones(descripcionOtrosCultivos.toString(), descripcionInaseOtrosCultivos.toString());
                String toleradasFinales = combinarDescripciones(descripcionMalezasToleradas.toString(), descripcionInaseMalezasToleradas.toString());
                String tolCeroFinales = combinarDescripciones(descripcionMalezasToleranciaC.toString(), descripcionInaseMalezasToleranciaC.toString());
                
                dto.setDescripcionMalezas(malezasFinales);
                dto.setDescripcionOtrosCultivos(cultivosFinales);
                dto.setDescripcionMalezasToleradas(toleradasFinales);
                dto.setDescripcionMalezasToleranciaC(tolCeroFinales);
                
                
                dto.setDescripcionInaseMalezas(descripcionInaseMalezas.toString());
                dto.setDescripcionInaseOtrosCultivos(descripcionInaseOtrosCultivos.toString());
                dto.setDescripcionInaseMalezasToleradas(descripcionInaseMalezasToleradas.toString());
                dto.setDescripcionInaseMalezasToleranciaC(descripcionInaseMalezasToleranciaC.toString());
                
                System.out.println("Descripciones Finales - Malezas: " + malezasFinales + 
                                   ", Toleradas: " + toleradasFinales + 
                                   ", Tol. Cero: " + tolCeroFinales + 
                                   ", Otros cultivos: " + cultivosFinales);
            } else {
                System.out.println("No se encontraron listados para este análisis de pureza");
            }
        } else {
            System.out.println("No se encontraron análisis de pureza para el lote");
        }
    }

    private void mapearDatosGerminacion(DatosExportacionExcelDTO dto, Lote lote) {
        List<Germinacion> analisisGerminacion = germinacionRepository.findByLoteLoteID(lote.getLoteID());
        
        if (!analisisGerminacion.isEmpty()) {
            Germinacion germinacion = analisisGerminacion.get(0);
            
            if (germinacion.getTablaGerm() != null && !germinacion.getTablaGerm().isEmpty()) {
                TablaGerm tabla = germinacion.getTablaGerm().get(0);
                
                
                if (tabla.getValoresGerm() != null) {
                    for (ValoresGerm valores : tabla.getValoresGerm()) {
                        if (valores.getInstituto() == Instituto.INIA) {
                            
                            dto.setGerminacionPlantulasNormales(valores.getNormales());
                            dto.setGerminacionPlantulasAnormales(valores.getAnormales());
                            dto.setGerminacionSemillasDeterioras(valores.getDuras());
                            dto.setGerminacionSemillasFrescas(valores.getFrescas());
                            dto.setGerminacionSemillasMuertas(valores.getMuertas());
                            dto.setGerminacionTotal(valores.getGerminacion());
                        } else if (valores.getInstituto() == Instituto.INASE) {
                            
                            dto.setGerminacionInasePlantulasNormales(valores.getNormales());
                            dto.setGerminacionInasePlantulasAnormales(valores.getAnormales());
                            dto.setGerminacionInaseSemillasDeterioras(valores.getDuras());
                            dto.setGerminacionInaseSemillasFrescas(valores.getFrescas());
                            dto.setGerminacionInaseSemillasMuertas(valores.getMuertas());
                            dto.setGerminacionInaseTotal(valores.getGerminacion());
                        }
                    }
                }
                
                
                if (tabla.getTratamiento() != null) {
                    dto.setTratamientoSemillas(tabla.getTratamiento());
                }
            }
        }
    }

    private void mapearDatosPms(DatosExportacionExcelDTO dto, Lote lote) {
        List<Pms> analisisPms = pmsRepository.findByLoteLoteID(lote.getLoteID());
        
        if (!analisisPms.isEmpty()) {
            Pms pms = analisisPms.get(0);
            dto.setPms(pms.getPmsconRedon());
        }
    }

    private void mapearDatosTetrazolio(DatosExportacionExcelDTO dto, Lote lote) {
        List<Tetrazolio> analisisTetrazolio = tetrazolioRepository.findByLoteLoteID(lote.getLoteID());
        
        if (!analisisTetrazolio.isEmpty()) {
            Tetrazolio tetrazolio = analisisTetrazolio.get(0);
            
            
            dto.setViabilidadPorcentaje(tetrazolio.getPorcViablesRedondeo());
            
            
            dto.setViabilidadInasePorcentaje(tetrazolio.getViabilidadInase());
        }
    }

    private void mapearDatosDosn(DatosExportacionExcelDTO dto, Lote lote) {
        List<Dosn> analisisDosn = dosnRepository.findByLoteLoteID(lote.getLoteID());
        
        if (!analisisDosn.isEmpty()) {
            Dosn dosn = analisisDosn.get(0);
            
            
            if (dosn.getListados() != null) {
                StringBuilder dosnOtrosCultivos = new StringBuilder();
                StringBuilder dosnMalezas = new StringBuilder();
                StringBuilder dosnMalezasToleradas = new StringBuilder();
                StringBuilder dosnMalezasToleranciaC = new StringBuilder();
                StringBuilder dosnBrassica = new StringBuilder();
                
                
                StringBuilder dosnInaseOtrosCultivos = new StringBuilder();
                StringBuilder dosnInaseMalezas = new StringBuilder();
                StringBuilder dosnInaseMalezasToleradas = new StringBuilder();
                StringBuilder dosnInaseMalezasToleranciaC = new StringBuilder();
                StringBuilder dosnInaseBrassica = new StringBuilder();
                
                for (Listado listado : dosn.getListados()) {
                    String nombre = null;
                    boolean esMaleza = false;
                    boolean esCultivo = false;
                    
                    
                    if (listado.getCatalogo() != null) {
                        nombre = listado.getCatalogo().getNombreComun();
                        esMaleza = true;
                    } else if (listado.getEspecie() != null) {
                        nombre = listado.getEspecie().getNombreComun();
                        esCultivo = true;
                    }
                    
                    if (nombre != null) {
                        TipoListado tipoListado = listado.getListadoTipo();
                        Instituto instituto = listado.getListadoInsti();
                        
                        
                        if (instituto == Instituto.INIA) {
                            
                            if (tipoListado == TipoListado.MAL_TOLERANCIA_CERO) {
                                if (dosnMalezasToleranciaC.length() > 0) dosnMalezasToleranciaC.append(", ");
                                dosnMalezasToleranciaC.append(nombre);
                            } else if (tipoListado == TipoListado.BRASSICA) {
                                if (dosnBrassica.length() > 0) dosnBrassica.append(", ");
                                dosnBrassica.append(nombre);
                            } else if (esMaleza) {
                                if (tipoListado == TipoListado.MAL_TOLERANCIA) {
                                    if (dosnMalezasToleradas.length() > 0) dosnMalezasToleradas.append(", ");
                                    dosnMalezasToleradas.append(nombre);
                                } else {
                                    if (dosnMalezas.length() > 0) dosnMalezas.append(", ");
                                    dosnMalezas.append(nombre);
                                }
                            } else if (esCultivo) {
                                if (dosnOtrosCultivos.length() > 0) dosnOtrosCultivos.append(", ");
                                dosnOtrosCultivos.append(nombre);
                            }
                        } else if (instituto == Instituto.INASE) {
                            
                            if (tipoListado == TipoListado.MAL_TOLERANCIA_CERO) {
                                if (dosnInaseMalezasToleranciaC.length() > 0) dosnInaseMalezasToleranciaC.append(", ");
                                dosnInaseMalezasToleranciaC.append(nombre);
                            } else if (tipoListado == TipoListado.BRASSICA) {
                                if (dosnInaseBrassica.length() > 0) dosnInaseBrassica.append(", ");
                                dosnInaseBrassica.append(nombre);
                            } else if (esMaleza) {
                                if (tipoListado == TipoListado.MAL_TOLERANCIA) {
                                    if (dosnInaseMalezasToleradas.length() > 0) dosnInaseMalezasToleradas.append(", ");
                                    dosnInaseMalezasToleradas.append(nombre);
                                } else {
                                    if (dosnInaseMalezas.length() > 0) dosnInaseMalezas.append(", ");
                                    dosnInaseMalezas.append(nombre);
                                }
                            } else if (esCultivo) {
                                if (dosnInaseOtrosCultivos.length() > 0) dosnInaseOtrosCultivos.append(", ");
                                dosnInaseOtrosCultivos.append(nombre);
                            }
                        }
                    }
                }
                
                
                dto.setDosnOtrosCultivos(dosnOtrosCultivos.toString());
                dto.setDosnMalezas(dosnMalezas.toString());
                dto.setDosnMalezasToleradas(dosnMalezasToleradas.toString());
                dto.setDosnMalezasToleranciaC(dosnMalezasToleranciaC.toString());
                dto.setDosnBrassica(dosnBrassica.toString());
                
                
                dto.setDosnInaseOtrosCultivos(dosnInaseOtrosCultivos.toString());
                dto.setDosnInaseMalezas(dosnInaseMalezas.toString());
                dto.setDosnInaseMalezasToleradas(dosnInaseMalezasToleradas.toString());
                dto.setDosnInaseMalezasToleranciaC(dosnInaseMalezasToleranciaC.toString());
                dto.setDosnInaseBrassica(dosnInaseBrassica.toString());
            }
        }
    }
    private CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THICK);
        style.setBorderBottom(BorderStyle.THICK);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.BOTTOM);
        
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        
        return style;
    }

    private CellStyle crearEstiloEncabezadoAmarillo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THICK);
        style.setBorderBottom(BorderStyle.THICK);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.BOTTOM);
        
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        
        return style;
    }

    private CellStyle crearEstiloSubEncabezado(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.BOTTOM);
        
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 8);
        style.setFont(font);
        
        return style;
    }

    private CellStyle crearEstiloData(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.BOTTOM);
        
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        
        return style;
    }

    private void ajustarAnchoColumnas(Sheet sheet) {
        
        for (int i = 0; i < 8; i++) {
            sheet.autoSizeColumn(i);
            
            if (sheet.getColumnWidth(i) < 3000) {
                sheet.setColumnWidth(i, 3000);
            }
        }
        
        
        for (int i = 8; i < 50; i++) {
            sheet.setColumnWidth(i, 2500);
        }
    }
}