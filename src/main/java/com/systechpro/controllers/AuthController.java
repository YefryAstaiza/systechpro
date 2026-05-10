package com.systechpro.controllers;

import com.systechpro.dao.UsuarioDAO;
import com.systechpro.dao.AuditoriaDAO;
import com.systechpro.dao.SolicitudPasswordDAO;
import com.systechpro.models.Auditoria;
import com.systechpro.models.Usuario;
import com.systechpro.models.SolicitudPassword;
import com.systechpro.utils.Encriptador;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "AuthController", urlPatterns = {"/api/auth/*"})
public class AuthController extends HttpServlet {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final AuditoriaDAO auditoriaDAO = new AuditoriaDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/login")) {
            login(request, response);
        } else if (pathInfo.equals("/reset-request")) {
            resetRequest(request, response);
        } else if (pathInfo.equals("/change-password")) {
            changePassword(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void login(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            Map<String, String> credenciales = objectMapper.readValue(request.getInputStream(), Map.class);
            String correo = credenciales.get("correo");
            String password = credenciales.get("password");

            if (correo == null || password == null || correo.isEmpty() || password.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Credenciales incompletas"));
                return;
            }

            // Buscar usuario por correo y luego verificar la contraseña con BCrypt
            Usuario usuario = usuarioDAO.buscarPorCorreo(correo);

            if (usuario != null && Encriptador.verificarPassword(password, usuario.getContrasena())) {
                
                if (usuario.isCambioObligatorio()) {
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("success", true);
                    resp.put("requirePasswordChange", true);
                    resp.put("usuario", Map.of(
                        "idUsuario", usuario.getIdUsuario(),
                        "nombre", usuario.getNombre(),
                        "correo", usuario.getCorreo(),
                        "rol", usuario.getRol()
                    ));
                    resp.put("mensaje", "Debe cambiar su contraseña antes de continuar");
                    objectMapper.writeValue(response.getWriter(), resp);
                    return;
                }

                HttpSession session = request.getSession();
                session.setAttribute("usuario", usuario);
                session.setAttribute("rol", usuario.getRol());
                
                // Registrar auditoria
                String ip = request.getRemoteAddr();
                Auditoria audit = new Auditoria(usuario.getIdUsuario(), "usuario", "LOGIN", usuario.getIdUsuario(), "Inicio de sesión", ip);
                auditoriaDAO.insertar(audit);
                
                Map<String, Object> respuesta = new HashMap<>();
                respuesta.put("success", true);
                respuesta.put("mensaje", "Login exitoso");
                respuesta.put("usuario", Map.of(
                    "idUsuario", usuario.getIdUsuario(),
                    "nombre", usuario.getNombre(),
                    "correo", usuario.getCorreo(),
                    "rol", usuario.getRol()
                ));
                
                objectMapper.writeValue(response.getWriter(), respuesta);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Credenciales inválidas"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor"));
        }
    }

    private void resetRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        try {
            Map<String, String> body = objectMapper.readValue(request.getInputStream(), Map.class);
            String correo = body.get("correo");
            
            Usuario u = usuarioDAO.buscarPorCorreo(correo);
            if (u == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Usuario no encontrado"));
                return;
            }
            
            SolicitudPasswordDAO solDAO = new SolicitudPasswordDAO();
            if (solDAO.buscarPendientePorUsuario(u.getIdUsuario()) != null) {
                objectMapper.writeValue(response.getWriter(), Map.of("success", true, "mensaje", "Ya tienes una solicitud pendiente. Contacta a un administrador."));
                return;
            }
            
            SolicitudPassword sol = new SolicitudPassword();
            sol.setIdUsuario(u.getIdUsuario());
            
            if (solDAO.insertar(sol)) {
                objectMapper.writeValue(response.getWriter(), Map.of("success", true, "mensaje", "Solicitud enviada. Contacta a un administrador para la aprobación."));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Error al procesar solicitud"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor"));
        }
    }

    private void changePassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        try {
            Map<String, Object> body = objectMapper.readValue(request.getInputStream(), Map.class);
            int idUsuario = Integer.parseInt(body.get("idUsuario").toString());
            String newPass = (String) body.get("newPassword");
            
            if (newPass == null || newPass.length() < 6) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "La contraseña debe tener al menos 6 caracteres"));
                return;
            }
            
            String hashed = Encriptador.encriptarBCrypt(newPass);
            if (usuarioDAO.actualizarPasswordForceChange(idUsuario, hashed, false)) {
                objectMapper.writeValue(response.getWriter(), Map.of("success", true, "mensaje", "Contraseña actualizada. Ya puedes iniciar sesión."));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Error al actualizar contraseña"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor"));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo != null && pathInfo.equals("/logout")) {
            logout(request, response);
        } else if (pathInfo != null && pathInfo.equals("/sesion")) {
            verificarSesion(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), Map.of("success", true, "mensaje", "Sesión cerrada"));
    }

    private void verificarSesion(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        response.setContentType("application/json");
        HttpSession session = request.getSession(false);
        
        if (session != null && session.getAttribute("usuario") != null) {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            objectMapper.writeValue(response.getWriter(), Map.of(
                "authenticated", true,
                "usuario", Map.of(
                    "idUsuario", usuario.getIdUsuario(),
                    "nombre", usuario.getNombre(),
                    "correo", usuario.getCorreo(),
                    "rol", usuario.getRol()
                )
            ));
        } else {
            objectMapper.writeValue(response.getWriter(), Map.of("authenticated", false));
        }
    }
}
