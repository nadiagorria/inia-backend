package utec.proyectofinal.Proyecto.Final.UTEC.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class FiltroJWTAutorizacion extends OncePerRequestFilter {

    private final String CLAVE = "@Z9@vQ3!pL8#wX7^tR2&nG6*yM4$eB1(dF0)sH5%kJ3&uY8*rE4#wQ1@zX6^nM9$";
    private final SecretKey secretKey = Keys.hmacShaKeyFor(CLAVE.getBytes(StandardCharsets.UTF_8));
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extraerToken(request);
            if (token != null && !token.isEmpty()) {
                Claims claims = validarToken(token);
                if (claims.get("authorities") != null) {
                    crearAutenticacion(claims);
                } else {
                    SecurityContextHolder.clearContext();
                }
            } else {
                SecurityContextHolder.clearContext();
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException ex) {
            // Log del error para debugging
            System.err.println("Error JWT: " + ex.getMessage());
            SecurityContextHolder.clearContext();
            // Continuar con el filtro para que Spring Security maneje la autenticación fallida
            filterChain.doFilter(request, response);
            return;
        }
    }

    private void crearAutenticacion(Claims claims) {
        @SuppressWarnings("unchecked")
        List<String> autorizaciones = (List<String>) claims.get("authorities");
        
        
        // Agregar prefijo ROLE_ para que funcione con hasRole()
        List<SimpleGrantedAuthority> authorities = autorizaciones.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        
        
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    /**
     * Extrae el token JWT desde cookies (preferible) o desde el header Authorization (fallback).
     */
    private String extraerToken(HttpServletRequest request) {
        
        // 1) Intentar obtener desde cookie HttpOnly (método seguro)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        } else {
            System.out.println("No se encontraron cookies en la solicitud.");
        }
        
        // 2) Fallback: obtener desde header Authorization (compatibilidad temporal)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.replace("Bearer ", "");
        }
        
        return null;
    }

    private Claims validarToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
