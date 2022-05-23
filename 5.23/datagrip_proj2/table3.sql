create  table center
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

create table advanced_store
(
    name     varchar(100) primary key,
    quantity integer not null
);



-- 设计情景：
-- 用户权限以及视图的结合使用
-- 一个USER manager 可以增删查改staff,需要一个人来专门管理员工，他自然可以对员工表拥有所有的权限

-- 一个USER supplier，只可以增store里面的数据，这是一个供应商，那么他只能向库存里面添加货物，不能对库存进行其他的操作

-- 一个USER visitor，只可以查enterprise表,这是一个访客用户，他只能对enterprise进行查操作来了解我们公司的合作伙伴，他也可以查看我们公司
-- 的store的商品内容，查看价值在500以下的产品,都只能看商品名称,中心和销售价

-- 一个USER vip 只可以查enterprise表,这是一个访客用户，他只能对enterprise进行查操作来了解我们公司的合作伙伴，他也可以查看我们公司
-- 的store的商品内容，查看产品只能看商品名称，中心和销售价

-- 一个USER salesman,可以增改order_table，这是销售人员的账户，他只能增加订单，修改订单，不能做其他的事情，还可以有一个对store的视图，可以看
-- 商品名称，中心，销售价和剩余量
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
grant select on visitor_store to visitor;-------------------------------------


create role vip with
    login
    nosuperuser
    noreplication
    password '123456';
COMMENT ON ROLE vip is 'It has select permission on enterprise';
grant select on enterprise to vip;
grant select on vip_store to vip;----------------------------------------

create role salesman with
    login
    nosuperuser
    noreplication
    password '123456';
COMMENT ON ROLE salesman is 'It has insert and update permissions on order_table';
grant insert, update on order_table to salesman;
grant select on salesman_store to salesman;-------------------------------



create table advanced_store
(
    name     varchar(100) primary key,
    quantity integer not null
);

delete from order_table where quantity=4;

revoke all on order_table from supplier;
select *
from order_table;