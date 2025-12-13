-- ============================================================================
-- SCRIPT PARA RECREAR LA BASE DE DATOS NEXUSPOS
-- ============================================================================
-- Este script elimina y recrea la base de datos completamente.
-- ADVERTENCIA: Todos los datos se perderán.
-- ============================================================================

-- 1. Conectarse a la base de datos postgres (no a nexuspos)
-- Ejecuta esto primero

-- 2. Terminar todas las conexiones activas a la base de datos nexuspos_db
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = 'nexuspos_db'
  AND pid <> pg_backend_pid();

-- 3. Eliminar la base de datos
DROP DATABASE IF EXISTS nexuspos_db;

-- 4. Crear la base de datos nuevamente
CREATE DATABASE nexuspos_db
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'Spanish_Paraguay.1252'
    LC_CTYPE = 'Spanish_Paraguay.1252'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- ============================================================================
-- INSTRUCCIONES:
-- ============================================================================
-- 1. Abre pgAdmin o tu cliente de PostgreSQL
-- 2. Conéctate a la base de datos "postgres" (NO a "nexuspos")
-- 3. Ejecuta este script completo
-- 4. Reinicia tu aplicación Spring Boot
-- 5. Hibernate creará automáticamente todas las tablas con la nueva estructura
-- ============================================================================
