USE systechpro3;

-- Índices en columnas de filtro frecuente que no quedan cubiertas automáticamente
-- por las claves foráneas (Fase 4 de la auditoría técnica - Optimización SQL).
-- Ejecutar en instalaciones existentes que ya corrieron database.sql antes de este cambio.

CREATE INDEX idx_dispositivo_estado ON dispositivo(estado);
CREATE INDEX idx_dispositivo_tipo ON dispositivo(tipo);
CREATE INDEX idx_prestamo_estado_dispositivo ON prestamo(estado, id_dispositivo);
CREATE INDEX idx_mantenimiento_estado ON mantenimiento(estado);
