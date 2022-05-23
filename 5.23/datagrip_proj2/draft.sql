select center
from enterprise
where enterprise = (select enterprise from contract where contract_number = 'CSE0000208');

update order_table
set quantity=1,
    estimated_date='2022-02-28',
    lodgement_date='2022-02-13'
where id = 74;

insert into order_table(id, contract_number, product_model, quantity, estimated_date, lodgement_date, salesman_number)
values (186, 'CSE0000328', 'akkkkk', 1, '2022-04-03', '2022-04-02', '11710110');

insert into order_table(id, contract_number, product_model, quantity, estimated_date, lodgement_date, salesman_number)
values (187, 'CSE0000328', 'bkkkkk', 1, '2022-04-03', '2022-04-02', '11710110');

select *, rank() over (partition by contract_number,salesman_number order by estimated_date desc ,product_model)
from order_table;

select id
from (select *, rank() over (partition by contract_number,salesman_number order by estimated_date desc ,product_model) r
      from order_table) rank
where r = 2;

select type, count(*)
from staff
group by type;

select count(*)
from (select distinct contract_number
      from contract) temp;

select count(*)
from (select distinct id
      from order_table) temp;

select count(*)
from
(select distinct product_model--不可以用!=
from store
where product_model not in
      (select distinct product_model as ordered_product
       from order_table))temp;

select contract_number,c.enterprise,e.center,s.name
from contract c
join enterprise e
on c.enterprise=e.enterprise
join staff s
on c.contract_manager=s.staff
where contract_number='CSE0000306';

select o.product_model,s.name,quantity,m.unit_price,o.estimated_date,o.lodgement_date
from order_table o
join model m
on o.product_model=m.product_model
join staff s
on o.salesman_number=s.staff
where contract_number='CSE0000209';

create or replace procedure update_contract_type(cur_contract varchar)
as $$
    begin
        update contract set contract_type='unfinished' where contract_number=cur_contract;
    end;
    $$ language plpgsql;

call update_contract_type('CSE0000103');

--先通过enterprise找到该enterprise参与的contract，再通过每一个contract去找每一个order，并返回order中的model，quantity，然后找到对应的价格（目前还是用的unit_price）
select *
from contract
where enterprise='Bilibili';

--接下来，遍历contract
select *,sum(cost) over () as sum
from
(
select o.product_model as model,m.unit_price as price,o.quantity as quantity
     ,m.unit_price*o.quantity as cost
from order_table o
join model m on o.product_model = m.product_model
where o.contract_number='CSE0000111'
)temp;

--先遍历center，对于每一个center，去找在center订货的企业，遍历企业同时打印企业中的数据
select *
from enterprise where center='America';

select *
from staff where staff.staff='12211522';

select s.product_model as model,s.purchase_price as purchase,m.unit_price as unitprice,s.purchase_price-m.unit_price as difference,s.quantity as quantity
from store s
join model m on s.product_model = m.product_model
where supply_staff='11210906';


------
select o.product_model,m.unit_price,o.quantity,m.unit_price*o.quantity as cost
from order_table o
join model m on o.product_model = m.product_model
where salesman_number='12110429';


------
select contract_number,enterprise,contract_type
from contract
where contract_manager='11410907';

