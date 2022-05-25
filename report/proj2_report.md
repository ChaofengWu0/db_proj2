# 							Database project1 report

#### contributers: 裴梦远 12011312  程博 12011310   		lab:2

#### contribution percentage: 50% per person







## APIs

##### Basic APIs:

1) insert(),delete(),update(),select(). What you need to input has been written clearly in the program command lines. And please strictly follow the rules or you won't get the result you want. 
   1) insert(): You can insert every legal data into the database
   2) delete(): You can delete every legal data from the database
   3) update(): You can update every legal data in the database
   4) select(): You can select any data like datagrip in the database

2) stockIn(): insert data into database from the given file "in_stoke_test.csv"
3) placeOrder(): insert data into database from the given file "task2_test_data_final_public.tsv"
4) 
5) 
6) 
7) 
8) 
9) 
10) getFavoriteProductModel() : input nothing, and it will return the models with the highest sold quantity, and the number of sales
11) getAvgStockByCenter() : input nothing, and it will return the average quantity of the remaining product models ordered by the name of the supply centers and rounded to one decimal place.
12) getProductByNumber() : input product_number, and it will return the current inventory capacity of each product model in each supply center.
13) 

##### advanced APIs: (It could be in the Advanced parts)







# Advanced

##### connection pools: 

I use and polish the code in the class from the teacher. And I design a scene where the store has only 5 products,while there are 10 people want to but them. I want to know which of them can get the product.

###### And it shows like this.

![image-20220525115742285](C:\Users\ll\AppData\Roaming\Typora\typora-user-images\image-20220525115742285.png)





----------------------------------------------------------------你添加你的部分吧---------------------------------------------------









##### privilege,procedure, index, view:

###### privilege and view :  

Firstly, views are created. Since I want some users only to get partial information about some tables, I create views. 

I create some users with different privileges. And the brief introduction is here.

1. manager: It can insert delete update and select information about the table staff. Since I need one to particularly manage the staff.
2. supplier:  It can only insert data into the table store since it's created to supply the company with products.
3. visitor: It can select the enterprise table, since it should know something about the companies cooperating with us which won't reveal secrets from them. Also, it can know something about our store, but limited to the product_name ,center and purchase_price and they should lower than 500.
4. vip: It can select the enterprise table, since it should know something about the companies cooperating with us which won't reveal secrets from them. Also, it can know something about our store, but limited to the product_name ,center and purchase_price.
5. salesman: It It can insert and update the table order_table, since they should sale products to client. Also they could have a view about the store table, but they can know the product_name, center, purchase_price and quantity.

###### index:

I create many index about different tables on different columns.

For example, I may need to select data from order_table by the salesman_number. Since the operation may be very  frequent, I create the index on it which truly bring me much improvement on speed (increase by 64.17%).  

###### procedure:

I create procedures to simplify the code and it brings much convenience.

Fot example, before I create the procedure, I need to code two rows but now I only need one row. Apart from this, the whole project becomes more modular and easier to debug.



