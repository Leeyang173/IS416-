package sg.edu.smu.livelabs.mobicom.services;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.greenrobot.dao.query.Query;
import de.hdodenhof.circleimageview.CircleImageView;
import flow.Flow;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.BadgesListAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.BannerEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.BannerPostBackEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.GamesEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.LeaderboardDismissProgressEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.LeaderboardEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.RankingEvent;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.AttendeeEntity;
import sg.edu.smu.livelabs.mobicom.models.data.BadgeEntity;
import sg.edu.smu.livelabs.mobicom.models.data.GameListEntity;
import sg.edu.smu.livelabs.mobicom.models.data.GameListEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.ScavengerEntity;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.api.GameApi;
import sg.edu.smu.livelabs.mobicom.net.item.BadgeItem;
import sg.edu.smu.livelabs.mobicom.net.item.EVAPromotionItem;
import sg.edu.smu.livelabs.mobicom.net.item.GameItem;
import sg.edu.smu.livelabs.mobicom.net.item.RankingItem;
import sg.edu.smu.livelabs.mobicom.net.item.StumpItem;
import sg.edu.smu.livelabs.mobicom.net.response.BadgeResponse;
import sg.edu.smu.livelabs.mobicom.net.response.BannerResponse;
import sg.edu.smu.livelabs.mobicom.net.response.EVAPromotionResponse;
import sg.edu.smu.livelabs.mobicom.net.response.GameResponse;
import sg.edu.smu.livelabs.mobicom.net.response.RankingResponse;
import sg.edu.smu.livelabs.mobicom.net.response.ScavengerHuntListResponse;
import sg.edu.smu.livelabs.mobicom.net.response.StumpResponse;
import sg.edu.smu.livelabs.mobicom.presenters.AgendaPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.ChatPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.BeaconScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.BingoScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ChatScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.EVAPromotionScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.FavoriteItemScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.FavoriteScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ForumScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.IceBreakerScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.LeaderboardScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.MemoriesMainScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.OrganizersScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ProfileScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.QuizScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ScavengerHuntDetailScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ScavengerHuntScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SelfieScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SponsorScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.StumpListScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.StumpScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SurveyScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SurveyWebScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.VotingListScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.VotingScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.YourSGScreen;
import sg.edu.smu.livelabs.mobicom.util.Util;

/**
 * Created by smu on 28/2/16.
 */
public class GameService extends GeneralService {
    private static final GameService instance = new GameService();
    public static GameService getInstance(){return instance;}
    private GameListEntityDao gameListEntityDao;
    private GameApi gameApi;
    private Bus bus;
    private RestClient restClient;
    private User me;

    private Dialog dialog;
    private boolean dialogShownOnce = false;

    public void init(Context context, GameApi gameApi, Bus bus, RestClient restClient){
        this.context = context;
        gameListEntityDao = DatabaseService.getInstance().getGameListEntityDao();
        this.gameApi = gameApi;
        this.bus = bus;
        this.restClient = restClient;
        me = DatabaseService.getInstance().getMe();
    }

    public void initMe(){
        me = DatabaseService.getInstance().getMe();
    }

    public List<GameListEntity> updateGameList(List<GameItem> gameItems){
        List<GameListEntity> gameListEntities = new ArrayList<GameListEntity>();
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);

        try{
            for(GameItem game: gameItems){
                GameListEntity g = new GameListEntity();
                g.setId(Long.parseLong(game.id));
                g.setDescription(game.description);
                g.setGameName(game.title);
                g.setImageURL(game.imageId);
                g.setInsertTime(new Date(game.insertTime.getTime()));
                g.setLastUpdate(new Date(game.lastUdpate.getTime() + 1000));
                g.setStatus(game.status);
                g.setKeyword(game.keyword);
                gameListEntities.add(g);
            }
            gameListEntityDao.deleteAll();
            gameListEntityDao.insertOrReplaceInTx(gameListEntities);
        }
        catch (Exception e){

        }

        return gameListEntities;
    }

    public GameListEntity getGame(long gameId){
        Query<GameListEntity> query = gameListEntityDao.queryBuilder().where(GameListEntityDao.Properties.Id.eq(gameId)).limit(1).build();
        List<GameListEntity> gameListEntities = query.list();
        if (gameListEntities.size() > 0) {
            return gameListEntities.get(0);
        }
        return null;
    }

    public GameListEntity getGameByKeyword(String keyword){
        Query<GameListEntity> query = gameListEntityDao.queryBuilder().where(GameListEntityDao.Properties.Keyword.eq(keyword)).limit(1).build();
        List<GameListEntity> gameListEntities = query.list();
        if (gameListEntities.size() > 0) {
            return gameListEntities.get(0);
        }
        return null;
    }

    /**
     * Get lastest updated time
     * @return
     */
    public GameListEntity getGame(){
        Query<GameListEntity> query = gameListEntityDao.queryBuilder()
                .orderDesc(GameListEntityDao.Properties.LastUpdate).limit(1).build();
        List<GameListEntity> gameListEntities = query.list();
        if (gameListEntities.size() > 0) {
            return gameListEntities.get(0);
        }
        return null;
    }

    public List<GameListEntity> getGames(){
        return gameListEntityDao.queryBuilder().list();
    }

    public int getGameSize(){
        return gameListEntityDao.queryBuilder().list().size();
    }

    public void loadGameAPI(String userId){
        final Handler mainHandler = new Handler(context.getMainLooper());
        final GameListEntity gameListEntity = GameService.getInstance().getGame();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
        String lastTime = "2016-01-01";
        gameApi.getGames(userId, lastTime)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<GameResponse>() {
                    @Override
                    public void call(final GameResponse response) {
//                        UIHelper.getInstance().dismissProgressDialog();

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                List<GameListEntity> gameListEntities = new ArrayList<GameListEntity>();
                                if(response.status.equals("success")) {
                                    if (response.gameItems != null && response.gameItems.size() > 0) {
                                        gameListEntities = GameService.getInstance().updateGameList(response.gameItems);
                                    }
                                }

                                bus.post(new GamesEvent(gameListEntities, response.status));

                            }
                        });

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                        Log.e("Mobisys: ", "cannot get games details", throwable);
                        UIHelper.getInstance().dismissProgressDialog();
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                bus.post(new GamesEvent("failed"));

                            }
                        });

//                        if ( throwable instanceof SocketTimeoutException){
//                            UIHelper.getInstance().showAlert(context, "It seems like there is a Network problem.");
//                        }
                    }
                });
    }

    public void loadHomeRanking(final String userId){
        final Handler mainHandler = new Handler(context.getMainLooper());
        gameApi.getRankingHome(userId)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<RankingResponse>() {
                    @Override
                    public void call(final RankingResponse response) {
                        UIHelper.getInstance().dismissProgressDialog();
                        if(response.status.equals("success")) {
                            if (response.rankingItems != null && response.rankingItems.size() > 0) {
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        int myPosition = 0;
                                        for(RankingItem r:response.rankingItems){
                                            if (r.userId.equals(userId)){
                                                SharedPreferences s = context.getSharedPreferences("Mobicom_MyRankingPref", Context.MODE_PRIVATE);
                                                SharedPreferences.Editor editor = s.edit();
                                                int ranking = r.rank;
                                                if(ranking < 0){
                                                    ranking = 0;
                                                }
                                                editor.putInt("ranking", ranking);
                                                editor.commit();
                                                break;
                                            }
                                            myPosition++;
                                        }
                                        bus.post(new RankingEvent(response.status ,response.rankingItems, myPosition));

                                    }
                                });
                            }
                            else{
                                SharedPreferences s = context.getSharedPreferences("Mobicom_MyRankingPref", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = s.edit();
                                editor.putInt("ranking", 0);
                                editor.commit();
                            }
                        }


                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("Mobisys: ", "cannot get ranking", throwable);

                    }
                });
    }

    public void loadRanking(String userId){

        final Handler mainHandler = new Handler(context.getMainLooper());
        gameApi.getRanking(userId)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<RankingResponse>() {
                    @Override
                    public void call(final RankingResponse response) {
                        UIHelper.getInstance().dismissProgressDialog();
                        if(response.status.equals("success")) {
//                            if (response.rankingItems != null && response.rankingItems.size() > 0) {
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        SharedPreferences s = context.getSharedPreferences("Mobicom_MyRankingPref", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = s.edit();
                                        int ranking = response.userRanking.rank;
                                        if(ranking < 0){
                                            ranking = 0;
                                        }
                                        editor.putInt("ranking", ranking);
                                        editor.commit();

                                        bus.post(new RankingEvent(response.rankingItems,response.assetRankingItems,
                                                response.userRanking.rank, response.userRanking.count, response.status
                                        ));

                                        if(response.assetRankingItems == null || (response.assetRankingItems != null
                                        && response.assetRankingItems.size() <= 0)){
                                            bus.post(new LeaderboardEvent(false));
                                        }
                                        else {
                                            bus.post(new LeaderboardEvent(true));
                                        }

                                    }
                                });
//                            }
//                            else{
//                                mainHandler.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        bus.post(new LeaderboardDismissProgressEvent());
//                                    }
//                                });
//                            }
                        }
                        else{
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    bus.post(new LeaderboardDismissProgressEvent());
                                }
                            });
                        }


                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("Mobisys: ", "cannot get ranking", throwable);
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                bus.post(new LeaderboardDismissProgressEvent());
                            }
                        });
                    }
                });
    }

    public void loadBanners(String userId){
        final Handler mainHandler = new Handler(context.getMainLooper());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
        gameApi.getBanners(userId, "2016-01-01")
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<BannerResponse>() {
                    @Override
                    public void call(final BannerResponse response) {
                        UIHelper.getInstance().dismissProgressDialog();
                        if(response.status.equals("success")) {
                            if (response.bannerItems != null && response.bannerItems.size() > 0) {
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        bus.post(new BannerEvent(response.bannerItems,response.status));

                                    }
                                });
                            }
                        }


                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("Mobisys: ", "cannot get banner", throwable);

                    }
                });
    }

    public boolean fromGames(String name){
        switch (name){
            case "VotingPresenter": return true;
            case "VotingListPresenter": return true;
            case "SurveyWebPresenter": return true;
            case "SurveyPresenter": return true;
            case "SelfiePresenter": return true;
            case "SelfieFullScreenPresenter": return true;
            case "ScavengerHuntPresenter": return true;
            case "ScavengerHuntDetailPresenter": return true;
            case "QuizPresenter": return true;
            case "ProfilePresenter": return true;
            case "PrizePresenter": return true;
            case "IceBreakerPresenter": return true;
            case "IceBreakerLeaderBoardPresenter": return true;
            case "FavoritePresenter": return true;
            case "FavoriteItemPresenter": return true;
            case "BeaconPresenter": return true;
            case "EvaPromotionPresenter": return true;
            case "StumpPresenter": return true;
            case "StumpListPresenter": return true;
            case "MemoriesPresenter": return true;
            case "MemoriesHomePresenter": return true;
            case "MemoriesMainPresenter": return true;

        }
        return false;
    }

    public void loadScavengerHunt(final long id, final String keyword, final MainActivity activity){
        ScavengerEntity s = ScavengerService.getInstance().getScavengerHunt(id);
        if(s != null){
            bus.post(new BannerPostBackEvent(id, keyword, "success"));
        }
        else{
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
            String lastTime = s != null ? df.format(s.getLastUpdate()) : "2016-01-01";

            final Handler mainHandler = new Handler(context.getMainLooper());
            restClient.getScavengerApi().getScavengerHuntList(lastTime, Long.toString(me.getUID()))
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Action1<ScavengerHuntListResponse>() {
                        @Override
                        public void call(final ScavengerHuntListResponse response) {
                            UIHelper.getInstance().dismissProgressDialog();

                            if (response.details != null && response.details.size() > 0) {
                                ScavengerService.getInstance().updateScavengerHuntList(response.details);
                                final ScavengerEntity sTmp = ScavengerService.getInstance().getScavengerHunt(id);

                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
//                                        bus.post(new BannerPostBackEvent(id, keyword, "success"));
                                        Date current = Calendar.getInstance().getTime();
                                        ScavengerEntity s = ScavengerService.getInstance().getScavengerHunt(id);
                                        if(s != null && s.getStartTime() != null && s.getEndTime() != null) { //will only redirect, if hunt started
                                            if (current.after(s.getStartTime()) && current.before(s.getEndTime())
                                                    && !s.getIsCompleted()) {
//                                                Flow.get(activity).set(new ScavengerHuntDetailScreen(s));
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.e("Mobisys: ", "cannot get scavenger hunt", throwable);
                        }
                    });
        }
    }

    public void getEVAPromotions(final long id, final String keyword, final MainActivity activity) {
        final int TYPE_SELFIE_NEXT_COMING = 1;
        final int TYPE_SELFIE_PAST = 2;
        final int TYPE_SELFIE_CURRENT = 3;
        final int TYPE_LUCKY_DRAW = 4;
        final int SELFIE_PHOTOS_PER_PAGE = 20;

        final SimpleDateFormat dateToStrServerFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final SimpleDateFormat dateToStrFormat = new SimpleDateFormat("MMM dd");
        final SimpleDateFormat dateToStrFormat2 = new SimpleDateFormat("dd MMMM");

        final Handler mainHandler = new Handler(context.getMainLooper());
        restClient.getSelfieAPI().getPromotions(Long.toString(me.getUID()))
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<EVAPromotionResponse>() {
                    @Override
                    public void call(EVAPromotionResponse evaPromotionResponse) {
                        if ("success".equals(evaPromotionResponse.status)) {

                            EVAPromotionItem promotions = new EVAPromotionItem();
                            if (evaPromotionResponse.promotions != null) {
                                for (EVAPromotionItem promotion : evaPromotionResponse.promotions) {
                                    if (!"active".equals(promotion.status)) {
                                        continue;
                                    }

                                    if(Long.parseLong(promotion.id) == id) {
                                        promotions = promotion;
                                        if (promotion.timeString == null || promotion.timeString.isEmpty()) {
                                            promotion.timeString = String.format("From %s To %s",
                                                    dateToStrFormat.format(promotion.startTime),
                                                    dateToStrFormat.format(promotion.endTime));
                                        }
                                        promotion.startStr = dateToStrFormat2.format(promotion.startTime);
                                        Date now = new Date();
                                        long currentTime = now.getTime();
                                        long startTime = promotion.startTime.getTime();
                                        long endTime = promotion.endTime.getTime();
                                        if (startTime > currentTime) {
                                            promotion.promotionType = TYPE_SELFIE_NEXT_COMING;
                                            promotion.description = context.getResources().getString(R.string.coming_soon);

                                        } else {
                                            if (endTime < currentTime) {
                                                promotion.promotionType = TYPE_SELFIE_PAST;
                                            } else {
                                                promotion.promotionType = TYPE_SELFIE_CURRENT;
                                            }
                                            if (promotion.imageCount > 1) {
                                                promotion.description = String.format("%d photos", promotion.imageCount);
                                            } else if (promotion.imageCount == 1) {
                                                promotion.description = String.format("%d photo", promotion.imageCount);
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                            EVAPromotionService.getInstance().currentPromotion = promotions;
                            EVAPromotionService.getInstance().setUserId();
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
//                                    bus.post(new BannerPostBackEvent(id, keyword, "success"));
//                                    Flow.get(activity).set(new SelfieScreen());
                                }
                            });
                        }

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                        Log.e("XXX: selfie", "cannot get promotions", throwable);
                    }
                });
    }

    public void loadStumpList(final long stumpId, final MainActivity activity){
        UIHelper.getInstance().showProgressDialog(context, "Loading Stump...", false);
        final Handler mainHandler = new Handler(context.getMainLooper());
        restClient.getStumpApi().getStumps(Long.toString(me.getUID()))
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<StumpResponse>() {
                    @Override
                    public void call(final StumpResponse response) {

                        if(response.status.equals("success")){
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    for(StumpItem s: response.details){
                                        if(s.id.equals(Long.toString(stumpId))){
//                                            Flow.get(activity).set(new StumpScreen(s));
                                            break;
                                        }
                                    }

                                    UIHelper.getInstance().dismissProgressDialog();
                                }
                            });
                        }


                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("Mobisys: ", "cannot get stump list", throwable);

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                UIHelper.getInstance().dismissProgressDialog();
                            }
                        });
                    }
                });
    }

    public boolean checkBeaconsPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            String permission = "android.permission.ACCESS_COARSE_LOCATION";
            int res = context.checkCallingOrSelfPermission(permission);
            return (res == PackageManager.PERMISSION_GRANTED);

        }
        else{
            return true;
        }
    }

    public void drawNameShort(String name, final  ImageView avatar, final TextView shortNameTV){
        if(avatar == null) return;

        if(name == null || name.isEmpty()){
            try {
                Picasso.with(context).load(R.drawable.icon_no_profile).into(avatar);
                shortNameTV.setVisibility(View.GONE);
            }
            catch (Throwable e){
                Log.d("AAA", "GameService:" + e.toString());
            }
        }
        else{
            String nameShortFormTmp = "";
            String[] tmp = name.split(" ");
            for(int i =0; i< tmp.length; i++){
                if(i == 2){//only capture the first char of the first 2 word/name
                    break;
                }

                if(tmp[i].length() > 0) {
                    nameShortFormTmp += tmp[i].charAt(0);
                }

            }

            shortNameTV.setVisibility(View.VISIBLE);
            try {
                avatar.setImageBitmap(drawRectToAvatar());
            }
            catch(Throwable e){
                Log.d("AAA", "GameService:" + e.toString());
            }
            shortNameTV.setText(nameShortFormTmp.toUpperCase());
        }

    }

    private Bitmap drawRectToAvatar(){
        BitmapFactory.Options myOptions = new BitmapFactory.Options();
        myOptions.inDither = true;
        myOptions.inScaled = false;
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important
        myOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.next,myOptions);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(context.getResources().getColor(R.color.avatar_grey));

        Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                context.getResources().getDimension(R.dimen.item_height),
                context.getResources().getDisplayMetrics());
        Canvas canvas = new Canvas(mutableBitmap);
        canvas.drawRect(0, 0, px, px, paint);

        return mutableBitmap;
    }

    /**
     * This function helps to direct to the particular game page based on keyword and target id.
     * If target id is 0, it will only bring user to the particular game list (ex: scavenger hunt) and not
     * directly to the particular hunt
     * @param keyword
     * @param targetId
     * @param activity (MainActivity): this is use for Flow to load, context initialize from app.java cannot be use
     */
    public void openGamePage(String keyword, long targetId, MainActivity activity){

        switch(keyword){
            case "ice breaker":
                Flow.get(activity).set(new IceBreakerScreen());
                break;
            case "polling":
                boolean isMeAdmin = false;
                User user = DatabaseService.getInstance().getMe();
                if(user.getRole() != null && user.getRole().length > 0) {
                    for (String s : user.getRole()) {
                        if (s.trim().toLowerCase().equals("moderator")) {
                            isMeAdmin = true;
                            break;
                        }
                    }
                }
                if (isMeAdmin)
                    Flow.get(activity).set(new VotingScreen(0, "Polling"));
                else
                    Flow.get(activity).set(new VotingListScreen());
                break;
            case "survey":
                if(targetId == 0){
                    Flow.get(activity).set(new SurveyScreen());
                }
                else {
                    Flow.get(activity).set(new SurveyWebScreen(targetId));
                }
                break;

            case "demo":
                Flow.get(activity).set(new BeaconScreen());

                break;
            case "scavenger hunt":
                if(targetId == 0){
                    Flow.get(activity).set(new ScavengerHuntScreen());
                }
                else {
                    loadScavengerHunt(targetId, keyword, activity); //we need to ensure those items are already loaded
                }
                break;
            case "favourite":
                if(targetId == 0) {
                    Flow.get(activity).set(new FavoriteScreen());
                }
                else{
                    Flow.get(activity).set(new FavoriteItemScreen(targetId));
                }
                break;
            case "coolfie":
                if(targetId == 0){
                    Flow.get(activity).set(new EVAPromotionScreen());
                }
                else{
                    getEVAPromotions(targetId, keyword, activity); //we need to load this first
                }
                break;
            case "stump":
                if(targetId == 0){
                    Flow.get(activity).set(new StumpListScreen());
                }
                else {
                    loadStumpList(targetId, activity);
                }
                break;
            case "forum":
                Flow.get(activity).set(new ForumScreen());
                UIHelper.getInstance().showProgressDialog(context, context.getString(R.string.progressing), true);
                AgendaService.getInstance().getMainTopic();
                ForumService.getInstance().syncTopic();
                break;
            case "organizers":
                Flow.get(activity).set(new OrganizersScreen());
                break;
            case "your_sg":
                Flow.get(activity).set(new YourSGScreen());
                break;
            case "leaderboard":
                Flow.get(activity).set(new LeaderboardScreen());
                break;
            case "papers":
                activity.setCurrentTab(MainActivity.AGENDA_TAB, AgendaPresenter.PAPER);
                break;
            case "agenda":
                activity.setCurrentTab(MainActivity.AGENDA_TAB, AgendaPresenter.FULL_AGENDA);
                break;
            case "message":
                activity.setCurrentTab(MainActivity.MESSAGE_TAB, -1);
                break;
            case "sponsors":
                Flow.get(activity).set(new SponsorScreen());
                break;
            case "profile":
                Flow.get(activity).set(new ProfileScreen(true, ""));
                break;
            case "quizes":
                Flow.get(activity).set(new QuizScreen(targetId));
                break;
            case "memories":
                Calendar c = Calendar.getInstance();
                c.set(Calendar.YEAR, 2016);
                c.set(Calendar.MONTH, Calendar.JULY);
                c.set(Calendar.DAY_OF_MONTH, 13);
                MemoriesService.getInstance().currentSelectedDate = c.getTime(); //new Date();
                MemoriesService.getInstance().startDate = c.getTime();
                Flow.get(activity).set(new MemoriesMainScreen());
                break;
            case "photo_bingo":
                Flow.get(activity).set(new BingoScreen());
                break;
        }
    }


    public void showDialog(final RankingItem i, final Context context, ConnectivityManager cm, final  MainActivity mainActivity){
        final AttendeeEntity user = AttendeesService.getInstance().getAttendeesByUID(Long.parseLong(i.userId));
        if((dialog != null && dialog.isShowing()) || (dialog != null && dialogShownOnce) ){
            dialog.dismiss();
            dialogShownOnce = false;
        }
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_user_badge_info);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        CircleImageView closeBtn = (CircleImageView) dialog.findViewById(R.id.close_btn);
        TextView nameTV = (TextView) dialog.findViewById(R.id.name_text);
        TextView desTV = (TextView) dialog.findViewById(R.id.description_text);
        final TextView nameShortFormTV = (TextView) dialog.findViewById(R.id.name_short_form);
        final TextView messageTV = (TextView) dialog.findViewById(R.id.message);
        final CircleImageView personIV = (CircleImageView) dialog.findViewById(R.id.avatar_image);
        final ListView badgesLV = (ListView) dialog.findViewById(R.id.badges_list);
        Button addToPhone  = (Button) dialog.findViewById(R.id.add_to_phone_btn);
        Button message  = (Button) dialog.findViewById(R.id.message_btn);

        if(i.userId.equals(Long.toString(me.getUID()))){
            addToPhone.setVisibility(View.GONE);
            message.setVisibility(View.GONE);
        }

        nameTV.setText(i.name);
        if(user !=null && user.getDescription() != null)
            desTV.setText(user.getDescription());
        if(i.avatarId != null && !i.avatarId.isEmpty()){
            try {
                Picasso.with(context).load(Util.getPhotoUrlFromId(i.avatarId, 256))
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .placeholder(R.drawable.empty_profile).into(personIV, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        nameShortFormTV.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() { //when failed to load, draw a rect on top of the noavatar and add the name short form
                        GameService.getInstance().drawNameShort(i.name, personIV, nameShortFormTV);
                    }
                });
            }
            catch(Throwable e){
                Log.d("AAA", "GameService:showDialog:" + e.toString());
            }
        }
        else{
            GameService.getInstance().drawNameShort(i.name, personIV, nameShortFormTV);
        }

        addToPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user != null) {
                    addToContact(user, mainActivity);
                }
            }
        });

        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( App.getInstance().currentChatType != ChatPresenter.SINGLE_TYPE){
                    if(user != null) {
                        Flow.get(context).set(new ChatScreen(ChatService.
                                getInstance().findSingleChatRoom(user, true)));
                        dialog.dismiss();
                        dialogShownOnce = false;
                    }
                }
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dialogShownOnce = false;
            }
        });

        if(cm.getActiveNetworkInfo() != null) {
            final Handler mainHandler = new Handler(context.getMainLooper());
            restClient.getBadgeApi().getBadges(i.userId, "").subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .subscribe(new Action1<BadgeResponse>() {
                        @Override
                        public void call(BadgeResponse response) {
                            if ("success".equals(response.status)) {
                                if (response.badgeItems != null) {
                                    final List<BadgeEntity> badgeEntities = new ArrayList<BadgeEntity>();
                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);

                                    try {
                                        for (BadgeItem badgeItem : response.badgeItems) {
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
                                            badgeEntity.setLastUpdated(new Date(df.parse(badgeItem.lastModifiedTime).getTime() + 1000)); //add additional 1 sec to round the microsecond
                                            badgeEntities.add(badgeEntity);
                                        }

                                        Collections.sort(badgeEntities, new Comparator<BadgeEntity>() {
                                            public int compare(BadgeEntity b1, BadgeEntity b2) {
                                                return String.valueOf(b1.getBadges()).compareTo(b2.getBadges());
                                            }
                                        });

                                        //sort by max count and type
                                        Collections.sort(badgeEntities, new Comparator<BadgeEntity>() {
                                            public int compare(BadgeEntity b1, BadgeEntity b2) {
                                                int c;
                                                c = Integer.valueOf(b1.getMax()).compareTo(b2.getMax());
                                                if (c == 0)
                                                    c = Integer.valueOf(b1.getBadgesType()).compareTo(b2.getBadgesType());


                                                return c;
                                            }
                                        });


                                        mainHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if(badgeEntities != null && badgeEntities.size() > 0) {
                                                    BadgesListAdapter badgesListAdapter = new BadgesListAdapter(context, bus, badgeEntities);
                                                    badgesLV.setAdapter(badgesListAdapter);
                                                }
                                            }
                                        });

                                    } catch (Throwable throwable) {
                                        messageTV.setVisibility(View.VISIBLE);
                                        messageTV.setText("Badges list cannot be loaded, Please try again later. ");
                                        badgesLV.setVisibility(View.GONE);
                                    }

                                }
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.e("Mobisys:", "Cannot get attendee badges", throwable);
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    messageTV.setVisibility(View.VISIBLE);
                                    messageTV.setText("Badges list cannot be loaded, Please try again later. ");
                                    badgesLV.setVisibility(View.GONE);
                                }
                            });
                        }
                    });
        }
        else{
            messageTV.setVisibility(View.VISIBLE);
            messageTV.setText(context.getResources().getText(R.string.no_internet_connection));
            badgesLV.setVisibility(View.GONE);
        }

        if((dialog != null && dialog.isShowing()) || (dialog != null && dialogShownOnce) ){
            dialog.dismiss();
            dialogShownOnce = false;
        }
        dialog.show();
        dialogShownOnce = true;
    }

    public void closeDialog(){
        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }

    public void showDialogIceBreaker(final Long userId, final String name, final String avatarId, final Context context, ConnectivityManager cm, final  MainActivity mainActivity){
        final AttendeeEntity user = AttendeesService.getInstance().getAttendeesByUID(userId);
        if((dialog != null && dialog.isShowing()) || (dialog != null && dialogShownOnce) ){
            dialog.dismiss();
            dialogShownOnce = false;
        }
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_user_badge_info);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        CircleImageView closeBtn = (CircleImageView) dialog.findViewById(R.id.close_btn);
        TextView nameTV = (TextView) dialog.findViewById(R.id.name_text);
        TextView desTV = (TextView) dialog.findViewById(R.id.description_text);
        final TextView nameShortFormTV = (TextView) dialog.findViewById(R.id.name_short_form);
        final TextView messageTV = (TextView) dialog.findViewById(R.id.message);
        final CircleImageView personIV = (CircleImageView) dialog.findViewById(R.id.avatar_image);
        final ListView badgesLV = (ListView) dialog.findViewById(R.id.badges_list);
        Button addToPhone  = (Button) dialog.findViewById(R.id.add_to_phone_btn);
        Button message  = (Button) dialog.findViewById(R.id.message_btn);

        if(userId == me.getUID()){
            addToPhone.setVisibility(View.GONE);
            message.setVisibility(View.GONE);
        }

        nameTV.setText(name);
        if(user !=null && user.getDescription() != null)
            desTV.setText(user.getDescription());
        if(avatarId != null && !avatarId.isEmpty()){
            try {
                Picasso.with(context).load(Util.getPhotoUrlFromId(avatarId, 256))
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .placeholder(R.drawable.empty_profile).into(personIV, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        nameShortFormTV.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() { //when failed to load, draw a rect on top of the noavatar and add the name short form
                        GameService.getInstance().drawNameShort(name, personIV, nameShortFormTV);
                    }
                });
            }
            catch (Throwable e){
                Log.d("AAA", "GameService:showDialogIceBreaker" + e.toString());
            }
        }
        else{
            GameService.getInstance().drawNameShort(name, personIV, nameShortFormTV);
        }

        addToPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user != null) {
                    addToContact(user, mainActivity);
                }
            }
        });

        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( App.getInstance().currentChatType != ChatPresenter.SINGLE_TYPE){
                    if(user != null) {
                        Flow.get(context).set(new ChatScreen(ChatService.
                                getInstance().findSingleChatRoom(user, true)));
                        dialog.dismiss();
                    }
                }
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dialogShownOnce = false;
            }
        });

        if(cm.getActiveNetworkInfo() != null) {
            final Handler mainHandler = new Handler(context.getMainLooper());
            restClient.getBadgeApi().getBadges(Long.toString(userId), "").subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .subscribe(new Action1<BadgeResponse>() {
                        @Override
                        public void call(BadgeResponse response) {
                            if ("success".equals(response.status)) {
                                if (response.badgeItems != null) {
                                    final List<BadgeEntity> badgeEntities = new ArrayList<BadgeEntity>();
                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);

                                    try {
                                        for (BadgeItem badgeItem : response.badgeItems) {
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
                                            badgeEntity.setLastUpdated(new Date(df.parse(badgeItem.lastModifiedTime).getTime() + 1000)); //add additional 1 sec to round the microsecond
                                            badgeEntities.add(badgeEntity);
                                        }


                                        //sort by max count and type
                                        Collections.sort(badgeEntities, new Comparator<BadgeEntity>() {
                                            public int compare(BadgeEntity b1, BadgeEntity b2) {
                                                int c;
                                                c = Integer.valueOf(b1.getMax()).compareTo(b2.getMax());
                                                if (c == 0)
                                                    c = Integer.valueOf(b1.getBadgesType()).compareTo(b2.getBadgesType());

                                                return c;
                                            }
                                        });


                                        mainHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                BadgesListAdapter badgesListAdapter = new BadgesListAdapter(context, bus, badgeEntities);
                                                badgesLV.setAdapter(badgesListAdapter);
                                            }
                                        });

                                    } catch (Exception e) {

                                    }

                                }
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.e("Mobisys:", "Cannot get attendee badges", throwable);
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    messageTV.setVisibility(View.VISIBLE);
                                    messageTV.setText("Badges list cannot be loaded, Please try again later. ");
                                    badgesLV.setVisibility(View.GONE);
                                }
                            });
                        }
                    });
        }
        else{
            messageTV.setVisibility(View.VISIBLE);
            messageTV.setText(context.getResources().getText(R.string.no_internet_connection));
            badgesLV.setVisibility(View.GONE);
        }

        if((dialog != null && dialog.isShowing()) || (dialog != null && dialogShownOnce) ){
            dialog.dismiss();
            dialogShownOnce = false;
        }
        dialog.show();
        dialogShownOnce = true;
    }

    //this function open the phone contact
    private void addToContact(AttendeeEntity friend, MainActivity mainActivity){

        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.NAME, friend.getName());
        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, friend.getEmail())
                .putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK);
        mainActivity.startActivity(intent);

    }
}
