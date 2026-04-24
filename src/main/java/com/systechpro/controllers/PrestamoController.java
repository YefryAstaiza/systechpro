package com.systechpro.controllers;

import com.systechpro.dao.PrestamoDAO;
import com.systechpro.dao.DispositivoDAO;
import com.systechpro.models.Prestamo;
import com.systechpro.models.Dispositivo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@WebServlet(name = "PrestamoController", urlPatterns = {"/api/prestamos/*"})
public class PrestamoController extends HttpServlet {
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final DispositivoDAO dispositivoDAO = new DispositivoDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("usuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "No autorizado"));
            return;
        }

        String rol = (String) session.getAttribute("rol");

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Listar préstamos según el rol
                List<Prestamo> prestamos;
                if ("ADMIN".equals(rol)) {
                    String estado = request.getParameter("estado");
                    if (estado != null && !estado.isEmpty()) {
                        prestamos = prestamoDAO.listarPorEstado(estado);
                    } else {
                        prestamos = prestamoDAO.listar();
                    }
                } else {
                    int idUsuario = ((com.systechpro.models.Usuario) session.getAttribute("usuario")).getIdUsuario();
                    prestamos = prestamoDAO.listarPorUsuario(idUsuario);
                }
                objectMapper.writeValue(response.getWriter(), prestamos);
            } else if (pathInfo.equals("/pendientes") && "ADMIN".equals(rol)) {
                // Préstamos pendientes (solo admin)
                List<Prestamo> prestamos = prestamoDAO.listarPorEstado("PENDIENTE");
                objectMapper.writeValue(response.getWriter(), prestamos);
            } else {
                // Obtener préstamo por ID
                String idStr = pathInfo.substring(1);
                int id = Integer.parseInt(idStr);
                Prestamo prestamo = prestamoDAO.buscarPorId(id);

                if (prestamo != null) {
                    objectMapper.writeValue(response.getWriter(), prestamo);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    objectMapper.writeValue(response.getWriter(), Map.of("error", "Préstamo no encontrado"));
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

        try {
            Map<String, Object> datos = objectMapper.readValue(request.getInputStream(), Map.class);
            
            int idDispositivo = ((Number) datos.get("idDispositivo")).intValue();
            int idSalon = ((Number) datos.get("idSalon")).intValue();
            int idUsuario = ((com.systechpro.models.Usuario) session.getAttribute("usuario")).getIdUsuario();
            
            // Verificar que el dispositivo esté disponible
            Dispositivo dispositivo = dispositivoDAO.buscarPorId(idDispositivo);
            if (dispositivo == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Dispositivo no encontrado"));
                return;
            }
            
            if (!"DISPONIBLE".equals(dispositivo.getEstado())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Dispositivo no disponible"));
                return;
            }

            // Crear préstamo
            Prestamo prestamo = new Prestamo();
            prestamo.setIdUsuario(idUsuario);
            prestamo.setIdDispositivo(idDispositivo);
            prestamo.setIdSalon(idSalon);
            prestamo.setFechaInicio(new Timestamp(System.currentTimeMillis()));
            prestamo.setEstado("PENDIENTE");
            
            // Fecha fin (por defecto 7 días)
            long fechaFin = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000);
            prestamo.setFechaFin(new Timestamp(fechaFin));

            boolean resultado = prestamoDAO.insertar(prestamo);

            if (resultado) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                objectMapper.writeValue(response.getWriter(), Map.of("success", true, "mensaje", "Solicitud de préstamo enviada"));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Error al registrar"));
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
        if (!"ADMIN".equals(rol)) {
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

            // Parsear /{id}/estado
            String[] parts = pathInfo.substring(1).split("/");
            if (parts.length != 2 || !parts[1].equals("estado")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Ruta inválida"));
                return;
            }

            int id = Integer.parseInt(parts[0]);
            Map<String, Object> datos = objectMapper.readValue(request.getInputStream(), Map.class);
            String nuevoEstado = (String) datos.get("estado");

            if (nuevoEstado == null || (!nuevoEstado.equals("APROBADO") && !nuevoEstado.equals("RECHAZADO") && !nuevoEstado.equals("DEVUELTO"))) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Estado inválido"));
                return;
            }

            boolean resultado = prestamoDAO.actualizarEstado(id, nuevoEstado);

            if (resultado) {
                // Si se aprueba, cambiar estado del dispositivo
                if ("APROBADO".equals(nuevoEstado)) {
                    Prestamo prestamo = prestamoDAO.buscarPorId(id);
                    if (prestamo != null) {
                        dispositivoDAO.actualizarEstado(prestamo.getIdDispositivo(), "EN_USO");
                    }
                } else if ("RECHAZADO".equals(nuevoEstado) || "DEVUELTO".equals(nuevoEstado)) {
                    // Si se rechaza o devuelve, dispositivo vuelve a disponible
                    Prestamo prestamo = prestamoDAO.buscarPorId(id);
                    if (prestamo != null) {
                        dispositivoDAO.actualizarEstado(prestamo.getIdDispositivo(), "DISPONIBLE");
                    }
                }
                
                objectMapper.writeValue(response.getWriter(), Map.of("success", true, "mensaje", "Estado actualizado"));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Préstamo no encontrado"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor"));
        }
    }
}
