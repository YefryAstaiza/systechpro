package com.systechpro.models;

import java.sql.Timestamp;

public class Mantenimiento {
    private int idMantenimiento;
    private int idDispositivo;
    private int idUsuario; // técnico
    private String tipo; // PREVENTIVO, CORRECTIVO
    private Timestamp fechaInicio;
    private Timestamp fechaFin;
    private String descripcion;
    private String estado; // EN_PROCESO, FINALIZADO

    public Mantenimiento() {}

    public Mantenimiento(int idMantenimiento, int idDispositivo, int idUsuario, String tipo,
                         Timestamp fechaInicio, Timestamp fechaFin, String descripcion, String estado) {
        this.idMantenimiento = idMantenimiento;
        this.idDispositivo = idDispositivo;
        this.idUsuario = idUsuario;
        this.tipo = tipo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.descripcion = descripcion;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdMantenimiento() { return idMantenimiento; }
    public void setIdMantenimiento(int idMantenimiento) { this.idMantenimiento = idMantenimiento; }

    public int getIdDispositivo() { return idDispositivo; }
    public void setIdDispositivo(int idDispositivo) { this.idDispositivo = idDispositivo; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Timestamp getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Timestamp fechaInicio) { this.fechaInicio = fechaInicio; }

    public Timestamp getFechaFin() { return fechaFin; }
    public void setFechaFin(Timestamp fechaFin) { this.fechaFin = fechaFin; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    // Propiedades para frontend
    private String nombreDispositivo;
    private String nombreUsuario;

    public String getNombreDispositivo() { return nombreDispositivo; }
    public void setNombreDispositivo(String nombreDispositivo) { this.nombreDispositivo = nombreDispositivo; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    private String ubicacion;

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
}
