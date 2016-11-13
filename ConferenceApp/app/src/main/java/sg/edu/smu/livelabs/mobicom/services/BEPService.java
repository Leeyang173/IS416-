package sg.edu.smu.livelabs.mobicom.services;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.net.api.BEPApi;
import sg.edu.smu.livelabs.mobicom.net.api.PublicIpApi;
import sg.edu.smu.livelabs.mobicom.net.response.MacIpResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;

/**
 * Created by smu on 28/2/16.
 */
public class BEPService extends GeneralService {
    private static final BEPService instance = new BEPService();
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    public static BEPService getInstance() {
        return instance;
    }

    private BEPApi bepApi;
    private PublicIpApi publicIpApi;
    private User me;

    public void init(Context context, BEPApi bepApi) {
        this.context = context;
        this.bepApi = bepApi;
        me = DatabaseService.getInstance().getMe();
        try{
            Retrofit retrofitPublicIp = new Retrofit.Builder()
                    .baseUrl("https://ifcfg.me/ip/")
                    .build();
            publicIpApi = retrofitPublicIp.create(PublicIpApi.class);
        } catch (Exception e){
            Log.d(App.APP_TAG, "Cannot create publicIpApis" + e.toString());
        }

    }

    /**
     * @param type
     */
    public void trackReceivedNotification(String id, String type) {
        String now = df.format(new Date());

        bepApi.recordReceiptDetection(Long.toString(me.getUID()), id, now, type)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<SimpleResponse>() {
                    @Override
                    public void call(SimpleResponse response) {


                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(App.APP_TAG, "cannot send to record_receipt_detection", throwable);
                    }
                });
    }

    /**
     * @param type
     */
    public void trackClickNotification(String id, long type) {
        String now = df.format(new Date());

        bepApi.recorcClickDetection(Long.toString(me.getUID()), id, now, Long.toString(type))
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<SimpleResponse>() {
                    @Override
                    public void call(SimpleResponse response) {


                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(App.APP_TAG, "cannot send to record_click_detection", throwable);
                    }
                });
    }

    public void updateUserMac(final Context context, String wifiName){

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if(publicIpApi == null) return null;
                publicIpApi.getPublicIP()
                        .enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
                                try {
                                    if (response.isSuccess()) {
                                        final String publicIP = new String((response.body()).bytes());
                                        Log.d(App.APP_TAG, "getpulic IP :success " + publicIP);
                                        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                                        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                                        int ipAddress = wifiInfo.getIpAddress();
                                        final String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
                                        Log.d("localIP", ip);
//                                        UIHelper.getInstance().showConfirmAlert(context, "Test", String.format("PublicIP: %s, localIP: %s. Click 'YES' to get mac address.", publicIP, ip), "YES", "NO",
//                                                new Action0() {
//                                                    @Override
//                                                    public void call() {
//                                                        bepApi.getMacFromIP(ip, publicIP)
//                                                                .observeOn(AndroidSchedulers.mainThread())
//                                                                .subscribeOn(Schedulers.io())
//                                                                .subscribe(new Action1<MacIpResponse>() {
//                                                                    @Override
//                                                                    public void call(MacIpResponse macIpResponse) {
//                                                                        if (!"fail".equals(macIpResponse.status)) {
//                                                                            long userID = DatabaseService.getInstance().getMe().getUID();
//                                                                            ChatService.getInstance().updateUserMac(userID, macIpResponse.mac, "" + Build.VERSION.RELEASE);
//                                                                            UIHelper.getInstance().showAlert(context, String.format("PublicIP: %s, localIP: %s, Mac: %s", publicIP, ip, macIpResponse.mac));
//                                                                        }
//                                                                    }
//                                                                }, new Action1<Throwable>() {
//                                                                    @Override
//                                                                    public void call(Throwable throwable) {
//                                                                        Log.d(App.APP_TAG, "Cannot get mac from ip", throwable);
//                                                                    }
//                                                                });
//                                                    }
//                                                }, new Action0() {
//                                                    @Override
//                                                    public void call() {
//
//                                                    }
//                                                });
                                        bepApi.getMacFromIP(ip, publicIP)
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribeOn(Schedulers.io())
                                                .subscribe(new Action1<MacIpResponse>() {
                                                    @Override
                                                    public void call(MacIpResponse macIpResponse) {
                                                        if (!"fail".equals(macIpResponse.status)) {
                                                            long userID = DatabaseService.getInstance().getMe().getUID();
                                                            ChatService.getInstance().updateUserMac(userID, macIpResponse.mac, "" + Build.VERSION.RELEASE);
                                                        }
                                                    }
                                                }, new Action1<Throwable>() {
                                                    @Override
                                                    public void call(Throwable throwable) {
                                                        Log.d(App.APP_TAG, "Cannot get mac from ip", throwable);
                                                    }
                                                });
                                    } else {
                                        Log.d(App.APP_TAG, "getpulic IP :fail status");
                                    }

                                } catch (IOException e) {
                                    Log.d(App.APP_TAG, e.toString());
                                }
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                Log.d(App.APP_TAG, "getpulic IP: fail response");
                            }
                        });
                return null;
            }
        }.execute();


    }

}
