package com.systechpro.controllers;

import com.systechpro.dao.UsuarioDAO;
import com.systechpro.dao.AuditoriaDAO;
import com.systechpro.models.Auditoria;
import com.systechpro.models.Usuario;
import com.systechpro.utils.Encriptador;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "UsuarioController", urlPatterns = {"/api/usuarios/*"})
public class UsuarioController extends HttpServlet {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final AuditoriaDAO auditoriaDAO = new AuditoriaDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private boolean validarRol(String rol) {
        return "ADMINISTRADOR".equals(rol) || "DOCENTE".equals(rol) || 
               "TECNICO".equals(rol) || "ADMINISTRATIVO".equals(rol);
    }

    private boolean validarCorreo(String correo) {
        return correo != null && correo.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "No autorizado"));
            return;
        }

        String rol = (String) session.getAttribute("rol");
        if (!"ADMINISTRADOR".equals(rol)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Solo administradores"));
            return;
        }

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                List<Usuario> usuarios = usuarioDAO.listar();
                // No enviar contraseñas al frontend
                for (Usuario u : usuarios) {
                    u.setContrasena(null);
                }
                objectMapper.writeValue(response.getWriter(), usuarios);
            } else {
                String idStr = pathInfo.substring(1);
                int id = Integer.parseInt(idStr);
                Usuario usuario = usuarioDAO.buscarPorId(id);

                if (usuario != null) {
                    usuario.setContrasena(null); // Seguridad
                    objectMapper.writeValue(response.getWriter(), usuario);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    objectMapper.writeValue(response.getWriter(), Map.of("error", "Usuario no encontrado"));
                }
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "ID inválido"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "No autorizado"));
            return;
        }

        String rol = (String) session.getAttribute("rol");
        if (!"ADMINISTRADOR".equals(rol)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Solo administradores"));
            return;
        }

        try {
            Usuario usuario = objectMapper.readValue(request.getInputStream(), Usuario.class);

            if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty() ||
                usuario.getCorreo() == null || usuario.getContrasena() == null || 
                usuario.getRol() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Datos incompletos"));
                return;
            }

            if (!validarCorreo(usuario.getCorreo())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Formato de correo inválido"));
                return;
            }

            if (usuarioDAO.correoExiste(usuario.getCorreo(), 0)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "El correo ya existe"));
                return;
            }

            if (usuario.getContrasena().length() < 6) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "La contraseña debe tener mínimo 6 caracteres"));
                return;
            }

            if (!validarRol(usuario.getRol())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Rol inválido"));
                return;
            }

            // Encriptar con BCrypt
            usuario.setContrasena(Encriptador.encriptarBCrypt(usuario.getContrasena()));

            boolean resultado = usuarioDAO.insertar(usuario);

            if (resultado) {
                Usuario usuarioActual = usuarioDAO.buscarPorCorreo(usuario.getCorreo());
                int idUsuarioSesion = ((Usuario) session.getAttribute("usuario")).getIdUsuario();
                auditoriaDAO.insertar(new Auditoria(idUsuarioSesion, "usuario", "INSERT", usuarioActual.getIdUsuario(), "Usuario creado: " + usuario.getCorreo(), request.getRemoteAddr()));
                
                response.setStatus(HttpServletResponse.SC_CREATED);
                objectMapper.writeValue(response.getWriter(), Map.of("success", true, "mensaje", "Usuario registrado correctamente"));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Error al registrar en BD"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor"));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "No autorizado"));
            return;
        }

        String rol = (String) session.getAttribute("rol");
        if (!"ADMINISTRADOR".equals(rol)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Solo administradores"));
            return;
        }

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "ID requerido"));
                return;
            }

            String idStr = pathInfo.substring(1);
            int id = Integer.parseInt(idStr);

            Usuario usuario = objectMapper.readValue(request.getInputStream(), Usuario.class);
            usuario.setIdUsuario(id);

            if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty() ||
                usuario.getCorreo() == null || usuario.getRol() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Datos incompletos"));
                return;
            }

            if (!validarCorreo(usuario.getCorreo())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Formato de correo inválido"));
                return;
            }

            if (usuarioDAO.correoExiste(usuario.getCorreo(), id)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "El correo ya existe"));
                return;
            }

            if (!validarRol(usuario.getRol())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Rol inválido"));
                return;
            }

            if (usuario.getContrasena() != null && !usuario.getContrasena().trim().isEmpty()) {
                if (usuario.getContrasena().length() < 6) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    objectMapper.writeValue(response.getWriter(), Map.of("error", "La contraseña debe tener mínimo 6 caracteres"));
                    return;
                }
                usuario.setContrasena(Encriptador.encriptarBCrypt(usuario.getContrasena()));
            }

            boolean resultado = usuarioDAO.actualizar(usuario);

            if (resultado) {
                int idUsuarioSesion = ((Usuario) session.getAttribute("usuario")).getIdUsuario();
                auditoriaDAO.insertar(new Auditoria(idUsuarioSesion, "usuario", "UPDATE", id, "Usuario editado: " + usuario.getCorreo(), request.getRemoteAddr()));

                objectMapper.writeValue(response.getWriter(), Map.of("success", true, "mensaje", "Usuario actualizado correctamente"));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Usuario no encontrado"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor"));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "No autorizado"));
            return;
        }

        String rol = (String) session.getAttribute("rol");
        if (!"ADMINISTRADOR".equals(rol)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Solo administradores"));
            return;
        }

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "ID requerido"));
                return;
            }

            String idStr = pathInfo.substring(1);
            int id = Integer.parseInt(idStr);
            
            Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
            if (usuarioSesion.getIdUsuario() == id) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "No puedes eliminar tu propio usuario"));
                return;
            }

            boolean resultado = usuarioDAO.eliminar(id);

            if (resultado) {
                int idUsuarioSesion = ((Usuario) session.getAttribute("usuario")).getIdUsuario();
                auditoriaDAO.insertar(new Auditoria(idUsuarioSesion, "usuario", "DELETE", id, "Usuario eliminado ID: " + id, request.getRemoteAddr()));

                objectMapper.writeValue(response.getWriter(), Map.of("success", true, "mensaje", "Usuario eliminado correctamente"));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Usuario no encontrado"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor"));
        }
    }
}
