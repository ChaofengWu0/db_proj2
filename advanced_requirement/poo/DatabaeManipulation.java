package dao;

import Util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseManipulation {
    private Connection con;
    private PreparedStatement statement;
    private DatabaseUtil util;

    public DatabaseManipulation(DatabaseUtil util) {
        this.util = util;
    }

    public void getConnection() {
        con = this.util.getConnection();
//        System.out.println("------Thread " + Thread.currentThread().getId() + " visiting DB!------");
        System.out.println(this.util.getConnectState());
    }

    public void closeConnection() {
        this.util.closeConnection(con, statement);
//        System.out.println("------Thread " + Thread.currentThread().getId() + " close DB!------");
    }


    private void getAllStaffCount() throws SQLException {
        PreparedStatement statement1 = null;
        statement1 = con.prepareStatement("select type,count(*)\n" +
                "from staff\n" +
                "group by type");
        ResultSet staffType = statement1.executeQuery();
        staffType.next();//跳过列名的第一排
        System.out.println("Q6");
        while (staffType.getRow() != 0) {
            String type = staffType.getString("type");
            int count = staffType.getInt("count");
            System.out.printf("%-40s%-10d\n", type, count);
            staffType.next();
        }
//        con.commit();
        if (statement1 != null) statement1.close();
    }

    private void getContractCount() throws SQLException {
        PreparedStatement statement1 = null;
        statement1 = con.prepareStatement("select count(*)from (select distinct contract_number\n" +
                "from contract)temp");
        ResultSet countSet = statement1.executeQuery();
        countSet.next();//跳过列名的第一排
        System.out.print("Q7 ");
        System.out.println(countSet.getString("count"));
//        con.commit();
        if (statement1 != null) statement1.close();
    }

    private void getOrderCount() throws SQLException {
        PreparedStatement statement1 = null;
        statement1 = con.prepareStatement("select count(*)from (select distinct id\n" +
                "from order_table)temp");
        ResultSet countSet = statement1.executeQuery();
        countSet.next();//跳过列名的第一排
        System.out.print("Q8 ");
        System.out.println(countSet.getString("count"));

//        con.commit();
        if (statement1 != null) statement1.close();
    }

    private void getNeverSoldProductCount() throws SQLException {
        PreparedStatement statement1 = null;
        statement1 = con.prepareStatement("select count(*)\n" +
                "from\n" +
                "(select distinct product_model\n" +
                "from store\n" +
                "where product_model not in\n" +
                "      (select distinct product_model as ordered_product\n" +
                "       from order_table))temp;");
        ResultSet countSet = statement1.executeQuery();
        countSet.next();//跳过列名的第一排
        System.out.print("Q9 ");
        System.out.println(countSet.getString("count"));

//        con.commit();
        if (statement1 != null) statement1.close();
    }

    private void getFavoriteProductModel() throws SQLException {
        PreparedStatement statement = null;
        statement = con.prepareStatement("select distinct ot.product_model, sum(quantity) over () as quantity\n" +
                "from (\n" +
                "         select product_model\n" +
                "         from (\n" +
                "                  select*, max(quantity) over () as max\n" +
                "                  from order_table) sub_table1\n" +
                "         where quantity = max) sub_table2\n" +
                "         join order_table ot on ot.product_model = sub_table2.product_model\n" +
                "where ot.product_model = sub_table2.product_model;");
        ResultSet result = statement.executeQuery();
        System.out.println("Q10");
        result.next();
        while (result.getRow() != 0) {
            String product_model = result.getString("product_model");
            int quantity = result.getInt("quantity");
            System.out.printf("%-40s%-10d\n", product_model, quantity);
            result.next();
        }
        if (statement != null)
            statement.close();
    }

    private void getAvgStockByCenter() throws SQLException {
        PreparedStatement statement = null;
        statement = con.prepareStatement("select distinct center, round(1.0 * avg(quantity) over (partition by center), 1) as quantity\n" +
                "from store\n" +
                "order by center;");
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        System.out.println("Q11");
        while (resultSet.getRow() != 0) {
            String center = resultSet.getString("center");
            double quantity = resultSet.getDouble("quantity");
            System.out.printf("%-50s %-10.1f\n", center, quantity);
            resultSet.next();
        }


        if (statement != null)
            statement.close();
    }

    private void getProductByNumber(String number) throws SQLException {
        PreparedStatement statement = null;
        statement = con.prepareStatement("select center, m.product_model," +
                " purchase_price, quantity" +
                "\n" +
                "from (\n" +
                "         select product_model\n" +
                "         from product\n" +
                "                  join model m on product.product_code = m.product_code\n" +
                "         where m.product_code = ?) sub_table\n" +
                "         join model m on m.product_model = sub_table.product_model\n" +
                "         join store s on m.product_model = s.product_model\n" +
                ";");
        statement.setString(1, number);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        String center, product_model;
        int purchase_price, quantity;
        String a = "center";
        String b = "product_model";
        String c = "purchase_price";
        String d = "quantity";
        System.out.println("Q12");
        System.out.printf("%-20s %-25s %-20s %-20s\n", a, b, c, d);
        while (resultSet.getRow() != 0) {
            center = resultSet.getString("center");
            product_model = resultSet.getString("product_model");
            purchase_price = resultSet.getInt("purchase_price");
            quantity = resultSet.getInt("quantity");
            resultSet.next();
            System.out.printf("%-20s %-25s %-20d %-20d\n", center, product_model, purchase_price, quantity);
        }

        if (statement != null)
            statement.close();
    }

    private void getContractInfo(String contract_number) throws SQLException {
        PreparedStatement statement1 = null;
        PreparedStatement statement2 = null;
        statement1 = con.prepareStatement("select contract_number,c.enterprise,e.center,s.name\n" +
                "from contract c\n" +
                "join enterprise e\n" +
                "on c.enterprise=e.enterprise\n" +
                "join staff s\n" +
                "on c.contract_manager=s.staff\n" +
                "where contract_number=?");
        statement1.setString(1, contract_number);
        ResultSet resultSet1 = statement1.executeQuery();
        resultSet1.next();//跳过列名行
        System.out.println("Q13");
        while (resultSet1.getRow() != 0) {
            String a1 = resultSet1.getString("contract_number");
            String b1 = resultSet1.getString("enterprise");
            String c1 = resultSet1.getString("name");
            String d1 = resultSet1.getString("center");
            System.out.printf("contract_number:%s\n", a1);
            System.out.printf("enterprise:%s\n", b1);
            System.out.printf("manager:%s\n", c1);
            System.out.printf("supply_center:%s\n", d1);
            resultSet1.next();//往下走一排
        }

        statement2 = con.prepareStatement("select o.product_model,s.name,quantity,m.unit_price,o.estimated_date,o.lodgement_date\n" +
                "from order_table o\n" +
                "join model m\n" +
                "on o.product_model=m.product_model\n" +
                "join staff s\n" +
                "on o.salesman_number=s.staff\n" +
                "where contract_number=?");
        statement2.setString(1, contract_number);
        ResultSet resultSet2 = statement2.executeQuery();
        resultSet2.next();


        String product_model, salesman, estimate_delivery_date, lodgement_date;
        int quantity, unit_price;
        int temp_cnt = 0;
        while (resultSet2.getRow() != 0) {
            if (temp_cnt == 0) {
                System.out.printf("%-30s %-25s %-15s %-5s %-5s %-10s\n", "product_model", "salesman", "quantity", "unit_price", "estimate_delivery_date", "lodgement_date");
                temp_cnt++;
            }
            product_model = resultSet2.getString("product_model");
            salesman = resultSet2.getString("name");
            quantity = resultSet2.getInt("quantity");
            unit_price = resultSet2.getInt("unit_price");
            estimate_delivery_date = resultSet2.getString("estimated_date");
            lodgement_date = resultSet2.getString("lodgement_date");
            System.out.printf("%-30s %-25s %-15d %-5d %-5s %-10s\n", product_model, salesman, quantity, unit_price, estimate_delivery_date, lodgement_date);
            resultSet2.next();
        }

        if (statement != null)
            statement.close();
        if (statement2 != null)
            statement2.close();
    }

    public void getAns(int finalI, String q12, ArrayList<String> q13, int[] cnt) throws SQLException {
        switch (finalI) {
            case (1): {
                getAllStaffCount();
                break;
            }
            case (2): {
                getContractCount();
                break;
            }
            case (3): {
                getOrderCount();
                break;
            }
            case (4): {
                getNeverSoldProductCount();
                break;
            }
            case (5): {
                getFavoriteProductModel();
                break;
            }
            case (6): {
                getAvgStockByCenter();
                break;
            }
            case (7): {
                getProductByNumber(q12);
                break;
            }
            default: {
                getContractInfo(q13.get(cnt[0]++));
                break;
            }
        }
    }


//    private static void poolRequest(int count, String Q12, ArrayList<String> Q13) {
//        int cnt = 0;
//        for (int i = 0; i < count; i++) {
//            int finalI = i;
//            new Thread(() -> {
//                DatabaseManipulation dm = new DatabaseManipulation(DatabaseUtil.getInstance());
//                dm.getConnection();
//                try {
//                    dm.getAns(finalI, Q12, Q13, cnt);
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//                dm.closeConnection();
//            }
//            ).start();
//        }
//    }

}
