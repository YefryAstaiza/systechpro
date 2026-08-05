package com.systechpro.dao;

import com.systechpro.dao.DispositivoDAO;
import com.systechpro.models.Dispositivo;
import com.systechpro.models.Mantenimiento;
import com.systechpro.utils.GestorJDBC;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MantenimientoDAO {
    private static final Logger LOGGER = Logger.getLogger(MantenimientoDAO.class.getName());

    private final DispositivoDAO dispositivoDAO = new DispositivoDAO();

    private static final String BASE_QUERY_JOIN = 
        "SELECT m.*, d.nombre AS nombre_dispositivo, u.nombre AS nombre_usuario " +
        "FROM mantenimiento m " +
        "JOIN dispositivo d ON m.id_dispositivo = d.id_dispositivo " +
        "JOIN usuario u ON m.id_usuario = u.id_usuario ";
    
    public boolean insertar(Mantenimiento mantenimiento) {
        String sql = "INSERT INTO mantenimiento (id_dispositivo, id_usuario, tipo, fecha_inicio, fecha_fin, descripcion, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, mantenimiento.getIdDispositivo());
            pstmt.setInt(2, mantenimiento.getIdUsuario());
            pstmt.setString(3, mantenimiento.getTipo());
            pstmt.setTimestamp(4, mantenimiento.getFechaInicio());
            pstmt.setTimestamp(5, mantenimiento.getFechaFin());
            pstmt.setString(6, mantenimiento.getDescripcion());
            pstmt.setString(7, mantenimiento.getEstado());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al insertar mantenimiento", e);
            return false;
        }
    }
    
    public List<Mantenimiento> listar() {
        List<Mantenimiento> mantenimientos = new ArrayList<>();
        String sql = BASE_QUERY_JOIN + "ORDER BY m.fecha_inicio DESC";
        
        try (Connection conn = GestorJDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                mantenimientos.add(mapearMantenimiento(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar mantenimientos", e);
        }
        return mantenimientos;
    }
    
    private String construirFiltro(String busqueda, String estadoFiltro, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE 1=1 ");
        if (busqueda != null && !busqueda.isEmpty()) {
            where.append("AND d.nombre LIKE ? ");
            params.add("%" + busqueda + "%");
        }
        if (estadoFiltro != null && !estadoFiltro.isEmpty()) {
            where.append("AND m.estado = ? ");
            params.add(estadoFiltro);
        }
        return where.toString();
    }

    /** Búsqueda por dispositivo + filtro de estado, paginada en SQL (LIMIT/OFFSET, no en memoria). */
    public List<Mantenimiento> listar(String busqueda, String estadoFiltro, int pagina, int tamanoPagina) {
        List<Mantenimiento> mantenimientos = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        String where = construirFiltro(busqueda, estadoFiltro, params);
        String sql = BASE_QUERY_JOIN + where + "ORDER BY m.fecha_inicio DESC LIMIT ? OFFSET ?";
        params.add(tamanoPagina);
        params.add((pagina - 1) * tamanoPagina);

        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) pstmt.setObject(i + 1, params.get(i));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) mantenimientos.add(mapearMantenimiento(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar mantenimientos filtrados", e);
        }
        return mantenimientos;
    }

    public int contarTotal(String busqueda, String estadoFiltro) {
        List<Object> params = new ArrayList<>();
        String where = construirFiltro(busqueda, estadoFiltro, params);
        String sql = "SELECT COUNT(*) FROM mantenimiento m " +
                     "JOIN dispositivo d ON m.id_dispositivo = d.id_dispositivo " + where;
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) pstmt.setObject(i + 1, params.get(i));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al contar mantenimientos filtrados", e);
        }
        return 0;
    }

    public List<Mantenimiento> listarPorDispositivo(int idDispositivo) {
        List<Mantenimiento> mantenimientos = new ArrayList<>();
        String sql = BASE_QUERY_JOIN + "WHERE m.id_dispositivo = ? ORDER BY m.fecha_inicio DESC";
        
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idDispositivo);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                mantenimientos.add(mapearMantenimiento(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar mantenimientos por dispositivo", e);
        }
        return mantenimientos;
    }
    
    public boolean actualizar(Mantenimiento mantenimiento) {
        String sql = "UPDATE mantenimiento SET tipo = ?, fecha_fin = ?, descripcion = ?, estado = ? WHERE id_mantenimiento = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, mantenimiento.getTipo());
            pstmt.setTimestamp(2, mantenimiento.getFechaFin());
            pstmt.setString(3, mantenimiento.getDescripcion());
            pstmt.setString(4, mantenimiento.getEstado());
            pstmt.setInt(5, mantenimiento.getIdMantenimiento());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar mantenimiento", e);
            return false;
        }
    }
    
    public Mantenimiento buscarPorId(int id) {
        String sql = BASE_QUERY_JOIN + "WHERE m.id_mantenimiento = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapearMantenimiento(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar mantenimiento", e);
        }
        return null;
    }
    
    private Mantenimiento mapearMantenimiento(ResultSet rs) throws SQLException {
        Mantenimiento m = new Mantenimiento();
        m.setIdMantenimiento(rs.getInt("id_mantenimiento"));
        m.setIdDispositivo(rs.getInt("id_dispositivo"));
        m.setIdUsuario(rs.getInt("id_usuario"));
        m.setTipo(rs.getString("tipo"));
        m.setFechaInicio(rs.getTimestamp("fecha_inicio"));
        m.setFechaFin(rs.getTimestamp("fecha_fin"));
        m.setDescripcion(rs.getString("descripcion"));
        m.setEstado(rs.getString("estado"));
        
        m.setNombreDispositivo(rs.getString("nombre_dispositivo"));
        m.setNombreUsuario(rs.getString("nombre_usuario"));
        m.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        
        return m;
    }
}
