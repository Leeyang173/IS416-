package sg.edu.smu.livelabs.mobicom.busEvents;

/**
 * Created by smu on 21/3/16.
 */
public class LoginFail {
    public boolean firstTimeLogin;

    public LoginFail(boolean firstTimeLogin) {
        this.firstTimeLogin = firstTimeLogin;
    }

    public LoginFail() {
    }
}
