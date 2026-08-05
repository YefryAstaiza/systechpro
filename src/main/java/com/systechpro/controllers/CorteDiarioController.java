package com.systechpro.controllers;

import com.systechpro.dao.CorteDiarioDAO;
import com.systechpro.dao.DispositivoDAO;
import com.systechpro.dao.PrestamoDAO;
import com.systechpro.models.CorteDiario;
import com.systechpro.models.Dispositivo;
import com.systechpro.models.EstadoDispositivo;
import com.systechpro.models.EstadoPrestamo;
import com.systechpro.models.Prestamo;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Corte diario: snapshot manual e histórico del inventario (total, disponibles,
 * y quién tiene qué préstamo activo en ese momento). Solo Administrador o Técnico.
 */
@WebServlet(name = "CorteDiarioController", urlPatterns = {"/api/cortes/*"})
public class CorteDiarioController extends HttpServlet {
    private final CorteDiarioDAO corteDAO = new CorteDiarioDAO();
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final DispositivoDAO dispositivoDAO = new DispositivoDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        Usuario usuario = usuarioAutorizado(request, response);
        if (usuario == null) return;

        String pathInfo = request.getPathInfo();
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                List<CorteDiario> historial = corteDAO.listar();
                objectMapper.writeValue(response.getWriter(), historial);
            } else {
                int idCorte = Integer.parseInt(pathInfo.substring(1));
                CorteDiario corte = corteDAO.buscarPorId(idCorte);
                if (corte == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    objectMapper.writeValue(response.getWriter(), Map.of("error", "Corte no encontrado"));
                    return;
                }
                objectMapper.writeValue(response.getWriter(), corte);
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

        Usuario usuario = usuarioAutorizado(request, response);
        if (usuario == null) return;

        try {
            // disponibles/en_prestamo/en_mantenimiento se cuentan desde dispositivo.estado (única fuente
            // de verdad) para que la suma siempre cuadre con el total, sin importar el estado de las
            // tablas prestamo/mantenimiento (que solo se usan para completar el detalle "quién y desde cuándo").
            List<Dispositivo> todos = dispositivoDAO.listar();
            int total = todos.size();
            int disponibles = 0;
            int enMantenimiento = 0;
            Set<Integer> idsEnUso = new HashSet<>();
            for (Dispositivo d : todos) {
                if (EstadoDispositivo.DISPONIBLE.name().equals(d.getEstado())) {
                    disponibles++;
                } else if (EstadoDispositivo.MANTENIMIENTO.name().equals(d.getEstado())) {
                    enMantenimiento++;
                } else if (EstadoDispositivo.EN_USO.name().equals(d.getEstado())) {
                    idsEnUso.add(d.getIdDispositivo());
                }
            }

            // Un dispositivo EN_USO puede tener más de un préstamo APROBADO si quedó algo huérfano;
            // nos quedamos con el más reciente por dispositivo (ya viene ordenado por fecha_inicio DESC).
            Map<Integer, Prestamo> prestamoPorDispositivo = new LinkedHashMap<>();
            for (Prestamo p : prestamoDAO.listarPorEstado(EstadoPrestamo.APROBADO.name())) {
                if (idsEnUso.contains(p.getIdDispositivo())) {
                    prestamoPorDispositivo.putIfAbsent(p.getIdDispositivo(), p);
                }
            }
            List<Prestamo> activos = new ArrayList<>(prestamoPorDispositivo.values());

            // enPrestamo se toma de idsEnUso.size() (no de activos.size()) para que la suma con
            // disponibles/enMantenimiento cuadre siempre con el total, aunque algún dispositivo EN_USO
            // no tenga (todavía) un préstamo que lo respalde.
            CorteDiario corte = corteDAO.generar(usuario.getIdUsuario(), total, disponibles, idsEnUso.size(), enMantenimiento, activos);
            if (corte == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Error al generar el corte"));
                return;
            }

            response.setStatus(HttpServletResponse.SC_CREATED);
            objectMapper.writeValue(response.getWriter(), corte);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor"));
        }
    }

    private Usuario usuarioAutorizado(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "No autorizado"));
            return null;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        String rol = (String) session.getAttribute("rol");
        if (!Rol.ADMINISTRADOR.name().equalsIgnoreCase(rol) && !Rol.TECNICO.name().equalsIgnoreCase(rol)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Solo Administrador o Técnico pueden usar el corte diario"));
            return null;
        }
        return usuario;
    }
}
