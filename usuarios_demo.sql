-- Usuarios de prueba para SysTechPro
INSERT INTO usuario (nombre, correo, contrasena, rol) VALUES
('Carlos Tulande', 'carlos.tulande@gti.fup.edu.co', SHA2('123456', 256), 'ADMINISTRADOR'),
('Juan Perez', 'juan.perez@docente.fup.edu.co', SHA2('123456', 256), 'DOCENTE'),
('Pedro Gomez', 'pedro.gomez@tecnico.fup.edu.co', SHA2('123456', 256), 'TECNICO'),
('Ana Martinez', 'ana.martinez@administrativo.fup.edu.co', SHA2('123456', 256), 'ADMINISTRATIVO');
