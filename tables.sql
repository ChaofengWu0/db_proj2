drop table center,staff,model,product,enterprise,contract,store,order_table;

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



-- create table test
-- (
--     test integer primary key
-- );
--
-- insert into test (test)
-- values (1);
--
-- create table test2
-- (
--     id   serial primary key,
--     test integer references test (test) on delete cascade
-- );
--
-- insert into test2(test)
-- values (1);


-- -- Q10
-- select distinct ot.product_model, sum(quantity) over () as quantity
-- from (
--          select product_model
--          from (
--                   select*, max(quantity) over () as max
--                   from order_table) sub_table1
--          where quantity = max) sub_table2
--          join order_table ot on ot.product_model = sub_table2.product_model
-- where ot.product_model = sub_table2.product_model;
--
-- -- Q11
-- select distinct center, round(1.0 * avg(quantity) over (partition by center), 1) as quantity
-- from store
-- order by center;
--
-- select *, count(*) over (partition by center)
-- from store;
--
--
-- -- Q12
-- select center, m.product_model, purchase_price, quantity
-- from (
--          select product_model
--          from product
--                   join model m on product.product_code = m.product_code
--          where m.product_code = 'A50L172') sub_table
--          join model m on m.product_model = sub_table.product_model
--          join store s on m.product_model = s.product_model
-- ;
--
-- with q as (
--     select count(*) cnt, product_model
--     from order_table
--     group by product_model)
-- select max as quantity, product_model
-- from (
--          select max(cnt) over () as max, *
--          from q) sub_table
-- where cnt = max
-- ;
--
--
create table advanced_store
(
    name     varchar(100) primary key,
    quantity integer not null
);

create table user_table
(
    id   integer primary key,
    name varchar(100),
    pwd  varchar(100),
    type varchar(100)
);



