USE systechpro3;

-- Tabla de notificaciones en panel (Fase de notificaciones - post auditoría técnica).
-- Ejecutar en instalaciones existentes que ya corrieron database.sql antes de este cambio.

CREATE TABLE IF NOT EXISTS notificacion (
    id_notificacion INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    mensaje VARCHAR(255) NOT NULL,
    leida BOOLEAN DEFAULT FALSE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

CREATE INDEX idx_notificacion_usuario_leida ON notificacion(id_usuario, leida);
