package com.systechpro.models;

import java.sql.Timestamp;

public class SolicitudPassword {
    private int idSolicitud;
    private int idUsuario;
    private Timestamp fechaSolicitud;
    private String estado; // PENDIENTE, APROBADA, RECHAZADA
    private Timestamp fechaResolucion;
    private Integer idResolutor;
    private String passwordTemporal;
    
    // Campos extra para mostrar en la tabla sin necesidad de otro fetch
    private String nombreUsuario;
    private String correoUsuario;
    private String nombreResolutor;

    public SolicitudPassword() {}

    public int getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(int idSolicitud) { this.idSolicitud = idSolicitud; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public Timestamp getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(Timestamp fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Timestamp getFechaResolucion() { return fechaResolucion; }
    public void setFechaResolucion(Timestamp fechaResolucion) { this.fechaResolucion = fechaResolucion; }

    public Integer getIdResolutor() { return idResolutor; }
    public void setIdResolutor(Integer idResolutor) { this.idResolutor = idResolutor; }

    public String getPasswordTemporal() { return passwordTemporal; }
    public void setPasswordTemporal(String passwordTemporal) { this.passwordTemporal = passwordTemporal; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getCorreoUsuario() { return correoUsuario; }
    public void setCorreoUsuario(String correoUsuario) { this.correoUsuario = correoUsuario; }

    public String getNombreResolutor() { return nombreResolutor; }
    public void setNombreResolutor(String nombreResolutor) { this.nombreResolutor = nombreResolutor; }
}
