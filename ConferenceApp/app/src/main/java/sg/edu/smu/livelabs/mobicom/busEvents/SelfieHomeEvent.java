package sg.edu.smu.livelabs.mobicom.busEvents;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.net.item.DeletedSelfie;
import sg.edu.smu.livelabs.mobicom.net.item.Selfie;


/**
 * Created by smu on 6/11/15.
 */
public class SelfieHomeEvent {
    public boolean isNext;
    public boolean isFirst;
    public List<Selfie> selfies;
    public List<DeletedSelfie> selfiesToRemove;
}
