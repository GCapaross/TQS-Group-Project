-- Create users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT           ,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    credit_card VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'USER'
    -- removed works_at_station_id, will be handled by a join table station_workers
);

-- Create companies table
CREATE TABLE companies (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    owner_id INTEGER REFERENCES users(id) ON DELETE SET NULL -- Assuming a company can exist without an owner or owner deletion doesn't delete company
);

-- Create stations table
CREATE TABLE stations (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    price_per_kwh DECIMAL(10, 2) NOT NULL,
    timetable VARCHAR(255),
    company_id INTEGER REFERENCES companies(id) ON DELETE SET NULL -- Station can exist without a company, or company deletion doesn't delete stations
);

-- Create station_supported_connectors table (for Station.supportedConnectors)
CREATE TABLE station_supported_connectors (
    station_id INTEGER REFERENCES stations(id) ON DELETE CASCADE,
    connector_type VARCHAR(255) NOT NULL,
    PRIMARY KEY (station_id, connector_type)
);

-- Create chargers table (for Station.chargers)
CREATE TABLE chargers (
    id SERIAL PRIMARY KEY,
    station_id INTEGER REFERENCES stations(id) ON DELETE CASCADE, -- Foreign key to stations
    status VARCHAR(50) NOT NULL,
    charging_speed_kw DECIMAL(10, 2)
);

-- Create station_workers join table (for Station.workers)
CREATE TABLE station_workers (
    station_id INTEGER REFERENCES stations(id) ON DELETE CASCADE,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (station_id, user_id)
);

-- Create reservations table
CREATE TABLE reservations (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    station_id INTEGER REFERENCES stations(id) ON DELETE CASCADE,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP
);

-- Create receipts table
CREATE TABLE receipts (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE NOT NULL,
    station_id INTEGER REFERENCES stations(id) ON DELETE CASCADE NOT NULL,
    energy DECIMAL(10, 2),
    cost DECIMAL(10, 2),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL
);