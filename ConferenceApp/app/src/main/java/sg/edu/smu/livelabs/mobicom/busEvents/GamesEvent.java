package sg.edu.smu.livelabs.mobicom.busEvents;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.models.data.GameListEntity;

/**
 * Created by smu on 20/7/15.
 */
public class GamesEvent {
    public List<GameListEntity> gameListEntityList;
    public String status;

    public GamesEvent(String status) {
        this.status = status;
    }

    public GamesEvent(List<GameListEntity> gameListEntityList, String status) {
        this.gameListEntityList = gameListEntityList;
        this.status = status;
    }
}
