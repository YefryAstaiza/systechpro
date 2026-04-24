package com.systechpro.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Encriptador {
    
    public static String encriptarMD5(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error al encriptar: " + e.getMessage());
            return null;
        }
    }
    
    public static boolean verificarPassword(String passwordIngresada, String passwordAlmacenada) {
        String passwordEncriptada = encriptarMD5(passwordIngresada);
        return passwordEncriptada != null && passwordEncriptada.equals(passwordAlmacenada);
    }
}