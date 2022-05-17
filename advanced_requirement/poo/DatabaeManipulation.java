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

    public void getConnection() throws SQLException {
        con = this.util.getConnection();
//        System.out.println("------Thread " + Thread.currentThread().getId() + " visiting DB!------");
//        System.out.println(this.util.getConnectState());
    }

    public void closeConnection() {
        this.util.closeConnection(con, statement);
//        System.out.println("------Thread " + Thread.currentThread().getId() + " close DB!------");
    }

    public void buy_IPhone() throws SQLException {
        if (update_advanced_store()) {
            // 说明可以买
            System.out.printf("Thread %d successfully buy the IPhone\n", Thread.currentThread().getId());
        }else{
            System.out.printf("Thread %d can not buy the IPhone, since the IPhones have been sold out\n", Thread.currentThread().getId());
        }
    }

    public int select_advanced_store() throws SQLException {
        Statement update_statement = null;
        String sql_select = "select quantity from advanced_store;";
        update_statement = con.createStatement();
        ResultSet rs = update_statement.executeQuery(sql_select);
        rs.next();
        return rs.getInt("quantity");
    }

    public boolean update_advanced_store() throws SQLException {
        Statement update_statement = null;
        if (select_advanced_store() <= 0) {
            return false;
        }
        String update = "update advanced_store\n" +
                "set quantity = quantity - 1;\n";
        update_statement = con.createStatement();
        update_statement.executeUpdate(update);
        con.commit();
        return true;
    }

}
