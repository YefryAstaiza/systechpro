package com.systechpro.utils;

public class ValidadorTexto {

    /**
     * Valida nombres/textos libres ingresados por el usuario (nombre de usuario, nombre de
     * dispositivo, etc.): longitud razonable, sin caracteres extraños y con letras reales.
     */
    public static boolean esTextoLibreValido(String texto) {
        if (texto == null) return false;
        String valor = texto.trim();
        if (valor.length() < 5 || valor.length() > 80) return false;
        if (!valor.matches("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ0-9 .,_\\-\\(\\)\\/]+$")) return false;
        if (!valor.matches(".*[A-Za-zÁÉÍÓÚÜÑáéíóúüñ].*")) return false;
        if (valor.matches("^\\d+$")) return false;
        if (valor.matches(".*[0-9].*") && !valor.matches(".*[ \\-_\\/].*")) return false;
        if (valor.replaceAll("[^A-Za-zÁÉÍÓÚÜÑáéíóúüñ]", "").length() < 2) return false;
        return true;
    }

    /**
     * Valida descripciones libres (opcionales): longitud máxima y sin caracteres extraños.
     */
    public static boolean esDescripcionValida(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) return true;
        String texto = descripcion.trim();
        if (texto.length() > 250) return false;
        return texto.matches("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ0-9 .,_;:\\-\\(\\)\\[\\]\\/\\n\\r]*$");
    }
}
