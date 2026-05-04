package com.systechpro.controllers;

import com.systechpro.dao.DispositivoDAO;
import com.systechpro.dao.MantenimientoDAO;
import com.systechpro.models.Dispositivo;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "TecnicoController", urlPatterns = {"/api/tecnico/*"})
public class TecnicoController extends HttpServlet {
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

        String rol = (String) session.getAttribute("rol");
        if (!"TECNICO".equals(rol)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Acceso restringido"));
            return;
        }

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/dashboard")) {
                enviarDashboard(response);
            } else if (pathInfo.equals("/historial")) {
                enviarHistorial(response);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Recurso no encontrado"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor"));
        }
    }

    private void enviarDashboard(HttpServletResponse response) throws IOException {
        List<Mantenimiento> mantenimientos = mantenimientoDAO.listar();
        int mantenimientosEnProceso = 0;
        int mantenimientosFinalizados = 0;
        List<Integer> dispositivosEnMantenimientoIds = new ArrayList<>();

        List<Map<String, Object>> recientes = new ArrayList<>();
        int recientesAgregados = 0;

        for (Mantenimiento m : mantenimientos) {
            if ("EN_PROCESO".equals(m.getEstado())) {
                mantenimientosEnProceso++;
                if (!dispositivosEnMantenimientoIds.contains(m.getIdDispositivo())) {
                    dispositivosEnMantenimientoIds.add(m.getIdDispositivo());
                }
            }
            if ("FINALIZADO".equals(m.getEstado())) {
                mantenimientosFinalizados++;
            }

            if (recientesAgregados < 3) {
                recientes.add(mapearMantenimientoResumen(m));
                recientesAgregados++;
            }
        }

        int dispositivosEnMantenimiento = dispositivosEnMantenimientoIds.size();

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("dispositivosEnMantenimiento", dispositivosEnMantenimiento);
        respuesta.put("mantenimientosEnProceso", mantenimientosEnProceso);
        respuesta.put("mantenimientosFinalizados", mantenimientosFinalizados);
        respuesta.put("recientes", recientes);

        objectMapper.writeValue(response.getWriter(), respuesta);
    }

    private void enviarHistorial(HttpServletResponse response) throws IOException {
        List<Mantenimiento> mantenimientos = mantenimientoDAO.listar();
        List<Map<String, Object>> historial = new ArrayList<>();

        for (Mantenimiento m : mantenimientos) {
            historial.add(mapearMantenimientoResumen(m));
        }

        objectMapper.writeValue(response.getWriter(), historial);
    }

    private Map<String, Object> mapearMantenimientoResumen(Mantenimiento m) {
        Map<String, Object> map = new HashMap<>();
        map.put("idMantenimiento", m.getIdMantenimiento());
        map.put("nombreDispositivo", m.getNombreDispositivo());
        map.put("tipo", traducirTipo(m.getTipo()));
        map.put("ubicacion", obtenerUbicacionDispositivo(m.getIdDispositivo()));
        map.put("fechaInicio", formatearFecha(m.getFechaInicio()));
        map.put("estado", m.getEstado());
        map.put("estadoTexto", traducirEstado(m.getEstado()));
        map.put("estadoClass", "EN_PROCESO".equals(m.getEstado()) ? "status-en-proceso" : "status-finalizado");
        map.put("descripcion", m.getDescripcion());
        return map;
    }

    private String traducirEstado(String estado) {
        if ("FINALIZADO".equals(estado)) return "Finalizado";
        if ("EN_PROCESO".equals(estado)) return "En proceso";
        return estado != null ? estado : "N/A";
    }

    private String traducirTipo(String tipo) {
        if ("CORRECTIVO".equals(tipo)) return "Correctivo";
        if ("PREVENTIVO".equals(tipo)) return "Preventivo";
        return tipo != null ? tipo : "N/A";
    }

    private String obtenerUbicacionDispositivo(int idDispositivo) {
        Dispositivo dispositivo = dispositivoDAO.buscarPorId(idDispositivo);
        return dispositivo != null ? (dispositivo.getUbicacion() != null ? dispositivo.getUbicacion() : "N/A") : "N/A";
    }

    private String formatearFecha(Timestamp timestamp) {
        if (timestamp == null) return "N/A";
        return String.valueOf(timestamp.getTime());
    }
}
