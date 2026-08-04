package com.systechpro.models;

public enum EstadoPrestamo {
    PENDIENTE,
    APROBADO,
    RECHAZADO,
    DEVUELTO;

    public static boolean esValido(String valor) {
        return valorDe(valor) != null;
    }

    public static EstadoPrestamo valorDe(String valor) {
        if (valor == null) return null;
        for (EstadoPrestamo estado : values()) {
            if (estado.name().equals(valor)) return estado;
        }
        return null;
    }
}
