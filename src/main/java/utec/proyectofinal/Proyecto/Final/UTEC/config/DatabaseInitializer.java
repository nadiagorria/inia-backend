package utec.proyectofinal.Proyecto.Final.UTEC.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.UsuarioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol;
import utec.proyectofinal.Proyecto.Final.UTEC.services.UsuarioService;

/**
 * Inicializador de datos base del sistema.
 * Crea el usuario administrador predeterminado si no existe.
 */
@Component
@Order(1) // Se ejecuta primero
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultAdmin();
    }

    private void initializeDefaultAdmin() {
        try {
            // Verificar si ya existe al menos un admin en el sistema
            if (!usuarioRepository.existsByRol(Rol.ADMIN)) {
                System.out.println("\n No se encontró ningún administrador en el sistema");
                System.out.println(" Creando administrador predeterminado con 2FA...\n");
                
                usuarioService.crearAdminPredeterminado();
                
                
            } else {
                System.out.println("ℹ  Ya existe al menos un administrador en el sistema");
            }
        } catch (Exception e) {
            System.err.println(" Error al inicializar admin predeterminado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}