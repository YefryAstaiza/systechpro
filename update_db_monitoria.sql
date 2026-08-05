USE systechpro3;

-- Checkout rápido de dispositivos para monitoría (sin aprobación, sin fecha de fin)
-- y snapshots históricos de corte diario. Ejecutar en instalaciones existentes
-- que ya corrieron database.sql antes de este cambio.

CREATE TABLE IF NOT EXISTS prestamo_monitoria (
    id_monitoria INT AUTO_INCREMENT PRIMARY KEY,
    id_dispositivo INT NOT NULL,
    id_usuario INT NOT NULL,
    fecha_toma TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_devolucion TIMESTAMP NULL,
    estado ENUM('ACTIVO', 'DEVUELTO') DEFAULT 'ACTIVO',

    FOREIGN KEY (id_dispositivo) REFERENCES dispositivo(id_dispositivo),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

CREATE INDEX idx_monitoria_estado ON prestamo_monitoria(estado);
CREATE INDEX idx_monitoria_dispositivo_estado ON prestamo_monitoria(id_dispositivo, estado);

CREATE TABLE IF NOT EXISTS corte_diario (
    id_corte INT AUTO_INCREMENT PRIMARY KEY,
    fecha_corte TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_dispositivos INT NOT NULL,
    disponibles INT NOT NULL,
    en_monitoria INT NOT NULL,
    id_generador INT NOT NULL,

    FOREIGN KEY (id_generador) REFERENCES usuario(id_usuario)
);

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
