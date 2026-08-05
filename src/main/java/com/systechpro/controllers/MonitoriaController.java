package com.systechpro.controllers;

import com.systechpro.dao.DispositivoDAO;
import com.systechpro.dao.PrestamoMonitoriaDAO;
import com.systechpro.models.Dispositivo;
import com.systechpro.models.EstadoDispositivo;
import com.systechpro.models.PrestamoMonitoria;
import com.systechpro.models.Rol;
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

/**
 * Checkout rápido de dispositivos para monitoría: un Docente o Administrativo,
 * ya autenticado, toma y devuelve un dispositivo con su propia sesión como
 * firma - sin aprobación, sin pedir nombre ni fecha de fin.
 */
@WebServlet(name = "MonitoriaController", urlPatterns = {"/api/monitoria/*"})
public class MonitoriaController extends HttpServlet {
    private final PrestamoMonitoriaDAO monitoriaDAO = new PrestamoMonitoriaDAO();
    private final DispositivoDAO dispositivoDAO = new DispositivoDAO();
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

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/disponibles")) {
                List<Dispositivo> disponibles = dispositivoDAO.listarPorFiltro(null, EstadoDispositivo.DISPONIBLE.name());
                objectMapper.writeValue(response.getWriter(), disponibles);
            } else if (pathInfo.equals("/activas")) {
                // Todas (Admin/Técnico) o solo las propias (Docente/Administrativo)
                boolean puedeVerTodas = Rol.ADMINISTRADOR.name().equalsIgnoreCase(usuario.getRol())
                        || Rol.TECNICO.name().equalsIgnoreCase(usuario.getRol());
                List<PrestamoMonitoria> activas = puedeVerTodas
                        ? monitoriaDAO.listarActivos()
                        : monitoriaDAO.listarActivasPorUsuario(usuario.getIdUsuario());
                objectMapper.writeValue(response.getWriter(), activas);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
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

        Usuario usuario = usuarioDeSesion(request);
        if (usuario == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "No autorizado"));
            return;
        }

        if (!tienePermiso(usuario.getRol())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Solo Docentes o Administrativos pueden usar monitoría"));
            return;
        }

        try {
            Map<String, Object> datos = objectMapper.readValue(request.getInputStream(), Map.class);
            if (!datos.containsKey("idDispositivo")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "idDispositivo requerido"));
                return;
            }
            int idDispositivo = Integer.parseInt(datos.get("idDispositivo").toString());

            Dispositivo dispositivo = dispositivoDAO.buscarPorId(idDispositivo);
            if (dispositivo == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Dispositivo no encontrado"));
                return;
            }
            if (!EstadoDispositivo.DISPONIBLE.name().equals(dispositivo.getEstado())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "El dispositivo no está disponible"));
                return;
            }

            boolean tomado = monitoriaDAO.tomar(idDispositivo, usuario.getIdUsuario());
            if (!tomado) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Error al registrar la toma"));
                return;
            }

            dispositivoDAO.actualizarEstado(idDispositivo, EstadoDispositivo.EN_USO.name());
            response.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(response.getWriter(), Map.of("success", true, "mensaje", "Dispositivo tomado"));
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

        if (!tienePermiso(usuario.getRol())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Solo Docentes o Administrativos pueden usar monitoría"));
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.endsWith("/devolver")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            String[] parts = pathInfo.substring(1).split("/");
            if (parts.length != 2) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            int idMonitoria = Integer.parseInt(parts[0]);

            PrestamoMonitoria monitoria = monitoriaDAO.buscarPorId(idMonitoria);
            if (monitoria == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Registro de monitoría no encontrado"));
                return;
            }

            boolean devuelto = monitoriaDAO.devolver(idMonitoria, usuario.getIdUsuario());
            if (!devuelto) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "No se pudo devolver (ya devuelto o no te pertenece)"));
                return;
            }

            dispositivoDAO.actualizarEstado(monitoria.getIdDispositivo(), EstadoDispositivo.DISPONIBLE.name());
            objectMapper.writeValue(response.getWriter(), Map.of("success", true, "mensaje", "Dispositivo devuelto"));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Ruta inválida"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor"));
        }
    }

    private boolean tienePermiso(String rol) {
        return Rol.DOCENTE.name().equalsIgnoreCase(rol) || Rol.ADMINISTRATIVO.name().equalsIgnoreCase(rol);
    }

    private Usuario usuarioDeSesion(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        return (Usuario) session.getAttribute("usuario");
    }
}
