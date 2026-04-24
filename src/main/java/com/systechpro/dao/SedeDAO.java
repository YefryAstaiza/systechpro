package com.systechpro.dao;

import com.systechpro.models.Sede;
import com.systechpro.utils.GestorJDBC;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SedeDAO {
    
    public List<Sede> listar() {
        List<Sede> sedes = new ArrayList<>();
        String sql = "SELECT * FROM sede";
        
        try (Connection conn = GestorJDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Sede s = new Sede();
                s.setIdSede(rs.getInt("id_sede"));
                s.setNombre(rs.getString("nombre"));
                s.setCodigo(rs.getString("codigo"));
                sedes.add(s);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar sedes: " + e.getMessage());
        }
        return sedes;
    }
    
    public Sede buscarPorId(int id) {
        String sql = "SELECT * FROM sede WHERE id_sede = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Sede s = new Sede();
                s.setIdSede(rs.getInt("id_sede"));
                s.setNombre(rs.getString("nombre"));
                s.setCodigo(rs.getString("codigo"));
                return s;
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar sede: " + e.getMessage());
        }
        return null;
    }
}