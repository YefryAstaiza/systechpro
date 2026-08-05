package com.systechpro.controllers;

import com.systechpro.dao.MantenimientoDAO;
import com.systechpro.dao.DispositivoDAO;
import com.systechpro.dao.NotificacionDAO;
import com.systechpro.dao.UsuarioDAO;
import com.systechpro.models.Dispositivo;
import com.systechpro.models.EstadoDispositivo;
import com.systechpro.models.EstadoMantenimiento;
import com.systechpro.models.Mantenimiento;
import com.systechpro.models.Rol;
import com.systechpro.models.Usuario;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "MantenimientoController", urlPatterns = {"/api/mantenimientos/*"})
public class MantenimientoController extends HttpServlet {
    private final MantenimientoDAO mantenimientoDAO = new MantenimientoDAO();
    private final DispositivoDAO dispositivoDAO = new DispositivoDAO();
    private final NotificacionDAO notificacionDAO = new NotificacionDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
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
                String busqueda = request.getParameter("q");
                String estadoFiltro = request.getParameter("estado");
                int pagina = PaginacionUtil.parsePagina(request.getParameter("pagina"));
                int tamano = PaginacionUtil.parseTamano(request.getParameter("tamano"));

                List<Mantenimiento> mantenimientos = mantenimientoDAO.listar(busqueda, estadoFiltro, pagina, tamano);
                int total = mantenimientoDAO.contarTotal(busqueda, estadoFiltro);

                Map<String, Object> resp = new HashMap<>();
                resp.put("datos", mantenimientos);
                resp.put("total", total);
                resp.put("pagina", pagina);
                resp.put("tamanoPagina", tamano);
                objectMapper.writeValue(response.getWriter(), resp);
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
        if (!Rol.ADMINISTRADOR.name().equals(rol) && !Rol.TECNICO.name().equals(rol)) {
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

            if (idDispositivo == 0 || tipo == null || tipo.isEmpty() || descripcion == null || descripcion.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Datos incompletos"));
                return;
            }

            // Evita registros de mantenimiento duplicados/huérfanos para un dispositivo que ya está
            // en uso o ya tiene otro mantenimiento en curso (sin esto, el conteo de "en mantenimiento"
            // del corte diario y de Dispositivos dejaba de coincidir con la realidad).
            Dispositivo dispositivoActual = dispositivoDAO.buscarPorId(idDispositivo);
            if (dispositivoActual == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Dispositivo no encontrado"));
                return;
            }
            if (!EstadoDispositivo.DISPONIBLE.name().equals(dispositivoActual.getEstado())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "El dispositivo no está disponible para mantenimiento"));
                return;
            }

            String estado = EstadoMantenimiento.EN_PROCESO.name();
            Timestamp fechaInicio = ahoraBogota();
            Timestamp fechaFin = null;

            Mantenimiento mantenimiento = new Mantenimiento();
            mantenimiento.setIdDispositivo(idDispositivo);
            mantenimiento.setIdUsuario(idUsuario);
            mantenimiento.setTipo(tipo);
            mantenimiento.setDescripcion(descripcion);
            mantenimiento.setFechaInicio(fechaInicio);
            mantenimiento.setFechaFin(fechaFin);
            mantenimiento.setEstado(estado);

            boolean resultado = mantenimientoDAO.insertar(mantenimiento);

            if (resultado) {
                if (EstadoMantenimiento.FINALIZADO.name().equals(estado)) {
                    dispositivoDAO.actualizarEstado(idDispositivo, EstadoDispositivo.DISPONIBLE.name());
                } else {
                    dispositivoDAO.actualizarEstado(idDispositivo, EstadoDispositivo.MANTENIMIENTO.name());
                }

                notificarNuevoMantenimiento(idDispositivo);

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

    private Timestamp ahoraBogota() {
        return Timestamp.valueOf(LocalDateTime.now(ZoneId.of("America/Bogota")));
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
        if (!Rol.ADMINISTRADOR.name().equals(rol) && !Rol.TECNICO.name().equals(rol)) {
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

            if (!EstadoMantenimiento.esValido(estado)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Estado de mantenimiento inválido"));
                return;
            }

            Mantenimiento mantenimiento = mantenimientoDAO.buscarPorId(id);
            if (mantenimiento == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Mantenimiento no encontrado"));
                return;
            }

            boolean seFinaliza = EstadoMantenimiento.FINALIZADO.name().equals(estado)
                    && !EstadoMantenimiento.FINALIZADO.name().equals(mantenimiento.getEstado());

            mantenimiento.setEstado(estado);
            if (EstadoMantenimiento.FINALIZADO.name().equals(estado)) {
                mantenimiento.setFechaFin(ahoraBogota());
                // Device returns to available
                dispositivoDAO.actualizarEstado(mantenimiento.getIdDispositivo(), EstadoDispositivo.DISPONIBLE.name());
            }

            boolean resultado = mantenimientoDAO.actualizar(mantenimiento);

            if (resultado) {
                if (seFinaliza) {
                    Usuario actor = (Usuario) session.getAttribute("usuario");
                    notificarMantenimientoFinalizado(mantenimiento, actor.getIdUsuario());
                }
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

    private void notificarNuevoMantenimiento(int idDispositivo) {
        Dispositivo dispositivo = dispositivoDAO.buscarPorId(idDispositivo);
        String nombreDispositivo = dispositivo != null ? dispositivo.getNombre() : ("#" + idDispositivo);
        String mensaje = "El dispositivo \"" + nombreDispositivo + "\" ingresó a mantenimiento.";

        for (int idAdmin : usuarioDAO.listarIdsPorRol(Rol.ADMINISTRADOR.name())) {
            notificacionDAO.crear(idAdmin, NotificacionDAO.TIPO_MANTENIMIENTO_CREADO, mensaje);
        }
    }

    private void notificarMantenimientoFinalizado(Mantenimiento mantenimiento, int idActor) {
        String mensaje = "El mantenimiento del dispositivo \"" + mantenimiento.getNombreDispositivo() + "\" fue finalizado.";

        for (int idAdmin : usuarioDAO.listarIdsPorRol(Rol.ADMINISTRADOR.name())) {
            notificacionDAO.crear(idAdmin, NotificacionDAO.TIPO_MANTENIMIENTO_FINALIZADO, mensaje);
        }

        // Notifica también al técnico que registró el mantenimiento, salvo que sea quien lo está finalizando
        if (mantenimiento.getIdUsuario() != idActor) {
            notificacionDAO.crear(mantenimiento.getIdUsuario(), NotificacionDAO.TIPO_MANTENIMIENTO_FINALIZADO, mensaje);
        }
    }
}
