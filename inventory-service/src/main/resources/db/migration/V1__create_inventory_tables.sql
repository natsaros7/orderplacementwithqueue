create table inventory_item (
    product_code text    primary key,
    stock        integer not null check (stock >= 0),
    version      bigint  not null default 0
);

create table inventory_reservation (
    id           uuid        primary key,
    order_id     uuid        not null,
    product_code text        not null,
    quantity     integer     not null check (quantity > 0),
    status       varchar(20) not null,
    decided_at   timestamptz not null,
    constraint uq_inventory_reservation_order_id unique (order_id)
);

create index idx_inventory_reservation_product_code on inventory_reservation (product_code);
