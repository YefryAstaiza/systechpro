package com.systechpro.utils;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GestorJDBC {
    private static final Logger LOGGER = Logger.getLogger(GestorJDBC.class.getName());

    // Configurables por variables de entorno (DB_URL/DB_USER/DB_PASSWORD) para no hardcodear
    // credenciales reales en el código - los valores por defecto son los del XAMPP local de
    // desarrollo (root sin contraseña), que nunca deberían usarse en un despliegue real.
    private static final String URL = getEnvOrDefault("DB_URL",
            "jdbc:mysql://localhost:3306/systechpro3?useSSL=false&serverTimezone=America/Bogota&allowPublicKeyRetrieval=true");
    private static final String USER = getEnvOrDefault("DB_USER", "root");
    private static final String PASSWORD = getEnvOrDefault("DB_PASSWORD", "");

    private static String getEnvOrDefault(String nombre, String porDefecto) {
        String valor = System.getenv(nombre);
        return (valor == null || valor.isEmpty()) ? porDefecto : valor;
    }

    private static final DataSource DATA_SOURCE = crearDataSource();

    static {
        // Establecer zona horaria por defecto para la JVM
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/Bogota"));
    }

    private static DataSource crearDataSource() {
        PoolProperties props = new PoolProperties();
        props.setUrl(URL);
        props.setDriverClassName("com.mysql.cj.jdbc.Driver");
        props.setUsername(USER);
        props.setPassword(PASSWORD);
        props.setInitialSize(5);
        props.setMinIdle(5);
        props.setMaxIdle(10);
        props.setMaxActive(20);
        props.setTestOnBorrow(true);
        props.setValidationQuery("SELECT 1");
        return new DataSource(props);
    }

    public static Connection getConnection() throws SQLException {
        try {
            return DATA_SOURCE.getConnection();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener conexión del pool (code=" + e.getErrorCode() + ", sqlState=" + e.getSQLState() + ")", e);
            throw e;
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error al cerrar la conexión", e);
            }
        }
    }
}
