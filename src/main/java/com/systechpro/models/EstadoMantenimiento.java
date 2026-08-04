package com.systechpro.models;

public enum EstadoMantenimiento {
    EN_PROCESO,
    FINALIZADO;

    public static boolean esValido(String valor) {
        return valorDe(valor) != null;
    }

    public static EstadoMantenimiento valorDe(String valor) {
        if (valor == null) return null;
        for (EstadoMantenimiento estado : values()) {
            if (estado.name().equals(valor)) return estado;
        }
        return null;
    }
}
