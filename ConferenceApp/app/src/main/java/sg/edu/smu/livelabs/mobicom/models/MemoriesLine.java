package sg.edu.smu.livelabs.mobicom.models;

import sg.edu.smu.livelabs.mobicom.net.item.MemoriesItem;

/**
 * Created by smu on 2/11/15.
 */
public class MemoriesLine {
    private MemoriesItem frist;
    private MemoriesItem second;
    private MemoriesItem third;

    public MemoriesLine(){

    }

    public MemoriesLine(MemoriesItem frist, MemoriesItem second, MemoriesItem third) {
        this.frist = frist;
        this.second = second;
        this.third = third;
    }

    public MemoriesItem getFrist() {
        return frist;
    }

    public void setFrist(MemoriesItem frist) {
        this.frist = frist;
    }

    public MemoriesItem getSecond() {
        return second;
    }

    public void setSecond(MemoriesItem second) {
        this.second = second;
    }

    public MemoriesItem getThird() {
        return third;
    }

    public void setThird(MemoriesItem third) {
        this.third = third;
    }
}
