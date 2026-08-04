package com.systechpro.utils;

import org.mindrot.jbcrypt.BCrypt;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Encriptador {
    private static final Logger LOGGER = Logger.getLogger(Encriptador.class.getName());

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
            LOGGER.log(Level.WARNING, "Error al verificar contraseña BCrypt", e);
            return false;
        }
    }
}
