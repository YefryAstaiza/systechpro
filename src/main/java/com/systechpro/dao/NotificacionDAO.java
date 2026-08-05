package com.systechpro.dao;

import com.systechpro.models.Notificacion;
import com.systechpro.utils.GestorJDBC;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotificacionDAO {
    private static final Logger LOGGER = Logger.getLogger(NotificacionDAO.class.getName());

    public static final String TIPO_PRESTAMO_APROBADO = "PRESTAMO_APROBADO";
    public static final String TIPO_PRESTAMO_RECHAZADO = "PRESTAMO_RECHAZADO";
    public static final String TIPO_PRESTAMO_DEVUELTO = "PRESTAMO_DEVUELTO";
    public static final String TIPO_PASSWORD_APROBADA = "PASSWORD_APROBADA";
    public static final String TIPO_PASSWORD_RECHAZADA = "PASSWORD_RECHAZADA";

    private static final int LIMITE_LISTADO = 30;

    public boolean crear(int idUsuario, String tipo, String mensaje) {
        String sql = "INSERT INTO notificacion (id_usuario, tipo, mensaje) VALUES (?, ?, ?)";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario);
            pstmt.setString(2, tipo);
            pstmt.setString(3, mensaje);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al crear notificacion", e);
            return false;
        }
    }

    public List<Notificacion> listarPorUsuario(int idUsuario) {
        List<Notificacion> notificaciones = new ArrayList<>();
        String sql = "SELECT * FROM notificacion WHERE id_usuario = ? " +
                     "ORDER BY fecha_creacion DESC LIMIT " + LIMITE_LISTADO;
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                notificaciones.add(mapearNotificacion(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar notificaciones", e);
        }
        return notificaciones;
    }

    public int contarNoLeidas(int idUsuario) {
        String sql = "SELECT COUNT(*) FROM notificacion WHERE id_usuario = ? AND leida = FALSE";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al contar notificaciones no leidas", e);
        }
        return 0;
    }

    public boolean marcarLeida(int idNotificacion, int idUsuario) {
        String sql = "UPDATE notificacion SET leida = TRUE WHERE id_notificacion = ? AND id_usuario = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idNotificacion);
            pstmt.setInt(2, idUsuario);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al marcar notificacion como leida", e);
            return false;
        }
    }

    public boolean marcarTodasLeidas(int idUsuario) {
        String sql = "UPDATE notificacion SET leida = TRUE WHERE id_usuario = ? AND leida = FALSE";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al marcar todas las notificaciones como leidas", e);
            return false;
        }
    }

    private Notificacion mapearNotificacion(ResultSet rs) throws SQLException {
        Notificacion n = new Notificacion();
        n.setIdNotificacion(rs.getInt("id_notificacion"));
        n.setIdUsuario(rs.getInt("id_usuario"));
        n.setTipo(rs.getString("tipo"));
        n.setMensaje(rs.getString("mensaje"));
        n.setLeida(rs.getBoolean("leida"));
        n.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        return n;
    }
}
