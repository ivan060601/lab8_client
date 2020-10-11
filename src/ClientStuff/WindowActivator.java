package ClientStuff;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Интерфейс для всех классов-контроллеров FXML
 */
public interface WindowActivator {

    /**
     * Создать всплывающее окно типа "Ошибка"
     * @param title название окна
     * @param message текст во всплывающем окне
     */
    default void makeAlert(String title, String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Создать всплывающее окно типа "Сообщение пользователю"
     * @param title название окна
     * @param message текст во всплывающем окне
     */
    default void makeNotification(String title, String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Создать всплывающее окно типа "Диалог"
     * @param title название окна
     * @param message текст во всплывающем окне
     * @return текст, введенный пользователем
     */
    default String makeTextDialogue(String title, String message){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(message);
        dialog.setContentText("");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            return result.get();
        }
        return null;
    }
}
