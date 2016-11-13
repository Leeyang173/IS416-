package sg.edu.smu.livelabs.mobicom.models;

/**
 * Created by johnlee on 22/2/16.
 */
public class GameToEarnPoint {

    private String title;
    private String bgImageURL;
    private int gameId;

    public GameToEarnPoint() {
    }

    public GameToEarnPoint(String title, String bgImageURL, int gameId) {
        this.title = title;
        this.bgImageURL = bgImageURL;
        this.gameId = gameId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBgImageURL() {
        return bgImageURL;
    }

    public void setBgImageURL(String bgImageURL) {
        this.bgImageURL = bgImageURL;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }
}
