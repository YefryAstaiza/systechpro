package com.systechpro.dao;

import com.systechpro.models.Prestamo;
import com.systechpro.utils.GestorJDBC;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAO {

    // Base query that joins the tables for frontend visualization
    private static final String BASE_QUERY_JOIN = 
        "SELECT p.*, " +
        "u.nombre AS nombre_usuario, " +
        "d.nombre AS nombre_dispositivo, " +
        "s.numero AS numero_salon, " +
        "se.nombre AS nombre_sede " +
        "FROM prestamo p " +
        "JOIN usuario u ON p.id_usuario = u.id_usuario " +
        "JOIN dispositivo d ON p.id_dispositivo = d.id_dispositivo " +
        "JOIN salon s ON p.id_salon = s.id_salon " +
        "JOIN sede se ON s.id_sede = se.id_sede ";
    
    public boolean insertar(Prestamo prestamo) {
        String sql = "INSERT INTO prestamo (id_usuario, id_dispositivo, id_salon, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, prestamo.getIdUsuario());
            pstmt.setInt(2, prestamo.getIdDispositivo());
            pstmt.setInt(3, prestamo.getIdSalon());
            pstmt.setTimestamp(4, prestamo.getFechaInicio());
            pstmt.setTimestamp(5, prestamo.getFechaFin());
            pstmt.setString(6, prestamo.getEstado());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar préstamo: " + e.getMessage());
            return false;
        }
    }
    
    public List<Prestamo> listar() {
        List<Prestamo> prestamos = new ArrayList<>();
        String sql = BASE_QUERY_JOIN + "ORDER BY p.fecha_inicio DESC";
        
        try (Connection conn = GestorJDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                prestamos.add(mapearPrestamoJoin(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar préstamos: " + e.getMessage());
        }
        return prestamos;
    }
    
    public List<Prestamo> listarPorUsuario(int idUsuario) {
        List<Prestamo> prestamos = new ArrayList<>();
        String sql = BASE_QUERY_JOIN + "WHERE p.id_usuario = ? ORDER BY p.fecha_inicio DESC";
        
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsuario);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                prestamos.add(mapearPrestamoJoin(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar préstamos por usuario: " + e.getMessage());
        }
        return prestamos;
    }
    
    public List<Prestamo> listarPorEstado(String estado) {
        List<Prestamo> prestamos = new ArrayList<>();
        String sql = BASE_QUERY_JOIN + "WHERE p.estado = ? ORDER BY p.fecha_inicio DESC";
        
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, estado);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                prestamos.add(mapearPrestamoJoin(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar préstamos por estado: " + e.getMessage());
        }
        return prestamos;
    }
    
    public boolean actualizarEstado(int id, String estado) {
        String sql = "UPDATE prestamo SET estado = ? WHERE id_prestamo = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, estado);
            pstmt.setInt(2, id);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar estado del préstamo: " + e.getMessage());
            return false;
        }
    }
    
    public Prestamo buscarPorId(int id) {
        String sql = BASE_QUERY_JOIN + "WHERE p.id_prestamo = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapearPrestamoJoin(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar préstamo: " + e.getMessage());
        }
        return null;
    }
    
    private Prestamo mapearPrestamoJoin(ResultSet rs) throws SQLException {
        Prestamo p = new Prestamo();
        p.setIdPrestamo(rs.getInt("id_prestamo"));
        p.setIdUsuario(rs.getInt("id_usuario"));
        p.setIdDispositivo(rs.getInt("id_dispositivo"));
        p.setIdSalon(rs.getInt("id_salon"));
        p.setFechaInicio(rs.getTimestamp("fecha_inicio"));
        p.setFechaFin(rs.getTimestamp("fecha_fin"));
        p.setEstado(rs.getString("estado"));
        
        // Propiedades adicionales del JOIN
        p.setNombreUsuario(rs.getString("nombre_usuario"));
        p.setNombreDispositivo(rs.getString("nombre_dispositivo"));
        p.setNumeroSalon(String.valueOf(rs.getInt("numero_salon")));
        p.setNombreSede(rs.getString("nombre_sede"));
        
        return p;
    }
}
