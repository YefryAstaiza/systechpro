USE systechpro3;
ALTER TABLE prestamo ADD COLUMN id_aprobador INT NULL AFTER fecha_creacion;
ALTER TABLE prestamo ADD CONSTRAINT fk_prestamo_aprobador FOREIGN KEY (id_aprobador) REFERENCES usuario(id_usuario);
