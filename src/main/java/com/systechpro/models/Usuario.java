package com.systechpro.models;

public class Usuario {
    private int idUsuario;
    private String nombre;
    private String correo;
    private String contrasena;
    private String rol; // ADMINISTRADOR, DOCENTE, TECNICO, ADMINISTRATIVO
    private boolean cambioObligatorio;

    public Usuario() {}

    public Usuario(int idUsuario, String nombre, String correo, String contrasena, String rol, boolean cambioObligatorio) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.rol = rol;
        this.cambioObligatorio = cambioObligatorio;
    }

    // Getters y Setters
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isCambioObligatorio() { return cambioObligatorio; }
    public void setCambioObligatorio(boolean cambioObligatorio) { this.cambioObligatorio = cambioObligatorio; }
}
