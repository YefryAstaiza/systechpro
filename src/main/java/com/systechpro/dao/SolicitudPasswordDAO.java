package com.systechpro.dao;

import com.systechpro.models.SolicitudPassword;
import com.systechpro.utils.GestorJDBC;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SolicitudPasswordDAO {
    private static final Logger LOGGER = Logger.getLogger(SolicitudPasswordDAO.class.getName());

    // Tras aprobarse, la clave temporal deja de mostrarse en el listado pasadas estas horas,
    // aunque nunca se haya usado (defensa adicional a la purga explícita en el cambio de contraseña).
    private static final long PASSWORD_TEMPORAL_EXPIRACION_HORAS = 24;

    public boolean insertar(SolicitudPassword solicitud) {
        String sql = "INSERT INTO solicitud_password (id_usuario, estado) VALUES (?, 'PENDIENTE')";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, solicitud.getIdUsuario());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al insertar solicitud password", e);
            return false;
        }
    }

    public List<SolicitudPassword> listar() {
        List<SolicitudPassword> solicitudes = new ArrayList<>();
        String sql = "SELECT s.*, u.nombre as nombre_usuario, u.correo as correo_usuario, " +
                     "r.nombre as nombre_resolutor " +
                     "FROM solicitud_password s " +
                     "JOIN usuario u ON s.id_usuario = u.id_usuario " +
                     "LEFT JOIN usuario r ON s.id_resolutor = r.id_usuario " +
                     "ORDER BY s.fecha_solicitud DESC";
        
        try (Connection conn = GestorJDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                solicitudes.add(mapearSolicitud(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar solicitudes password", e);
        }
        return solicitudes;
    }

    private String construirFiltro(String busqueda, String estadoFiltro, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE 1=1 ");
        if (busqueda != null && !busqueda.isEmpty()) {
            where.append("AND u.nombre LIKE ? ");
            params.add("%" + busqueda + "%");
        }
        if (estadoFiltro != null && !estadoFiltro.isEmpty()) {
            where.append("AND s.estado = ? ");
            params.add(estadoFiltro);
        }
        return where.toString();
    }

    /** Búsqueda por usuario + filtro de estado, paginada en SQL (LIMIT/OFFSET, no en memoria). */
    public List<SolicitudPassword> listar(String busqueda, String estadoFiltro, int pagina, int tamanoPagina) {
        List<SolicitudPassword> solicitudes = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        String where = construirFiltro(busqueda, estadoFiltro, params);
        String sql = "SELECT s.*, u.nombre as nombre_usuario, u.correo as correo_usuario, " +
                     "r.nombre as nombre_resolutor " +
                     "FROM solicitud_password s " +
                     "JOIN usuario u ON s.id_usuario = u.id_usuario " +
                     "LEFT JOIN usuario r ON s.id_resolutor = r.id_usuario " +
                     where + "ORDER BY s.fecha_solicitud DESC LIMIT ? OFFSET ?";
        params.add(tamanoPagina);
        params.add((pagina - 1) * tamanoPagina);

        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) pstmt.setObject(i + 1, params.get(i));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) solicitudes.add(mapearSolicitud(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar solicitudes password filtradas", e);
        }
        return solicitudes;
    }

    public int contarTotal(String busqueda, String estadoFiltro) {
        List<Object> params = new ArrayList<>();
        String where = construirFiltro(busqueda, estadoFiltro, params);
        String sql = "SELECT COUNT(*) FROM solicitud_password s " +
                     "JOIN usuario u ON s.id_usuario = u.id_usuario " + where;
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) pstmt.setObject(i + 1, params.get(i));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al contar solicitudes password filtradas", e);
        }
        return 0;
    }

    public boolean actualizarEstado(int idSolicitud, String estado, int idResolutor, String passwordTemporal) {
        String sql = "UPDATE solicitud_password SET estado = ?, id_resolutor = ?, fecha_resolucion = CURRENT_TIMESTAMP, password_temporal = ? WHERE id_solicitud = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, estado);
            pstmt.setInt(2, idResolutor);
            pstmt.setString(3, passwordTemporal);
            pstmt.setInt(4, idSolicitud);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar estado solicitud password", e);
            return false;
        }
    }

    public SolicitudPassword buscarPendientePorUsuario(int idUsuario) {
        String sql = "SELECT s.*, u.nombre as nombre_usuario, u.correo as correo_usuario, " +
                     "r.nombre as nombre_resolutor " +
                     "FROM solicitud_password s " +
                     "JOIN usuario u ON s.id_usuario = u.id_usuario " +
                     "LEFT JOIN usuario r ON s.id_resolutor = r.id_usuario " +
                     "WHERE s.id_usuario = ? AND s.estado = 'PENDIENTE'";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapearSolicitud(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar solicitud pendiente", e);
        }
        return null;
    }

    public SolicitudPassword buscarPorId(int idSolicitud) {
        String sql = "SELECT s.*, u.nombre as nombre_usuario, u.correo as correo_usuario, " +
                     "r.nombre as nombre_resolutor " +
                     "FROM solicitud_password s " +
                     "JOIN usuario u ON s.id_usuario = u.id_usuario " +
                     "LEFT JOIN usuario r ON s.id_resolutor = r.id_usuario " +
                     "WHERE s.id_solicitud = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idSolicitud);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapearSolicitud(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar solicitud por id", e);
        }
        return null;
    }

    /**
     * Elimina la clave temporal de la solicitud aprobada más reciente de un usuario.
     * Se invoca una vez que esa clave ya cumplió su propósito (el usuario cambió su contraseña).
     */
    public boolean limpiarPasswordTemporal(int idUsuario) {
        String sql = "UPDATE solicitud_password SET password_temporal = NULL " +
                     "WHERE id_usuario = ? AND estado = 'APROBADA' AND password_temporal IS NOT NULL";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al limpiar password temporal", e);
            return false;
        }
    }

    private SolicitudPassword mapearSolicitud(ResultSet rs) throws SQLException {
        SolicitudPassword s = new SolicitudPassword();
        s.setIdSolicitud(rs.getInt("id_solicitud"));
        s.setIdUsuario(rs.getInt("id_usuario"));
        s.setFechaSolicitud(rs.getTimestamp("fecha_solicitud"));
        s.setEstado(rs.getString("estado"));
        Timestamp fechaResolucion = rs.getTimestamp("fecha_resolucion");
        s.setFechaResolucion(fechaResolucion);
        s.setIdResolutor(rs.getObject("id_resolutor") != null ? rs.getInt("id_resolutor") : null);
        s.setPasswordTemporal(passwordTemporalVigente(rs.getString("password_temporal"), fechaResolucion));

        s.setNombreUsuario(rs.getString("nombre_usuario"));
        s.setCorreoUsuario(rs.getString("correo_usuario"));
        s.setNombreResolutor(rs.getString("nombre_resolutor"));
        return s;
    }

    /**
     * Deja de exponer la clave temporal (aunque siga en la base de datos) una vez
     * pasada la ventana de expiración desde que se aprobó la solicitud.
     */
    private String passwordTemporalVigente(String passwordTemporal, Timestamp fechaResolucion) {
        if (passwordTemporal == null || fechaResolucion == null) return passwordTemporal;
        long horasTranscurridas = (System.currentTimeMillis() - fechaResolucion.getTime()) / (1000 * 60 * 60);
        return horasTranscurridas >= PASSWORD_TEMPORAL_EXPIRACION_HORAS ? null : passwordTemporal;
    }
}
