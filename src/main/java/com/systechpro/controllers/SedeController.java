package com.systechpro.controllers;

import com.systechpro.dao.SedeDAO;
import com.systechpro.dao.SalonDAO;
import com.systechpro.models.Sede;
import com.systechpro.models.Salon;
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

@WebServlet(name = "SedeController", urlPatterns = {"/api/sedes/*"})
public class SedeController extends HttpServlet {
    private final SedeDAO sedeDAO = new SedeDAO();
    private final SalonDAO salonDAO = new SalonDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

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

        String pathInfo = request.getPathInfo();

        try {
            // GET /api/sedes/{id}/salones
            if (pathInfo != null && pathInfo.matches("/\\d+/salones")) {
                String[] parts = pathInfo.split("/");
                int idSede = Integer.parseInt(parts[1]);
                List<Salon> salones = salonDAO.listarPorSede(idSede);
                objectMapper.writeValue(response.getWriter(), salones);
            } else {
                // GET /api/sedes
                List<Sede> sedes = sedeDAO.listar();
                objectMapper.writeValue(response.getWriter(), sedes);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor: " + e.getMessage()));
        }
    }
}
