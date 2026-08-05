package com.systechpro.models;

import java.sql.Timestamp;

public class CorteDiarioDetalle {
    private int idDetalle;
    private int idCorte;
    private int idDispositivo;
    private String nombreDispositivo;
    private int idUsuario;
    private String nombreUsuario;
    private Timestamp fechaToma;

    public CorteDiarioDetalle() {}

    public int getIdDetalle() { return idDetalle; }
    public void setIdDetalle(int idDetalle) { this.idDetalle = idDetalle; }

    public int getIdCorte() { return idCorte; }
    public void setIdCorte(int idCorte) { this.idCorte = idCorte; }

    public int getIdDispositivo() { return idDispositivo; }
    public void setIdDispositivo(int idDispositivo) { this.idDispositivo = idDispositivo; }

    public String getNombreDispositivo() { return nombreDispositivo; }
    public void setNombreDispositivo(String nombreDispositivo) { this.nombreDispositivo = nombreDispositivo; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public Timestamp getFechaToma() { return fechaToma; }
    public void setFechaToma(Timestamp fechaToma) { this.fechaToma = fechaToma; }
}
