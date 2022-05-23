package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static javafx.scene.control.SelectionMode.MULTIPLE;
import static javafx.scene.control.SelectionMode.SINGLE;

//在controller中，我们实现每个组件对应的逻辑
//目前登录后只能手动关闭
public class Controller {
    //数据库的部分
    private static Connection con = null;
    private static Statement statement = null;
    private static final int BATCH_SIZE = 500;
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
    @FXML
    public TextField enterprise;
    @FXML
    public TextField center;
    @FXML
    public TextField time;
    @FXML
    public TextField staff;
    @FXML
    public TextArea window;
    @FXML
    public TextArea inputInsert;
    public TextArea output;
    public TextArea inputDelete;
    public Button finishDelete;
    public TextArea inputUpdate;
    public Button finishUpdate;
    public TextArea inputSelect;
    public Button finishSelect;
    public TextArea outputSelect;
    Properties defprop = new Properties();
    private static boolean verbose = false;

    @FXML
    private TextField user;//用来接受user名，与MainUI中的user对应
    @FXML
    private PasswordField pswd;//用来接受password，与MainUI中的pswd对应
    StringBuilder sb=new StringBuilder();//用于把查询结果搜集起来
    @FXML
    public void login() throws IOException, SQLException {
        Stage primaryStage = new Stage();
        String name = user.getText();

        String password = pswd.getText();
        //连接数据库
        defprop.put("host", "localhost");
        defprop.put("user", name);//////////////////////pmy需要改
        defprop.put("password", password);//////////////////////pmy需要改
        defprop.put("database", "cs3072");//////////////////////pmy需要改
        Properties prop = new Properties(defprop);
        //删表
//        openDB(prop.getProperty("host"), prop.getProperty("database"),
//                prop.getProperty("user"), prop.getProperty("password"));
//        if (con != null) {
//            statement = con.createStatement();
//            statement.execute("truncate table center,staff,enterprise " +
//                    ",product,model,contract,store cascade");
//            statement.execute("TRUNCATE order_table RESTART IDENTITY");
//            con.commit();
//            statement.close();
//        }
//
//
//        closeDB();
        openDB(prop.getProperty("host"), prop.getProperty("database"),
                prop.getProperty("user"), prop.getProperty("password"));

        System.out.println("Welcome!" + name+"!");

        //之后打开主界面

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("operation.fxml"));
        Pane root = fxmlLoader.load();
        primaryStage.setTitle("User:"+name);
        primaryStage.setScene(new Scene(root));

        primaryStage.show();
    }

    public static void openDB(String host, String dbname,
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


    public static void closeDB() {
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (Exception e) {
                // Forget about it
            }
        }
    }
    public void enterpriseQuary(ActionEvent actionEvent) throws SQLException {
        enterpriseBill(enterprise.getText());
    }
//查询enterprise的账单
    public void enterpriseBill(String enterprise) throws SQLException {
        PreparedStatement statement1 = null;
        PreparedStatement statement2 = null;
        PreparedStatement statement3 = null;
        PreparedStatement statement4 = null;
        PreparedStatement statement5 = null;
        int total_cost=0;
        statement1=con.prepareStatement("select *\n" +
                "from contract\n" +
                "where enterprise=?");
        statement1.setString(1,enterprise);
        ResultSet contractSet=statement1.executeQuery();
        contractSet.next();
        String contract;
        System.out.printf("%-20s %-20s %-20s %20s \n","model","price","quantity","cost");
        sb.append(String.format("%-20s %-20s %-20s %20s \n","model","price","quantity","cost"));
        while (contractSet.getRow() != 0) {//可能有bug
            contract=contractSet.getString("contract_number");
            statement2=con.prepareStatement("select *,sum(cost) over () as sum\n" +
                    "from\n" +
                    "(\n" +
                    "select o.product_model as model,m.unit_price as price,o.quantity as quantity\n" +
                    "     ,m.unit_price*o.quantity as cost\n" +
                    "from order_table o\n" +
                    "join model m on o.product_model = m.product_model\n" +
                    "where o.contract_number=?\n" +
                    ")temp;");
            statement2.setString(1,contract);
            ResultSet orderSet=statement2.executeQuery();
            orderSet.next();
            int contractCost=0;
            while(orderSet.getRow()!=0){
                contractCost=orderSet.getInt("sum");
                System.out.printf("%-20s %-20s %-20s %20s \n",orderSet.getString("model"),orderSet.getString("price"),orderSet.getInt("quantity"),orderSet.getInt("cost"));
                sb.append(String.format("%-20s %-20s %-20s %20s \n",orderSet.getString("model"),orderSet.getString("price"),orderSet.getInt("quantity"),orderSet.getInt("cost")));
                orderSet.next();//
            }
            //到这里是一个合同查完了，我们更新一下已经花的钱
            total_cost+=contractCost;
            contractSet.next();//
        }
        con.commit();
        System.out.println("total cost:"+total_cost);
        sb.append("total cost:"+total_cost+"\n");
        window.setText(sb.toString());//把查询结果展示出来
    }

    //staff业绩查询
    public void staffQuary(ActionEvent actionEvent) throws SQLException {
        staffPerformance(staff.getText());
    }
    public void staffPerformance(String staff) throws SQLException {
        PreparedStatement statement1 = null;
        PreparedStatement statement2 = null;
        PreparedStatement statement3 = null;
        PreparedStatement statement4 = null;
        PreparedStatement statement5 = null;
        statement1=con.prepareStatement("select *\n" +
                "from staff\n" +
                "where staff=?");
        statement1.setString(1,staff);
        ResultSet staffSet=statement1.executeQuery();
        staffSet.next();
        String type=staffSet.getString("type");
        System.out.printf("name:%s\nage:%d\nphone:%s\ntype:%s\n",staffSet.getString("name"),staffSet.getInt("age"),staffSet.getString("mobile_number"),staffSet.getString("type"));
        sb.append(String.format("name:%s\nage:%d\nphone:%s\ntype:%s\n",staffSet.getString("name"),staffSet.getInt("age"),staffSet.getString("mobile_number"),staffSet.getString("type")));
        if(type.equals("Director")){
            statement2=con.prepareStatement("select * from center where director=?");
            statement2.setString(1,staffSet.getString("name"));
            ResultSet directorSet= statement2.executeQuery();
            directorSet.next();
            centerSales(directorSet.getString("center"));
        }else if(type.equals("Supply Staff")){
            int maxDifference=Integer.MIN_VALUE;
            int minDifference=Integer.MAX_VALUE;
            int difference=0;
            int stores=0;
            statement3=con.prepareStatement("select s.product_model as model,s.purchase_price as purchase,m.unit_price as unitprice,s.purchase_price-m.unit_price as difference,s.quantity as quantity\n" +
                    "from store s\n" +
                    "join model m on s.product_model = m.product_model\n" +
                    "where supply_staff=?");
            statement3.setString(1,staffSet.getString("staff"));
            ResultSet supplySet=statement3.executeQuery();
            supplySet.next();
            System.out.printf("%-20s %-20s %-20s %-20s %20s \n","model","purchasePrice","unitPrice","difference","quantity");
            sb.append(String.format("%-20s %-20s %-20s %-20s %20s \n","model","purchasePrice","unitPrice","difference","quantity"));
            while(supplySet.getRow()!=0){
                stores++;
                System.out.printf("%-20s %-20d %-20d %-20d %20d \n",supplySet.getString("model"),supplySet.getInt("purchase"),supplySet.getInt("unitprice"),supplySet.getInt("difference"),supplySet.getInt("quantity"));
                sb.append(String.format("%-20s %-20d %-20d %-20d %20d \n",supplySet.getString("model"),supplySet.getInt("purchase"),supplySet.getInt("unitprice"),supplySet.getInt("difference"),supplySet.getInt("quantity")));
                maxDifference=Math.max(maxDifference,supplySet.getInt("difference"));
                minDifference=Math.min(minDifference,supplySet.getInt("difference"));
                difference+=supplySet.getInt("difference");
                supplySet.next();
            }
            System.out.println("number of stores:"+stores);
            sb.append("number of stores:"+stores+"\n");
            System.out.println("maxDifference:"+maxDifference);
            sb.append("maxDifference:"+maxDifference+"\n");
            System.out.println("minDifference:"+minDifference);
            sb.append("minDifference:"+minDifference+"\n");
            System.out.println("meanDifference:"+difference*1.0/stores);
            sb.append("meanDifference:"+difference*1.0/stores+"\n");
        }else if(type.equals("Salesman")){
            statement4=con.prepareStatement("select o.product_model,m.unit_price,o.quantity,m.unit_price*o.quantity as cost\n" +
                    "from order_table o\n" +
                    "join model m on o.product_model = m.product_model\n" +
                    "where salesman_number=?");
            statement4.setString(1,staffSet.getString("staff"));
            ResultSet salesSet=statement4.executeQuery();
            salesSet.next();
            System.out.printf("%-20s %-20s %-20s %-20s \n","model","price","quantity","cost");
            sb.append(String.format("%-20s %-20s %-20s %-20s \n","model","price","quantity","cost"));
            int orders=0;
            int business=0;
            while(salesSet.getRow()!=0){
                orders++;
                System.out.printf("%-20s %-20s %-20s %-20s \n",salesSet.getString("product_model"),salesSet.getInt("unit_price"),salesSet.getInt("quantity"),salesSet.getInt("cost"));
                sb.append(String.format("%-20s %-20s %-20s %-20s \n",salesSet.getString("product_model"),salesSet.getInt("unit_price"),salesSet.getInt("quantity"),salesSet.getInt("cost")));
                business+=salesSet.getInt("cost");
                salesSet.next();
            }
            System.out.println("number of orders:"+orders);
            sb.append("number of orders:"+orders+"\n");
            System.out.println("business:"+business);
            sb.append("business:"+business+"\n");
        }else if(type.equals("Contracts Manager")){
            int unfinished=0;
            int finished=0;
            statement5=con.prepareStatement("select contract_number,enterprise,contract_type\n" +
                    "from contract\n" +
                    "where contract_manager=?");
            statement5.setString(1,staffSet.getString("staff"));
            ResultSet managerSet=statement5.executeQuery();
            managerSet.next();
            System.out.printf("%-20s %-20s %-20s \n","contract","enterprise","attitude");
            sb.append(String.format("%-20s %-20s %-20s \n","contract","enterprise","attitude"));
            String contract;
            String enterprise;
            String attitude;
            while(managerSet.getRow()!=0){
                contract=managerSet.getString("contract_number");
                enterprise=managerSet.getString("enterprise");
                attitude=managerSet.getString("contract_type");
                if(attitude.equals("Finished")){
                    finished++;
                }else{
                    unfinished++;
                }
                System.out.printf("%-20s %-20s %-20s \n",contract,enterprise,attitude);
                sb.append(String.format("%-20s %-20s %-20s \n",contract,enterprise,attitude));
                managerSet.next();
            }
            System.out.println("total number of contracts:"+(unfinished+finished));
            sb.append("total number of contracts:"+(unfinished+finished)+"\n");
            System.out.println("finish rate:"+(finished*1.0/(finished+unfinished))*100+"%");
            sb.append("finish rate:"+(finished*1.0/(finished+unfinished))*100+"%"+"\n");
        }
        window.setText(sb.toString());
        con.commit();
    }
    //center销售情况
    public void centerQuary(ActionEvent actionEvent) throws SQLException {
        centerSales(center.getText());
    }
    public void centerSales(String center) throws SQLException {
        PreparedStatement statement1 = null;
        PreparedStatement statement2 = null;
        PreparedStatement statement3 = null;
        PreparedStatement statement4 = null;
        PreparedStatement statement5 = null;
        statement3=con.prepareStatement("select *\n" +
                "from enterprise\n" +
                "where center=?");
        statement3.setString(1,center);
        ResultSet enterpriseSet=statement3.executeQuery();
        enterpriseSet.next();
        while(enterpriseSet.getRow()!=0){
            String enterprise=enterpriseSet.getString("enterprise");
            System.out.println(enterprise);
            sb.append(enterprise+"\n");
            enterpriseBill(enterprise);
            enterpriseSet.next();
        }
        window.setText(sb.toString());
        con.commit();
    }
    //设计根据时间⽇期更改订单状态的机制(需要先输入一个参考时间点)
    public void updateTime(ActionEvent actionEvent) throws SQLException {
        updateOrderTime(time.getText());
    }
    private static void updateOrderTime(String time) throws SQLException {
        /*
        先遍历contract，对每一个contract，找到它所有的order
        如果有lodgement time大于当前时间的，把状态改为unfinished，否则改为finished

         */
        PreparedStatement statement1 = null;
        PreparedStatement statement2 = null;
        PreparedStatement statement3 = null;
        PreparedStatement statement4 = null;
        PreparedStatement statement5 = null;
        statement1=con.prepareStatement("select * from contract");
        ResultSet contractSet=statement1.executeQuery();
        contractSet.next();
        String contract;
        statement4=con.prepareStatement("create or replace procedure update_contract_type(cur_contract varchar)\n" +
                "as $$\n" +
                "    begin\n" +
                "        update contract set contract_type='unfinished' where contract_number=cur_contract;\n" +
                "    end;\n" +
                "    $$ language plpgsql;");//使用了procedure
        statement4.execute();
        while (contractSet.getRow() != 0) {//可能有bug
            contract=contractSet.getString("contract_number");
            statement2=con.prepareStatement("select * from order_table where contract_number=?");
            statement2.setString(1,contract);
            ResultSet orderSet=statement2.executeQuery();
            orderSet.next();
            String lodgement_time;
            while(orderSet.getRow()!=0){
                lodgement_time=orderSet.getString("lodgement_date");
                if(compareTime(time,lodgement_time)<0){
                    statement3=con.prepareStatement("call update_contract_type(?)");
                    statement3.setString(1,contract);
                    statement3.executeUpdate();
                    break;
                }
                orderSet.next();//
            }
            contractSet.next();//
        }
        con.commit();
    }
    public static int compareTime(String now,String lodgement){
        String[]s1=now.split("-");
        String[]s2=lodgement.split("-");
        if(Integer.parseInt(s1[0])<Integer.parseInt(s2[0])){
            return -1;
        }else if(Integer.parseInt(s1[0])>Integer.parseInt(s2[0])){
            return 1;
        }else if(Integer.parseInt(s1[1])<Integer.parseInt(s2[1])) {
            return -1;
        }else if(Integer.parseInt(s1[1])>Integer.parseInt(s2[1])) {
            return 1;
        }else if(Integer.parseInt(s1[2])<Integer.parseInt(s2[2])) {
            return -1;
        }else if(Integer.parseInt(s1[2])>Integer.parseInt(s2[2])) {
            return 1;
        }else{
            return 0;
        }
    }


    public void clear(ActionEvent actionEvent) {
        sb=new StringBuilder();
        window.setText(sb.toString());
    }


    //以下只是单纯搬运的之前实现的Basic部分的内容
    public void Basic(ActionEvent actionEvent) throws IOException, SQLException {
        load_original_data();

        stockIn();
        placeOrder();
        updateOrder();
        deleteOrder();

        String Q12_address = "C:\\javaA\\javafx\\javafxStudy\\src\\data\\Q12";
        String Q13_address = "C:\\javaA\\javafx\\javafxStudy\\src\\data\\Q13";
        BufferedReader br = Files.newBufferedReader(Paths.get(Q12_address));
        String temp;
        ArrayList<String> Q12 = new ArrayList<>();

        while ((temp = br.readLine()) != null) {
            String[] Q12_array = temp.split(",");
            Collections.addAll(Q12, Q12_array);
        }
        br = Files.newBufferedReader(Paths.get(Q13_address));
        ArrayList<String> Q13 = new ArrayList<>();
        while ((temp = br.readLine()) != null) {
            String[] Q13_array = temp.split(",");
            Collections.addAll(Q13, Q13_array);
        }



        getAllStaffCount();
        getContractCount();
        getOrderCount();
        getNeverSoldProductCount();
        getFavoriteProductModel();
        getAvgStockByCenter();
        System.out.println("Q12");
        sb.append("Q12"+"\n");
        for (String s : Q12) {
            getProductByNumber(s);
        }
        System.out.println("Q13");
        sb.append("Q13"+"\n");
        for (String s : Q13) {
            getContractInfo(s);
        }
        window.setText(sb.toString());
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
//        System.out.println("Q13");
//        sb.append("Q13"+"\n");
        while (resultSet1.getRow() != 0) {
            String a1 = resultSet1.getString("contract_number");
            String b1 = resultSet1.getString("enterprise");
            String c1 = resultSet1.getString("name");
            String d1 = resultSet1.getString("center");
            System.out.printf("contract_number:%s\n", a1);
            sb.append(String.format("contract_number:%s\n", a1));
            System.out.printf("enterprise:%s\n", b1);
            sb.append(String.format("enterprise:%s\n", b1));
            System.out.printf("manager:%s\n", c1);
            sb.append(String.format("manager:%s\n", c1));
            System.out.printf("supply_center:%s\n", d1);
            sb.append(String.format("supply_center:%s\n", d1));
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
                System.out.printf("%-35s %-25s %-15s %-5s %-5s %-10s\n", "product_model", "salesman", "quantity", "unit_price", "estimate_delivery_date", "lodgement_date");
                sb.append(String.format("%-35s %-25s %-15s %-5s %-5s %-10s\n", "product_model", "salesman", "quantity", "unit_price", "estimate_delivery_date", "lodgement_date"));
                temp_cnt++;
            }
            product_model = resultSet2.getString("product_model");
            salesman = resultSet2.getString("name");
            quantity = resultSet2.getInt("quantity");
            unit_price = resultSet2.getInt("unit_price");
            estimate_delivery_date = resultSet2.getString("estimated_date");
            lodgement_date = resultSet2.getString("lodgement_date");
            System.out.printf("%-35s %-25s %-15d %-5d %-5s %-10s\n", product_model, salesman, quantity, unit_price, estimate_delivery_date, lodgement_date);
            sb.append(String.format("%-35s %-25s %-15d %-5d %-5s %-10s\n", product_model, salesman, quantity, unit_price, estimate_delivery_date, lodgement_date));
            resultSet2.next();
        }

        if (statement != null)
            statement.close();
        if (statement2 != null)
            statement2.close();
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
//        System.out.println("Q12");
//        sb.append("Q12"+"\n");
        System.out.printf("%-50s %-25s %-20s %-20s\n", a, b, c, d);
        sb.append(String.format("%-50s %-25s %-20s %-20s\n", a, b, c, d));
        while (resultSet.getRow() != 0) {
            center = resultSet.getString("center");
            product_model = resultSet.getString("product_model");
            purchase_price = resultSet.getInt("purchase_price");
            quantity = resultSet.getInt("quantity");
            resultSet.next();
            System.out.printf("%-50s %-25s %-20d %-20d\n", center, product_model, purchase_price, quantity);
            sb.append(String.format("%-50s %-25s %-20d %-20d\n", center, product_model, purchase_price, quantity));
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
        sb.append("Q11"+"\n");
        while (resultSet.getRow() != 0) {
            String center = resultSet.getString("center");
            double quantity = resultSet.getDouble("quantity");
            System.out.printf("%-50s %-10.1f\n", center, quantity);
            sb.append(String.format("%-50s %-10.1f\n", center, quantity));
            resultSet.next();
        }


        if (statement != null)
            statement.close();
    }

    private void getFavoriteProductModel() throws SQLException {
        PreparedStatement statement = null;
        statement = con.prepareStatement("select distinct product_model, sum as quantity\n" +
                "from (\n" +
                "         select *, max(sum) over () as max\n" +
                "         from (\n" +
                "                  select *, sum(quantity) over (partition by product_model) as sum\n" +
                "                  from order_table) sub_table) sub_table2\n" +
                "where sum = max;");
        ResultSet result = statement.executeQuery();
        System.out.println("Q10");
        sb.append("Q10"+"\n");
        result.next();
        while (result.getRow() != 0) {
            String product_model = result.getString("product_model");
            int quantity = result.getInt("quantity");
            System.out.printf("%-40s%-10d\n", product_model, quantity);
            sb.append(String.format("%-40s%-10d\n", product_model, quantity));
            result.next();
        }
        if (statement != null)
            statement.close();
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
        sb.append("Q9 ");
        System.out.println(countSet.getString("count"));
        sb.append(countSet.getString("count")+"\n");

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
        sb.append("Q8 ");
        System.out.println(countSet.getString("count"));
        sb.append(countSet.getString("count")+"\n");

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
        sb.append("Q7 ");
        System.out.println(countSet.getString("count"));
        sb.append(countSet.getString("count")+"\n");
//        con.commit();
        if (statement1 != null) statement1.close();
    }

    private void getAllStaffCount() throws SQLException {
        PreparedStatement statement1 = null;
        statement1 = con.prepareStatement("select type,count(*)\n" +
                "from staff\n" +
                "group by type");
        ResultSet staffType = statement1.executeQuery();
        staffType.next();//跳过列名的第一排
        System.out.println("Q6");
        sb.append("Q6"+"\n");
        while (staffType.getRow() != 0) {
            String type = staffType.getString("type");
            int count = staffType.getInt("count");
            System.out.printf("%-40s%-10d\n", type, count);
            sb.append(String.format("%-40s%-10d\n", type, count));
            staffType.next();
        }
//        con.commit();
        if (statement1 != null) statement1.close();
    }

    private void deleteOrder() throws IOException, SQLException {
        PreparedStatement statement1 = null;
        PreparedStatement statement2 = null;
        PreparedStatement statement3 = null;
        PreparedStatement statement4 = null;
        PreparedStatement statement5 = null;
        BufferedReader br = new BufferedReader(new FileReader("C:\\javaA\\javafx\\javafxStudy\\src\\data\\delete_final.csv"));
        br.readLine();
        String line;
        String contract, salesman;
        int seq;
        while ((line = br.readLine()) != null) {
            // 这里改了
            String[] parts = line.split(",");
            contract = parts[0];
            salesman = parts[1];
            seq = Integer.parseInt(parts[2]);
            statement1 = con.prepareStatement("select id from(select *, rank() over(partition by contract_number,salesman_number order by estimated_date ,product_model)r\n" +
                    "from order_table)rank where contract_number=? and salesman_number=? and r=?");
//           statement1 = con.prepareStatement("select id from(select *, rank() over(partition by contract_number,salesman_number order by estimated_date,product_model)r\\n\" +\n" +
//                   "//                    \"from order_table)rank where contract_number=? and salesman_number=? and r=?");

            statement1.setString(1, contract);
            statement1.setString(2, salesman);
            statement1.setInt(3, seq);
            ResultSet idSet = statement1.executeQuery();
            idSet.next();
            if (idSet.getRow() == 0) {
                continue;
            }
            int id = idSet.getInt("id");
            //第一个检查点自动满足了，因为前面查询的时候就对salesman有要求
            statement3 = con.prepareStatement("select * from order_table where id=?");
            statement3.setInt(1, id);
            ResultSet orderSet = statement3.executeQuery();
            orderSet.next();
            if (orderSet.getRow() == 0) {
                continue;
            }

            statement4 = con.prepareStatement("select center from enterprise where enterprise=(select enterprise from contract where contract_number=?)");
            statement4.setString(1, orderSet.getString("contract_number"));
            ResultSet centerSet = statement4.executeQuery();
            centerSet.next();
            if (centerSet.getRow() == 0) continue;
            String nowCenter = centerSet.getString("center");
            //第二个检查点，删除订单后，库存数量要随之改变
            update_data_for_store(orderSet.getInt("quantity"), nowCenter, orderSet.getString("product_model"), orderSet.getString("lodgement_date"));
            statement2 = con.prepareStatement("delete from order_table where id=?");
            statement2.setInt(1, id);
            statement2.execute();
            //第三个检查点自动满足
        }
        con.commit();
    }

    private void updateOrder() throws IOException, SQLException {
        PreparedStatement statement1 = null;
        PreparedStatement statement2 = null;
        PreparedStatement statement3 = null;
        PreparedStatement statement4 = null;
        PreparedStatement statement5 = null;
        PreparedStatement statement6 = null;
        BufferedReader br = new BufferedReader(new FileReader("C:\\javaA\\javafx\\javafxStudy\\src\\data\\update_final_test.tsv"));
        br.readLine();
        String line;
        String contract, product_model, salesman, estimate_delivery_date, lodgement_date;
        int quantity;
        while ((line = br.readLine()) != null) {
            // 修改了
            String[] parts = line.split("\t");
            contract = parts[0];
            product_model = parts[1];
            salesman = parts[2];
            quantity = Integer.parseInt(parts[3]);
            estimate_delivery_date = parts[4];
            lodgement_date = parts[5];
            /*
            目标：通过update中的数据找到order表格中对应的数据
            直接在order中搜索contract和model
             */
            statement1 = con.prepareStatement("select id from order_table where contract_number=? and product_model=? and salesman_number=?");
            statement1.setString(1, contract);
            statement1.setString(2, product_model);
            statement1.setString(3, salesman);
            ResultSet IDset = statement1.executeQuery();
            IDset.next();
            if (IDset.getRow() == 0) {
                continue;
            }
            int id = IDset.getInt("id");
            //第一个检查点：销售员只能更新自己的订单
            statement3 = con.prepareStatement("select * from order_table where id=?");
            statement3.setInt(1, id);
            ResultSet orderSet = statement3.executeQuery();
            orderSet.next();
            if (orderSet.getRow() == 0) {
                continue;
            }
            String nowSalesman = orderSet.getString("salesman_number");
            if (!salesman.equals(nowSalesman)) {
                continue;
            }
            //未提到的检查点：爆仓
            statement4 = con.prepareStatement("select center from enterprise where enterprise=(select enterprise from contract where contract_number=?)");
            statement4.setString(1, contract);
            ResultSet centerSet = statement4.executeQuery();
            centerSet.next();
            if (centerSet.getRow() == 0) continue;
            String nowCenter = centerSet.getString("center");
            statement5 = con.prepareStatement("select quantity from store where center = ? and product_model = ?");
            statement5.setString(1, nowCenter);
            statement5.setString(2, product_model);
            ResultSet check1_b = statement5.executeQuery();
            check1_b.next();
            if (check1_b.getRow() == 0) continue;
            int store_quantity = check1_b.getInt("quantity");
            if (store_quantity + orderSet.getInt("quantity") < quantity) continue;

            //第二个检查点：更新订单数量的同时，库存数量也要随之改变,(ps：记得把原来的order中的quantity先要加回去)
            update_data_for_store(-quantity + orderSet.getInt("quantity"), nowCenter, product_model, lodgement_date);
            //第三个检查点，如果一个订单更新后数量是0，那么这个订单要在合同中移除
            if (quantity == 0) {
                deleteOrderByID(id);
            }
            //第四个检查点貌似并不用写了
            //大部分更新
            statement2 = con.prepareStatement("update order_table set quantity=?,estimated_date=?,lodgement_date=? where id=?");//只有数量，edd，ld可能会被更新
            statement2.setInt(1, quantity);
            statement2.setString(2, estimate_delivery_date);
            statement2.setString(3, lodgement_date);
            statement2.setInt(4, id);
            statement2.executeUpdate();
        }
        con.commit();
    }

    private void deleteOrderByID(int id) throws SQLException {
        PreparedStatement statement = con.prepareStatement("delete from order_table where id=?");
        statement.setInt(1, id);
        statement.execute();
    }

    private void placeOrder() throws IOException, SQLException {
        PreparedStatement statement1 = null;
        PreparedStatement statement2 = null;
        String address_for_place_order = "C:\\javaA\\javafx\\javafxStudy\\src\\data\\task2_test_data_final_public.tsv";
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
            parts = line.split("\t");
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

    private void stockIn() throws IOException, SQLException {
        PreparedStatement statement = null;//////////////////////pmy需要改
        String address_for_stock = "C:\\javaA\\javafx\\javafxStudy\\src\\data\\in_stoke_test.csv";
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
                center = center.replaceAll("\"", "");
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
            // 第一个检查和第三个检查，供应中心与人员所在的供应中心对不上， 供应中心不存在,人不存在
            statement = con.prepareStatement("select center from center where center = ?");
            statement.setString(1, center);
            ResultSet check1_a = statement.executeQuery();
            check1_a.next();
            // 供应中心不存在
            if (check1_a.getRow() == 0) continue;

            // 人不存在
            statement = con.prepareStatement("select center from staff where staff = ?");
            statement.setString(1, staff);
            ResultSet check1_b = statement.executeQuery();
            check1_b.next();
            if (check1_b.getRow() == 0) continue;

            String check_center = check1_b.getString("center");
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

    private void load_original_data() throws SQLException {
        if (con != null) {
            statement = con.createStatement();
            statement.execute("truncate table center,staff,enterprise " +
                    ",product,model,contract,store cascade");
            statement.execute("TRUNCATE order_table RESTART IDENTITY");
            con.commit();
            statement.close();
        }
        try {                                      ////////////////////////////pmy需要改
            String address1 = "C:\\javaA\\javafx\\javafxStudy\\src\\data\\center.csv";
            String address2 = "C:\\javaA\\javafx\\javafxStudy\\src\\data\\staff.csv";
            String address3 = "C:\\javaA\\javafx\\javafxStudy\\src\\data\\model.csv";
            String address4 = "C:\\javaA\\javafx\\javafxStudy\\src\\data\\enterprise.csv";

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
                    center = center.replaceAll("\"", "");
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
                        center = center.replaceAll("\"", "");
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
                        center = center.replaceAll("\"", "");
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

    public void openInsert(ActionEvent actionEvent) throws IOException {
        Stage primaryStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("InsertGUI.fxml"));
        Pane root = fxmlLoader.load();
        primaryStage.setTitle("Insert");
        primaryStage.setScene(new Scene(root));

        primaryStage.show();
    }

    public void openDelete(ActionEvent actionEvent) throws IOException {
        Stage primaryStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("DeleteGUI.fxml"));
        Pane root = fxmlLoader.load();
        primaryStage.setTitle("Delete");
        primaryStage.setScene(new Scene(root));

        primaryStage.show();
    }

    public void openUpdate(ActionEvent actionEvent) throws IOException {
        Stage primaryStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("UpdateGUI.fxml"));
        Pane root = fxmlLoader.load();
        primaryStage.setTitle("Update");
        primaryStage.setScene(new Scene(root));

        primaryStage.show();
    }

    public void openSelect(ActionEvent actionEvent) throws IOException {
        Stage primaryStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("SelectGUI.fxml"));
        Pane root = fxmlLoader.load();
        primaryStage.setTitle("Select");
        primaryStage.setScene(new Scene(root));

        primaryStage.show();
    }

    private void insert() throws SQLException {
        int count=0;
        String center = null, name = null, type = null;
        Statement statement;
        statement = con.createStatement();
        String[]para=inputInsert.getText().split("\t");
        System.out.println("Please input the table name you want to insert data");
        String tableName = para[count];
        count++;
        String select_statement = "select * from ";
        select_statement += tableName;
        ResultSet rs = statement.executeQuery(select_statement);
        ResultSetMetaData rsmd = rs.getMetaData();
        String[] titles = new String[rsmd.getColumnCount()];
        System.out.println("Now please input the values you want to insert\n" +
                "If you don't want to insert the value,please input null,else input the values\n");
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
            inserts[i] = para[count];
            count++;
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

    private void delete() throws SQLException {
        String[]para=inputDelete.getText().split("\t");
        int count=0;
        System.out.println("Please input the table name you want to delete");
        String table_name = para[count];
        count++;
        Statement statement = con.createStatement();
        ArrayList<String> tempTitles = new ArrayList<>();
        getTitles(table_name, tempTitles);

        StringBuilder sql = new StringBuilder("delete from ");
        sql.append(table_name);
        String[] constrains = new String[1];
        constrains[0] = null;
        getConstraints(tempTitles, constrains,count,para);
        sql.append(" ");
        sql.append(constrains[0]);
        statement.execute(sql.toString());
        con.commit();
        if (statement != null) statement.close();
    }

    private void update() throws SQLException {
        String[]para=inputUpdate.getText().split("\t");
        int count=0;
        StringBuilder sql = new StringBuilder("update ");
        System.out.println("Please input the table name you want to delete");
        String table_name = para[count];
        count++;
        sql.append(table_name);
        sql.append(" set ");
        Statement statement = con.createStatement();
        ArrayList<String> tempTitles = new ArrayList<>();
        getTitles(table_name, tempTitles);
        String[] constrains = new String[1];
        count=getConstraints(tempTitles, constrains,count,para);
        System.out.println("Please choose the columns you want to update\n" +
                "If you want to update the value of the column,input the value\n" +
                "If you do not want to update the value, input 'null'\n" +
                "And please input 'stop' to finish the input\n");

        for (String tempTitle : tempTitles) {
            System.out.print(tempTitle + "     ");
        }
        System.out.println();
        ArrayList<String> update_titles = new ArrayList<>();
        ArrayList<String> update_values = new ArrayList<>();
        String temp;
        int cnt = 0;
        while (!(temp = para[count++]).equals("stop")) {
            if (!temp.equals("null")) {
                update_titles.add(tempTitles.get(cnt));
                update_values.add(temp);
            }
            cnt++;
        }
        // update设置
        for (int i = 0; i < update_titles.size(); i++) {
            sql.append(update_titles.get(i));
            sql.append(" = ");
            if (check(update_titles.get(i))) {
                sql.append(update_values.get(i));
            } else {
                sql.append("'");
                sql.append(update_values.get(i));
                sql.append("'");
            }
            if (i != update_titles.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(constrains[0]);
        statement.executeUpdate(sql.toString());
        con.commit();
        if (statement != null) {
            statement.close();
        }
    }

    private void select() throws SQLException, IOException {
        String[]para=inputSelect.getText().split("\t");
        int count=0;
        System.out.println("Please input the table name you want to select");
        String table_name = para[count];
        count++;
        Statement statement = con.createStatement();
        ArrayList<String> tempTitles = new ArrayList<>();
        getTitles(table_name, tempTitles);
        System.out.println("Now,please choose the column you want\n" +
                "If you want to select the column, input the name of the column\n" +
                "Or you want to select all columns,please input * \n" +
                "And if you finished the selection please input 'stop'\n" +
                "And here is the column name of the table\n");
        for (String tempTitle : tempTitles) {
            System.out.print(tempTitle + "   ");
        }
        System.out.println();
        ArrayList<String> columns = new ArrayList<>();
        String temp;
        while (!(temp = para[count++]).equals("stop")) {
            if (!temp.equals("null"))
                columns.add(temp);
        }
        String[] constrains = new String[1];
        count=getConstraints(tempTitles, constrains,count,para);
        // 在这里的时候我就获得了where之后的内容
        StringBuilder sql = new StringBuilder("select ");
        for (int i = 0; i < columns.size(); i++) {
            sql.append(columns.get(i));
            if (i != columns.size() - 1) {
                sql.append(",");
            }
        }

        sql.append(" from ");
        sql.append(table_name);
        if (constrains[0] != null)
            sql.append(constrains[0]);

        ResultSet rs = statement.executeQuery(sql.toString());
        ResultSetMetaData rsmd = rs.getMetaData();

        // true_titles里面存放的是实际需要的列
        ArrayList<String> true_titles = new ArrayList<>();

        ArrayList<Integer> lengths = new ArrayList<>();
        for (int i = 0; i < rsmd.getColumnCount(); i++) {
            lengths.add(rsmd.getColumnName(i + 1).length());
        }

        int title_cnt = rsmd.getColumnCount();

        for (int i = 0; i < title_cnt; i++) {

            sb.append(rsmd.getColumnName(i + 1));
            true_titles.add(rsmd.getColumnName(i + 1));
            if (i !=
                    title_cnt - 1) {
                sb.append(",");
                for (int j = 0; j < 30 - lengths.get(i); j++) {
                    sb.append(" ");
                }

            }
        }
        sb.append("\n");
        // 表头写好了

        rs.next();
        while (rs.getRow() != 0) {
            ArrayList<String> write_in = new ArrayList<>();
            for (String tempTitle : true_titles) {
                /**
                 * 这里没写好，返回的值为null时，我也会往write_in里面添加数据，会出错
                 */
                if (check(tempTitle)) {
                    if (String.valueOf(rs.getInt(tempTitle)) != null) {
                        write_in.add(String.valueOf(rs.getInt(tempTitle)));
                    } else {
                        write_in.add("null");
                    }
                } else {
                    if (rs.getString(tempTitle) != null) {
                        write_in.add(rs.getString(tempTitle));
                    } else write_in.add("null");
                }
            }

            for (int i = 0; i < write_in.size(); i++) {
                sb.append(write_in.get(i));
                if (i != write_in.size() - 1) {
                    sb.append(",");
                    for (int j = 0; j < 30 - write_in.get(i).length(); j++) {
                        sb.append(" ");
                    }

                }
            }
            sb.append("\n");
            rs.next();
        }
        con.commit();
        if (statement != null) statement.close();
        outputSelect.setText(sb.toString());
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

    private static int getConstraints(ArrayList<String> tempTitles, String[] constrains,int count,String[]para) {
        ArrayList<String> arrayList = new ArrayList<>();
        System.out.println("Please choose the constrains\n" +
                "If the input is null,it means that this attribute doesn't have a constraint\n" +
                "If the the type of column is 'age' 'unit_price' 'purchase_price' 'quantity' 'id',please input two numbers a,b meaning [a,b]\n" +
                "Otherwise, please input a string to satisfy your attempt.\n" +
                "And please input 'stop' to finish the input\n");
        // 我要做什么？ 我要给where后面添加语句。
        // 怎么添加？ 当有限制的时候就要添加。
        // 怎么判断有没有限制？ 用has_value来确定此处有没有限制,如果has_value对应的索引值是true，那么说明此值有限制,否则无限制，如果一个限制都没有，那么直接返回即可
        // 具体的值应该怎么添加？ 如果是varchar类型，直接where column = value即可，如果是integer类型, between and
        ArrayList<String> values = new ArrayList<>();
        for (String tempTitle : tempTitles) {
            System.out.print(tempTitle + "     ");
        }
        System.out.println();
        String temp;
        int cnt_for_title = 0;
        int cnt = 0;
        while (!(temp = para[count++]).equals("stop")) {
            if (temp.equals("null")) {
                cnt_for_title++;
                continue;
            }
            arrayList.add(tempTitles.get(cnt_for_title));
            if (!check(tempTitles.get(cnt_for_title))) {
                values.add(temp);
            } else {
                values.add(temp);
                temp = para[count];
                count++;
                values.add(temp);
            }
            cnt_for_title++;
            cnt++;
        }
        int value_cnt = 0;
        for (int i = 0; i < cnt; i++) {
            if (i == 0) {
                constrains[0] = " where ";
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
        return count;
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
        return (s.equals("age") || s.equals("purchase_price") || s.equals("quantity") || s.equals("unit_price") || s.equals("id"));
    }

    public void finishInsert(ActionEvent actionEvent) throws SQLException {
        insert();
    }

    public void finishDelete(ActionEvent actionEvent) throws SQLException {
        delete();
    }


    public void finishUpdate(ActionEvent actionEvent) throws SQLException {
        update();
    }

    public void finishSelect(ActionEvent actionEvent) throws IOException, SQLException {
        select();
    }

}

