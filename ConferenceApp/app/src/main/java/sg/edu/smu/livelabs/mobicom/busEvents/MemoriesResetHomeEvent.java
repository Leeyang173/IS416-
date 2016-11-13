package sg.edu.smu.livelabs.mobicom.busEvents;

import java.util.Date;


/**
 * Created by smu on 6/11/15.
 */
public class MemoriesResetHomeEvent {
    public int index;
    public Date date;

    public MemoriesResetHomeEvent(int index) {
        this.index = index;
    }

    public MemoriesResetHomeEvent(Date date, int index) {
        this.date = date;
        this.index = index;
    }
}
