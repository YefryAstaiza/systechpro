USE systechpro3;

-- Añadir columna para forzar cambio de contraseña
ALTER TABLE usuario ADD COLUMN cambio_obligatorio BOOLEAN DEFAULT FALSE;

-- Crear tabla de solicitudes de password
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

-- Insertar una auditoría del cambio de esquema si se desea (opcional)
