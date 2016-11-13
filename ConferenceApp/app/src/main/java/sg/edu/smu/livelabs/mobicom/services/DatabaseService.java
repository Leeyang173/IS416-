package sg.edu.smu.livelabs.mobicom.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.AttendeeEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.BadgeEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.BadgeRuleEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.BeaconEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.ChatMessageEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.ChatRoomEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.CommentEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.DaoMaster;
import sg.edu.smu.livelabs.mobicom.models.data.DaoSession;
import sg.edu.smu.livelabs.mobicom.models.data.EventEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.GameEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.GameEventEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.GameListEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.IceBreakerFriendsEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.IceBreakerLeaderBoardEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.InboxEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.InterestsEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.MasterPointEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.PaperEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.PaperEventEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.ScavengerEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.ScavengerGroupDetailEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.ScavengerWinnersEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.SearchKeyEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.SurveyEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.TopicEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.UserBadgeEntityDao;

/**
 * Created by smu on 18/1/16.
 */
public class DatabaseService {
    private static final DatabaseService instance = new DatabaseService();


    public static DatabaseService getInstance(){return instance;};
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private DaoMaster.DevOpenHelper helper;
    private SQLiteDatabase db;
    private AttendeeEntityDao attendeeEntityDao;
    private EventEntityDao eventEntityDao;
    private GameEventEntityDao gameEventEntityDao;
    private PaperEventEntityDao paperEventEntityDao;
    private PaperEntityDao paperEntityDao;
    private CommentEntityDao commentEntityDao;
    private TopicEntityDao topicEntityDao;
    private ChatRoomEntityDao chatRoomEntityDao;
    private ChatMessageEntityDao chatMessageEntityDao;
    private MasterPointEntityDao masterPointEntityDao;
    private InterestsEntityDao interestsEntityDao;
    private SearchKeyEntityDao searchKeyEntityDao;
    private BeaconEntityDao beaconEntityDao;
    private IceBreakerFriendsEntityDao iceBreakerFriendsEntityDao;
    private IceBreakerLeaderBoardEntityDao iceBreakerLeaderBoardEntityDao;
    private SurveyEntityDao surveyEntityDao;
    private BadgeEntityDao badgeEntityDao;
    private BadgeRuleEntityDao bagdeRuleEntityDao;
    private UserBadgeEntityDao userBadgeEntityDao;
    private GameEntityDao gameEntityDao;
    private ScavengerEntityDao scavengerEntityDao;
    private GameListEntityDao gameListEntityDao;
    private ScavengerGroupDetailEntityDao scavengerGroupDetailEntityDao;
    private ScavengerWinnersEntityDao scavengerWinnersEntityDAO;
    private InboxEntityDao inboxEntityDao;
    private User me;
    private Context context;

    public void init(Context context){
        this.context = context;
        helper = new DaoMaster.DevOpenHelper(context, "conference-db", null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        attendeeEntityDao = daoSession.getAttendeeEntityDao();
        eventEntityDao = daoSession.getEventEntityDao();
        gameEventEntityDao = daoSession.getGameEventEntityDao();
        paperEventEntityDao = daoSession.getPaperEventEntityDao();
        paperEntityDao = daoSession.getPaperEntityDao();
        commentEntityDao = daoSession.getCommentEntityDao();
        topicEntityDao = daoSession.getTopicEntityDao();
        chatRoomEntityDao = daoSession.getChatRoomEntityDao();
        chatMessageEntityDao = daoSession.getChatMessageEntityDao();
        masterPointEntityDao = daoSession.getMasterPointEntityDao();
        interestsEntityDao = daoSession.getInterestsEntityDao();
        searchKeyEntityDao = daoSession.getSearchKeyEntityDao();
        beaconEntityDao = daoSession.getBeaconEntityDao();
        iceBreakerFriendsEntityDao = daoSession.getIceBreakerFriendsEntityDao();
        iceBreakerLeaderBoardEntityDao = daoSession.getIceBreakerLeaderBoardEntityDao();
        surveyEntityDao = daoSession.getSurveyEntityDao();
        badgeEntityDao = daoSession.getBadgeEntityDao();
        bagdeRuleEntityDao = daoSession.getBadgeRuleEntityDao();
        userBadgeEntityDao = daoSession.getUserBadgeEntityDao();
        gameEntityDao = daoSession.getGameEntityDao();
        scavengerEntityDao = daoSession.getScavengerEntityDao();
        scavengerGroupDetailEntityDao = daoSession.getScavengerGroupDetailEntityDao();
        gameListEntityDao = daoSession.getGameListEntityDao();
        scavengerWinnersEntityDAO = daoSession.getScavengerWinnersEntityDao();
        inboxEntityDao = daoSession.getInboxEntityDao();
    }

    public SQLiteDatabase getDatabase() {
        return db;
    }

    public void setMe(User me) {
        this.me = me;
        SharedPreferences preferences = context.getSharedPreferences("MOBICOM_USER_DBSERVICE1", Context.MODE_PRIVATE);
        preferences
                .edit()
                .clear()
                .putLong("uid", me.getUID())
                .putString("email", me.getEmail())
                .putString("password", me.getPassword())
                .putString("qrCode", me.getQrCode())
                .putString("name", me.getName())
                .putString("avatar", me.getAvatar())
                .putString("cover", me.getCover())
                .putString("status", me.getStatus())
                .putString("roleStr", me.getRoleStr())
                .putString("designation", me.getDesignation())
                .putString("school", me.getSchool())
                .putString("interestsStr", me.getInterestsStr())
                .putInt("totalPoints", me.getTotalPoints())
                .putString("session_token", me.getSessionToken())
                .putString("user_handle", me.getUserHandle())
                .commit();
    }

    public User getMe() {
        if (me == null) {
            me = new User();
            SharedPreferences preferences = context.getSharedPreferences("MOBICOM_USER_DBSERVICE1", Context.MODE_PRIVATE);
            long uid = preferences.getLong("uid", -1);
            me.setUID(uid);
            if (uid != -1) {
                me.setEmail(preferences.getString("email", ""));
                me.setPassword(preferences.getString("password", ""));
                me.setQrCode(preferences.getString("qrCode", ""));
                me.setName(preferences.getString("name", ""));
                me.setAvatar(preferences.getString("avatar", ""));
                me.setCover(preferences.getString("cover", ""));
                me.setStatus(preferences.getString("status", ""));
                me.setRoleStr(preferences.getString("roleStr", ""));
                me.setDesignation(preferences.getString("designation", ""));
                me.setSchool(preferences.getString("school", ""));
                me.setInterestsStr(preferences.getString("interestsStr", ""));
                me.setTotalPoints(preferences.getInt("totalPoints", 0));
                me.setSessionToken(preferences.getString("session_token", ""));
                me.setUserHandle(preferences.getString("user_handle", ""));
            }
        }
        return me;
    }

    public DaoSession getDaoSession(){
        return daoSession;
    }

    public AttendeeEntityDao getAttendeeEntityDao(){
        return attendeeEntityDao;
    }

    public EventEntityDao getEventEntityDao(){
        return eventEntityDao;
    }

    public PaperEntityDao getPaperEntityDao(){
        return paperEntityDao;
    }

    public PaperEventEntityDao getPaperEventEntityDao(){
        return paperEventEntityDao;
    }

    public ChatRoomEntityDao getChatRoomEntityDao(){
        return chatRoomEntityDao;
    }

    public ChatMessageEntityDao getChatMessageEntityDao(){
        return chatMessageEntityDao;
    }

    public MasterPointEntityDao getMasterPointEntityDao() {
        return masterPointEntityDao;
    }

    public InterestsEntityDao getInterestsEntityDao() {
        return interestsEntityDao;
    }

    public SearchKeyEntityDao getSearchKeyEntityDao() {
        return searchKeyEntityDao;
    }

    public BeaconEntityDao getBeaconEntityDao() {
        return beaconEntityDao;
    }

    public IceBreakerFriendsEntityDao getIceBreakerFriendsEntityDao(){
        return iceBreakerFriendsEntityDao;
    }

    public IceBreakerLeaderBoardEntityDao getIceBreakerLeaderBoardEntityDao(){
        return iceBreakerLeaderBoardEntityDao;
    }

    public SurveyEntityDao getSurveyEntityDao(){
        return surveyEntityDao;
    }

    public BadgeRuleEntityDao getBagdeRuleEntityDao (){
        return bagdeRuleEntityDao;
    }

    public BadgeEntityDao getBadgeEntityDao(){
        return  badgeEntityDao;
    }

    public UserBadgeEntityDao getUserBadgeEntityDao(){
        return userBadgeEntityDao;
    }

    public GameEntityDao getGameEntityDao(){
        return gameEntityDao;
    }

    public ScavengerEntityDao getScavengerEntityDao(){
        return scavengerEntityDao;
    }

    public ScavengerGroupDetailEntityDao getScavengerGroupDetailEntityDao(){
        return scavengerGroupDetailEntityDao;
    }

    public CommentEntityDao getCommentEntityDao(){
        return commentEntityDao;
    }

    public TopicEntityDao getTopicEntityDao(){
        return topicEntityDao;
    }

    public GameListEntityDao getGameListEntityDao(){
        return gameListEntityDao;
    }

    public ScavengerWinnersEntityDao getScavengerWinnersEntityDAO(){
        return scavengerWinnersEntityDAO;
    }

    public GameEventEntityDao getGameEventEntityDao(){
        return gameEventEntityDao;
    }

    public InboxEntityDao getInboxEntityDao(){
        return inboxEntityDao;
    }
}
