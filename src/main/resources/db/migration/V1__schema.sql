create table customers
(
    id          uuid primary key,
    external_id bigint not null unique,
    name        text   not null
);

/**
  Can contain duplicate asset entries.
 */
create table assets
(
    id          uuid primary key,
    customer_id uuid   not null references customers (id) on delete cascade,
    name        text   not null,
    type        text   not null,
    external_id bigint not null,
    value       bigint not null
);



