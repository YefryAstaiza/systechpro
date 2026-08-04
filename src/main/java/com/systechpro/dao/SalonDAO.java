package com.systechpro.dao;

import com.systechpro.models.Salon;
import com.systechpro.utils.GestorJDBC;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SalonDAO {
    private static final Logger LOGGER = Logger.getLogger(SalonDAO.class.getName());

    public List<Salon> listar() {
        List<Salon> salones = new ArrayList<>();
        String sql = "SELECT * FROM salon";
        
        try (Connection conn = GestorJDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Salon s = new Salon();
                s.setIdSalon(rs.getInt("id_salon"));
                s.setNumero(rs.getInt("numero"));
                s.setIdSede(rs.getInt("id_sede"));
                salones.add(s);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar salones", e);
        }
        return salones;
    }
    
    public List<Salon> listarPorSede(int idSede) {
        List<Salon> salones = new ArrayList<>();
        String sql = "SELECT * FROM salon WHERE id_sede = ?";
        
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idSede);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Salon s = new Salon();
                s.setIdSalon(rs.getInt("id_salon"));
                s.setNumero(rs.getInt("numero"));
                s.setIdSede(rs.getInt("id_sede"));
                salones.add(s);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar salones por sede", e);
        }
        return salones;
    }
    
    public Salon buscarPorId(int id) {
        String sql = "SELECT * FROM salon WHERE id_salon = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Salon s = new Salon();
                s.setIdSalon(rs.getInt("id_salon"));
                s.setNumero(rs.getInt("numero"));
                s.setIdSede(rs.getInt("id_sede"));
                return s;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar salon", e);
        }
        return null;
    }
}
