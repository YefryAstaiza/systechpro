package com.systechpro.controllers;

import com.systechpro.dao.NotificacionDAO;
import com.systechpro.models.Notificacion;
import com.systechpro.models.Usuario;
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

@WebServlet(name = "NotificacionController", urlPatterns = {"/api/notificaciones/*"})
public class NotificacionController extends HttpServlet {
    private final NotificacionDAO notificacionDAO = new NotificacionDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        Usuario usuario = usuarioDeSesion(request);
        if (usuario == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "No autorizado"));
            return;
        }

        try {
            List<Notificacion> lista = notificacionDAO.listarPorUsuario(usuario.getIdUsuario());
            int noLeidas = notificacionDAO.contarNoLeidas(usuario.getIdUsuario());
            objectMapper.writeValue(response.getWriter(), Map.of("notificaciones", lista, "noLeidas", noLeidas));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor"));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        Usuario usuario = usuarioDeSesion(request);
        if (usuario == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "No autorizado"));
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Ruta inválida"));
            return;
        }

        try {
            String recurso = pathInfo.substring(1);
            boolean resultado;

            if (recurso.equals("leer-todas")) {
                resultado = notificacionDAO.marcarTodasLeidas(usuario.getIdUsuario());
            } else {
                int idNotificacion = Integer.parseInt(recurso);
                resultado = notificacionDAO.marcarLeida(idNotificacion, usuario.getIdUsuario());
            }

            if (resultado) {
                objectMapper.writeValue(response.getWriter(), Map.of("success", true));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Notificación no encontrada"));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Ruta inválida"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor"));
        }
    }

    private Usuario usuarioDeSesion(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        return (Usuario) session.getAttribute("usuario");
    }
}
