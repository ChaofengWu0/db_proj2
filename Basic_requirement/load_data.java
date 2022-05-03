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

    private static PreparedStatement stmt0 = null;
    private static PreparedStatement stmt1 = null;
    private static PreparedStatement stmt2 = null;
    private static PreparedStatement stmt3 = null;
    private static PreparedStatement stmt4 = null;
    private static PreparedStatement stmt5 = null;

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

                con.close();
                con = null;
            } catch (Exception e) {
                // Forget about it
            }
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


    public static void main(String[] args) throws SQLException {
        String fileName = null;
        boolean verbose = false;


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
//            Statement statement;
        if (con != null) {
            statement = con.createStatement();
            statement.execute("truncate table center,staff,enterprise " +
                    ",product,model,order_table,contract,store");
            con.commit();
            statement.close();
        }
        closeDB();

        openDB(prop.getProperty("host"), prop.getProperty("database"),
                prop.getProperty("user"), prop.getProperty("password"));

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
                    center = parts1[1] + parts1[2];
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
                        center = parts2[5] + parts2[6];
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
                        center = parts4[4] + parts4[5];
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


        closeDB();
    }
}


