package com.systechpro.dao;

import com.systechpro.utils.GestorJDBC;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReporteDAO {
    private static final Logger LOGGER = Logger.getLogger(ReporteDAO.class.getName());

    private static final String[] DIAS_SEMANA = {
        "Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"
    };

    public Map<String, Integer> obtenerEstadisticasDispositivos() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT estado, COUNT(*) as total FROM dispositivo GROUP BY estado";

        try (Connection conn = GestorJDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                stats.put(rs.getString("estado"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en obtenerEstadisticasDispositivos", e);
        }
        return stats;
    }

    public Map<String, Integer> obtenerEstadisticasMantenimientos() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT tipo, COUNT(*) as total FROM mantenimiento GROUP BY tipo";

        try (Connection conn = GestorJDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                stats.put(rs.getString("tipo"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en obtenerEstadisticasMantenimientos", e);
        }
        return stats;
    }

    /** Top de dispositivos por cantidad de préstamos reales (aprobados o ya devueltos). */
    public List<Map<String, Object>> obtenerDispositivosMasPrestados(int limite) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT d.nombre AS nombre_dispositivo, COUNT(*) AS cantidad " +
                     "FROM prestamo p JOIN dispositivo d ON p.id_dispositivo = d.id_dispositivo " +
                     "WHERE p.estado IN ('APROBADO', 'DEVUELTO') " +
                     "GROUP BY p.id_dispositivo, d.nombre " +
                     "ORDER BY cantidad DESC LIMIT ?";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limite);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("nombreDispositivo", rs.getString("nombre_dispositivo"));
                fila.put("cantidad", rs.getInt("cantidad"));
                lista.add(fila);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en obtenerDispositivosMasPrestados", e);
        }
        return lista;
    }

    /** Promedio real de horas entre que se aprueba un préstamo y se devuelve. Null si aún no hay ninguno devuelto. */
    public Double obtenerPromedioHorasPrestamo() {
        String sql = "SELECT AVG(TIMESTAMPDIFF(MINUTE, fecha_inicio, fecha_devolucion)) AS promedio_minutos " +
                     "FROM prestamo WHERE estado = 'DEVUELTO' AND fecha_devolucion IS NOT NULL";
        try (Connection conn = GestorJDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                double minutos = rs.getDouble("promedio_minutos");
                return rs.wasNull() ? null : minutos / 60.0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en obtenerPromedioHorasPrestamo", e);
        }
        return null;
    }

    /** Mantenimientos que llevan EN_PROCESO más de diasUmbral días sin finalizar. */
    public List<Map<String, Object>> obtenerAlertasMantenimiento(int diasUmbral) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT d.nombre AS nombre_dispositivo, m.fecha_inicio, " +
                     "TIMESTAMPDIFF(DAY, m.fecha_inicio, NOW()) AS dias " +
                     "FROM mantenimiento m JOIN dispositivo d ON m.id_dispositivo = d.id_dispositivo " +
                     "WHERE m.estado = 'EN_PROCESO' AND TIMESTAMPDIFF(DAY, m.fecha_inicio, NOW()) >= ? " +
                     "ORDER BY dias DESC";
        try (Connection conn = GestorJDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, diasUmbral);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("nombreDispositivo", rs.getString("nombre_dispositivo"));
                fila.put("dias", rs.getInt("dias"));
                lista.add(fila);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en obtenerAlertasMantenimiento", e);
        }
        return lista;
    }

    /** Cantidad de préstamos iniciados por día de la semana (histórico completo, no solo días recientes). */
    public Map<String, Integer> obtenerUsoPorDiaSemana() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        for (String dia : DIAS_SEMANA) {
            stats.put(dia, 0);
        }
        String sql = "SELECT DAYOFWEEK(fecha_inicio) AS dia_semana, COUNT(*) AS cantidad " +
                     "FROM prestamo WHERE estado IN ('APROBADO', 'DEVUELTO') " +
                     "GROUP BY DAYOFWEEK(fecha_inicio)";
        try (Connection conn = GestorJDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int diaSemana = rs.getInt("dia_semana"); // 1=Domingo ... 7=Sábado
                stats.put(DIAS_SEMANA[diaSemana - 1], rs.getInt("cantidad"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error en obtenerUsoPorDiaSemana", e);
        }
        return stats;
    }
}
