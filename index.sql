-- 索引，我可能需要在订单表中，查询某个员工谈成的事，这样会加速处理，
-- 有索引则是8.29  无索引则是13.61，可以看到提速了64.17%

create index on order_table (salesman_number);

drop index order_table_salesman_number_idx;

explain
select *
from order_table
where salesman_number = '12011310';



-- 我可能需要在订单表中，利用contract_number查出contract的所有信息
-- 无索引是13.61   有索引是10.66 提速27.67%
create index on order_table (contract_number);
-- create index on contract(contract_number);
drop index order_table_contract_number_idx;
-- drop index contract_contract_number_idx;

explain
select *
from order_table
         join contract c on c.contract_number = order_table.contract_number
where order_table.contract_number = 'CSE0000101';


-- 我可能需要在store表中查询某个员工干的事
-- 无索引: 20.20  有索引:8.29  提速: 143.67%
create index on store (supply_staff);

drop index store_supply_staff_idx;


explain
select *
from store
where supply_staff = '12011310';

-- 可能需要对某个员工进行查找
-- 无索引:28.50  有索引:16.59  提速71.79%
explain
select *
from store
         join staff s on s.staff = store.supply_staff;
-- where staff = '12011310';

