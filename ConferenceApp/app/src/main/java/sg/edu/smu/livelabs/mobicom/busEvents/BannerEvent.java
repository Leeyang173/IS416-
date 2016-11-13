package sg.edu.smu.livelabs.mobicom.busEvents;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.net.item.BannerItem;

/**
 * Created by smu on 20/7/15.
 */
public class BannerEvent {
    public List<BannerItem> bannerItems;
    public String status;

    public BannerEvent(String status) {
        this.status = status;
    }

    public BannerEvent(List<BannerItem> bannerItems, String status) {
        this.bannerItems = bannerItems;
        this.status = status;
    }
}
