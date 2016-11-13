package sg.edu.smu.livelabs.mobicom.busEvents;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.net.response.SelfieUserResponse;

/**
 * Created by smu on 26/11/15.
 */
public class SelfieLikerEvent {
    public boolean isFromMainPage = false;
    public boolean hasExecuted = false;
    public List<SelfieUserResponse> users = null;
}
