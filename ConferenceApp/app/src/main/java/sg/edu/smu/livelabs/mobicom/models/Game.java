package sg.edu.smu.livelabs.mobicom.models;

/**
 * Created by johnlee on 22/2/16.
 */
public class Game {

    private String title;
    private String decription;
    private int resourceId;

    public Game() {
    }

    public Game(String title, String decription, int resourceId) {
        this.title = title;
        this.decription = decription;
        this.resourceId = resourceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDecription() {
        return decription;
    }

    public void setDecription(String decription) {
        this.decription = decription;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

}
