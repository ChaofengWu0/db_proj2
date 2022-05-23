package Util;

import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.admin.SnapshotIF;
import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;

import java.sql.*;

public class DatabaseUtil {
    private static DatabaseUtil instance = new DatabaseUtil();

    public DatabaseUtil() {
        try {
            JAXPConfigurator.configure("C:\\Users\\ll\\Desktop\\University\\dataBase\\proj\\db_proj2\\src\\proxool.xml", false);
        } catch (ProxoolException e) {
            e.printStackTrace();
        }
    }

//    public String getConnectState() {
//        try {
//            SnapshotIF snapshotIF = ProxoolFacade.getSnapshot("postgres", true);
//            int curActiveCnt = snapshotIF.getActiveConnectionCount();
//            int availableCnt = snapshotIF.getAvailableConnectionCount();
//            int maxCnt = snapshotIF.getMaximumConnectionCount();
//            return String.format("--- Active:%d\tAvailable:%d  \tMax:%d ---",
//                    curActiveCnt, availableCnt, maxCnt);
//        } catch (ProxoolException e) {
//            e.printStackTrace();
//        }
//        return "visit error";
//    }

    public static DatabaseUtil getInstance() {
        return instance;
    }

    public Connection getConnection() throws SQLException {
        Connection con = null;
        try {
            con = DriverManager.getConnection("proxool.postgres");
//            System.out.println(this.getConnectState());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        con.setAutoCommit(false);
        return con;
    }


    public void closeConnection(Connection con) {
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Thread.interrupted();
//        System.out.println("------Thread " + Thread.currentThread().getId() + " close DB!------");
    }
}
