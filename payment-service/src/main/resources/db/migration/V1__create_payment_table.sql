create table payment (
    id           uuid        primary key,
    order_id     uuid        not null,
    product_code text        not null,
    quantity     integer     not null check (quantity > 0),
    status       varchar(20) not null,
    processed_at timestamptz not null,
    constraint uq_payment_order_id unique (order_id)
);
