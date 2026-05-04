package com.systechpro.controllers;

import com.systechpro.dao.PrestamoDAO;
import com.systechpro.models.Prestamo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.List;

@WebServlet(name = "AdministrativoController", urlPatterns = {"/api/administrativo/*"})
public class AdministrativoController extends HttpServlet {
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
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
        if (!"ADMINISTRATIVO".equals(rol)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Acceso restringido"));
            return;
        }

        try {
            List<Prestamo> prestamos = prestamoDAO.listarPorUsuario(((com.systechpro.models.Usuario) session.getAttribute("usuario")).getIdUsuario());
            objectMapper.writeValue(response.getWriter(), prestamos);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
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
        if (!"ADMINISTRATIVO".equals(rol)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Acceso restringido"));
            return;
        }

        try {
            Map<String, Object> datos = objectMapper.readValue(request.getInputStream(), Map.class);
            int idDispositivo = Integer.parseInt(datos.get("idDispositivo").toString());
            int idSalon = Integer.parseInt(datos.get("idSalon").toString());
            String fechaInicioStr = (String) datos.get("fechaInicio");
            String fechaFinStr = (String) datos.get("fechaFin");

            if (fechaInicioStr == null || fechaFinStr == null || fechaInicioStr.isEmpty() || fechaFinStr.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Faltan datos requeridos"));
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime inicio = LocalDateTime.parse(fechaInicioStr, formatter);
            LocalDateTime fin = LocalDateTime.parse(fechaFinStr, formatter);

            if (!fin.isAfter(inicio)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "La fecha de fin debe ser mayor a la fecha de inicio"));
                return;
            }

            Prestamo prestamo = new Prestamo();
            prestamo.setIdUsuario(((com.systechpro.models.Usuario) session.getAttribute("usuario")).getIdUsuario());
            prestamo.setIdDispositivo(idDispositivo);
            prestamo.setIdSalon(idSalon);
            prestamo.setFechaInicio(Timestamp.valueOf(inicio));
            prestamo.setFechaFin(Timestamp.valueOf(fin));
            prestamo.setEstado("PENDIENTE");

            boolean resultado = prestamoDAO.insertar(prestamo);
            if (resultado) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                objectMapper.writeValue(response.getWriter(), Map.of("success", true, "mensaje", "Solicitud de préstamo registrada"));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Error al registrar la solicitud"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor"));
        }
    }
}
