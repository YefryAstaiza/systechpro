package com.systechpro.models;

public class Salon {
    private int idSalon;
    private int numero;
    private int idSede;

    public Salon() {}

    public Salon(int idSalon, int numero, int idSede) {
        this.idSalon = idSalon;
        this.numero = numero;
        this.idSede = idSede;
    }

    public int getIdSalon() { return idSalon; }
    public void setIdSalon(int idSalon) { this.idSalon = idSalon; }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public int getIdSede() { return idSede; }
    public void setIdSede(int idSede) { this.idSede = idSede; }
}
