package com.systechpro.controllers;

import com.systechpro.dao.PrestamoDAO;
import com.systechpro.dao.DispositivoDAO;
import com.systechpro.dao.NotificacionDAO;
import com.systechpro.dao.UsuarioDAO;
import com.systechpro.models.Prestamo;
import com.systechpro.models.Dispositivo;
import com.systechpro.models.EstadoDispositivo;
import com.systechpro.models.EstadoPrestamo;
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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "PrestamoController", urlPatterns = {"/api/prestamos/*"})
public class PrestamoController extends HttpServlet {
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final DispositivoDAO dispositivoDAO = new DispositivoDAO();
    private final NotificacionDAO notificacionDAO = new NotificacionDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private boolean tienePermisoAdmin(String rol) {
        return Rol.ADMINISTRADOR.name().equals(rol) || Rol.TECNICO.name().equals(rol);
    }

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
                String busqueda = request.getParameter("q");
                String estadoFiltro = request.getParameter("estado");
                Timestamp fechaDesde = PaginacionUtil.parseFechaInicioDia(request.getParameter("fechaDesde"));
                Timestamp fechaHasta = PaginacionUtil.parseFechaFinDia(request.getParameter("fechaHasta"));
                int pagina = PaginacionUtil.parsePagina(request.getParameter("pagina"));
                int tamano = PaginacionUtil.parseTamano(request.getParameter("tamano"));

                // Docente/Administrativo solo ven sus propios préstamos - se aplica como un filtro
                // más dentro de la misma consulta paginada, no como una ruta de código aparte.
                Integer idUsuarioFiltro = tienePermisoAdmin(rol)
                        ? null
                        : ((Usuario) session.getAttribute("usuario")).getIdUsuario();

                List<Prestamo> prestamos = prestamoDAO.listar(busqueda, estadoFiltro, idUsuarioFiltro, fechaDesde, fechaHasta, pagina, tamano);
                int total = prestamoDAO.contarTotal(busqueda, estadoFiltro, idUsuarioFiltro, fechaDesde, fechaHasta);

                Map<String, Object> resp = new HashMap<>();
                resp.put("datos", prestamos);
                resp.put("total", total);
                resp.put("pagina", pagina);
                resp.put("tamanoPagina", tamano);
                objectMapper.writeValue(response.getWriter(), resp);
            } else if (pathInfo.equals("/pendientes") && tienePermisoAdmin(rol)) {
                List<Prestamo> prestamos = prestamoDAO.listarPorEstado(EstadoPrestamo.PENDIENTE.name());
                objectMapper.writeValue(response.getWriter(), prestamos);
            } else {
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
        
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "No autorizado"));
            return;
        }

        try {
            Map<String, Object> datos = objectMapper.readValue(request.getInputStream(), Map.class);
            
            if (!datos.containsKey("idDispositivo") || !datos.containsKey("idSalon") || 
                !datos.containsKey("fechaInicio") || !datos.containsKey("fechaFin")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Faltan datos requeridos"));
                return;
            }

            int idDispositivo = Integer.parseInt(datos.get("idDispositivo").toString());
            int idSalon = Integer.parseInt(datos.get("idSalon").toString());
            com.systechpro.models.Usuario solicitante = (com.systechpro.models.Usuario) session.getAttribute("usuario");
            int idUsuario = solicitante.getIdUsuario();
            
            String fechaInicioStr = (String) datos.get("fechaInicio");
            String fechaFinStr = (String) datos.get("fechaFin");

            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime inicio = LocalDateTime.parse(fechaInicioStr, formatter);
            LocalDateTime fin = LocalDateTime.parse(fechaFinStr, formatter);

            if (fin.isBefore(inicio) || fin.isEqual(inicio)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "La fecha de fin debe ser mayor a la fecha de inicio"));
                return;
            }

            Dispositivo dispositivo = dispositivoDAO.buscarPorId(idDispositivo);
            if (dispositivo == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Dispositivo no encontrado"));
                return;
            }

            Timestamp inicioTs = Timestamp.valueOf(inicio);
            Timestamp finTs = Timestamp.valueOf(fin);

            // No se valida dispositivo.estado aquí: que esté EN_USO ahora mismo no importa si la
            // reserva es para más adelante. Lo que sí importa es que el horario pedido no choque
            // con otro préstamo YA APROBADO del mismo dispositivo.
            if (prestamoDAO.existeSolapamiento(idDispositivo, inicioTs, finTs, null)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "El dispositivo ya está reservado en ese horario"));
                return;
            }

            // Crear préstamo
            Prestamo prestamo = new Prestamo();
            prestamo.setIdUsuario(idUsuario);
            prestamo.setIdDispositivo(idDispositivo);
            prestamo.setIdSalon(idSalon);
            prestamo.setFechaInicio(inicioTs);
            prestamo.setFechaFin(finTs);
            prestamo.setEstado(EstadoPrestamo.PENDIENTE.name());

            boolean resultado = prestamoDAO.insertar(prestamo);

            if (resultado) {
                String mensaje = solicitante.getNombre() + " solicitó el préstamo de \"" + dispositivo.getNombre() + "\".";
                for (int idAdmin : usuarioDAO.listarIdsPorRol(Rol.ADMINISTRADOR.name())) {
                    notificacionDAO.crear(idAdmin, NotificacionDAO.TIPO_PRESTAMO_SOLICITADO, mensaje);
                }
                response.setStatus(HttpServletResponse.SC_CREATED);
                objectMapper.writeValue(response.getWriter(), Map.of("success", true, "mensaje", "Solicitud de préstamo enviada con éxito"));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Error al registrar la solicitud"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor: " + e.getMessage()));
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
        com.systechpro.models.Usuario usuarioSesion = (com.systechpro.models.Usuario) session.getAttribute("usuario");

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

            if (nuevoEstado == null || (!nuevoEstado.equals(EstadoPrestamo.APROBADO.name()) && !nuevoEstado.equals(EstadoPrestamo.RECHAZADO.name()) && !nuevoEstado.equals(EstadoPrestamo.DEVUELTO.name()))) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Estado inválido"));
                return;
            }

            // Verificar préstamo actual
            Prestamo prestamoActual = prestamoDAO.buscarPorId(id);
            if (prestamoActual == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Préstamo no encontrado"));
                return;
            }

            // Admin/Técnico pueden aprobar, rechazar o marcar como devuelto cualquier préstamo.
            // El propio solicitante (Docente/Administrativo) solo puede devolver SU PROPIO préstamo
            // ya aprobado - es la única acción de autoservicio, sin pasar por un admin.
            boolean esAutoDevolucion = EstadoPrestamo.DEVUELTO.name().equals(nuevoEstado)
                    && prestamoActual.getIdUsuario() == usuarioSesion.getIdUsuario();
            if (!tienePermisoAdmin(rol) && !esAutoDevolucion) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "No tienes permiso para realizar esta acción"));
                return;
            }

            // NOTA: la comparación con EstadoDispositivo.EN_USO es preexistente; prestamo.estado nunca toma
            // ese valor (solo dispositivo.estado lo hace), por lo que esta rama del OR nunca se cumple.
            // Se preserva el comportamiento original tal cual; revisar si la intención era otra condición.
            if (!EstadoPrestamo.PENDIENTE.name().equals(prestamoActual.getEstado()) && !EstadoDispositivo.EN_USO.name().equals(prestamoActual.getEstado()) && !nuevoEstado.equals(EstadoPrestamo.DEVUELTO.name())) {
                 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                 objectMapper.writeValue(response.getWriter(), Map.of("error", "No se puede cambiar el estado de este préstamo"));
                 return;
            }

            // Otra reserva pudo aprobarse para el mismo horario entre que se creó esta solicitud y
            // que se aprueba. Se revalida el solapamiento (no el estado actual del dispositivo: que
            // esté EN_USO ahora mismo no importa si esta reserva es para más adelante).
            if (EstadoPrestamo.APROBADO.name().equals(nuevoEstado)) {
                boolean choca = prestamoDAO.existeSolapamiento(
                        prestamoActual.getIdDispositivo(), prestamoActual.getFechaInicio(),
                        prestamoActual.getFechaFin(), prestamoActual.getIdPrestamo());
                if (choca) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    objectMapper.writeValue(response.getWriter(), Map.of("error", "El dispositivo ya está reservado en ese horario"));
                    return;
                }
            }

            boolean resultado = EstadoPrestamo.DEVUELTO.name().equals(nuevoEstado)
                    ? prestamoDAO.marcarDevuelto(id)
                    : prestamoDAO.actualizarEstado(id, nuevoEstado);

            if (resultado) {
                // Recalcula el estado real del dispositivo en vez de fijarlo a ciegas: una reserva
                // futura recién aprobada no debe bloquear el dispositivo hoy, y rechazar/devolver un
                // préstamo no debe marcar "Disponible" si otra reserva de ese dispositivo sigue activa.
                dispositivoDAO.sincronizarEstadoDispositivo(prestamoActual.getIdDispositivo());

                notificarCambioEstado(prestamoActual, nuevoEstado);

                objectMapper.writeValue(response.getWriter(), Map.of("success", true, "mensaje", "Estado del préstamo actualizado"));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Error al actualizar estado"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor"));
        }
    }

    private void notificarCambioEstado(Prestamo prestamo, String nuevoEstado) {
        String tipo;
        String mensaje;

        if (EstadoPrestamo.APROBADO.name().equals(nuevoEstado)) {
            tipo = NotificacionDAO.TIPO_PRESTAMO_APROBADO;
            mensaje = "Tu solicitud de préstamo del dispositivo \"" + prestamo.getNombreDispositivo() + "\" fue aprobada.";
        } else if (EstadoPrestamo.RECHAZADO.name().equals(nuevoEstado)) {
            tipo = NotificacionDAO.TIPO_PRESTAMO_RECHAZADO;
            mensaje = "Tu solicitud de préstamo del dispositivo \"" + prestamo.getNombreDispositivo() + "\" fue rechazada.";
        } else if (EstadoPrestamo.DEVUELTO.name().equals(nuevoEstado)) {
            tipo = NotificacionDAO.TIPO_PRESTAMO_DEVUELTO;
            mensaje = "Se registró la devolución del dispositivo \"" + prestamo.getNombreDispositivo() + "\".";
        } else {
            return;
        }

        notificacionDAO.crear(prestamo.getIdUsuario(), tipo, mensaje);
    }
}
