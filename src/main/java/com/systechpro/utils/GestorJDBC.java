package com.systechpro.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class GestorJDBC {
    private static final String URL = "jdbc:mysql://localhost:3306/systechpro3?useSSL=false&serverTimezone=America/Bogota&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    static {
        try {
            // Establecer zona horaria por defecto para la JVM
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/Bogota"));
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error al cargar el driver de MySQL: " + e.getMessage());
        }
    }
    
    public static Connection getConnection() throws SQLException {
        try {
            System.out.println("Intentando conectar a: " + URL);
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexión exitosa a la base de datos");
            return conn;
        } catch (SQLException e) {
            System.err.println("Error de conexión SQL: " + e.getMessage());
            System.err.println("Error code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            e.printStackTrace();
            throw e;
        }
    }
    
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
}
