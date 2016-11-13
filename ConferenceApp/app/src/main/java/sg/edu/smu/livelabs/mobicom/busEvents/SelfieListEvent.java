package sg.edu.smu.livelabs.mobicom.busEvents;

import java.io.Serializable;
import java.util.List;

import sg.edu.smu.livelabs.mobicom.net.item.Selfie;

/**
 * Created by smu on 3/11/15.
 */
public class SelfieListEvent implements Serializable {
    public int previousPage;
    public boolean isFromMainPage = false;
    public boolean hasExecuted = false;
    public boolean isProfilePhoto = false;
    public boolean isLoadMore = false;
    public int current;
    public List<Selfie> selfies;
}
