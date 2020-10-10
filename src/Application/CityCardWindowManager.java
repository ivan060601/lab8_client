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

public class CityCardWindowManager implements WindowActivator {
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

    private City city;
    private Stage currentStage;

    public CityCardWindowManager(City city, Stage stage){
        this.city = city;
        this.currentStage = stage;
    }

    @FXML
    public void ok_button_clicked(ActionEvent actionEvent) {
        currentStage.close();
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
}