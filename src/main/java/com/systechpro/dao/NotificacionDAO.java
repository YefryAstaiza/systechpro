package com.systechpro.dao;

import com.systechpro.models.Notificacion;
import com.systechpro.utils.EmailService;
import com.systechpro.utils.GestorJDBC;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotificacionDAO {
    private static final Logger LOGGER = Logger.getLogger(NotificacionDAO.class.getName());

    public static final String TIPO_PRESTAMO_APROBADO = "PRESTAMO_APROBADO";
    public static final String TIPO_PRESTAMO_RECHAZADO = "PRESTAMO_RECHAZADO";
    public static final String TIPO_PRESTAMO_DEVUELTO = "PRESTAMO_DEVUELTO";
    public static final String TIPO_PASSWORD_APROBADA = "PASSWORD_APROBADA";
    public static final String TIPO_PASSWORD_RECHAZADA = "PASSWORD_RECHAZADA";
    public static final String TIPO_MANTENIMIENTO_CREADO = "MANTENIMIENTO_CREADO";
    public static final String TIPO_MANTENIMIENTO_FINALIZADO = "MANTENIMIENTO_FINALIZADO";

    private static final int LIMITE_LISTADO = 30;

    private static final Map<String, String> ASUNTOS_POR_TIPO = Map.of(
        TIPO_PRESTAMO_APROBADO, "Tu préstamo fue aprobado",
        TIPO_PRESTAMO_RECHAZADO, "Tu préstamo fue rechazado",
        TIPO_PRESTAMO_DEVUELTO, "Devolución de dispositivo registrada",
        TIPO_PASSWORD_APROBADA, "Tu solicitud de contraseña fue aprobada",
        TIPO_PASSWORD_RECHAZADA, "Tu solicitud de contraseña fue rechazada",
        TIPO_MANTENIMIENTO_CREADO, "Nuevo mantenimiento registrado",
        TIPO_MANTENIMIENTO_FINALIZADO, "Mantenimiento finalizado"
    );

    public boolean crear(int idUsuario, String tipo, String mensaje) {
        String sql = "INSERT INTO notificacion (id_usuario, tipo, mensaje) VALUES (?, ?, ?)";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario);
            pstmt.setString(2, tipo);
            pstmt.setString(3, mensaje);
            boolean creada = pstmt.executeUpdate() > 0;
            if (creada) {
                enviarCorreo(idUsuario, tipo, mensaje);
            }
            return creada;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al crear notificacion", e);
            return false;
        }
    }

    private void enviarCorreo(int idUsuario, String tipo, String mensaje) {
        String sql = "SELECT correo FROM usuario WHERE id_usuario = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String correo = rs.getString("correo");
                String asunto = ASUNTOS_POR_TIPO.getOrDefault(tipo, "Nueva notificación");
                String cuerpo = mensaje + "\n\n— SysTechPro (Fundación Universitaria de Popayán)";
                EmailService.enviarAsync(correo, asunto, cuerpo);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error al obtener correo para notificación por email", e);
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
