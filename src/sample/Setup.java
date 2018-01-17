package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import main.TBA;
import models.simple.STeam;
import models.standard.Event;
import models.standard.Team;

import java.net.URL;
import java.util.ResourceBundle;

public class Setup implements Initializable {
    @FXML public Label tncl = new Label();
    @FXML public Label eidcl = new Label();
    @FXML public Label tba_id = new Label();
    @FXML public TextField tn_box = new TextField();
    @FXML public TextField eid_box = new TextField();
    @FXML public Button tncb = new Button();
    @FXML public Button eidcb = new Button();
    @FXML public Button go = new Button();

    Controller controller = Main.getController();
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populateWindow();
    }

    private void populateWindow(){
        eidcl.setVisible(false);
        tba_id.setVisible(false);
        eid_box.setVisible(false);
        eidcb.setVisible(false);
        go.setVisible(false);
        tn_box.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                tn_box.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    @FXML
    public void checkTeamNumber(){
        try{
            TBA.setAuthToken(api_auth.api_key);
            TBA tba = new TBA();
            Team team = tba.getTeam(Integer.parseInt(tn_box.getText()));
            tncl.setText("Valid Team");
            tncl.setTextFill(Color.GREEN);
            controller.teamNum = Integer.parseInt(tn_box.getText());
            eidcl.setVisible(true);
            tba_id.setVisible(true);
            eid_box.setVisible(true);
            eidcb.setVisible(true);
        }catch(Exception e){
            tncl.setText("Invalid Team");
            tncl.setTextFill(Color.RED);
        }
    }

    @FXML
    public void checkEventID(){
        try{
            TBA.setAuthToken(api_auth.api_key);
            TBA tba = new TBA();
            Event event = tba.getEvent(eid_box.getText());
            eidcl.setText("Valid Event, Team Not Found in Event");
            eidcl.setTextFill(Color.YELLOW);
            controller.eventId = eid_box.getText();

            if(isTeamInEvent(tba)){
                eidcl.setText("Valid Event, Team In Event");
                eidcl.setTextFill(Color.GREEN);
                go.setVisible(true);
            }
        }catch(Exception e){
            eidcl.setText("Invalid Event");
            eidcl.setTextFill(Color.RED);
        }

    }

    private boolean isTeamInEvent(TBA tba){
        STeam[] teams = tba.getEventTeams(controller.eventId);
        for(STeam t : teams){
            if(t.getTeamNumber() == controller.teamNum)return true;
        }
        return false;
    }

    @FXML
    public void go(){
        controller.setUpDisplay();
        Stage stage = (Stage) eid_box.getScene().getWindow();
        stage.close();
    }
}
