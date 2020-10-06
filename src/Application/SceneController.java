package Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;

public class SceneController {
    private HashMap<String, Scene> sceneMap = new HashMap<>();
    private Stage stage;
    //private FXMLLoader loader = new FXMLLoader();

    public SceneController(Stage stage) {
        this.stage = stage;
    }

    public void addScene(String name, Scene scene){
        sceneMap.put(name, scene);
    }

    public Scene getScene(String name){
        return sceneMap.get(name);
    }

    public void removeScene(String name){
        sceneMap.remove(name);
    }

    public void activate(String name){
        stage.setScene(sceneMap.get(name));
        stage.setTitle(name);
    }

    public void loadWithController(Object controller, String name, String path){
        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
        loader.setController(controller);
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene s_login = new Scene(root, 330, 371);
        addScene(name, s_login);
    }

    public Stage getStage(){
        return stage;
    }
}
