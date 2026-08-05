package com.systechpro.dao;

import com.systechpro.models.Dispositivo;
import com.systechpro.utils.GestorJDBC;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DispositivoDAO {
    private static final Logger LOGGER = Logger.getLogger(DispositivoDAO.class.getName());

    private static final String BASE_QUERY =
        "SELECT d.*, " +
        "  CASE WHEN p_latest.id_salon IS NOT NULL " +
        "    THEN CONCAT(se.codigo, '-', sl.numero) " +
        "    ELSE NULL END AS ubicacion " +
        "FROM dispositivo d " +
        "LEFT JOIN (" +
        "  SELECT id_dispositivo, MAX(id_prestamo) as max_id " +
        "  FROM prestamo WHERE estado = 'APROBADO' " +
        "  GROUP BY id_dispositivo" +
        ") best ON best.id_dispositivo = d.id_dispositivo " +
        "LEFT JOIN prestamo p_latest ON p_latest.id_prestamo = best.max_id " +
        "LEFT JOIN salon sl ON sl.id_salon = p_latest.id_salon " +
        "LEFT JOIN sede se ON se.id_sede = sl.id_sede ";

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
            LOGGER.log(Level.SEVERE, "Error al insertar dispositivo", e);
            return false;
        }
    }

    public List<Dispositivo> listar() {
        sincronizarPorReservas();
        List<Dispositivo> dispositivos = new ArrayList<>();
        String sql = BASE_QUERY + "ORDER BY d.id_dispositivo DESC";

        try (Connection conn = GestorJDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                dispositivos.add(mapearDispositivo(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar dispositivos", e);
        }
        return dispositivos;
    }

    /**
     * Con reservas futuras, un dispositivo puede quedar "Disponible" al aprobarse (si la reserva
     * es para más adelante) y debe pasar solo a "En uso" cuando esa reserva realmente empieza.
     * Como no hay un job programado, se recalcula en cada lectura: barato y siempre correcto,
     * sin tocar dispositivos en MANTENIMIENTO ni revertir EN_USO cuando el préstamo simplemente
     * no se ha marcado como devuelto todavía (eso sigue requiriendo la acción explícita "Devolver").
     */
    private void sincronizarPorReservas() {
        String sql = "UPDATE dispositivo d SET d.estado = 'EN_USO' " +
                     "WHERE d.estado = 'DISPONIBLE' AND EXISTS (" +
                     "  SELECT 1 FROM prestamo p WHERE p.id_dispositivo = d.id_dispositivo " +
                     "  AND p.estado = 'APROBADO' AND NOW() BETWEEN p.fecha_inicio AND p.fecha_fin" +
                     ")";
        try (Connection conn = GestorJDBC.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error al sincronizar dispositivos por reservas activas", e);
        }
    }

    /**
     * Recalcula el estado de UN dispositivo tras un cambio de préstamo (aprobar/rechazar/devolver):
     * EN_USO si tiene un préstamo APROBADO cuya franja incluye este momento, DISPONIBLE si no.
     * No toca dispositivos en MANTENIMIENTO.
     */
    public void sincronizarEstadoDispositivo(int idDispositivo) {
        String sql = "UPDATE dispositivo SET estado = CASE WHEN EXISTS (" +
                     "  SELECT 1 FROM prestamo p WHERE p.id_dispositivo = ? " +
                     "  AND p.estado = 'APROBADO' AND NOW() BETWEEN p.fecha_inicio AND p.fecha_fin" +
                     ") THEN 'EN_USO' ELSE 'DISPONIBLE' END " +
                     "WHERE id_dispositivo = ? AND estado != 'MANTENIMIENTO'";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idDispositivo);
            pstmt.setInt(2, idDispositivo);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al sincronizar estado del dispositivo " + idDispositivo, e);
        }
    }

    public List<Dispositivo> listarPorFiltro(String tipo, String estado) {
        sincronizarPorReservas();
        List<Dispositivo> dispositivos = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_QUERY + "WHERE 1=1 ");

        if (tipo != null && !tipo.isEmpty()) sql.append("AND d.tipo = ? ");
        if (estado != null && !estado.isEmpty()) sql.append("AND d.estado = ? ");
        sql.append("ORDER BY d.id_dispositivo DESC");

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
            LOGGER.log(Level.SEVERE, "Error al filtrar dispositivos", e);
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
            LOGGER.log(Level.SEVERE, "Error al actualizar dispositivo", e);
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
            LOGGER.log(Level.SEVERE, "Error al actualizar estado", e);
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
            LOGGER.log(Level.SEVERE, "Error al eliminar dispositivo", e);
            return false;
        }
    }

    public Dispositivo buscarPorId(int id) {
        sincronizarEstadoDispositivo(id);
        String sql = BASE_QUERY + "WHERE d.id_dispositivo = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapearDispositivo(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar dispositivo", e);
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
        d.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        String ub = rs.getString("ubicacion");
        d.setUbicacion(ub != null ? ub : "\u2014");
        return d;
    }
}
