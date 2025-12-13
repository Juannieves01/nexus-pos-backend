-- ============================================
-- SCRIPT DE LIMPIEZA: ELIMINAR PROVEEDORES
-- ============================================
-- Este script elimina todas las referencias a proveedores
-- de la base de datos NexusPOS

-- ADVERTENCIA: Ejecuta este script desde pgAdmin o psql
-- conectado a la base de datos nexuspos_db

-- 1. Eliminar la columna proveedor_id de la tabla productos
ALTER TABLE productos
DROP COLUMN IF EXISTS proveedor_id CASCADE;

-- 2. Eliminar la columna proveedor_id de la tabla movimientos_inventario
ALTER TABLE movimientos_inventario
DROP COLUMN IF EXISTS proveedor_id CASCADE;

-- 3. Eliminar la tabla proveedores completamente
DROP TABLE IF EXISTS proveedores CASCADE;

-- ============================================
-- VERIFICACIÓN
-- ============================================
-- Ejecuta estas queries para verificar que todo se eliminó correctamente:

-- Verificar que la columna proveedor_id ya no existe en productos:
-- SELECT column_name FROM information_schema.columns
-- WHERE table_name = 'productos' AND column_name = 'proveedor_id';

-- Verificar que la tabla proveedores ya no existe:
-- SELECT table_name FROM information_schema.tables
-- WHERE table_name = 'proveedores';
