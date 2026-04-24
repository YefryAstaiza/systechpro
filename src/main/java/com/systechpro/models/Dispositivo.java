package com.systechpro.models;

public class Dispositivo {
    private int idDispositivo;
    private String nombre;
    private String tipo;
    private String estado; // DISPONIBLE, EN_USO, MANTENIMIENTO
    private String descripcion;

    public Dispositivo() {}

    public Dispositivo(int idDispositivo, String nombre, String tipo, String estado, String descripcion) {
        this.idDispositivo = idDispositivo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.estado = estado;
        this.descripcion = descripcion;
    }

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
}
