-- =====================================================================
-- FIXFLOW - Sistema de Gestión de Servicios Técnicos
-- Base de datos relacional (MySQL / MariaDB)
-- Sprint 1 y Sprint 2 - Proyecto de Clase Spring Boot + JavaScript
-- =====================================================================
-- Este script es idempotente: puede importarse varias veces desde
-- phpMyAdmin sin generar errores (elimina y vuelve a crear las tablas).
--
-- Usuarios de prueba (contraseña en texto plano -> almacenada con BCrypt):
--   ADMIN   : admin@fixflow.com    / admin123
--   ADMIN   : admin2@fixflow.com   / admin123
--   TECNICO : tecnico@fixflow.com  / tecnico123
--   TECNICO : carlos.tecnico@fixflow.com / tecnico123
--   TECNICO : maria.tecnico@fixflow.com  / tecnico123
--   CLIENTE : cliente@fixflow.com  / cliente123
--   CLIENTE : sofia@fixflow.com    / cliente123   (y diego/valentina/ricardo)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS fixflow
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fixflow;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS reparacion;
DROP TABLE IF EXISTS diagnostico;
DROP TABLE IF EXISTS asignacion;
DROP TABLE IF EXISTS solicitud;
DROP TABLE IF EXISTS equipo;
DROP TABLE IF EXISTS tecnico;
DROP TABLE IF EXISTS cliente;
DROP TABLE IF EXISTS usuario;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- USUARIO: datos de autenticación y rol (ADMIN, TECNICO, CLIENTE)
-- ---------------------------------------------------------------------
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- CLIENTE: perfil asociado a un usuario con rol CLIENTE
-- ---------------------------------------------------------------------
CREATE TABLE cliente (
    id_cliente BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL UNIQUE,
    telefono VARCHAR(30) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_cliente),
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- TECNICO: perfil asociado a un usuario con rol TECNICO
-- ---------------------------------------------------------------------
CREATE TABLE tecnico (
    id_tecnico BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL UNIQUE,
    especialidad VARCHAR(150) NOT NULL,
    telefono VARCHAR(30) NOT NULL,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_tecnico),
    CONSTRAINT fk_tecnico_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- EQUIPO: pertenece a un cliente. El serial es único.
-- ---------------------------------------------------------------------
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
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- SOLICITUD: nace en PENDIENTE (US-10) y avanza por el flujo de estados
-- ---------------------------------------------------------------------
CREATE TABLE solicitud (
    id_solicitud BIGINT NOT NULL AUTO_INCREMENT,
    id_cliente BIGINT NOT NULL,
    id_equipo BIGINT NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    prioridad ENUM('BAJA','MEDIA','ALTA') NOT NULL DEFAULT 'MEDIA',
    descripcion TEXT NOT NULL,
    estado ENUM('SOLICITUD','PENDIENTE','ASIGNADA','EN_DIAGNOSTICO','EN_REPARACION','FINALIZADA','CANCELADA')
        NOT NULL DEFAULT 'PENDIENTE',
    PRIMARY KEY (id_solicitud),
    CONSTRAINT fk_solicitud_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_solicitud_equipo FOREIGN KEY (id_equipo) REFERENCES equipo(id_equipo)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- ASIGNACION: un técnico por solicitud (US-13)
-- ---------------------------------------------------------------------
CREATE TABLE asignacion (
    id_asignacion BIGINT NOT NULL AUTO_INCREMENT,
    id_solicitud BIGINT NOT NULL UNIQUE,
    id_tecnico BIGINT NOT NULL,
    fecha_asignacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_asignacion),
    CONSTRAINT fk_asignacion_solicitud FOREIGN KEY (id_solicitud) REFERENCES solicitud(id_solicitud)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_asignacion_tecnico FOREIGN KEY (id_tecnico) REFERENCES tecnico(id_tecnico)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- DIAGNOSTICO: uno por solicitud (US-14)
-- ---------------------------------------------------------------------
CREATE TABLE diagnostico (
    id_diagnostico BIGINT NOT NULL AUTO_INCREMENT,
    id_solicitud BIGINT NOT NULL UNIQUE,
    descripcion TEXT NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_diagnostico),
    CONSTRAINT fk_diagnostico_solicitud FOREIGN KEY (id_solicitud) REFERENCES solicitud(id_solicitud)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- REPARACION: una por solicitud (US-15)
-- ---------------------------------------------------------------------
CREATE TABLE reparacion (
    id_reparacion BIGINT NOT NULL AUTO_INCREMENT,
    id_solicitud BIGINT NOT NULL UNIQUE,
    solucion TEXT NOT NULL,
    fecha_inicio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_finalizacion TIMESTAMP NULL,
    observaciones TEXT,
    PRIMARY KEY (id_reparacion),
    CONSTRAINT fk_reparacion_solicitud FOREIGN KEY (id_solicitud) REFERENCES solicitud(id_solicitud)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- DATOS DE PRUEBA + MOVIMIENTO HISTÓRICO
-- =====================================================================

-- ---- Usuarios base (documentados en el encabezado) ----
INSERT INTO usuario (nombre, apellido, email, password, rol, estado) VALUES
('Admin', 'FixFlow', 'admin@fixflow.com', '$2a$10$dZ776avWcHg788XBmcu1ZeTk6fue3vX5Arg/O7z7TgAxnQO/sf3SW', 'ADMIN', 'ACTIVO'),
('Técnico', 'Soporte', 'tecnico@fixflow.com', '$2a$10$wfPhoYzxVFySluOAGglcreNC3NzjtLMn3p5VXzNTAycABPRNTHGMW', 'TECNICO', 'ACTIVO'),
('Cliente', 'Demo', 'cliente@fixflow.com', '$2a$10$h5560ZHIDiCiP6E0bkGh0.C9JcDp7J3pT/cWPYcjXyfvYa59UwRLa', 'CLIENTE', 'ACTIVO');

-- ---- Capturar los IDs reales de los usuarios por email (robusto ante auto-increment) ----
SET @u_admin1  = (SELECT id_usuario FROM usuario WHERE email='admin@fixflow.com');
SET @u_tec1    = (SELECT id_usuario FROM usuario WHERE email='tecnico@fixflow.com');
SET @u_cli1    = (SELECT id_usuario FROM usuario WHERE email='cliente@fixflow.com');

-- ---- Usuarios adicionales (ADMIN, TECNICOs y CLIENTEs) ----
INSERT INTO usuario (nombre, apellido, email, password, rol, estado) VALUES
('Laura',   'Martínez', 'admin2@fixflow.com',       '$2a$10$dZ776avWcHg788XBmcu1ZeTk6fue3vX5Arg/O7z7TgAxnQO/sf3SW', 'ADMIN',   'ACTIVO'),
('Carlos',  'Ramírez',  'carlos.tecnico@fixflow.com','$2a$10$wfPhoYzxVFySluOAGglcreNC3NzjtLMn3p5VXzNTAycABPRNTHGMW','TECNICO', 'ACTIVO'),
('María',   'Gómez',    'maria.tecnico@fixflow.com', '$2a$10$wfPhoYzxVFySluOAGglcreNC3NzjtLMn3p5VXzNTAycABPRNTHGMW', 'TECNICO', 'ACTIVO'),
('Andrés',  'Torres',   'andres.tecnico@fixflow.com','$2a$10$wfPhoYzxVFySluOAGglcreNC3NzjtLMn3p5VXzNTAycABPRNTHGMW','TECNICO', 'INACTIVO'),
('Sofía',   'Herrera',  'sofia@fixflow.com',         '$2a$10$h5560ZHIDiCiP6E0bkGh0.C9JcDp7J3pT/cWPYcjXyfvYa59UwRLa', 'CLIENTE', 'ACTIVO'),
('Diego',   'Castro',   'diego@fixflow.com',         '$2a$10$h5560ZHIDiCiP6E0bkGh0.C9JcDp7J3pT/cWPYcjXyfvYa59UwRLa', 'CLIENTE', 'ACTIVO'),
('Valentina','Ortiz',   'valentina@fixflow.com',     '$2a$10$h5560ZHIDiCiP6E0bkGh0.C9JcDp7J3pT/cWPYcjXyfvYa59UwRLa', 'CLIENTE', 'ACTIVO'),
('Ricardo', 'Mendoza',  'ricardo@fixflow.com',       '$2a$10$h5560ZHIDiCiP6E0bkGh0.C9JcDp7J3pT/cWPYcjXyfvYa59UwRLa', 'CLIENTE', 'ACTIVO'),
('Camila',  'Rojas',    'camila@fixflow.com',        '$2a$10$h5560ZHIDiCiP6E0bkGh0.C9JcDp7J3pT/cWPYcjXyfvYa59UwRLa', 'CLIENTE', 'INACTIVO');

-- ---- Capturar IDs de los usuarios adicionales ----
SET @u_admin2  = (SELECT id_usuario FROM usuario WHERE email='admin2@fixflow.com');
SET @u_tec2    = (SELECT id_usuario FROM usuario WHERE email='carlos.tecnico@fixflow.com');
SET @u_tec3    = (SELECT id_usuario FROM usuario WHERE email='maria.tecnico@fixflow.com');
SET @u_tec4    = (SELECT id_usuario FROM usuario WHERE email='andres.tecnico@fixflow.com');
SET @u_cli2    = (SELECT id_usuario FROM usuario WHERE email='sofia@fixflow.com');
SET @u_cli3    = (SELECT id_usuario FROM usuario WHERE email='diego@fixflow.com');
SET @u_cli4    = (SELECT id_usuario FROM usuario WHERE email='valentina@fixflow.com');
SET @u_cli5    = (SELECT id_usuario FROM usuario WHERE email='ricardo@fixflow.com');
SET @u_cli6    = (SELECT id_usuario FROM usuario WHERE email='camila@fixflow.com');

-- ---- Perfiles de técnico (base + usuarios 4,5,6) ----
INSERT INTO tecnico (id_usuario, especialidad, telefono) VALUES
(@u_tec1, 'Hardware y redes', '912345678'),
(@u_tec2, 'Reparación de hardware', '931112233'),
(@u_tec3, 'Redes y conectividad',   '944556677'),
(@u_tec4, 'Soporte de software',    '966778899');

-- Capturar IDs de los perfiles de técnico
SET @t1 = (SELECT t.id_tecnico FROM tecnico t JOIN usuario u ON u.id_usuario=t.id_usuario WHERE u.email='tecnico@fixflow.com');
SET @t2 = (SELECT t.id_tecnico FROM tecnico t JOIN usuario u ON u.id_usuario=t.id_usuario WHERE u.email='carlos.tecnico@fixflow.com');
SET @t3 = (SELECT t.id_tecnico FROM tecnico t JOIN usuario u ON u.id_usuario=t.id_usuario WHERE u.email='maria.tecnico@fixflow.com');
SET @t4 = (SELECT t.id_tecnico FROM tecnico t JOIN usuario u ON u.id_usuario=t.id_usuario WHERE u.email='andres.tecnico@fixflow.com');

-- ---- Perfiles de cliente (base + usuarios adicionales) ----
INSERT INTO cliente (id_usuario, telefono, direccion) VALUES
(@u_cli1, '987654321', 'Av. Principal 123'),
(@u_cli2, '955112233', 'Jr. Los Álamos 456, Lima'),
(@u_cli3, '988445566', 'Av. Brasil 1120, Lima'),
(@u_cli4, '977889900', 'Calle Las Flores 78, Arequipa'),
(@u_cli5, '933221144', 'Av. Universitaria 245, Lima'),
(@u_cli6, '966554433', 'Calle Puno 30, Trujillo');

-- Capturar IDs de los perfiles de cliente
SET @c1 = (SELECT c.id_cliente FROM cliente c JOIN usuario u ON u.id_usuario=c.id_usuario WHERE u.email='cliente@fixflow.com');
SET @c2 = (SELECT c.id_cliente FROM cliente c JOIN usuario u ON u.id_usuario=c.id_usuario WHERE u.email='sofia@fixflow.com');
SET @c3 = (SELECT c.id_cliente FROM cliente c JOIN usuario u ON u.id_usuario=c.id_usuario WHERE u.email='diego@fixflow.com');
SET @c4 = (SELECT c.id_cliente FROM cliente c JOIN usuario u ON u.id_usuario=c.id_usuario WHERE u.email='valentina@fixflow.com');
SET @c5 = (SELECT c.id_cliente FROM cliente c JOIN usuario u ON u.id_usuario=c.id_usuario WHERE u.email='ricardo@fixflow.com');
SET @c6 = (SELECT c.id_cliente FROM cliente c JOIN usuario u ON u.id_usuario=c.id_usuario WHERE u.email='camila@fixflow.com');

-- ---- Inventario de equipos de los clientes ----
INSERT INTO equipo (id_cliente, tipo, marca, modelo, serial, descripcion) VALUES
(@c1, 'Laptop',     'Dell',    'Latitude 5420',  'DL-2024-001',  'Laptop con problema de arranque y batería'),
(@c2, 'Laptop',     'Lenovo',  'ThinkPad T14',   'LN-TP14-0091', 'Laptop corporativa con lector de huella'),
(@c2, 'Desktop',    'HP',      'ProDesk 400',    'HP-PD400-3390','Equipo de escritorio de oficina'),
(@c3, 'Laptop',     'Acer',    'Aspire 5',       'AC-AS5-7712',  'Laptop de uso personal'),
(@c3, 'Impresora',  'Epson',   'EcoTank L3250',  'EP-L3250-1188','Impresora multifuncional'),
(@c4, 'Desktop',    'Dell',    'OptiPlex 3080',  'DL-OP3080-5521','PC de escritorio con SSD'),
(@c4, 'Laptop',     'Asus',    'VivoBook 15',    'AS-VB15-9034', 'Laptop con problemas de batería'),
(@c5, 'Servidor',   'HP',      'ProLiant ML30',  'HP-ML30-0099', 'Servidor de archivos pequeño'),
(@c5, 'Laptop',     'Apple',   'MacBook Air M1', 'MB-AIR-M1-330', 'Laptop macOS corporativa'),
(@c6, 'Desktop',    'Lenovo',  'IdeaCentre 5',   'LN-IC5-4450',  'PC familiar');

-- Capturar IDs de los equipos por serial
SET @eDemo    = (SELECT id_equipo FROM equipo WHERE serial='DL-2024-001');
SET @eLenovo  = (SELECT id_equipo FROM equipo WHERE serial='LN-TP14-0091');
SET @eHPDesk  = (SELECT id_equipo FROM equipo WHERE serial='HP-PD400-3390');
SET @eAcer    = (SELECT id_equipo FROM equipo WHERE serial='AC-AS5-7712');
SET @eEpson   = (SELECT id_equipo FROM equipo WHERE serial='EP-L3250-1188');
SET @eDellPC  = (SELECT id_equipo FROM equipo WHERE serial='DL-OP3080-5521');
SET @eAsus    = (SELECT id_equipo FROM equipo WHERE serial='AS-VB15-9034');
SET @eServer  = (SELECT id_equipo FROM equipo WHERE serial='HP-ML30-0099');
SET @eMac     = (SELECT id_equipo FROM equipo WHERE serial='MB-AIR-M1-330');
SET @eIdea    = (SELECT id_equipo FROM equipo WHERE serial='LN-IC5-4450');

-- ---- Solicitudes históricas (fechas repartidas en ~6 meses) ----
INSERT INTO solicitud (id_cliente, id_equipo, fecha, prioridad, descripcion, estado) VALUES
(@c1, @eDemo,   '2026-04-08 09:15:00', 'ALTA',  'La laptop no enciende tras corte de energía.', 'FINALIZADA'),
(@c2, @eLenovo, '2026-04-15 11:40:00', 'MEDIA', 'El equipo se reinicia solo cada cierto tiempo.', 'FINALIZADA'),
(@c3, @eEpson,  '2026-04-22 14:05:00', 'BAJA',  'La impresora no imprime a color.', 'FINALIZADA'),
(@c4, @eDellPC, '2026-05-03 10:20:00', 'ALTA',  'El PC muestra pantalla azul al abrir programas.', 'FINALIZADA'),
(@c5, @eServer, '2026-05-11 16:30:00', 'ALTA',  'El servidor no responde a las peticiones internas.', 'FINALIZADA'),
(@c6, @eIdea,   '2026-05-19 08:45:00', 'MEDIA', 'El equipo está muy lento al iniciar Windows.', 'FINALIZADA'),
(@c2, @eHPDesk, '2026-06-02 12:10:00', 'BAJA',  'La fuente hace ruido y se calienta.', 'FINALIZADA'),
(@c3, @eEpson,  '2026-06-14 15:25:00', 'MEDIA', 'La impresora se atasca con frecuencia.', 'FINALIZADA'),
(@c4, @eAsus,   '2026-06-27 09:00:00', 'ALTA',  'La batería de la laptop dura pocos minutos.', 'FINALIZADA'),
(@c5, @eMac,    '2026-07-05 13:35:00', 'MEDIA', 'El MacBook se sobrecalienta con poco uso.', 'FINALIZADA'),
(@c1, @eDemo,   '2026-07-18 10:50:00', 'BAJA',  'Una tecla del teclado dejó de funcionar.', 'FINALIZADA'),
(@c6, @eIdea,   '2026-07-29 17:15:00', 'MEDIA', 'El mouse se desconecta de forma intermitente.', 'FINALIZADA'),
(@c2, @eLenovo, '2026-08-09 11:05:00', 'ALTA',  'La laptop se apaga de golpe al jugar.', 'FINALIZADA'),
(@c3, @eAcer,   '2026-08-21 14:40:00', 'MEDIA', 'El ventilador suena muy fuerte.', 'FINALIZADA'),
(@c4, @eDellPC, '2026-09-01 09:30:00', 'BAJA',  'Windows no reconoce el monitor externo.', 'FINALIZADA'),
(@c5, @eServer, '2026-09-12 16:00:00', 'ALTA',  'Perdí acceso a una carpeta compartida.', 'FINALIZADA'),
(@c6, @eIdea,   '2026-09-25 10:15:00', 'MEDIA', 'La PC se reinicia al conectar un USB.', 'FINALIZADA'),
-- Estados intermedios recientes (mes actual)
(@c1, @eDemo,   '2026-10-05 09:20:00', 'ALTA',  'La laptop tarda mucho en arrancar.', 'EN_REPARACION'),
(@c2, @eHPDesk, '2026-10-07 11:50:00', 'MEDIA', 'Se escucha un pitido al encender.', 'EN_DIAGNOSTICO'),
(@c3, @eEpson,  '2026-10-09 15:10:00', 'BAJA',  'La impresora no escanea documentos.', 'ASIGNADA'),
(@c4, @eAsus,   '2026-10-11 08:30:00', 'ALTA',  'La laptop no carga la batería.', 'ASIGNADA'),
(@c5, @eMac,    '2026-10-12 12:45:00', 'MEDIA', 'El teclado del MacBook no responde bien.', 'EN_REPARACION'),
-- Pendientes de asignar
(@c6, @eIdea,   '2026-10-14 10:05:00', 'MEDIA', 'El sistema operativo va muy lento.', 'PENDIENTE'),
(@c1, @eDemo,   '2026-10-15 14:20:00', 'BAJA',  'El disco hace ruido al leer archivos.', 'PENDIENTE'),
(@c2, @eAcer,   '2026-10-16 09:55:00', 'ALTA',  'La pantalla parpadea intermitentemente.', 'PENDIENTE'),
(@c3, @eAcer,   '2026-10-17 13:40:00', 'MEDIA', 'No puedo instalar actualizaciones.', 'PENDIENTE'),
-- Canceladas
(@c4, @eDellPC, '2026-08-30 15:30:00', 'BAJA',  'Consulta por limpieza interna (el cliente canceló).', 'CANCELADA'),
(@c5, @eServer, '2026-09-18 11:10:00', 'MEDIA', 'Migración de correo (cancelada por el cliente).', 'CANCELADA');

-- ---- Asignaciones de técnico (US-13) ----
-- Técnico 1: Técnico Soporte | 2: Carlos | 3: María | 4: Andrés
INSERT INTO asignacion (id_solicitud, id_tecnico, fecha_asignacion) VALUES
(1, @t1, '2026-04-08 10:00:00'),
(2, @t2, '2026-04-15 12:30:00'),
(3, @t3, '2026-04-22 15:00:00'),
(4, @t1, '2026-05-03 11:00:00'),
(5, @t2, '2026-05-11 17:00:00'),
(6, @t3, '2026-05-19 09:30:00'),
(7, @t1, '2026-06-02 13:00:00'),
(8, @t2, '2026-06-14 16:00:00'),
(9, @t3, '2026-06-27 10:00:00'),
(10, @t1, '2026-07-05 14:00:00'),
(11, @t2, '2026-07-18 11:30:00'),
(12, @t4, '2026-07-29 18:00:00'),
(13, @t1, '2026-08-09 12:00:00'),
(14, @t3, '2026-08-21 15:30:00'),
(15, @t2, '2026-09-01 10:15:00'),
(16, @t1, '2026-09-12 16:45:00'),
(17, @t3, '2026-09-25 11:00:00'),
(18, @t2, '2026-10-05 10:00:00'),
(19, @t3, '2026-10-07 12:15:00'),
(20, @t1, '2026-10-09 15:40:00'),
(21, @t2, '2026-10-11 09:00:00'),
(22, @t3, '2026-10-12 13:10:00');

-- ---- Diagnósticos (US-14) ----
INSERT INTO diagnostico (id_solicitud, descripcion, fecha) VALUES
(1, 'Fuente de poder dañada; se recomienda su reemplazo.', '2026-04-08 15:00:00'),
(2, 'Memoria RAM defectuosa en el segundo módulo.', '2026-04-15 17:00:00'),
(3, 'Cabezal de impresión obstruido.', '2026-04-22 18:30:00'),
(4, 'Disco duro con sectores dañados; requiere SSD.', '2026-05-03 16:00:00'),
(5, 'Servicio de red caído por configuración incorrecta.', '2026-05-11 19:30:00'),
(6, 'Muchos programas en el arranque y malware leve.', '2026-05-19 13:00:00'),
(7, 'Ventilador de la fuente desgastado.', '2026-06-02 16:45:00'),
(8, 'Rodillos de arrastre desgastados.', '2026-06-14 18:00:00'),
(9, 'Batería con celdas agotadas.', '2026-06-27 13:30:00'),
(10,'Pasta térmica seca y ventilación obstruida.', '2026-07-05 17:15:00'),
(11,'Tecla con membrana rota.', '2026-07-18 14:00:00'),
(12,'Receptor inalámbrico del mouse con interferencia.', '2026-07-29 19:00:00'),
(13,'Sobrecalentamiento por disipador sucio.', '2026-08-09 15:00:00'),
(14,'Rodamiento del ventilador dañado.', '2026-08-21 18:00:00'),
(15,'Controlador de video desactualizado.', '2026-09-01 13:00:00'),
(16,'Permisos de red compartida mal configurados.', '2026-09-12 18:30:00'),
(17,'Puerto USB con falso contacto.', '2026-09-25 13:45:00'),
(18,'Disco lento; se recomienda migrar a SSD.', '2026-10-05 12:00:00'),
(19,'Pitido de memoria: módulo mal insertado.', '2026-10-07 14:00:00'),
(22,'Teclado con fallas en varias teclas.', '2026-10-12 15:00:00');

-- ---- Reparaciones (US-15) ----
INSERT INTO reparacion (id_solicitud, solucion, fecha_inicio, fecha_finalizacion, observaciones) VALUES
(1, 'Reemplazo de fuente de poder 500W.', '2026-04-10 09:00:00', '2026-04-10 15:00:00', 'Equipo probado satisfactoriamente.'),
(2, 'Cambio de módulo de RAM de 8GB.', '2026-04-17 10:00:00', '2026-04-17 13:00:00', 'Se ejecutó memtest sin errores.'),
(3, 'Limpieza y recalibración de cabezales.', '2026-04-24 09:30:00', '2026-04-24 12:30:00', 'Impresión a color verificada.'),
(4, 'Instalación de SSD 480GB y clonado del sistema.', '2026-05-05 08:30:00', '2026-05-05 17:00:00', 'Se conservaron todos los datos.'),
(5, 'Reconfiguración de servicios de red y reinicio.', '2026-05-12 10:00:00', '2026-05-12 14:00:00', 'Servidor operativo sin incidencias.'),
(6, 'Limpieza de arranque, antivirus y actualización.', '2026-05-21 09:00:00', '2026-05-21 12:00:00', 'Arranque notablemente más rápido.'),
(7, 'Cambio de ventilador de la fuente.', '2026-06-04 10:00:00', '2026-06-04 13:30:00', 'Ruido eliminado.'),
(8, 'Cambio de rodillos de arrastre.', '2026-06-16 09:00:00', '2026-06-16 11:30:00', 'Sin atascos en pruebas.'),
(9, 'Reemplazo de batería original.', '2026-06-29 10:00:00', '2026-06-29 14:00:00', 'Autonomía restaurada.'),
(10,'Limpieza interna y cambio de pasta térmica.', '2026-07-07 09:00:00', '2026-07-07 12:00:00', 'Temperaturas normales.'),
(11,'Reemplazo del teclado completo.', '2026-07-20 10:00:00', '2026-07-20 13:00:00', 'Todas las teclas funcionan.'),
(12,'Cambio de receptor inalámbrico.', '2026-07-31 10:00:00', '2026-07-31 12:00:00', 'Conexión estable.'),
(13,'Limpieza de disipador y ventiladores.', '2026-08-11 09:00:00', '2026-08-11 12:30:00', 'Sin apagones en pruebas.'),
(14,'Cambio de ventilador del chasis.', '2026-08-23 10:00:00', '2026-08-23 13:00:00', 'Nivel de ruido normal.'),
(15,'Actualización de controlador de video.', '2026-09-03 09:00:00', '2026-09-03 11:00:00', 'Monitor externo reconocido.'),
(16,'Corrección de permisos y compartición.', '2026-09-14 10:00:00', '2026-09-14 13:00:00', 'Acceso restaurado.'),
(17,'Reparación del puerto USB.', '2026-09-27 09:00:00', '2026-09-27 12:00:00', 'Dispositivos reconocidos.'),
(18,'Migración de HDD a SSD 480GB.', '2026-10-06 08:30:00', NULL, 'En proceso de clonado y pruebas.');

-- =====================================================================
-- REPARACIÓN IDEMPOTENTE: usuarios TECNICO sin perfil en la tabla tecnico
-- (necesario para que aparezcan en el selector de asignación)
-- =====================================================================
INSERT INTO tecnico (id_usuario, especialidad, telefono)
SELECT u.id_usuario, 'General', '000000000'
FROM usuario u
WHERE u.rol = 'TECNICO'
  AND NOT EXISTS (SELECT 1 FROM tecnico t WHERE t.id_usuario = u.id_usuario);

-- =====================================================================
-- CONSULTA DE VERIFICACIÓN (opcional)
-- =====================================================================
SELECT
    s.id_solicitud,
    CONCAT(u.nombre, ' ', u.apellido) AS cliente,
    e.marca,
    e.modelo,
    s.prioridad,
    s.estado,
    CONCAT(ut.nombre, ' ', ut.apellido) AS tecnico
FROM solicitud s
INNER JOIN cliente c ON s.id_cliente = c.id_cliente
INNER JOIN usuario u ON c.id_usuario = u.id_usuario
INNER JOIN equipo e ON s.id_equipo = e.id_equipo
LEFT JOIN asignacion a ON s.id_solicitud = a.id_solicitud
LEFT JOIN tecnico t ON a.id_tecnico = t.id_tecnico
LEFT JOIN usuario ut ON t.id_usuario = ut.id_usuario;
