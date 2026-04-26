package com.systechpro.utils;

import org.mindrot.jbcrypt.BCrypt;

public class Encriptador {
    
    /**
     * Encripta una contraseña usando BCrypt.
     */
    public static String encriptarBCrypt(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    
    /**
     * Verifica una contraseña ingresada en texto plano contra un hash BCrypt almacenado.
     */
    public static boolean verificarPassword(String passwordIngresada, String passwordAlmacenada) {
        if (passwordIngresada == null || passwordAlmacenada == null || passwordAlmacenada.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(passwordIngresada, passwordAlmacenada);
        } catch (Exception e) {
            System.err.println("Error al verificar contraseña BCrypt: " + e.getMessage());
            return false;
        }
    }
}
