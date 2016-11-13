package sg.edu.smu.livelabs.mobicom.models;

import sg.edu.smu.livelabs.mobicom.net.item.Selfie;

/**
 * Created by smu on 2/11/15.
 */
public class SelfieLine {
    private Selfie frist;
    private Selfie second;
    private Selfie third;

    public SelfieLine(){

    }

    public SelfieLine(Selfie frist, Selfie second, Selfie third) {
        this.frist = frist;
        this.second = second;
        this.third = third;
    }

    public Selfie getFrist() {
        return frist;
    }

    public Selfie getSecond() {
        return second;
    }

    public Selfie getThird() {
        return third;
    }

    public void setFrist(Selfie frist) {
        this.frist = frist;
    }

    public void setSecond(Selfie second) {
        this.second = second;
    }

    public void setThird(Selfie third) {
        this.third = third;
    }
}
