package sg.edu.smu.livelabs.mobicom.models;

/**
 * Created by johnlee on 22/2/16.
 */
public class Prize {

    private int points;
    private String prizeImageURL;

    public Prize() {
    }

    public Prize(int points, String prizeImageURL) {
        this.points = points;
        this.prizeImageURL = prizeImageURL;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getPrizeImageURL() {
        return prizeImageURL;
    }

    public void setPrizeImageURL(String prizeImageURL) {
        this.prizeImageURL = prizeImageURL;
    }
}
