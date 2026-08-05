USE systechpro3;

-- Agrega el conteo de "en mantenimiento" al corte diario, que faltaba.
-- Ejecutar en instalaciones existentes que ya corrieron database.sql antes de este cambio.

ALTER TABLE corte_diario ADD COLUMN en_mantenimiento INT NOT NULL DEFAULT 0 AFTER en_prestamo;
