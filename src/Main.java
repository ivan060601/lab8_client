import ClientStuff.Client;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.NoSuchElementException;
import Application.SceneController;

public class Main extends Application {
    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage stage){
        stage.setResizable(false);
        stage.setOnCloseRequest(e -> System.exit(0));
        SceneController sceneController = new SceneController(stage);
        try {
            sceneController.loadWithController(new Client(sceneController, stage), "Connection", "/Application/FXMLs/connect_window.fxml");
            stage.setScene(sceneController.getScene("Connection"));
            stage.setWidth(300);
            stage.setHeight(371);
            stage.show();
        } catch (Exception e){
            stage.setScene(sceneController.getScene("Connection"));
            sceneController.removeScene("Login");
            sceneController.removeScene("Main");
            /*
            e.printStackTrace();
            System.out.println("Internal error occurred. Client will be shut down now.");
            System.exit(0);*/
        }
    }
}