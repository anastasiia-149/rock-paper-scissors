#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
	CREATE DATABASE rockpaperscissors;
	GRANT ALL PRIVILEGES ON DATABASE rockpaperscissors TO postgres;
EOSQL