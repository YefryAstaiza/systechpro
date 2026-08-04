package com.systechpro.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorTextoTest {

    @Test
    void rechazaTextoNulo() {
        assertFalse(ValidadorTexto.esTextoLibreValido(null));
    }

    @Test
    void rechazaTextoDemasiadoCorto() {
        assertFalse(ValidadorTexto.esTextoLibreValido("abc"));
    }

    @Test
    void rechazaTextoDemasiadoLargo() {
        assertFalse(ValidadorTexto.esTextoLibreValido("a".repeat(81)));
    }

    @Test
    void rechazaSoloNumeros() {
        assertFalse(ValidadorTexto.esTextoLibreValido("123456"));
    }

    @Test
    void rechazaCaracteresExtranos() {
        assertFalse(ValidadorTexto.esTextoLibreValido("<script>alert(1)</script>"));
    }

    @Test
    void rechazaMuyPocasLetrasReales() {
        // Solo una letra real entre símbolos: no alcanza el mínimo de 2
        assertFalse(ValidadorTexto.esTextoLibreValido("a-----"));
    }

    @Test
    void aceptaNombrePersonaValido() {
        assertTrue(ValidadorTexto.esTextoLibreValido("Ana Tulande"));
    }

    @Test
    void aceptaNombreConAcentosYEnie() {
        assertTrue(ValidadorTexto.esTextoLibreValido("Peña Muñoz"));
    }

    @Test
    void aceptaNombreConNumeroSiHaySeparador() {
        assertTrue(ValidadorTexto.esTextoLibreValido("Laptop Dell 3"));
    }

    @Test
    void descripcionVaciaEsValida() {
        assertTrue(ValidadorTexto.esDescripcionValida(null));
        assertTrue(ValidadorTexto.esDescripcionValida(""));
    }

    @Test
    void descripcionDemasiadoLargaEsInvalida() {
        assertFalse(ValidadorTexto.esDescripcionValida("a".repeat(251)));
    }

    @Test
    void descripcionConCaracteresExtranosEsInvalida() {
        assertFalse(ValidadorTexto.esDescripcionValida("<img src=x onerror=alert(1)>"));
    }

    @Test
    void descripcionNormalEsValida() {
        assertTrue(ValidadorTexto.esDescripcionValida("Laptop para clases, sala 101 (piso 2)."));
    }
}
