package sg.edu.smu.livelabs.mobicom.models.data;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "BADGE_ENTITY".
 */
public class BadgeEntity {

    private Long id;
    private String badges;
    private Integer max;
    private Integer countAchieved;
    private String keyword;
    private Integer badgesType;
    private String description;
    private Integer gameId;
    private String imageId;
    private String playNow;
    private java.util.Date lastUpdated;

    public BadgeEntity() {
    }

    public BadgeEntity(Long id) {
        this.id = id;
    }

    public BadgeEntity(Long id, String badges, Integer max, Integer countAchieved, String keyword, Integer badgesType, String description, Integer gameId, String imageId, String playNow, java.util.Date lastUpdated) {
        this.id = id;
        this.badges = badges;
        this.max = max;
        this.countAchieved = countAchieved;
        this.keyword = keyword;
        this.badgesType = badgesType;
        this.description = description;
        this.gameId = gameId;
        this.imageId = imageId;
        this.playNow = playNow;
        this.lastUpdated = lastUpdated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBadges() {
        return badges;
    }

    public void setBadges(String badges) {
        this.badges = badges;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Integer getCountAchieved() {
        return countAchieved;
    }

    public void setCountAchieved(Integer countAchieved) {
        this.countAchieved = countAchieved;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getBadgesType() {
        return badgesType;
    }

    public void setBadgesType(Integer badgesType) {
        this.badgesType = badgesType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getPlayNow() {
        return playNow;
    }

    public void setPlayNow(String playNow) {
        this.playNow = playNow;
    }

    public java.util.Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(java.util.Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

}
