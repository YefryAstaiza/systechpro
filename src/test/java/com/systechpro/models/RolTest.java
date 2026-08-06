package com.systechpro.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RolTest {

    @Test
    void losCincoRolesDelSistemaSonValidos() {
        assertTrue(Rol.esValido("ADMINISTRADOR"));
        assertTrue(Rol.esValido("DOCENTE"));
        assertTrue(Rol.esValido("TECNICO"));
        assertTrue(Rol.esValido("ADMINISTRATIVO"));
        assertTrue(Rol.esValido("MONITOR"));
    }

    @Test
    void rechazaRolDesconocido() {
        assertFalse(Rol.esValido("SUPERADMIN"));
        assertFalse(Rol.esValido(""));
        assertFalse(Rol.esValido(null));
    }

    @Test
    void esSensibleAMayusculas() {
        // El resto del sistema compara con .equals() contra Rol.X.name(), en mayúsculas exactas
        assertFalse(Rol.esValido("administrador"));
    }

    @Test
    void valorDeDevuelveElEnumCorrecto() {
        assertEquals(Rol.ADMINISTRADOR, Rol.valorDe("ADMINISTRADOR"));
        assertNull(Rol.valorDe("otro"));
    }
}
