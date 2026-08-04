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
