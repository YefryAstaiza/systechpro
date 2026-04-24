package com.systechpro.controllers;

import com.systechpro.dao.UsuarioDAO;
import com.systechpro.models.Usuario;
import com.systechpro.utils.Encriptador;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "AuthController", urlPatterns = {"/api/auth/*"})
public class AuthController extends HttpServlet {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/login")) {
            login(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void login(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        response.setContentType("application/json");
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

            // Encriptar la contraseña recibida para comparar con la almacenada en BD
            String passwordEncriptada = Encriptador.encriptarMD5(password);
            Usuario usuario = usuarioDAO.login(correo, passwordEncriptada);

            if (usuario != null) {
                HttpSession session = request.getSession();
                session.setAttribute("usuario", usuario);
                session.setAttribute("rol", usuario.getRol());
                
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