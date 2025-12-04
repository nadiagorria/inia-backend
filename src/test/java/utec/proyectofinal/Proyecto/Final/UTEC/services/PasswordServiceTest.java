package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de PasswordService (Seguridad)")
class PasswordServiceTest {

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
    }

    @Test
    @DisplayName("Encriptar contraseña - debe retornar hash BCrypt válido")
    void encryptPassword_debeRetornarHashBCrypt() {
        
        String plainPassword = "MiContraseña123!";

        
        String encryptedPassword = passwordService.encryptPassword(plainPassword);

        
        assertNotNull(encryptedPassword, "El hash no debe ser nulo");
        assertNotEquals(plainPassword, encryptedPassword, "El hash debe ser diferente a la contraseña original");
        assertTrue(encryptedPassword.startsWith("$2a$") || encryptedPassword.startsWith("$2b$"), 
            "Debe ser un hash BCrypt válido");
        assertTrue(encryptedPassword.length() > 50, "El hash BCrypt debe tener longitud apropiada");
    }

    @Test
    @DisplayName("Encriptar misma contraseña dos veces - debe generar hashes diferentes")
    void encryptPassword_mismaContraseña_debeGenerarHashesDiferentes() {
        
        String plainPassword = "Password123!";

        
        String hash1 = passwordService.encryptPassword(plainPassword);
        String hash2 = passwordService.encryptPassword(plainPassword);

        
        assertNotEquals(hash1, hash2, 
            "BCrypt debe generar hashes diferentes para la misma contraseña (salt aleatorio)");
    }

    @Test
    @DisplayName("Verificar contraseña correcta - debe retornar true")
    void matchPassword_contraseñaCorrecta_debeRetornarTrue() {
        
        String plainPassword = "SecurePass456!";
        String encryptedPassword = passwordService.encryptPassword(plainPassword);

        
        boolean matches = passwordService.matchPassword(plainPassword, encryptedPassword);

        
        assertTrue(matches, "La contraseña correcta debe ser verificada exitosamente");
    }

    @Test
    @DisplayName("Verificar contraseña incorrecta - debe retornar false")
    void matchPassword_contraseñaIncorrecta_debeRetornarFalse() {
        
        String plainPassword = "CorrectPassword123!";
        String wrongPassword = "WrongPassword456!";
        String encryptedPassword = passwordService.encryptPassword(plainPassword);

        
        boolean matches = passwordService.matchPassword(wrongPassword, encryptedPassword);

        
        assertFalse(matches, "Una contraseña incorrecta debe fallar la verificación");
    }

    @Test
    @DisplayName("Verificar contraseña con mayúsculas/minúsculas - debe ser case sensitive")
    void matchPassword_caseSensitive_debeFallar() {
        
        String plainPassword = "Password123";
        String wrongCasePassword = "password123";
        String encryptedPassword = passwordService.encryptPassword(plainPassword);

        
        boolean matches = passwordService.matchPassword(wrongCasePassword, encryptedPassword);

        
        assertFalse(matches, "La verificación debe ser case-sensitive");
    }

    @Test
    @DisplayName("Encriptar contraseña con caracteres especiales - debe funcionar correctamente")
    void encryptPassword_conCaracteresEspeciales_debeFuncionar() {
        
        String specialPassword = "P@ssw0rd!#$%&*()";

        
        String encrypted = passwordService.encryptPassword(specialPassword);
        boolean matches = passwordService.matchPassword(specialPassword, encrypted);

        
        assertNotNull(encrypted);
        assertTrue(matches, "Debe manejar caracteres especiales correctamente");
    }

    @Test
    @DisplayName("Encriptar contraseña con espacios - debe preservar espacios")
    void encryptPassword_conEspacios_debePreservarEspacios() {
        
        String passwordWithSpaces = "My Pass Word 123";

        
        String encrypted = passwordService.encryptPassword(passwordWithSpaces);
        boolean matchesWithSpaces = passwordService.matchPassword(passwordWithSpaces, encrypted);
        boolean matchesWithoutSpaces = passwordService.matchPassword("MyPassWord123", encrypted);

        
        assertTrue(matchesWithSpaces, "Debe preservar espacios en la contraseña");
        assertFalse(matchesWithoutSpaces, "No debe coincidir sin los espacios");
    }

    @Test
    @DisplayName("Encriptar contraseña muy larga - debe funcionar")
    void encryptPassword_contraseñaMuyLarga_debeFuncionar() {
        
        
        String longPassword = "A".repeat(70);

        
        String encrypted = passwordService.encryptPassword(longPassword);
        boolean matches = passwordService.matchPassword(longPassword, encrypted);

        
        assertNotNull(encrypted);
        assertTrue(matches, "Debe manejar contraseñas largas (hasta 72 bytes)");
    }

    @Test
    @DisplayName("Encriptar contraseña muy corta - debe funcionar")
    void encryptPassword_contraseñaMuyCorta_debeFuncionar() {
        
        String shortPassword = "Ab1";

        
        String encrypted = passwordService.encryptPassword(shortPassword);
        boolean matches = passwordService.matchPassword(shortPassword, encrypted);

        
        assertNotNull(encrypted);
        assertTrue(matches, "Debe manejar contraseñas cortas");
    }

    @Test
    @DisplayName("Verificar con contraseña vacía - debe retornar false")
    void matchPassword_contraseñaVacia_debeRetornarFalse() {
        
        String plainPassword = "ValidPassword123";
        String encryptedPassword = passwordService.encryptPassword(plainPassword);

        
        boolean matches = passwordService.matchPassword("", encryptedPassword);

        
        assertFalse(matches, "Una contraseña vacía no debe coincidir");
    }

    @Test
    @DisplayName("Encriptar contraseña con Unicode - debe funcionar")
    void encryptPassword_conUnicode_debeFuncionar() {
        
        String unicodePassword = "Contraseña123áéíóú";

        
        String encrypted = passwordService.encryptPassword(unicodePassword);
        boolean matches = passwordService.matchPassword(unicodePassword, encrypted);

        
        assertNotNull(encrypted);
        assertTrue(matches, "Debe manejar caracteres Unicode correctamente");
    }

    @Test
    @DisplayName("Verificar múltiples contraseñas diferentes - solo una debe coincidir")
    void matchPassword_multiplesCandidatos_soloUnaCoincide() {
        
        String correctPassword = "CorrectOne123!";
        String encryptedPassword = passwordService.encryptPassword(correctPassword);
        
        String[] wrongPasswords = {
            "WrongOne123!",
            "CorrectOne124!",
            "correctone123!",
            "CorrectOne123",
            "CorrectOne123!!"
        };

        
        assertTrue(passwordService.matchPassword(correctPassword, encryptedPassword), 
            "La contraseña correcta debe coincidir");
        
        for (String wrongPassword : wrongPasswords) {
            assertFalse(passwordService.matchPassword(wrongPassword, encryptedPassword), 
                "La contraseña '" + wrongPassword + "' no debe coincidir");
        }
    }

    @Test
    @DisplayName("Encriptar y verificar contraseña compleja - debe funcionar")
    void encryptPassword_contraseñaCompleja_debeFuncionar() {
        
        String complexPassword = "C0mpl3x!P@ssw0rd#2024$%&";

        
        String encrypted = passwordService.encryptPassword(complexPassword);
        boolean matches = passwordService.matchPassword(complexPassword, encrypted);

        
        assertNotNull(encrypted);
        assertTrue(matches, "Debe manejar contraseñas complejas correctamente");
    }

    @Test
    @DisplayName("Encriptar misma contraseña 10 veces - todos los hashes deben ser únicos")
    void encryptPassword_10Veces_todosHashesUnicos() {
        
        String password = "TestPassword123";
        int iterations = 10;

        
        String[] hashes = new String[iterations];
        for (int i = 0; i < iterations; i++) {
            hashes[i] = passwordService.encryptPassword(password);
        }

        
        for (int i = 0; i < iterations; i++) {
            for (int j = i + 1; j < iterations; j++) {
                assertNotEquals(hashes[i], hashes[j], 
                    "Todos los hashes deben ser únicos debido al salt aleatorio");
            }
        }

        
        for (String hash : hashes) {
            assertTrue(passwordService.matchPassword(password, hash), 
                "Todos los hashes deben verificar correctamente");
        }
    }
}
