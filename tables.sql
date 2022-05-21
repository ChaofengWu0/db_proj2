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

-- insert into test (test)
-- values (1);


-- create table test2
-- (
--     id   serial primary key,
--     test integer references test (test) on delete cascade
-- );

-- insert into test2(test)
-- values (1);


-- Q10
-- select distinct ot.product_model, sum(quantity) over () as quantity
-- from (
--          select product_model
--          from (
--                   select*, max(quantity) over () as max
--                   from order_table) sub_table1
--          where quantity = max) sub_table2
--          join order_table ot on ot.product_model = sub_table2.product_model
-- where ot.product_model = sub_table2.product_model;


-- select *
-- from order_table
-- where product_model = 'ServerPower76';


-- select distinct product_model, sum as quantity
-- from (
--          select *, max(sum) over () as max
--          from (
--                   select *, sum(quantity) over (partition by product_model) as sum
--                   from order_table) sub_table) sub_table2
-- where sum = max;


-- Q11
-- select distinct center, round(1.0 * avg(quantity) over (partition by center), 1) as quantity
-- from store
-- order by center;


-- select *, count(*) over (partition by center)
-- from store;


-- select *
-- from store
-- where center = 'Asia';

-- select *
-- from order_table
-- where contract_number = 'CSE0000323'
-- ORDER BY estimated_date, product_model;
--
-- select *
-- from (select *, rank() over (partition by contract_number,salesman_number order by estimated_date desc ,product_model) r
--       from order_table) rank
-- where contract_number= 'CSE0000323'
-- --   and salesman_number=
--   and r= 2;


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


create table advanced_store
(
    name     varchar(100) primary key,
    quantity integer not null
);



-- 设计情景：
-- 用户权限
-- 一个USER manager 可以增删查改staff,需要一个人来专门管理员工，他自然可以对员工表拥有所有的权限

-- 一个USER supplier，只可以增store里面的数据，这是一个供应商，那么他只能向库存里面添加货物，不能对库存进行其他的操作

-- 一个USER visitor，只可以查enterprise表,这是一个访客用户，他只能对enterprise进行查操作来了解我们公司的合作伙伴，他也可以查看我们公司
-- 的store的商品内容，查看价值在500以下的产品,都只能看商品名称,中心和销售价

-- 一个USER vip 只可以查enterprise表,这是一个访客用户，他只能对enterprise进行查操作来了解我们公司的合作伙伴，他也可以查看我们公司
-- 的store的商品内容，查看产品只能看商品名称，中心和销售价

-- 一个USER salesman,可以增改order_table，这是销售人员的账户，他只能增加订单，修改订单，不能做其他的事情，还可以有一个对store的视图，可以看
-- 商品名称，中心，销售价和剩余量
create role manager with
    login
    nosuperuser
    noreplication
    password '123456';
COMMENT ON ROLE manager is 'It has all permissions on staff';
grant all on staff to manager;


create role supplier with
    login
    nosuperuser
    noreplication
    password '123456';
COMMENT ON ROLE manager is 'It has insert permission on store';
grant insert on store to supplier;


create role visitor with
    login
    nosuperuser
    noreplication
    password '123456';
COMMENT ON ROLE visitor is 'It has select permission on enterprise';
grant select on enterprise to visitor;
grant select on visitor_store to visitor;


create role vip with
    login
    nosuperuser
    noreplication
    password '123456';
COMMENT ON ROLE vip is 'It has select permission on enterprise';
grant select on enterprise to vip;
grant select on vip_store to vip;

create role salesman with
    login
    nosuperuser
    noreplication
    password '123456';
COMMENT ON ROLE salesman is 'It has insert and update permissions on order_table';
grant insert, update on order_table to salesman;
grant select on salesman_store to salesman;


create or replace view visitor_store as
select product_model, center, purchase_price
from store
where purchase_price <= 500;


create or replace view vip_store as
select product_model, center, purchase_price
from store;

create or replace view salesman_store as
select product_model, center, purchase_price, quantity
from store;

revoke all on enterprise,store,order_table,staff,vip_store,visitor_store,salesman_store from supplier,manager,visitor,vip,salesman;

DROP role manager,salesman,supplier,vip,visitor;
