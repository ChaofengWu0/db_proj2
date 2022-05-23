--还有一个procedure在实现修改时间的时候用的

create or replace procedure add_product_model(product_code_ varchar, product_name_ varchar, product_model_ varchar,
                                              unit_price_ integer)
as
$$
begin
    insert into product(product_code, product_name) values (product_code_, product_name_);
    insert into model(product_code, product_model, unit_price) values (product_code_, product_model_, unit_price_);
end
$$ language plpgsql;

-- drop procedure add_product_model;

-- 这个只要center和type符合条件，就可以加
create or replace procedure add_store(product_code_ varchar, product_model_ varchar, product_name_ varchar,
                                      unit_price_ integer, supply_staff_ varchar, name_ varchar, age_ integer,
                                      gender_ varchar, center_ varchar, mobile_phone_ varchar, type_ varchar,
                                      date_ varchar, purchase_price_ integer, quantity_ integer)
as
$$
begin
    if (type_ <> 'Supply Staff') then
        raise exception E'WRONG! SINCE THE TYPE IS NOT \'SUPPLY STAFF\'';
    end if;
    if (not exists(select center from center where center.center = center_)) then
        raise exception E'WRONG! SINCE THE CENTER IS NOT INCLUDED';
    end if;
    call add_product_model(product_code_, product_name_, product_model_, unit_price_);
    insert into staff (name, age, gender, staff, center, mobile_number, type)
    values (name_, age_, gender_, supply_staff_, center_, mobile_phone_, type_);
    insert into store(center, product_model, supply_staff, date, purchase_price, quantity)
    values (center_, product_model_, supply_staff_, date_, purchase_price_, quantity_);
end
$$ language plpgsql;
