-- Create schemas
CREATE SCHEMA IF NOT EXISTS accounts;
CREATE SCHEMA IF NOT EXISTS cash;
CREATE SCHEMA IF NOT EXISTS transfer;

-- Grant permissions
GRANT ALL PRIVILEGES ON SCHEMA accounts TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA cash TO postgres;
GRANT ALL PRIVILEGES ON SCHEMA transfer TO postgres;

-- Set search path (optional)
ALTER DATABASE bank SET search_path TO accounts, cash, transfer, public;