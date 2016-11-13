package sg.edu.smu.livelabs.mobicom;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.ArmaRssiFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sg.edu.smu.livelabs.mobicom.models.BeaconOccurrences;
import sg.edu.smu.livelabs.mobicom.presenters.BeaconPresenter;
import sg.edu.smu.livelabs.mobicom.services.BeaconsService;

/**
 * Created by smu on 18/1/16.
 */
public class BeaconServices extends Service implements BeaconConsumer{
    private static final String TAG = "RegIntentService";

    private BeaconManager beaconManager;
    private List<Beacon> frameOfNearestBeacon = new ArrayList<>();
    private List<Integer> numberOfInsertedBeaconList = new ArrayList<>();
    private List<BeaconOccurrences> topThreeBeacons = new ArrayList<>();
    private Beacon currentBeacon;
    private Beacon previousBeacon;
    boolean foundNearestBeacon = false;
    private int counter;

    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    // Registered callbacks
    private BeaconPresenter.ServiceCallbacks serviceCallbacks;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // Class used for the client Binder.
    public class LocalBinder extends Binder {
        public BeaconServices getService() {
            // Return this instance of MyService so clients can call public methods
            return BeaconServices.this;
        }
    }

    public void setCallbacks(BeaconPresenter.ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        counter = 0;
        beaconManager = BeaconManager.getInstanceForApplication(this);
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        beaconManager.bind(this);

    }

    @Override
    public void onBeaconServiceConnect() {

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

                if (beacons.size() > 0) {
                    int index = 0;
                    List<Integer> indexList = new ArrayList<Integer>();
                    final List<Beacon> bList = new ArrayList<Beacon>();
                    bList.addAll(beacons);
                    Collections.sort(bList, new Comparator<Beacon>() {
                        public int compare(Beacon b1, Beacon b2) {
                            return Integer.valueOf(b2.getRssi()).compareTo(b1.getRssi());
                        }
                    });

                    //clean up the list by comparing to the list from database so that we can get the list of valid beacon
                    //if after clean up, bList.size == 0, wait for next rescan
                    for (Beacon b : bList) {
                        if (BeaconsService.getInstance().checkBeaconExist(b)) {
                            indexList.add(index);
                        }

                        index++;

                        if (indexList.size() > 3)//max limit to add 3 beacons the most
                            break;
                    }

                    if (bList.size() == 0) {
                        return;
                    }

//                    //do a check of nearest beacon for a period of 20 windows frame to confirm the current beacon bList.get(0) is the nearest beacon
                    if (numberOfInsertedBeaconList.size() >= 10) {
                        List<Beacon> beaconsTmpList = new ArrayList<Beacon>();
                        for (Beacon bTmp : frameOfNearestBeacon) {
                            if (!beaconsTmpList.contains(bTmp)) {
                                beaconsTmpList.add(bTmp);
                            }
                        }

                        //count which beacon have the higest obersvation
                        List<BeaconOccurrences> beaconOccurrences = new ArrayList<BeaconOccurrences>();
                        for (Beacon beaconsTmp : beaconsTmpList) {
                            int count = 0;
                            for (Beacon bTmp : frameOfNearestBeacon) {
                                if (bTmp.getId2().toString().equals(beaconsTmp.getId2().toString())
                                        && bTmp.getId3().toString().equals(beaconsTmp.getId3().toString()))
                                    count++;
                            }
                            beaconOccurrences.add(new BeaconOccurrences(beaconsTmp, count));
                        }


                        Collections.sort(beaconOccurrences, new Comparator<BeaconOccurrences>() {
                            public int compare(BeaconOccurrences b1, BeaconOccurrences b2) {
                                return Integer.valueOf(b2.getCount()).compareTo(b1.getCount());
                            }
                        });


                        if (beaconOccurrences.size() > 0) {
                            for (int k = 0; k < beaconOccurrences.size(); k++) {
                                topThreeBeacons.add(beaconOccurrences.get(k));
                                if (topThreeBeacons.size() > 3)
                                    break;
                            }

                            Collections.sort(topThreeBeacons, new Comparator<BeaconOccurrences>() {
                                public int compare(BeaconOccurrences b1, BeaconOccurrences b2) {
                                    return Integer.valueOf(b1.getCount()).compareTo(b2.getCount());
                                }
                            });

//                            currentBeacon = beaconOccurrences.get(0).getBeacon();
                            foundNearestBeacon = true;
                        }

                        //to remove the first 10 windows
                        for (int k = 0; k < 5; k++) {
                            if (numberOfInsertedBeaconList.size() > 0) {
                                for (int i = 0; i < numberOfInsertedBeaconList.get(0); i++) {
                                    frameOfNearestBeacon.remove(0);
                                }
                                numberOfInsertedBeaconList.remove(0);
                            }
                        }


                    } else { //less than 30
                        for (int indexTmp : indexList) {
                            frameOfNearestBeacon.add(bList.get(indexTmp));
                        }

                        numberOfInsertedBeaconList.add(indexList.size());

//                        System.out.println(">>>>>> size: " + frameOfNearestBeacon.size() + " counter: " + numberOfInsertedBeaconList.size());
                    }


                    //to display the beacon(s) after we have accumulate enough info to display the top 3 nearest beacons
                    if ((previousBeacon == null && topThreeBeacons != null && topThreeBeacons.size() > 0 && foundNearestBeacon)
                            ||
                            (foundNearestBeacon && !previousBeacon.getId2().toString().equals(currentBeacon.getId2().toString()) &&
                                    !previousBeacon.getId3().toString().equals(currentBeacon.getId3().toString()))) {
                        previousBeacon = currentBeacon;
                        foundNearestBeacon = false;
                        Handler mHandler = new Handler(getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
//                                serviceCallbacks.loadWebSite(currentBeacon.getId2().toString());
                                if (serviceCallbacks != null) {
                                    serviceCallbacks.loadWebSite(topThreeBeacons);
                                }

                                topThreeBeacons.clear();
                            }
                        });
                    }
                }
            }
        });

        try {
            beaconManager.setRssiFilterImplClass(ArmaRssiFilter.class);
            beaconManager.setBackgroundScanPeriod(400l); //400 default
            beaconManager.setForegroundScanPeriod(400l); //400
            beaconManager.setForegroundBetweenScanPeriod(200l); //200 default
            beaconManager.setBackgroundBetweenScanPeriod(200l); //200
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", Identifier.parse("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null));
        } catch (RemoteException e) {
        }
    }

    @Override
    public void onDestroy() {
        beaconManager.unbind(this);
        super.onDestroy();
    }
}
