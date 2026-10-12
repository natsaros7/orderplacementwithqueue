create table orders (
    id          uuid        primary key,
    product_code text       not null,
    quantity    integer     not null check (quantity > 0),
    placed_at   timestamptz not null,
    status      varchar(20) not null
);

create index idx_orders_status on orders (status);
