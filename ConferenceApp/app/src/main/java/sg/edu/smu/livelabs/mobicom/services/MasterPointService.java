package sg.edu.smu.livelabs.mobicom.services;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.squareup.otto.Bus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sg.edu.smu.livelabs.mobicom.ToolTipsWindow;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgesEvent;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.BadgeEntity;
import sg.edu.smu.livelabs.mobicom.models.data.BadgeEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.BadgeRuleEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.GameEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.MasterPointEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.UserBadgeEntityDao;
import sg.edu.smu.livelabs.mobicom.net.api.BadgeApi;
import sg.edu.smu.livelabs.mobicom.net.item.BadgeItem;
import sg.edu.smu.livelabs.mobicom.net.response.BadgeResponse;

/**
 * Created by john lee on 28/2/16.
 */

/**
 Game Table (get update from getGameNameAPI)
 game_id	Name
 1	ice breaker
 2	profile
 3	scavenger hunt
 4	polling
 5	post comment
 6	rating event
 7	survey
 8	quiz
 9	demo
 10	coolfie photo upload
 11	coolfie photo like
 12	favorite
 13	stump
 */

public class MasterPointService extends GeneralService {
    private static final MasterPointService instance = new MasterPointService();
    public static int ICE_BREAKER = 1;
    public static int PROFILE = 2;
    public static int SCAVENGER_HUNT = 3;
    public static int POLLING = 4;
    public static int POST_COMMENT = 5;
    public static int RATING = 6;
    public static int SURVEY = 7;
    public static int QUIZ = 8;
    public static int DEMO = 9;
    public static int COOLFIE_PHOTO_UPLOAD = 10;
    public static int COOLFIE_PHOTO_LIKE = 11;
    public static int FAVORITE = 12;
    public static int STUMP = 13;
    public static int PROFILE_INTEEST = 14;
    public static int DEMO_RATING = 15;
    private int count = 0; //to keep track of number of mobi alert that came out

    public static MasterPointService getInstance() {
        return instance;
    }

    private MasterPointEntityDao masterPointEntityDao;
    private BadgeEntityDao badgeEntityDao;
    private BadgeRuleEntityDao badgeRuleEntityDao;
    private UserBadgeEntityDao userBadgeEntityDao;
    private GameEntityDao gameEntityDao;


    private int screenWidth;
    private int screenHeight;
    private ToolTipsWindow toolTipsWindow;

    private BadgeApi badgeApi;

    public void init(Context context, BadgeApi badgeApi, Bus bus) {
        this.context = context;
        masterPointEntityDao = DatabaseService.getInstance().getMasterPointEntityDao();
        badgeEntityDao = DatabaseService.getInstance().getBadgeEntityDao();
        badgeRuleEntityDao = DatabaseService.getInstance().getBagdeRuleEntityDao();
        userBadgeEntityDao = DatabaseService.getInstance().getUserBadgeEntityDao();
        gameEntityDao = DatabaseService.getInstance().getGameEntityDao();

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        this.bus = bus;
        this.badgeApi = badgeApi;
    }


    public List<BadgeEntity> getBadges(){
        return badgeEntityDao.queryBuilder().orderAsc(BadgeEntityDao.Properties.Max, BadgeEntityDao.Properties.BadgesType,
                BadgeEntityDao.Properties.Badges).list();
    }



    public void showToolTips(final View view, final String badgeName) {
        if (toolTipsWindow != null && toolTipsWindow.isTooltipShown()) {
            Handler handler = new Handler();
            long sec = count * 4000;
            final Context c = this.context;
            count++;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toolTipsWindow.dismissTooltip();
                    toolTipsWindow = new ToolTipsWindow(c, badgeName + " badge unlocked", new TooltipCount() {
                        @Override
                        public void reduceCallBack() {
                            count--;
                        }
                    });
                    toolTipsWindow.showToolTip(view, screenWidth, screenHeight);
                }
            }, sec);
        }
        else{
            count++;
            toolTipsWindow = new ToolTipsWindow(this.context, badgeName + " badge unlocked", new TooltipCount() {
                @Override
                public void reduceCallBack() {
                    count--;
                }
            });
            toolTipsWindow.showToolTip(view, screenWidth, screenHeight);
        }


    }

    public int getNumberOfUserAchievedBadge(){
        List<BadgeEntity> badgeEntities = badgeEntityDao.queryBuilder().build().list();
        int count = 0;
        for(BadgeEntity b: badgeEntities){
            count += b.getCountAchieved();
        }

        return count;
    }

    /**
     *
     * @param badgeItems
     */
    public void updateBadges(List<BadgeItem> badgeItems){
        List<BadgeEntity> badgeEntities = new ArrayList<BadgeEntity>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);

        try{
            for(BadgeItem badgeItem: badgeItems){
                BadgeEntity badgeEntity = new BadgeEntity();
                badgeEntity.setId(Long.parseLong(badgeItem.id));
                badgeEntity.setBadges(badgeItem.badgeName);
                badgeEntity.setBadgesType(Integer.parseInt(badgeItem.badgeType));
                badgeEntity.setDescription(badgeItem.description);
                badgeEntity.setGameId(Integer.parseInt(badgeItem.gameId));
                badgeEntity.setImageId(badgeItem.imageUrl);
                badgeEntity.setMax(badgeItem.starCount);
                badgeEntity.setCountAchieved(badgeItem.countAchieved);
                badgeEntity.setKeyword(badgeItem.keyword);
                badgeEntity.setPlayNow(badgeItem.playNow);
                badgeEntity.setLastUpdated(new Date(df.parse(badgeItem.lastModifiedTime).getTime() + 1000)); //add additional 1 sec to round the microsecond
                badgeEntities.add(badgeEntity);
            }

            badgeEntityDao.deleteAll();
            badgeEntityDao.insertOrReplaceInTx(badgeEntities);
        }
        catch (Exception e){

        }

        final Handler mainHandler = new Handler(context.getMainLooper());
        mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    List<BadgeEntity> b = badgeEntityDao.queryBuilder().build().list();
                    if(bus != null) {
                        bus.post(new BadgesEvent(b));
                    }
                }
        });

    }

    public void getBadgesAPI() {
        User me = DatabaseService.getInstance().getMe();
        badgeApi.getBadges(Long.toString(me.getUID()), "").subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Action1<BadgeResponse>() {
                    @Override
                    public void call(BadgeResponse response) {
                        if ("success".equals(response.status)) {
                            if (response.badgeItems != null) {
                                updateBadges(response.badgeItems);

                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("Mobisys:", "Cannot get badges", throwable);
                    }
                });
    }

    public interface TooltipCount{
        void reduceCallBack();
    }
}
