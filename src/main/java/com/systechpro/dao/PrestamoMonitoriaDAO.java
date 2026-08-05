package com.systechpro.dao;

import com.systechpro.models.PrestamoMonitoria;
import com.systechpro.utils.GestorJDBC;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrestamoMonitoriaDAO {
    private static final Logger LOGGER = Logger.getLogger(PrestamoMonitoriaDAO.class.getName());

    private static final String BASE_QUERY_JOIN =
        "SELECT m.*, d.nombre AS nombre_dispositivo, u.nombre AS nombre_usuario " +
        "FROM prestamo_monitoria m " +
        "JOIN dispositivo d ON m.id_dispositivo = d.id_dispositivo " +
        "JOIN usuario u ON m.id_usuario = u.id_usuario ";

    public boolean tomar(int idDispositivo, int idUsuario) {
        String sql = "INSERT INTO prestamo_monitoria (id_dispositivo, id_usuario) VALUES (?, ?)";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idDispositivo);
            pstmt.setInt(2, idUsuario);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar toma de monitoria", e);
            return false;
        }
    }

    public boolean devolver(int idMonitoria, int idUsuario) {
        String sql = "UPDATE prestamo_monitoria SET estado = 'DEVUELTO', fecha_devolucion = CURRENT_TIMESTAMP " +
                     "WHERE id_monitoria = ? AND id_usuario = ? AND estado = 'ACTIVO'";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idMonitoria);
            pstmt.setInt(2, idUsuario);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al registrar devolución de monitoria", e);
            return false;
        }
    }

    public List<PrestamoMonitoria> listarActivos() {
        List<PrestamoMonitoria> lista = new ArrayList<>();
        String sql = BASE_QUERY_JOIN + "WHERE m.estado = 'ACTIVO' ORDER BY m.fecha_toma ASC";
        try (Connection conn = GestorJDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar monitorias activas", e);
        }
        return lista;
    }

    public List<PrestamoMonitoria> listarActivasPorUsuario(int idUsuario) {
        List<PrestamoMonitoria> lista = new ArrayList<>();
        String sql = BASE_QUERY_JOIN + "WHERE m.estado = 'ACTIVO' AND m.id_usuario = ? ORDER BY m.fecha_toma ASC";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar monitorias activas por usuario", e);
        }
        return lista;
    }

    public PrestamoMonitoria buscarPorId(int idMonitoria) {
        String sql = BASE_QUERY_JOIN + "WHERE m.id_monitoria = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idMonitoria);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapear(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar monitoria por id", e);
        }
        return null;
    }

    private PrestamoMonitoria mapear(ResultSet rs) throws SQLException {
        PrestamoMonitoria m = new PrestamoMonitoria();
        m.setIdMonitoria(rs.getInt("id_monitoria"));
        m.setIdDispositivo(rs.getInt("id_dispositivo"));
        m.setIdUsuario(rs.getInt("id_usuario"));
        m.setFechaToma(rs.getTimestamp("fecha_toma"));
        m.setFechaDevolucion(rs.getTimestamp("fecha_devolucion"));
        m.setEstado(rs.getString("estado"));
        m.setNombreDispositivo(rs.getString("nombre_dispositivo"));
        m.setNombreUsuario(rs.getString("nombre_usuario"));
        return m;
    }
}
