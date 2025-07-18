-- Script de inicialización para Pokemon API Database
-- Se ejecuta automáticamente cuando se crea el contenedor de PostgreSQL

-- Crear extensiones útiles
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Crear índices adicionales para optimizar consultas
-- (Las tablas se crean automáticamente por Hibernate DDL)

-- Función para actualizar timestamp automáticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Comentario informativo
COMMENT ON DATABASE pokemon_db IS 'Base de datos para Pokemon API - Arquitectura Limpia';

-- Log de inicialización
DO $$
BEGIN
    RAISE NOTICE 'Pokemon API Database initialized successfully at %', NOW();
END $$; 