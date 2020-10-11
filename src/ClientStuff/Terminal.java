package ClientStuff;

import Application.*;
import Application.Locale.Localizator;
import CityStructure.City;
import CityStructure.CityTree;
import CityStructure.Human;
import CityStructure.StandardOfLiving;
import javafx.beans.Observable;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Terminal implements WindowActivator {

    //Все переменные, связанные с интерфейсом
    @FXML
    public Text username_text;
    public Text y1;
    public Text y2;
    public Text y3;
    public Text y4;
    public Text y5;
    public Text y6;
    public Text x1;
    public Text x2;
    public Text x3;
    public Text x4;
    public Text x5;
    public Text x6;
    public Text x7;
    public Text x8;
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

    //Все переменные для окна визуализации
    private String noCities = "No available cities to show";
    private double CANVAS_HEIGHT;
    private double CANVAS_WIDTH;
    private float X_MAX_COORDINATE = 0;
    private double Y_MAX_COORDINATE = 0;
    private float X_MIN_COORDINATE = 0;
    private double Y_MIN_COORDINATE = 0;
    private Group cityGroup;
    final static Image image = new Image("/Application/Transparent.png");
    private ObservableList<SmartCoordinates> coordinatesObservableList = FXCollections.observableArrayList(
            param -> new Observable[]{
                    param.getYProperty(),
                    param.getXProperty()
            });

    public Terminal(User user) {
        this.user = user;
        client = user.getClient();
        command.setUser(user);
        client.getMainStage().setHeight(580);
        client.getMainStage().setWidth(935);
        username = user.getLogin();
        updater.setEverything("show", null);
    }

    /**
     * Метод для смены языка
     */
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

    private void changeLanguage(){
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

    /**
     * Реализация отправки и обработки команды help
     */
    @FXML
    public void command_help_execute(ActionEvent actionEvent) {
        if(serverFlag) {
            command.setEverything("help", null);
            client.writeCommand(command);
            client.makeNotification("Help", client.getRespond().getMsg());
        }else {
            logout();
        }
        redrawAllCities();
    }

    /**
     * Реализация отправки и обработки команды info
     */
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

    /**
     * Реализация отправки и обработки команды add
     */
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

    /**
     * Реализация отправки и обработки команды update
     */
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
                    makeNotification("City not found", "City with such ID was not found in your collection");
                }
            }catch (NumberFormatException | NullPointerException e){
                makeAlert(invalidID, invalidID);
            }

        }else {
            logout();
        }
    }

    /**
     * Реализация отправки и обработки команды remove
     */
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

    /**
     * Реализация отправки и обработки команды clear
     */
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

    /**
     * Реализация отправки и обработки команды execute_script
     */
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

    /**
     * Реализация отправки и обработки команды add_if_max
     */
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

    /**
     * Реализация отправки и обработки команды remove_lower
     */
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

    /**
     * Реализация отправки и обработки команды history
     */
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

    /**
     * Реализация отправки и обработки команды remove_any_by_governor
     */
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

    /**
     * Реализация отправки и обработки команды filter_starts_with_name
     */
    @FXML
    public void command_filter_name_execute(ActionEvent actionEvent) {
        String tempString = makeTextDialogue("Filter starts with name", enterFilter);
        if (tempString!=null){
            FILTER = tempString;
        }
    }

    /**
     * Обработка нажатия на кнопку выхода
     */
    @FXML
    public void logout(ActionEvent actionEvent) {
        client.getMainStage().setScene(sceneController.getScene("Connection"));
        client.getMainStage().setWidth(300);
        client.getMainStage().setHeight(371);
        sceneController.removeScene("Login");
        sceneController.removeScene("Main");
        updateThreadFlag = false;
    }


    private void logout() {
        makeAlert(serverUnreachable, "");
        client.getMainStage().setScene(sceneController.getScene("Connection"));
        client.getMainStage().setWidth(300);
        client.getMainStage().setHeight(371);
        sceneController.removeScene("Login");
        sceneController.removeScene("Main");
        updateThreadFlag = false;
    }

    /**
     * Обработка нажатия на Canvas чтобы показать карточку с информацией о городе
     */
    @FXML
    public void showCityCard(MouseEvent mouseEvent) {
        AtomicBoolean shown = new AtomicBoolean(false);
        double mouseX = mouseEvent.getX();
        double mouseY = mouseEvent.getY();
        coordinatesObservableList.stream().forEach(coordinates -> {
            if (mouseX > XtoCanvasCoordinates(coordinates.getX()) && mouseX < XtoCanvasCoordinates(coordinates.getX()+30)){
                if (mouseY > YtoCanvasCoordinates(coordinates.getY()) && mouseY < YtoCanvasCoordinates(coordinates.getY() + 30) && !shown.get()){
                    shown.set(true);
                    if(serverFlag) {
                        Stage smallStage = new Stage();
                        SceneController smallSceneController = new SceneController(smallStage);
                        CityCardWindowManager cityCardWindowManager = new CityCardWindowManager(cityTree.getCityByID(coordinates.getId()), smallStage);
                        smallSceneController.loadWithController(cityCardWindowManager, "City card", "/Application/FXMLs/city_popup.fxml");
                        smallStage.setScene(smallSceneController.getScene("City card"));
                        smallStage.setResizable(false);
                        smallStage.setTitle("City card");
                        smallStage.setHeight(450);
                        smallStage.setWidth(350);
                        smallStage.show();
                        smallStage.setOnCloseRequest(event -> smallSceneController.removeScene("City card"));
                    }else {
                        logout();
                    }
                }
            }
        });
    }

    @FXML
    public void initialize(){
        changeLanguage();
        username_text.setText(username);
        cityTree = new CityTree();
        drawGrid();
        setBounds();

        /**
         * Добавление Listener, чтобы знать, когда меняются/добавляются/удаляются города
         */
        coordinatesObservableList.addListener((ListChangeListener<SmartCoordinates>) c -> {
            while (c.next()) {
                if (c.wasUpdated()) {
                    for (int i = c.getFrom(); i < c.getTo(); ++i) {
                        System.out.println(coordinatesObservableList.get(i).getId() + " was updated");
                        if (paramsChanged()){
                            coordinatesObservableList.get(i).setRedraw(true);
                            drawGrid();
                            setBounds();
                            redrawAllCities();
                            drawCity(coordinatesObservableList.get(i));
                            drawGrid();
                            setBounds();
                            redrawAllCities();
                        }else {
                            coordinatesObservableList.get(i).setRedraw(true);
                            drawCity(coordinatesObservableList.get(i));
                            drawGrid();
                            setBounds();
                            redrawAllCities();
                        }
                    }
                }else if (c.wasRemoved()) {
                    for (SmartCoordinates sc : c.getRemoved()) {
                        //Уберем отображение объекта
                        System.out.println(sc.getId() + " was removed");
                        if (paramsChanged()){
                            removeCity(sc);
                            drawGrid();
                            setBounds();
                            redrawAllCities();
                        }else {
                            removeCity(sc);
                        }
                    }
                }else if (c.wasAdded()) {
                    for (SmartCoordinates sc : c.getAddedSubList()) {
                        //Нарисуем объект с нуля
                        System.out.println(sc.getId() + " was added");
                        if (paramsChanged()){
                            drawGrid();
                            setBounds();
                            redrawAllCities();
                            drawCity(sc);
                            drawGrid();
                            setBounds();
                            redrawAllCities();
                        }else {
                            drawCity(sc);
                            drawGrid();
                            setBounds();
                            redrawAllCities();
                        }
                    }
                }
            }
        });

        for (City city:cityTree){
            coordinatesObservableList.add(new SmartCoordinates(city.getId(), city.getX(), city.getY(), username));
        }

        CANVAS_HEIGHT = visualisation_canvas.getHeight() - 20;
        CANVAS_WIDTH = visualisation_canvas.getWidth() - 20;

        updateThreadFlag = true;
        /**
         * Запуск отдельного потока для получения актуальной коллекции с сервера
         */
        updateThread = new Thread(() ->{
            boolean firstLoad = true;
            CityTree tempCityTree;
            TreeSet<Long> tempIDSet = new TreeSet<>();

            synchronized (cityTree) {
                while (updateThreadFlag) {
                    try {
                        int prevSize = cityTree.size();
                        //Получили актуальный список City
                        client.writeCommand(updater);
                        tempCityTree = (CityTree) client.getRespond().getSecondParameter();
                        tempIDSet.clear();
                        tempCityTree.forEach(city -> tempIDSet.add(city.getId()));

                        cityTree.removeIf(city -> (!tempIDSet.contains(city.getId())));
                        observableList.removeIf(city -> (!tempIDSet.contains(city.getId())));
                        coordinatesObservableList.removeIf(smartCoordinates -> (!tempIDSet.contains(smartCoordinates.getId())));

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
                                    removeCityWithoutTouchingItself(coordinatesObservableList.get(getIndexByID(city.getId())));
                                    city.getCoordinates().setX(tempCity.getX());
                                    city.getCoordinates().setY(tempCity.getY());
                                    coordinatesObservableList.get(getIndexByID(city.getId())).setXY(tempCity.getX(), tempCity.getY());
                                }else if (!Float.valueOf(city.getX()).equals(Float.valueOf(tempCity.getX()))){
                                    removeCityWithoutTouchingItself(coordinatesObservableList.get(getIndexByID(city.getId())));
                                    city.getCoordinates().setX(tempCity.getX());
                                    coordinatesObservableList.get(getIndexByID(city.getId())).setX(tempCity.getX());
                                }else if (!Double.valueOf(city.getY()).equals(Double.valueOf(tempCity.getY()))){
                                    removeCityWithoutTouchingItself(coordinatesObservableList.get(getIndexByID(city.getId())));
                                    city.getCoordinates().setY(tempCity.getY());
                                    coordinatesObservableList.get(getIndexByID(city.getId())).setY(tempCity.getY());
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
                                coordinatesObservableList.add(new SmartCoordinates(tempCity.getId(), tempCity.getX(), tempCity.getY(), tempCity.getOwner()));
                                if (firstLoad){
                                    observableList.add(tempCity);
                                }
                            }
                        }

                        //Фильтруем коллекцию для показа в таблице и в визуализации по именному фильтру
                        firstLoad = false;
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

    /**
     * Метод для конвертации координаты X города в координату на Canvas'е
     * @param x координата на карте
     * @return координата на Canvas'е
     */
    public int XtoCanvasCoordinates(float x){
        //из формулы ((Input - InputLow) / (InputHigh - InputLow)) * (OutputHigh - OutputLow) + OutputLow
        return (int) (((x-X_MIN_COORDINATE)/(X_MAX_COORDINATE-X_MIN_COORDINATE))*(CANVAS_WIDTH));
    }

    /**
     * Метод для конвертации координаты Y города в координату на Canvas'е
     * @param y координата на карте
     * @return координата на Canvas'е
     */
    public int YtoCanvasCoordinates(double y){
        //из формулы ((Input - InputLow) / (InputHigh - InputLow)) * (OutputHigh - OutputLow) + OutputLow
        return (int) (((y-Y_MIN_COORDINATE)/(Y_MAX_COORDINATE-Y_MIN_COORDINATE))*(CANVAS_HEIGHT));
    }

    private int getIndexByID(long id){
        for (SmartCoordinates sc: coordinatesObservableList){
            if (sc.getId() == id){
                return coordinatesObservableList.indexOf(sc);
            }
        }
        return -1;
    }

    /**
     * Метод для отрисовки города на Canvas'е
     * @param coordinates координаты города
     */
    private void drawCity(SmartCoordinates coordinates){
        if (coordinates.toRedraw()){
            for (double i = 0; i <= 1; i = i + 0.01){
                drawCityWithOpacity(coordinates, i);
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            coordinates.setRedraw(false);
        }else {
            visualisation_canvas.getGraphicsContext2D().drawImage(coordinates.getImage(), XtoCanvasCoordinates(coordinates.getX()), YtoCanvasCoordinates(coordinates.getY()));
        }
    }

    /**
     * Метод для отрисовки города на Canvas'е, но с эффектом прозрачности
     * @param coordinates координаты
     * @param opacity непрозрачность
     */
    private void drawCityWithOpacity(SmartCoordinates coordinates, double opacity){
        visualisation_canvas.getGraphicsContext2D().drawImage(coordinates.getTransparentImage(opacity), XtoCanvasCoordinates(coordinates.getX()), YtoCanvasCoordinates(coordinates.getY()));
    }

    /**
     * Метод, перересовывающий все города из коллекции
     * Наличие анимации при отрисовке зависит от флага redraw в SmartCoordinates
     */
    private void redrawAllCities(){
        coordinatesObservableList.stream().forEach(coordinates -> drawCity(coordinates));
    }

    /**
     * Метод для отрисовки анимации удаления города
     * redrawAllCities() в конце работает, т.к. coordinatesObservableList уже не содержит coordinates,
     * переданных в аргументах
     * @param coordinates координаты
     */
    private void removeCity(SmartCoordinates coordinates){
        for (double i = 1; i > 0; i = i - 0.01){
            for (SmartCoordinates coordinates1 : coordinatesObservableList){
                drawCity(coordinates1);
            }
            drawCityWithOpacity(coordinates, i);
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            drawGrid();
        }
        redrawAllCities();
    }

    /**
     * Аналог removeCity, но в конце при перерисовке всех элементов коллекции coordinates
     * будут проигнорированы и не отрисованы заново
     * @param coordinates координаты
     */
    private void removeCityWithoutTouchingItself(SmartCoordinates coordinates){
        for (double i = 1; i > 0; i = i - 0.01){
            for (SmartCoordinates coordinates1 : coordinatesObservableList){
                if (coordinates.getId() != coordinates1.getId()) {
                    drawCity(coordinates1);
                }
            }
            drawCityWithOpacity(coordinates, i);
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            drawGrid();
        }
        coordinatesObservableList.stream().filter(coordinates1 -> coordinates1.getId()!=coordinates.getId()).forEach(coordinates1 -> drawCity(coordinates1));
    }

    /**
     * Метод для установки крайних точек на Canvas'е в реальных координатах(не пикселях)
     * @return изменились ли крайние точки
     */
    private boolean paramsChanged(){
        float prevX_MAX_COORDINATE = X_MAX_COORDINATE;
        double prevY_MAX_COORDINATE = Y_MAX_COORDINATE;
        float prevX_MIN_COORDINATE = X_MIN_COORDINATE;
        double prevY_MIN_COORDINATE = Y_MIN_COORDINATE;

        if (cityTree.size() != 0) {
            X_MAX_COORDINATE = cityTree.stream().map(city -> city.getX()).max(Float::compare).get();
            Y_MAX_COORDINATE = cityTree.stream().map(city -> city.getY()).max(Double::compare).get();
            X_MIN_COORDINATE = cityTree.stream().map(city -> city.getX()).min(Float::compare).get();
            Y_MIN_COORDINATE = cityTree.stream().map(city -> city.getY()).min(Double::compare).get();

            if (prevX_MAX_COORDINATE != X_MAX_COORDINATE || prevY_MAX_COORDINATE != Y_MAX_COORDINATE || prevX_MIN_COORDINATE != X_MIN_COORDINATE || prevY_MIN_COORDINATE != Y_MIN_COORDINATE) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Метод для отрисовки сетки
     */
    private void drawGrid(){
        GraphicsContext gc = visualisation_canvas.getGraphicsContext2D();
        gc.clearRect(0,0,visualisation_canvas.getWidth(),visualisation_canvas.getHeight());
        gc.setStroke(javafx.scene.paint.Color.BLACK);
        if (cityTree.size() == 0){
            gc.setFill(Color.BLACK);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(noCities, visualisation_canvas.getWidth()/2, visualisation_canvas.getHeight()/2);
        }else if(cityTree.size() == 1){
            gc.drawImage(image, visualisation_canvas.getWidth()/2, visualisation_canvas.getHeight()/2);
        }else {
            for (double i = 0; i < visualisation_canvas.getWidth(); i=i+40){
                gc.strokeLine(i,0,i,visualisation_canvas.getHeight());
            }
            for (double j = visualisation_canvas.getHeight(); j > 0; j=j-40){
                gc.strokeLine(0, j, visualisation_canvas.getWidth(), j);
            }
        }
    }

    /**
     * Метод, задающий подписи к сетке
     */
    private void setBounds() {
        if (cityTree.size() > 1) {
            double stepY = ((Y_MAX_COORDINATE - Y_MIN_COORDINATE) / 6);
            y1.setText(String.format("%.1f", Y_MIN_COORDINATE + stepY));
            y2.setText(String.format("%.1f", Y_MIN_COORDINATE + 2 * stepY));
            y3.setText(String.format("%.1f", Y_MIN_COORDINATE + 3 * stepY));
            y4.setText(String.format("%.1f", Y_MIN_COORDINATE + 4 * stepY));
            y5.setText(String.format("%.1f", Y_MIN_COORDINATE + 5 * stepY));
            y6.setText(String.format("%.1f", Y_MIN_COORDINATE + 6 * stepY));

            float stepX = (X_MAX_COORDINATE - X_MIN_COORDINATE) / 9;
            x1.setText(String.format("%.1f", X_MIN_COORDINATE + stepX));
            x2.setText(String.format("%.1f", X_MIN_COORDINATE + 2 * stepX));
            x3.setText(String.format("%.1f", X_MIN_COORDINATE + 3 * stepX));
            x4.setText(String.format("%.1f", X_MIN_COORDINATE + 4 * stepX));
            x5.setText(String.format("%.1f", X_MIN_COORDINATE + 5 * stepX));
            x6.setText(String.format("%.1f", X_MIN_COORDINATE + 6 * stepX));
            x7.setText(String.format("%.1f", X_MIN_COORDINATE + 7 * stepX));
            x8.setText(String.format("%.1f", X_MIN_COORDINATE + 8 * stepX));
        } else {
            y1.setText("");
            y2.setText("");
            y3.setText("");
            y4.setText("");
            y5.setText("");
            y6.setText("");
            x1.setText("");
            x2.setText("");
            x3.setText("");
            x4.setText("");
            x5.setText("");
            x6.setText("");
            x7.setText("");
            x8.setText("");
        }
    }
}