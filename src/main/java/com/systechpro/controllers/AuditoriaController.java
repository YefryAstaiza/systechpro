package com.systechpro.controllers;

import com.systechpro.dao.AuditoriaDAO;
import com.systechpro.models.Auditoria;
import com.systechpro.models.Rol;
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

@WebServlet(name = "AuditoriaController", urlPatterns = {"/api/auditoria/*"})
public class AuditoriaController extends HttpServlet {
    private final AuditoriaDAO auditoriaDAO = new AuditoriaDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        if (!Rol.ADMINISTRADOR.name().equals(rol)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Acceso denegado. Solo administradores."));
            return;
        }

        try {
            List<Auditoria> lista = auditoriaDAO.listar();
            objectMapper.writeValue(response.getWriter(), lista);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor"));
        }
    }
}
