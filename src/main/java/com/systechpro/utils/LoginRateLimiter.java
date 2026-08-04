package com.systechpro.utils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limitador de intentos de login en memoria, por correo. Tras varios intentos
 * fallidos dentro de una ventana de tiempo, bloquea temporalmente ese correo.
 * No sobrevive a un reinicio del servidor (no hay persistencia ni es necesaria
 * para este propósito).
 */
public class LoginRateLimiter {
    private static final int MAX_INTENTOS = 5;
    private static final long VENTANA_SEGUNDOS = 15 * 60;
    private static final long BLOQUEO_SEGUNDOS = 15 * 60;

    private static final ConcurrentHashMap<String, Intento> intentos = new ConcurrentHashMap<>();

    private static class Intento {
        int fallos = 0;
        Instant primerFallo = Instant.now();
        Instant bloqueadoHasta = null;
    }

    public static boolean estaBloqueado(String correo) {
        Intento i = intentos.get(normalizar(correo));
        if (i == null || i.bloqueadoHasta == null) return false;
        synchronized (i) {
            if (Instant.now().isAfter(i.bloqueadoHasta)) {
                intentos.remove(normalizar(correo));
                return false;
            }
            return true;
        }
    }

    public static long segundosRestantesBloqueo(String correo) {
        Intento i = intentos.get(normalizar(correo));
        if (i == null || i.bloqueadoHasta == null) return 0;
        long restante = Instant.now().until(i.bloqueadoHasta, ChronoUnit.SECONDS);
        return Math.max(restante, 0);
    }

    public static void registrarFallo(String correo) {
        String key = normalizar(correo);
        if (key.isEmpty()) return;
        Intento i = intentos.computeIfAbsent(key, k -> new Intento());
        synchronized (i) {
            if (Instant.now().isAfter(i.primerFallo.plusSeconds(VENTANA_SEGUNDOS))) {
                i.fallos = 0;
                i.primerFallo = Instant.now();
                i.bloqueadoHasta = null;
            }
            i.fallos++;
            if (i.fallos >= MAX_INTENTOS) {
                i.bloqueadoHasta = Instant.now().plusSeconds(BLOQUEO_SEGUNDOS);
            }
        }
    }

    public static void registrarExito(String correo) {
        intentos.remove(normalizar(correo));
    }

    private static String normalizar(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase();
    }
}
