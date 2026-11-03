-- Base de datos del Sistema de Facturación de la Comercializadora de Teléfonos Celulares.
-- Motor: MariaDB / MySQL. Ejecutar como root:
--   mysql -u root -p < COMERCIALIZADORA.sql
-- No maneja inventario: CELULAR es solo el catálogo de teléfonos disponibles para la venta.
-- Las ventas con crédito directo guardan el código del crédito otorgado por el Banco BanQuito.

DROP DATABASE IF EXISTS comercializadora;
CREATE DATABASE comercializadora CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'comercializadora'@'localhost' IDENTIFIED BY 'comercializadora';
GRANT ALL PRIVILEGES ON comercializadora.* TO 'comercializadora'@'localhost';
FLUSH PRIVILEGES;
USE comercializadora;

-- Catálogo de teléfonos celulares a la venta; DISPONIBLE = 0 lo retira del catálogo sin perder las facturas
CREATE TABLE CELULAR (
    COD_CELULAR INT           NOT NULL AUTO_INCREMENT,
    MARCA       VARCHAR(50)   NOT NULL,
    MODELO      VARCHAR(100)  NOT NULL,
    DESCRIPCION VARCHAR(255)  NULL,
    PRECIO      NUMERIC(10,2) NOT NULL,
    DISPONIBLE  TINYINT(1)    NOT NULL DEFAULT 1,
    CONSTRAINT PK_CELULAR PRIMARY KEY (COD_CELULAR),
    CONSTRAINT CK_CELULAR_PRECIO CHECK (PRECIO > 0)
);

-- Compradores; la cédula es la que se envía al banco para verificar si es sujeto de crédito
CREATE TABLE COMPRADOR (
    CEDULA VARCHAR(10)  NOT NULL,
    NOMBRE VARCHAR(100) NOT NULL,
    CONSTRAINT PK_COMPRADOR PRIMARY KEY (CEDULA)
);

-- Cabecera de la factura. En efectivo DESCUENTO = 42% del SUBTOTAL.
-- En crédito directo DESCUENTO = 0 y se registran el crédito del banco, las cuotas y el valor de la cuota.
CREATE TABLE FACTURA (
    COD_FACTURA   INT           NOT NULL AUTO_INCREMENT,
    CEDULA        VARCHAR(10)   NOT NULL,
    FECHA         DATETIME      NOT NULL,
    FORMA_PAGO    VARCHAR(10)   NOT NULL,
    SUBTOTAL      NUMERIC(10,2) NOT NULL,
    DESCUENTO     NUMERIC(10,2) NOT NULL DEFAULT 0,
    TOTAL         NUMERIC(10,2) NOT NULL,
    COD_CREDITO   INT           NULL,
    NUMERO_CUOTAS INT           NULL,
    VALOR_CUOTA   NUMERIC(10,2) NULL,
    CONSTRAINT PK_FACTURA PRIMARY KEY (COD_FACTURA),
    CONSTRAINT FK_FACTURA_COMPRADOR FOREIGN KEY (CEDULA) REFERENCES COMPRADOR (CEDULA),
    CONSTRAINT CK_FACTURA_FORMA_PAGO CHECK (FORMA_PAGO IN ('EFECTIVO', 'CREDITO')),
    CONSTRAINT CK_FACTURA_CREDITO CHECK (
        (FORMA_PAGO = 'EFECTIVO' AND COD_CREDITO IS NULL AND NUMERO_CUOTAS IS NULL)
        OR (FORMA_PAGO = 'CREDITO' AND COD_CREDITO IS NOT NULL AND NUMERO_CUOTAS BETWEEN 3 AND 18))
);

CREATE TABLE DETALLE_FACTURA (
    COD_FACTURA     INT           NOT NULL,
    COD_CELULAR     INT           NOT NULL,
    CANTIDAD        INT           NOT NULL,
    PRECIO_UNITARIO NUMERIC(10,2) NOT NULL,
    SUBTOTAL        NUMERIC(10,2) NOT NULL,
    CONSTRAINT PK_DETALLE_FACTURA PRIMARY KEY (COD_FACTURA, COD_CELULAR),
    CONSTRAINT FK_DETALLE_FACTURA FOREIGN KEY (COD_FACTURA) REFERENCES FACTURA (COD_FACTURA),
    CONSTRAINT FK_DETALLE_CELULAR FOREIGN KEY (COD_CELULAR) REFERENCES CELULAR (COD_CELULAR),
    CONSTRAINT CK_DETALLE_CANTIDAD CHECK (CANTIDAD > 0)
);

INSERT INTO CELULAR (COD_CELULAR, MARCA, MODELO, DESCRIPCION, PRECIO) VALUES
    (1, 'Samsung', 'Galaxy A15', '6.5", 128 GB, 4 GB RAM', 199.00),
    (2, 'Xiaomi', 'Redmi Note 13', '6.67", 256 GB, 8 GB RAM', 249.00),
    (3, 'Motorola', 'Moto G84', '6.55", 256 GB, 12 GB RAM', 299.00),
    (4, 'Samsung', 'Galaxy A55', '6.6", 256 GB, 8 GB RAM', 450.00),
    (5, 'Apple', 'iPhone 13', '6.1", 128 GB', 600.00),
    (6, 'Honor', 'Magic 6 Lite', '6.78", 256 GB, 8 GB RAM', 349.00),
    (7, 'Apple', 'iPhone 15 Pro', '6.1", 256 GB', 1299.00),
    (8, 'Samsung', 'Galaxy S24 Ultra', '6.8", 512 GB, 12 GB RAM', 1499.00);

-- Ventas de ejemplo:
--   1 en efectivo a Ana (199.00 - 42% = 115.42).
--   2 con crédito directo a Jorge: corresponde al crédito 1 cargado en BANQUITO.sql (600.00 a 12 cuotas).
INSERT INTO COMPRADOR (CEDULA, NOMBRE) VALUES
    ('1712345678', 'Ana Lucía Torres'),
    ('1756789012', 'Jorge Esteban Ruiz');

INSERT INTO FACTURA (COD_FACTURA, CEDULA, FECHA, FORMA_PAGO, SUBTOTAL, DESCUENTO, TOTAL,
        COD_CREDITO, NUMERO_CUOTAS, VALOR_CUOTA) VALUES
    (1, '1712345678', NOW() - INTERVAL 30 DAY, 'EFECTIVO', 199.00, 83.58, 115.42, NULL, NULL, NULL),
    (2, '1756789012', NOW() - INTERVAL 20 DAY, 'CREDITO', 600.00, 0.00, 600.00, 1, 12, 54.58);

INSERT INTO DETALLE_FACTURA (COD_FACTURA, COD_CELULAR, CANTIDAD, PRECIO_UNITARIO, SUBTOTAL) VALUES
    (1, 1, 1, 199.00, 199.00),
    (2, 5, 1, 600.00, 600.00);
