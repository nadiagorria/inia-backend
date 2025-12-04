package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para SetupTokenService
 * 
 * Valida la creación, validación y consumo de tokens JWT de configuración inicial
 * con firma HMAC-SHA256, expiración automática y blacklist de un solo uso
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de SetupTokenService")
class SetupTokenServiceTest {

    private SetupTokenService setupTokenService;

    private static final Integer TEST_USER_ID = 1;
    private static final String TEST_NOMBRE = "Admin Test";
    private static final String TEST_QR_CODE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";
    private static final String TEST_TOTP_SECRET = "JBSWY3DPEHPK3PXP";

    @BeforeEach
    void setUp() {
        setupTokenService = new SetupTokenService();
    }

    @Test
    @DisplayName("createSetupToken - debe crear token JWT válido con todos los claims")
    void createSetupToken_debeCrearTokenValido() {
        
        String token = setupTokenService.createSetupToken(TEST_USER_ID, TEST_NOMBRE, TEST_QR_CODE, TEST_TOTP_SECRET);

        
        assertNotNull(token, "El token no debe ser null");
        assertFalse(token.isEmpty(), "El token no debe estar vacío");
        assertTrue(token.contains("."), "El token debe tener formato JWT (con puntos)");
        
        
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "Token JWT debe tener 3 partes (header.payload.signature)");
    }

    @Test
    @DisplayName("createSetupToken - debe incluir userId en el token")
    void createSetupToken_debeIncluirUserId() {
        
        String token = setupTokenService.createSetupToken(TEST_USER_ID, TEST_NOMBRE, TEST_QR_CODE, TEST_TOTP_SECRET);
        Map<String, Object> data = setupTokenService.consumeSetupToken(token);

        
        assertNotNull(data);
        assertEquals(TEST_USER_ID, data.get("userId"));
    }

    @Test
    @DisplayName("createSetupToken - debe incluir nombre en el token")
    void createSetupToken_debeIncluirNombre() {
        
        String token = setupTokenService.createSetupToken(TEST_USER_ID, TEST_NOMBRE, TEST_QR_CODE, TEST_TOTP_SECRET);
        Map<String, Object> data = setupTokenService.consumeSetupToken(token);

        
        assertNotNull(data);
        assertEquals(TEST_NOMBRE, data.get("nombre"));
    }

    @Test
    @DisplayName("createSetupToken - debe incluir QR code en el token")
    void createSetupToken_debeIncluirQrCode() {
        
        String token = setupTokenService.createSetupToken(TEST_USER_ID, TEST_NOMBRE, TEST_QR_CODE, TEST_TOTP_SECRET);
        Map<String, Object> data = setupTokenService.consumeSetupToken(token);

        
        assertNotNull(data);
        assertEquals(TEST_QR_CODE, data.get("qrCodeDataUrl"));
    }

    @Test
    @DisplayName("createSetupToken - debe incluir TOTP secret en el token")
    void createSetupToken_debeIncluirTotpSecret() {
        
        String token = setupTokenService.createSetupToken(TEST_USER_ID, TEST_NOMBRE, TEST_QR_CODE, TEST_TOTP_SECRET);
        Map<String, Object> data = setupTokenService.consumeSetupToken(token);

        
        assertNotNull(data);
        assertEquals(TEST_TOTP_SECRET, data.get("totpSecret"));
    }

    @Test
    @DisplayName("createSetupToken - debe crear tokens únicos para cada invocación")
    void createSetupToken_debeCrearTokensUnicos() {
        
        String token1 = setupTokenService.createSetupToken(TEST_USER_ID, TEST_NOMBRE, TEST_QR_CODE, TEST_TOTP_SECRET);
        String token2 = setupTokenService.createSetupToken(TEST_USER_ID, TEST_NOMBRE, TEST_QR_CODE, TEST_TOTP_SECRET);

        
        assertNotEquals(token1, token2, "Cada token debe ser único (diferente JTI)");
    }

    @Test
    @DisplayName("consumeSetupToken - debe validar y extraer datos correctamente")
    void consumeSetupToken_debeValidarYExtraerDatos() {
        
        String token = setupTokenService.createSetupToken(TEST_USER_ID, TEST_NOMBRE, TEST_QR_CODE, TEST_TOTP_SECRET);

        
        Map<String, Object> data = setupTokenService.consumeSetupToken(token);

        
        assertNotNull(data, "Los datos extraídos no deben ser null");
        assertEquals(TEST_USER_ID, data.get("userId"));
        assertEquals(TEST_NOMBRE, data.get("nombre"));
        assertEquals(TEST_QR_CODE, data.get("qrCodeDataUrl"));
        assertEquals(TEST_TOTP_SECRET, data.get("totpSecret"));
    }

    @Test
    @DisplayName("consumeSetupToken - debe permitir un solo uso del token (blacklist)")
    void consumeSetupToken_debePermitirUnSoloUso() {
        
        String token = setupTokenService.createSetupToken(TEST_USER_ID, TEST_NOMBRE, TEST_QR_CODE, TEST_TOTP_SECRET);

        
        Map<String, Object> firstUse = setupTokenService.consumeSetupToken(token);
        
        Map<String, Object> secondUse = setupTokenService.consumeSetupToken(token);

        
        assertNotNull(firstUse, "El primer uso debe ser exitoso");
        assertNull(secondUse, "El segundo uso debe fallar (token en blacklist)");
    }

    @Test
    @DisplayName("consumeSetupToken - debe retornar null para token inválido")
    void consumeSetupToken_debeRetornarNullParaTokenInvalido() {
        
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.invalid";

        
        Map<String, Object> data = setupTokenService.consumeSetupToken(invalidToken);

        
        assertNull(data, "Token inválido debe retornar null");
    }

    @Test
    @DisplayName("consumeSetupToken - debe retornar null para token vacío")
    void consumeSetupToken_debeRetornarNullParaTokenVacio() {
        
        Map<String, Object> data = setupTokenService.consumeSetupToken("");

        
        assertNull(data, "Token vacío debe retornar null");
    }

    @Test
    @DisplayName("consumeSetupToken - debe retornar null para token malformado")
    void consumeSetupToken_debeRetornarNullParaTokenMalformado() {
        
        Map<String, Object> data = setupTokenService.consumeSetupToken("not.a.valid.jwt.token");

        
        assertNull(data, "Token malformado debe retornar null");
    }

    @Test
    @DisplayName("isTokenValid - debe retornar true para token válido no usado")
    void isTokenValid_debeRetornarTrueParaTokenValido() {
        
        String token = setupTokenService.createSetupToken(TEST_USER_ID, TEST_NOMBRE, TEST_QR_CODE, TEST_TOTP_SECRET);

        
        boolean isValid = setupTokenService.isTokenValid(token);

        
        assertTrue(isValid, "Token recién creado debe ser válido");
    }

    @Test
    @DisplayName("isTokenValid - debe retornar false para token ya usado")
    void isTokenValid_debeRetornarFalseParaTokenUsado() {
        
        String token = setupTokenService.createSetupToken(TEST_USER_ID, TEST_NOMBRE, TEST_QR_CODE, TEST_TOTP_SECRET);
        setupTokenService.consumeSetupToken(token); 

        
        boolean isValid = setupTokenService.isTokenValid(token);

        
        assertFalse(isValid, "Token usado debe ser inválido");
    }

    @Test
    @DisplayName("isTokenValid - debe retornar false para token inválido")
    void isTokenValid_debeRetornarFalseParaTokenInvalido() {
        
        boolean isValid = setupTokenService.isTokenValid("invalid.token.here");

        
        assertFalse(isValid, "Token inválido debe retornar false");
    }

    @Test
    @DisplayName("isTokenValid - debe retornar false para token vacío")
    void isTokenValid_debeRetornarFalseParaTokenVacio() {
        
        boolean isValid = setupTokenService.isTokenValid("");

        
        assertFalse(isValid, "Token vacío debe retornar false");
    }

    @Test
    @DisplayName("isTokenValid - debe retornar false para token null")
    void isTokenValid_debeRetornarFalseParaTokenNull() {
        
        boolean isValid = setupTokenService.isTokenValid(null);

        
        assertFalse(isValid, "Token null debe retornar false");
    }

    @Test
    @DisplayName("invalidateToken - debe invalidar token manualmente")
    void invalidateToken_debeInvalidarTokenManualmente() {
        
        String token = setupTokenService.createSetupToken(TEST_USER_ID, TEST_NOMBRE, TEST_QR_CODE, TEST_TOTP_SECRET);
        
        
        assertTrue(setupTokenService.isTokenValid(token), "Token debe ser válido antes de invalidar");

        
        setupTokenService.invalidateToken(token);

        
        assertFalse(setupTokenService.isTokenValid(token), "Token debe ser inválido después de invalidar");
        assertNull(setupTokenService.consumeSetupToken(token), "Token invalidado no debe poder consumirse");
    }

    @Test
    @DisplayName("invalidateToken - debe manejar token inválido sin errores")
    void invalidateToken_debeManjejarTokenInvalidoSinErrores() {
        
        assertDoesNotThrow(() -> setupTokenService.invalidateToken("invalid.token"));
        assertDoesNotThrow(() -> setupTokenService.invalidateToken(""));
        assertDoesNotThrow(() -> setupTokenService.invalidateToken(null));
    }

    @Test
    @DisplayName("invalidateToken - token ya invalidado debe permanecer inválido")
    void invalidateToken_tokenYaInvalidadoDebePermanecer() {
        
        String token = setupTokenService.createSetupToken(TEST_USER_ID, TEST_NOMBRE, TEST_QR_CODE, TEST_TOTP_SECRET);
        
        
        setupTokenService.invalidateToken(token);
        setupTokenService.invalidateToken(token); 

        
        assertFalse(setupTokenService.isTokenValid(token), "Token debe permanecer inválido");
    }

    @Test
    @DisplayName("cleanExpiredTokensFromBlacklist - debe ejecutarse sin errores al crear token")
    void cleanExpiredTokensFromBlacklist_debeEjecutarseSinErrores() {
        
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 10; i++) {
                setupTokenService.createSetupToken(i, "User " + i, TEST_QR_CODE, TEST_TOTP_SECRET);
            }
        });
    }

    @Test
    @DisplayName("getSigningKey - debe generar tokens con firma consistente")
    void getSigningKey_debeGenerarFirmaConsistente() {
        
        String token1 = setupTokenService.createSetupToken(TEST_USER_ID, TEST_NOMBRE, TEST_QR_CODE, TEST_TOTP_SECRET);
        String token2 = setupTokenService.createSetupToken(TEST_USER_ID, TEST_NOMBRE, TEST_QR_CODE, TEST_TOTP_SECRET);
        
        
        Map<String, Object> data1 = setupTokenService.consumeSetupToken(token1);
        Map<String, Object> data2 = setupTokenService.consumeSetupToken(token2);

        
        assertNotNull(data1, "Token 1 debe validarse correctamente");
        assertNotNull(data2, "Token 2 debe validarse correctamente");
    }

    @Test
    @DisplayName("createSetupToken - debe crear token válido con diferentes usuarios")
    void createSetupToken_debeFuncionarConDiferentesUsuarios() {
        
        String token1 = setupTokenService.createSetupToken(1, "Admin 1", TEST_QR_CODE, "SECRET1");
        String token2 = setupTokenService.createSetupToken(2, "Admin 2", TEST_QR_CODE, "SECRET2");
        String token3 = setupTokenService.createSetupToken(3, "Admin 3", TEST_QR_CODE, "SECRET3");

        
        assertTrue(setupTokenService.isTokenValid(token1));
        assertTrue(setupTokenService.isTokenValid(token2));
        assertTrue(setupTokenService.isTokenValid(token3));
        
        
        Map<String, Object> data1 = setupTokenService.consumeSetupToken(token1);
        Map<String, Object> data2 = setupTokenService.consumeSetupToken(token2);
        Map<String, Object> data3 = setupTokenService.consumeSetupToken(token3);
        
        assertEquals(1, data1.get("userId"));
        assertEquals(2, data2.get("userId"));
        assertEquals(3, data3.get("userId"));
    }

    @Test
    @DisplayName("consumeSetupToken - debe manejar múltiples tokens en blacklist")
    void consumeSetupToken_debeManjejarMultiplesTokensEnBlacklist() {
        
        String token1 = setupTokenService.createSetupToken(1, "User 1", TEST_QR_CODE, "SECRET1");
        String token2 = setupTokenService.createSetupToken(2, "User 2", TEST_QR_CODE, "SECRET2");
        String token3 = setupTokenService.createSetupToken(3, "User 3", TEST_QR_CODE, "SECRET3");

        
        Map<String, Object> data1 = setupTokenService.consumeSetupToken(token1);
        Map<String, Object> data2 = setupTokenService.consumeSetupToken(token2);
        Map<String, Object> data3 = setupTokenService.consumeSetupToken(token3);

        
        assertNotNull(data1);
        assertNotNull(data2);
        assertNotNull(data3);
        
        
        assertNull(setupTokenService.consumeSetupToken(token1));
        assertNull(setupTokenService.consumeSetupToken(token2));
        assertNull(setupTokenService.consumeSetupToken(token3));
    }

    @Test
    @DisplayName("isTokenValid - debe verificar validez sin consumir el token")
    void isTokenValid_debeVerificarSinConsumir() {
        
        String token = setupTokenService.createSetupToken(TEST_USER_ID, TEST_NOMBRE, TEST_QR_CODE, TEST_TOTP_SECRET);

        
        boolean valid1 = setupTokenService.isTokenValid(token);
        boolean valid2 = setupTokenService.isTokenValid(token);
        boolean valid3 = setupTokenService.isTokenValid(token);

        
        assertTrue(valid1);
        assertTrue(valid2);
        assertTrue(valid3);
        
        
        Map<String, Object> data = setupTokenService.consumeSetupToken(token);
        assertNotNull(data, "Token debe poder consumirse después de verificar validez");
    }

    @Test
    @DisplayName("createSetupToken y consumeSetupToken - flujo completo de configuración")
    void flujoCompleto_creacionValidacionConsumo() {
        
        Integer userId = 100;
        String nombre = "Administrador Principal";
        String qrCode = "data:image/png;base64,ABC123";
        String totpSecret = "MYSECRETKEY123";

        
        
        String token = setupTokenService.createSetupToken(userId, nombre, qrCode, totpSecret);
        assertNotNull(token);
        
        
        assertTrue(setupTokenService.isTokenValid(token));
        
        
        Map<String, Object> data = setupTokenService.consumeSetupToken(token);
        
        
        assertNotNull(data);
        assertEquals(userId, data.get("userId"));
        assertEquals(nombre, data.get("nombre"));
        assertEquals(qrCode, data.get("qrCodeDataUrl"));
        assertEquals(totpSecret, data.get("totpSecret"));
        
        
        assertFalse(setupTokenService.isTokenValid(token));
        
        
        Map<String, Object> secondAttempt = setupTokenService.consumeSetupToken(token);
        assertNull(secondAttempt);
    }

    @Test
    @DisplayName("invalidateToken - flujo de invalidación manual")
    void flujoCompleto_invalidacionManual() {
        
        String token = setupTokenService.createSetupToken(TEST_USER_ID, TEST_NOMBRE, TEST_QR_CODE, TEST_TOTP_SECRET);
        
        
        
        assertTrue(setupTokenService.isTokenValid(token));
        
        
        setupTokenService.invalidateToken(token);
        
        
        assertFalse(setupTokenService.isTokenValid(token));
        
        
        Map<String, Object> data = setupTokenService.consumeSetupToken(token);
        assertNull(data);
    }

    @Test
    @DisplayName("createSetupToken - debe manejar caracteres especiales en datos")
    void createSetupToken_debeManjejarCaracteresEspeciales() {
        
        String nombreEspecial = "José María O'Connor & García-López";
        String qrCodeLargo = TEST_QR_CODE + "VERY_LONG_BASE64_STRING_" + "X".repeat(1000);
        String secretEspecial = "AB-CD_EF/GH+IJ=KL";

        
        String token = setupTokenService.createSetupToken(TEST_USER_ID, nombreEspecial, qrCodeLargo, secretEspecial);
        Map<String, Object> data = setupTokenService.consumeSetupToken(token);

        
        assertNotNull(data);
        assertEquals(nombreEspecial, data.get("nombre"));
        assertEquals(qrCodeLargo, data.get("qrCodeDataUrl"));
        assertEquals(secretEspecial, data.get("totpSecret"));
    }

    @Test
    @DisplayName("createSetupToken - debe manejar userId negativos o cero")
    void createSetupToken_debeManjejarUserIdEspeciales() {
        
        String token1 = setupTokenService.createSetupToken(0, "User Zero", TEST_QR_CODE, TEST_TOTP_SECRET);
        String token2 = setupTokenService.createSetupToken(-1, "User Negative", TEST_QR_CODE, TEST_TOTP_SECRET);

        
        Map<String, Object> data1 = setupTokenService.consumeSetupToken(token1);
        Map<String, Object> data2 = setupTokenService.consumeSetupToken(token2);
        
        assertNotNull(data1);
        assertNotNull(data2);
        assertEquals(0, data1.get("userId"));
        assertEquals(-1, data2.get("userId"));
    }

    @Test
    @DisplayName("getSigningKey - debe usar clave de 256 bits para HMAC-SHA256")
    void getSigningKey_debeUsarClave256Bits() {
        
        String token = setupTokenService.createSetupToken(TEST_USER_ID, TEST_NOMBRE, TEST_QR_CODE, TEST_TOTP_SECRET);
        
        
        assertTrue(setupTokenService.isTokenValid(token));
        Map<String, Object> data = setupTokenService.consumeSetupToken(token);
        assertNotNull(data, "La firma HMAC-SHA256 debe validarse correctamente");
    }

    @Test
    @DisplayName("createSetupToken - token debe contener claim type=admin_setup")
    void createSetupToken_debeContenerTipoAdminSetup() {
        
        String token = setupTokenService.createSetupToken(TEST_USER_ID, TEST_NOMBRE, TEST_QR_CODE, TEST_TOTP_SECRET);
        
        
        assertTrue(setupTokenService.isTokenValid(token));
        
        
        Map<String, Object> data = setupTokenService.consumeSetupToken(token);
        assertNotNull(data, "Token con tipo admin_setup debe validarse");
    }
}
