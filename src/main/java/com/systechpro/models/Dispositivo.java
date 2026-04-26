package com.systechpro.models;

import java.sql.Timestamp;

public class Dispositivo {
    private int idDispositivo;
    private String nombre;
    private String tipo;
    private String estado; // DISPONIBLE, EN_USO, MANTENIMIENTO
    private String descripcion;
    private Timestamp fechaCreacion;

    // Transient: calculado desde el último préstamo aprobado
    private String ubicacion;

    public Dispositivo() {}

    // Getters y Setters
    public int getIdDispositivo() { return idDispositivo; }
    public void setIdDispositivo(int idDispositivo) { this.idDispositivo = idDispositivo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
}
