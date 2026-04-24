package com.systechpro.dao;

import com.systechpro.models.Salon;
import com.systechpro.utils.GestorJDBC;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalonDAO {
    
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
            System.err.println("Error al listar salones: " + e.getMessage());
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
            System.err.println("Error al listar salones por sede: " + e.getMessage());
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
            System.err.println("Error al buscar salon: " + e.getMessage());
        }
        return null;
    }
}