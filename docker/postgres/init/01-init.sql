-- Initialize PostgreSQL database
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Create test database for unit tests
CREATE DATABASE static_data_platform_test;

-- Create additional schemas if needed
-- CREATE SCHEMA IF NOT EXISTS audit;
-- CREATE SCHEMA IF NOT EXISTS reporting;
