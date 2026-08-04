package com.systechpro.controllers;

import com.systechpro.dao.SolicitudPasswordDAO;
import com.systechpro.models.Rol;
import com.systechpro.models.SolicitudPassword;
import com.systechpro.models.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "AdminController", urlPatterns = {"/api/admin/*"})
public class AdminController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final SolicitudPasswordDAO solicitudPasswordDAO = new SolicitudPasswordDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "No autorizado"));
            return;
        }

        String rol = (String) session.getAttribute("rol");
        if (!Rol.ADMINISTRADOR.name().equalsIgnoreCase(rol)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Acceso restringido"));
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/password-requests")) {
            try {
                List<SolicitudPassword> solicitudes = solicitudPasswordDAO.listar();
                objectMapper.writeValue(response.getWriter(), solicitudes);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor"));
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "No autorizado"));
            return;
        }

        String rol = (String) session.getAttribute("rol");
        if (!Rol.ADMINISTRADOR.name().equalsIgnoreCase(rol)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Acceso restringido"));
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.startsWith("/password-requests/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String[] parts = pathInfo.split("/");
        if (parts.length != 4) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            int idSolicitud = Integer.parseInt(parts[2]);
            String accion = parts[3];
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            String estado;
            String passwordTemporal = null;

            if ("approve".equalsIgnoreCase(accion)) {
                estado = "APROBADA";
                passwordTemporal = generarClaveTemporal(10);
            } else if ("reject".equalsIgnoreCase(accion)) {
                estado = "RECHAZADA";
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            boolean actualizado = solicitudPasswordDAO.actualizarEstado(idSolicitud, estado, usuario.getIdUsuario(), passwordTemporal);
            if (actualizado) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("success", true);
                resp.put("mensaje", estado.equals("APROBADA") ? "Solicitud aprobada" : "Solicitud rechazada");
                if (passwordTemporal != null) {
                    resp.put("passwordTemporal", passwordTemporal);
                }
                objectMapper.writeValue(response.getWriter(), resp);
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Error al actualizar solicitud"));
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private String generarClaveTemporal(int longitud) {
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            sb.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
