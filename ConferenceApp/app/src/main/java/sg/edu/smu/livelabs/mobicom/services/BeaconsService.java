package sg.edu.smu.livelabs.mobicom.services;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.squareup.otto.Bus;

import org.altbeacon.beacon.Beacon;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.greenrobot.dao.query.Query;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.busEvents.BeaconRatingEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.BeaconRefreshEvent;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.BeaconEntity;
import sg.edu.smu.livelabs.mobicom.models.data.BeaconEntityDao;
import sg.edu.smu.livelabs.mobicom.net.api.BeaconApi;
import sg.edu.smu.livelabs.mobicom.net.item.BeaconItem;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;

/**
 * Created by smu on 28/2/16.
 */
public class BeaconsService extends GeneralService {
    private static final BeaconsService instance = new BeaconsService();
    public static BeaconsService getInstance(){return instance;}
    private BeaconEntityDao beaconEntityDao;
    private Bus bus;
    private BeaconApi beaconApi;

    public void init(Context context, Bus bus, BeaconApi beaconApi){
        this.context = context;
        this.bus = bus;
        beaconEntityDao = DatabaseService.getInstance().getBeaconEntityDao();
        this.beaconApi = beaconApi;
    }

    public List<BeaconEntity> updateBeaconList(List<BeaconItem> beaconItems){
        List<BeaconEntity> beaconEntities = new ArrayList<BeaconEntity>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        try{
            for(BeaconItem beaconItem: beaconItems){
                BeaconEntity beaconEntity = new BeaconEntity();
                beaconEntity.setId(Long.parseLong(beaconItem.id));
                beaconEntity.setUuid(beaconItem.uuid);
                beaconEntity.setMajor(beaconItem.major);
                beaconEntity.setMinor(beaconItem.minor);
                beaconEntity.setUrl(beaconItem.url);
                beaconEntity.setPdfUrl(beaconItem.pdf);
                beaconEntity.setPaperName(beaconItem.paperName);
                beaconEntity.setCapChar(beaconItem.capChar);
                beaconEntity.setRate(beaconItem.userRating);
                beaconEntity.setAvgRating(beaconItem.avgRating);
                beaconEntity.setLastUpdated(new Date(df.parse(beaconItem.lastModifiedTime).getTime())); //add additional 1 sec to round the microsecond
                beaconEntities.add(beaconEntity);
            }

            beaconEntityDao.deleteAll();
            beaconEntityDao.insertOrReplaceInTx(beaconEntities);

        }
        catch (Exception e){
            Log.d(App.APP_TAG, e.toString());
        }
        return beaconEntities;
    }

    public List<BeaconEntity> getAllBeacons(){
        return beaconEntityDao.queryBuilder().orderAsc(BeaconEntityDao.Properties.PaperName).list();
    }

    public BeaconEntity getBeacon(){
        Query<BeaconEntity> query = beaconEntityDao.queryBuilder()
                .orderDesc(BeaconEntityDao.Properties.LastUpdated).limit(1).build();
        List<BeaconEntity> beaconEntities = query.list();
        if (beaconEntities.size() > 0) {
            return beaconEntities.get(0);
        }
        return null;
    }

    public boolean checkBeaconExist(Beacon b){
        Query<BeaconEntity> query = beaconEntityDao.queryBuilder()
                .where(BeaconEntityDao.Properties.Major.eq(b.getId2().toString()),
                        BeaconEntityDao.Properties.Minor.eq(b.getId3().toString()))
                .limit(1).build();
        List<BeaconEntity> beaconEntities = query.list();
        if (beaconEntities.size() > 0) {
            return true;
        }
        return false;
    }

    public void rateBeacon(BeaconEntity b, int rate){
        b.setRate(rate);
        beaconEntityDao.insertOrReplace(b);
        BeaconRatingEvent updatedBeacon = new BeaconRatingEvent();
        updatedBeacon.b = b;
        if(bus != null) {
            bus.post(updatedBeacon);
        }
    }

    public void updateHasOpened(BeaconEntity b, boolean hasOpened){
        b.setHasViewed(hasOpened);
        beaconEntityDao.insertOrReplace(b);
    }

    public void updateBeaconRating(final BeaconEntity b, final int rating){
        User me = DatabaseService.getInstance().getMe();
        beaconApi.updateUserBeaconRating(Long.toString(me.getUID()), Long.toString(b.getId()), Integer.toString(rating))
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<SimpleResponse>() {
                    @Override
                    public void call(SimpleResponse response) {
                        if (response.status.equals("success") && bus != null) {
                            final Handler mainHandler = new Handler(context.getMainLooper());
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    bus.post(new BeaconRefreshEvent());
                                }
                            });
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
//                                        getView().msgLL.setVisibility(View.VISIBLE);
                        Log.e("Mobisys: ", "cannot update beacon rating", throwable);
                    }
                });
    }
}
