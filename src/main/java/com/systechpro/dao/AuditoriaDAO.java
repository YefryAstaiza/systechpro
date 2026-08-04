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
                
                lista.add(a);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar auditoria", e);
        }
        return lista;
    }
}
