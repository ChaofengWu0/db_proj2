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
            JAXPConfigurator.configure("src/proxool.xml", false);
        } catch (ProxoolException e) {
            e.printStackTrace();
        }
    }

    public String getConnectState() {
        try {
            SnapshotIF snapshotIF = ProxoolFacade.getSnapshot("postgres", true);
            int curActiveCnt = snapshotIF.getActiveConnectionCount();
            int availableCnt = snapshotIF.getAvailableConnectionCount();
            int maxCnt = snapshotIF.getMaximumConnectionCount();
//            return String.format("--- Active:%d\tAvailable:%d  \tMax:%d ---",
//                    curActiveCnt, availableCnt, maxCnt);
            return " ";
        } catch (ProxoolException e) {
            e.printStackTrace();
        }
        return "visit error";
    }

    public static DatabaseUtil getInstance() {
        return instance;
    }

    public Connection getConnection() {
        Connection con = null;
        try {
            con = DriverManager.getConnection("proxool.postgres");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }

    public void closeConnection(Connection con, PreparedStatement preparedStatement) {
        try {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
