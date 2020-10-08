package ClientStuff;

import Application.Locale.Localizator;
import Application.SceneController;
import com.sun.xml.internal.ws.developer.Serialization;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.commons.lang3.SerializationException;

import javafx.scene.text.Text;
import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;

public class User implements Serializable, WindowActivator {
    final static long serialVersionUID = 1L;
    private transient static Scanner scanner = new Scanner(System.in);
    private transient Client client;
    private transient SceneController sceneController;
    private transient Stage mainStage;
    private String login;
    private String password;
    private transient Locale currentLocale = Localizator.DEFAULT;
    private transient String serverUnreachable = "Server is unreachable";
    private transient String nullFound = "Null field noticed";
    private transient String noNullFields = "Fields shouldn't be null";

    @FXML
    public transient MenuItem change_ru;
    public transient MenuItem change_cz;
    public transient MenuItem change_hr;
    public transient MenuItem change_en;
    public transient PasswordField pass_field;
    public transient TextField login_field;
    public transient Button register_button;
    public transient Button connect_button;
    public transient Text text_login;
    public transient Text text_pass;

    private User(String login, String password, Client client){
        this.client = client;
        this.login = login;
        this.password = password;
        this.sceneController = client.getSceneController();
        this.mainStage = client.getMainStage();
        mainStage.setWidth(300);
        mainStage.setHeight(371);
    }

    public  User(Client client){
        this.client = client;
        this.sceneController = client.getSceneController();
        this.mainStage = client.getMainStage();
        mainStage.setWidth(300);
        mainStage.setHeight(371);
    }

    public String getLogin() {
        return login;
    }

    public Client getClient() {
        return client;
    }

    @FXML
    public void changeLanguage(ActionEvent actionEvent) {
        Object menuItem = actionEvent.getSource();
        ResourceBundle newBundle = null;
        if (change_ru.equals(menuItem)){
            currentLocale = Localizator.RUSSIA_RUSSIA;
        }else if (change_cz.equals(menuItem)){
            currentLocale = Localizator.CZECH_CZECH;
        }else if (change_en.equals(menuItem)){
            currentLocale = Localizator.ENGLISH_CANADA;
        }else if (change_hr.equals(menuItem)){
            currentLocale = Localizator.CROATIAN_CROATIA;
        }
        newBundle = Localizator.changeLocale("Application.Locale.Login.LoginResources", currentLocale);

        text_login.setText((String) newBundle.getObject("Login"));
        text_pass.setText((String) newBundle.getObject("Password"));
        register_button.setText((String) newBundle.getObject("Sign up"));
        connect_button.setText((String) newBundle.getObject("Sign in"));
        serverUnreachable = (String) newBundle.getObject("Server is unreachable");
        nullFound = (String) newBundle.getObject("Null field noticed");
        noNullFields = (String) newBundle.getObject("Fields shouldn't be null");

        Locale.setDefault(currentLocale);
    }

    private void changeLanguage(){
        ResourceBundle newBundle = Localizator.changeLocale("Application.Locale.Login.LoginResources", Locale.getDefault());
        text_login.setText((String) newBundle.getObject("Login"));
        text_pass.setText((String) newBundle.getObject("Password"));
        register_button.setText((String) newBundle.getObject("Sign up"));
        connect_button.setText((String) newBundle.getObject("Sign in"));
        serverUnreachable = (String) newBundle.getObject("Server is unreachable");
        nullFound = (String) newBundle.getObject("Null field noticed");
        noNullFields = (String) newBundle.getObject("Fields shouldn't be null");
    }

    @FXML
    public void button_connect_clicked(ActionEvent actionEvent) {
        String l = login_field.getText();
        String p = pass_field.getText();
        String[] params = new String[]{l, p};
        if (!l.equals("") && !p.equals("")) {
            try {
                client.writeCommand(new Command<>("login", params));
                String msg = client.getRespond().getMsg();
                if (msg.equals("Logged in successfully")) {
                    setLogin(l);
                    setPassword(p);
                    sceneController.loadWithController(new Terminal(this), "Main", "/Application/FXMLs/main_window.fxml");
                    mainStage.setScene(sceneController.getScene("Main"));
                } else {
                    makeAlert(msg, msg);
                }
            } catch (NullPointerException | SerializationException e) {
                makeAlert(serverUnreachable, serverUnreachable);
                e.printStackTrace();
            }
        }else {
            makeAlert(nullFound, noNullFields);
        }
    }

    @FXML
    public void button_register_clicked(ActionEvent actionEvent) {
        String l = login_field.getText();
        String p = pass_field.getText();
        String[] params = new String[]{l, p};
        if (!l.equals("") && !p.equals("")) {
            try {
                client.writeCommand(new Command<>("register", params));
                String msg = client.getRespond().getMsg();
                makeNotification(msg, msg);
            } catch (NullPointerException | SerializationException e) {
                makeAlert(serverUnreachable, serverUnreachable);
            }
        }else {
            makeAlert(nullFound, noNullFields);
        }
    }

    @FXML
    public void initialize() {
        changeLanguage();
    }

    private void setLogin(String login) {
        this.login = login;
    }

    private void setPassword(String password) {
        this.password = password;
    }
}
