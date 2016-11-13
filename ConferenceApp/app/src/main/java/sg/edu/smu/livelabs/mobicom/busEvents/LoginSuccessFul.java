package sg.edu.smu.livelabs.mobicom.busEvents;

/**
 * Created by smu on 18/3/16.
 */
public class LoginSuccessFul {
    public boolean firstTimeLogin;
    public boolean onResume;

    public LoginSuccessFul(boolean firstTimeLogin, boolean onResume) {
        this.firstTimeLogin = firstTimeLogin;
        this.onResume = onResume;
    }

    public LoginSuccessFul() {
    }
}
