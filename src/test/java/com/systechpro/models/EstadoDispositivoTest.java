package com.systechpro.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstadoDispositivoTest {

    @Test
    void losTresEstadosDelSistemaSonValidos() {
        assertTrue(EstadoDispositivo.esValido("DISPONIBLE"));
        assertTrue(EstadoDispositivo.esValido("EN_USO"));
        assertTrue(EstadoDispositivo.esValido("MANTENIMIENTO"));
    }

    @Test
    void rechazaEstadosQueNuncaExistieron() {
        // "PRESTADO" era el usado por el frontend muerto (app.js, eliminado en Fase 3);
        // nunca fue un estado real de dispositivo.
        assertFalse(EstadoDispositivo.esValido("PRESTADO"));
        assertFalse(EstadoDispositivo.esValido(null));
    }
}
