-- =============================================
-- Base de datos: systechpro3
-- =============================================

CREATE DATABASE IF NOT EXISTS systechpro3;
USE systechpro3;

-- =============================================
-- Tabla: usuario
-- =============================================
CREATE TABLE IF NOT EXISTS usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    rol ENUM('ADMIN', 'TECNICO', 'DOCENTE', 'ADMINISTRATIVO') NOT NULL DEFAULT 'DOCENTE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- Tabla: dispositivo
-- =============================================
CREATE TABLE IF NOT EXISTS dispositivo (
    id_dispositivo INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo ENUM('COMPUTADOR', 'PROYECTOR', 'IMPRESORA', 'TABLET', 'OTRO') NOT NULL,
    estado ENUM('DISPONIBLE', 'EN_USO', 'MANTENIMIENTO', 'FUERA_SERVICIO') NOT NULL DEFAULT 'DISPONIBLE',
    ubicacion VARCHAR(100),
    descripcion TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- Tabla: prestamo
-- =============================================
CREATE TABLE IF NOT EXISTS prestamo (
    id_prestamo INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_dispositivo INT NOT NULL,
    fecha_prestamo DATETIME NOT NULL,
    fecha_devolucion DATETIME,
    estado ENUM('PENDIENTE', 'APROBADO', 'RECHAZADO', 'DEVUELTO') NOT NULL DEFAULT 'PENDIENTE',
    observacion TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_dispositivo) REFERENCES dispositivo(id_dispositivo) ON DELETE CASCADE
);

-- =============================================
-- Tabla: mantenimiento
-- =============================================
CREATE TABLE IF NOT EXISTS mantenimiento (
    id_mantenimiento INT AUTO_INCREMENT PRIMARY KEY,
    id_dispositivo INT NOT NULL,
    descripcion TEXT NOT NULL,
    fecha_inicio DATETIME NOT NULL,
    fecha_fin DATETIME,
    estado ENUM('EN_PROCESO', 'COMPLETADO') NOT NULL DEFAULT 'EN_PROCESO',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_dispositivo) REFERENCES dispositivo(id_dispositivo) ON DELETE CASCADE
);

-- =============================================
-- Usuario administrador inicial
-- Correo: admin@systechpro.com
-- Contraseña: admin123 (encriptación MD5 + Base64)
-- =============================================
INSERT INTO usuario (nombre, correo, contrasena, rol) 
VALUES ('Administrador', 'admin@systechpro.com', 'AZICOnu9cyUFFvBp3xi1AA==', 'ADMIN');

-- =============================================
-- Dispositivos de ejemplo
-- =============================================
INSERT INTO dispositivo (nombre, tipo, estado, ubicacion, descripcion) VALUES
('Dell OptiPlex 7090', 'COMPUTADOR', 'DISPONIBLE', 'Laboratorio 1', 'Computador de escritorio'),
('HP ProBook 450', 'COMPUTADOR', 'DISPONIBLE', 'Oficina Administrativa', 'Laptop'),
('Epson PowerLite S41', 'PROYECTOR', 'DISPONIBLE', 'Aula 101', 'Proyector HD'),
('Canon imageRUNNER', 'IMPRESORA', 'DISPONIBLE', 'Secretaría', 'Impresora láser color'),
('iPad Air 4', 'TABLET', 'DISPONIBLE', 'Biblioteca', 'Tableta para préstamos');