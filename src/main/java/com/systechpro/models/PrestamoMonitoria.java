package com.systechpro.models;

import java.sql.Timestamp;

public class PrestamoMonitoria {
    private int idMonitoria;
    private int idDispositivo;
    private int idUsuario;
    private Timestamp fechaToma;
    private Timestamp fechaDevolucion;
    private String estado; // ACTIVO, DEVUELTO

    // Campos extra para mostrar sin necesidad de otro fetch
    private String nombreDispositivo;
    private String nombreUsuario;

    public PrestamoMonitoria() {}

    public int getIdMonitoria() { return idMonitoria; }
    public void setIdMonitoria(int idMonitoria) { this.idMonitoria = idMonitoria; }

    public int getIdDispositivo() { return idDispositivo; }
    public void setIdDispositivo(int idDispositivo) { this.idDispositivo = idDispositivo; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public Timestamp getFechaToma() { return fechaToma; }
    public void setFechaToma(Timestamp fechaToma) { this.fechaToma = fechaToma; }

    public Timestamp getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(Timestamp fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getNombreDispositivo() { return nombreDispositivo; }
    public void setNombreDispositivo(String nombreDispositivo) { this.nombreDispositivo = nombreDispositivo; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
}
