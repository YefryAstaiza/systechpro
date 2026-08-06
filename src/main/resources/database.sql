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
    rol ENUM('ADMINISTRADOR', 'DOCENTE', 'TECNICO', 'ADMINISTRATIVO', 'MONITOR') NOT NULL,
    cambio_obligatorio BOOLEAN DEFAULT FALSE,

    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índice para el filtro por rol (búsqueda/paginación de Usuarios)
CREATE INDEX idx_usuario_rol ON usuario(rol);

-- ======================================
-- TABLA: SOLICITUD_PASSWORD
-- ======================================
CREATE TABLE IF NOT EXISTS solicitud_password (
    id_solicitud INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    fecha_solicitud TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado ENUM('PENDIENTE', 'APROBADA', 'RECHAZADA') DEFAULT 'PENDIENTE',
    fecha_resolucion TIMESTAMP NULL,
    id_resolutor INT NULL,
    password_temporal VARCHAR(255) NULL,

    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_resolutor) REFERENCES usuario(id_usuario)
);

-- Índice para el filtro por estado en la búsqueda/paginación de Solicitudes Clave
CREATE INDEX idx_solicitud_password_estado ON solicitud_password(estado);

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

-- Índices para los filtros de DispositivoDAO.listarPorFiltro (tipo, estado)
CREATE INDEX idx_dispositivo_estado ON dispositivo(estado);
CREATE INDEX idx_dispositivo_tipo ON dispositivo(tipo);

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
    fecha_devolucion TIMESTAMP NULL,
    estado ENUM('PENDIENTE', 'APROBADO', 'RECHAZADO', 'DEVUELTO') DEFAULT 'PENDIENTE',

    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_aprobador INT NULL,

    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_dispositivo) REFERENCES dispositivo(id_dispositivo),
    FOREIGN KEY (id_salon) REFERENCES salon(id_salon),
    FOREIGN KEY (id_aprobador) REFERENCES usuario(id_usuario)
);

-- Índice compuesto para PrestamoDAO.listarPorEstado (WHERE estado = ?) y para la
-- subconsulta de "última ubicación" de DispositivoDAO (WHERE estado = 'APROBADO' GROUP BY id_dispositivo)
CREATE INDEX idx_prestamo_estado_dispositivo ON prestamo(estado, id_dispositivo);

-- Índice para el filtro por rango de fechas en la búsqueda/paginación de Préstamos
CREATE INDEX idx_prestamo_fecha_inicio ON prestamo(fecha_inicio);

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

-- Índice para futuros filtros por estado (EN_PROCESO/FINALIZADO)
CREATE INDEX idx_mantenimiento_estado ON mantenimiento(estado);

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

-- Índice para el filtro por rango de fechas en la búsqueda/paginación de Auditoría
-- (el módulo donde más importa, por el volumen que suele tener esta tabla)
CREATE INDEX idx_auditoria_fecha_evento ON auditoria(fecha_evento);

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

-- ======================================
-- TABLA: NOTIFICACIÓN
-- ======================================
CREATE TABLE IF NOT EXISTS notificacion (
    id_notificacion INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    mensaje VARCHAR(255) NOT NULL,
    leida BOOLEAN DEFAULT FALSE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

-- Índice para el caso de uso principal: contar/listar no leídas de un usuario
CREATE INDEX idx_notificacion_usuario_leida ON notificacion(id_usuario, leida);

-- ======================================
-- TABLA: CORTE_DIARIO (snapshot histórico, no una vista en vivo)
-- disponibles/en_prestamo/en_mantenimiento se cuentan directamente desde
-- dispositivo.estado (única fuente de verdad) para que siempre sumen el total.
-- ======================================
CREATE TABLE IF NOT EXISTS corte_diario (
    id_corte INT AUTO_INCREMENT PRIMARY KEY,
    fecha_corte TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_dispositivos INT NOT NULL,
    disponibles INT NOT NULL,
    en_prestamo INT NOT NULL,
    en_mantenimiento INT NOT NULL DEFAULT 0,
    id_generador INT NOT NULL,

    FOREIGN KEY (id_generador) REFERENCES usuario(id_usuario)
);

-- ======================================
-- TABLA: CORTE_DIARIO_DETALLE
-- Datos de dispositivo/usuario duplicados a propósito (snapshot congelado en el
-- momento del corte; no debe cambiar si luego se renombra o elimina algo).
-- fecha_toma = prestamo.fecha_inicio del préstamo activo en ese momento.
-- ======================================
CREATE TABLE IF NOT EXISTS corte_diario_detalle (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_corte INT NOT NULL,
    id_dispositivo INT NOT NULL,
    nombre_dispositivo VARCHAR(100) NOT NULL,
    id_usuario INT NOT NULL,
    nombre_usuario VARCHAR(100) NOT NULL,
    fecha_toma TIMESTAMP NOT NULL,

    FOREIGN KEY (id_corte) REFERENCES corte_diario(id_corte),
    FOREIGN KEY (id_dispositivo) REFERENCES dispositivo(id_dispositivo),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

CREATE INDEX idx_corte_detalle_corte ON corte_diario_detalle(id_corte);