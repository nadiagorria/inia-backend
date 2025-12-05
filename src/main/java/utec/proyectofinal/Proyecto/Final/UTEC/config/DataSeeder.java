package utec.proyectofinal.Proyecto.Final.UTEC.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.*;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.*;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoContacto;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoLote;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoCatalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoAnalisis;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Instituto;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoListado;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoDOSN;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final LoteRepository loteRepository;
    private final GerminacionRepository germinacionRepository;
    private final PurezaRepository purezaRepository;
    private final PmsRepository pmsRepository;
    private final TetrazolioRepository tetrazolioRepository;
    private final DosnRepository dosnRepository;
    private final CultivarRepository cultivarRepository;
    private final ContactoRepository contactoRepository;
    private final CatalogoCrudRepository catalogoRepository;
    private final EspecieRepository especieRepository;
    private final MalezasCatalogoRepository malezasCatalogoRepository;
    private final ListadoRepository listadoRepository;

    @Override
    public void run(String... args) throws Exception {
        // Solo ejecutar si no hay lotes en la BD
        if (loteRepository.count() > 0) {
            log.info("La base de datos ya contiene lotes. Saltando seeder.");
            return;
        }

        log.info("Iniciando seeder de datos...");

        // 1. Crear catálogos necesarios
        Catalogo deposito1 = createCatalogo(TipoCatalogo.DEPOSITO, "Depósito Central");
        Catalogo deposito2 = createCatalogo(TipoCatalogo.DEPOSITO, "Depósito Norte");
        
        Catalogo articulo1 = createCatalogo(TipoCatalogo.ARTICULO, "ART-001");
        Catalogo articulo2 = createCatalogo(TipoCatalogo.ARTICULO, "ART-002");
        
        Catalogo origen1 = createCatalogo(TipoCatalogo.ORIGEN, "Nacional");
        Catalogo origen2 = createCatalogo(TipoCatalogo.ORIGEN, "Importado");
        
        Catalogo estado1 = createCatalogo(TipoCatalogo.ESTADO, "En Análisis");
        Catalogo estado2 = createCatalogo(TipoCatalogo.ESTADO, "Aprobado");
        
        catalogoRepository.saveAll(Arrays.asList(
            deposito1, deposito2, articulo1, articulo2, 
            origen1, origen2, estado1, estado2
        ));

        // 2. Crear especies y cultivares
        Especie trigo = createEspecie("Trigo");
        Especie soja = createEspecie("Soja");
        Especie maiz = createEspecie("Maíz");
        especieRepository.saveAll(Arrays.asList(trigo, soja, maiz));

        Cultivar cultivarTrigo = createCultivar(trigo, "INIA Tero");
        Cultivar cultivarSoja = createCultivar(soja, "Don Mario 4800");
        Cultivar cultivarMaiz = createCultivar(maiz, "DK 670");
        cultivarRepository.saveAll(Arrays.asList(cultivarTrigo, cultivarSoja, cultivarMaiz));

        // 2.1. Crear catálogo de malezas
        MalezasCatalogo maleza1 = createMaleza("Yuyo Colorado", "Amaranthus quitensis");
        MalezasCatalogo maleza2 = createMaleza("Nabo", "Raphanus sativus");
        MalezasCatalogo maleza3 = createMaleza("Avena Negra", "Avena fatua");
        MalezasCatalogo maleza4 = createMaleza("Cebadilla", "Bromus catharticus");
        MalezasCatalogo maleza5 = createMaleza("Pasto Blanco", "Digitaria sanguinalis");
        MalezasCatalogo maleza6 = createMaleza("Capiquí", "Stellaria media");
        malezasCatalogoRepository.saveAll(Arrays.asList(maleza1, maleza2, maleza3, maleza4, maleza5, maleza6));
        log.info("Catálogo de malezas creado");

        // 3. Crear contactos (empresas y clientes)
        Contacto empresa1 = createContacto("Semillas del Uruguay", "099123456", TipoContacto.EMPRESA);
        Contacto empresa2 = createContacto("Agropecuaria La Estancia", "099234567", TipoContacto.EMPRESA);
        Contacto cliente1 = createContacto("Juan Pérez", "099345678", TipoContacto.CLIENTE);
        Contacto cliente2 = createContacto("María González", "099456789", TipoContacto.CLIENTE);
        contactoRepository.saveAll(Arrays.asList(empresa1, empresa2, cliente1, cliente2));

        // 4. Crear lotes con diferentes configuraciones
        Lote lote1 = createLote(
            "FICHA-2024-001",
            "LOTE-TRIGO-001",
            cultivarTrigo,
            TipoLote.INTERNO,
            empresa1,
            cliente1,
            deposito1,
            articulo1,
            origen1,
            estado1,
            new BigDecimal("500.50"),
            LocalDate.now().minusDays(30),
            LocalDate.now().minusDays(25),
            Arrays.asList(TipoAnalisis.GERMINACION, TipoAnalisis.PUREZA)
        );

        Lote lote2 = createLote(
            "FICHA-2024-002",
            "LOTE-SOJA-002",
            cultivarSoja,
            TipoLote.EXTERNOS,
            empresa2,
            cliente2,
            deposito2,
            articulo2,
            origen2,
            estado1,
            new BigDecimal("750.25"),
            LocalDate.now().minusDays(20),
            LocalDate.now().minusDays(15),
            Arrays.asList(TipoAnalisis.GERMINACION, TipoAnalisis.PUREZA, TipoAnalisis.PMS, TipoAnalisis.DOSN)
        );

        Lote lote3 = createLote(
            "FICHA-2024-003",
            "LOTE-MAIZ-003",
            cultivarMaiz,
            TipoLote.OTROS_CENTROS_COSTOS,
            empresa1,
            cliente1,
            deposito1,
            articulo1,
            origen1,
            estado2,
            new BigDecimal("1000.00"),
            LocalDate.now().minusDays(45),
            LocalDate.now().minusDays(40),
            Arrays.asList(TipoAnalisis.GERMINACION)
        );

        loteRepository.saveAll(Arrays.asList(lote1, lote2, lote3));
        log.info("Lotes creados: {}, {}, {}", lote1.getNomLote(), lote2.getNomLote(), lote3.getNomLote());

        // 5. Crear análisis de germinación con tablas completas
        Germinacion germ1 = createGerminacionConTablas(
            lote1,
            Estado.EN_PROCESO,
            LocalDateTime.now().minusDays(5),
            null,
            "Análisis en curso",
            LocalDate.now().minusDays(1)
        );
        germinacionRepository.save(germ1);

        Germinacion germ2 = createGerminacionConTablas(
            lote2,
            Estado.APROBADO,
            LocalDateTime.now().minusDays(10),
            LocalDateTime.now().minusDays(3),
            "Germinación aprobada según estándares",
            LocalDate.now().minusDays(3)
        );
        germinacionRepository.save(germ2);

        Germinacion germ3 = createGerminacionConTablas(
            lote3,
            Estado.APROBADO,
            LocalDateTime.now().minusDays(35),
            LocalDateTime.now().minusDays(30),
            "Excelente germinación",
            LocalDate.now().minusDays(30)
        );
        germinacionRepository.save(germ3);

        log.info("Análisis de germinación creados con tablas completas: 3");

        // 6. Crear análisis de pureza
        Pureza pureza1 = createPureza(
            lote1,
            Estado.REGISTRADO,
            LocalDateTime.now().minusDays(4),
            null,
            "Análisis pendiente",
            LocalDate.now().minusDays(4),
            new BigDecimal("100.0000"),
            new BigDecimal("95.5000"),
            new BigDecimal("3.2000"),
            new BigDecimal("0.8000"),
            new BigDecimal("0.5000"),
            true
        );
        purezaRepository.save(pureza1);
        
        // Agregar listados de contaminantes a pureza1
        List<Listado> listadosPureza1 = new ArrayList<>();
        listadosPureza1.add(createListadoMaleza(pureza1, maleza1, TipoListado.MAL_COMUNES, Instituto.INIA, 3));
        listadosPureza1.add(createListadoMaleza(pureza1, maleza2, TipoListado.MAL_TOLERANCIA, Instituto.INIA, 2));
        listadosPureza1.add(createListadoOtroCultivo(pureza1, soja, Instituto.INIA, 1));
        pureza1.setListados(listadosPureza1);
        purezaRepository.save(pureza1);

        Pureza pureza2 = createPureza(
            lote2,
            Estado.APROBADO,
            LocalDateTime.now().minusDays(12),
            LocalDateTime.now().minusDays(5),
            "Pureza dentro de estándares",
            LocalDate.now().minusDays(12),
            new BigDecimal("100.0000"),
            new BigDecimal("97.8000"),
            new BigDecimal("1.5000"),
            new BigDecimal("0.4000"),
            new BigDecimal("0.3000"),
            true
        );
        purezaRepository.save(pureza2);
        
        // Agregar listados de contaminantes a pureza2
        List<Listado> listadosPureza2 = new ArrayList<>();
        listadosPureza2.add(createListadoMaleza(pureza2, maleza3, TipoListado.MAL_COMUNES, Instituto.INIA, 2));
        listadosPureza2.add(createListadoMaleza(pureza2, maleza4, TipoListado.MAL_COMUNES, Instituto.INASE, 1));
        listadosPureza2.add(createListadoOtroCultivo(pureza2, trigo, Instituto.INIA, 1));
        pureza2.setListados(listadosPureza2);
        purezaRepository.save(pureza2);

        log.info("Análisis de pureza creados: 2");

        // 7. Crear análisis de PMS con repeticiones
        Pms pms1 = createPms(
            lote2,
            Estado.APROBADO,
            LocalDateTime.now().minusDays(8),
            LocalDateTime.now().minusDays(2),
            "PMS completado y aprobado",
            8,  // 8 repeticiones
            1,  // 1 tanda
            false
        );
        pmsRepository.save(pms1);
        
        // Crear repeticiones para PMS1
        List<RepPms> repsPms1 = Arrays.asList(
            createRepPms(1, 1, new BigDecimal("5.234"), true, pms1),
            createRepPms(2, 1, new BigDecimal("5.189"), true, pms1),
            createRepPms(3, 1, new BigDecimal("5.267"), true, pms1),
            createRepPms(4, 1, new BigDecimal("5.221"), true, pms1),
            createRepPms(5, 1, new BigDecimal("5.198"), true, pms1),
            createRepPms(6, 1, new BigDecimal("5.245"), true, pms1),
            createRepPms(7, 1, new BigDecimal("5.212"), true, pms1),
            createRepPms(8, 1, new BigDecimal("5.278"), true, pms1)
        );
        pms1.setRepPms(repsPms1);
        pms1.setPromedio100g(new BigDecimal("5.2305"));
        pms1.setDesvioStd(new BigDecimal("0.0315"));
        pms1.setCoefVariacion(new BigDecimal("0.60"));
        pms1.setPmssinRedon(new BigDecimal("52.305"));
        pms1.setPmsconRedon(new BigDecimal("52.31"));
        pmsRepository.save(pms1);

        Pms pms2 = createPms(
            lote1,
            Estado.EN_PROCESO,
            LocalDateTime.now().minusDays(3),
            null,
            "Análisis en curso",
            4,
            2,
            true
        );
        pmsRepository.save(pms2);
        
        // Crear repeticiones para PMS2
        List<RepPms> repsPms2 = Arrays.asList(
            createRepPms(1, 1, new BigDecimal("3.145"), true, pms2),
            createRepPms(2, 1, new BigDecimal("3.178"), true, pms2),
            createRepPms(1, 2, new BigDecimal("3.156"), true, pms2),
            createRepPms(2, 2, new BigDecimal("3.189"), true, pms2)
        );
        pms2.setRepPms(repsPms2);
        pmsRepository.save(pms2);
        
        log.info("Análisis de PMS creados: 2");

        // 8. Crear análisis de Tetrazolio
        Tetrazolio tetra1 = createTetrazolio(
            lote3,
            Estado.APROBADO,
            LocalDateTime.now().minusDays(25),
            LocalDateTime.now().minusDays(20),
            "Tetrazolio completado",
            LocalDate.now().minusDays(25),
            100,
            "Remojo 18h",
            "1%",
            24,
            30,
            4
        );
        tetrazolioRepository.save(tetra1);
        
        // Crear repeticiones para Tetrazolio1
        List<RepTetrazolioViabilidad> repsTetra1 = Arrays.asList(
            createRepTetrazolio(LocalDate.now().minusDays(25), 92, 6, 2, tetra1),
            createRepTetrazolio(LocalDate.now().minusDays(25), 89, 8, 3, tetra1),
            createRepTetrazolio(LocalDate.now().minusDays(25), 91, 7, 2, tetra1),
            createRepTetrazolio(LocalDate.now().minusDays(25), 90, 8, 2, tetra1)
        );
        tetra1.setRepeticiones(repsTetra1);
        tetra1.setPorcViablesRedondeo(new BigDecimal("90.50"));
        tetra1.setPorcNoViablesRedondeo(new BigDecimal("7.25"));
        tetra1.setPorcDurasRedondeo(new BigDecimal("2.25"));
        tetra1.setViabilidadInase(new BigDecimal("92.75"));
        tetrazolioRepository.save(tetra1);

        Tetrazolio tetra2 = createTetrazolio(
            lote2,
            Estado.REGISTRADO,
            LocalDateTime.now().minusDays(2),
            null,
            "Tetrazolio iniciado",
            LocalDate.now().minusDays(2),
            50,
            "Remojo 24h",
            "0.5%",
            48,
            25,
            4
        );
        tetrazolioRepository.save(tetra2);
        
        log.info("Análisis de Tetrazolio creados: 2");

        // 9. Crear más lotes para variedad en reportes
        Lote lote4 = createLote(
            "FICHA-2024-004",
            "LOTE-SOJA-004",
            cultivarSoja,
            TipoLote.INTERNO,
            empresa1,
            cliente2,
            deposito1,
            articulo1,
            origen1,
            estado2,
            new BigDecimal("850.75"),
            LocalDate.now().minusDays(60),
            LocalDate.now().minusDays(55),
            Arrays.asList(TipoAnalisis.GERMINACION, TipoAnalisis.PUREZA, TipoAnalisis.TETRAZOLIO, TipoAnalisis.DOSN)
        );

        Lote lote5 = createLote(
            "FICHA-2024-005",
            "LOTE-TRIGO-005",
            cultivarTrigo,
            TipoLote.EXTERNOS,
            empresa2,
            cliente1,
            deposito2,
            articulo2,
            origen2,
            estado1,
            new BigDecimal("1200.00"),
            LocalDate.now().minusDays(15),
            LocalDate.now().minusDays(10),
            Arrays.asList(TipoAnalisis.GERMINACION, TipoAnalisis.PMS, TipoAnalisis.DOSN)
        );

        loteRepository.saveAll(Arrays.asList(lote4, lote5));
        log.info("Lotes adicionales creados: {}, {}", lote4.getNomLote(), lote5.getNomLote());

        // 10. Crear análisis de DOSN con listados completos
        Dosn dosn1 = createDosn(
            lote2,
            Estado.APROBADO,
            LocalDateTime.now().minusDays(14),
            LocalDateTime.now().minusDays(10),
            "DOSN Soja - Completo",
            true,
            LocalDate.now().minusDays(14),
            new BigDecimal("1000.0"),
            Arrays.asList(TipoDOSN.COMPLETO),
            LocalDate.now().minusDays(13),
            new BigDecimal("1000.0"),
            Arrays.asList(TipoDOSN.COMPLETO)
        );
        dosnRepository.save(dosn1);
        
        // Agregar cuscuta registros
        List<CuscutaRegistro> cuscutasDosn1 = Arrays.asList(
            createCuscutaRegistro(Instituto.INIA, new BigDecimal("0.5"), 2, LocalDate.now().minusDays(14), dosn1),
            createCuscutaRegistro(Instituto.INASE, new BigDecimal("0.3"), 1, LocalDate.now().minusDays(13), dosn1)
        );
        dosn1.setCuscutaRegistros(cuscutasDosn1);
        
        // Agregar listados de malezas
        List<Listado> listadosDosn1 = new ArrayList<>(Arrays.asList(
            createListadoDosnMaleza(dosn1, maleza1, TipoListado.MAL_COMUNES, Instituto.INIA, 5),
            createListadoDosnMaleza(dosn1, maleza2, TipoListado.MAL_TOLERANCIA, Instituto.INIA, 3),
            createListadoDosnMaleza(dosn1, maleza4, TipoListado.MAL_COMUNES, Instituto.INASE, 2),
            createListadoDosnOtroCultivo(dosn1, trigo, Instituto.INIA, 1)
        ));
        dosn1.setListados(listadosDosn1);
        dosnRepository.save(dosn1);

        Dosn dosn2 = createDosn(
            lote4,
            Estado.APROBADO,
            LocalDateTime.now().minusDays(53),
            LocalDateTime.now().minusDays(50),
            "DOSN Soja - Reducido",
            true,
            LocalDate.now().minusDays(53),
            new BigDecimal("500.0"),
            Arrays.asList(TipoDOSN.REDUCIDO),
            LocalDate.now().minusDays(52),
            new BigDecimal("500.0"),
            Arrays.asList(TipoDOSN.REDUCIDO)
        );
        dosnRepository.save(dosn2);
        
        // Agregar cuscuta registros
        List<CuscutaRegistro> cuscutasDosn2 = Arrays.asList(
            createCuscutaRegistro(Instituto.INIA, new BigDecimal("0.2"), 1, LocalDate.now().minusDays(53), dosn2),
            createCuscutaRegistro(Instituto.INASE, new BigDecimal("0.4"), 2, LocalDate.now().minusDays(52), dosn2)
        );
        dosn2.setCuscutaRegistros(cuscutasDosn2);
        
        // Agregar listados
        List<Listado> listadosDosn2 = new ArrayList<>(Arrays.asList(
            createListadoDosnMaleza(dosn2, maleza3, TipoListado.MAL_COMUNES, Instituto.INIA, 4),
            createListadoDosnMaleza(dosn2, maleza5, TipoListado.MAL_TOLERANCIA, Instituto.INASE, 2),
            createListadoDosnOtroCultivo(dosn2, maiz, Instituto.INIA, 1),
            createListadoDosnOtroCultivo(dosn2, trigo, Instituto.INASE, 1)
        ));
        dosn2.setListados(listadosDosn2);
        dosnRepository.save(dosn2);

        Dosn dosn3 = createDosn(
            lote5,
            Estado.EN_PROCESO,
            LocalDateTime.now().minusDays(8),
            null,
            "DOSN Trigo - En proceso",
            false,
            LocalDate.now().minusDays(8),
            new BigDecimal("1500.0"),
            Arrays.asList(TipoDOSN.COMPLETO, TipoDOSN.LIMITADO),
            null,
            null,
            null
        );
        dosnRepository.save(dosn3);
        
        // Agregar cuscuta registros (solo INIA por ahora)
        List<CuscutaRegistro> cuscutasDosn3 = Arrays.asList(
            createCuscutaRegistro(Instituto.INIA, new BigDecimal("0.8"), 3, LocalDate.now().minusDays(8), dosn3)
        );
        dosn3.setCuscutaRegistros(cuscutasDosn3);
        
        // Agregar listados
        List<Listado> listadosDosn3 = new ArrayList<>(Arrays.asList(
            createListadoDosnMaleza(dosn3, maleza6, TipoListado.MAL_COMUNES, Instituto.INIA, 6),
            createListadoDosnMaleza(dosn3, maleza1, TipoListado.MAL_TOLERANCIA_CERO, Instituto.INIA, 1),
            createListadoDosnOtroCultivo(dosn3, soja, Instituto.INIA, 2)
        ));
        dosn3.setListados(listadosDosn3);
        dosnRepository.save(dosn3);

        log.info("Análisis de DOSN creados: 3");

        // 11. Análisis adicionales con datos completos
        Germinacion germ4 = createGerminacionConTablas(
            lote4,
            Estado.APROBADO,
            LocalDateTime.now().minusDays(50),
            LocalDateTime.now().minusDays(45),
            "Germinación excelente - Soja",
            LocalDate.now().minusDays(45)
        );
        germinacionRepository.save(germ4);

        Germinacion germ5 = createGerminacionConTablas(
            lote5,
            Estado.EN_PROCESO,
            LocalDateTime.now().minusDays(7),
            null,
            "Germinación en curso - Trigo",
            LocalDate.now().minusDays(7)
        );
        germinacionRepository.save(germ5);
        log.info("Análisis de germinación adicionales creados: 2");

        Pureza pureza3 = createPureza(
            lote4,
            Estado.APROBADO,
            LocalDateTime.now().minusDays(52),
            LocalDateTime.now().minusDays(48),
            "Pureza excelente",
            LocalDate.now().minusDays(52),
            new BigDecimal("100.0000"),
            new BigDecimal("98.2000"),
            new BigDecimal("1.3000"),
            new BigDecimal("0.3000"),
            new BigDecimal("0.2000"),
            true
        );
        purezaRepository.save(pureza3);
        
        // Agregar listados de contaminantes a pureza3
        List<Listado> listadosPureza3 = new ArrayList<>();
        listadosPureza3.add(createListadoMaleza(pureza3, maleza5, TipoListado.MAL_COMUNES, Instituto.INIA, 1));
        listadosPureza3.add(createListadoMaleza(pureza3, maleza6, TipoListado.MAL_TOLERANCIA, Instituto.INASE, 1));
        listadosPureza3.add(createListadoOtroCultivo(pureza3, maiz, Instituto.INIA, 2));
        pureza3.setListados(listadosPureza3);
        purezaRepository.save(pureza3);

        Pms pms3 = createPms(
            lote5,
            Estado.APROBADO,
            LocalDateTime.now().minusDays(9),
            LocalDateTime.now().minusDays(6),
            "PMS Trigo - Excelente",
            8,
            1,
            false
        );
        pmsRepository.save(pms3);
        
        List<RepPms> repsPms3 = Arrays.asList(
            createRepPms(1, 1, new BigDecimal("4.567"), true, pms3),
            createRepPms(2, 1, new BigDecimal("4.589"), true, pms3),
            createRepPms(3, 1, new BigDecimal("4.543"), true, pms3),
            createRepPms(4, 1, new BigDecimal("4.578"), true, pms3),
            createRepPms(5, 1, new BigDecimal("4.592"), true, pms3),
            createRepPms(6, 1, new BigDecimal("4.561"), true, pms3),
            createRepPms(7, 1, new BigDecimal("4.575"), true, pms3),
            createRepPms(8, 1, new BigDecimal("4.584"), true, pms3)
        );
        pms3.setRepPms(repsPms3);
        pms3.setPromedio100g(new BigDecimal("4.5736"));
        pms3.setDesvioStd(new BigDecimal("0.0158"));
        pms3.setCoefVariacion(new BigDecimal("0.35"));
        pms3.setPmssinRedon(new BigDecimal("45.736"));
        pms3.setPmsconRedon(new BigDecimal("45.74"));
        pmsRepository.save(pms3);
        
        log.info("Análisis adicionales de Pureza y PMS creados");

        // 11. Crear más lotes para variedad en reportes (diferentes estados)
        Lote lote6 = createLote(
            "FICHA-2024-006",
            "LOTE-MAIZ-006",
            cultivarMaiz,
            TipoLote.INTERNO,
            empresa2,
            cliente2,
            deposito2,
            articulo1,
            origen1,
            estado1,
            new BigDecimal("625.30"),
            LocalDate.now().minusDays(90),
            LocalDate.now().minusDays(85),
            Arrays.asList(TipoAnalisis.GERMINACION, TipoAnalisis.PUREZA, TipoAnalisis.PMS, TipoAnalisis.TETRAZOLIO)
        );

        Lote lote7 = createLote(
            "FICHA-2024-007",
            "LOTE-TRIGO-007",
            cultivarTrigo,
            TipoLote.EXTERNOS,
            empresa1,
            cliente1,
            deposito1,
            articulo2,
            origen2,
            estado2,
            new BigDecimal("950.00"),
            LocalDate.now().minusDays(120),
            LocalDate.now().minusDays(115),
            Arrays.asList(TipoAnalisis.GERMINACION, TipoAnalisis.TETRAZOLIO)
        );

        loteRepository.saveAll(Arrays.asList(lote6, lote7));
        log.info("Lotes 6 y 7 creados");

        // Análisis para lote6 (todos completos y aprobados)
        Germinacion germ6 = createGerminacionConTablas(
            lote6,
            Estado.APROBADO,
            LocalDateTime.now().minusDays(80),
            LocalDateTime.now().minusDays(75),
            "Germinación Maíz - Aprobado",
            LocalDate.now().minusDays(75)
        );
        germinacionRepository.save(germ6);

        Pureza pureza4 = createPureza(
            lote6,
            Estado.APROBADO,
            LocalDateTime.now().minusDays(82),
            LocalDateTime.now().minusDays(78),
            "Pureza Maíz - Excelente",
            LocalDate.now().minusDays(82),
            new BigDecimal("100.0000"),
            new BigDecimal("96.5000"),
            new BigDecimal("2.8000"),
            new BigDecimal("0.4000"),
            new BigDecimal("0.3000"),
            true
        );
        purezaRepository.save(pureza4);
        
        // Agregar listados de contaminantes a pureza4
        List<Listado> listadosPureza4 = new ArrayList<>();
        listadosPureza4.add(createListadoMaleza(pureza4, maleza1, TipoListado.MAL_COMUNES, Instituto.INIA, 2));
        listadosPureza4.add(createListadoMaleza(pureza4, maleza3, TipoListado.MAL_TOLERANCIA, Instituto.INIA, 1));
        listadosPureza4.add(createListadoMaleza(pureza4, maleza4, TipoListado.MAL_TOLERANCIA_CERO, Instituto.INASE, 1));
        listadosPureza4.add(createListadoOtroCultivo(pureza4, trigo, Instituto.INIA, 1));
        listadosPureza4.add(createListadoOtroCultivo(pureza4, soja, Instituto.INASE, 1));
        pureza4.setListados(listadosPureza4);
        purezaRepository.save(pureza4);

        Pms pms4 = createPms(
            lote6,
            Estado.APROBADO,
            LocalDateTime.now().minusDays(78),
            LocalDateTime.now().minusDays(74),
            "PMS Maíz - Completo",
            8,
            1,
            false
        );
        pmsRepository.save(pms4);
        
        List<RepPms> repsPms4 = Arrays.asList(
            createRepPms(1, 1, new BigDecimal("6.234"), true, pms4),
            createRepPms(2, 1, new BigDecimal("6.189"), true, pms4),
            createRepPms(3, 1, new BigDecimal("6.267"), true, pms4),
            createRepPms(4, 1, new BigDecimal("6.221"), true, pms4),
            createRepPms(5, 1, new BigDecimal("6.198"), true, pms4),
            createRepPms(6, 1, new BigDecimal("6.245"), true, pms4),
            createRepPms(7, 1, new BigDecimal("6.212"), true, pms4),
            createRepPms(8, 1, new BigDecimal("6.278"), true, pms4)
        );
        pms4.setRepPms(repsPms4);
        pms4.setPromedio100g(new BigDecimal("6.2305"));
        pms4.setDesvioStd(new BigDecimal("0.0315"));
        pms4.setCoefVariacion(new BigDecimal("0.51"));
        pms4.setPmssinRedon(new BigDecimal("62.305"));
        pms4.setPmsconRedon(new BigDecimal("62.31"));
        pmsRepository.save(pms4);

        Tetrazolio tetra3 = createTetrazolio(
            lote6,
            Estado.APROBADO,
            LocalDateTime.now().minusDays(76),
            LocalDateTime.now().minusDays(72),
            "Tetrazolio Maíz - Aprobado",
            LocalDate.now().minusDays(76),
            100,
            "Remojo 16h",
            "1%",
            20,
            28,
            4
        );
        tetrazolioRepository.save(tetra3);
        
        List<RepTetrazolioViabilidad> repsTetra3 = Arrays.asList(
            createRepTetrazolio(LocalDate.now().minusDays(76), 94, 4, 2, tetra3),
            createRepTetrazolio(LocalDate.now().minusDays(76), 95, 3, 2, tetra3),
            createRepTetrazolio(LocalDate.now().minusDays(76), 93, 5, 2, tetra3),
            createRepTetrazolio(LocalDate.now().minusDays(76), 94, 4, 2, tetra3)
        );
        tetra3.setRepeticiones(repsTetra3);
        tetra3.setPorcViablesRedondeo(new BigDecimal("94.00"));
        tetra3.setPorcNoViablesRedondeo(new BigDecimal("4.00"));
        tetra3.setPorcDurasRedondeo(new BigDecimal("2.00"));
        tetra3.setViabilidadInase(new BigDecimal("96.00"));
        tetrazolioRepository.save(tetra3);

        log.info("Análisis completos para lote6 creados");

        // Análisis para lote7 (algunos pendientes)
        Germinacion germ7 = createGerminacionConTablas(
            lote7,
            Estado.A_REPETIR,
            LocalDateTime.now().minusDays(110),
            LocalDateTime.now().minusDays(105),
            "Germinación a repetir por resultados inconsistentes",
            LocalDate.now().minusDays(105)
        );
        germinacionRepository.save(germ7);

        Tetrazolio tetra4 = createTetrazolio(
            lote7,
            Estado.PENDIENTE_APROBACION,
            LocalDateTime.now().minusDays(108),
            LocalDateTime.now().minusDays(104),
            "Tetrazolio pendiente de aprobación",
            LocalDate.now().minusDays(108),
            100,
            "Remojo 20h",
            "0.75%",
            24,
            30,
            4
        );
        tetrazolioRepository.save(tetra4);
        
        List<RepTetrazolioViabilidad> repsTetra4 = Arrays.asList(
            createRepTetrazolio(LocalDate.now().minusDays(108), 85, 12, 3, tetra4),
            createRepTetrazolio(LocalDate.now().minusDays(108), 87, 10, 3, tetra4),
            createRepTetrazolio(LocalDate.now().minusDays(108), 86, 11, 3, tetra4),
            createRepTetrazolio(LocalDate.now().minusDays(108), 88, 9, 3, tetra4)
        );
        tetra4.setRepeticiones(repsTetra4);
        tetra4.setPorcViablesRedondeo(new BigDecimal("86.50"));
        tetra4.setPorcNoViablesRedondeo(new BigDecimal("10.50"));
        tetra4.setPorcDurasRedondeo(new BigDecimal("3.00"));
        tetra4.setViabilidadInase(new BigDecimal("89.50"));
        tetrazolioRepository.save(tetra4);

        log.info("Análisis para lote7 creados");

        log.info("Seeder completado exitosamente!");
        log.info("Total de lotes creados: 7");
        log.info("Total de análisis de Germinación: 7 (todos con tablas completas)");
        log.info("Total de análisis de Pureza: 4 (con listados de contaminantes)");
        log.info("Total de análisis de PMS: 4");
        log.info("Total de análisis de Tetrazolio: 4");
        log.info("Total de análisis de DOSN: 3 (con listados y cuscuta)");
    }

    private Catalogo createCatalogo(TipoCatalogo tipo, String valor) {
        Catalogo catalogo = new Catalogo();
        catalogo.setTipo(tipo);
        catalogo.setValor(valor);
        catalogo.setActivo(true);
        return catalogo;
    }

    private Especie createEspecie(String nombre) {
        Especie especie = new Especie();
        especie.setNombreComun(nombre);
        especie.setNombreCientifico(nombre + " sp.");
        especie.setActivo(true);
        return especie;
    }

    private Cultivar createCultivar(Especie especie, String nombre) {
        Cultivar cultivar = new Cultivar();
        cultivar.setEspecie(especie);
        cultivar.setNombre(nombre);
        cultivar.setActivo(true);
        return cultivar;
    }

    private Contacto createContacto(String nombre, String contacto, TipoContacto tipo) {
        Contacto c = new Contacto();
        c.setNombre(nombre);
        c.setContacto(contacto);
        c.setTipo(tipo);
        c.setActivo(true);
        return c;
    }

    private Lote createLote(
        String ficha,
        String nomLote,
        Cultivar cultivar,
        TipoLote tipo,
        Contacto empresa,
        Contacto cliente,
        Catalogo deposito,
        Catalogo numeroArticulo,
        Catalogo origen,
        Catalogo estado,
        BigDecimal kilosLimpios,
        LocalDate fechaEntrega,
        LocalDate fechaRecibo,
        List<TipoAnalisis> tiposAnalisis
    ) {
        Lote lote = new Lote();
        lote.setFicha(ficha);
        lote.setNomLote(nomLote);
        lote.setCultivar(cultivar);
        lote.setTipo(tipo);
        lote.setEmpresa(empresa);
        lote.setCliente(cliente);
        lote.setDeposito(deposito);
        lote.setNumeroArticulo(numeroArticulo);
        lote.setOrigen(origen);
        lote.setEstado(estado);
        lote.setKilosLimpios(kilosLimpios);
        lote.setFechaEntrega(fechaEntrega);
        lote.setFechaRecibo(fechaRecibo);
        lote.setFechaCosecha(fechaEntrega.minusMonths(1));
        lote.setCodigoCC("CC-" + ficha.substring(ficha.length() - 3));
        lote.setCodigoFF("FF-" + ficha.substring(ficha.length() - 3));
        lote.setUnidadEmbolsado("Bolsas 50kg");
        lote.setRemitente("Transportes del Campo");
        lote.setObservaciones("Lote de muestra generado por seeder");
        lote.setTiposAnalisisAsignados(tiposAnalisis);
        lote.setActivo(true);
        return lote;
    }

    private Pureza createPureza(
        Lote lote,
        Estado estado,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        String comentarios,
        LocalDate fecha,
        BigDecimal pesoInicial,
        BigDecimal semillaPura,
        BigDecimal materiaInerte,
        BigDecimal otrosCultivos,
        BigDecimal malezas,
        Boolean cumpleEstandar
    ) {
        Pureza pureza = new Pureza();
        pureza.setLote(lote);
        pureza.setEstado(estado);
        pureza.setFechaInicio(fechaInicio);
        pureza.setFechaFin(fechaFin);
        pureza.setComentarios(comentarios);
        pureza.setFecha(fecha);
        pureza.setPesoInicial_g(pesoInicial);
        pureza.setSemillaPura_g(semillaPura);
        pureza.setMateriaInerte_g(materiaInerte);
        pureza.setOtrosCultivos_g(otrosCultivos);
        pureza.setMalezas_g(malezas);
        pureza.setCumpleEstandar(cumpleEstandar);
        pureza.setActivo(true);
        return pureza;
    }

    private Germinacion createGerminacionConTablas(
        Lote lote,
        Estado estado,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        String comentarios,
        LocalDate fechaFinal
    ) {
        Germinacion germ = new Germinacion();
        germ.setLote(lote);
        germ.setEstado(estado);
        germ.setFechaInicio(fechaInicio);
        germ.setFechaFin(fechaFin);
        germ.setComentarios(comentarios);
        germ.setActivo(true);

        // Crear tabla de germinación con repeticiones completas
        TablaGerm tabla = new TablaGerm();
        tabla.setGerminacion(germ); // IMPORTANTE: vincular la tabla con la germinación
        tabla.setFechaFinal(fechaFinal);
        tabla.setTotal(400); // Total de semillas (4 repeticiones x 100)
        tabla.setFinalizada(estado == Estado.APROBADO);
        
        // Configurar campos adicionales de la tabla
        tabla.setTratamiento("Papel de Filtro");
        tabla.setProductoYDosis("Sin tratamiento");
        tabla.setNumSemillasPRep(100);
        tabla.setMetodo("Entre papel");
        tabla.setTemperatura("20°C constante");
        tabla.setTienePrefrio(false);
        tabla.setTienePretratamiento(false);
        tabla.setFechaIngreso(fechaFinal.minusDays(14));
        tabla.setFechaGerminacion(fechaFinal.minusDays(10));
        tabla.setFechaUltConteo(fechaFinal);
        tabla.setNumDias("14");
        tabla.setNumeroRepeticiones(4);
        tabla.setNumeroConteos(4);
        
        // Fechas de conteos
        List<LocalDate> fechasConteos = Arrays.asList(
            fechaFinal.minusDays(10),
            fechaFinal.minusDays(7),
            fechaFinal.minusDays(4),
            fechaFinal
        );
        tabla.setFechaConteos(fechasConteos);

        // Crear repeticiones con variedad de datos
        List<RepGerm> repeticiones = new ArrayList<>();
        
        RepGerm rep1 = new RepGerm();
        rep1.setNumRep(1);
        rep1.setNormales(Arrays.asList(18, 42, 68, 88)); // Conteos acumulativos por día
        rep1.setAnormales(8);
        rep1.setDuras(2);
        rep1.setFrescas(1);
        rep1.setMuertas(1);
        rep1.setTotal(100);
        rep1.setTablaGerm(tabla);
        repeticiones.add(rep1);

        RepGerm rep2 = new RepGerm();
        rep2.setNumRep(2);
        rep2.setNormales(Arrays.asList(16, 40, 66, 87));
        rep2.setAnormales(9);
        rep2.setDuras(1);
        rep2.setFrescas(2);
        rep2.setMuertas(1);
        rep2.setTotal(100);
        rep2.setTablaGerm(tabla);
        repeticiones.add(rep2);

        RepGerm rep3 = new RepGerm();
        rep3.setNumRep(3);
        rep3.setNormales(Arrays.asList(19, 43, 69, 89));
        rep3.setAnormales(7);
        rep3.setDuras(2);
        rep3.setFrescas(1);
        rep3.setMuertas(1);
        rep3.setTotal(100);
        rep3.setTablaGerm(tabla);
        repeticiones.add(rep3);

        RepGerm rep4 = new RepGerm();
        rep4.setNumRep(4);
        rep4.setNormales(Arrays.asList(17, 41, 67, 88));
        rep4.setAnormales(8);
        rep4.setDuras(2);
        rep4.setFrescas(1);
        rep4.setMuertas(1);
        rep4.setTotal(100);
        rep4.setTablaGerm(tabla);
        repeticiones.add(rep4);

        tabla.setRepGerm(repeticiones);

        // Configurar promedios sin redondeo (promedio de los finales)
        BigDecimal promedioNormales = new BigDecimal("88.00");
        BigDecimal promedioAnormales = new BigDecimal("8.00");
        BigDecimal promedioDuras = new BigDecimal("1.75");
        BigDecimal promedioFrescas = new BigDecimal("1.25");
        BigDecimal promedioMuertas = new BigDecimal("1.00");

        tabla.setPromedioSinRedondeo(Arrays.asList(
            promedioNormales,
            promedioAnormales,
            promedioDuras,
            promedioFrescas,
            promedioMuertas
        ));

        // Promedios sin redondeo por conteo (promedio de cada columna de conteos)
        tabla.setPromediosSinRedPorConteo(Arrays.asList(
            new BigDecimal("17.50"),  // Promedio primer conteo
            new BigDecimal("41.50"),  // Promedio segundo conteo
            new BigDecimal("67.50"),  // Promedio tercer conteo
            new BigDecimal("88.00")   // Promedio cuarto conteo (final)
        ));

        // Porcentajes con redondeo manual
        tabla.setPorcentajeNormalesConRedondeo(new BigDecimal("88.0"));
        tabla.setPorcentajeAnormalesConRedondeo(new BigDecimal("8.0"));
        tabla.setPorcentajeDurasConRedondeo(new BigDecimal("1.8"));
        tabla.setPorcentajeFrescasConRedondeo(new BigDecimal("1.3"));
        tabla.setPorcentajeMuertasConRedondeo(new BigDecimal("1.0"));

        // Crear valores INIA e INASE
        List<ValoresGerm> valores = new ArrayList<>();

        ValoresGerm valorINIA = new ValoresGerm();
        valorINIA.setInstituto(Instituto.INIA);
        valorINIA.setNormales(promedioNormales);
        valorINIA.setAnormales(promedioAnormales);
        valorINIA.setDuras(promedioDuras);
        valorINIA.setFrescas(promedioFrescas);
        valorINIA.setMuertas(promedioMuertas);
        valorINIA.setGerminacion(promedioNormales); // INIA: solo normales
        valorINIA.setTablaGerm(tabla);
        valores.add(valorINIA);

        ValoresGerm valorINASE = new ValoresGerm();
        valorINASE.setInstituto(Instituto.INASE);
        valorINASE.setNormales(promedioNormales);
        valorINASE.setAnormales(promedioAnormales);
        valorINASE.setDuras(promedioDuras);
        valorINASE.setFrescas(promedioFrescas);
        valorINASE.setMuertas(promedioMuertas);
        // INASE: normales + duras
        valorINASE.setGerminacion(promedioNormales.add(promedioDuras));
        valorINASE.setTablaGerm(tabla);
        valores.add(valorINASE);

        tabla.setValoresGerm(valores);

        // Asignar la tabla a la germinación
        List<TablaGerm> tablas = new ArrayList<>();
        tablas.add(tabla);
        germ.setTablaGerm(tablas);

        return germ;
    }

    private Pms createPms(
        Lote lote,
        Estado estado,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        String comentarios,
        Integer numRepeticiones,
        Integer numTandas,
        Boolean esSemillaBrozosa
    ) {
        Pms pms = new Pms();
        pms.setLote(lote);
        pms.setEstado(estado);
        pms.setFechaInicio(fechaInicio);
        pms.setFechaFin(fechaFin);
        pms.setComentarios(comentarios);
        pms.setNumRepeticionesEsperadas(numRepeticiones);
        pms.setNumTandas(numTandas);
        pms.setEsSemillaBrozosa(esSemillaBrozosa);
        pms.setActivo(true);
        return pms;
    }

    private RepPms createRepPms(
        Integer numRep,
        Integer numTanda,
        BigDecimal peso,
        Boolean valido,
        Pms pms
    ) {
        RepPms rep = new RepPms();
        rep.setNumRep(numRep);
        rep.setNumTanda(numTanda);
        rep.setPeso(peso);
        rep.setValido(valido);
        rep.setPms(pms);
        return rep;
    }

    private Tetrazolio createTetrazolio(
        Lote lote,
        Estado estado,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        String comentarios,
        LocalDate fecha,
        Integer numSemillasPorRep,
        String pretratamiento,
        String concentracion,
        Integer tincionHs,
        Integer tincionTemp,
        Integer numRepeticiones
    ) {
        Tetrazolio tetra = new Tetrazolio();
        tetra.setLote(lote);
        tetra.setEstado(estado);
        tetra.setFechaInicio(fechaInicio);
        tetra.setFechaFin(fechaFin);
        tetra.setComentarios(comentarios);
        tetra.setFecha(fecha);
        tetra.setNumSemillasPorRep(numSemillasPorRep);
        tetra.setPretratamiento(pretratamiento);
        tetra.setConcentracion(concentracion);
        tetra.setTincionHs(tincionHs);
        tetra.setTincionTemp(tincionTemp);
        tetra.setNumRepeticionesEsperadas(numRepeticiones);
        tetra.setActivo(true);
        return tetra;
    }

    private RepTetrazolioViabilidad createRepTetrazolio(
        LocalDate fecha,
        Integer viables,
        Integer noViables,
        Integer duras,
        Tetrazolio tetrazolio
    ) {
        RepTetrazolioViabilidad rep = new RepTetrazolioViabilidad();
        rep.setFecha(fecha);
        rep.setViablesNum(viables);
        rep.setNoViablesNum(noViables);
        rep.setDuras(duras);
        rep.setTetrazolio(tetrazolio);
        return rep;
    }

    private MalezasCatalogo createMaleza(String nombreComun, String nombreCientifico) {
        MalezasCatalogo maleza = new MalezasCatalogo();
        maleza.setNombreComun(nombreComun);
        maleza.setNombreCientifico(nombreCientifico);
        maleza.setActivo(true);
        return maleza;
    }

    private Listado createListadoMaleza(
        Pureza pureza,
        MalezasCatalogo maleza,
        TipoListado tipo,
        Instituto instituto,
        Integer numero
    ) {
        Listado listado = new Listado();
        listado.setPureza(pureza);
        listado.setCatalogo(maleza);
        listado.setListadoTipo(tipo);
        listado.setListadoInsti(instituto);
        listado.setListadoNum(numero);
        return listado;
    }

    private Listado createListadoOtroCultivo(
        Pureza pureza,
        Especie especie,
        Instituto instituto,
        Integer numero
    ) {
        Listado listado = new Listado();
        listado.setPureza(pureza);
        listado.setEspecie(especie);
        listado.setListadoTipo(TipoListado.OTROS);
        listado.setListadoInsti(instituto);
        listado.setListadoNum(numero);
        return listado;
    }

    private Dosn createDosn(
        Lote lote,
        Estado estado,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        String comentarios,
        Boolean cumpleEstandar,
        LocalDate fechaINIA,
        BigDecimal gramosINIA,
        List<TipoDOSN> tipoINIA,
        LocalDate fechaINASE,
        BigDecimal gramosINASE,
        List<TipoDOSN> tipoINASE
    ) {
        Dosn dosn = new Dosn();
        dosn.setLote(lote);
        dosn.setEstado(estado);
        dosn.setFechaInicio(fechaInicio);
        dosn.setFechaFin(fechaFin);
        dosn.setComentarios(comentarios);
        dosn.setCumpleEstandar(cumpleEstandar);
        dosn.setFechaINIA(fechaINIA);
        dosn.setGramosAnalizadosINIA(gramosINIA);
        dosn.setTipoINIA(tipoINIA);
        dosn.setFechaINASE(fechaINASE);
        dosn.setGramosAnalizadosINASE(gramosINASE);
        dosn.setTipoINASE(tipoINASE);
        dosn.setActivo(true);
        return dosn;
    }

    private CuscutaRegistro createCuscutaRegistro(
        Instituto instituto,
        BigDecimal cuscutaGramos,
        Integer cuscutaNum,
        LocalDate fecha,
        Dosn dosn
    ) {
        CuscutaRegistro registro = new CuscutaRegistro();
        registro.setInstituto(instituto);
        registro.setCuscuta_g(cuscutaGramos);
        registro.setCuscutaNum(cuscutaNum);
        registro.setFechaCuscuta(fecha);
        registro.setDosn(dosn);
        return registro;
    }

    private Listado createListadoDosnMaleza(
        Dosn dosn,
        MalezasCatalogo maleza,
        TipoListado tipo,
        Instituto instituto,
        Integer numero
    ) {
        Listado listado = new Listado();
        listado.setDosn(dosn);
        listado.setCatalogo(maleza);
        listado.setListadoTipo(tipo);
        listado.setListadoInsti(instituto);
        listado.setListadoNum(numero);
        return listado;
    }

    private Listado createListadoDosnOtroCultivo(
        Dosn dosn,
        Especie especie,
        Instituto instituto,
        Integer numero
    ) {
        Listado listado = new Listado();
        listado.setDosn(dosn);
        listado.setEspecie(especie);
        listado.setListadoTipo(TipoListado.OTROS);
        listado.setListadoInsti(instituto);
        listado.setListadoNum(numero);
        return listado;
    }
}
