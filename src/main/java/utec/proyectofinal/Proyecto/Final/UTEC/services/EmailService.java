package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String INIA_GREEN = "#2d5016";
    private static final String INIA_LIGHT_GREEN = "#4a7c2c";
    private static final String INIA_YELLOW = "#f4c430";
    private static final String INIA_GRAY = "#f5f5f5";

    
    public void enviarEmailNuevoRegistro(String analistaEmail, String analistaNombre, 
                                        String usuarioNombre, String usuarioEmail) {
        String asunto = "Nuevo Usuario Registrado - Sistema INIA";
        
        String contenidoHtml = "<!DOCTYPE html>" +
            "<html lang='es'>" +
            "<head>" +
            "    <meta charset='UTF-8'>" +
            "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "    <title>Nuevo Registro INIA</title>" +
            "</head>" +
            "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f5f5f5;'>" +
            "    <table role='presentation' cellpadding='0' cellspacing='0' width='100%' style='background-color: #f5f5f5; padding: 20px;'>" +
            "        <tr>" +
            "            <td align='center'>" +
            "                <table role='presentation' cellpadding='0' cellspacing='0' width='600' style='background-color: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>" +
            "                    " +
            "                    <!-- Header -->" +
            "                    <tr>" +
            "                        <td style='background: linear-gradient(135deg, " + INIA_GREEN + " 0%, " + INIA_LIGHT_GREEN + " 100%); padding: 40px 30px; text-align: center;'>" +
            "                            <h1 style='margin: 0; color: white; font-size: 28px; font-weight: bold;'>INIA</h1>" +
            "                            <p style='margin: 10px 0 0 0; color: " + INIA_YELLOW + "; font-size: 14px;'>Instituto Nacional de Investigación Agropecuaria</p>" +
            "                        </td>" +
            "                    </tr>" +
            "                    " +
            "                    <!-- Body -->" +
            "                    <tr>" +
            "                        <td style='padding: 40px 30px;'>" +
            "                            <h2 style='margin: 0 0 20px 0; color: " + INIA_GREEN + "; font-size: 22px;'>Hola " + analistaNombre + ",</h2>" +
            "                            " +
            "                            <p style='margin: 0 0 15px 0; color: #333; font-size: 16px; line-height: 1.6;'>" +
            "                                Te informamos que un nuevo usuario se ha registrado en el <strong>Sistema INIA</strong> y requiere aprobación para acceder a la plataforma." +
            "                            </p>" +
            "                            " +
            "                            <div style='background-color: " + INIA_GRAY + "; border-left: 4px solid " + INIA_GREEN + "; padding: 20px; margin: 25px 0; border-radius: 4px;'>" +
            "                                <p style='margin: 0 0 10px 0; color: #666; font-size: 14px; font-weight: bold;'>DATOS DEL USUARIO:</p>" +
            "                                <p style='margin: 5px 0; color: #333; font-size: 15px;'><strong>Nombre:</strong> " + usuarioNombre + "</p>" +
            "                                <p style='margin: 5px 0; color: #333; font-size: 15px;'><strong>Email:</strong> " + usuarioEmail + "</p>" +
            "                            </div>" +
            "                            " +
            "                            <p style='margin: 20px 0 30px 0; color: #333; font-size: 16px; line-height: 1.6;'>" +
            "                                Por favor, ingresa al sistema para revisar y aprobar esta solicitud." +
            "                            </p>" +
            "                            " +
            "                            <div style='text-align: center;'>" +
            "                                <a href='http://localhost:3000/administracion/usuarios' " +
            "                                   style='display: inline-block; background: linear-gradient(135deg, " + INIA_GREEN + " 0%, " + INIA_LIGHT_GREEN + " 100%); " +
            "                                   color: white; text-decoration: none; padding: 14px 40px; border-radius: 6px; " +
            "                                   font-weight: bold; font-size: 16px; box-shadow: 0 2px 4px rgba(0,0,0,0.2);'>" +
            "                                    Revisar Solicitud" +
            "                                </a>" +
            "                            </div>" +
            "                        </td>" +
            "                    </tr>" +
            "                    " +
            "                    <!-- Footer -->" +
            "                    <tr>" +
            "                        <td style='background-color: " + INIA_GRAY + "; padding: 30px; text-align: center; border-top: 1px solid #ddd;'>" +
            "                            <p style='margin: 0 0 10px 0; color: #666; font-size: 13px;'>" +
            "                                Este es un correo automático del Sistema INIA." +
            "                            </p>" +
            "                            <p style='margin: 0; color: #999; font-size: 12px;'>" +
            "                                © 2025 INIA - Instituto Nacional de Investigación Agropecuaria." +
            "                            </p>" +
            "                        </td>" +
            "                    </tr>" +
            "                </table>" +
            "            </td>" +
            "        </tr>" +
            "    </table>" +
            "</body>" +
            "</html>";

        enviarEmail(analistaEmail, asunto, contenidoHtml);
    }

    
    public void enviarEmailConfirmacionRegistro(String usuarioEmail, String usuarioNombre) {
        String asunto = "Registro Exitoso - Sistema INIA";
        
        String contenidoHtml = "<!DOCTYPE html>" +
            "<html lang='es'>" +
            "<head>" +
            "    <meta charset='UTF-8'>" +
            "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "    <title>Registro Exitoso INIA</title>" +
            "</head>" +
            "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f5f5f5;'>" +
            "    <table role='presentation' cellpadding='0' cellspacing='0' width='100%' style='background-color: #f5f5f5; padding: 20px;'>" +
            "        <tr>" +
            "            <td align='center'>" +
            "                <table role='presentation' cellpadding='0' cellspacing='0' width='600' style='background-color: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>" +
            "                    " +
            "                    <!-- Header -->" +
            "                    <tr>" +
            "                        <td style='background: linear-gradient(135deg, " + INIA_GREEN + " 0%, " + INIA_LIGHT_GREEN + " 100%); padding: 40px 30px; text-align: center;'>" +
            "                            <h1 style='margin: 0; color: white; font-size: 28px; font-weight: bold;'>INIA</h1>" +
            "                            <p style='margin: 10px 0 0 0; color: " + INIA_YELLOW + "; font-size: 14px;'>Instituto Nacional de Investigación Agropecuaria</p>" +
            "                        </td>" +
            "                    </tr>" +
            "                    " +
            "                    <!-- Body -->" +
            "                    <tr>" +
            "                        <td style='padding: 40px 30px;'>" +
            "                            <div style='text-align: center; margin-bottom: 30px;'>" +
            "                                <div style='display: inline-block; background-color: " + INIA_YELLOW + "; width: 80px; height: 80px; border-radius: 50%; " +
            "                                     line-height: 80px; font-size: 40px;'>✓</div>" +
            "                            </div>" +
            "                            " +
            "                            <h2 style='margin: 0 0 20px 0; color: " + INIA_GREEN + "; font-size: 24px; text-align: center;'>¡Registro Exitoso!</h2>" +
            "                            " +
            "                            <p style='margin: 0 0 15px 0; color: #333; font-size: 16px; line-height: 1.6;'>" +
            "                                Estimado/a <strong>" + usuarioNombre + "</strong>," +
            "                            </p>" +
            "                            " +
            "                            <p style='margin: 0 0 15px 0; color: #333; font-size: 16px; line-height: 1.6;'>" +
            "                                Te has registrado exitosamente en el <strong>Sistema INIA</strong>." +
            "                            </p>" +
            "                            " +
            "                            <div style='background-color: #fff3cd; border-left: 4px solid " + INIA_YELLOW + "; padding: 20px; margin: 25px 0; border-radius: 4px;'>" +
            "                                <p style='margin: 0; color: #856404; font-size: 15px; line-height: 1.6;'>" +
            "                                    <strong>Pendiente de Aprobación</strong><br>" +
            "                                    Tu solicitud está siendo revisada por nuestro equipo. Te notificaremos por correo cuando tu cuenta sea aprobada." +
            "                                </p>" +
            "                            </div>" +
            "                            " +
            "                            <p style='margin: 20px 0; color: #333; font-size: 16px; line-height: 1.6;'>" +
            "                                Una vez aprobada tu cuenta, podrás acceder a todas las funcionalidades del sistema." +
            "                            </p>" +
            "                            " +
            "                            <p style='margin: 0; color: #666; font-size: 14px; line-height: 1.6;'>" +
            "                                Si tienes alguna pregunta, no dudes en contactarnos." +
            "                            </p>" +
            "                        </td>" +
            "                    </tr>" +
            "                    " +
            "                    <!-- Footer -->" +
            "                    <tr>" +
            "                        <td style='background-color: " + INIA_GRAY + "; padding: 30px; text-align: center; border-top: 1px solid #ddd;'>" +
            "                            <p style='margin: 0 0 10px 0; color: #666; font-size: 13px;'>" +
            "                                Este es un correo automático del Sistema INIA." +
            "                            </p>" +
            "                            <p style='margin: 0; color: #999; font-size: 12px;'>" +
            "                                © 2025 INIA - Instituto Nacional de Investigación Agropecuaria." +
            "                            </p>" +
            "                        </td>" +
            "                    </tr>" +
            "                </table>" +
            "            </td>" +
            "        </tr>" +
            "    </table>" +
            "</body>" +
            "</html>";

        enviarEmail(usuarioEmail, asunto, contenidoHtml);
    }

    
    public void enviarEmailBienvenida(String usuarioEmail, String usuarioNombre) {
        String asunto = "¡Bienvenido al Sistema INIA!";
        
        String contenidoHtml = "<!DOCTYPE html>" +
            "<html lang='es'>" +
            "<head>" +
            "    <meta charset='UTF-8'>" +
            "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "    <title>Bienvenida INIA</title>" +
            "</head>" +
            "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f5f5f5;'>" +
            "    <table role='presentation' cellpadding='0' cellspacing='0' width='100%' style='background-color: #f5f5f5; padding: 20px;'>" +
            "        <tr>" +
            "            <td align='center'>" +
            "                <table role='presentation' cellpadding='0' cellspacing='0' width='600' style='background-color: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>" +
            "                    " +
            "                    <!-- Header -->" +
            "                    <tr>" +
            "                        <td style='background: linear-gradient(135deg, " + INIA_GREEN + " 0%, " + INIA_LIGHT_GREEN + " 100%); padding: 40px 30px; text-align: center;'>" +
            "                            <h1 style='margin: 0; color: white; font-size: 28px; font-weight: bold;'>INIA</h1>" +
            "                            <p style='margin: 10px 0 0 0; color: " + INIA_YELLOW + "; font-size: 14px;'>Instituto Nacional de Investigación Agropecuaria</p>" +
            "                        </td>" +
            "                    </tr>" +
            "                    " +
            "                    <!-- Body -->" +
            "                    <tr>" +
            "                        <td style='padding: 40px 30px;'>" +
            "                            <div style='text-align: center; margin-bottom: 30px;'>" +
            "                                <div style='display: inline-block; background: linear-gradient(135deg, " + INIA_GREEN + " 0%, " + INIA_LIGHT_GREEN + " 100%); " +
            "                                     width: 80px; height: 80px; border-radius: 50%; line-height: 80px; font-size: 40px; color: white;'>🎉</div>" +
            "                            </div>" +
            "                            " +
            "                            <h2 style='margin: 0 0 20px 0; color: " + INIA_GREEN + "; font-size: 26px; text-align: center;'>¡Bienvenido al Sistema INIA!</h2>" +
            "                            " +
            "                            <p style='margin: 0 0 20px 0; color: #333; font-size: 16px; line-height: 1.6;'>" +
            "                                Estimado/a <strong>" + usuarioNombre + "</strong>," +
            "                            </p>" +
            "                            " +
            "                            <div style='background-color: #d4edda; border-left: 4px solid #28a745; padding: 20px; margin: 25px 0; border-radius: 4px;'>" +
            "                                <p style='margin: 0; color: #155724; font-size: 15px; line-height: 1.6;'>" +
            "                                    <strong> Cuenta Aprobada</strong><br>" +
            "                                    ¡Felicitaciones! Tu cuenta ha sido aprobada y ya puedes acceder al Sistema INIA." +
            "                                </p>" +
            "                            </div>" +
            "                            " +
            "                            <p style='margin: 20px 0; color: #333; font-size: 16px; line-height: 1.6;'>" +
            "                                Ahora tienes acceso a nuestra plataforma de gestión agropecuaria." +
            "                            </p>" +
            "                            <div style='text-align: center; margin: 30px 0;'>" +
            "                                <a href='http://localhost:3000/login' " +
            "                                   style='display: inline-block; background: linear-gradient(135deg, " + INIA_GREEN + " 0%, " + INIA_LIGHT_GREEN + " 100%); " +
            "                                   color: white; text-decoration: none; padding: 14px 40px; border-radius: 6px; " +
            "                                   font-weight: bold; font-size: 16px; box-shadow: 0 2px 4px rgba(0,0,0,0.2);'>" +
            "                                    Iniciar Sesión" +
            "                                </a>" +
            "                            </div>" +
            "                            " +
            "                        </td>" +
            "                    </tr>" +
            "                    " +
            "                    <!-- Footer -->" +
            "                    <tr>" +
            "                        <td style='background-color: " + INIA_GRAY + "; padding: 30px; text-align: center; border-top: 1px solid #ddd;'>" +
            "                            <p style='margin: 0 0 10px 0; color: #666; font-size: 13px;'>" +
            "                                Este es un correo automático del Sistema INIA." +
            "                            </p>" +
            "                            <p style='margin: 0; color: #999; font-size: 12px;'>" +
            "                                © 2025 INIA - Instituto Nacional de Investigación Agropecuaria" +
            "                            </p>" +
            "                        </td>" +
            "                    </tr>" +
            "                </table>" +
            "            </td>" +
            "        </tr>" +
            "    </table>" +
            "</body>" +
            "</html>";

        enviarEmail(usuarioEmail, asunto, contenidoHtml);
    }

    
    public void enviarCodigoRecuperacion(String usuarioEmail, String usuarioNombre, String codigoRecuperacion) {
        String asunto = "Código de Recuperación - Sistema INIA";
        
        String contenidoHtml = "<!DOCTYPE html>" +
            "<html lang='es'>" +
            "<head>" +
            "    <meta charset='UTF-8'>" +
            "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "    <title>Código de Recuperación INIA</title>" +
            "</head>" +
            "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f5f5f5;'>" +
            "    <table role='presentation' cellpadding='0' cellspacing='0' width='100%' style='background-color: #f5f5f5; padding: 20px;'>" +
            "        <tr>" +
            "            <td align='center'>" +
            "                <table role='presentation' cellpadding='0' cellspacing='0' width='600' style='background-color: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>" +
            "                    " +
            "                    <!-- Header -->" +
            "                    <tr>" +
            "                        <td style='background: linear-gradient(135deg, " + INIA_GREEN + " 0%, " + INIA_LIGHT_GREEN + " 100%); padding: 40px 30px; text-align: center;'>" +
            "                            <h1 style='margin: 0; color: white; font-size: 28px; font-weight: bold;'>INIA</h1>" +
            "                            <p style='margin: 10px 0 0 0; color: " + INIA_YELLOW + "; font-size: 14px;'>Instituto Nacional de Investigación Agropecuaria</p>" +
            "                        </td>" +
            "                    </tr>" +
            "                    " +
            "                    <!-- Body -->" +
            "                    <tr>" +
            "                        <td style='padding: 40px 30px;'>" +
            "                            <div style='text-align: center; margin-bottom: 30px;'>" +
            "                                <div style='display: inline-block; background-color: #ff9800; width: 80px; height: 80px; border-radius: 50%; " +
            "                                     line-height: 80px; font-size: 40px; color: white;'>🔐</div>" +
            "                            </div>" +
            "                            " +
            "                            <h2 style='margin: 0 0 20px 0; color: " + INIA_GREEN + "; font-size: 24px; text-align: center;'>Recuperación de Contraseña</h2>" +
            "                            " +
            "                            <p style='margin: 0 0 20px 0; color: #333; font-size: 16px; line-height: 1.6;'>" +
            "                                Hola <strong>" + usuarioNombre + "</strong>," +
            "                            </p>" +
            "                            " +
            "                            <p style='margin: 0 0 20px 0; color: #333; font-size: 16px; line-height: 1.6;'>" +
            "                                Recibimos una solicitud para restablecer tu contraseña. Usa el siguiente código:" +
            "                            </p>" +
            "                            " +
            "                            <div style='background-color: " + INIA_GRAY + "; border: 2px dashed " + INIA_GREEN + "; padding: 25px; margin: 30px 0; border-radius: 8px; text-align: center;'>" +
            "                                <p style='margin: 0 0 10px 0; color: #666; font-size: 14px; font-weight: bold;'>TU CÓDIGO DE RECUPERACIÓN:</p>" +
            "                                <p style='margin: 0; color: " + INIA_GREEN + "; font-size: 32px; font-weight: bold; letter-spacing: 8px; font-family: monospace;'>" + codigoRecuperacion + "</p>" +
            "                            </div>" +
            "                            " +
            "                            <div style='background-color: #fff3cd; border-left: 4px solid #ff9800; padding: 20px; margin: 25px 0; border-radius: 4px;'>" +
            "                                <p style='margin: 0 0 10px 0; color: #856404; font-size: 15px; line-height: 1.6;'>" +
            "                                    <strong>⏱️ IMPORTANTE:</strong><br>" +
            "                                    Este código es válido solo por <strong>10 minutos</strong>." +
            "                                </p>" +
            "                                <p style='margin: 10px 0 0 0; color: #856404; font-size: 15px; line-height: 1.6;'>" +
            "                                    <strong>🔐 SEGURIDAD:</strong><br>" +
            "                                    También necesitarás tu código de Google Authenticator para completar el cambio de contraseña." +
            "                                </p>" +
            "                            </div>" +
            "                            " +
            "                            <div style='background-color: #f8d7da; border-left: 4px solid #dc3545; padding: 20px; margin: 25px 0; border-radius: 4px;'>" +
            "                                <p style='margin: 0; color: #721c24; font-size: 14px; line-height: 1.6;'>" +
            "                                    <strong>¿No solicitaste este código?</strong><br>" +
            "                                    Si no fuiste tú, ignora este correo. Tu contraseña permanecerá segura." +
            "                                </p>" +
            "                            </div>" +
            "                            " +
            "                        </td>" +
            "                    </tr>" +
            "                    " +
            "                    <!-- Footer -->" +
            "                    <tr>" +
            "                        <td style='background-color: " + INIA_GRAY + "; padding: 30px; text-align: center; border-top: 1px solid #ddd;'>" +
            "                            <p style='margin: 0 0 10px 0; color: #666; font-size: 13px;'>" +
            "                                Este es un correo automático del Sistema INIA." +
            "                            </p>" +
            "                            <p style='margin: 0; color: #999; font-size: 12px;'>" +
            "                                © 2025 INIA - Instituto Nacional de Investigación Agropecuaria" +
            "                            </p>" +
            "                        </td>" +
            "                    </tr>" +
            "                </table>" +
            "            </td>" +
            "        </tr>" +
            "    </table>" +
            "</body>" +
            "</html>";

        enviarEmail(usuarioEmail, asunto, contenidoHtml);
    }

    
    public void enviar2FAActivado(String usuarioEmail, String usuarioNombre) {
        String asunto = "Autenticación de Dos Factores Activada - Sistema INIA";
        
        String contenidoHtml = "<!DOCTYPE html>" +
            "<html lang='es'>" +
            "<head>" +
            "    <meta charset='UTF-8'>" +
            "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "    <title>2FA Activado INIA</title>" +
            "</head>" +
            "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f5f5f5;'>" +
            "    <table role='presentation' cellpadding='0' cellspacing='0' width='100%' style='background-color: #f5f5f5; padding: 20px;'>" +
            "        <tr>" +
            "            <td align='center'>" +
            "                <table role='presentation' cellpadding='0' cellspacing='0' width='600' style='background-color: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>" +
            "                    " +
            "                    <!-- Header -->" +
            "                    <tr>" +
            "                        <td style='background: linear-gradient(135deg, " + INIA_GREEN + " 0%, " + INIA_LIGHT_GREEN + " 100%); padding: 40px 30px; text-align: center;'>" +
            "                            <h1 style='margin: 0; color: white; font-size: 28px; font-weight: bold;'>INIA</h1>" +
            "                            <p style='margin: 10px 0 0 0; color: " + INIA_YELLOW + "; font-size: 14px;'>Instituto Nacional de Investigación Agropecuaria</p>" +
            "                        </td>" +
            "                    </tr>" +
            "                    " +
            "                    <!-- Body -->" +
            "                    <tr>" +
            "                        <td style='padding: 40px 30px;'>" +
            "                            <div style='text-align: center; margin-bottom: 30px;'>" +
            "                                <div style='display: inline-block; background-color: #28a745; width: 80px; height: 80px; border-radius: 50%; " +
            "                                     line-height: 80px; font-size: 40px; color: white;'>🛡️</div>" +
            "                            </div>" +
            "                            " +
            "                            <h2 style='margin: 0 0 20px 0; color: " + INIA_GREEN + "; font-size: 24px; text-align: center;'>Seguridad Mejorada</h2>" +
            "                            " +
            "                            <p style='margin: 0 0 20px 0; color: #333; font-size: 16px; line-height: 1.6;'>" +
            "                                Hola <strong>" + usuarioNombre + "</strong>," +
            "                            </p>" +
            "                            " +
            "                            <div style='background-color: #d4edda; border-left: 4px solid #28a745; padding: 20px; margin: 25px 0; border-radius: 4px;'>" +
            "                                <p style='margin: 0; color: #155724; font-size: 15px; line-height: 1.6;'>" +
            "                                    <strong>✅ Autenticación de Dos Factores Activada</strong><br>" +
            "                                    Has activado exitosamente la autenticación de dos factores (2FA) en tu cuenta." +
            "                                </p>" +
            "                            </div>" +
            "                            " +
            "                            <p style='margin: 20px 0; color: #333; font-size: 16px; line-height: 1.6;'>" +
            "                                Tu cuenta ahora está protegida con una capa adicional de seguridad:" +
            "                            </p>" +
            "                            " +
            "                            <ul style='color: #333; font-size: 15px; line-height: 1.8; margin: 20px 0;'>" +
            "                                <li>Necesitarás tu código de Google Authenticator para iniciar sesión en nuevos dispositivos</li>" +
            "                                <li>Los dispositivos de confianza no requerirán 2FA por 60 días</li>" +
            "                                <li>Se requiere 2FA para recuperación de contraseña</li>" +
            "                            </ul>" +
            "                            " +
            "                            <div style='background-color: #fff3cd; border-left: 4px solid #ff9800; padding: 20px; margin: 25px 0; border-radius: 4px;'>" +
            "                                <p style='margin: 0; color: #856404; font-size: 14px; line-height: 1.6;'>" +
            "                                    <strong>⚠️ ¿No fuiste tú?</strong><br>" +
            "                                    Si no activaste 2FA, contacta inmediatamente al administrador." +
            "                                </p>" +
            "                            </div>" +
            "                            " +
            "                        </td>" +
            "                    </tr>" +
            "                    " +
            "                    <!-- Footer -->" +
            "                    <tr>" +
            "                        <td style='background-color: " + INIA_GRAY + "; padding: 30px; text-align: center; border-top: 1px solid #ddd;'>" +
            "                            <p style='margin: 0 0 10px 0; color: #666; font-size: 13px;'>" +
            "                                Este es un correo automático del Sistema INIA." +
            "                            </p>" +
            "                            <p style='margin: 0; color: #999; font-size: 12px;'>" +
            "                                © 2025 INIA - Instituto Nacional de Investigación Agropecuaria" +
            "                            </p>" +
            "                        </td>" +
            "                    </tr>" +
            "                </table>" +
            "            </td>" +
            "        </tr>" +
            "    </table>" +
            "</body>" +
            "</html>";

        enviarEmail(usuarioEmail, asunto, contenidoHtml);
    }

    
    public void enviarNuevoDispositivo(String usuarioEmail, String usuarioNombre, String nombreDispositivo, String ipAddress) {
        String asunto = "Nuevo Dispositivo de Confianza - Sistema INIA";
        
        String contenidoHtml = "<!DOCTYPE html>" +
            "<html lang='es'>" +
            "<head>" +
            "    <meta charset='UTF-8'>" +
            "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "    <title>Nuevo Dispositivo INIA</title>" +
            "</head>" +
            "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f5f5f5;'>" +
            "    <table role='presentation' cellpadding='0' cellspacing='0' width='100%' style='background-color: #f5f5f5; padding: 20px;'>" +
            "        <tr>" +
            "            <td align='center'>" +
            "                <table role='presentation' cellpadding='0' cellspacing='0' width='600' style='background-color: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>" +
            "                    " +
            "                    <!-- Header -->" +
            "                    <tr>" +
            "                        <td style='background: linear-gradient(135deg, " + INIA_GREEN + " 0%, " + INIA_LIGHT_GREEN + " 100%); padding: 40px 30px; text-align: center;'>" +
            "                            <h1 style='margin: 0; color: white; font-size: 28px; font-weight: bold;'>INIA</h1>" +
            "                            <p style='margin: 10px 0 0 0; color: " + INIA_YELLOW + "; font-size: 14px;'>Instituto Nacional de Investigación Agropecuaria</p>" +
            "                        </td>" +
            "                    </tr>" +
            "                    " +
            "                    <!-- Body -->" +
            "                    <tr>" +
            "                        <td style='padding: 40px 30px;'>" +
            "                            <div style='text-align: center; margin-bottom: 30px;'>" +
            "                                <div style='display: inline-block; background-color: #17a2b8; width: 80px; height: 80px; border-radius: 50%; " +
            "                                     line-height: 80px; font-size: 40px; color: white;'>📱</div>" +
            "                            </div>" +
            "                            " +
            "                            <h2 style='margin: 0 0 20px 0; color: " + INIA_GREEN + "; font-size: 24px; text-align: center;'>Nuevo Dispositivo de Confianza</h2>" +
            "                            " +
            "                            <p style='margin: 0 0 20px 0; color: #333; font-size: 16px; line-height: 1.6;'>" +
            "                                Hola <strong>" + usuarioNombre + "</strong>," +
            "                            </p>" +
            "                            " +
            "                            <p style='margin: 0 0 20px 0; color: #333; font-size: 16px; line-height: 1.6;'>" +
            "                                Se ha agregado un nuevo dispositivo de confianza a tu cuenta:" +
            "                            </p>" +
            "                            " +
            "                            <div style='background-color: " + INIA_GRAY + "; border-left: 4px solid " + INIA_GREEN + "; padding: 20px; margin: 25px 0; border-radius: 4px;'>" +
            "                                <p style='margin: 0 0 10px 0; color: #666; font-size: 14px; font-weight: bold;'>DETALLES DEL DISPOSITIVO:</p>" +
            "                                <p style='margin: 5px 0; color: #333; font-size: 15px;'><strong>Dispositivo:</strong> " + nombreDispositivo + "</p>" +
            "                                <p style='margin: 5px 0; color: #333; font-size: 15px;'><strong>IP:</strong> " + ipAddress + "</p>" +
            "                            </div>" +
            "                            " +
            "                            <p style='margin: 20px 0; color: #333; font-size: 16px; line-height: 1.6;'>" +
            "                                Este dispositivo no requerirá código 2FA durante los próximos 60 días." +
            "                            </p>" +
            "                            " +
            "                            <div style='background-color: #f8d7da; border-left: 4px solid #dc3545; padding: 20px; margin: 25px 0; border-radius: 4px;'>" +
            "                                <p style='margin: 0; color: #721c24; font-size: 14px; line-height: 1.6;'>" +
            "                                    <strong>⚠️ ¿No reconoces este dispositivo?</strong><br>" +
            "                                    Inicia sesión inmediatamente y revoca este dispositivo desde tu perfil." +
            "                                </p>" +
            "                            </div>" +
            "                            " +
            "                        </td>" +
            "                    </tr>" +
            "                    " +
            "                    <!-- Footer -->" +
            "                    <tr>" +
            "                        <td style='background-color: " + INIA_GRAY + "; padding: 30px; text-align: center; border-top: 1px solid #ddd;'>" +
            "                            <p style='margin: 0 0 10px 0; color: #666; font-size: 13px;'>" +
            "                                Este es un correo automático del Sistema INIA." +
            "                            </p>" +
            "                            <p style='margin: 0; color: #999; font-size: 12px;'>" +
            "                                © 2025 INIA - Instituto Nacional de Investigación Agropecuaria" +
            "                            </p>" +
            "                        </td>" +
            "                    </tr>" +
            "                </table>" +
            "            </td>" +
            "        </tr>" +
            "    </table>" +
            "</body>" +
            "</html>";

        enviarEmail(usuarioEmail, asunto, contenidoHtml);
    }

    
    private void enviarEmail(String destinatario, String asunto, String contenidoHtml) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(fromEmail, "Sistema INIA");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true);

            mailSender.send(mensaje);
            System.out.println("✉️ Email enviado exitosamente a: " + destinatario);

        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar email a " + destinatario + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Error inesperado al enviar email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
