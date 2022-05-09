import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class FourOperations_version2 {

    private static Connection con = null;

    public static void choose(Connection con___) throws SQLException {
        con = con___;
        Scanner sc = new Scanner(System.in);
        System.out.println("Please choose the operations");
        System.out.println("1 insert");
        System.out.println("2 delete");
        System.out.println("3 update");
        System.out.println("4 select");
        System.out.println("q quit the programme");
        String choice___ = sc.next();
        while (!choice___.equals("q")) {
            int choice = Integer.parseInt(choice___);
            if (choice == 1) {
                insert();
            }
            if (choice == 2) {
                delete();
            }
            if (choice == 3) {
                update();
            }
            if (choice == 4) {
                select();
            }
            System.out.println("Please choose the operations");
            System.out.println("1 insert");
            System.out.println("2 delete");
            System.out.println("3 update");
            System.out.println("4 select");
            System.out.println("q quit the programme");
            choice___ = sc.next();
        }
        System.out.println("PROGRAMME OVER! BYE!");
    }

    private static void insert() throws SQLException {
        String center = null, name = null, type = null;


        Scanner sc = new Scanner(System.in);
        Statement statement;
        statement = con.createStatement();
        System.out.println("Please input the table name you want to insert data");
        String tableName = sc.next();
        String select_statement = "select * from ";
        select_statement += tableName;
        ResultSet rs = statement.executeQuery(select_statement);
        ResultSetMetaData rsmd = rs.getMetaData();
        String[] titles = new String[rsmd.getColumnCount()];
        System.out.println("Now please input the values you want to insert\n" +
                "If you don't want to insert the value,please input null,else input the values");
//        ArrayList<String> titles = new ArrayList<>();
        boolean[] has_value = new boolean[rsmd.getColumnCount()];
        String[] inserts = new String[rsmd.getColumnCount()];
        for (int i = 0; i < titles.length; i++) {
            String now_title = rsmd.getColumnName(i + 1);
            titles[i] = now_title;
            System.out.print(now_title + " ");
        }
        System.out.println();

        int cnt = 0;
        for (int i = 0; i < inserts.length; i++) {
            inserts[i] = sc.next();
            if (inserts[i].equals("null")) {
                has_value[i] = false;
                continue;
            }
            has_value[i] = true;
            cnt++;
            if (titles[i].equals("center")) {
                center = inserts[i];
            }
            if (titles[i].equals("type")) {
                type = inserts[i];
            }
            if (titles[i].equals("name")) {
                name = inserts[i];
            }
        }

        StringBuilder sql = new StringBuilder("insert into " + tableName + "(");
        if (cnt == 0) {
            return;
        }

        for (int i = 0; i < titles.length; i++) {
            if (has_value[i]) {
                sql.append(titles[i]);
                if (i == inserts.length - 1) continue;
                sql.append(",");
            }
        }
        sql.append(") values(");
        for (int i = 0; i < inserts.length; i++) {
            if (has_value[i]) {
                if (!titles[i].equals("age") && !titles[i].equals("unit_price") &&
                        !titles[i].equals("quantity") && !titles[i].equals("purchase_price")) {
                    sql.append("'");
                    sql.append(inserts[i]);
                    sql.append("'");
                } else {
                    sql.append(inserts[i]);
                }
                sql.append(",");
            }
        }
        sql.append(")");
        String final_ = sql.toString().replaceAll(",\\)", ")");
        statement.execute(final_);
        con.commit();
        if (tableName.equals("staff") && type != null && type.equals("director")) {
            updateForCenter(center, name, con);
        }
        if (statement != null) statement.close();
    }

    private static void delete() throws SQLException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Please input the table name you want to delete");
        String tale_name = sc.next();
        Statement statement = con.createStatement();
        String sql = "delete from ";


    }

    private static void update() {

    }

    private static void select() {

    }

    private static void updateForCenter(String center, String name, Connection con) throws SQLException {
        String check_ = "select director from center where center = ?";
        PreparedStatement preparedStatement1 = con.prepareStatement(check_);
        preparedStatement1.setString(1, center);
        ResultSet resultSet = preparedStatement1.executeQuery();
        resultSet.next();
        if (resultSet.getRow()!=0){
            return;
        }
        String update = "update center set director = ? where center = ?";
        PreparedStatement preparedStatement = con.prepareStatement(update);
        preparedStatement.setString(2, center);
        preparedStatement.setString(1, name);
        preparedStatement.executeUpdate();
        con.commit();
    }

    private static void getConstraints(String[] tempTitles, String table_name, String[] constrains) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Please choose the constrains\n" +
                "If the input is null,it means that this attribute doesn't have a constraint\n" +
                "If the the type of column is 'age' 'unit_price',please input two numbers a,b meaning [a,b]\n" +
                "Otherwise, please input a string to satisfy your attempt.");
        if (!table_name.equals("model")) {
            // 这里可以获得总共要输入的数量
            int total = 0;
            boolean has_age = false;
            for (int i = 0; i < tempTitles.length; i++) {
                System.out.print(tempTitles[i] + "   ");
                if (tempTitles[i].equals("age")) {
                    total += 2;
                    has_age = true;
                } else total++;
            }
            ArrayList<String> inputs = new ArrayList<>();
            for (int i = 0; i < total; i++) {
                inputs.add(sc.next());
            }


        } else {
            // 0是product
            // 1是model

        }
    }
}
