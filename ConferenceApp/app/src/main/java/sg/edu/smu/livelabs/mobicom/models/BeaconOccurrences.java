package sg.edu.smu.livelabs.mobicom.models;

import org.altbeacon.beacon.Beacon;

/**
 * Created by smu on 22/1/16.
 */
public class BeaconOccurrences {

    public Beacon beacon;
    public int count;

    public BeaconOccurrences(Beacon beacon, int count) {
        this.beacon = beacon;
        this.count = count;
    }

    public Beacon getBeacon() {
        return beacon;
    }

    public void setBeacon(Beacon beacon) {
        this.beacon = beacon;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
