import Util.DatabaseUtil;
import com.sun.deploy.appcontext.AppContext;
import dao.DatabaseManipulation;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.admin.SnapshotIF;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.TreeMap;

/**
 *
 */

public class Pool {
    private static Connection con = null;

    private static Statement statement = null;

    private static boolean verbose = false;

    private static URL propertyURL = Pool.class
            .getResource("/loader.cnf");

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
            // con = dbpool.getConnection();
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
    }


    private static void closeDB() {
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (Exception e) {
                // Forget about it
            }
        }
    }

    public static void main(String[] args) throws SQLException, InterruptedException, IOException {
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
            statement.execute("truncate table advanced_store cascade");
            statement.execute("TRUNCATE advanced_store RESTART IDENTITY");
            con.commit();
            statement.close();
        }

        closeDB();

        openDB(prop.getProperty("host"), prop.getProperty("database"),
                prop.getProperty("user"), prop.getProperty("password"));

        String sql = "insert into advanced_store(name,quantity) values('IPhone',5);";
        statement = con.createStatement();
        statement.execute(sql);
        con.commit();
//        con.close();
        // 在这里我把数据存好了，接下来是购买
        // 设置15个人去抢

        buy(10);

//        for (int i = 0; i < 5; i++) {
//            new Thread(() -> {
//                DatabaseManipulation dm = new DatabaseManipulation(DatabaseUtil.getInstance());
//                try {
//                    dm.getConnection();
//                    dm.findStationsByLine();
//                    dm.closeConnection();
//                } catch (SQLException | InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }).start();
//        }
        closeDB();
    }

    private static void buy(int count) throws IOException {
        Random random = new Random();
//        String temp = "C:\\Users\\ll\\Desktop\\University\\dataBase\\proj\\db_proj2\\src\\temp";
//        BufferedWriter br = Files.newBufferedWriter(Paths.get(temp));
        for (int i = 0; i < count; i++) {
            new Thread(() -> {
                DatabaseManipulation dm = new DatabaseManipulation(DatabaseUtil.getInstance());
                try {
                    int time = random.nextInt(1000) + 1000;
                    Thread.sleep(time);
                    dm.getConnection();
                    dm.buy_IPhone();
                    dm.closeConnection();
                } catch (SQLException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ).start();
        }
    }

}
