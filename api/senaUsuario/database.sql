-- ============================================================
--  SENA DB — Script de instalación
--  Ejecutar una sola vez en el servidor MySQL/MariaDB
-- ============================================================

CREATE DATABASE IF NOT EXISTS sena_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sena_db;

CREATE TABLE IF NOT EXISTS usuarios (
    id          INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    documento   VARCHAR(15)     NOT NULL,
    nombre      VARCHAR(80)     NOT NULL,
    apellido    VARCHAR(80)     NOT NULL,
    email       VARCHAR(120)    NOT NULL,
    password    VARCHAR(255)    NOT NULL,   -- bcrypt hash
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_documento (documento),
    UNIQUE KEY uq_email     (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
--  Usuario de prueba (contraseña: sena1234)
-- ============================================================
-- INSERT INTO usuarios (documento, nombre, apellido, email, password)
-- VALUES ('12345678', 'Admin', 'SENA',
--         'admin@sena.edu.co',
--         '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi');
