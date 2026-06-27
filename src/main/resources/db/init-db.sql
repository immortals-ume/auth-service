-- ══════════════════════════════════════════════════════════════════════════════════════════════
-- PostgreSQL Database & Schema Initialisation
-- Run this ONCE as a superuser (postgres) BEFORE starting the application or Liquibase.
-- Liquibase handles all table/index/sequence DDL — this script only sets up the database,
-- roles, schemas, extensions, and permissions.
--
-- Usage (local):
--   psql -U postgres -f init-db.sql
--
-- Usage (Docker):
--   docker exec -i <container> psql -U postgres -f /docker-entrypoint-initdb.d/init-db.sql
-- ══════════════════════════════════════════════════════════════════════════════════════════════

-- ── 1. Roles ──────────────────────────────────────────────────────────────────────────────────
-- Write role: used by the application write datasource
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'admin') THEN
        CREATE ROLE admin WITH LOGIN PASSWORD 'admin' NOSUPERUSER NOCREATEDB NOCREATEROLE;
    END IF;
END
$$;

-- Read-only role: used by the application read (replica) datasource
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'replicator') THEN
        CREATE ROLE replicator WITH LOGIN PASSWORD 'replicator' NOSUPERUSER NOCREATEDB NOCREATEROLE;
    END IF;
END
$$;

-- ── 2. Database ───────────────────────────────────────────────────────────────────────────────
-- Run this block connected to the 'postgres' database.
-- Creates user_db if it does not exist and sets admin as owner.
SELECT 'CREATE DATABASE user_db OWNER admin ENCODING ''UTF8'' LC_COLLATE ''en_US.UTF-8'' LC_CTYPE ''en_US.UTF-8'' TEMPLATE template0'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'user_db')\gexec

-- ══════════════════════════════════════════════════════════════════════════════════════════════
-- Everything below runs connected to user_db.
-- If running manually: \c user_db
-- ══════════════════════════════════════════════════════════════════════════════════════════════
\c user_db

-- ── 3. Extensions ─────────────────────────────────────────────────────────────────────────────
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ── 4. Schemas ────────────────────────────────────────────────────────────────────────────────
CREATE SCHEMA IF NOT EXISTS user_auth   AUTHORIZATION admin;
CREATE SCHEMA IF NOT EXISTS user_audit  AUTHORIZATION admin;
CREATE SCHEMA IF NOT EXISTS user_session AUTHORIZATION admin;
CREATE SCHEMA IF NOT EXISTS user_oauth  AUTHORIZATION admin;

-- Liquibase tracking tables live in public schema
-- (spring.liquibase.liquibase-schema=public)

-- ── 5. Search path ────────────────────────────────────────────────────────────────────────────
ALTER ROLE admin       SET search_path TO user_auth, user_audit, user_session, user_oauth, public;
ALTER ROLE replicator  SET search_path TO user_auth, user_audit, user_session, user_oauth, public;

-- ── 6. Schema privileges — admin (write) ──────────────────────────────────────────────────────
GRANT ALL PRIVILEGES ON DATABASE user_db TO admin;

GRANT ALL ON SCHEMA user_auth    TO admin;
GRANT ALL ON SCHEMA user_audit   TO admin;
GRANT ALL ON SCHEMA user_session TO admin;
GRANT ALL ON SCHEMA user_oauth   TO admin;
GRANT ALL ON SCHEMA public       TO admin;

-- Future tables created in these schemas are auto-accessible to admin
ALTER DEFAULT PRIVILEGES FOR ROLE admin IN SCHEMA user_auth    GRANT ALL ON TABLES    TO admin;
ALTER DEFAULT PRIVILEGES FOR ROLE admin IN SCHEMA user_auth    GRANT ALL ON SEQUENCES TO admin;
ALTER DEFAULT PRIVILEGES FOR ROLE admin IN SCHEMA user_audit   GRANT ALL ON TABLES    TO admin;
ALTER DEFAULT PRIVILEGES FOR ROLE admin IN SCHEMA user_audit   GRANT ALL ON SEQUENCES TO admin;
ALTER DEFAULT PRIVILEGES FOR ROLE admin IN SCHEMA user_session GRANT ALL ON TABLES    TO admin;
ALTER DEFAULT PRIVILEGES FOR ROLE admin IN SCHEMA user_session GRANT ALL ON SEQUENCES TO admin;
ALTER DEFAULT PRIVILEGES FOR ROLE admin IN SCHEMA user_oauth   GRANT ALL ON TABLES    TO admin;
ALTER DEFAULT PRIVILEGES FOR ROLE admin IN SCHEMA public       GRANT ALL ON TABLES    TO admin;
ALTER DEFAULT PRIVILEGES FOR ROLE admin IN SCHEMA public       GRANT ALL ON SEQUENCES TO admin;

-- ── 7. Schema privileges — replicator (read-only) ────────────────────────────────────────────
GRANT CONNECT ON DATABASE user_db TO replicator;

GRANT USAGE ON SCHEMA user_auth    TO replicator;
GRANT USAGE ON SCHEMA user_audit   TO replicator;
GRANT USAGE ON SCHEMA user_session TO replicator;
GRANT USAGE ON SCHEMA user_oauth   TO replicator;
GRANT USAGE ON SCHEMA public       TO replicator;

-- Future tables created in these schemas are auto-readable by replicator
ALTER DEFAULT PRIVILEGES FOR ROLE admin IN SCHEMA user_auth    GRANT SELECT ON TABLES    TO replicator;
ALTER DEFAULT PRIVILEGES FOR ROLE admin IN SCHEMA user_auth    GRANT SELECT ON SEQUENCES TO replicator;
ALTER DEFAULT PRIVILEGES FOR ROLE admin IN SCHEMA user_audit   GRANT SELECT ON TABLES    TO replicator;
ALTER DEFAULT PRIVILEGES FOR ROLE admin IN SCHEMA user_audit   GRANT SELECT ON SEQUENCES TO replicator;
ALTER DEFAULT PRIVILEGES FOR ROLE admin IN SCHEMA user_session GRANT SELECT ON TABLES    TO replicator;
ALTER DEFAULT PRIVILEGES FOR ROLE admin IN SCHEMA user_session GRANT SELECT ON SEQUENCES TO replicator;
ALTER DEFAULT PRIVILEGES FOR ROLE admin IN SCHEMA user_oauth   GRANT SELECT ON TABLES    TO replicator;
ALTER DEFAULT PRIVILEGES FOR ROLE admin IN SCHEMA public       GRANT SELECT ON TABLES    TO replicator;
ALTER DEFAULT PRIVILEGES FOR ROLE admin IN SCHEMA public       GRANT SELECT ON SEQUENCES TO replicator;

-- ── 8. Liquibase lock table permissions ───────────────────────────────────────────────────────
-- Liquibase needs write access to public schema for its tracking tables.
-- replicator does NOT need access to these.
GRANT ALL ON SCHEMA public TO admin;

-- ── Verification ──────────────────────────────────────────────────────────────────────────────
DO $$
BEGIN
    RAISE NOTICE '✓ Database : user_db';
    RAISE NOTICE '✓ Schemas  : user_auth, user_audit, user_session, user_oauth, public';
    RAISE NOTICE '✓ Roles    : admin (write), replicator (read-only)';
    RAISE NOTICE '✓ Ready for Liquibase migration';
END
$$;
