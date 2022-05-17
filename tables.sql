create table center
(
    center   varchar
        primary key,
    director varchar
);

create table staff
(
    name          varchar,
    age           integer,
    gender        varchar,
    staff         char(8),
    center        varchar
        references center (center) on delete cascade on update cascade,
    mobile_number varchar(11),
    type          varchar,
    primary key (staff)
);

create table product
(
    product_code varchar
        primary key,
    product_name varchar
);

create table model
(
    product_code  varchar
        references product (product_code) on delete cascade on update cascade,
    product_model varchar
        primary key,
    unit_price    integer
);

create table enterprise
(
    enterprise varchar
        primary key,
    country    varchar,
    city       varchar,
    center     varchar references center (center) on delete cascade on update cascade,
    industry   varchar
);

create table contract
(
    contract_number  char(10)
        primary key,
    enterprise       varchar references enterprise (enterprise) on delete cascade on update cascade,
    contract_manager char(8),
    contract_date    varchar(10),
    contract_type    varchar(10)
);

create table store
(
    center         varchar,
    product_model  varchar references model (product_model) on delete cascade on update cascade,
    supply_staff   char(8) references staff (staff) on delete cascade on update cascade,
    date           varchar,
    purchase_price integer,
    quantity       integer,
    primary key (center, product_model)
);

create table order_table
(
    id              serial primary key,
    contract_number char(10) references contract (contract_number) on delete cascade on update cascade,
    product_model   varchar,
    quantity        integer,
    estimated_date  varchar(10),
    lodgement_date  varchar(10),
    salesman_number char(8) references staff (staff) on delete cascade on update cascade
);
