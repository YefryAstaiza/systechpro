package com.systechpro.models;

public enum EstadoDispositivo {
    DISPONIBLE,
    EN_USO,
    MANTENIMIENTO;

    public static boolean esValido(String valor) {
        return valorDe(valor) != null;
    }

    public static EstadoDispositivo valorDe(String valor) {
        if (valor == null) return null;
        for (EstadoDispositivo estado : values()) {
            if (estado.name().equals(valor)) return estado;
        }
        return null;
    }
}
