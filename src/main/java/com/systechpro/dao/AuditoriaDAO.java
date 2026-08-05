package com.systechpro.dao;

import com.systechpro.models.Auditoria;
import com.systechpro.utils.GestorJDBC;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuditoriaDAO {
    private static final Logger LOGGER = Logger.getLogger(AuditoriaDAO.class.getName());

    public boolean insertar(Auditoria auditoria) {
        String sql = "INSERT INTO auditoria (id_usuario, tabla_afectada, accion, id_registro, descripcion, ip) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, auditoria.getIdUsuario());
            pstmt.setString(2, auditoria.getTablaAfectada());
            pstmt.setString(3, auditoria.getAccion());
            
            if (auditoria.getIdRegistro() == 0) {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                pstmt.setInt(4, auditoria.getIdRegistro());
            }
            
            pstmt.setString(5, auditoria.getDescripcion());
            pstmt.setString(6, auditoria.getIp());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al insertar auditoria", e);
            return false;
        }
    }
    
    public List<Auditoria> listar() {
        List<Auditoria> lista = new ArrayList<>();
        String sql = "SELECT a.*, u.nombre AS nombre_usuario " +
                     "FROM auditoria a " +
                     "JOIN usuario u ON a.id_usuario = u.id_usuario " +
                     "ORDER BY a.fecha_evento DESC";

        try (Connection conn = GestorJDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(mapearAuditoria(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar auditoria", e);
        }
        return lista;
    }

    private String construirFiltro(String busqueda, Timestamp fechaDesde, Timestamp fechaHasta, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE 1=1 ");
        if (busqueda != null && !busqueda.isEmpty()) {
            where.append("AND (u.nombre LIKE ? OR a.descripcion LIKE ?) ");
            String comodin = "%" + busqueda + "%";
            params.add(comodin);
            params.add(comodin);
        }
        if (fechaDesde != null) {
            where.append("AND a.fecha_evento >= ? ");
            params.add(fechaDesde);
        }
        if (fechaHasta != null) {
            where.append("AND a.fecha_evento <= ? ");
            params.add(fechaHasta);
        }
        return where.toString();
    }

    /** Búsqueda por usuario/descripción + rango de fechas, paginada en SQL (LIMIT/OFFSET, no en memoria). */
    public List<Auditoria> listar(String busqueda, Timestamp fechaDesde, Timestamp fechaHasta, int pagina, int tamanoPagina) {
        List<Auditoria> lista = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        String where = construirFiltro(busqueda, fechaDesde, fechaHasta, params);
        String sql = "SELECT a.*, u.nombre AS nombre_usuario FROM auditoria a " +
                     "JOIN usuario u ON a.id_usuario = u.id_usuario " +
                     where + "ORDER BY a.fecha_evento DESC LIMIT ? OFFSET ?";
        params.add(tamanoPagina);
        params.add((pagina - 1) * tamanoPagina);

        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) pstmt.setObject(i + 1, params.get(i));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) lista.add(mapearAuditoria(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar auditoría filtrada", e);
        }
        return lista;
    }

    public int contarTotal(String busqueda, Timestamp fechaDesde, Timestamp fechaHasta) {
        List<Object> params = new ArrayList<>();
        String where = construirFiltro(busqueda, fechaDesde, fechaHasta, params);
        String sql = "SELECT COUNT(*) FROM auditoria a " +
                     "JOIN usuario u ON a.id_usuario = u.id_usuario " + where;
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) pstmt.setObject(i + 1, params.get(i));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al contar auditoría filtrada", e);
        }
        return 0;
    }

    private Auditoria mapearAuditoria(ResultSet rs) throws SQLException {
        Auditoria a = new Auditoria();
        a.setIdAuditoria(rs.getInt("id_auditoria"));
        a.setIdUsuario(rs.getInt("id_usuario"));
        a.setTablaAfectada(rs.getString("tabla_afectada"));
        a.setAccion(rs.getString("accion"));
        a.setIdRegistro(rs.getInt("id_registro"));
        a.setDescripcion(rs.getString("descripcion"));
        a.setIp(rs.getString("ip"));
        a.setFechaEvento(rs.getTimestamp("fecha_evento"));
        a.setNombreUsuario(rs.getString("nombre_usuario"));
        return a;
    }
}
