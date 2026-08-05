package com.systechpro.controllers;

import com.systechpro.dao.NotificacionDAO;
import com.systechpro.dao.SolicitudPasswordDAO;
import com.systechpro.dao.UsuarioDAO;
import com.systechpro.models.Rol;
import com.systechpro.models.SolicitudPassword;
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
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "AdminController", urlPatterns = {"/api/admin/*"})
public class AdminController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final SolicitudPasswordDAO solicitudPasswordDAO = new SolicitudPasswordDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final NotificacionDAO notificacionDAO = new NotificacionDAO();
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
            Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
            String estado;
            String passwordTemporal = null;

            SolicitudPassword solicitud = solicitudPasswordDAO.buscarPorId(idSolicitud);
            if (solicitud == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Solicitud no encontrada"));
                return;
            }

            if ("approve".equalsIgnoreCase(accion)) {
                estado = "APROBADA";
                passwordTemporal = generarClaveTemporal(10);

                // Aplicar la clave temporal al usuario real (antes solo se guardaba en la solicitud
                // y nunca se escribía en usuario.contrasena, por lo que la clave "temporal" jamás
                // servía para iniciar sesión).
                String hashTemporal = Encriptador.encriptarBCrypt(passwordTemporal);
                boolean passwordAplicada = usuarioDAO.actualizarPasswordForceChange(solicitud.getIdUsuario(), hashTemporal, true);
                if (!passwordAplicada) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    objectMapper.writeValue(response.getWriter(), Map.of("error", "Error al aplicar la contraseña temporal al usuario"));
                    return;
                }
            } else if ("reject".equalsIgnoreCase(accion)) {
                estado = "RECHAZADA";
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            boolean actualizado = solicitudPasswordDAO.actualizarEstado(idSolicitud, estado, usuarioSesion.getIdUsuario(), passwordTemporal);
            if (actualizado) {
                if (estado.equals("APROBADA")) {
                    notificacionDAO.crear(solicitud.getIdUsuario(),
                            NotificacionDAO.TIPO_PASSWORD_APROBADA,
                            "Tu solicitud de restablecimiento de contraseña fue aprobada. Consulta con un administrador tu clave temporal.",
                            "Tu solicitud de restablecimiento de contraseña fue aprobada.\n\n" +
                                    "Tu clave temporal es: " + passwordTemporal + "\n\n" +
                                    "Inicia sesión con esta clave; el sistema te pedirá cambiarla de inmediato. " +
                                    "Vence en 24 horas o al usarla, lo que ocurra primero.");
                } else {
                    notificacionDAO.crear(solicitud.getIdUsuario(),
                            NotificacionDAO.TIPO_PASSWORD_RECHAZADA,
                            "Tu solicitud de restablecimiento de contraseña fue rechazada.");
                }

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
