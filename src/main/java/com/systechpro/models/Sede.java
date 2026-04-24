package com.systechpro.models;

public class Sede {
    private int idSede;
    private String nombre;
    private String codigo;

    public Sede() {}

    public Sede(int idSede, String nombre, String codigo) {
        this.idSede = idSede;
        this.nombre = nombre;
        this.codigo = codigo;
    }

    public int getIdSede() { return idSede; }
    public void setIdSede(int idSede) { this.idSede = idSede; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
}