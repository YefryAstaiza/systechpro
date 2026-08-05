package com.systechpro.utils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Parseo compartido de parámetros de paginación y rango de fechas para los
 * endpoints de búsqueda/listado (evita repetir esta lógica en cada controller).
 */
public class PaginacionUtil {
    private static final int TAMANO_DEFECTO = 10;
    private static final int TAMANO_MAXIMO = 50;

    public static int parsePagina(String valor) {
        try {
            int pagina = Integer.parseInt(valor);
            return pagina < 1 ? 1 : pagina;
        } catch (Exception e) {
            return 1;
        }
    }

    /** Nunca deja pasar un tamaño de página mayor al máximo permitido (evita abuso). */
    public static int parseTamano(String valor) {
        try {
            int tamano = Integer.parseInt(valor);
            if (tamano < 1) return TAMANO_DEFECTO;
            return Math.min(tamano, TAMANO_MAXIMO);
        } catch (Exception e) {
            return TAMANO_DEFECTO;
        }
    }

    /** Interpreta "YYYY-MM-DD" como el inicio de ese día (00:00:00). Null si no viene o es inválido. */
    public static Timestamp parseFechaInicioDia(String valor) {
        if (valor == null || valor.isEmpty()) return null;
        try {
            return Timestamp.valueOf(LocalDate.parse(valor).atStartOfDay());
        } catch (Exception e) {
            return null;
        }
    }

    /** Interpreta "YYYY-MM-DD" como el final de ese día (23:59:59). Null si no viene o es inválido. */
    public static Timestamp parseFechaFinDia(String valor) {
        if (valor == null || valor.isEmpty()) return null;
        try {
            return Timestamp.valueOf(LocalDateTime.of(LocalDate.parse(valor), java.time.LocalTime.MAX));
        } catch (Exception e) {
            return null;
        }
    }
}
