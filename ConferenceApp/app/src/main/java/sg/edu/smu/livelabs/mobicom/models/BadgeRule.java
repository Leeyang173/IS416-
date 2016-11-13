package sg.edu.smu.livelabs.mobicom.models;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.models.data.BadgeEntity;
import sg.edu.smu.livelabs.mobicom.models.data.BadgeRuleEntity;

/**
 * Created by johnlee on 22/2/16.
 */
public class BadgeRule {

    public BadgeEntity badgeEntity;
    public List<BadgeRuleEntity> badgeRuleEntityList;

    public BadgeRule(BadgeEntity badgeEntity, List<BadgeRuleEntity> badgeRuleEntityList) {
        this.badgeEntity = badgeEntity;
        this.badgeRuleEntityList = badgeRuleEntityList;
    }
}
