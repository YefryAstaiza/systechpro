-- ======================================
-- BASE DE DATOS
-- ======================================
CREATE DATABASE IF NOT EXISTS systechpro3;
USE systechpro3;

-- ======================================
-- TABLA: USUARIO
-- ======================================
CREATE TABLE IF NOT EXISTS usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(100) UNIQUE NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    rol ENUM('ADMINISTRADOR', 'DOCENTE', 'TECNICO', 'ADMINISTRATIVO') NOT NULL,

    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ======================================
-- TABLA: SEDE
-- ======================================
CREATE TABLE IF NOT EXISTS sede (
    id_sede INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    codigo VARCHAR(5) NOT NULL UNIQUE,

    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ======================================
-- TABLA: SALON (RELACIONADO A SEDE ✔)
-- ======================================
CREATE TABLE IF NOT EXISTS salon (
    id_salon INT AUTO_INCREMENT PRIMARY KEY,
    numero INT NOT NULL,
    id_sede INT NOT NULL,

    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (id_sede) REFERENCES sede(id_sede)
);

-- ======================================
-- TABLA: DISPOSITIVO (SIN UBICACIÓN ✔)
-- ======================================
CREATE TABLE IF NOT EXISTS dispositivo (
    id_dispositivo INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    estado ENUM('DISPONIBLE', 'EN_USO', 'MANTENIMIENTO') DEFAULT 'DISPONIBLE',
    descripcion TEXT,

    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ======================================
-- TABLA: PRESTAMO (UBICACIÓN = SALON ✔)
-- ======================================
CREATE TABLE IF NOT EXISTS prestamo (
    id_prestamo INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_dispositivo INT NOT NULL,
    id_salon INT NOT NULL,

    fecha_inicio DATETIME NOT NULL,
    fecha_fin DATETIME NOT NULL,
    estado ENUM('PENDIENTE', 'APROBADO', 'RECHAZADO') DEFAULT 'PENDIENTE',

    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_dispositivo) REFERENCES dispositivo(id_dispositivo),
    FOREIGN KEY (id_salon) REFERENCES salon(id_salon)
);

-- ======================================
-- TABLA: MANTENIMIENTO
-- ======================================
CREATE TABLE IF NOT EXISTS mantenimiento (
    id_mantenimiento INT AUTO_INCREMENT PRIMARY KEY,
    id_dispositivo INT NOT NULL,
    id_usuario INT NOT NULL, -- técnico
    tipo ENUM('PREVENTIVO', 'CORRECTIVO') NOT NULL,
    fecha_inicio DATETIME NOT NULL,
    fecha_fin DATETIME,
    descripcion TEXT,
    estado ENUM('EN_PROCESO', 'FINALIZADO') DEFAULT 'EN_PROCESO',

    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (id_dispositivo) REFERENCES dispositivo(id_dispositivo),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

-- ======================================
-- TABLA: AUDITORÍA (CON IP ✔)
-- ======================================
CREATE TABLE IF NOT EXISTS auditoria (
    id_auditoria INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    tabla_afectada VARCHAR(50) NOT NULL,
    accion ENUM('INSERT', 'UPDATE', 'DELETE', 'LOGIN') NOT NULL,
    id_registro INT,
    descripcion TEXT,
    ip VARCHAR(45), -- NUEVO CAMPO
    fecha_evento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

-- ======================================
-- TABLA: LOGIN
-- ======================================
CREATE TABLE IF NOT EXISTS login_log (
    id_login INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT,
    fecha_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip VARCHAR(45),
    estado ENUM('EXITOSO', 'FALLIDO'),

    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);