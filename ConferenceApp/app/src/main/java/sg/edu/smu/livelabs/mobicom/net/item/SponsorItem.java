package sg.edu.smu.livelabs.mobicom.net.item;

/**
 * Created by smu on 15/1/16.
 */
public class SponsorItem {
    public String id;
    public String name;
    public String image;
    public int defaultImage;

    public SponsorItem() {
    }

    public SponsorItem(String id, String name, String image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    public SponsorItem(int defaultImage) {
        this.defaultImage = defaultImage;
    }
}
