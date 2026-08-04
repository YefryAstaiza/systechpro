package com.systechpro.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncriptadorTest {

    @Test
    void hashNuncaGuardaLaContrasenaEnTextoPlano() {
        String hash = Encriptador.encriptarBCrypt("miClaveSecreta123");
        assertNotNull(hash);
        assertNotEquals("miClaveSecreta123", hash);
    }

    @Test
    void verificaCorrectamenteLaContrasenaCorrecta() {
        String hash = Encriptador.encriptarBCrypt("admin123");
        assertTrue(Encriptador.verificarPassword("admin123", hash));
    }

    @Test
    void rechazaContrasenaIncorrecta() {
        String hash = Encriptador.encriptarBCrypt("admin123");
        assertFalse(Encriptador.verificarPassword("otraClave", hash));
    }

    @Test
    void doHashesDelMismoTextoSonDistintos() {
        // BCrypt usa un salt aleatorio por llamada, dos hashes del mismo texto no deben coincidir
        String hash1 = Encriptador.encriptarBCrypt("repetida");
        String hash2 = Encriptador.encriptarBCrypt("repetida");
        assertNotEquals(hash1, hash2);
        assertTrue(Encriptador.verificarPassword("repetida", hash1));
        assertTrue(Encriptador.verificarPassword("repetida", hash2));
    }

    @Test
    void manejaEntradasNulasSinLanzarExcepcion() {
        assertFalse(Encriptador.verificarPassword(null, "algunHash"));
        assertFalse(Encriptador.verificarPassword("algo", null));
    }
}
