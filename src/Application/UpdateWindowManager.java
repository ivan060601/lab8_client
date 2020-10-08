package Application;

import CityStructure.City;
import CityStructure.Coordinates;
import CityStructure.Human;
import CityStructure.StandardOfLiving;
import ClientStuff.Client;
import ClientStuff.Command;
import ClientStuff.User;
import ClientStuff.WindowActivator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class UpdateWindowManager implements WindowActivator {
    @FXML
    public Text name_text;
    public Text population_text;
    public Text x_coordinate_text;
    public Text y_coordinate_text;
    public Text zone_text;
    public Text meters_above_sealevel_text;
    public Text est_text;
    public Text carcode_text;
    public Text SOF_text;
    public Text human_birthday_text;
    public DatePicker est_date;
    public DatePicker human_birthday_field;
    public ChoiceBox<StandardOfLiving> SOF_field;
    public TextField name_field;
    public TextField population_field;
    public TextField x_field;
    public TextField y_field;
    public TextField zone_field;
    public TextField meters_above_sealevel_field;
    public TextField carcode_field;
    public Button apply_button;
    public Button OK_button;

    private User user;
    private City city;
    private City cityToSend = new City();
    private Client client;
    private Command command = new Command();
    private Stage currentStage;

    public UpdateWindowManager(User user, City city, Stage stage){
        this.city = city;
        this.user = user;
        this.client = user.getClient();
        this.currentStage = stage;
        command.setUser(this.user);
        command.setAdditional(city.getId());
    }

    @FXML
    public void apply_button_clicked(ActionEvent actionEvent) {
        if (check_fields()){
            makeNotification("OK", "Everything is OK");
        }
    }

    @FXML
    public void ok_button_clicked(ActionEvent actionEvent) {
        if (check_fields()){
            command.setEverything("update", cityToSend);
            client.writeCommand(command);
            makeNotification("Update city", client.getRespond().getMsg());
            currentStage.close();
        }
    }

    @FXML
    public void initialize(){
        ObservableList<StandardOfLiving> list = FXCollections.observableArrayList(StandardOfLiving.MEDIUM, StandardOfLiving.HIGH, StandardOfLiving.ULTRA_LOW);
        SOF_field.getItems().addAll(list);

        name_field.setText(city.getName());
        population_field.setText(String.valueOf(city.getPopulation()));
        x_field.setText(String.valueOf(city.getCoordinates().getX()));
        y_field.setText(String.valueOf(city.getCoordinates().getY()));
        zone_field.setText(String.valueOf(city.getArea()));
        meters_above_sealevel_field.setText(String.valueOf(city.getMetersAboveSeaLevel()));
        carcode_field.setText(String.valueOf(city.getCarCode()));
        SOF_field.setValue(city.getStandardOfLiving());
        est_date.setValue(city.getEstablishmentDate());
        //
        human_birthday_field.setValue(Instant.ofEpochMilli(city.getGovernor().getBirthday().getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate());
    }

    private boolean check_fields(){
        try {
            String name = name_field.getText();
            if (!name.equals(null)) {
                float x = Float.parseFloat(x_field.getText());
                if (x <= 349f) {
                    Double y = Double.parseDouble(y_field.getText());
                    int population = Integer.parseInt(population_field.getText());
                    if(population > 0){
                        float area = Float.parseFloat(zone_field.getText());
                        if (area > 0){
                            long metersAboveSeaLevel = Long.parseLong(meters_above_sealevel_field.getText());
                            long carcode = Long.parseLong(carcode_field.getText());
                            if (carcode < 1000 && carcode > 0){
                                LocalDate establishment_date = est_date.getValue();
                                Date birthday = Date.from(human_birthday_field.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                                cityToSend.setCity(name, new Coordinates(x, y), area, population, metersAboveSeaLevel, establishment_date, carcode, SOF_field.getValue(), new Human(birthday));
                                return true;
                            }else {
                                makeAlert("Invalid carcode", "Carcode should be from 0 to 1000");
                                return false;
                            }
                        }else {
                            makeAlert("Invalid area", "Area should be more then 0");
                            return false;
                        }
                    }else {
                        makeAlert("Invalid population", "Population should be more then 0");
                        return false;
                    }
                } else {
                    makeAlert("Invalid X-coordinate", "X should be less then 349");
                    return false;
                }
            } else {
                makeAlert("Null name field", "Null name field");
                return false;
            }
        }catch (NumberFormatException e){
            makeAlert("Wrong number format", "Wrong number format");
            return false;
        }catch (NullPointerException  e){
            makeAlert("Null date field", "Null date field");
            return false;
        }
    }
}