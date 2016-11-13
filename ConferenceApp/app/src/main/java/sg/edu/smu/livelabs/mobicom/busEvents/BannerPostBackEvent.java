package sg.edu.smu.livelabs.mobicom.busEvents;

/**
 * Created by smu on 20/7/15.
 */
public class BannerPostBackEvent {
    public long id;
    public String keyword;
    public String status;

    public BannerPostBackEvent(long id, String keyword, String status) {
        this.id = id;
        this.keyword = keyword;
        this.status = status;
    }
}
