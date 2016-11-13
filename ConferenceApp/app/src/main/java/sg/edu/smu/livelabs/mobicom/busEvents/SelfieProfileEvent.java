package sg.edu.smu.livelabs.mobicom.busEvents;

import java.io.Serializable;
import java.util.List;

import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.net.item.Selfie;


/**
 * Created by smu on 6/11/15.
 */
public class SelfieProfileEvent implements Serializable {
    public int previousPage = -1;
    public User user;
    public int totalPhotos;
    public List<Selfie> photos;
    public boolean isFromMainPage;
    public boolean hasExecuted;

    public SelfieProfileEvent(User user, int totalPhotos, List<Selfie> photos, boolean isFromMainPage, int previousPage) {
        this.user = user;
        this.totalPhotos = totalPhotos;
        this.photos = photos;
        this.isFromMainPage = isFromMainPage;
        this.hasExecuted = false;
        this.previousPage = previousPage;
    }
}
