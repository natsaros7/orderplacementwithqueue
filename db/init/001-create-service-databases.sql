-- Runs once, on first container start against an empty data directory (standard
-- /docker-entrypoint-initdb.d behavior). Database-per-service: each service gets its own
-- database and login role, so a service can only ever see/touch its own schema even though all
-- three share one Postgres container (see README "Design notes & trade-offs").
create user orders with password 'orders';
create database orders owner orders;

create user inventory with password 'inventory';
create database inventory owner inventory;

create user payments with password 'payments';
create database payments owner payments;
