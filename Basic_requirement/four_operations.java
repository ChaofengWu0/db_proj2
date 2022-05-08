
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

public class FourOperations {

    private static Connection con = null;
    static String address1 = "C:\\Users\\ll\\Desktop\\University\\dataBase\\proj\\db_proj2\\src\\center.csv";
    static String address2 = "C:\\Users\\ll\\Desktop\\University\\dataBase\\proj\\db_proj2\\src\\enterprise.csv";
    static String address3 = "C:\\Users\\ll\\Desktop\\University\\dataBase\\proj\\db_proj2\\src\\staff.csv";
    static String address4 = "C:\\Users\\ll\\Desktop\\University\\dataBase\\proj\\db_proj2\\src\\model.csv";


    public static void choose(Connection con___) throws IOException, SQLException {
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

    private static void insert() throws IOException, SQLException {
        Scanner sc = new Scanner(System.in);
        Statement statement;
        statement = con.createStatement();
        System.out.println("Please input the table name you want to insert data");
        String tableName = sc.next();
        String[] address = new String[1];
        getAddress(address, tableName);
        BufferedReader br = Files.newBufferedReader(Paths.get(address[0]));
        String[] tempTitles = br.readLine().split(",");

        ArrayList<String> titles = new ArrayList<>();
        String[] inserts;
        if (!tableName.equals("center")) {
            inserts = new String[tempTitles.length - 1];
        } else {
            inserts = new String[tempTitles.length];
        }
        System.out.println("Now please input the values you want to insert\n" +
                "If you don't want to insert the value,please input null,else input the values");
        for (int i = 0; i < tempTitles.length; i++) {
            if (tempTitles[i].equals("id")) {
                continue;
            }
            System.out.print(tempTitles[i] + "   ");
            titles.add(tempTitles[i]);
            if (tableName.equals("center") && i == tempTitles.length - 1) {
                System.out.print("director");
            }
        }
        System.out.println();


        boolean[] has_value = new boolean[inserts.length];
        int cnt = 0;
        for (int i = 0; i < inserts.length; i++) {
            inserts[i] = sc.next();
            if (!inserts[i].equals("null")) {
                cnt++;
                has_value[i] = true;
            }
        }


        // 到这里为止，已经获取了想要插入的表的所有列的名字存在了titles里面,所有的model都要特判
        // has_value数组值为true则说明在这个值里面输入了值
        StringBuilder sql = new StringBuilder("insert into " + tableName + "(");

        StringBuilder sql1 = new StringBuilder("insert into product(");
        StringBuilder sql2 = new StringBuilder("insert into model(");
        if (!tableName.equals("model")) {
            if (cnt == 0) {
                return;
            }
            for (int i = 0; i < titles.size(); i++) {
                if (titles.get(i).equals("id")) continue;
                if (has_value[i]) {
                    sql.append(titles.get(i));
                    if (i == inserts.length - 1) continue;
                    sql.append(",");
                }
            }
            sql.append(") values(");
            for (int i = 0; i < inserts.length; i++) {
                if (has_value[i]) {
                    if (!titles.get(i).equals("age") && !titles.get(i).equals("unit_price")) {
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
        } else {
            // 要在这里面直接操作
            for (int i = 0; i < titles.size(); i++) {
                String title = titles.get(i);
                if (has_value[i]) {
                    switch (title) {
                        case ("number"): {
                            sql2.append(title);
                            sql2.append(",");
                            sql1.append(title);
                            sql1.append(",");
                            break;
                        }
                        case ("name"): {
                            sql1.append(title);
                            sql1.append(",");
                            break;
                        }
                        case ("model"):
                        case ("unit_price"): {
                            sql2.append(title);
                            sql2.append(",");
                            break;
                        }
                    }
                    if (i == titles.size() - 1) {
                        sql1.append(") values(");
                        sql2.append(") values(");
                    }
                }
            }
            for (int i = 0; i < titles.size(); i++) {
                if (has_value[i]) {
                    String value = inserts[i];
                    String title = titles.get(i);
                    switch (title) {
                        case ("number"): {
                            sql1.append("'");
                            sql1.append(value);
                            sql1.append("'");
                            sql2.append("'");
                            sql2.append(value);
                            sql2.append("'");

                            sql1.append(",");
                            sql2.append(",");
                            break;
                        }
                        case ("model"): {
                            sql2.append("'");
                            sql2.append(value);
                            sql2.append("'");
                            sql2.append(",");
                            break;
                        }
                        case ("name"): {
                            sql1.append("'");
                            sql1.append(value);
                            sql1.append("'");
                            sql1.append(",");
                            break;
                        }
                        case ("unit_price"): {
                            sql2.append(value);
                            sql2.append(",");
                            break;
                        }
                    }
                    if (i == titles.size() - 1) {
                        sql1.append(")");
                        sql2.append(")");
                    }
                }
            }
            String[] final_string1 = new String[1];
            String[] final_string2 = new String[1];
            final_string1[0] = sql1.toString();
            final_string2[0] = sql2.toString();


            statement.execute(final_string1[0]);
            con.commit();

            String front = sql2.substring(0, 18);
            String[] temp = new String[1];
            temp[0] = final_string2[0].substring(18);
            change_name(final_string1, "product");
            change_name(temp, "model");
            String actual_execute_sql2 = front + temp[0];
            statement.execute(actual_execute_sql2);
            con.commit();
            if (statement != null) statement.close();
            return;
        }

        String[] final_string = new String[1];
        final_string[0] = sql.toString();
        change_name(final_string, tableName);
        statement.execute(final_string[0]);

        con.commit();
        if (statement != null) statement.close();

    }


    private static void delete() {

    }

    private static void update() {

    }

    private static void select() {

    }


    private static void getAddress(String[] address, String target) {
        switch (target) {
            case ("center"): {
                address[0] = address1;
                break;
            }
            case ("staff"): {
                address[0] = address3;
                break;
            }
            case ("enterprise"): {
                address[0] = address2;
                break;
            }
            case ("product"): {
                address[0] = address4;
                break;
            }
            case ("model"): {
                address[0] = address4;
                break;
            }
        }
    }

    private static void change_name(String[] src, String table_name) {
        src[0] = src[0].replaceAll(",\\)", ")");
        switch (table_name) {
            case ("center"): {
                src[0] = src[0].replaceAll("name", "center");
                break;
            }
            case ("staff"): {
                src[0] = src[0].replaceAll("supply_center", "center");
                src[0] = src[0].replaceAll("number", "staff");
                break;
            }
            case ("enterprise"): {
                src[0] = src[0].replaceAll("name", "enterprise");
                src[0] = src[0].replaceAll("supply_center", "center");
                break;
            }
            case ("model"): {
                src[0] = src[0].replaceAll("number", "product_code");
                src[0] = src[0].replaceAll("model", "product_model");
                break;
            }
            case ("product"): {
                src[0] = src[0].replaceAll("number", "product_code");
                src[0] = src[0].replaceAll("name", "product_name");
                break;
            }
        }
    }

}
