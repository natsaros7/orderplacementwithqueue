-- Guard against a PK violation on restart against a persistent (non-in-memory) database: plain
-- INSERTs would abort startup the second time this runs.
insert into inventory_item (product_code, stock, version)
select 'WIDGET', 50, 0 where not exists (select 1 from inventory_item where product_code = 'WIDGET');
insert into inventory_item (product_code, stock, version)
select 'GADGET', 25, 0 where not exists (select 1 from inventory_item where product_code = 'GADGET');
insert into inventory_item (product_code, stock, version)
select 'GIZMO', 10, 0 where not exists (select 1 from inventory_item where product_code = 'GIZMO');
