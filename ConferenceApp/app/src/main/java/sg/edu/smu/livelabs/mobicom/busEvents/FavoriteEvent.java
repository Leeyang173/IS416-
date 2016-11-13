package sg.edu.smu.livelabs.mobicom.busEvents;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.net.item.FavoriteListItem;

/**
 * Created by smu on 20/7/15.
 */
public class FavoriteEvent {
    public String status;
    public List<FavoriteListItem> items;

    public FavoriteEvent(String status, List<FavoriteListItem> items) {
        this.status = status;
        this.items = items;

    }

}
