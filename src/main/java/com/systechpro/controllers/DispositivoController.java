package com.systechpro.controllers;

import com.systechpro.dao.DispositivoDAO;
import com.systechpro.dao.AuditoriaDAO;
import com.systechpro.models.Auditoria;
import com.systechpro.models.Dispositivo;
import com.systechpro.models.EstadoDispositivo;
import com.systechpro.models.Rol;
import com.systechpro.utils.ValidadorTexto;
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
import java.util.Arrays;

@WebServlet(name = "DispositivoController", urlPatterns = {"/api/dispositivos/*"})
public class DispositivoController extends HttpServlet {
    private final DispositivoDAO dispositivoDAO = new DispositivoDAO();
    private final AuditoriaDAO auditoriaDAO = new AuditoriaDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<String> TIPOS_VALIDOS = Arrays.asList("COMPUTADOR", "PROYECTOR", "IMPRESORA", "TABLET", "OTRO");

    private boolean validarRol(String rol) {
        return Rol.ADMINISTRADOR.name().equals(rol);
    }

    private boolean validarNombreDispositivo(String nombre) {
        return ValidadorTexto.esTextoLibreValido(nombre);
    }

    private boolean validarDescripcionDispositivo(String descripcion) {
        return ValidadorTexto.esDescripcionValida(descripcion);
    }

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
                String tipo = request.getParameter("tipo");
                String estado = request.getParameter("estado");

                List<Dispositivo> dispositivos;
                if (tipo != null || estado != null) {
                    dispositivos = dispositivoDAO.listarPorFiltro(tipo, estado);
                } else {
                    dispositivos = dispositivoDAO.listar();
                }
                objectMapper.writeValue(response.getWriter(), dispositivos);
            } else {
                String idStr = pathInfo.substring(1);
                int id = Integer.parseInt(idStr);
                Dispositivo dispositivo = dispositivoDAO.buscarPorId(id);

                if (dispositivo != null) {
                    objectMapper.writeValue(response.getWriter(), dispositivo);
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    objectMapper.writeValue(response.getWriter(), Map.of("error", "Dispositivo no encontrado"));
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
        if (!validarRol(rol)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Solo administradores pueden crear dispositivos"));
            return;
        }

        try {
            Dispositivo dispositivo = objectMapper.readValue(request.getInputStream(), Dispositivo.class);

            if (dispositivo.getNombre() == null || dispositivo.getNombre().trim().isEmpty() ||
                dispositivo.getTipo() == null || dispositivo.getEstado() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Nombre, tipo y estado son requeridos"));
                return;
            }

            if (!validarNombreDispositivo(dispositivo.getNombre())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Nombre de dispositivo inválido. Usa un nombre descriptivo y legible."));
                return;
            }

            if (!validarDescripcionDispositivo(dispositivo.getDescripcion())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Descripción inválida. Máximo 250 caracteres y sin caracteres extraños."));
                return;
            }

            if (!TIPOS_VALIDOS.contains(dispositivo.getTipo())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Tipo de dispositivo inválido"));
                return;
            }

            if (!EstadoDispositivo.esValido(dispositivo.getEstado())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Estado de dispositivo inválido"));
                return;
            }

            boolean resultado = dispositivoDAO.insertar(dispositivo);

            if (resultado) {
                // Find ID of newly inserted by name (simplification)
                // Actually Dispositivo doesn't have a unique name constraint maybe, but we can log id as 0 or so
                int idUsuarioSesion = ((com.systechpro.models.Usuario) session.getAttribute("usuario")).getIdUsuario();
                auditoriaDAO.insertar(new Auditoria(idUsuarioSesion, "dispositivo", "INSERT", 0, "Dispositivo creado: " + dispositivo.getNombre(), request.getRemoteAddr()));
                
                response.setStatus(HttpServletResponse.SC_CREATED);
                objectMapper.writeValue(response.getWriter(), Map.of("success", true, "mensaje", "Dispositivo creado correctamente"));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Error al guardar dispositivo"));
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
        if (!validarRol(rol)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Solo administradores pueden modificar dispositivos"));
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

            Dispositivo dispositivo = objectMapper.readValue(request.getInputStream(), Dispositivo.class);
            dispositivo.setIdDispositivo(id);

            if (dispositivo.getNombre() == null || dispositivo.getNombre().trim().isEmpty() ||
                dispositivo.getTipo() == null || dispositivo.getEstado() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Nombre, tipo y estado son requeridos"));
                return;
            }

            if (!validarNombreDispositivo(dispositivo.getNombre())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Nombre de dispositivo inválido. Usa un nombre descriptivo y legible."));
                return;
            }

            if (!validarDescripcionDispositivo(dispositivo.getDescripcion())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Descripción inválida. Máximo 250 caracteres y sin caracteres extraños."));
                return;
            }

            if (!TIPOS_VALIDOS.contains(dispositivo.getTipo())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Tipo de dispositivo inválido"));
                return;
            }

            if (!EstadoDispositivo.esValido(dispositivo.getEstado())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Estado de dispositivo inválido"));
                return;
            }

            boolean resultado = dispositivoDAO.actualizar(dispositivo);

            if (resultado) {
                int idUsuarioSesion = ((com.systechpro.models.Usuario) session.getAttribute("usuario")).getIdUsuario();
                auditoriaDAO.insertar(new Auditoria(idUsuarioSesion, "dispositivo", "UPDATE", id, "Dispositivo editado: " + dispositivo.getNombre(), request.getRemoteAddr()));

                objectMapper.writeValue(response.getWriter(), Map.of("success", true, "mensaje", "Dispositivo actualizado correctamente"));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Dispositivo no encontrado"));
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
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
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
        if (!validarRol(rol)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Solo administradores pueden eliminar dispositivos"));
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
            
            // Validar que no esté en uso
            Dispositivo dispositivoExistente = dispositivoDAO.buscarPorId(id);
            if (dispositivoExistente == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Dispositivo no encontrado"));
                return;
            }
            
            if (EstadoDispositivo.EN_USO.name().equals(dispositivoExistente.getEstado())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "No se puede eliminar un dispositivo que está EN_USO"));
                return;
            }

            boolean resultado = dispositivoDAO.eliminar(id);

            if (resultado) {
                int idUsuarioSesion = ((com.systechpro.models.Usuario) session.getAttribute("usuario")).getIdUsuario();
                auditoriaDAO.insertar(new Auditoria(idUsuarioSesion, "dispositivo", "DELETE", id, "Dispositivo eliminado ID: " + id, request.getRemoteAddr()));

                objectMapper.writeValue(response.getWriter(), Map.of("success", true, "mensaje", "Dispositivo eliminado correctamente"));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                objectMapper.writeValue(response.getWriter(), Map.of("error", "Error al eliminar dispositivo"));
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "ID inválido"));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of("error", "Error en el servidor"));
        }
    }
}
