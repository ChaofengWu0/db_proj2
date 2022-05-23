
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class WebViewDemo extends Application {


    public void start(Stage primaryStage) {
        primaryStage.setTitle("JavaFX WebView Example");

        // JavaFX WebView is capable of showing web pages (HTML, CSS, JavaScript) inside a JavaFX application.
        // Consider JavaFX WebView as a mini browser
        WebView webView = new WebView();

        // WebView manages a WebEngine and displays its content.
        // The associated WebEngine is created automatically at construction time and cannot be changed afterwards
        webView.getEngine().load("http://www.bing.com");

        // WebView is a Node so it can be included in the scene graph like any other JavaFX nodes
        Scene scene = new Scene(webView, 960, 600);

        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
