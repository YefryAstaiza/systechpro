package com.systechpro.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginRateLimiterTest {

    @Test
    void correoNuevoNoEstaBloqueado() {
        assertFalse(LoginRateLimiter.estaBloqueado("nuevo1@systechpro.com"));
    }

    @Test
    void seBloqueaTrasCincoFallos() {
        String correo = "bloqueo1@systechpro.com";
        for (int i = 0; i < 5; i++) {
            LoginRateLimiter.registrarFallo(correo);
        }
        assertTrue(LoginRateLimiter.estaBloqueado(correo));
        assertTrue(LoginRateLimiter.segundosRestantesBloqueo(correo) > 0);
    }

    @Test
    void noSeBloqueaConMenosDeCincoFallos() {
        String correo = "bloqueo2@systechpro.com";
        for (int i = 0; i < 4; i++) {
            LoginRateLimiter.registrarFallo(correo);
        }
        assertFalse(LoginRateLimiter.estaBloqueado(correo));
    }

    @Test
    void unLoginExitosoLimpiaLosFallosPrevios() {
        String correo = "bloqueo3@systechpro.com";
        for (int i = 0; i < 4; i++) {
            LoginRateLimiter.registrarFallo(correo);
        }
        LoginRateLimiter.registrarExito(correo);
        assertFalse(LoginRateLimiter.estaBloqueado(correo));

        // Debe poder volver a fallar 4 veces sin bloquearse (el contador se reinició)
        for (int i = 0; i < 4; i++) {
            LoginRateLimiter.registrarFallo(correo);
        }
        assertFalse(LoginRateLimiter.estaBloqueado(correo));
    }

    @Test
    void elBloqueoEsPorCorreoNoGlobal() {
        String correoBloqueado = "bloqueo4@systechpro.com";
        String otroCorreo = "libre4@systechpro.com";
        for (int i = 0; i < 5; i++) {
            LoginRateLimiter.registrarFallo(correoBloqueado);
        }
        assertTrue(LoginRateLimiter.estaBloqueado(correoBloqueado));
        assertFalse(LoginRateLimiter.estaBloqueado(otroCorreo));
    }

    @Test
    void esInsensibleAMayusculasYEspacios() {
        String correo = "MayUs5@systechpro.com";
        for (int i = 0; i < 5; i++) {
            LoginRateLimiter.registrarFallo(correo);
        }
        assertTrue(LoginRateLimiter.estaBloqueado("  mayus5@SYSTECHPRO.com  "));
    }
}
