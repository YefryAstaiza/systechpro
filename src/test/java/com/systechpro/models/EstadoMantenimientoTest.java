package com.systechpro.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstadoMantenimientoTest {

    @Test
    void losDosEstadosSonValidos() {
        assertTrue(EstadoMantenimiento.esValido("EN_PROCESO"));
        assertTrue(EstadoMantenimiento.esValido("FINALIZADO"));
    }

    @Test
    void rechazaEstadosQueNuncaExistieron() {
        // "COMPLETADO" era el usado por el frontend muerto (app.js, eliminado en Fase 3).
        assertFalse(EstadoMantenimiento.esValido("COMPLETADO"));
        assertFalse(EstadoMantenimiento.esValido(null));
    }
}
