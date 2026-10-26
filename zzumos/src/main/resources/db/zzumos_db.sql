-- =====================================================================
-- Zzumos - Script del modelo físico (MySQL 8.0, InnoDB, utf8mb4)
-- 10 tablas. Basado en el Anexo A, con 2 correcciones marcadas [FIX].
-- =====================================================================
CREATE DATABASE IF NOT EXISTS zzumos_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE zzumos_db;

CREATE TABLE usuario (
  id_usuario      INT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombres         VARCHAR(100) NOT NULL,
  correo          VARCHAR(120) NOT NULL,
  contrasena_hash VARCHAR(255) NOT NULL COMMENT 'Hash (ej. BCrypt), nunca texto plano',
  rol             ENUM('CLIENTE','PERSONAL','ADMINISTRADOR') NOT NULL DEFAULT 'CLIENTE',
  activo          TINYINT(1)   NOT NULL DEFAULT 1,
  fecha_registro  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
  CONSTRAINT uq_usuario_correo UNIQUE (correo)
) ENGINE=InnoDB;

CREATE TABLE cliente (
  id_cliente     INT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_usuario     INT UNSIGNED NULL,
  nombres        VARCHAR(100) NOT NULL,
  apellidos      VARCHAR(100) NULL,
  telefono       VARCHAR(15)  NOT NULL,
  fecha_registro DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_cliente PRIMARY KEY (id_cliente),
  CONSTRAINT uq_cliente_usuario UNIQUE (id_usuario),
  CONSTRAINT fk_cliente_usuario FOREIGN KEY (id_usuario)
    REFERENCES usuario (id_usuario) ON UPDATE CASCADE ON DELETE SET NULL,
  INDEX idx_cliente_telefono (telefono)
) ENGINE=InnoDB;

CREATE TABLE producto (
  id_producto INT UNSIGNED  NOT NULL AUTO_INCREMENT,
  nombre      VARCHAR(80)   NOT NULL,
  descripcion VARCHAR(200)  NULL,
  precio_base DECIMAL(8,2)  NOT NULL,
  disponible  TINYINT(1)    NOT NULL DEFAULT 1,
  CONSTRAINT pk_producto PRIMARY KEY (id_producto),
  CONSTRAINT uq_producto_nombre UNIQUE (nombre),
  CONSTRAINT ck_producto_precio CHECK (precio_base >= 0)
) ENGINE=InnoDB;

CREATE TABLE opcion_producto (
  id_opcion       INT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_producto     INT UNSIGNED NOT NULL,
  grupo           ENUM('PAN','COMPLEMENTO','BEBIDA') NOT NULL,
  nombre          VARCHAR(60)  NOT NULL,
  precio_adicional DECIMAL(8,2) NOT NULL DEFAULT 0.00,
  disponible      TINYINT(1)   NOT NULL DEFAULT 1,
  CONSTRAINT pk_opcion_producto PRIMARY KEY (id_opcion),
  CONSTRAINT uq_opcion UNIQUE (id_producto, grupo, nombre),
  CONSTRAINT ck_opcion_precio CHECK (precio_adicional >= 0),
  CONSTRAINT fk_opcion_producto FOREIGN KEY (id_producto)
    REFERENCES producto (id_producto) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE pedido (
  id_pedido           INT UNSIGNED NOT NULL AUTO_INCREMENT,
  codigo_pedido       VARCHAR(12)  NOT NULL COMMENT 'Ej. ZZ-0047',
  id_cliente          INT UNSIGNED NOT NULL,
  -- [FIX 1] Era NOT NULL con FK ON DELETE SET NULL (MySQL lo rechaza) y,
  -- además, al crear el pedido todavía no lo atiende ningún personal.
  id_usuario_atencion INT UNSIGNED NULL COMMENT 'Personal que atendió/actualizó el pedido',
  fecha_hora          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado              ENUM('PENDIENTE','EN_PREPARACION','LISTO','ENTREGADO','CANCELADO')
                      NOT NULL DEFAULT 'PENDIENTE',
  -- [FIX 2] El método de pago se elige al confirmar el pedido (RF07),
  -- pero el modelo de 10 tablas solo lo guardaba en PAGO (que se crea al entregar).
  metodo_pago         ENUM('EFECTIVO','YAPE','PLIN') NOT NULL DEFAULT 'EFECTIVO',
  direccion_entrega   VARCHAR(200) NULL,
  referencia          VARCHAR(120) NULL,
  costo_envio         DECIMAL(6,2) NOT NULL DEFAULT 0.00,
  total               DECIMAL(9,2) NOT NULL,
  CONSTRAINT pk_pedido PRIMARY KEY (id_pedido),
  CONSTRAINT uq_pedido_codigo UNIQUE (codigo_pedido),
  CONSTRAINT uq_pedido_cliente UNIQUE (id_pedido, id_cliente),
  CONSTRAINT ck_pedido_total CHECK (total >= 0 AND costo_envio >= 0),
  CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente)
    REFERENCES cliente (id_cliente) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_pedido_usuario FOREIGN KEY (id_usuario_atencion)
    REFERENCES usuario (id_usuario) ON UPDATE CASCADE ON DELETE SET NULL,
  INDEX idx_pedido_cliente (id_cliente),
  INDEX idx_pedido_estado_fecha (estado, fecha_hora),
  INDEX idx_pedido_fecha (fecha_hora)
) ENGINE=InnoDB;

CREATE TABLE detalle_pedido (
  id_detalle      INT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_pedido       INT UNSIGNED NOT NULL,
  id_producto     INT UNSIGNED NOT NULL,
  cantidad        SMALLINT UNSIGNED NOT NULL DEFAULT 1,
  precio_unitario DECIMAL(8,2) NOT NULL,
  nota_cocina     VARCHAR(200) NULL,
  CONSTRAINT pk_detalle_pedido PRIMARY KEY (id_detalle),
  CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0),
  CONSTRAINT ck_detalle_precio CHECK (precio_unitario >= 0),
  CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido)
    REFERENCES pedido (id_pedido) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto)
    REFERENCES producto (id_producto) ON UPDATE CASCADE ON DELETE RESTRICT,
  INDEX idx_detalle_pedido (id_pedido),
  INDEX idx_detalle_producto (id_producto)
) ENGINE=InnoDB;

CREATE TABLE detalle_pedido_opcion (
  id_detalle       INT UNSIGNED NOT NULL,
  id_opcion        INT UNSIGNED NOT NULL,
  precio_adicional DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT 'Precio vigente al pedir',
  CONSTRAINT pk_detalle_pedido_opcion PRIMARY KEY (id_detalle, id_opcion),
  CONSTRAINT ck_dpo_precio CHECK (precio_adicional >= 0),
  CONSTRAINT fk_dpo_detalle FOREIGN KEY (id_detalle)
    REFERENCES detalle_pedido (id_detalle) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_dpo_opcion FOREIGN KEY (id_opcion)
    REFERENCES opcion_producto (id_opcion) ON UPDATE CASCADE ON DELETE RESTRICT,
  INDEX idx_dpo_opcion (id_opcion)
) ENGINE=InnoDB;

CREATE TABLE pedido_habitual (
  id_pedido_habitual INT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_cliente         INT UNSIGNED NOT NULL,
  id_pedido_base     INT UNSIGNED NOT NULL,
  fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT pk_pedido_habitual PRIMARY KEY (id_pedido_habitual),
  CONSTRAINT uq_habitual_cliente UNIQUE (id_cliente),
  CONSTRAINT fk_habitual_cliente FOREIGN KEY (id_cliente)
    REFERENCES cliente (id_cliente) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_habitual_pedido FOREIGN KEY (id_pedido_base, id_cliente)
    REFERENCES pedido (id_pedido, id_cliente) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE venta (
  id_venta    INT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_pedido   INT UNSIGNED NOT NULL,
  fecha_venta DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado      ENUM('REGISTRADA','ANULADA') NOT NULL DEFAULT 'REGISTRADA',
  CONSTRAINT pk_venta PRIMARY KEY (id_venta),
  CONSTRAINT uq_venta_pedido UNIQUE (id_pedido),
  CONSTRAINT fk_venta_pedido FOREIGN KEY (id_pedido)
    REFERENCES pedido (id_pedido) ON UPDATE CASCADE ON DELETE RESTRICT,
  INDEX idx_venta_fecha (fecha_venta)
) ENGINE=InnoDB;

CREATE TABLE pago (
  id_pago     INT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_venta    INT UNSIGNED NOT NULL,
  metodo      ENUM('EFECTIVO','YAPE','PLIN') NOT NULL,
  estado_pago ENUM('PENDIENTE','CONFIRMADO') NOT NULL DEFAULT 'PENDIENTE',
  fecha_pago  DATETIME NULL,
  CONSTRAINT pk_pago PRIMARY KEY (id_pago),
  CONSTRAINT uq_pago_venta UNIQUE (id_venta),
  CONSTRAINT fk_pago_venta FOREIGN KEY (id_venta)
    REFERENCES venta (id_venta) ON UPDATE CASCADE ON DELETE RESTRICT,
  INDEX idx_pago_metodo (metodo, estado_pago)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Datos de prueba (tomados de los prototipos)
-- ---------------------------------------------------------------------
INSERT INTO producto (nombre, descripcion, precio_base) VALUES
 ('Desayuno Clásico',  'Pan, huevo, jugo y café',            15.00),
 ('Bowl de Frutas',    'Frutas de temporada + granola',      18.00),
 ('Pancakes Especiales','Pancakes con miel y fresas',        22.00);

INSERT INTO opcion_producto (id_producto, grupo, nombre, precio_adicional) VALUES
 (1,'PAN','Pan blanco',0.00), (1,'PAN','Pan integral',0.00), (1,'PAN','Pan ciabatta',0.00),
 (1,'COMPLEMENTO','Palta',3.00), (1,'COMPLEMENTO','Queso crema',2.00),
 (1,'COMPLEMENTO','Jamón extra',4.00), (1,'COMPLEMENTO','Mermelada',1.50),
 (1,'COMPLEMENTO','Mantequilla',1.00),
 (1,'BEBIDA','Café americano',0.00), (1,'BEBIDA','Jugo de naranja',0.00),
 (1,'BEBIDA','Té verde',0.00), (1,'BEBIDA','Leche con chocolate',0.00);

INSERT INTO cliente (nombres, apellidos, telefono) VALUES ('Ana','García','999888777');
