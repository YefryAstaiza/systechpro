package com.systechpro.models;

import java.sql.Timestamp;
import java.util.List;

public class CorteDiario {
    private int idCorte;
    private Timestamp fechaCorte;
    private int totalDispositivos;
    private int disponibles;
    private int enMonitoria;
    private int idGenerador;

    // Campos extra para mostrar sin necesidad de otro fetch
    private String nombreGenerador;
    private List<CorteDiarioDetalle> detalle;

    public CorteDiario() {}

    public int getIdCorte() { return idCorte; }
    public void setIdCorte(int idCorte) { this.idCorte = idCorte; }

    public Timestamp getFechaCorte() { return fechaCorte; }
    public void setFechaCorte(Timestamp fechaCorte) { this.fechaCorte = fechaCorte; }

    public int getTotalDispositivos() { return totalDispositivos; }
    public void setTotalDispositivos(int totalDispositivos) { this.totalDispositivos = totalDispositivos; }

    public int getDisponibles() { return disponibles; }
    public void setDisponibles(int disponibles) { this.disponibles = disponibles; }

    public int getEnMonitoria() { return enMonitoria; }
    public void setEnMonitoria(int enMonitoria) { this.enMonitoria = enMonitoria; }

    public int getIdGenerador() { return idGenerador; }
    public void setIdGenerador(int idGenerador) { this.idGenerador = idGenerador; }

    public String getNombreGenerador() { return nombreGenerador; }
    public void setNombreGenerador(String nombreGenerador) { this.nombreGenerador = nombreGenerador; }

    public List<CorteDiarioDetalle> getDetalle() { return detalle; }
    public void setDetalle(List<CorteDiarioDetalle> detalle) { this.detalle = detalle; }
}
