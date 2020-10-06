package ClientStuff;

import Application.AddWindowManager;
import Application.SceneController;
import Application.UpdateWindowManager;
import CityStructure.City;
import CityStructure.CityTree;
import CityStructure.Human;
import CityStructure.StandardOfLiving;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.SerializationException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Terminal implements WindowActivator {
    @FXML
    public Text username_text;
    public Button logout_button;
    public MenuBar lang_changer;
    public MenuItem change_ru;
    public MenuItem change_cz;
    public MenuItem change_hr;
    public MenuItem change_en;
    public Button command_help;
    public Button command_info;
    public Button command_add;
    public Button command_update;
    public Button command_remove;
    public Button command_clear;
    public Button command_add_if_max;
    public Button command_remove_lower;
    public Button command_history;
    public Button command_remove_any_by_governor;
    public Button command_filter_name;
    public Button command_execute_script;
    public Tab table_tab;
    public TableView<City> main_table;
    public TableColumn<City, Long> id_column;
    public TableColumn<City, String> name_column;
    public TableColumn<City, Integer> population_column;
    public TableColumn<City, String> owner_column;
    public TableColumn<City, StandardOfLiving> sof_column;
    public TableColumn<City, LocalDate> est_column;
    public TableColumn<City, Long> carcode_column;
    public TableColumn<City, Float> area_column;
    public TableColumn<City, Double> y_column;
    public TableColumn<City, Float> x_column;
    public Tab visualisation_tab;

    private Client client;
    private Scanner scanner = new Scanner(System.in);
    private String newLine = new String();
    private Command command = new Command();
    private Command updater = new Command();
    private Thread updateThread;
    private boolean updateThreadFlag;
    private boolean serverFlag = true;
    private SceneController sceneController = client.getSceneController();
    private String username;
    private CityTree cityTree;
    private User user;
    private ObservableList<City> observableList = FXCollections.observableArrayList();
    CityTree collectionToShow;
    private String FILTER = "";

    public Terminal(User user) {
        this.user = user;
        client = user.getClient();
        command.setUser(user);
        client.getMainStage().setHeight(580);
        client.getMainStage().setWidth(900);
        username = user.getLogin();
        updater.setEverything("show", null);
    }

    public void start() {
        try {
            while (true) {
                newLine = scanner.nextLine().trim();
                String[] arr = newLine.split(" ", 2);
                if (arr.length == 1) {
                    commandManager(arr[0], null);
                } else {
                    commandManager(arr[0], arr[1]);
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("End of input");
            System.exit(0);
        }
    }

    private void commandManager(String arr1, String arr2) {
        try {
            switch (arr1) {
                case "exit":
                    System.out.println("Client will be terminated now");
                    System.exit(0);
                    break;
                case "add":
                case "add_if_max":
                case "remove_lower":
                    String json = readJSON(arr2);
                    System.out.println("json is: " + json);
                    if (json != null) {
                        City c = client.makeCityFromJSON(json);
                        if (c != null) {
                            command.setEverything(arr1, c);
                        } else {
                            command.setEverything(arr1, null);
                        }
                        client.writeCommand(command);
                    } else {
                        command.setEverything(arr1, null);
                        client.writeCommand(command);
                    }
                    break;
                case "remove_any_by_governor":
                    if (arr2 != null) {
                        command.setEverything(arr1, client.makeHumanFromJSON(arr2));
                        client.writeCommand(command);
                    } else {
                        command.setEverything(arr1, null);
                        client.writeCommand(command);
                    }
                    break;
                case "execute_script":
                    command.setEverything(arr1, arr2);
                    client.writeCommand(command);
                    break;
                case "update":
                    if (arr2 != null) {
                        String args[] = arr2.split(" ", 2);
                        if (args.length == 1){
                            command.setEverything("update", null);
                            boolean numberChanged = false;
                            while (!numberChanged) {
                                try {
                                    command.setAdditional(Long.parseLong(args[0]));
                                    numberChanged = true;
                                } catch (NumberFormatException e) {
                                    System.out.println("Entered ID is not a number. Enter new one:");
                                    args[0] = scanner.nextLine();
                                }
                            }
                            System.out.println("Entered city is null. Enter new one:");
                            command.setArgument(client.makeCityFromJSON(readJSON(scanner.nextLine())));
                        }else{
                            command.setEverything("update", null);
                            boolean numberChanged = false;
                            while (!numberChanged) {
                                try {
                                    command.setAdditional(Long.parseLong(args[0]));
                                    numberChanged = true;
                                } catch (NumberFormatException e) {
                                    System.out.println("Entered ID is not a number. Enter new one:");
                                    args[0] = scanner.nextLine();
                                }
                            }
                            command.setArgument(null);
                            command.setArgument(client.makeCityFromJSON(args[1]));
                        }
                    }else {
                        command.setEverything("update", null);
                        command.setAdditional(null);
                    }
                    client.writeCommand(command);
                    break;
                default:
                    command.setEverything(arr1, arr2);
                    client.writeCommand(command);
                    break;
            }
            System.out.println(client.getRespond().getMsg());
        }catch (SerializationException e){
            System.out.println("Error occurred while receiving message from the server. Try later");
        }

    }

    private String readJSON(String beginning){
        String toReturn = "";
        int open = 0;
        int close = 0;
        if (beginning != null) {
            if (beginning.contains("{")) {
                open++;
                toReturn = beginning;
                if (beginning.contains("}")) {
                    close++;
                }
                while (open != close) {
                    if (scanner.hasNext()) {
                        String s = scanner.nextLine();
                        if (s.contains("{")) {
                            open++;
                            toReturn = toReturn + s;
                        }
                        if (s.contains("}")) {
                            close++;
                            toReturn = toReturn + s;
                        } else {
                            toReturn = toReturn + s;
                        }
                    } else {
                        System.out.println("End of input");
                        System.exit(0);
                    }
                }
            } else {
                toReturn = beginning;
            }
        }else {
            toReturn = null;
        }

        return toReturn;
    }

    @FXML
    public void changeLanguage(ActionEvent actionEvent) {

    }

    @FXML
    public void command_help_execute(ActionEvent actionEvent) {
        if(serverFlag) {
            command.setEverything("help", null);
            client.writeCommand(command);
            client.makeNotification("Help", client.getRespond().getMsg());
        }else {
            logout();
        }
    }

    @FXML
    public void command_info_execute(ActionEvent actionEvent) {
        if(serverFlag) {
            command.setEverything("info", null);
            client.writeCommand(command);
            client.makeNotification("Info", client.getRespond().getMsg());
        }else {
            logout();
        }
    }

    @FXML
    public void command_add_execute(ActionEvent actionEvent) {
        if(serverFlag) {
            Stage smallStage = new Stage();
            SceneController smallSceneController = new SceneController(smallStage);
            AddWindowManager addWindowManager = new AddWindowManager(this.user, smallStage);
            smallSceneController.loadWithController(addWindowManager, "Add city", "/Application/FXMLs/create_city_window.fxml");
            smallStage.setScene(smallSceneController.getScene("Add city"));
            smallStage.setResizable(false);
            smallStage.setTitle("Add new city");
            smallStage.setHeight(450);
            smallStage.setWidth(350);
            smallStage.show();
            smallStage.setOnCloseRequest(event -> smallSceneController.removeScene("Add city"));
        }else {
            logout();
        }
    }

    @FXML
    public void command_update_execute(ActionEvent actionEvent) {
        if(serverFlag) {
            Stage smallStage = new Stage();
            SceneController smallSceneController = new SceneController(smallStage);
            ArrayList<City> cityTree_byOwner =
                    cityTree.stream()
                            .filter(city1 -> city1.getOwner().equals(user.getLogin()))
                            .collect(Collectors.toCollection(ArrayList::new));
            try {
                long id = Long.parseLong(makeTextDialogue("Update by id","Enter id"));
                ArrayList<City> idFiltered = cityTree_byOwner.stream().filter(city1 -> city1.getId() == id).collect(Collectors.toCollection(ArrayList::new));
                if (idFiltered.size() > 0){
                    UpdateWindowManager updateWindowManager = new UpdateWindowManager(user, idFiltered.get(0), smallStage);
                    smallSceneController.loadWithController(updateWindowManager, "Update city", "/Application/FXMLs/create_city_window.fxml");
                    smallStage.setScene(smallSceneController.getScene("Update city"));
                    smallStage.setResizable(false);
                    smallStage.setTitle("Update city");
                    smallStage.setHeight(450);
                    smallStage.setWidth(350);
                    smallStage.show();
                }
            }catch (NumberFormatException | NullPointerException e){
                makeAlert("Invalid id", "Invalid id");
            }

        }else {
            logout();
        }
    }

    @FXML
    public void command_remove_execute(ActionEvent actionEvent) {
        if(serverFlag){
            try {
                command.setEverything("remove_by_id", makeTextDialogue("Remove by ID", "Введите ID"));
                client.writeCommand(command);
                client.makeNotification("Remove by id", client.getRespond().getMsg());
            }catch (NumberFormatException | NullPointerException e){
                makeAlert("Invalid ID", "Invalid ID");
            }
        }else {
            logout();
        }
    }

    @FXML
    public void command_clear_execute(ActionEvent actionEvent) {
        if(serverFlag) {
            command.setEverything("clear", null);
            client.writeCommand(command);
            client.makeNotification("Clear", client.getRespond().getMsg());
        }else {
            logout();
        }
    }

    @FXML
    public void command_execute_script_execute(ActionEvent actionEvent) {
        if(serverFlag) {
            command.setEverything("execute_script", makeTextDialogue("Execute script", "Введите имя скрипта"));
            client.writeCommand(command);
            client.makeNotification("Execute script", client.getRespond().getMsg());
        }else {
            logout();
        }
    }

    @FXML
    public void command_add_if_max_execute(ActionEvent actionEvent) {

    }

    @FXML
    public void command_remove_lower_execute(ActionEvent actionEvent) {

    }

    @FXML
    public void command_history_execute(ActionEvent actionEvent) {
        if(serverFlag) {
            command.setEverything("history", null);
            client.writeCommand(command);
            client.makeNotification("History", client.getRespond().getMsg());
        }else {
            logout();
        }
    }

    @FXML
    public void command_remove_any_by_governor_execute(ActionEvent actionEvent) {
        Human governor;
        if(serverFlag) {
            try {
                String dateInString = makeTextDialogue("Remove any by governor", "Введите день рождения градоначальника в формате yyyy-MM-dd");
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                governor = new Human(format.parse(dateInString));
                command.setEverything("remove_any_by_governor", governor);
                client.writeCommand(command);
                client.makeNotification("Remove any by governor", client.getRespond().getMsg());
            } catch (ParseException e) {
                makeAlert("Wrong birthday format", "Неправильный формат дня рождения");
            }
        }else {
            logout();
        }
    }

    @FXML
    public void command_filter_name_execute(ActionEvent actionEvent) {
        FILTER = makeTextDialogue("Filter starts with name", "Enter filter");
    }

    @FXML
    public void logout(ActionEvent actionEvent) {
        client.getMainStage().setScene(sceneController.getScene("Connection"));
        client.getMainStage().setWidth(300);
        client.getMainStage().setHeight(371);
        sceneController.removeScene("Login");
        sceneController.removeScene("Main");
        updateThreadFlag = false;
    }


    public void logout() {
        makeAlert("Server unreachable", "");
        client.getMainStage().setScene(sceneController.getScene("Connection"));
        client.getMainStage().setWidth(300);
        client.getMainStage().setHeight(371);
        sceneController.removeScene("Login");
        sceneController.removeScene("Main");
        updateThreadFlag = false;
    }

    @FXML
    public void initialize() {
        username_text.setText(username);
        client.writeCommand(updater);
        cityTree = (CityTree) client.getRespond().getSecondParameter();

        updateThreadFlag = true;
        updateThread = new Thread(() ->{
            synchronized (cityTree) {
                while (updateThreadFlag) {
                    try {
                        client.writeCommand(updater);
                        cityTree = (CityTree) client.getRespond().getSecondParameter();
                        collectionToShow = cityTree.stream().filter(city -> city.getName().startsWith(FILTER)).collect(Collectors.toCollection(CityTree::new));
                        observableList.setAll(collectionToShow);
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        serverFlag = false;
                        e.printStackTrace();
                    }
                }
            }
        } ){{start();}};

        id_column.setCellValueFactory(new PropertyValueFactory<City, Long>("id"));
        name_column.setCellValueFactory(new PropertyValueFactory<City, String>("name"));
        area_column.setCellValueFactory(new PropertyValueFactory<City, Float>("area"));
        owner_column.setCellValueFactory(new PropertyValueFactory<City, String>("owner"));
        carcode_column.setCellValueFactory(new PropertyValueFactory<City, Long>("carCode"));
        population_column.setCellValueFactory(new PropertyValueFactory<City, Integer>("population"));
        est_column.setCellValueFactory(new PropertyValueFactory<City, LocalDate>("establishmentDate"));
        sof_column.setCellValueFactory(new PropertyValueFactory<City, StandardOfLiving>("standardOfLiving"));
        x_column.setCellValueFactory(new PropertyValueFactory<City, Float>("x"));
        y_column.setCellValueFactory(new PropertyValueFactory<City, Double>("y"));
        main_table.setItems(observableList);
    }
}