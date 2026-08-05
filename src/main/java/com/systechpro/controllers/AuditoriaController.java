package com.systechpro.controllers;

import com.systechpro.dao.AuditoriaDAO;
import com.systechpro.models.Auditoria;
import com.systechpro.models.Rol;
import com.systechpro.utils.PaginacionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
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
            String busqueda = request.getParameter("q");
            Timestamp fechaDesde = PaginacionUtil.parseFechaInicioDia(request.getParameter("fechaDesde"));
            Timestamp fechaHasta = PaginacionUtil.parseFechaFinDia(request.getParameter("fechaHasta"));
            int pagina = PaginacionUtil.parsePagina(request.getParameter("pagina"));
            int tamano = PaginacionUtil.parseTamano(request.getParameter("tamano"));

            List<Auditoria> lista = auditoriaDAO.listar(busqueda, fechaDesde, fechaHasta, pagina, tamano);
            int total = auditoriaDAO.contarTotal(busqueda, fechaDesde, fechaHasta);

            Map<String, Object> resp = new HashMap<>();
            resp.put("datos", lista);
            resp.put("total", total);
            resp.put("pagina", pagina);
            resp.put("tamanoPagina", tamano);
            objectMapper.writeValue(response.getWriter(), resp);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor"));
        }
    }
}
