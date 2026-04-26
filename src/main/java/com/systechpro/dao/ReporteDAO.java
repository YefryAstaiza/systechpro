package com.systechpro.dao;

import com.systechpro.utils.GestorJDBC;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class ReporteDAO {

    public Map<String, Integer> obtenerEstadisticasDispositivos() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT estado, COUNT(*) as total FROM dispositivo GROUP BY estado";
        
        try (Connection conn = GestorJDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                stats.put(rs.getString("estado"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            System.err.println("Error en obtenerEstadisticasDispositivos: " + e.getMessage());
        }
        return stats;
    }

    public Map<String, Integer> obtenerEstadisticasMantenimientos() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT tipo, COUNT(*) as total FROM mantenimiento GROUP BY tipo";
        
        try (Connection conn = GestorJDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                stats.put(rs.getString("tipo"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            System.err.println("Error en obtenerEstadisticasMantenimientos: " + e.getMessage());
        }
        return stats;
    }
}
