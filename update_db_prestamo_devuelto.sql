USE systechpro3;

-- El código (PrestamoController, DispositivoDAO) ya acepta y usa el estado DEVUELTO
-- para préstamos, pero el ENUM original de la tabla prestamo nunca lo incluyó.
-- Ejecutar este script en instalaciones existentes para evitar errores de truncamiento
-- de datos al intentar guardar un préstamo con estado DEVUELTO.
ALTER TABLE prestamo MODIFY estado ENUM('PENDIENTE', 'APROBADO', 'RECHAZADO', 'DEVUELTO') DEFAULT 'PENDIENTE';
