package Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;

/**
 * Класс, хранящий в себе коллекцию сцен и их контроллеров
 */
public class SceneController {
    private HashMap<String, Scene> sceneMap = new HashMap<>();
    private Stage stage;

    public SceneController(Stage stage) {
        this.stage = stage;
    }

    /**
     * Добавить сцену
     * @param name имя сцены
     * @param scene сама сцена
     */
    public void addScene(String name, Scene scene){
        sceneMap.put(name, scene);
    }

    /**
     * Достать сцену по имени
     */
    public Scene getScene(String name){
        return sceneMap.get(name);
    }

    /**
     *Удалить сцену по имени
     */
    public void removeScene(String name) {
        sceneMap.remove(name);
    }

    /**
     * Загрузить сцену с необходимым контроллером
     * @param controller контроллек
     * @param name имя сцены
     * @param path путь к FXML
     */
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
