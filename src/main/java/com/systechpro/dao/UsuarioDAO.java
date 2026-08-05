package com.systechpro.dao;

import com.systechpro.models.Usuario;
import com.systechpro.utils.GestorJDBC;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsuarioDAO {
    private static final Logger LOGGER = Logger.getLogger(UsuarioDAO.class.getName());

    public Usuario buscarPorCorreo(String correo) {
        String sql = "SELECT * FROM usuario WHERE correo = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, correo);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapearUsuario(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar por correo", e);
        }
        return null;
    }
    
    public boolean insertar(Usuario usuario) {
        String sql = "INSERT INTO usuario (nombre, correo, contrasena, rol, cambio_obligatorio) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getCorreo());
            pstmt.setString(3, usuario.getContrasena());
            pstmt.setString(4, usuario.getRol());
            pstmt.setBoolean(5, usuario.isCambioObligatorio());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al insertar usuario", e);
            return false;
        }
    }
    
    public List<Usuario> listar() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuario";

        try (Connection conn = GestorJDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar usuarios", e);
        }
        return usuarios;
    }

    private String construirFiltro(String busqueda, String rolFiltro, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE 1=1 ");
        if (busqueda != null && !busqueda.isEmpty()) {
            where.append("AND (nombre LIKE ? OR correo LIKE ?) ");
            String comodin = "%" + busqueda + "%";
            params.add(comodin);
            params.add(comodin);
        }
        if (rolFiltro != null && !rolFiltro.isEmpty()) {
            where.append("AND rol = ? ");
            params.add(rolFiltro);
        }
        return where.toString();
    }

    /** Búsqueda por nombre/correo + filtro de rol, paginada en SQL (LIMIT/OFFSET, no en memoria). */
    public List<Usuario> listar(String busqueda, String rolFiltro, int pagina, int tamanoPagina) {
        List<Usuario> usuarios = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        String where = construirFiltro(busqueda, rolFiltro, params);
        String sql = "SELECT * FROM usuario " + where + "ORDER BY id_usuario DESC LIMIT ? OFFSET ?";
        params.add(tamanoPagina);
        params.add((pagina - 1) * tamanoPagina);

        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) pstmt.setObject(i + 1, params.get(i));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) usuarios.add(mapearUsuario(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar usuarios filtrados", e);
        }
        return usuarios;
    }

    public int contarTotal(String busqueda, String rolFiltro) {
        List<Object> params = new ArrayList<>();
        String where = construirFiltro(busqueda, rolFiltro, params);
        String sql = "SELECT COUNT(*) FROM usuario " + where;
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) pstmt.setObject(i + 1, params.get(i));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al contar usuarios filtrados", e);
        }
        return 0;
    }

    public List<Integer> listarIdsPorRol(String rol) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id_usuario FROM usuario WHERE rol = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rol);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("id_usuario"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar ids de usuarios por rol", e);
        }
        return ids;
    }
    
    public boolean actualizar(Usuario usuario) {
        String sql;
        boolean updatePassword = usuario.getContrasena() != null && !usuario.getContrasena().trim().isEmpty();
        
        if (updatePassword) {
            sql = "UPDATE usuario SET nombre = ?, correo = ?, rol = ?, contrasena = ?, cambio_obligatorio = ? WHERE id_usuario = ?";
        } else {
            sql = "UPDATE usuario SET nombre = ?, correo = ?, rol = ?, cambio_obligatorio = ? WHERE id_usuario = ?";
        }
        
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getCorreo());
            pstmt.setString(3, usuario.getRol());
            
            if (updatePassword) {
                pstmt.setString(4, usuario.getContrasena());
                pstmt.setBoolean(5, usuario.isCambioObligatorio());
                pstmt.setInt(6, usuario.getIdUsuario());
            } else {
                pstmt.setBoolean(4, usuario.isCambioObligatorio());
                pstmt.setInt(5, usuario.getIdUsuario());
            }
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar usuario", e);
            return false;
        }
    }

    public boolean actualizarPasswordForceChange(int id, String newPassEncrypted, boolean forceChange) {
        String sql = "UPDATE usuario SET contrasena = ?, cambio_obligatorio = ? WHERE id_usuario = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassEncrypted);
            pstmt.setBoolean(2, forceChange);
            pstmt.setInt(3, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar password force change", e);
            return false;
        }
    }
    
    public boolean correoExiste(String correo, int excludeIdUsuario) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE correo = ? AND id_usuario != ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, correo);
            pstmt.setInt(2, excludeIdUsuario);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar correo", e);
        }
        return false;
    }
    
    public boolean eliminar(int id) {
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar usuario", e);
            return false;
        }
    }
    
    public Usuario buscarPorId(int id) {
        String sql = "SELECT * FROM usuario WHERE id_usuario = ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapearUsuario(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar usuario", e);
        }
        return null;
    }
    
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setCorreo(rs.getString("correo"));
        usuario.setContrasena(rs.getString("contrasena"));
        usuario.setRol(rs.getString("rol"));
        usuario.setCambioObligatorio(rs.getBoolean("cambio_obligatorio"));
        return usuario;
    }
}
