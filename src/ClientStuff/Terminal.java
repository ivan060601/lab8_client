package ClientStuff;

import Application.AddWindowManager;
import Application.Locale.Localizator;
import Application.SceneController;
import Application.SmartCoordinates;
import Application.UpdateWindowManager;
import CityStructure.City;
import CityStructure.CityTree;
import CityStructure.Human;
import CityStructure.StandardOfLiving;
import javafx.collections.*;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.SerializationException;

import java.awt.geom.Arc2D;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Terminal implements WindowActivator {

    //Все переменные, связанные с интерфейсом
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
    public Canvas visualisation_canvas;

    //Переменные для исполнения команд
    private Client client;
    private Scanner scanner = new Scanner(System.in);
    private String newLine = new String();
    private Command command = new Command();
    private Command updater = new Command();
    private Thread updateThread;
    private Thread drawThread;
    private boolean updateThreadFlag;
    private boolean serverFlag = true;
    private SceneController sceneController = client.getSceneController();
    private String username;
    private CityTree cityTree;
    private User user;
    private TreeSet<Long> IDSet = new TreeSet<>();
    private ObservableList<City> observableList = FXCollections.observableArrayList();
    private String FILTER = "";
    private transient Locale currentLocale = Localizator.DEFAULT;
    private String invalidID = "Invalid ID";
    private String wrongBDFormat = "Wrong birthday format";
    private String enterFilter = "Enter filter";
    private String serverUnreachable = "Server unreachable";
    //private final Image image = new Image("Application/isu_naelsya.png");

    //Все переменные для окна визуализации
    private String noCities = "No available cities to show";
    private double CANVAS_HEIGHT;
    private double CANVAS_WIDTH;
    private float X_MAX_COORDINATE = 0;
    private double Y_MAX_COORDINATE = 0;
    private float X_MIN_COORDINATE = 0;
    private double Y_MIN_COORDINATE = 0;
    private ObservableMap<Long, SmartCoordinates> collectionToDraw = FXCollections.observableHashMap();
    private ObservableList<SmartCoordinates> coordinatesObservableList = FXCollections.observableArrayList();

    public Terminal(User user) {
        this.user = user;
        client = user.getClient();
        command.setUser(user);
        client.getMainStage().setHeight(580);
        client.getMainStage().setWidth(900);
        username = user.getLogin();
        updater.setEverything("show", null);
    }

    /*
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
    }*/

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
        Locale.setDefault(currentLocale);
        changeLanguage();
    }

    public void changeLanguage(){
        ResourceBundle newBundle = Localizator.changeLocale("Application.Locale.MainWindow.MainResources", Locale.getDefault());

        invalidID = (String) newBundle.getObject("Invalid ID");
        noCities = (String) newBundle.getObject("No available cities to show");
        wrongBDFormat = (String) newBundle.getObject("Wrong birthday format");
        enterFilter = (String) newBundle.getObject("Enter filter");
        serverUnreachable = (String) newBundle.getObject("Server unreachable");
        logout_button.setText((String) newBundle.getObject("Logout"));
        table_tab.setText((String) newBundle.getObject("Table"));
        visualisation_tab.setText((String) newBundle.getObject("Visualisation"));
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
            AddWindowManager addWindowManager = new AddWindowManager(this.user, smallStage, "add");
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
                ArrayList<City> idFiltered = cityTree_byOwner
                        .stream()
                        .filter(city1 -> city1.getId() == id)
                        .collect(Collectors.toCollection(ArrayList::new));
                if (idFiltered.size() > 0){
                    UpdateWindowManager updateWindowManager = new UpdateWindowManager(user, idFiltered.get(0), smallStage);
                    smallSceneController.loadWithController(updateWindowManager, "Update city", "/Application/FXMLs/create_city_window.fxml");
                    smallStage.setScene(smallSceneController.getScene("Update city"));
                    smallStage.setResizable(false);
                    smallStage.setTitle("Update city");
                    smallStage.setHeight(450);
                    smallStage.setWidth(350);
                    smallStage.show();
                }else {
                    makeNotification("City not found", "City with such ID was not found");
                }
            }catch (NumberFormatException | NullPointerException e){
                makeAlert(invalidID, invalidID);
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
                makeAlert(invalidID, invalidID);
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
            String temp = makeTextDialogue("Execute script", "Введите имя скрипта");
            if (temp != null) {
                command.setEverything("execute_script", temp);
                client.writeCommand(command);
                client.makeNotification("Execute script", client.getRespond().getMsg());
            }
        }else {
            logout();
        }
    }

    @FXML
    public void command_add_if_max_execute(ActionEvent actionEvent) {
        if(serverFlag) {
            Stage smallStage = new Stage();
            SceneController smallSceneController = new SceneController(smallStage);
            AddWindowManager addWindowManager = new AddWindowManager(this.user, smallStage, "add_if_max");
            smallSceneController.loadWithController(addWindowManager, "Add city if max", "/Application/FXMLs/create_city_window.fxml");
            smallStage.setScene(smallSceneController.getScene("Add city if max"));
            smallStage.setResizable(false);
            smallStage.setTitle("Add city if max");
            smallStage.setHeight(450);
            smallStage.setWidth(350);
            smallStage.show();
            smallStage.setOnCloseRequest(event -> smallSceneController.removeScene("Add city if max"));
        }else {
            logout();
        }
    }

    @FXML
    public void command_remove_lower_execute(ActionEvent actionEvent) {
        if(serverFlag) {
            Stage smallStage = new Stage();
            SceneController smallSceneController = new SceneController(smallStage);
            AddWindowManager addWindowManager = new AddWindowManager(this.user, smallStage, "remove_lower");
            smallSceneController.loadWithController(addWindowManager, "Remove lower", "/Application/FXMLs/create_city_window.fxml");
            smallStage.setScene(smallSceneController.getScene("Remove lower"));
            smallStage.setResizable(false);
            smallStage.setTitle("Remove lower");
            smallStage.setHeight(450);
            smallStage.setWidth(350);
            smallStage.show();
            smallStage.setOnCloseRequest(event -> smallSceneController.removeScene("Remove lower"));
        }else {
            logout();
        }
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
                makeAlert(wrongBDFormat, wrongBDFormat);
            } catch (NullPointerException e){

            }
        }else {
            logout();
        }
    }

    @FXML
    public void command_filter_name_execute(ActionEvent actionEvent) {
        String tempString = makeTextDialogue("Filter starts with name", enterFilter);
        if (tempString!=null){
            FILTER = tempString;
        }

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
        makeAlert(serverUnreachable, "");
        client.getMainStage().setScene(sceneController.getScene("Connection"));
        client.getMainStage().setWidth(300);
        client.getMainStage().setHeight(371);
        sceneController.removeScene("Login");
        sceneController.removeScene("Main");
        updateThreadFlag = false;
    }

    @FXML
    public void initialize() throws InterruptedException {
        changeLanguage();
        username_text.setText(username);
        cityTree = new CityTree();

        coordinatesObservableList.addListener(new ListChangeListener<SmartCoordinates>() {
            @Override
            public void onChanged(Change<? extends SmartCoordinates> c) {
                while (c.next()) {
                    if (c.wasAdded()) {
                        for (SmartCoordinates sc : c.getAddedSubList()) {
                            System.out.println(sc.getId() + " was added");
                        }
                    }
                    if (c.wasRemoved()) {
                        for (SmartCoordinates sc : c.getAddedSubList()) {
                            System.out.println(sc.getId() + " was removed");
                        }
                    }
                    if (c.wasUpdated()) {
                        for (SmartCoordinates sc : c.getAddedSubList()) {
                            System.out.println(sc.getId() + " was updated");
                        }
                    }
                }
            }
        });

        /*
        collectionToDraw.addListener(new MapChangeListener<Long, SmartCoordinates>() {
            @Override
            public void onChanged(Change<? extends Long, ? extends SmartCoordinates> change) {
                System.out.println(change.getKey());
            }
        });*/


        for (City city:cityTree){
            //collectionToDraw.put(city.getId(), new SmartCoordinates(city.getX(), city.getY(), username));
            coordinatesObservableList.add(new SmartCoordinates(city.getId(), city.getX(), city.getY(), username));
        }

        CANVAS_HEIGHT = visualisation_canvas.getHeight();
        CANVAS_WIDTH = visualisation_canvas.getWidth();

        updateThreadFlag = true;
        updateThread = new Thread(() ->{
            CityTree tempCityTree;
            TreeSet<Long> tempIDSet = new TreeSet<>();

            synchronized (cityTree) {
                while (updateThreadFlag) {
                    try {
                        //Получили актуальный список City
                        client.writeCommand(updater);
                        tempCityTree = (CityTree) client.getRespond().getSecondParameter();
                        tempIDSet.clear();
                        tempCityTree.forEach(city -> tempIDSet.add(city.getId()));

                        cityTree.removeIf(city -> (!tempIDSet.contains(city.getId())));
                        observableList.removeIf(city -> (!tempIDSet.contains(city.getId())));
                        coordinatesObservableList.removeIf(smartCoordinates -> (!tempIDSet.contains(smartCoordinates.getId())));

                        /*
                        collectionToDraw.forEach((id, coords) -> {
                            if (!tempIDSet.contains(id)){
                                collectionToDraw.remove(id);
                            }
                        });*/

                        //Сравниваем, поменялось ли что-то в каждом городе и меняем если это так
                        for (City tempCity: tempCityTree){
                            if (IDSet.contains(tempCity.getId())){
                                //Элемент имеющейся коллекции
                                City city = cityTree.getCityByID(tempCity.getId());

                                if (!city.getName().equals(tempCity.getName())){
                                    city.setName(tempCity.getName());
                                }
                                if (city.getPopulation() != (tempCity.getPopulation())){
                                    city.setPopulation(tempCity.getPopulation());
                                }

                                if (!Float.valueOf(city.getX()).equals(Float.valueOf(tempCity.getX())) && !Double.valueOf(city.getY()).equals(Double.valueOf(tempCity.getY()))){
                                    city.getCoordinates().setX(tempCity.getX());
                                    city.getCoordinates().setY(tempCity.getY());
                                    System.out.println("Changed XY");
                                    collectionToDraw.get(city.getId()).setXY(tempCity.getX(), tempCity.getY());
                                }else if (!Float.valueOf(city.getX()).equals(Float.valueOf(tempCity.getX()))){
                                    city.getCoordinates().setX(tempCity.getX());
                                    System.out.println("Changed X");
                                    collectionToDraw.get(city.getId()).setX(tempCity.getX());
                                }else if (!Double.valueOf(city.getY()).equals(Double.valueOf(tempCity.getY()))){
                                    city.getCoordinates().setY(tempCity.getY());
                                    System.out.println("Changed Y");
                                    collectionToDraw.get(city.getId()).setY(tempCity.getY());
                                }

                                if (city.getArea() != (tempCity.getArea())){
                                    city.setArea(tempCity.getArea());
                                }
                                if (city.getMetersAboveSeaLevel() != (tempCity.getMetersAboveSeaLevel())){
                                    city.setMetersAboveSeaLevel(tempCity.getMetersAboveSeaLevel());
                                }
                                if (city.getCarCode() != (tempCity.getCarCode())){
                                    city.setCarCode(tempCity.getCarCode());
                                }
                                if (!city.getStandardOfLiving().equals(tempCity.getStandardOfLiving())){
                                    city.setStandardOfLiving(tempCity.getStandardOfLiving());
                                }
                                if (!city.getEstablishmentDate().equals(tempCity.getEstablishmentDate())){
                                    city.setEstablishmentDate(tempCity.getEstablishmentDate());
                                }
                                if (!city.getGovernor().getBirthday().equals(tempCity.getGovernor().getBirthday())){
                                    city.getGovernor().setBirthday(tempCity.getGovernor().getBirthday());
                                }
                            }else {
                                cityTree.add(tempCity);
                                collectionToDraw.put(tempCity.getId(), new SmartCoordinates(tempCity.getX(), tempCity.getY(), username));
                                coordinatesObservableList.add(new SmartCoordinates(tempCity.getId(), tempCity.getX(), tempCity.getY(), username));
                            }
                        }

                        //Фильтруем коллекцию для показа в таблице и в визуализации по именному фильтру
                        observableList.clear();
                        cityTree.stream().filter(city -> city.getName().startsWith(FILTER)).collect(Collectors.toCollection(()->observableList));
                        IDSet.clear();
                        cityTree.forEach(city -> IDSet.add(city.getId()));

                        Thread.sleep(1000);
                    } catch (Exception e) {
                        serverFlag = false;
                        e.printStackTrace();
                    }
                }
            }
        } ){{start();}};


        drawThread = new Thread(() -> {
            while (updateThreadFlag) {
                if (cityTree.size() == 0) {
                    visualisation_canvas.getGraphicsContext2D().clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
                    visualisation_canvas.getGraphicsContext2D().setTextAlign(TextAlignment.CENTER);
                    visualisation_canvas.getGraphicsContext2D().fillText( noCities, (int) CANVAS_WIDTH / 2, (int) CANVAS_HEIGHT / 2);
                }else {
                    X_MAX_COORDINATE = cityTree.stream().map(city -> city.getX()).max(Float::compare).get();
                    Y_MAX_COORDINATE = cityTree.stream().map(city -> city.getY()).max(Double::compare).get();
                    X_MIN_COORDINATE = cityTree.stream().map(city -> city.getX()).min(Float::compare).get();
                    Y_MIN_COORDINATE = cityTree.stream().map(city -> city.getY()).min(Double::compare).get();
                }
            }
        }){{start();}};

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

    public void toCanvasCoordinates(float x, double y){

    }
}
