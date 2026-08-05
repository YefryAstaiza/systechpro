package com.systechpro.dao;

import com.systechpro.models.Prestamo;
import com.systechpro.utils.GestorJDBC;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrestamoDAO {
    private static final Logger LOGGER = Logger.getLogger(PrestamoDAO.class.getName());

    // Base query that joins the tables for frontend visualization
    private static final String BASE_QUERY_JOIN = 
        "SELECT p.*, " +
        "u.nombre AS nombre_usuario, " +
        "d.nombre AS nombre_dispositivo, " +
        "s.numero AS numero_salon, " +
        "se.nombre AS nombre_sede, " +
        "se.codigo AS codigo_sede " +
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
            LOGGER.log(Level.SEVERE, "Error al insertar préstamo", e);
            return false;
        }
    }

    /**
     * ¿Hay algún préstamo ya APROBADO de este dispositivo cuyo rango de fechas se cruce
     * con [inicio, fin)? Los límites que solo se tocan (uno termina justo cuando el otro
     * empieza) no cuentan como choque. excluirIdPrestamo permite re-chequear un préstamo
     * al aprobarlo sin que se compare contra sí mismo.
     */
    public boolean existeSolapamiento(int idDispositivo, Timestamp inicio, Timestamp fin, Integer excluirIdPrestamo) {
        String sql = "SELECT COUNT(*) FROM prestamo " +
                     "WHERE id_dispositivo = ? AND estado = 'APROBADO' " +
                     "AND fecha_inicio < ? AND fecha_fin > ? " +
                     (excluirIdPrestamo != null ? "AND id_prestamo != ? " : "");
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idDispositivo);
            pstmt.setTimestamp(2, fin);
            pstmt.setTimestamp(3, inicio);
            if (excluirIdPrestamo != null) {
                pstmt.setInt(4, excluirIdPrestamo);
            }
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar solapamiento de horarios", e);
            return true; // ante la duda, no dejar reservar (evita choques por error de validación)
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
            LOGGER.log(Level.SEVERE, "Error al listar préstamos", e);
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
            LOGGER.log(Level.SEVERE, "Error al listar préstamos por usuario", e);
        }
        return prestamos;
    }
    
    /**
     * Construye el WHERE dinámico compartido por listar()/contarTotal() paginados.
     * Nunca concatena el texto de búsqueda: siempre va como parámetro de PreparedStatement.
     */
    private String construirFiltro(String busqueda, String estadoFiltro, Integer idUsuarioFiltro,
                                    Timestamp fechaDesde, Timestamp fechaHasta, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE 1=1 ");
        if (busqueda != null && !busqueda.isEmpty()) {
            where.append("AND (u.nombre LIKE ? OR d.nombre LIKE ?) ");
            String comodin = "%" + busqueda + "%";
            params.add(comodin);
            params.add(comodin);
        }
        if (estadoFiltro != null && !estadoFiltro.isEmpty()) {
            where.append("AND p.estado = ? ");
            params.add(estadoFiltro);
        }
        if (idUsuarioFiltro != null) {
            where.append("AND p.id_usuario = ? ");
            params.add(idUsuarioFiltro);
        }
        if (fechaDesde != null) {
            where.append("AND p.fecha_inicio >= ? ");
            params.add(fechaDesde);
        }
        if (fechaHasta != null) {
            where.append("AND p.fecha_inicio <= ? ");
            params.add(fechaHasta);
        }
        return where.toString();
    }

    /** Búsqueda por usuario/dispositivo + filtros, paginada en SQL (LIMIT/OFFSET, no en memoria). */
    public List<Prestamo> listar(String busqueda, String estadoFiltro, Integer idUsuarioFiltro,
                                  Timestamp fechaDesde, Timestamp fechaHasta, int pagina, int tamanoPagina) {
        List<Prestamo> prestamos = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        String where = construirFiltro(busqueda, estadoFiltro, idUsuarioFiltro, fechaDesde, fechaHasta, params);
        String sql = BASE_QUERY_JOIN + where + "ORDER BY p.fecha_inicio DESC LIMIT ? OFFSET ?";
        params.add(tamanoPagina);
        params.add((pagina - 1) * tamanoPagina);

        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) pstmt.setObject(i + 1, params.get(i));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) prestamos.add(mapearPrestamoJoin(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar préstamos filtrados", e);
        }
        return prestamos;
    }

    /** Mismo filtro que listar(), solo el conteo (para calcular el total de páginas). */
    public int contarTotal(String busqueda, String estadoFiltro, Integer idUsuarioFiltro,
                            Timestamp fechaDesde, Timestamp fechaHasta) {
        List<Object> params = new ArrayList<>();
        String where = construirFiltro(busqueda, estadoFiltro, idUsuarioFiltro, fechaDesde, fechaHasta, params);
        String sql = "SELECT COUNT(*) FROM prestamo p " +
                     "JOIN usuario u ON p.id_usuario = u.id_usuario " +
                     "JOIN dispositivo d ON p.id_dispositivo = d.id_dispositivo " + where;
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) pstmt.setObject(i + 1, params.get(i));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al contar préstamos filtrados", e);
        }
        return 0;
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
            LOGGER.log(Level.SEVERE, "Error al listar préstamos por estado", e);
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
            LOGGER.log(Level.SEVERE, "Error al actualizar estado del préstamo", e);
            return false;
        }
    }

    /** Marca un préstamo como DEVUELTO y registra el momento real de la devolución. */
    public boolean marcarDevuelto(int id) {
        String sql = "UPDATE prestamo SET estado = 'DEVUELTO', fecha_devolucion = CURRENT_TIMESTAMP WHERE id_prestamo = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al marcar préstamo como devuelto", e);
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
            LOGGER.log(Level.SEVERE, "Error al buscar préstamo", e);
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
        p.setFechaDevolucion(rs.getTimestamp("fecha_devolucion"));
        p.setEstado(rs.getString("estado"));
        
        // Propiedades adicionales del JOIN
        p.setNombreUsuario(rs.getString("nombre_usuario"));
        p.setNombreDispositivo(rs.getString("nombre_dispositivo"));
        p.setNumeroSalon(String.valueOf(rs.getInt("numero_salon")));
        p.setNombreSede(rs.getString("nombre_sede"));
        p.setCodigoSede(rs.getString("codigo_sede"));
        
        return p;
    }
}
