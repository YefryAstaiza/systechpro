USE systechpro3;

-- Registra cuando un prestamo realmente se devuelve (antes solo existia fecha_fin,
-- que es la fecha PLANEADA al crear la solicitud, no la devolucion real).
-- Ejecutar en instalaciones existentes que ya corrieron database.sql antes de este cambio.

ALTER TABLE prestamo ADD COLUMN fecha_devolucion TIMESTAMP NULL AFTER fecha_fin;
