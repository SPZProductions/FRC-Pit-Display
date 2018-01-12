package sample;

public class score_table {
    private String redAlliance;
    private String blueAlliance;
    private String wlt;
    private String score;
    private String matchNumber;

    public score_table(){
        redAlliance = "";
        blueAlliance = "";
        wlt = "";
        score = "";
        matchNumber = "";
    }

    public score_table(String redAlliance, String blueAlliance, String wlt, String score, String matchNumber){
        this.redAlliance = redAlliance;
        this.blueAlliance = blueAlliance;
        this.wlt = wlt;
        this.score = score;
        this.matchNumber = matchNumber;
    }

    public String getRedAlliance() {
        return redAlliance;
    }

    public void setRedAlliance(String redAlliance) {
        this.redAlliance = redAlliance;
    }

    public String getBlueAlliance() {
        return blueAlliance;
    }

    public void setBlueAlliance(String blueAlliance) {
        this.blueAlliance = blueAlliance;
    }

    public String getWlt() {
        return wlt;
    }

    public void setWlt(String wlt) {
        this.wlt = wlt;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getMatchNumber() {
        return matchNumber;
    }

    public void setMatchNumber(String matchNumber) {
        this.matchNumber = matchNumber;
    }
}
