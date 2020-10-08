package ClientStuff;

import Application.Locale.Localizator;
import Application.SceneController;
import CityStructure.City;
import CityStructure.Human;
import ClientStuff.Checkers.CheckParameter;
import ClientStuff.Checkers.NullPointerChecker;
import ClientStuff.Checkers.WrongFieldChecker;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;

/**
 * Класс, реализующий все клиентские методы
 */
public class Client implements WindowActivator{
    @FXML
    public TextField server_field;
    public TextField port_field;
    public Button connect_button;
    public MenuBar lang_changer;
    public MenuItem change_ru;
    public MenuItem change_cz;
    public MenuItem change_hr;
    public MenuItem change_en;
    public Text text_server;
    public Text text_port;

    private SocketAddress inetSocket;
    private Scanner scanner = new Scanner(System.in);
    private DatagramSocket datagramSocket;
    private DatagramPacket input;
    private DatagramPacket output;
    private byte[] buffer = new byte[64000];
    private Gson gson = new Gson();
    private NullPointerChecker np = new NullPointerChecker();
    private WrongFieldChecker wf = new WrongFieldChecker();
    public static SceneController sceneController;
    public static Stage mainStage;
    private Locale currentLocale = Localizator.DEFAULT;

    public Client(){

    }

    public Client(SceneController sceneController, Stage stage){
        this.mainStage = stage;
        this.sceneController = sceneController;
        mainStage.setWidth(300);
        mainStage.setHeight(371);
    }

    /**
     * Метод для определения русских букв в строке
     * @param str Строка, для анализа
     * @return Результат (True - содержит русские буквы, False - нет)
     */
    public boolean isRussian(String str)
    {
        char[] chr = str.toCharArray();
        for (int i = 0; i < chr.length; i++)
        {
            if (chr[i] >= 'А' && chr[i] <= 'я')
                return true;
        }
        return false;
    }

    /**
     * Метод для получения СТРОКИ
     * @return строка, полученная с сервера
     */
    public String getLine(){
        Arrays.fill(buffer, (byte) 0);
        input = new DatagramPacket(buffer, buffer.length);
        try {
            datagramSocket.receive(input);
        } catch (IOException e) {
            makeAlert("Error", "Error while receiving message from server. Try later");
            System.out.println("Error while receiving message from server. Try later");
        }
        byte[] b = input.getData();
        return new String(b);
    }

    /**
     * Метод для записи СТРОКИ на сервер
     * @param s строка, которую надо записать
     */
    public void writeLine(String s) {
        output = new DatagramPacket(s.getBytes(), s.getBytes().length, inetSocket);
        try {
            datagramSocket.send(output);
        } catch (IOException e) {
            makeAlert("Error", "Error while sending message to server. Try later");
            System.out.println("Error while sending message to server. Try later");
        }
    }

    /**
     * Метод для записи МАССИВА ТИПА byte[] на сервер
     * @param array массив, который надо записать
     */
    public void writeBytes(byte[] array){
        output = new DatagramPacket(array, array.length, inetSocket);
        try {
            datagramSocket.send(output);
        } catch (IOException e) {
            makeAlert("Error", "Error while sending message to server. Try later");
            System.out.println("Error while sending message to server. Try later");
        }
    }

    /**
     * Метод для получения МАССИВА ТИПА byte[] с сервера
     * @return массив с сервера
     */
    public byte[] getBytes(){
        Arrays.fill(buffer, (byte) 0);
        input = new DatagramPacket(buffer, buffer.length);
        try {
            datagramSocket.receive(input);
        } catch (IOException e) {
            makeAlert("Error", "Error while receiving message from server. Try later");
            System.out.println("Error while receiving message from server. Try later");
        }
        return input.getData();
    }

    /**
     * Метод для получения ГОРОДА с сервера
     * @return город с сервера
     */
    public City getCity(){
        return byteArrayToCity(this.getLine().getBytes());
    }

    /**
     * Метод для записи ГОРОДА на сервер
     * @param city город с сервера
     */
    public void writeCity(City city){
        this.writeLine(cityToByteArray(city).toString());
    }

    /**
     * Метод для записи ГОРОДА на сервер из JSON-строки
     * @param s строка в формате JSON
     */
    public void writeCityFromJSON(String s){
        try {
            City c = gson.fromJson(s, City.class);
            np.checkEverything(c, CheckParameter.WITH_ASKING);
            wf.checkEverything(c, CheckParameter.WITH_ASKING);
            this.writeCity(c);
        }catch (JsonSyntaxException e){
            System.out.print("JSON Syntax error. ");
        }catch (NumberFormatException e){
            System.out.print("Some number-fields have incorrect values. ");
        }
    }

    /**
     * Метод для конвертации JSON-строки в City
     * @param s строка в формате JSON
     * @return город
     */
    public City makeCityFromJSON(String s){
        try {
            City c = gson.fromJson(s, City.class);
            np.checkEverything(c, CheckParameter.WITH_ASKING);
            wf.checkEverything(c, CheckParameter.WITH_ASKING);
            return c;
        }catch (JsonSyntaxException e){
            System.out.print("JSON Syntax error. ");
        }catch (NumberFormatException e){
            System.out.print("Some number-fields have incorrect values. ");
        }
        return null;
    }

    /**
     * Метод для конвертации JSON-строки в Human
     * @param s строка в формате JSON
     * @return Human
     */
    public Human makeHumanFromJSON(String s){
        try {
            Human human = gson.fromJson(s, Human.class);
            if (human.getBirthday() == null){
                System.out.println("Looks like governors birthday is null. Enter new one:");
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                boolean checker = false;
                while (!checker){
                    try {
                        human.setBirthday(format.parse(scanner.nextLine()));
                        checker = true;
                    } catch (ParseException e) {
                        System.out.println("Wrong date format. Try yyyy-MM-dd");
                    }
                }
            }
            return human;
        }catch (JsonSyntaxException e){
            System.out.print("JSON Syntax error. ");
        }catch (NumberFormatException e){
            System.out.print("Some number-fields have incorrect values. ");
        }
        return null;
    }

    /**
     * Метод для записи КОМАНДЫ на сервер
     * @param command команда для записи
     */
    public void writeCommand(Command command){
        this.writeBytes(commandToByteArray(command));
    }

    /**
     * Команда для получения ОТВЕТА с сервера
     * @return ответ с сервера
     */
    public Respond getRespond(){
        return byteArrayToRespond(getBytes());
    }

    private byte[] cityToByteArray(City city) {
        return SerializationUtils.serialize(city);
    }

    private City byteArrayToCity(byte[] array) {
        return SerializationUtils.deserialize(array);
    }

    private byte[] commandToByteArray(Command command){
        return SerializationUtils.serialize(command);
    }

    private Command byteArrayToCommand(byte[] array){
        return SerializationUtils.deserialize(array);
    }

    private byte[] respondToByteArray(Respond respond){
        return SerializationUtils.serialize(respond);
    }

    private Respond byteArrayToRespond(byte[] array){
        return SerializationUtils.deserialize(array);
    }

    /**
     * Метод обработки нажатия на кнопку "Подключиться"
     */
    @FXML
    public void button_connect_clicked(ActionEvent actionEvent) {
        String server = server_field.getText();
        try {
            int port = Integer.parseInt(port_field.getText().trim());
            if (check_port(port)) {
                try {
                    inetSocket = new InetSocketAddress(server, port);
                    datagramSocket = new DatagramSocket();
                    datagramSocket.setSoTimeout(3000);
                    System.out.println("You can start working");
                    sceneController.loadWithController(new User(this), "Login", "/Application/FXMLs/login_window.fxml");
                    sceneController.getStage().setScene(sceneController.getScene("Login"));
                } catch (SocketException e) {
                    makeAlert("Server is unreachable", "Сервер недоступен");
                }
            } else {
                makeAlert("Wrong port format", "Порт должен быть от 1024 до 65535");
            }
        }catch (NumberFormatException e){
            makeAlert("Wrong port format", "Порт должен быть целым числом");
        }
    }

    private boolean check_port(int port){
        return (port < 1024 || port > 65535) ? false : true;
    }

    /**
     * Фильтр для поля "Порт", учитывающий только числа
     * @param keyEvent событие - кнопка нажата(typed)
     */
    @FXML
    public void port_key_typed(KeyEvent keyEvent) {
        char enteredValue = keyEvent.getCharacter().toCharArray()[0];
        if(Character.isLetter(enteredValue)) {
            port_field.setEditable(false);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Wrong port format");
            alert.setHeaderText(null);
            alert.setContentText("В порте не должно быть букв");
            alert.showAndWait();
        }else {
            port_field.setEditable(true);
        }
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
        newBundle = Localizator.changeLocale("Application.Locale.Connection.ConnectionResources", currentLocale);

        text_server.setText((String) newBundle.getObject("Server"));
        text_port.setText((String) newBundle.getObject("Port"));
        connect_button.setText((String) newBundle.getObject("Next ->"));

        Locale.setDefault(currentLocale);
    }

    @FXML
    public void initialize() {
        ResourceBundle newBundle = Localizator.changeLocale("Application.Locale.Connection.ConnectionResources", Locale.getDefault());
        text_server.setText((String) newBundle.getObject("Server"));
        text_port.setText((String) newBundle.getObject("Port"));
        connect_button.setText((String) newBundle.getObject("Next ->"));
        Locale.setDefault(currentLocale);
    }

    @FXML
    public void onEnter(ActionEvent ae){
        button_connect_clicked(ae);
    }

    public static SceneController getSceneController() {
        return sceneController;
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void setSceneController(SceneController sceneController) {
        Client.sceneController = sceneController;
    }
}