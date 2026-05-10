package com.systechpro;

import com.systechpro.dao.UsuarioDAO;
import com.systechpro.models.Usuario;
import com.systechpro.utils.Encriptador;
import com.systechpro.utils.GestorJDBC;
import java.sql.Connection;

public class LoginTester {
    public static void main(String[] args) {
        String correo = "admin@systechpro.com";
        String password = "admin123";
        
        System.out.println("Testing login for: " + correo);
        
        try {
            Connection conn = GestorJDBC.getConnection();
            System.out.println("DB Connection successful");
            conn.close();
            
            UsuarioDAO dao = new UsuarioDAO();
            Usuario usuario = dao.buscarPorCorreo(correo);
            
            if (usuario == null) {
                System.out.println("ERROR: User not found in DB");
                return;
            }
            
            System.out.println("User found: " + usuario.getNombre());
            System.out.println("Stored Hash: [" + usuario.getContrasena() + "]");
            
            boolean verified = Encriptador.verificarPassword(password, usuario.getContrasena());
            System.out.println("Verification Result: " + verified);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
