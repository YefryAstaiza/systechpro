package com.systechpro.dao;

import com.systechpro.models.CorteDiario;
import com.systechpro.models.CorteDiarioDetalle;
import com.systechpro.models.Prestamo;
import com.systechpro.utils.GestorJDBC;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CorteDiarioDAO {
    private static final Logger LOGGER = Logger.getLogger(CorteDiarioDAO.class.getName());

    /**
     * Genera y guarda un snapshot: cuenta el inventario actual, congela quién
     * tiene qué préstamo activo (APROBADO, aún no DEVUELTO) en este momento,
     * y lo persiste como historial inmutable (no una vista en vivo). Todo
     * dentro de una sola transacción.
     */
    public CorteDiario generar(int idGenerador, int totalDispositivos, int disponibles,
                                List<Prestamo> activos) {
        String sqlHeader = "INSERT INTO corte_diario (total_dispositivos, disponibles, en_prestamo, id_generador) " +
                            "VALUES (?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO corte_diario_detalle " +
                             "(id_corte, id_dispositivo, nombre_dispositivo, id_usuario, nombre_usuario, fecha_toma) " +
                             "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = GestorJDBC.getConnection();
            conn.setAutoCommit(false);

            int idCorte;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlHeader, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, totalDispositivos);
                pstmt.setInt(2, disponibles);
                pstmt.setInt(3, activos.size());
                pstmt.setInt(4, idGenerador);
                pstmt.executeUpdate();

                ResultSet keys = pstmt.getGeneratedKeys();
                if (!keys.next()) {
                    conn.rollback();
                    return null;
                }
                idCorte = keys.getInt(1);
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sqlDetalle)) {
                for (Prestamo p : activos) {
                    pstmt.setInt(1, idCorte);
                    pstmt.setInt(2, p.getIdDispositivo());
                    pstmt.setString(3, p.getNombreDispositivo());
                    pstmt.setInt(4, p.getIdUsuario());
                    pstmt.setString(5, p.getNombreUsuario());
                    pstmt.setTimestamp(6, p.getFechaInicio());
                    pstmt.addBatch();
                }
                if (!activos.isEmpty()) {
                    pstmt.executeBatch();
                }
            }

            conn.commit();
            return buscarPorId(idCorte);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al generar corte diario", e);
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Error al hacer rollback del corte diario", ex); }
            }
            return null;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) { }
                GestorJDBC.closeConnection(conn);
            }
        }
    }

    public List<CorteDiario> listar() {
        List<CorteDiario> lista = new ArrayList<>();
        String sql = "SELECT c.*, u.nombre AS nombre_generador FROM corte_diario c " +
                     "JOIN usuario u ON c.id_generador = u.id_usuario " +
                     "ORDER BY c.fecha_corte DESC";
        try (Connection conn = GestorJDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearCorte(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar cortes diarios", e);
        }
        return lista;
    }

    public CorteDiario buscarPorId(int idCorte) {
        String sql = "SELECT c.*, u.nombre AS nombre_generador FROM corte_diario c " +
                     "JOIN usuario u ON c.id_generador = u.id_usuario " +
                     "WHERE c.id_corte = ?";
        CorteDiario corte = null;
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCorte);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                corte = mapearCorte(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar corte diario", e);
            return null;
        }
        if (corte != null) {
            corte.setDetalle(listarDetalle(idCorte));
        }
        return corte;
    }

    private List<CorteDiarioDetalle> listarDetalle(int idCorte) {
        List<CorteDiarioDetalle> lista = new ArrayList<>();
        String sql = "SELECT * FROM corte_diario_detalle WHERE id_corte = ? ORDER BY fecha_toma ASC";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCorte);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                CorteDiarioDetalle d = new CorteDiarioDetalle();
                d.setIdDetalle(rs.getInt("id_detalle"));
                d.setIdCorte(rs.getInt("id_corte"));
                d.setIdDispositivo(rs.getInt("id_dispositivo"));
                d.setNombreDispositivo(rs.getString("nombre_dispositivo"));
                d.setIdUsuario(rs.getInt("id_usuario"));
                d.setNombreUsuario(rs.getString("nombre_usuario"));
                d.setFechaToma(rs.getTimestamp("fecha_toma"));
                lista.add(d);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar detalle de corte diario", e);
        }
        return lista;
    }

    private CorteDiario mapearCorte(ResultSet rs) throws SQLException {
        CorteDiario c = new CorteDiario();
        c.setIdCorte(rs.getInt("id_corte"));
        c.setFechaCorte(rs.getTimestamp("fecha_corte"));
        c.setTotalDispositivos(rs.getInt("total_dispositivos"));
        c.setDisponibles(rs.getInt("disponibles"));
        c.setEnPrestamo(rs.getInt("en_prestamo"));
        c.setIdGenerador(rs.getInt("id_generador"));
        c.setNombreGenerador(rs.getString("nombre_generador"));
        return c;
    }
}
