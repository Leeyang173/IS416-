package sg.edu.smu.livelabs.mobicom.busEvents;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.net.item.RankingItem;

/**
 * Created by smu on 20/7/15.
 */
public class RankingEvent {
    public List<RankingItem> rankingItems;
    public List<RankingItem> assetRankingItems;
    public int userRanking;
    public String userBadgeCount;
    public String status;
    public int myPosition;

    public RankingEvent(String status) {
        this.status = status;
    }

    public RankingEvent(String status, List<RankingItem> rankingItems) {
        this.status = status;
        this.rankingItems = rankingItems;
    }

    public RankingEvent(String status, List<RankingItem> rankingItems, int myPosition) {
        this.status = status;
        this.rankingItems = rankingItems;
        this.myPosition = myPosition;
    }

    public RankingEvent(List<RankingItem> rankingItems, int userRanking, String userBadgeCount, String status) {
        this.rankingItems = rankingItems;
        this.userRanking = userRanking;
        this.userBadgeCount = userBadgeCount;
        this.status = status;
    }

    public RankingEvent(List<RankingItem> rankingItems, List<RankingItem> assetRankingItems, int userRanking, String userBadgeCount, String status) {
        this.rankingItems = rankingItems;
        this.assetRankingItems = assetRankingItems;
        this.userRanking = userRanking;
        this.userBadgeCount = userBadgeCount;
        this.status = status;
    }
}
