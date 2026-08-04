package com.systechpro.controllers;

import com.systechpro.dao.PrestamoDAO;
import com.systechpro.dao.DispositivoDAO;
import com.systechpro.models.Prestamo;
import com.systechpro.models.Dispositivo;
import com.systechpro.models.EstadoDispositivo;
import com.systechpro.models.EstadoPrestamo;
import com.systechpro.models.Rol;
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
import java.util.List;
import java.util.Map;

@WebServlet(name = "PrestamoController", urlPatterns = {"/api/prestamos/*"})
public class PrestamoController extends HttpServlet {
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final DispositivoDAO dispositivoDAO = new DispositivoDAO();
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
                List<Prestamo> prestamos;
                if (tienePermisoAdmin(rol)) {
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
            int idUsuario = ((com.systechpro.models.Usuario) session.getAttribute("usuario")).getIdUsuario();
            
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

            // Verificar que el dispositivo esté disponible
            Dispositivo dispositivo = dispositivoDAO.buscarPorId(idDispositivo);
            if (dispositivo == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Dispositivo no encontrado"));
                return;
            }
            
            if (!EstadoDispositivo.DISPONIBLE.name().equals(dispositivo.getEstado())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "El dispositivo no está disponible"));
                return;
            }

            // Crear préstamo
            Prestamo prestamo = new Prestamo();
            prestamo.setIdUsuario(idUsuario);
            prestamo.setIdDispositivo(idDispositivo);
            prestamo.setIdSalon(idSalon);
            prestamo.setFechaInicio(Timestamp.valueOf(inicio));
            prestamo.setFechaFin(Timestamp.valueOf(fin));
            prestamo.setEstado(EstadoPrestamo.PENDIENTE.name());

            boolean resultado = prestamoDAO.insertar(prestamo);

            if (resultado) {
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
        if (!tienePermisoAdmin(rol)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Solo los administradores o técnicos pueden aprobar/rechazar solicitudes"));
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

            // NOTA: la comparación con EstadoDispositivo.EN_USO es preexistente; prestamo.estado nunca toma
            // ese valor (solo dispositivo.estado lo hace), por lo que esta rama del OR nunca se cumple.
            // Se preserva el comportamiento original tal cual; revisar si la intención era otra condición.
            if (!EstadoPrestamo.PENDIENTE.name().equals(prestamoActual.getEstado()) && !EstadoDispositivo.EN_USO.name().equals(prestamoActual.getEstado()) && !nuevoEstado.equals(EstadoPrestamo.DEVUELTO.name())) {
                 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                 objectMapper.writeValue(response.getWriter(), Map.of("error", "No se puede cambiar el estado de este préstamo"));
                 return;
            }

            boolean resultado = prestamoDAO.actualizarEstado(id, nuevoEstado);

            if (resultado) {
                if (EstadoPrestamo.APROBADO.name().equals(nuevoEstado)) {
                    dispositivoDAO.actualizarEstado(prestamoActual.getIdDispositivo(), EstadoDispositivo.EN_USO.name());
                } else if (EstadoPrestamo.RECHAZADO.name().equals(nuevoEstado) || EstadoPrestamo.DEVUELTO.name().equals(nuevoEstado)) {
                    dispositivoDAO.actualizarEstado(prestamoActual.getIdDispositivo(), EstadoDispositivo.DISPONIBLE.name());
                }
                
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
}
