package sg.edu.smu.livelabs.mobicom.models;

/**
 * Created by johnlee on 22/2/16.
 */
public class MasterPoint {

    private int counter;
    private  int gameId;

    public MasterPoint() {
    }

    public MasterPoint(int counter, int gameId) {
        this.counter = counter;
        this.gameId = gameId;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }
}
