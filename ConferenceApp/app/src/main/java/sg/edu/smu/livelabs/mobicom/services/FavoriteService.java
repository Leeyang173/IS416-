package sg.edu.smu.livelabs.mobicom.services;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sg.edu.smu.livelabs.mobicom.busEvents.FavoriteEvent;
import sg.edu.smu.livelabs.mobicom.net.api.FavoriteApi;
import sg.edu.smu.livelabs.mobicom.net.item.FavoriteListItem;
import sg.edu.smu.livelabs.mobicom.net.response.FavoriteResponse;

/**
 * Created by smu on 27/10/15.
 */
public class FavoriteService extends GeneralService {
    public static final int TYPE_VOTING_NEXT_COMING = 1;
    public static final int TYPE_VOTING_PAST = 2;
    public static final int TYPE_VOTING_CURRENT = 3;
    public static final int TYPE_LUCKY_DRAW = 4;
    public static final int VOTING_PHOTOS_PER_PAGE = 20;

    private static final FavoriteService instance = new FavoriteService();
    private Context context;
    private Gson nullGson;
    private Gson notNullGson;
    private FavoriteApi favoriteApi;
    private SimpleDateFormat dateToStrServerFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat dateToStrFormat = new SimpleDateFormat("MMM dd");
    private SimpleDateFormat dateToStrFormat2 = new SimpleDateFormat("dd MMMM");
    private HashMap<String, FavoriteListItem> itemHashMap;
    private String userId;
    public FavoriteListItem currentPromotion;

    public static FavoriteService getInstance() {
        return instance;
    }

    public void init(Context context, Bus bus, Gson gson, FavoriteApi favoriteApi) {
        this.context = context;
        this.bus = bus;
        this.notNullGson = gson;
        this.nullGson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .serializeNulls()
                .create();
        this.favoriteApi = favoriteApi;
    }

    //get the list of item to vote
    public void getFavoriteList(){
        final Handler mainHandler = new Handler(context.getMainLooper());
        userId = String.valueOf(DatabaseService.getInstance().getMe().getUID());

        favoriteApi.getFavorites(userId)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<FavoriteResponse>() {
                    @Override
                    public void call(final FavoriteResponse response) {
                        if (response.status.equals("success")) {
                            if (response.items != null) {
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(bus != null)
                                            bus.post(new FavoriteEvent("success", response.items));
                                    }
                                });

                            }

                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("Mobisys: ", "cannot get favorite list", throwable);
                    }
                });

    }
}
