package com.systechpro.controllers;

import com.systechpro.dao.MantenimientoDAO;
import com.systechpro.dao.DispositivoDAO;
import com.systechpro.models.Mantenimiento;
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

@WebServlet(name = "MantenimientoController", urlPatterns = {"/api/mantenimientos/*"})
public class MantenimientoController extends HttpServlet {
    private final MantenimientoDAO mantenimientoDAO = new MantenimientoDAO();
    private final DispositivoDAO dispositivoDAO = new DispositivoDAO();
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

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                List<Mantenimiento> mantenimientos = mantenimientoDAO.listar();
                objectMapper.writeValue(response.getWriter(), mantenimientos);
            } else if (pathInfo.startsWith("/dispositivo/")) {
                // Historial de mantenimiento de un dispositivo
                String idStr = pathInfo.substring("/dispositivo/".length());
                int idDispositivo = Integer.parseInt(idStr);
                List<Mantenimiento> mantenimientos = mantenimientoDAO.listarPorDispositivo(idDispositivo);
                objectMapper.writeValue(response.getWriter(), mantenimientos);
            } else {
                String idStr = pathInfo.substring(1);
                int id = Integer.parseInt(idStr);
                Mantenimiento mantenimiento = mantenimientoDAO.buscarPorId(id);

                if (mantenimiento != null) {
                    objectMapper.writeValue(response.getWriter(), mantenimiento);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    objectMapper.writeValue(response.getWriter(), Map.of("error", "Mantenimiento no encontrado"));
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
        
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "No autorizado"));
            return;
        }

        String rol = (String) session.getAttribute("rol");
        if (!"ADMINISTRADOR".equals(rol) && !"TECNICO".equals(rol)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Solo administradores o técnicos"));
            return;
        }

        try {
            Map<String, Object> datos = objectMapper.readValue(request.getInputStream(), Map.class);
            
            int idDispositivo = ((Number) datos.get("idDispositivo")).intValue();
            int idUsuario = ((com.systechpro.models.Usuario) session.getAttribute("usuario")).getIdUsuario();
            String tipo = (String) datos.get("tipo");
            String descripcion = (String) datos.get("descripcion");

            if (idDispositivo == 0 || tipo == null || descripcion == null || descripcion.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Datos incompletos"));
                return;
            }

            // Crear mantenimiento y cambiar estado del dispositivo
            Mantenimiento mantenimiento = new Mantenimiento();
            mantenimiento.setIdDispositivo(idDispositivo);
            mantenimiento.setIdUsuario(idUsuario);
            mantenimiento.setTipo(tipo);
            mantenimiento.setDescripcion(descripcion);
            mantenimiento.setFechaInicio(new Timestamp(System.currentTimeMillis()));
            mantenimiento.setEstado("EN_PROCESO");

            boolean resultado = mantenimientoDAO.insertar(mantenimiento);

            if (resultado) {
                // Cambiar estado del dispositivo a mantenimiento
                dispositivoDAO.actualizarEstado(idDispositivo, "MANTENIMIENTO");
                
                response.setStatus(HttpServletResponse.SC_CREATED);
                objectMapper.writeValue(response.getWriter(), Map.of("success", true, "mensaje", "Mantenimiento registrado"));
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
        
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "No autorizado"));
            return;
        }

        String rol = (String) session.getAttribute("rol");
        if (!"ADMINISTRADOR".equals(rol) && !"TECNICO".equals(rol)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Solo administradores o técnicos"));
            return;
        }

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "ID requerido"));
                return;
            }

            String idStr = pathInfo.substring(1);
            int id = Integer.parseInt(idStr);

            Map<String, Object> datos = objectMapper.readValue(request.getInputStream(), Map.class);
            String estado = (String) datos.get("estado");

            Mantenimiento mantenimiento = mantenimientoDAO.buscarPorId(id);
            if (mantenimiento == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Mantenimiento no encontrado"));
                return;
            }

            mantenimiento.setEstado(estado);
            if ("FINALIZADO".equals(estado)) {
                mantenimiento.setFechaFin(new Timestamp(System.currentTimeMillis()));
                // Device returns to available
                dispositivoDAO.actualizarEstado(mantenimiento.getIdDispositivo(), "DISPONIBLE");
            }

            boolean resultado = mantenimientoDAO.actualizar(mantenimiento);

            if (resultado) {
                objectMapper.writeValue(response.getWriter(), Map.of("success", true, "mensaje", "Mantenimiento actualizado"));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Error al actualizar"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor"));
        }
    }
}
