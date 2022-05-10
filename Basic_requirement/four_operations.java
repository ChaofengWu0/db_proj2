import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

// age unit_price

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
        boolean[] has_value = new boolean[rsmd.getColumnCount()];
        String[] inserts = new String[rsmd.getColumnCount()];
        for (int i = 0; i < titles.length; i++) {
            String now_title = rsmd.getColumnName(i + 1);
            titles[i] = now_title;
            System.out.print(now_title + "     ");
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
        String table_name = sc.next();
        Statement statement = con.createStatement();
        ArrayList<String> tempTitles = new ArrayList<>();
        getTitles(table_name, tempTitles);

        StringBuilder sql = new StringBuilder("delete from ");
        sql.append(table_name);
        String[] constrains = new String[1];
        constrains[0] = null;
        getConstraints(tempTitles, constrains);
        sql.append(" ");
        sql.append(constrains[0]);
        statement.execute(sql.toString());
        con.commit();
        if (statement != null) statement.close();
    }

    private static void update() {

    }

    private static void select() throws SQLException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Please input the table name you want to select");
        String table_name = sc.next();
        Statement statement = con.createStatement();
        ArrayList<String> tempTitles = new ArrayList<>();
        getTitles(table_name, tempTitles);


        con.commit();
        if (statement != null) statement.close();
    }


    private static void updateForCenter(String center, String name, Connection con) throws SQLException {
        String check_ = "select director from center where center = ?";
        PreparedStatement preparedStatement1 = con.prepareStatement(check_);
        preparedStatement1.setString(1, center);
        ResultSet resultSet = preparedStatement1.executeQuery();
        resultSet.next();
        if (resultSet.getRow() != 0) {
            return;
        }
        String update = "update center set director = ? where center = ?";
        PreparedStatement preparedStatement = con.prepareStatement(update);
        preparedStatement.setString(2, center);
        preparedStatement.setString(1, name);
        preparedStatement.executeUpdate();
        con.commit();
    }

    private static void getConstraints(ArrayList<String> tempTitles, String[] constrains) {
        Scanner sc = new Scanner(System.in);
        ArrayList<String> arrayList = new ArrayList<>();
        System.out.println("Please choose the constrains\n" +
                "If the input is null,it means that this attribute doesn't have a constraint\n" +
                "If the the type of column is 'age' 'unit_price' 'purchase_price' 'quantity',please input two numbers a,b meaning [a,b]\n" +
                "Otherwise, please input a string to satisfy your attempt.\n" +
                "And please input stop to finish the input");
        // 我要做什么？ 我要给where后面添加语句。
        // 怎么添加？ 当有限制的时候就要添加。
        // 怎么判断有没有限制？ 用has_value来确定此处有没有限制,如果has_value对应的索引值是true，那么说明此值有限制,否则无限制，如果一个限制都没有，那么直接返回即可
        // 具体的值应该怎么添加？ 如果是varchar类型，直接where column = value即可，如果是integer类型, between and
//        ArrayList<Boolean> has_value = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        for (String tempTitle : tempTitles) {
            System.out.print(tempTitle + "     ");
        }
        System.out.println();
        String temp;
        int cnt_for_title = 0;
        int cnt = 0;
        while (!(temp = sc.next()).equals("stop")) {
            if (temp.equals("null")) {
                cnt_for_title++;
                continue;
            }
            arrayList.add(tempTitles.get(cnt_for_title));
            if (!check(tempTitles.get(cnt_for_title))) {
                values.add(temp);
            } else {
                values.add(temp);
                temp = sc.next();
                values.add(temp);
            }
            cnt_for_title++;
            cnt++;
        }
        int value_cnt = 0;
        for (int i = 0; i < cnt; i++) {
            if (i == 0) {
                constrains[0] = "where ";
            }
            // 说明是integer类型的
            constrains[0] += arrayList.get(i);
            if (check(arrayList.get(i))) {
                constrains[0] += " between ";
                constrains[0] += values.get(value_cnt++);
                constrains[0] += " and ";
                constrains[0] += values.get(value_cnt++);
            } else {
                constrains[0] += " = ";
                constrains[0] += "'";
                constrains[0] += values.get(value_cnt++);
                constrains[0] += "' ";
            }
            if (i != cnt - 1) {
                constrains[0] += " and ";
            } else {
                constrains[0] += ";";
            }
        }
    }

    private static void getTitles(String table_name, ArrayList<String> tempTitles) throws SQLException {
        String sql = "select * from ";
        sql += table_name;
        Statement statement = con.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        ResultSetMetaData rsmd = resultSet.getMetaData();
        for (int i = 0; i < rsmd.getColumnCount(); i++) {
            tempTitles.add(rsmd.getColumnName(i + 1));
        }
    }

    private static boolean check(String s) {
        return (s.equals("age") || s.equals("purchase_price") || s.equals("quantity") || s.equals("unit_price"));
    }

}
