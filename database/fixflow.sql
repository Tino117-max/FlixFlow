CREATE DATABASE IF NOT EXISTS fixflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fixflow;

CREATE TABLE usuario (
    id_usuario BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol ENUM('ADMIN','TECNICO','CLIENTE') NOT NULL,
    estado ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario)
);

CREATE TABLE cliente (
    id_cliente BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL UNIQUE,
    telefono VARCHAR(30) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_cliente),
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

CREATE TABLE tecnico (
    id_tecnico BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL UNIQUE,
    especialidad VARCHAR(150) NOT NULL,
    telefono VARCHAR(30) NOT NULL,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_tecnico),
    CONSTRAINT fk_tecnico_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

CREATE TABLE equipo (
    id_equipo BIGINT NOT NULL AUTO_INCREMENT,
    id_cliente BIGINT NOT NULL,
    tipo VARCHAR(100) NOT NULL,
    marca VARCHAR(100) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    serial VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_equipo),
    CONSTRAINT fk_equipo_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
);

CREATE TABLE solicitud (
    id_solicitud BIGINT NOT NULL AUTO_INCREMENT,
    id_cliente BIGINT NOT NULL,
    id_equipo BIGINT NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    prioridad ENUM('BAJA','MEDIA','ALTA') NOT NULL,
    descripcion TEXT NOT NULL,
    estado ENUM('SOLICITUD','PENDIENTE','ASIGNADA','EN_DIAGNOSTICO','EN_REPARACION','FINALIZADA','CANCELADA') NOT NULL DEFAULT 'SOLICITUD',
    PRIMARY KEY (id_solicitud),
    CONSTRAINT fk_solicitud_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente),
    CONSTRAINT fk_solicitud_equipo FOREIGN KEY (id_equipo) REFERENCES equipo(id_equipo)
);

CREATE TABLE asignacion (
    id_asignacion BIGINT NOT NULL AUTO_INCREMENT,
    id_solicitud BIGINT NOT NULL UNIQUE,
    id_tecnico BIGINT NOT NULL,
    fecha_asignacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_asignacion),
    CONSTRAINT fk_asignacion_solicitud FOREIGN KEY (id_solicitud) REFERENCES solicitud(id_solicitud),
    CONSTRAINT fk_asignacion_tecnico FOREIGN KEY (id_tecnico) REFERENCES tecnico(id_tecnico)
);

CREATE TABLE diagnostico (
    id_diagnostico BIGINT NOT NULL AUTO_INCREMENT,
    id_solicitud BIGINT NOT NULL UNIQUE,
    descripcion TEXT NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_diagnostico),
    CONSTRAINT fk_diagnostico_solicitud FOREIGN KEY (id_solicitud) REFERENCES solicitud(id_solicitud)
);

CREATE TABLE reparacion (
    id_reparacion BIGINT NOT NULL AUTO_INCREMENT,
    id_solicitud BIGINT NOT NULL UNIQUE,
    solucion TEXT NOT NULL,
    fecha_inicio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_finalizacion TIMESTAMP NULL,
    observaciones TEXT,
    PRIMARY KEY (id_reparacion),
    CONSTRAINT fk_reparacion_solicitud FOREIGN KEY (id_solicitud) REFERENCES solicitud(id_solicitud)
);

INSERT INTO usuario (nombre, apellido, email, password, rol, estado) VALUES
('Admin', 'FixFlow', 'admin@fixflow.com', '$2a$10$6vw/z/KwQtAiW4PAhwefNecsYe69VLLiKSIP6PGOaxPLs.MpDoVsy', 'ADMIN', 'ACTIVO'),
('Técnico', 'Soporte', 'tecnico@fixflow.com', '$2a$10$xNF5ZnyNL1pd16XyP7mPKOiiP1z84LwHhD1GQb/eUOAHawkg34vhu', 'TECNICO', 'ACTIVO'),
('Cliente', 'Demo', 'cliente@fixflow.com', '$2a$10$Di5IPTBUFGcOZ4JaLWn34.0TVJh5qHnYzXwYmLX6BOk3QsC096.Di', 'CLIENTE', 'ACTIVO');

INSERT INTO cliente (id_usuario, telefono, direccion) VALUES
(3, '987654321', 'Av. Principal 123');

INSERT INTO tecnico (id_usuario, especialidad, telefono) VALUES
(2, 'Hardware y redes', '912345678');

INSERT INTO equipo (id_cliente, tipo, marca, modelo, serial, descripcion) VALUES
(1, 'Laptop', 'Dell', 'Latitude 5420', 'DL-2024-001', 'Laptop con problema de arranque y batería');

INSERT INTO solicitud (id_cliente, id_equipo, prioridad, descripcion, estado) VALUES
(1, 1, 'ALTA', 'La laptop tarda en encender y muestra pantalla azul al iniciar.', 'PENDIENTE');

INSERT INTO asignacion (id_solicitud, id_tecnico) VALUES
(1, 1);

UPDATE solicitud SET estado = 'ASIGNADA' WHERE id_solicitud = 1;
