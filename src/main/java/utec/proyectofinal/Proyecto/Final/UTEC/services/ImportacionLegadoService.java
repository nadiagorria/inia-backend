package utec.proyectofinal.Proyecto.Final.UTEC.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.*;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.*;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ImportacionLegadoResponseDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ImportacionLegadoResponseDTO.ErrorImportacionDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoCatalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoContacto;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servicio para importar datos legados desde archivos Excel
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImportacionLegadoService {

    private final LegadoRepository legadoRepository;
    private final LoteRepository loteRepository;
    private final EspecieRepository especieRepository;
    private final CultivarRepository cultivarRepository;
    private final CatalogoCrudRepository catalogoRepository;
    private final ContactoRepository contactoRepository;

    // Mapeo de columnas del Excel (índice basado en 0)
    private static final int COL_EMPRESA = 0;           // A
    private static final int COL_COD_DOC = 1;           // B
    private static final int COL_NOM_DOC = 2;           // C
    private static final int COL_NRO_DOC = 3;           // D
    private static final int COL_DEPOSITO = 4;          // E
    private static final int COL_FAMILIA = 5;           // F
    private static final int COL_ESPECIE = 6;           // G
    private static final int COL_VARIEDAD = 7;          // H
    private static final int COL_LOTE = 8;              // I
    private static final int COL_TIPO_SEMILLA = 9;     // J
    private static final int COL_CANTIDAD = 10;         // K
    private static final int COL_PRECIO_UNIT = 11;      // L
    private static final int COL_ORIGEN = 12;           // M
    private static final int COL_UNIDAD = 13;           // N
    private static final int COL_NRO_FICHA = 14;        // O
    private static final int COL_FECHA_R = 15;          // P
    private static final int COL_GERM_C = 16;           // Q
    private static final int COL_GERM_SC = 17;          // R
    private static final int COL_PESO_1000 = 18;        // S
    private static final int COL_PURA = 19;             // T
    private static final int COL_OC = 20;               // U
    private static final int COL_PORC_OC = 21;          // V
    private static final int COL_MALEZA = 22;           // W
    private static final int COL_MALEZA_TOL = 23;       // X
    private static final int COL_MAT_INERTE = 24;       // Y
    private static final int COL_PURA_I = 25;           // Z
    private static final int COL_OC_I = 26;             // AA
    private static final int COL_MALEZA_I = 27;         // AB
    private static final int COL_MALEZA_TOL_I = 28;     // AC
    private static final int COL_MAT_INERTE_I = 29;     // AD
    private static final int COL_PESO_HEC = 30;         // AE
    private static final int COL_TRATADA = 31;          // AF
    private static final int COL_NRO_TRANS = 32;        // AG
    private static final int COL_FEC_DOC = 33;          // AH
    private static final int COL_CTA_MOV = 34;          // AI
    private static final int COL_CA_CC = 35;            // AJ
    private static final int COL_FF = 36;               // AK
    private static final int COL_TITULAR = 37;          // AL
    private static final int COL_CTA_ART = 38;          // AM
    private static final int COL_PROVEEDOR = 39;        // AN
    private static final int COL_DOC_AFECT = 40;        // AO
    private static final int COL_NRO_AFECT = 41;        // AP
    private static final int COL_MONEDA = 42;           // AQ
    private static final int COL_IMPORTE_MN = 43;       // AR
    private static final int COL_IMPORTE_MO = 44;       // AS
    private static final int COL_OBS_LOTE = 45;         // AT
    private static final int COL_STK = 46;              // AU
    private static final int COL_REFERENCIA = 47;       // AV
    private static final int COL_TIPO_TRAT_GERM = 48;   // AW
    private static final int COL_FECHA_SC_I = 49;       // AX
    private static final int COL_FECHA_C_I = 50;        // AY
    private static final int COL_GERM_TOTAL_SC_I = 51;  // AZ
    private static final int COL_GERM_TOTAL_C_I = 52;   // BA
    private static final int COL_OBS_TRANS = 53;        // BB
    private static final int COL_OTRAS_SEMILLAS_OBSER = 54; // BC
    private static final int COL_SEMILLA_PURA = 55;     // BD
    private static final int COL_SEMILLA_OTROS_CULTIVOS = 56; // BE
    private static final int COL_SEMILLA_MALEZAS = 57;  // BF
    private static final int COL_SEMILLA_MALEZAS_TOL = 58; // BG
    private static final int COL_MATERIA_INERTE = 59;   // BH

    /**
     * Importa datos desde un archivo Excel
     */
    @Transactional
    public ImportacionLegadoResponseDTO importarDesdeExcel(MultipartFile archivo, boolean soloValidar) {
        ImportacionLegadoResponseDTO response = new ImportacionLegadoResponseDTO();
        response.setExitoso(false);
        response.setTotalFilas(0);
        response.setFilasImportadas(0);
        response.setFilasConErrores(0);
        
        try (Workbook workbook = new XSSFWorkbook(archivo.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            int totalFilas = 0;
            int filasImportadas = 0;
            int filasConErrores = 0;
            
            // Empezar desde la fila 1 (asumiendo que la fila 0 son encabezados)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || esFilaVacia(row)) {
                    continue;
                }
                
                totalFilas++;
                
                try {
                    if (!soloValidar) {
                        procesarFila(row, i, archivo.getOriginalFilename());
                    }
                    filasImportadas++;
                } catch (Exception e) {
                    filasConErrores++;
                    response.getErrores().add(new ErrorImportacionDTO(
                        i + 1, // +1 para mostrar número de fila en Excel (1-based)
                        "General",
                        e.getMessage(),
                        ""
                    ));
                    log.error("Error procesando fila {}: {}", i, e.getMessage(), e);
                }
            }
            
            response.setTotalFilas(totalFilas);
            response.setFilasImportadas(filasImportadas);
            response.setFilasConErrores(filasConErrores);
            response.setExitoso(filasConErrores == 0);
            response.setMensaje(soloValidar ? 
                "Validación completada: " + totalFilas + " filas validadas" :
                "Importación completada: " + filasImportadas + " filas importadas, " + filasConErrores + " con errores"
            );
            
        } catch (IOException e) {
            response.setMensaje("Error al leer el archivo Excel: " + e.getMessage());
            log.error("Error al leer archivo Excel", e);
        }
        
        return response;
    }

    /**
     * Procesa una fila del Excel y crea las entidades necesarias
     */
    private void procesarFila(Row row, int numeroFila, String nombreArchivo) {
        // 1. Extraer y/o crear entidades relacionadas
        Contacto empresa = obtenerOCrearEmpresa(getCellValueAsString(row, COL_EMPRESA));
        Catalogo deposito = obtenerOCrearCatalogo(TipoCatalogo.DEPOSITO, getCellValueAsString(row, COL_DEPOSITO));
        Especie especie = obtenerOCrearEspecie(getCellValueAsString(row, COL_ESPECIE));
        Cultivar cultivar = obtenerOCrearCultivar(getCellValueAsString(row, COL_VARIEDAD), especie);
        Catalogo origen = obtenerOCrearCatalogo(TipoCatalogo.ORIGEN, getCellValueAsString(row, COL_ORIGEN));

        // 2. Crear o actualizar Lote
        Lote lote = crearLote(row, empresa, cultivar, deposito, origen);
        lote = loteRepository.save(lote);

        // 3. Crear registro Legado con el resto de la información
        Legado legado = crearLegado(row, lote, numeroFila, nombreArchivo);
        legadoRepository.save(legado);
    }

    /**
     * Obtiene o crea una empresa (Contacto tipo EMPRESA)
     */
    private Contacto obtenerOCrearEmpresa(String nombreEmpresa) {
        if (nombreEmpresa == null || nombreEmpresa.trim().isEmpty()) {
            return null;
        }
        
        // Buscar si ya existe
        Optional<Contacto> existente = contactoRepository.findByNombreAndTipoAndActivoTrue(
            nombreEmpresa.trim(), TipoContacto.EMPRESA
        );
        
        if (existente.isPresent()) {
            return existente.get();
        }
        
        // Crear nueva empresa
        Contacto empresa = new Contacto();
        empresa.setNombre(nombreEmpresa.trim());
        empresa.setContacto("Importado desde legado"); // Contacto por defecto
        empresa.setTipo(TipoContacto.EMPRESA);
        empresa.setActivo(true);
        
        return contactoRepository.save(empresa);
    }

    /**
     * Obtiene o crea un catálogo
     */
    private Catalogo obtenerOCrearCatalogo(TipoCatalogo tipo, String valorCompleto) {
        if (valorCompleto == null || valorCompleto.trim().isEmpty()) {
            return null;
        }
        
        
        String valor = parsearValorCatalogo(valorCompleto);
        
        // Buscar si ya existe
        Optional<Catalogo> existente = catalogoRepository.findByTipoAndValorAndActivoTrue(tipo, valor);
        
        if (existente.isPresent()) {
            return existente.get();
        }
        
        // Crear nuevo catálogo
        Catalogo catalogo = new Catalogo();
        catalogo.setTipo(tipo);
        catalogo.setValor(valor);
        catalogo.setActivo(true);
        
        return catalogoRepository.save(catalogo);
    }

    /**
     * Obtiene o crea una especie con búsqueda inteligente para evitar duplicados
     */
    private Especie obtenerOCrearEspecie(String nombreCompletoEspecie) {
        if (nombreCompletoEspecie == null || nombreCompletoEspecie.trim().isEmpty()) {
            return null;
        }
        
        
        String nombreComun = parsearValorCatalogo(nombreCompletoEspecie);
        
        // Normalizar el nombre (trim y quitar espacios múltiples)
        nombreComun = nombreComun.trim().replaceAll("\\s+", " ");
        
        // 1. Intentar búsqueda exacta (case-insensitive)
        Optional<Especie> existente = especieRepository.findByNombreComunIgnoreCase(nombreComun);
        if (existente.isPresent()) {
            log.debug("Especie encontrada (búsqueda exacta): {}", nombreComun);
            return existente.get();
        }
        
        // 2. Búsqueda flexible: el nombre de la BD puede contener el nombre buscado
        List<Especie> coincidencias = especieRepository.buscarPorNombreComunInicio(nombreComun);
        if (!coincidencias.isEmpty()) {
            log.debug("Especie encontrada (búsqueda por inicio): {} -> {}", nombreComun, coincidencias.get(0).getNombreComun());
            return coincidencias.get(0);
        }
        
        // 3. Búsqueda aún más flexible: buscar por palabras clave
        
        String nombreSinAcentos = removerAcentos(nombreComun);
        for (Especie especie : especieRepository.findByActivoTrue()) {
            String nombreBD = removerAcentos(especie.getNombreComun());
            
            // Verificar si el nombre buscado está contenido en el nombre de la BD
            if (nombreBD.toLowerCase().contains(nombreSinAcentos.toLowerCase())) {
                log.debug("Especie encontrada (búsqueda flexible): {} -> {}", nombreComun, especie.getNombreComun());
                return especie;
            }
            
            // Verificar si el nombre de la BD está contenido en el nombre buscado
            if (nombreSinAcentos.toLowerCase().contains(nombreBD.toLowerCase())) {
                log.debug("Especie encontrada (búsqueda inversa): {} -> {}", nombreComun, especie.getNombreComun());
                return especie;
            }
        }
        
        // 4. Si no existe, crear nueva especie
        log.info("Creando nueva especie: {}", nombreComun);
        Especie especie = new Especie();
        especie.setNombreComun(nombreComun);
        especie.setNombreCientifico(""); // Vacío por ahora
        especie.setActivo(true);
        
        return especieRepository.save(especie);
    }
    
    /**
     * Remueve acentos de un texto para búsqueda flexible
     */
    private String removerAcentos(String texto) {
        if (texto == null) return "";
        
        String normalized = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    /**
     * Obtiene o crea un cultivar
     */
    private Cultivar obtenerOCrearCultivar(String nombreCompletoCultivar, Especie especie) {
        if (nombreCompletoCultivar == null || nombreCompletoCultivar.trim().isEmpty() || especie == null) {
            return null;
        }
        
        // Parsear el nombre (quitar código)
        String nombre = parsearValorCatalogo(nombreCompletoCultivar);
        
        // Buscar si ya existe para esta especie
        Optional<Cultivar> existente = cultivarRepository.findByNombreAndEspecie_EspecieID(nombre, especie.getEspecieID());
        
        if (existente.isPresent()) {
            return existente.get();
        }
        
        // Crear nuevo cultivar
        Cultivar cultivar = new Cultivar();
        cultivar.setNombre(nombre);
        cultivar.setEspecie(especie);
        cultivar.setActivo(true);
        
        return cultivarRepository.save(cultivar);
    }

    /**
     * Crea un Lote con los datos extraídos
     */
    private Lote crearLote(Row row, Contacto empresa, Cultivar cultivar, 
                           Catalogo deposito, Catalogo origen) {
        
        String ficha = getCellValueAsString(row, COL_NRO_FICHA);
        
        // Verificar si ya existe un lote con esta ficha
        Optional<Lote> existente = loteRepository.findByFicha(ficha);
        if (existente.isPresent()) {
            return existente.get(); // Reutilizar lote existente
        }
        
        Lote lote = new Lote();
        lote.setFicha(ficha);
        lote.setCultivar(cultivar);
        lote.setEmpresa(empresa);
        lote.setDeposito(deposito);
        lote.setOrigen(origen);
        
        // Kilos limpios
        BigDecimal cantidad = getCellValueAsBigDecimal(row, COL_CANTIDAD);
        lote.setKilosLimpios(cantidad);
        
        // Observaciones
        lote.setObservaciones(getCellValueAsString(row, COL_OBS_LOTE));
        
        // Fecha de registro (Fecha R.)
        LocalDate fechaRegistro = getCellValueAsDate(row, COL_FECHA_R);
        lote.setFechaRecibo(fechaRegistro);
        
        lote.setActivo(true);
        
        return lote;
    }

    /**
     * Crea un registro Legado con todos los campos restantes
     */
    private Legado crearLegado(Row row, Lote lote, int numeroFila, String nombreArchivo) {
        Legado legado = new Legado();
        
        legado.setLote(lote);
        
        // Datos del documento
        legado.setCodDoc(getCellValueAsString(row, COL_COD_DOC));
        legado.setNomDoc(getCellValueAsString(row, COL_NOM_DOC));
        legado.setNroDoc(getCellValueAsString(row, COL_NRO_DOC));
        legado.setFechaDoc(getCellValueAsDate(row, COL_FEC_DOC));
        legado.setFamilia(getCellValueAsString(row, COL_FAMILIA));
        
        // Tipo de semilla y tratamiento
        legado.setTipoSemilla(getCellValueAsString(row, COL_TIPO_SEMILLA));
        legado.setTipoTratGerm(getCellValueAsString(row, COL_TIPO_TRAT_GERM));
        

        // Datos de germinación
        legado.setGermC(getCellValueAsInteger(row, COL_GERM_C));
        legado.setGermSC(getCellValueAsInteger(row, COL_GERM_SC));
        legado.setPeso1000(getCellValueAsBigDecimal(row, COL_PESO_1000));
        
        // Datos de pureza
        legado.setPura(getCellValueAsBigDecimal(row, COL_PURA));
        legado.setOc(getCellValueAsBigDecimal(row, COL_OC));
        legado.setPorcOC(getCellValueAsBigDecimal(row, COL_PORC_OC));
        legado.setMaleza(getCellValueAsBigDecimal(row, COL_MALEZA));
        legado.setMalezaTol(getCellValueAsBigDecimal(row, COL_MALEZA_TOL));
        legado.setMatInerte(getCellValueAsBigDecimal(row, COL_MAT_INERTE));
        
        // Datos de pureza I
        legado.setPuraI(getCellValueAsBigDecimal(row, COL_PURA_I));
        legado.setOcI(getCellValueAsBigDecimal(row, COL_OC_I));
        legado.setMalezaI(getCellValueAsBigDecimal(row, COL_MALEZA_I));
        legado.setMalezaTolI(getCellValueAsBigDecimal(row, COL_MALEZA_TOL_I));
        legado.setMatInerteI(getCellValueAsBigDecimal(row, COL_MAT_INERTE_I));
        
        // Peso HEC
        legado.setPesoHEC(getCellValueAsBigDecimal(row, COL_PESO_HEC));
        
        // Datos de transacción
        legado.setNroTrans(getCellValueAsString(row, COL_NRO_TRANS));
        legado.setCtaMov(getCellValueAsString(row, COL_CTA_MOV));
        
        // Stock
        legado.setStk(getCellValueAsBigDecimal(row, COL_STK));
        
        // Fechas adicionales
        legado.setFechaSC_I(getCellValueAsDate(row, COL_FECHA_SC_I));
        legado.setFechaC_I(getCellValueAsDate(row, COL_FECHA_C_I));
        
        // Germinación total
        legado.setGermTotalSC_I(getCellValueAsInteger(row, COL_GERM_TOTAL_SC_I));
        legado.setGermTotalC_I(getCellValueAsInteger(row, COL_GERM_TOTAL_C_I));
        
        // Observaciones
        legado.setOtrasSemillasObser(getCellValueAsString(row, COL_OTRAS_SEMILLAS_OBSER));
        
        // Análisis de semillas
        legado.setSemillaPura(getCellValueAsString(row, COL_SEMILLA_PURA));
        legado.setSemillaOtrosCultivos(getCellValueAsString(row, COL_SEMILLA_OTROS_CULTIVOS));
        legado.setSemillaMalezas(getCellValueAsString(row, COL_SEMILLA_MALEZAS));
        legado.setSemillaMalezasToleradas(getCellValueAsString(row, COL_SEMILLA_MALEZAS_TOL));
        legado.setMateriaInerte(getCellValueAsString(row, COL_MATERIA_INERTE));
        
        // Metadatos
        legado.setFechaImportacion(LocalDate.now());
        legado.setArchivoOrigen(nombreArchivo);
        legado.setFilaExcel(numeroFila);
        legado.setActivo(true);
        
        return legado;
    }

    // MÉTODOS AUXILIARES

    /**
     * Parsea un valor de catálogo eliminando el código inicial
     * Ej: "207 - Planta de Procesamiento" -> "Planta de Procesamiento"
     * Ej: "1517 - RAIGRAS" -> "RAIGRAS"
     */
    private String parsearValorCatalogo(String valorCompleto) {
        if (valorCompleto == null || valorCompleto.trim().isEmpty()) {
            return "";
        }
        
        // Buscar patrón: número - texto
        Pattern pattern = Pattern.compile("^\\d+\\s*-\\s*(.+)$");
        Matcher matcher = pattern.matcher(valorCompleto.trim());
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return valorCompleto.trim();
    }

    /**
     * Extrae solo el código de un valor
     * Ej: "0522 - Procesamiento y almacenaje de Semillas LE" -> "0522"
     * Ej: "050 - INIA" -> "050"
     */
    private String parsearCodigo(String valorCompleto) {
        if (valorCompleto == null || valorCompleto.trim().isEmpty()) {
            return null;
        }
        
        Pattern pattern = Pattern.compile("^(\\d+)\\s*-");
        Matcher matcher = pattern.matcher(valorCompleto.trim());
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // Si no hay guión, intentar devolver solo números al inicio
        pattern = Pattern.compile("^(\\d+)");
        matcher = pattern.matcher(valorCompleto.trim());
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return valorCompleto.trim();
    }

    /**
     * Verifica si una fila está vacía
     */
    private boolean esFilaVacia(Row row) {
        if (row == null) {
            return true;
        }
        
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(row, i);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Obtiene el valor de una celda como String
     */
    private String getCellValueAsString(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                // Formatear número como string sin notación científica
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    /**
     * Obtiene el valor de una celda como BigDecimal
     */
    private BigDecimal getCellValueAsBigDecimal(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                if (value.isEmpty()) {
                    return null;
                }
                // Reemplazar coma por punto para decimales
                value = value.replace(",", ".");
                return new BigDecimal(value);
            }
        } catch (Exception e) {
            log.warn("Error convirtiendo celda a BigDecimal en columna {}: {}", columnIndex, e.getMessage());
        }
        
        return null;
    }

    /**
     * Obtiene el valor de una celda como Integer
     */
    private Integer getCellValueAsInteger(Row row, int columnIndex) {
        BigDecimal value = getCellValueAsBigDecimal(row, columnIndex);
        return value != null ? value.intValue() : null;
    }

    /**
     * Obtiene el valor de una celda como LocalDate
     */
    private LocalDate getCellValueAsDate(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            } else if (cell.getCellType() == CellType.STRING) {
                // Intentar parsear string como fecha
                String dateStr = cell.getStringCellValue().trim();
                if (dateStr.isEmpty()) {
                    return null;
                }
                // Formato esperado: d/M/yyyy
                String[] parts = dateStr.split("/");
                if (parts.length == 3) {
                    int day = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int year = Integer.parseInt(parts[2]);
                    return LocalDate.of(year, month, day);
                }
            }
        } catch (Exception e) {
            log.warn("Error convirtiendo celda a LocalDate en columna {}: {}", columnIndex, e.getMessage());
        }
        
        return null;
    }
}
