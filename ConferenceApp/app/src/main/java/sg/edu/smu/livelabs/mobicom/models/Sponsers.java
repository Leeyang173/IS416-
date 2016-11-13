package sg.edu.smu.livelabs.mobicom.models;

/**
 * Created by johnlee on 22/2/16.
 */
public class Sponsers {

    private int imageResId;
    private String name;

    public Sponsers() {
    }

    public Sponsers(int imageResId, String name) {
        this.imageResId = imageResId;
        this.name = name;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
