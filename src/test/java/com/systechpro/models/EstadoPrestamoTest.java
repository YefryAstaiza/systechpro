package com.systechpro.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstadoPrestamoTest {

    @Test
    void losCuatroEstadosSonValidosIncluyendoDevuelto() {
        // DEVUELTO fue el schema drift corregido en Fase 2 (database.sql no lo incluía
        // aunque el código ya lo usaba) - se prueba explícitamente para no repetir el bug.
        assertTrue(EstadoPrestamo.esValido("PENDIENTE"));
        assertTrue(EstadoPrestamo.esValido("APROBADO"));
        assertTrue(EstadoPrestamo.esValido("RECHAZADO"));
        assertTrue(EstadoPrestamo.esValido("DEVUELTO"));
    }

    @Test
    void rechazaEstadoInvalido() {
        assertFalse(EstadoPrestamo.esValido("CANCELADO"));
        assertFalse(EstadoPrestamo.esValido(null));
    }
}
