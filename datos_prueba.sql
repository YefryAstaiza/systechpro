-- ======================================
-- SCRIPT SQL DE DATOS DE PRUEBA PARA SYSTECHPRO
-- ======================================
-- Este script inserta datos de prueba asumiendo que los usuarios ya existen.
-- Incluye 20 registros de préstamos y suficientes datos para probar paginación en todos los roles.

USE systechpro3;

-- Insertar sedes (si no existen)
INSERT IGNORE INTO sede (nombre, codigo) VALUES
('SAN CAMILO', 'SC'),
('SAN JOSE', 'SJ'),
('LOS ROBLES', 'SR'),
('SANTANDER DE QUILICHAO', 'SS');

-- Insertar salones (si no existen)
INSERT IGNORE INTO salon (numero, id_sede) VALUES
-- SAN CAMILO (SC)
(101, 1), (102, 1), (103, 1),
-- SAN JOSE (SJ) - especificados
(105, 2), (106, 2), (107, 2), (108, 2), (109, 2), (116, 2), (208, 2), (209, 2), (210, 2), (212, 2),
-- LOS ROBLES (SR)
(201, 3), (202, 3), (203, 3),
-- SANTANDER DE QUILICHAO (SS)
(301, 4), (302, 4), (303, 4);

-- Insertar dispositivos (si no existen)
INSERT IGNORE INTO dispositivo (nombre, tipo, estado, descripcion) VALUES
('Laptop Dell Inspiron', 'LAPTOP', 'DISPONIBLE', 'Laptop para clases'),
('Proyector Epson', 'PROYECTOR', 'DISPONIBLE', 'Proyector HD'),
('Tablet Samsung', 'TABLET', 'EN_USO', 'Tablet para presentaciones'),
('Computador HP', 'COMPUTADOR', 'DISPONIBLE', 'PC de escritorio'),
('Microfono Sony', 'MICROFONO', 'MANTENIMIENTO', 'Microfono inalámbrico'),
('Cámara Canon', 'CAMARA', 'DISPONIBLE', 'Cámara digital'),
('Impresora HP', 'IMPRESORA', 'DISPONIBLE', 'Impresora láser'),
('Router Cisco', 'ROUTER', 'DISPONIBLE', 'Router de red'),
('Monitor LG', 'MONITOR', 'EN_USO', 'Monitor 24 pulgadas'),
('Teclado Logitech', 'TECLADO', 'DISPONIBLE', 'Teclado mecánico'),
('Mouse Microsoft', 'MOUSE', 'DISPONIBLE', 'Mouse inalámbrico'),
('Altavoz JBL', 'ALTAVOZ', 'DISPONIBLE', 'Altavoz Bluetooth'),
('Switch TP-Link', 'SWITCH', 'MANTENIMIENTO', 'Switch de red'),
('Servidor Dell', 'SERVIDOR', 'DISPONIBLE', 'Servidor de archivos'),
('UPS APC', 'UPS', 'DISPONIBLE', 'Sistema de alimentación ininterrumpida');

-- Insertar 20 préstamos (usando id_usuario existentes 1-4)
INSERT INTO prestamo (id_usuario, id_dispositivo, id_salon, fecha_inicio, fecha_fin, estado) VALUES
(1, 1, 1, '2024-05-01 08:00:00', '2024-05-01 12:00:00', 'APROBADO'),
(2, 2, 4, '2024-05-02 09:00:00', '2024-05-02 13:00:00', 'PENDIENTE'),
(3, 3, 7, '2024-05-03 10:00:00', '2024-05-03 14:00:00', 'APROBADO'),
(4, 4, 10, '2024-05-04 11:00:00', '2024-05-04 15:00:00', 'RECHAZADO'),
(1, 5, 13, '2024-05-05 08:30:00', '2024-05-05 12:30:00', 'APROBADO'),
(2, 6, 16, '2024-05-06 09:30:00', '2024-05-06 13:30:00', 'PENDIENTE'),
(3, 7, 2, '2024-05-07 10:30:00', '2024-05-07 14:30:00', 'APROBADO'),
(4, 8, 5, '2024-05-08 11:30:00', '2024-05-08 15:30:00', 'PENDIENTE'),
(1, 9, 8, '2024-05-09 08:00:00', '2024-05-09 12:00:00', 'APROBADO'),
(2, 10, 11, '2024-05-10 09:00:00', '2024-05-10 13:00:00', 'RECHAZADO'),
(3, 11, 14, '2024-05-11 10:00:00', '2024-05-11 14:00:00', 'APROBADO'),
(4, 12, 17, '2024-05-12 11:00:00', '2024-05-12 15:00:00', 'PENDIENTE'),
(1, 13, 3, '2024-05-13 08:30:00', '2024-05-13 12:30:00', 'APROBADO'),
(2, 14, 6, '2024-05-14 09:30:00', '2024-05-14 13:30:00', 'PENDIENTE'),
(3, 15, 9, '2024-05-15 10:30:00', '2024-05-15 14:30:00', 'APROBADO'),
(4, 1, 12, '2024-05-16 11:30:00', '2024-05-16 15:30:00', 'RECHAZADO'),
(1, 2, 15, '2024-05-17 08:00:00', '2024-05-17 12:00:00', 'APROBADO'),
(2, 3, 18, '2024-05-18 09:00:00', '2024-05-18 13:00:00', 'PENDIENTE'),
(3, 4, 4, '2024-05-19 10:00:00', '2024-05-19 14:00:00', 'APROBADO'),
(4, 5, 7, '2024-05-20 11:00:00', '2024-05-20 15:00:00', 'PENDIENTE');

-- Insertar mantenimientos (usando id_usuario 2 y 3 para técnicos)
INSERT INTO mantenimiento (id_dispositivo, id_usuario, tipo, fecha_inicio, fecha_fin, descripcion, estado) VALUES
(5, 3, 'CORRECTIVO', '2024-04-01 08:00:00', '2024-04-01 12:00:00', 'Reparación de micrófono', 'FINALIZADO'),
(13, 3, 'PREVENTIVO', '2024-04-02 09:00:00', NULL, 'Mantenimiento preventivo del switch', 'EN_PROCESO'),
(3, 3, 'CORRECTIVO', '2024-04-03 10:00:00', '2024-04-03 14:00:00', 'Reparación de tablet', 'FINALIZADO'),
(9, 3, 'PREVENTIVO', '2024-04-04 11:00:00', NULL, 'Limpieza del monitor', 'EN_PROCESO'),
(1, 3, 'CORRECTIVO', '2024-04-05 08:30:00', '2024-04-05 12:30:00', 'Actualización de software en laptop', 'FINALIZADO'),
(7, 3, 'PREVENTIVO', '2024-04-06 09:30:00', NULL, 'Revisión de impresora', 'EN_PROCESO'),
(11, 3, 'CORRECTIVO', '2024-04-07 10:30:00', '2024-04-07 14:30:00', 'Reparación de mouse', 'FINALIZADO'),
(15, 3, 'PREVENTIVO', '2024-04-08 11:30:00', NULL, 'Chequeo de UPS', 'EN_PROCESO'),
(6, 3, 'CORRECTIVO', '2024-04-09 08:00:00', '2024-04-09 12:00:00', 'Calibración de cámara', 'FINALIZADO'),
(12, 3, 'PREVENTIVO', '2024-04-10 09:00:00', NULL, 'Prueba de altavoz', 'EN_PROCESO'),
(2, 3, 'CORRECTIVO', '2024-04-11 10:00:00', '2024-04-11 14:00:00', 'Ajuste de proyector', 'FINALIZADO'),
(8, 3, 'PREVENTIVO', '2024-04-12 11:00:00', NULL, 'Configuración de router', 'EN_PROCESO'),
(4, 3, 'CORRECTIVO', '2024-04-13 08:30:00', '2024-04-13 12:30:00', 'Reparación de PC', 'FINALIZADO'),
(10, 3, 'PREVENTIVO', '2024-04-14 09:30:00', NULL, 'Limpieza de teclado', 'EN_PROCESO'),
(14, 3, 'CORRECTIVO', '2024-04-15 10:30:00', '2024-04-15 14:30:00', 'Actualización de servidor', 'FINALIZADO');

-- Insertar auditoría (usando id_usuario 1-4)
INSERT INTO auditoria (id_usuario, tabla_afectada, accion, id_registro, descripcion, ip) VALUES
(1, 'usuario', 'INSERT', 1, 'Creación de usuario administrador', '192.168.1.100'),
(3, 'dispositivo', 'UPDATE', 1, 'Actualización de estado de laptop', '192.168.1.101'),
(3, 'prestamo', 'INSERT', 1, 'Nuevo préstamo aprobado', '192.168.1.102'),
(3, 'mantenimiento', 'INSERT', 1, 'Inicio de mantenimiento correctivo', '192.168.1.103'),
(1, 'sede', 'INSERT', 1, 'Creación de sede SAN CAMILO', '192.168.1.100'),
(1, 'salon', 'INSERT', 1, 'Creación de salón 101', '192.168.1.101'),
(1, 'dispositivo', 'INSERT', 1, 'Registro de laptop Dell', '192.168.1.102'),
(1, 'prestamo', 'UPDATE', 1, 'Cambio de estado a aprobado', '192.168.1.103'),
(1, 'usuario', 'LOGIN', NULL, 'Inicio de sesión exitoso', '192.168.1.100'),
(3, 'dispositivo', 'DELETE', 5, 'Eliminación de micrófono defectuoso', '192.168.1.101'),
(3, 'mantenimiento', 'UPDATE', 1, 'Finalización de mantenimiento', '192.168.1.102'),
(1, 'prestamo', 'INSERT', 2, 'Nuevo préstamo pendiente', '192.168.1.103'),
(1, 'auditoria', 'INSERT', 1, 'Registro de auditoría', '192.168.1.100'),
(1, 'usuario', 'UPDATE', 2, 'Cambio de contraseña', '192.168.1.101'),
(1, 'dispositivo', 'INSERT', 2, 'Registro de proyector', '192.168.1.102'),
(1, 'salon', 'INSERT', 2, 'Creación de salón 102', '192.168.1.103'),
(1, 'prestamo', 'DELETE', 4, 'Cancelación de préstamo', '192.168.1.100'),
(3, 'mantenimiento', 'INSERT', 2, 'Inicio de mantenimiento preventivo', '192.168.1.101'),
(1, 'usuario', 'LOGIN', NULL, 'Inicio de sesión exitoso', '192.168.1.102'),
(3, 'dispositivo', 'UPDATE', 3, 'Cambio de estado a mantenimiento', '192.168.1.103');

-- Insertar logs de login (usando id_usuario 1-4)
INSERT INTO login_log (id_usuario, ip, estado) VALUES
(1, '192.168.1.100', 'EXITOSO'),
(3, '192.168.1.101', 'EXITOSO'),
(2, '192.168.1.102', 'EXITOSO'),
(4, '192.168.1.103', 'EXITOSO'),
(1, '192.168.1.104', 'EXITOSO'),
(3, '192.168.1.105', 'EXITOSO'),
(2, '192.168.1.106', 'EXITOSO'),
(4, '192.168.1.107', 'EXITOSO'),
(1, '192.168.1.100', 'FALLIDO'),
(3, '192.168.1.101', 'EXITOSO');
