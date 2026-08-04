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

    public List<Dispositivo> listarPorFiltro(String tipo, String estado) {
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
