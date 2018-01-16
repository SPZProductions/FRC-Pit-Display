package sample;

import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import main.TBA;
import models.other.Ranking;
import models.other.WLTRecord;
import models.other.teams.status.TeamEventStatus;
import models.simple.SMatch;
import models.standard.Event;
import models.standard.Team;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable {


    @FXML public Label team_num = new Label();
    @FXML public Label team_name = new Label();
    @FXML public Label team_motto = new Label();
    @FXML public Label comp_name = new Label();
    @FXML public Label next_match = new Label();
    @FXML public Label rank = new Label();
    @FXML public Label wlt = new Label();
    @FXML public Label average = new Label();
    @FXML public Label rp = new Label();
    @FXML public TableView<score_table> scores = new TableView();
    @FXML public ScrollBar scores_sb = (ScrollBar) scores.lookup(".scroll-bar:vertical");
    @FXML public TableColumn<score_table, String> scores_red = new TableColumn<>("Red Alliance");
    @FXML public TableColumn<score_table, String> scores_blue = new TableColumn<>("Blue Alliance");
    @FXML public TableColumn<score_table, String> scores_wlt = new TableColumn<>("W/L/T");
    @FXML public TableColumn<score_table, String> scores_score = new TableColumn<>("Score");
    @FXML public TableColumn<score_table, String> scores_mid = new TableColumn<>("Match #");


    private int teamNum = 1322;//Store Team Number
    private String eventId = "2014miket";//Store event id
    private TBA tba = new TBA();// Create TBA object


    public void initialize(URL location, ResourceBundle resources) {
        populateWindow();
    }

    private void populateWindow() {
        scores_red.setCellValueFactory(new PropertyValueFactory<>("redAlliance"));
        scores_blue.setCellValueFactory(new PropertyValueFactory<>("blueAlliance"));
        scores_wlt.setCellValueFactory(new PropertyValueFactory<>("wlt"));
        scores_score.setCellValueFactory(new PropertyValueFactory<>("score"));
        scores_mid.setCellValueFactory(new PropertyValueFactory<>("matchNumber"));

        //Test API Key before application start
        testKey();
        //TODO: REMOVE
        setUpDisplay();

    }

    //Test API Key
    private void testKey(){
        try{
            //Set API Key
            TBA.setAuthToken(api_auth.api_key);
            // Pull the team object (make sure to do this asynchronously if it updates an UI
            Team team = tba.getTeam(1322);
        }catch(Exception e){
            System.out.println("Invalid API Key");
            //Shows Box That notifyes user that API key is invalid
            invalidAPIKeyDialoug();
        }

    }
    //Box That notifyes user that API key is invalid
    private void invalidAPIKeyDialoug(){
        ButtonType bar = new ButtonType("Exit", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid API Key. Please Fix and Restart Application",bar);

        alert.setTitle("Date format warning");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == bar) {
            Platform.exit();
        }
    }

    //Setup display useing team number we have
    private void setUpDisplay(){
        Team team = tba.getTeam(teamNum);
        team_name.setText(team.getNickname());
        team_motto.setText(team.getMotto());
        team_num.setText(team.getTeamNumber() + "");

        Event event = tba.getEvent(eventId);
        comp_name.setText("At " + event.getName() + " in " + event.getCity() + ", " + event.getStateProv());

        SMatch[] matches = tba.getTeamEventSMatches(teamNum, eventId);
        for(SMatch m : matches) {
            score_table st = new score_table();
            String matchResults = calculatWin(m);
            String mw = calculateMatchWinner(m);
            String color;
            boolean bold = false;
            switch (mw) {
                case "r":
                    color = "#d30000";
                    break;
                case "b":
                    color = "#0008ad";
                    break;
                case "t":
                    color = "##ad00a7";
                    break;
                default:
                    color = "BLACK";
                    break;
            }
            if (matchResults.equals("w")) {
                bold = true;
            }
            st.setRedAlliance(m.getRed().getTeamKeys()[0].substring(3) + ", " + m.getRed().getTeamKeys()[1].substring(3) + ", " + m.getRed().getTeamKeys()[2].substring(3));
            st.setBlueAlliance(m.getBlue().getTeamKeys()[0].substring(3) + ", " + m.getBlue().getTeamKeys()[1].substring(3) + ", " + m.getBlue().getTeamKeys()[2].substring(3));
            st.setWlt(matchResults.toUpperCase());
            st.setScore(calculateScore(m) + "," + color + "," + bold);
            st.setMatchNumber(formatMatchNumber(m));
            scores.getItems().add(st);

            TeamEventStatus tes = tba.getTeamEventStatus(teamNum, eventId);
            next_match.setText("Next Match: " + formatMatchNumber(calculateNextMatch(matches)));
            rank.setText("Current Rank: " + tes.getQual().getRanking().getRank());
            WLTRecord qual = tes.getQual().getRanking().getQualificationsRecord();
            WLTRecord elim = tes.getPlayoff().getRecord();
            wlt.setText(getWLT(qual, elim));
            average.setText("Average Score: " + getAverageMatchScore(matches));
            rp.setText("Matches Played: " + tes.getQual().getRanking().getMatchesPlayed());
        }
        customTableCellColor();
    }

    private String calculatWin(SMatch m) {
        String alliance = "";
        String matchWin;
        if (m.getBlue().getTeamKeys()[0].equals("frc" + teamNum) || m.getBlue().getTeamKeys()[1].equals("frc" + teamNum) || m.getBlue().getTeamKeys()[2].equals("frc" + teamNum)) {
            alliance = "b";
        } else if (m.getRed().getTeamKeys()[0].equals("frc" + teamNum) || m.getRed().getTeamKeys()[1].equals("frc" + teamNum) || m.getRed().getTeamKeys()[2].equals("frc" + teamNum)) {
            alliance = "r";
        }
        matchWin = calculateMatchWinner(m);
        if(matchWin.equals(alliance)){
            return "w";
        }else if(matchWin.equals("t") || matchWin.equals("u")){
            return matchWin;
        }else{
            return "l";
        }
    }

    private String calculateScore(SMatch m){
        String matchWin = calculateMatchWinner(m);
        String score = "";
        switch (matchWin) {
            case "u":
                score = "Unplayed";
                break;
            default:
                score = m.getRed().getScore() + " - " + m.getBlue().getScore();
                break;
        }
        return score;
    }

    private String calculateMatchWinner(SMatch m){
        String matchWin = "";
        if(m.getBlue().getScore() == -1){
            matchWin = "u";// U equals unplayed
        }else if(m.getBlue().getScore() > m.getRed().getScore()){
            matchWin = "b";//Blue Won
        }else if(m.getBlue().getScore() < m.getRed().getScore()){
            matchWin = "r";//Red Won
        }else if(m.getBlue().getScore() == m.getRed().getScore()){
            matchWin =  "t";//tie
        }
        return matchWin;
    }

    private void customTableCellColor(){
        scores_score.setCellFactory(column -> {
            return new TableCell<score_table, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if(item != null || !empty){
                        String[] seperate = item.split(",");
                        setText(seperate[0]);
                        setTextFill(Color.WHITE);
                        if(seperate[2].equals("true")){setFont(Font.font("Ariel", FontWeight.EXTRA_BOLD, 16));}
                        else{setFont(Font.font("Ariel", FontWeight.EXTRA_LIGHT, 16));}
                        setStyle("-fx-background-color: " + seperate[1]);
                        setAlignment(Pos.CENTER);
                    }
                }
            };
        });

        scores_blue.setCellFactory(column -> {
            return new TableCell<score_table, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if(item != null || !empty){
                        if(item.contains(teamNum + "")){
                            setText(item);
                            setStyle("-fx-background-color: #433dff");
                            setFont(Font.font("Ariel", FontWeight.EXTRA_BOLD, 16));
                            setAlignment(Pos.CENTER);
                        }else{
                            setText(item);
                            setStyle("-fx-background-color: #8682ff");
                            setFont(Font.font("Ariel", FontWeight.EXTRA_LIGHT, 16));
                            setAlignment(Pos.CENTER);
                        }
                    }
                }
            };
        });

        scores_red.setCellFactory(column -> {
            return new TableCell<score_table, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if(item != null || !empty){
                        if(item.contains(teamNum + "")){
                            setText(item);
                            setStyle("-fx-background-color: #ff3d3d");
                            setFont(Font.font("Ariel", FontWeight.EXTRA_BOLD, 16));
                            setAlignment(Pos.CENTER);
                        }else{
                            setText(item);
                            setStyle("-fx-background-color: #ff7777");
                            setFont(Font.font("Ariel", FontWeight.EXTRA_LIGHT, 16));
                            setAlignment(Pos.CENTER);
                        }
                    }
                }
            };
        });

        scores_wlt.setCellFactory(column -> {
            return new TableCell<score_table, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if(item != null || !empty){
                        if(item.contains("W")){
                            setText(item);
                            setStyle("-fx-background-color: #fcff7a");
                            setFont(Font.font("Ariel", FontWeight.EXTRA_BOLD, 16));
                            setAlignment(Pos.CENTER);
                        }else if(item.contains("L")){
                            setText(item);
                            setStyle("-fx-background-color: #bc8151");
                            setFont(Font.font("Ariel", FontWeight.EXTRA_LIGHT, 16));
                            setAlignment(Pos.CENTER);
                        }else{
                            setText(item);
                            setStyle("-fx-background-color: #80ff79");
                            setFont(Font.font("Ariel", FontWeight.EXTRA_BOLD, 16));
                            setAlignment(Pos.CENTER);
                        }
                    }
                }
            };
        });
        scores_mid.setCellFactory(column -> {
            return new TableCell<score_table, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if(item != null || !empty){
                        setText(item);
                        setFont(Font.font("Ariel", FontWeight.EXTRA_BOLD, 16));
                        setAlignment(Pos.CENTER);
                    }
                }
            };
        });
    }

    private SMatch calculateNextMatch(SMatch[] ms){
        for(SMatch m : ms){
            if(m.getBlue().getScore() == -1 && m.getRed().getScore() == -1){
                return m;
            }
        }
        return null;
    }

    private String formatMatchNumber(SMatch m){
        if(m != null){
            String compLevel = m.getCompLevel();
            if(compLevel.equals("qm")){compLevel = "Q";}
            return compLevel.toUpperCase() + " " + m.getMatchNumber() + "";
        }else {
            return "None Scheduled";
        }
    }

    private String getWLT(WLTRecord qual, WLTRecord elim){
        Long w = qual.getWins() + elim.getWins();
        Long l = qual.getLosses() + elim.getLosses();
        Long t = qual.getTies() + elim.getTies();
        return "W-L-T: " + w + "-" + l + "-" + t;
    }

    private Long getAverageMatchScore(SMatch[] matches){
        int i = 0;
        Long total = new Long("0");
        for(SMatch m : matches) {
            if (m.getBlue().getTeamKeys()[0].equals("frc" + teamNum) || m.getBlue().getTeamKeys()[1].equals("frc" + teamNum) || m.getBlue().getTeamKeys()[2].equals("frc" + teamNum) && m.getBlue().getScore() != -1) {
                total = total + m.getBlue().getScore();
                i++;
            } else if (m.getRed().getTeamKeys()[0].equals("frc" + teamNum) || m.getRed().getTeamKeys()[1].equals("frc" + teamNum) || m.getRed().getTeamKeys()[2].equals("frc" + teamNum) && m.getRed().getScore() != -1) {
                total = total + m.getRed().getScore();
                i++;
            }
        }
        total = total/i;
        return total;
    }

}