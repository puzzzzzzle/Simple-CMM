package zhangtao.iss2015.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("CMMGUI.fxml")
        );
        Parent root = fxmlLoader.load();
        Controller controller = fxmlLoader.getController();
        controller.setStage(primaryStage);
        primaryStage.setTitle("CMM解释器");
//        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 900, 540));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
