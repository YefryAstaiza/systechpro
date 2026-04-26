package com.systechpro.models;

import java.sql.Timestamp;

public class Auditoria {
    private int idAuditoria;
    private int idUsuario;
    private String tablaAfectada;
    private String accion;
    private int idRegistro;
    private String descripcion;
    private String ip;
    private Timestamp fechaEvento;

    // Frontend display field
    private String nombreUsuario;

    public Auditoria() {}

    public Auditoria(int idUsuario, String tablaAfectada, String accion, int idRegistro, String descripcion, String ip) {
        this.idUsuario = idUsuario;
        this.tablaAfectada = tablaAfectada;
        this.accion = accion;
        this.idRegistro = idRegistro;
        this.descripcion = descripcion;
        this.ip = ip;
    }

    public int getIdAuditoria() { return idAuditoria; }
    public void setIdAuditoria(int idAuditoria) { this.idAuditoria = idAuditoria; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getTablaAfectada() { return tablaAfectada; }
    public void setTablaAfectada(String tablaAfectada) { this.tablaAfectada = tablaAfectada; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public int getIdRegistro() { return idRegistro; }
    public void setIdRegistro(int idRegistro) { this.idRegistro = idRegistro; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public Timestamp getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(Timestamp fechaEvento) { this.fechaEvento = fechaEvento; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
}
