package sg.edu.smu.livelabs.mobicom.models.data;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "BADGE_RULE_ENTITY".
 */
public class BadgeRuleEntity {

    private Long id;
    private Long badgesId;
    private Integer count;
    private Boolean isUnlocked;
    private String completeBadgeToUnlock;

    public BadgeRuleEntity() {
    }

    public BadgeRuleEntity(Long id) {
        this.id = id;
    }

    public BadgeRuleEntity(Long id, Long badgesId, Integer count, Boolean isUnlocked, String completeBadgeToUnlock) {
        this.id = id;
        this.badgesId = badgesId;
        this.count = count;
        this.isUnlocked = isUnlocked;
        this.completeBadgeToUnlock = completeBadgeToUnlock;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBadgesId() {
        return badgesId;
    }

    public void setBadgesId(Long badgesId) {
        this.badgesId = badgesId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Boolean getIsUnlocked() {
        return isUnlocked;
    }

    public void setIsUnlocked(Boolean isUnlocked) {
        this.isUnlocked = isUnlocked;
    }

    public String getCompleteBadgeToUnlock() {
        return completeBadgeToUnlock;
    }

    public void setCompleteBadgeToUnlock(String completeBadgeToUnlock) {
        this.completeBadgeToUnlock = completeBadgeToUnlock;
    }

}
