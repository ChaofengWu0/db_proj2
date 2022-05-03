import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.HashMap;
import java.util.Properties;

public class LoadOriginalData {

    private static final int BATCH_SIZE = 500;
    private static URL propertyURL = LoadOriginalData.class
            .getResource("/loader.cnf");

    private static Connection con = null;

    //enterprise
    private static PreparedStatement stmt0 = null;
    // staff
    private static PreparedStatement stmt1 = null;
    // center (update)
    private static PreparedStatement stmt2 = null;
    // model
    private static PreparedStatement stmt3 = null;
    // product
    private static PreparedStatement stmt4 = null;
    // center (insert)
    private static PreparedStatement stmt5 = null;

    // 用来往stock里面传数据
    private static PreparedStatement stmt6 = null;
    // 用来往order里面传数据
    private static PreparedStatement stmt7 = null;
    // 用来往contract里面传数据
    private static PreparedStatement stmt8 = null;
    // 在placeOrder方法中用来更新库存
    private static PreparedStatement stmt9 = null;


    private static Statement statement = null;

    private static boolean verbose = false;

    private static void openDB(String host, String dbname,
                               String user, String pwd) {
        try {
            //
            Class.forName("org.postgresql.Driver");
        } catch (Exception e) {
            System.err.println("Cannot find the Postgres driver. Check CLASSPATH.");
            System.exit(1);
        }
        String url = "jdbc:postgresql://" + host + "/" + dbname;
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pwd);
        try {
            // 这里是连接数据库的部分
            con = DriverManager.getConnection(url, props);
            if (verbose) {
                System.out.println("Successfully connected to the database "
                        + dbname + " as " + user);
            }
            // 这里设置成false则是让数据先写入batch，写入量到了一定程度之后再发送
            con.setAutoCommit(false);
        } catch (SQLException e) {
            System.err.println("Database connection failed");
            System.err.println(e.getMessage());
            System.exit(1);
        }

        // enterprise 0
        try {
            //导入数据
            stmt0 = con.prepareStatement("insert into enterprise(enterprise,country,city,center,industry)"
                    + " values(?,?,?,?,?)");
        } catch (SQLException e) {
            System.err.println("Insert statement failed");
            System.err.println(e.getMessage());
            closeDB();
            System.exit(1);
        }

        // staff 1
        try {
            //导入数据
            stmt1 = con.prepareStatement("insert into staff(staff,name,age,gender,center,mobile_number,type)"
                    + " values(?,?,?,?,?,?,?)");
        } catch (SQLException e) {
            System.err.println("Insert statement failed");
            System.err.println(e.getMessage());
            closeDB();
            System.exit(1);
        }

        // center update 2
        try {
            //导入数据
            stmt2 = con.prepareStatement("update center set director =? where center =?");
        } catch (SQLException e) {
            System.err.println("Insert statement failed");
            System.err.println(e.getMessage());
            closeDB();
            System.exit(1);
        }

        // model 3
        try {
            //导入数据
            stmt3 = con.prepareStatement("insert into model(product_model,unit_price,product_code)"
                    + " values(?,?,?)");
        } catch (SQLException e) {
            System.err.println("Insert statement failed");
            System.err.println(e.getMessage());
            closeDB();
            System.exit(1);
        }

        // product 4
        try {
            //导入数据
            stmt4 = con.prepareStatement("insert into product(product_code,product_name)"
                    + " values(?,?)");
        } catch (SQLException e) {
            System.err.println("Insert statement failed");
            System.err.println(e.getMessage());
            closeDB();
            System.exit(1);
        }

        // center 5
        try {
            //导入数据
            stmt5 = con.prepareStatement("insert into center(center,director)"
                    + " values(?,?)");
        } catch (SQLException e) {
            System.err.println("Insert statement failed");
            System.err.println(e.getMessage());
            closeDB();
            System.exit(1);
        }

        try {
            stmt6 = con.prepareStatement("insert into store(center,product_model,supply_staff,date,purchase_price,quantity) values" +
                    "(?,?,?,?,?,?)");
        } catch (SQLException e) {
            System.err.println("Insert statement failed");
            System.err.println(e.getMessage());
            closeDB();
            System.exit(1);
        }

        try {
            stmt7 = con.prepareStatement("insert into order_table(contract_number,product_model,quantity,estimated_date,lodgement_date" +
                    ",salesman_number) values(?,?,?,?,?,?)");
        } catch (SQLException e) {
            System.err.println("Insert statement failed");
            System.err.println(e.getMessage());
            closeDB();
            System.exit(1);
        }

        try {
            stmt8 = con.prepareStatement("insert into contract(contract_number,enterprise,contract_manager,contract_date,contract_type) " +
                    "values(?,?,?,?,?)");
        } catch (SQLException e) {
            System.err.println("Insert statement failed");
            System.err.println(e.getMessage());
            closeDB();
            System.exit(1);
        }

        try {
            stmt9 = con.prepareStatement("update store set quantity = quantity + ? where center = ? and product_model = ? ;" +
                    "update store set date = ? where center = ? and product_model = ?");
        } catch (SQLException e) {
            System.err.println("Insert statement failed");
            System.err.println(e.getMessage());
            closeDB();
            System.exit(1);
        }

    }


    private static void closeDB() {
        if (con != null) {
            try {

                if (stmt0 != null) stmt0.close();

                if (stmt1 != null) stmt1.close();

                if (stmt2 != null) stmt2.close();

                if (stmt3 != null) stmt3.close();

                if (stmt4 != null) stmt4.close();

                if (stmt5 != null) stmt5.close();

                if (stmt6 != null) stmt6.close();

                if (stmt7 != null) stmt7.close();

                con.close();
                con = null;
            } catch (Exception e) {
                // Forget about it
            }
        }
    }

    public static void main(String[] args) throws SQLException, IOException {
        if (propertyURL == null) {
            System.err.println("No configuration file (loader.cnf) found");
            System.exit(1);
        }
        Properties defprop = new Properties();
        defprop.put("host", "localhost");
        defprop.put("user", "u99999999");
        defprop.put("password", "99999999");
        defprop.put("database", "postgres");
        Properties prop = new Properties(defprop);
        try (BufferedReader conf
                     = new BufferedReader(new FileReader(propertyURL.getPath()))) {
            prop.load(conf);
        } catch (IOException e) {
            // Ignore
            System.err.println("No configuration file (loader.cnf) found");
        }

        // Empty target table
        openDB(prop.getProperty("host"), prop.getProperty("database"),
                prop.getProperty("user"), prop.getProperty("password"));
        if (con != null) {
            statement = con.createStatement();
            statement.execute("truncate table center,staff,enterprise " +
                    ",product,model,order_table,contract,store cascade");
            con.commit();
            statement.close();
        }
        closeDB();

        openDB(prop.getProperty("host"), prop.getProperty("database"),
                prop.getProperty("user"), prop.getProperty("password"));

        load_original_data();
        stockIn();
        placeOrder();


        closeDB();
    }


    private static void placeOrder() throws IOException, SQLException {
        PreparedStatement statement1 = null;
        PreparedStatement statement2 = null;
        String address_for_place_order = "C:\\Users\\ll\\Desktop\\University\\dataBase\\proj\\db_proj2\\src\\task2_test_data_publish.csv";
        BufferedReader br = Files.newBufferedReader(Paths.get(address_for_place_order));
        br.readLine();

        String contract_num, enterprise, product_model, contract_manger, contract_date,
                estimated_delivery_date, lodgement_date, salesman_num, contract_type;
        int quantity;

        int store_quantity;
        String now_center, now_type;

        HashMap<String, Integer> map_for_table_con = new HashMap<>();

        String line;
        String[] parts;
        while ((line = br.readLine()) != null) {
            parts = line.split(",");
            contract_num = parts[0];
            enterprise = parts[1];
            product_model = parts[2];
            quantity = Integer.parseInt(parts[3]);
            contract_manger = parts[4];
            contract_date = parts[5];
            estimated_delivery_date = parts[6];
            lodgement_date = parts[7];
            salesman_num = parts[8];
            contract_type = parts[9];


            // 第一个检查，查订单中要求的数量和库存的数量
            // 先利用statement1从enterprise中查到supply_center在哪
            statement1 = con.prepareStatement("select center from enterprise where enterprise = ?");
            statement1.setString(1, enterprise);
            ResultSet check1_a = statement1.executeQuery();
            check1_a.next();
            if (check1_a.getRow() == 0) continue;
            now_center = check1_a.getString("center");

            statement2 = con.prepareStatement("select quantity from store where center = ? and product_model = ?");
            statement2.setString(1, now_center);
            statement2.setString(2, product_model);
            ResultSet check1_b = statement2.executeQuery();
            check1_b.next();
            if (check1_b.getRow() == 0) continue;
            store_quantity = check1_b.getInt("quantity");
            if (store_quantity < quantity) continue;


            // 第二个检查,查人员类型是不是salesman
            statement1 = con.prepareStatement("select type from staff where staff = ?");
            statement1.setString(1, salesman_num);
            ResultSet check2 = statement1.executeQuery();
            check2.next();
            if (check2.getRow() == 0) continue;
            now_type = check2.getString("type");
            if (!now_type.equals("Salesman")) continue;

            loadData_for_contract(contract_num, enterprise, contract_manger, contract_date, contract_type, map_for_table_con);
            loadData_for_order(contract_num, product_model, quantity, estimated_delivery_date, lodgement_date, salesman_num);
            update_data_for_store(-quantity, now_center, product_model, lodgement_date);
        }
        con.commit();
        stmt8.close();
        stmt7.close();
        if (statement1 != null)
            statement1.close();
        if (statement2 != null)
            statement2.close();
    }

    // 目前采取的办法是把date作为主键的参考
    private static void stockIn() throws IOException, SQLException {
        PreparedStatement statement = null;
        String address_for_stock = "C:\\Users\\ll\\Desktop\\University\\dataBase\\proj\\db_proj2\\src\\task1_in_stoke_test_data_publish.csv";
        BufferedReader br = Files.newBufferedReader(Paths.get(address_for_stock));
        br.readLine();

        String[] parts;
        String line;
        String center;
        String product_model;
        String staff;
        String date;
        int price;
        int quantity;
        while ((line = br.readLine()) != null) {
            parts = line.split(",");
            if (parts.length == 8) {
                center = parts[1] + "," + parts[2];
                product_model = parts[3];
                staff = parts[4];
                date = parts[5];
                price = Integer.parseInt(parts[6]);
                quantity = Integer.parseInt(parts[7]);
            } else {
                center = parts[1];
                product_model = parts[2];
                staff = parts[3];
                date = parts[4];
                price = Integer.parseInt(parts[5]);
                quantity = Integer.parseInt(parts[6]);
            }
            // 第一个检查和第三个检查，供应中心与人员所在的供应中心对不上， 供应中心不存在
            statement = con.prepareStatement("select center from staff where staff = ?");
            statement.setString(1, staff);
            ResultSet check1 = statement.executeQuery();
            check1.next();
            // 供应中心不存在
            if (check1.getRow() == 0) continue;
            String check_center = check1.getString("center");
            // 供应中心与人员所在供应中心对不上
            if (!check_center.equals(center)) continue;

            // 第二个检查和第五个检查，人员类型不是"supply_staff", 人员不存在
            statement = con.prepareStatement("select type from staff where staff = ?");
            statement.setString(1, staff);
            ResultSet check2 = statement.executeQuery();
            check2.next();
            // 人员不存在
            if (check2.getRow() == 0) continue;
            String check_type = check2.getString("type");
            if (!check_type.equals("Supply Staff")) continue;

            // 第四个检查, 产品不存在
            statement = con.prepareStatement("select product_model from model where product_model = ?");
            statement.setString(1, product_model);
            ResultSet check3 = statement.executeQuery();
            check3.next();
            // 产品不存在
            if (check3.getRow() == 0) continue;

            // 接下来是其他的条件判断

            // 如果supply_center和model同时在这张表内那就只增加库存的数量就可以了
            // 先不合并试试,事实证明需要合并
            statement = con.prepareStatement("select quantity from store where center = ? and product_model = ?");
            statement.setString(1, center);
            statement.setString(2, product_model);
            ResultSet check4 = statement.executeQuery();
            check4.next();
            if (check4.getRow() == 0) {
                load_data_for_store(center, product_model, staff, date, price, quantity);
            } else {
                update_data_for_store(quantity, center, product_model, date);
            }
        }
        con.commit();
        stmt6.close();
        if (statement != null)
            statement.close();
    }


    private static void load_original_data() throws SQLException {
        // 整个导入过程
        try {
            String address1 = "C:\\Users\\ll\\Desktop\\University\\dataBase\\proj\\db_proj2\\src\\center.csv";
            String address2 = "C:\\Users\\ll\\Desktop\\University\\dataBase\\proj\\db_proj2\\src\\staff.csv";
            String address3 = "C:\\Users\\ll\\Desktop\\University\\dataBase\\proj\\db_proj2\\src\\model.csv";
            String address4 = "C:\\Users\\ll\\Desktop\\University\\dataBase\\proj\\db_proj2\\src\\enterprise.csv";

            String[] parts1;
            String[] parts2;
            String[] parts3;
            String[] parts4;

            String enterprise;
            String center;
            String country;
            String city;
            String industry;
            String product_code;
            String product_name;
            String product_model;
            String staff;
            String name;
            int age;
            String gender;
            String mobile_number;
            String type;
            int unit_price;


            HashMap<String, Integer> map_for_table_model = new HashMap<>();
            HashMap<String, Integer> map_for_table_pro = new HashMap<>();
            int cnt = 0;
//            String[] titles;
            BufferedReader br = Files.newBufferedReader(Paths.get(address1));
            br.readLine();
            String line;

// table1
            while ((line = br.readLine()) != null) {
                parts1 = line.split(",");
                if (parts1.length == 2) {
                    center = parts1[1];
                    loadData_for_supply_center(center);
                } else {
                    center = parts1[1] + "," + parts1[2];
                    loadData_for_supply_center(center);
                }
            }


// table2
            br = Files.newBufferedReader(Paths.get(address2));
            br.readLine();
            while ((line = br.readLine()) != null) {
                parts2 = line.split(",");
                if (parts2.length > 1) {
                    cnt++;
                    name = parts2[1];
                    age = Integer.parseInt(parts2[2]);
                    gender = parts2[3];
                    staff = parts2[4];

                    if (parts2.length == 8) {
                        center = parts2[5];
                        mobile_number = parts2[6];
                        type = parts2[7];
                    } else {
                        center = parts2[5] + "," + parts2[6];
                        mobile_number = parts2[7];
                        type = parts2[8];
                    }
                    loadData_for_staff(staff, name, age, gender, center, mobile_number, type);
                    if (type.equals("Director")) {
                        update_data_for_center(center, name);
                    }

                    if (cnt % BATCH_SIZE == 0) {
                        stmt1.executeBatch();
//                        stmt2.executeBatch();
                        stmt1.clearBatch();
//                        stmt2.clearBatch();
                    }
                }
            }
            if (cnt % BATCH_SIZE != 0) {
                stmt1.executeBatch();
//                stmt2.executeBatch();
                cnt = 0;
            }


// table3
            br = Files.newBufferedReader(Paths.get(address3));
            br.readLine();
            while ((line = br.readLine()) != null) {
                parts3 = line.split(",");
                if (parts3.length > 1) {
                    cnt++;
                    product_code = parts3[1];
                    product_model = parts3[2];
                    product_name = parts3[3];
                    unit_price = Integer.parseInt(parts3[4]);
                    loadData_for_product(product_code, product_name, map_for_table_pro);
                    loadData_for_model(product_model, unit_price, product_code, map_for_table_model);
                    if (cnt % BATCH_SIZE == 0) {
                        stmt4.executeBatch();
                        stmt3.executeBatch();
                        stmt4.clearBatch();
                        stmt3.clearBatch();
                    }
                }
            }
            if (cnt % BATCH_SIZE != 0) {
                stmt4.executeBatch();
                stmt3.executeBatch();
                cnt = 0;
            }


// table4
            br = Files.newBufferedReader(Paths.get(address4));
            br.readLine();
            while ((line = br.readLine()) != null) {
                parts4 = line.split(",");
                if (parts4.length > 1) {
                    cnt++;
                    enterprise = parts4[1];
                    country = parts4[2];
                    city = parts4[3];
                    if (parts4.length == 6) {
                        center = parts4[4];
                        industry = parts4[5];
                    } else {
                        center = parts4[4] + "," + parts4[5];
                        industry = parts4[6];
                    }
                    loadData_for_client_enterprise(enterprise, country, city, center, industry);

                    if (cnt % BATCH_SIZE == 0) {
                        stmt0.executeBatch();
                        stmt0.clearBatch();
                    }
                }
            }
            if (cnt % BATCH_SIZE != 0) {
                stmt0.executeBatch();
                cnt = 0;
            }


            con.commit();
            stmt0.close();
        } catch (SQLException se) {
            System.err.println("SQL error: " + se.getMessage());
            try {
                con.rollback();
                stmt0.close();
                stmt1.close();
                stmt3.close();
                stmt4.close();
                stmt5.close();
            } catch (Exception e2) {
            }
            closeDB();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Fatal error: " + e.getMessage());
            try {
                con.rollback();
                stmt0.close();
                stmt1.close();
                stmt3.close();
                stmt4.close();
                stmt5.close();
            } catch (Exception e2) {
            }
            closeDB();
            System.exit(1);
        }
    }


    private static void loadData_for_client_enterprise(String client_enterprise, String country, String
            city, String supply_center, String industry
    )
            throws SQLException {
        if (con != null) {
            stmt0.setString(1, client_enterprise);
            stmt0.setString(2, country);
            stmt0.setString(3, city);
            stmt0.setString(4, supply_center);
            stmt0.setString(5, industry);
            stmt0.addBatch();
//                stmt.executeUpdate();
        }
    }


    private static void loadData_for_model(String product_model, int unit_price, String
            product_code, HashMap<String, Integer> map_for_table_model)
            throws SQLException {
        if (con != null) {
            map_for_table_model.put(product_model, 1);
            stmt3.setString(1, product_model);
            stmt3.setInt(2, unit_price);
            stmt3.setString(3, product_code);
            stmt3.addBatch();
        }
    }


    private static void loadData_for_staff(String staff, String name, int age,
                                           String gender, String center, String mobile_number, String type)
            throws SQLException {
        if (con != null) {
            stmt1.setString(1, staff);
            stmt1.setString(2, name);
            stmt1.setInt(3, age);
            stmt1.setString(4, gender);
            stmt1.setString(5, center);
            stmt1.setString(6, mobile_number);
            stmt1.setString(7, type);
            stmt1.addBatch();
        }
    }

    private static void loadData_for_product(String product_code, String
            product_name, HashMap<String, Integer> map_for_table_pro)
            throws SQLException {
        if (con != null) {
            if (map_for_table_pro.get(product_code) == null) {
                map_for_table_pro.put(product_code, 1);
                stmt4.setString(1, product_code);
                stmt4.setString(2, product_name);
//                stmt4.executeUpdate();
                stmt4.addBatch();
            }
        }
    }


    private static void loadData_for_supply_center(String supply_center)
            throws SQLException {
        if (con != null) {
            stmt5.setString(1, supply_center);
            stmt5.setString(2, null);
            stmt5.executeUpdate();
//            con.commit();
//            stmt5.addBatch();
        }
    }


    private static void update_data_for_center(String center, String director
    )
            throws SQLException {
        if (con != null) {
            stmt2.setString(1, director);
            stmt2.setString(2, center);
            stmt2.executeUpdate();
        }
    }


    private static void load_data_for_store(String center, String product_model, String supply_staff, String date, int purchase_price
            , int quantity)
            throws SQLException {
        if (con != null) {
            stmt6.setString(1, center);
            stmt6.setString(2, product_model);
            stmt6.setString(3, supply_staff);
            stmt6.setString(4, date);
            stmt6.setInt(5, purchase_price);
            stmt6.setInt(6, quantity);
//            stmt6.addBatch();
            stmt6.execute();
        }
    }


    private static void loadData_for_contract(String contract_number, String enter_prise, String contract_manager, String
            contract_date, String contract_type, HashMap<String, Integer> map_for_table_con)
            throws SQLException {
        if (con != null) {
            if (map_for_table_con.get(contract_number) == null) {
                map_for_table_con.put(contract_number, 1);
                stmt8.setString(1, contract_number);
                stmt8.setString(2, enter_prise);
                stmt8.setString(3, contract_manager);
                stmt8.setString(4, contract_date);
                stmt8.setString(5, contract_type);
                stmt8.execute();
            }
        }
    }


    private static void loadData_for_order(String contract_number, String product_model, int quantity, String estimated_date
            , String lodgement_date, String salesman_number
    )
            throws SQLException {
        if (con != null) {
            stmt7.setString(1, contract_number);
            stmt7.setString(2, product_model);
            stmt7.setInt(3, quantity);
            stmt7.setString(4, estimated_date);
            stmt7.setString(5, lodgement_date);
            stmt7.setString(6, salesman_number);
            stmt7.execute();
        }
    }


    private static void update_data_for_store(int delta, String center, String product_model, String date
    )
            throws SQLException {
        if (con != null) {
            stmt9.setInt(1, delta);
            stmt9.setString(2, center);
            stmt9.setString(3, product_model);
            stmt9.setString(4, date);
            stmt9.setString(5, center);
            stmt9.setString(6, product_model);
            stmt9.executeUpdate();
        }
    }


}


