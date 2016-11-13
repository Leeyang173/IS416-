package sg.edu.smu.livelabs.mobicom.models;

/**
 * Created by johnlee on 22/2/16.
 */
public class Badges {

    private int count;
    private String title;
    private String des;
    private int max;
    private int badgeType;
    private String badgeIcon;
    private int gameId;

    public Badges() {
    }

    public Badges(int count, String title, String des, int max, int badgeType, String badgeIcon, int gameId) {
        this.count = count;
        this.title = title;
        this.des = des;
        this.max = max;
        this.badgeType = badgeType;
        this.badgeIcon = badgeIcon;
        this.gameId = gameId;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }


    public int getBadgeType() {
        return badgeType;
    }

    public void setBadgeType(int badgeType) {
        this.badgeType = badgeType;
    }

    public String getBadgeIcon() {
        return badgeIcon;
    }

    public void setBadgeIcon(String badgeIcon) {
        this.badgeIcon = badgeIcon;
    }
}
