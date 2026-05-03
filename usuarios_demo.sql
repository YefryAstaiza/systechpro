-- Usuarios de prueba para SysTechPro
INSERT INTO usuario (nombre, correo, contrasena, rol) VALUES
('Docente Demo', 'docente@test.com', '$2a$12$Y/naKKSz8QdyPpbW1q02K.SDhnTvR1uXlFIsA53ePZJ7e0oqLOugi', 'DOCENTE'),
('Administrativo Demo', 'administra@test.com', '$2a$12$T2IBBtlMznQyphwWO.32bOfw0i9mwvdnLpPEX6fhdpETo.1hKvnlC', 'ADMINISTRATIVO'),
('Tecnico Demo', 'tecnico@test.com', '$2a$12$lpLEQFF4WUBveCb9Khfhmu.7KKclmA5wM.uMNIrBljAcvt/Tn/1KG', 'TECNICO'),
('Admin Demo', 'admin@systechpro.com', '$2a$12$Vl8fNiNulXo3d/2GrCNluOESniksteHN/ejSDT0ig2q7FoiABtHpG', 'ADMINISTRADOR')
ON DUPLICATE KEY UPDATE 
contrasena = VALUES(contrasena),
rol = VALUES(rol),
nombre = VALUES(nombre);

