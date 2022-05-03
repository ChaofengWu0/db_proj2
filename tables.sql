drop table center,staff,model,product,enterprise,contract,store,order_table;

create table center
(
    center   varchar
        primary key,
    director varchar
);

create table staff
(
    staff         char(8) primary key,
    name          varchar,
    age           integer,
    gender        varchar,
    center        varchar
        references center (center),
    mobile_number varchar(11),
    type          varchar
);

create table product
(
    product_code varchar
        primary key,
    product_name varchar
);

create table model
(
    product_model varchar
        primary key,
    unit_price    integer,
    product_code  varchar
        references product (product_code)
);

create table enterprise
(
    enterprise varchar
        primary key,
    country    varchar,
    city       varchar,
    center     varchar references center (center),
    industry   varchar
);

create table contract
(
    contract_number  char(10)
        primary key,
    enterprise       varchar references enterprise (enterprise),
    contract_manager char(8),
    contract_date    varchar(10),
    contract_type    varchar(10)
);

create table store
(
    center         varchar,
    product_model  varchar references model (product_model),
    supply_staff   char(8) references staff (staff),
    date           varchar,
    purchase_price integer,
    quantity       integer,
    primary key (center, product_model)
);

create table order_table
(
    id              serial primary key,
    contract_number char(10) references contract (contract_number),
    product_model   varchar,
    quantity        integer,
    estimated_date  varchar(10),
    lodgement_date  varchar(10),
    salesman_number char(8) references staff (staff)
);
