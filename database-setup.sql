-- PostgreSQL Database Setup for Location Management System
-- This script sets up the database with PostGIS extension for geospatial support

-- Create database (run this as postgres superuser)
-- CREATE DATABASE locationdb;

-- Connect to the database
\c locationdb;

-- Enable PostGIS extension for geospatial support
CREATE EXTENSION IF NOT EXISTS postgis;

-- Verify PostGIS installation
SELECT PostGIS_version();

-- Create indexes on geometry columns for optimal performance
-- These will be created automatically by Hibernate, but included here for reference

-- Create spatial index on cities coordinates
-- CREATE INDEX idx_cities_coordinates ON cities USING GIST (coordinates);

-- Create spatial index on areas coordinates
-- CREATE INDEX idx_areas_coordinates ON areas USING GIST (coordinates);

-- Create spatial index on locations coordinates
-- CREATE INDEX idx_locations_coordinates ON locations USING GIST (coordinates);

-- Grant permissions (adjust as needed)
-- GRANT ALL PRIVILEGES ON DATABASE locationdb TO your_app_user;
