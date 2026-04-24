package com.systechpro.dao;

import com.systechpro.models.Dispositivo;
import com.systechpro.utils.GestorJDBC;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DispositivoDAO {
    
    public boolean insertar(Dispositivo dispositivo) {
        String sql = "INSERT INTO dispositivo (nombre, tipo, estado, descripcion) VALUES (?, ?, ?, ?)";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, dispositivo.getNombre());
            pstmt.setString(2, dispositivo.getTipo());
            pstmt.setString(3, dispositivo.getEstado());
            pstmt.setString(4, dispositivo.getDescripcion());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar dispositivo: " + e.getMessage());
            return false;
        }
    }
    
    public List<Dispositivo> listar() {
        List<Dispositivo> dispositivos = new ArrayList<>();
        String sql = "SELECT * FROM dispositivo";
        
        try (Connection conn = GestorJDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                dispositivos.add(mapearDispositivo(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar dispositivos: " + e.getMessage());
        }
        return dispositivos;
    }
    
    public List<Dispositivo> listarPorFiltro(String tipo, String estado) {
        List<Dispositivo> dispositivos = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM dispositivo WHERE 1=1");
        
        if (tipo != null && !tipo.isEmpty()) sql.append(" AND tipo = ?");
        if (estado != null && !estado.isEmpty()) sql.append(" AND estado = ?");
        
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            int index = 1;
            if (tipo != null && !tipo.isEmpty()) pstmt.setString(index++, tipo);
            if (estado != null && !estado.isEmpty()) pstmt.setString(index++, estado);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                dispositivos.add(mapearDispositivo(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al filtrar dispositivos: " + e.getMessage());
        }
        return dispositivos;
    }
    
    public boolean actualizar(Dispositivo dispositivo) {
        String sql = "UPDATE dispositivo SET nombre = ?, tipo = ?, estado = ?, descripcion = ? WHERE id_dispositivo = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, dispositivo.getNombre());
            pstmt.setString(2, dispositivo.getTipo());
            pstmt.setString(3, dispositivo.getEstado());
            pstmt.setString(4, dispositivo.getDescripcion());
            pstmt.setInt(5, dispositivo.getIdDispositivo());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar dispositivo: " + e.getMessage());
            return false;
        }
    }
    
    public boolean actualizarEstado(int id, String estado) {
        String sql = "UPDATE dispositivo SET estado = ? WHERE id_dispositivo = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, estado);
            pstmt.setInt(2, id);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar estado: " + e.getMessage());
            return false;
        }
    }
    
    public boolean eliminar(int id) {
        String sql = "DELETE FROM dispositivo WHERE id_dispositivo = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar dispositivo: " + e.getMessage());
            return false;
        }
    }
    
    public Dispositivo buscarPorId(int id) {
        String sql = "SELECT * FROM dispositivo WHERE id_dispositivo = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapearDispositivo(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar dispositivo: " + e.getMessage());
        }
        return null;
    }
    
    private Dispositivo mapearDispositivo(ResultSet rs) throws SQLException {
        Dispositivo d = new Dispositivo();
        d.setIdDispositivo(rs.getInt("id_dispositivo"));
        d.setNombre(rs.getString("nombre"));
        d.setTipo(rs.getString("tipo"));
        d.setEstado(rs.getString("estado"));
        d.setDescripcion(rs.getString("descripcion"));
        return d;
    }
}
