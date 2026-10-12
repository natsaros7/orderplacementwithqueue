-- Flyway migrations run exactly once (tracked by checksum in flyway_schema_history), so plain
-- INSERTs are safe here unlike the data.sql this replaces, which re-ran (and had to guard against
-- a PK violation) on every application start.
insert into inventory_item (product_code, stock) values ('WIDGET', 50);
insert into inventory_item (product_code, stock) values ('GADGET', 25);
insert into inventory_item (product_code, stock) values ('GIZMO', 10);
