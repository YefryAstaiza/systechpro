package com.systechpro.dao;

import com.systechpro.models.Mantenimiento;
import com.systechpro.utils.GestorJDBC;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MantenimientoDAO {
    
    public boolean insertar(Mantenimiento mantenimiento) {
        String sql = "INSERT INTO mantenimiento (id_dispositivo, id_usuario, tipo, fecha_inicio, descripcion, estado) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, mantenimiento.getIdDispositivo());
            pstmt.setInt(2, mantenimiento.getIdUsuario());
            pstmt.setString(3, mantenimiento.getTipo());
            pstmt.setTimestamp(4, mantenimiento.getFechaInicio());
            pstmt.setString(5, mantenimiento.getDescripcion());
            pstmt.setString(6, mantenimiento.getEstado());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar mantenimiento: " + e.getMessage());
            return false;
        }
    }
    
    public List<Mantenimiento> listar() {
        List<Mantenimiento> mantenimientos = new ArrayList<>();
        String sql = "SELECT * FROM mantenimiento ORDER BY fecha_inicio DESC";
        
        try (Connection conn = GestorJDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                mantenimientos.add(mapearMantenimiento(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar mantenimientos: " + e.getMessage());
        }
        return mantenimientos;
    }
    
    public List<Mantenimiento> listarPorDispositivo(int idDispositivo) {
        List<Mantenimiento> mantenimientos = new ArrayList<>();
        String sql = "SELECT * FROM mantenimiento WHERE id_dispositivo = ? ORDER BY fecha_inicio DESC";
        
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idDispositivo);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                mantenimientos.add(mapearMantenimiento(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar mantenimientos por dispositivo: " + e.getMessage());
        }
        return mantenimientos;
    }
    
    public boolean actualizar(Mantenimiento mantenimiento) {
        String sql = "UPDATE mantenimiento SET tipo = ?, fecha_fin = ?, descripcion = ?, estado = ? WHERE id_mantenimiento = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, mantenimiento.getTipo());
            pstmt.setTimestamp(2, mantenimiento.getFechaFin());
            pstmt.setString(3, mantenimiento.getDescripcion());
            pstmt.setString(4, mantenimiento.getEstado());
            pstmt.setInt(5, mantenimiento.getIdMantenimiento());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar mantenimiento: " + e.getMessage());
            return false;
        }
    }
    
    public Mantenimiento buscarPorId(int id) {
        String sql = "SELECT * FROM mantenimiento WHERE id_mantenimiento = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapearMantenimiento(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar mantenimiento: " + e.getMessage());
        }
        return null;
    }
    
    private Mantenimiento mapearMantenimiento(ResultSet rs) throws SQLException {
        Mantenimiento m = new Mantenimiento();
        m.setIdMantenimiento(rs.getInt("id_mantenimiento"));
        m.setIdDispositivo(rs.getInt("id_dispositivo"));
        m.setIdUsuario(rs.getInt("id_usuario"));
        m.setTipo(rs.getString("tipo"));
        m.setFechaInicio(rs.getTimestamp("fecha_inicio"));
        m.setFechaFin(rs.getTimestamp("fecha_fin"));
        m.setDescripcion(rs.getString("descripcion"));
        m.setEstado(rs.getString("estado"));
        return m;
    }
}