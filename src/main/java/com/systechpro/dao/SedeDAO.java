package com.systechpro.dao;

import com.systechpro.models.Sede;
import com.systechpro.utils.GestorJDBC;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SedeDAO {
    private static final Logger LOGGER = Logger.getLogger(SedeDAO.class.getName());

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
            LOGGER.log(Level.SEVERE, "Error al listar sedes", e);
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
            LOGGER.log(Level.SEVERE, "Error al buscar sede", e);
        }
        return null;
    }
}
