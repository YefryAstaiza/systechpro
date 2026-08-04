package com.systechpro.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Agrega cabeceras de seguridad básicas a todas las respuestas. La CSP permite
 * explícitamente los CDN que ya usa la aplicación (Chart.js, SheetJS, Lucide,
 * Google Fonts) y 'unsafe-inline' en script/style porque el frontend actual
 * todavía depende de atributos onclick="" y estilos inline; una CSP más
 * estricta requeriría eliminarlos primero (ver AUDITORIA_TECNICA.md).
 */
@WebFilter("/*")
public class SecurityHeadersFilter implements Filter {

    private static final String CSP =
        "default-src 'self'; " +
        "script-src 'self' 'unsafe-inline' https://unpkg.com https://cdn.jsdelivr.net; " +
        "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
        "font-src 'self' https://fonts.gstatic.com; " +
        "img-src 'self' data:; " +
        "connect-src 'self'; " +
        "object-src 'none'; " +
        "base-uri 'self'; " +
        "frame-ancestors 'none';";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (response instanceof HttpServletResponse res) {
            res.setHeader("X-Content-Type-Options", "nosniff");
            res.setHeader("X-Frame-Options", "DENY");
            res.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            res.setHeader("Content-Security-Policy", CSP);
        }
        chain.doFilter(request, response);
    }
}
