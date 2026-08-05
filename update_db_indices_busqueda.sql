USE systechpro3;

-- Índices para las columnas de filtro usadas por la búsqueda/paginación server-side
-- de Préstamos, Mantenimiento, Usuarios, Auditoría y Solicitudes Clave (hallazgo BD3
-- de la auditoría). idx_mantenimiento_estado y el filtro por estado de préstamo ya
-- existían desde la Fase 4; aquí solo se agregan los que faltaban.
-- Ejecutar en instalaciones existentes que ya corrieron database.sql antes de este cambio.

CREATE INDEX idx_usuario_rol ON usuario(rol);
CREATE INDEX idx_prestamo_fecha_inicio ON prestamo(fecha_inicio);
CREATE INDEX idx_auditoria_fecha_evento ON auditoria(fecha_evento);
CREATE INDEX idx_solicitud_password_estado ON solicitud_password(estado);
