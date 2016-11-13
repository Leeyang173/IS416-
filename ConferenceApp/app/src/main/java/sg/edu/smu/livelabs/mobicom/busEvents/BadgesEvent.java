package sg.edu.smu.livelabs.mobicom.busEvents;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.models.data.BadgeEntity;

/**
 * Created by smu on 20/7/15.
 */
public class BadgesEvent {

    public List<BadgeEntity> badgeEntityList;

    public BadgesEvent(List<BadgeEntity> badgeEntityList) {
        this.badgeEntityList = badgeEntityList;
    }
}
