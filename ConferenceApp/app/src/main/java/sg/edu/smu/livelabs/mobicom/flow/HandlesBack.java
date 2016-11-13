package sg.edu.smu.livelabs.mobicom.flow;

/**
 * Created by Aftershock PC on 30/6/2015.
 */
public interface HandlesBack {
    /**
     * Returns <code>true</code> if back event was handled, <code>false</code> if someone higher in
     * the chain should.
     */
    boolean onBackPressed();
}
