//package application;
//
//import javafx.application.Application;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.layout.Pane;
//import javafx.stage.Stage;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//
//import com.sun.org.apache.bcel.internal.generic.NEW;
//import javafx.animation.FadeTransition;
//import javafx.application.Application;
//import javafx.event.ActionEvent;
//import javafx.event.EventHandler;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.GridPane;
//import javafx.scene.paint.Color;
//import javafx.scene.paint.Paint;
//import javafx.scene.text.Font;
//import javafx.scene.text.Text;
//import javafx.stage.Stage;
//import javafx.util.Duration;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.Properties;
//
//public class database extends Application {
//    //数据库的部分
//    private static Connection con = null;
//    private static Statement statement = null;
//    Properties defprop = new Properties();
//    private static boolean verbose = false;
//
//    @Override
//    public void start(Stage primaryStage) throws Exception {
//        Label l_name = new Label("User：");
//        l_name.setFont(new Font(20));//字体大小
//        Label l_pswd = new Label("Password：");
//        l_pswd.setFont(new Font(20));
//
//        TextField t_name = new TextField();
//        PasswordField p_pswd = new PasswordField();
//        Button login = new Button("login in");
//
//        GridPane gr = new GridPane();
//
//        gr.setStyle("-fx-background-color: #efead0");
//        gr.add(l_name, 0, 0);
//        gr.add(t_name, 1, 0);
//        gr.add(l_pswd, 0, 1);
//        gr.add(p_pswd, 1, 1);
//        gr.add(login, 0, 2);
//
//        gr.setAlignment(Pos.CENTER);
//        gr.setHgap(10);//设置水平间距
//        gr.setVgap(17);//设置垂直间距
//        GridPane.setMargin(login, new Insets(0, 0, 0, 120));
//        //登录
//        login.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {
//                String name = t_name.getText();
//                String pswd = p_pswd.getText();
//                //连接数据库
//                defprop.put("host", "localhost");
//                defprop.put("user", name);//////////////////////pmy需要改
//                defprop.put("password", pswd);//////////////////////pmy需要改
//                defprop.put("database", "cs3072");//////////////////////pmy需要改
//                Properties prop = new Properties(defprop);
//                openDB(prop.getProperty("host"), prop.getProperty("database"),
//                        prop.getProperty("user"), prop.getProperty("password"));
//                System.out.println("Welcome!" + name+"!");
//                mywindow MWD = new mywindow(name, pswd);
//                primaryStage.close();
//
//            }
//
//        });
//
//
//        Scene scene = new Scene(gr);
//
//
//        primaryStage.setScene(scene);
//        primaryStage.setTitle("Java FX - 登录页面 ");
//        primaryStage.setWidth(500);
//        primaryStage.setHeight(300);
//        primaryStage.setResizable(false); //登录窗口的大小不允许改变
//        primaryStage.show();
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//
//    //打开数据库
//    public static void openDB(String host, String dbname,
//                              String user, String pwd) {
//        try {
//            //
//            Class.forName("org.postgresql.Driver");
//        } catch (Exception e) {
//            System.err.println("Cannot find the Postgres driver. Check CLASSPATH.");
//            System.exit(1);
//        }
//        String url = "jdbc:postgresql://" + host + "/" + dbname;
//        Properties props = new Properties();
//        props.setProperty("user", user);
//        props.setProperty("password", pwd);
//        try {
//            // 这里是连接数据库的部分
//            con = DriverManager.getConnection(url, props);
//            if (verbose) {
//                System.out.println("Successfully connected to the database "
//                        + dbname + " as " + user);
//            }
//            // 这里设置成false则是让数据先写入batch，写入量到了一定程度之后再发送
//            con.setAutoCommit(false);
//        } catch (SQLException e) {
//            System.err.println("Database connection failed");
//            System.err.println(e.getMessage());
//            System.exit(1);
//        }
//
//
//    }
//
//
//    public static void closeDB() {
//        if (con != null) {
//            try {
//                con.close();
//                con = null;
//            } catch (Exception e) {
//                // Forget about it
//            }
//        }
//    }
//}
//class Window{
//    private final Stage stage = new Stage();
//}
