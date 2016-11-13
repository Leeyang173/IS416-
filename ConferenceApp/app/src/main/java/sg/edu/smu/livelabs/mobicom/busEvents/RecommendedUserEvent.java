package sg.edu.smu.livelabs.mobicom.busEvents;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.models.data.AttendeeEntity;

/**
 * Created by smu on 19/4/16.
 */
public class RecommendedUserEvent {
    public List<AttendeeEntity> result;
    public RecommendedUserEvent(List<AttendeeEntity> attendees){
        this.result = attendees;
    }
}
