package com.systechpro.models;

import java.sql.Timestamp;

public class Prestamo {
    private int idPrestamo;
    private int idUsuario;
    private int idDispositivo;
    private int idSalon;
    private Timestamp fechaInicio;
    private Timestamp fechaFin;
    private String estado; // PENDIENTE, APROBADO, RECHAZADO

    public Prestamo() {}

    public Prestamo(int idPrestamo, int idUsuario, int idDispositivo, int idSalon, 
                    Timestamp fechaInicio, Timestamp fechaFin, String estado) {
        this.idPrestamo = idPrestamo;
        this.idUsuario = idUsuario;
        this.idDispositivo = idDispositivo;
        this.idSalon = idSalon;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdPrestamo() { return idPrestamo; }
    public void setIdPrestamo(int idPrestamo) { this.idPrestamo = idPrestamo; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public int getIdDispositivo() { return idDispositivo; }
    public void setIdDispositivo(int idDispositivo) { this.idDispositivo = idDispositivo; }

    public int getIdSalon() { return idSalon; }
    public void setIdSalon(int idSalon) { this.idSalon = idSalon; }

    public Timestamp getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Timestamp fechaInicio) { this.fechaInicio = fechaInicio; }

    public Timestamp getFechaFin() { return fechaFin; }
    public void setFechaFin(Timestamp fechaFin) { this.fechaFin = fechaFin; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}