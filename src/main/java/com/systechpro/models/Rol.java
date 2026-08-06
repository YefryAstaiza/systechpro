package com.systechpro.models;

public enum Rol {
    ADMINISTRADOR,
    DOCENTE,
    TECNICO,
    ADMINISTRATIVO,
    MONITOR;

    public static boolean esValido(String valor) {
        return valorDe(valor) != null;
    }

    public static Rol valorDe(String valor) {
        if (valor == null) return null;
        for (Rol rol : values()) {
            if (rol.name().equals(valor)) return rol;
        }
        return null;
    }
}
