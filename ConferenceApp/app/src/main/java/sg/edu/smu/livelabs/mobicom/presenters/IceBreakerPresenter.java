package sg.edu.smu.livelabs.mobicom.presenters;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import flow.Flow;
import me.itangqi.waveloadingview.WaveLoadingView;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterIceBreakerEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateNetworkEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.GameListEntity;
import sg.edu.smu.livelabs.mobicom.models.data.IceBreakerFriendsEntity;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.item.FriendDetailFromQRItem;
import sg.edu.smu.livelabs.mobicom.net.response.IceBreakerAddFriendResponse;
import sg.edu.smu.livelabs.mobicom.net.response.IceBreakerFriendDetailResponse;
import sg.edu.smu.livelabs.mobicom.net.response.IceBreakerGetFriendListResponse;
import sg.edu.smu.livelabs.mobicom.presenters.screen.IceBreakerLeaderBoardScreen;
import sg.edu.smu.livelabs.mobicom.qrScanner.QRScannerService;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.IceBreakerService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.util.Util;
import sg.edu.smu.livelabs.mobicom.views.IceBreakerView;

/**
 * Created by smu on 22/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(IceBreakerPresenter.class)
@Layout(R.layout.icebreaker_view)
public class IceBreakerPresenter extends ViewPresenter<IceBreakerView>{

        private final RestClient restClient;
        private ActionBarOwner actionBarOwner;
        private Bus bus;
        private MainActivity mainActivity;
        public static final String NAME = "IceBreakerPresenter";

        private int screenWidth;
        private int screenHeight;
        private  ConnectivityManager cm;
        private User me;
        private List<IceBreakerFriendsEntity> friends;

        private static int countOnMakingQR = 0;
        private boolean isToLeaderBoard = false;

        public IceBreakerPresenter(RestClient restClient, Bus bus, ActionBarOwner actionBarOwner, MainActivity mainActivity) {
                this.restClient = restClient;
                this.bus = bus;
                this.mainActivity =mainActivity;
                this.actionBarOwner = actionBarOwner;
        }

        @Override
        protected void onLoad(Bundle savedInstanceState) {
                super.onLoad(savedInstanceState);
                Log.d(App.APP_TAG, " IceBreakerPresenter onload");

                GameListEntity game = GameService.getInstance().getGameByKeyword("ice breaker");
                if(game != null &&  !game.getGameName().isEmpty()){
                        actionBarOwner.setConfig(new ActionBarOwner.Config(true, game.getGameName(), null));
                }
                else{
                        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "The Talent Firm", null));
                }

                cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                mainActivity.setVisibleBottombar(View.GONE);

                me = DatabaseService.getInstance().getMe();

//                //Mimimum acceptable free memory you think your app needs
//                long minRunningMemory = 20000000; //20MB
//
//                Runtime runtime = Runtime.getRuntime();
//                if(runtime.freeMemory()<minRunningMemory)
//                        System.gc();

//                GameListEntity icebreaker = GameService.getInstance().getGame(1);
                if(game != null) {
                        if (game.getDescription() != null && !game.getDescription().isEmpty())
                                getView().descriptionTV.setSingleLine(false);
                                getView().descriptionTV.setText(game.getDescription());
                }

                WindowManager wm = (WindowManager) getView().getContext().getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                screenWidth = size.x;
                screenHeight = size.y;

                if(cm.getActiveNetworkInfo() != null) {
                        loadFriendList();
                }

                //to set the number of friends as text (numeric count)
                friends = IceBreakerService.getInstance().getAllFriends();
                if(friends != null){
                        getView().currentFriendTV.setText("" + friends.size());
                }


                SharedPreferences preferences = getView().getContext().getSharedPreferences("MOBICOM_ICE_BREAKER_TOTAL_USER", Context.MODE_PRIVATE);
                int totalUsers = preferences.getInt("total_users", 0); //the number of users in mobisys

                RelativeLayout.LayoutParams layout =  new RelativeLayout.LayoutParams((int) (screenWidth * 0.55), (int) (screenWidth * 0.55));
                layout.addRule(RelativeLayout.CENTER_HORIZONTAL);
                         getView().waveLoadingView.setLayoutParams(layout);
                layout.setMargins(0, 50, 0, 0);
                getView().waveLoadingView.setShapeType(WaveLoadingView.ShapeType.SQUARE);

                //this set the progress in precentage of the ice breaker
                if(totalUsers != 0 && friends != null) {
                        getView().waveLoadingView.setProgressValue( (int)(((double) friends.size() / totalUsers) * 100d));
                }
                else{
                        getView().waveLoadingView.setProgressValue(0);
                }
                getView().waveLoadingView.setBorderWidth(10);
                getView().waveLoadingView.setAmplitudeRatio(20);
                getView().waveLoadingView.setWaveColor(getView().getContext().getResources().getColor(R.color.transparent_blue));
                getView().waveLoadingView.setBorderColor(getView().getContext().getResources().getColor(R.color.transparent_blue));

                generateQrCode(me.getQrCode());

                getView().scanButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                if (cm.getActiveNetworkInfo() != null) {
                                        QRScannerService.getInstance().requestScan(mainActivity, new Action1<String>() {
                                                @Override
                                                public void call(String s) {
                                                        getCheckQR(s);
//                                                        Toast.makeText(getView().getContext(), s, Toast.LENGTH_SHORT).show();

                                                }
                                        });
                                }
                        }
                });

                getView().currentFriendContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                isToLeaderBoard = true;
                                Flow.get(getView().getContext()).set(new IceBreakerLeaderBoardScreen());
                        }
                });

                getView().leaderboardIV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                isToLeaderBoard = true;
                                Flow.get(getView().getContext()).set(new IceBreakerLeaderBoardScreen());
                        }
                });

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        (int) (screenHeight * 0.35));
                layoutParams.gravity = Gravity.TOP;
                getView().containerTV.setLayoutParams(layoutParams);

//                LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams((int) (screenHeight * 0.1),
//                        (int) (screenHeight * 0.1));
//                layoutParams2.setMargins(0, 5,0,5);
//                getView().scanButton.setLayoutParams(layoutParams2);


                App.getInstance().startNetworkMonitoringReceiver();
        }

        private Handler handler;
        public  void generateQrCode(final String myCodeText)  {
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());
                Thread thread = new Thread() {
                        @Override
                        public void run() {
                                QRCodeWriter writer = new QRCodeWriter();
                                try {
                                        BitMatrix bitMatrix = writer.encode(myCodeText, BarcodeFormat.QR_CODE, (int) (screenWidth * 0.6), (int) (screenWidth * 0.6));
                                        int width = bitMatrix.getWidth();
                                        int height = bitMatrix.getHeight();
                                        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                                        for (int x = 0; x < width; x++) {
                                                for (int y = 0; y < height; y++) {
                                                        bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                                                }
                                        }

                                        final Bitmap bmpTmp = bmp;

                                        mainHandler.post(new Runnable() { //TODO need to check whether will there be a lag that this will execute first
                                                @Override
                                                public void run() {
                                                        if(getView() != null) {
                                                                getView().qrCodeIV.setImageBitmap(makeTransparent(bmpTmp, Color.parseColor("#ffffff")));

                                                                getView().qrCodeIV.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                                                        @Override
                                                                        public void onGlobalLayout() {
                                                                                if (getView() != null) {
                                                                                        int spacing = (int) (Math.abs(getView().waveLoadingView.getHeight() - getView().qrCodeIV.getHeight()) / 2);
                                                                                        RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                                                                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                                                                                        layout.setMargins(0, spacing, 0, 0);
                                                                                        layout.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                                                                                        layout.addRule(RelativeLayout.CENTER_HORIZONTAL);
                                                                                        getView().qrCodeIV.setLayoutParams(layout);

                                                                                        getView().spinnerLL.setVisibility(View.GONE);
                                                                                }
                                                                        }
                                                                });
                                                        }
                                                }
                                        });


                                } catch (WriterException e) {
                                        e.printStackTrace();
                                }
                        }
                };

                thread.start();

//                handler = new Handler();
//
//                final Runnable r = new Runnable() {
//                        public void run() {
//
//                        }
//                };


        }

        // Convert transparentColor to be transparent in a Bitmap.
        public static Bitmap makeTransparent(Bitmap bit, int transparentColor) {
                int width = bit.getWidth();
                int height = bit.getHeight();
                try {
                        Bitmap myBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        int[] allpixels = new int[myBitmap.getHeight() * myBitmap.getWidth()];
                        bit.getPixels(allpixels, 0, myBitmap.getWidth(), 0, 0, myBitmap.getWidth(), myBitmap.getHeight());
                        myBitmap.setPixels(allpixels, 0, width, 0, 0, width, height);

                        for (int i = 0; i < myBitmap.getHeight() * myBitmap.getWidth(); i++) {
                                if (allpixels[i] == transparentColor)

                                        allpixels[i] = Color.alpha(Color.TRANSPARENT);
                        }

                        myBitmap.setPixels(allpixels, 0, myBitmap.getWidth(), 0, 0, myBitmap.getWidth(), myBitmap.getHeight());
                        countOnMakingQR = 0;
                        return myBitmap;
                }
                catch(Throwable e){
                        countOnMakingQR++;
                        if(countOnMakingQR < 3) { //retry 3 times
                                return makeTransparent(bit, transparentColor);
                        }
                        else{
                                return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        }
                }
        }

        @Override
        protected void onEnterScope(MortarScope scope) {
                super.onEnterScope(scope);
                bus.register(this);
                if(App.getInstance().currentPresenter.equals("HomePresenter")) {
                        App.getInstance().setPrevious();
                }
                App.getInstance().currentPresenter = NAME;
                mainActivity.setVisibleBottombar(View.GONE);
        }

        @Override
        protected void onExitScope() {
                super.onExitScope();
                bus.unregister(this);
                if(!GameService.getInstance().fromGames(App.getInstance().previousPresenter) && !isToLeaderBoard){
                        mainActivity.setVisibleBottombar(View.VISIBLE);
                }
                if (NAME.equals(App.getInstance().currentPresenter)){
                        App.getInstance().currentPresenter = "";
                }
        }

        @Subscribe
        public void unregisterInternetReceiver(UnregisterIceBreakerEvent event){
                if(event.toUnregister) {
                        App.getInstance().stopNetworkMonitoringReceiver();
                }
                else{
                        App.getInstance().startNetworkMonitoringReceiver();
                }
        }

        @Subscribe
        public void updateNetwork(UpdateNetworkEvent event){
                if(event.isConnected){
                        getView().scanButton.setBackground(getView().getResources().getDrawable(R.drawable.custom_button_blue));
                        getView().scanButton.setEnabled(true);
                        getView().scanButton.setText("Scan QR Code");
                        loadFriendList();
//                        CoolfieCacheService.getInstance().uploadCachedPhoto(mainActivity);
                }
                else{
                        getView().scanButton.setBackground(getView().getResources().getDrawable(R.drawable.custom_button_grey));
                        getView().scanButton.setEnabled(false);
//                        getView().scanButton.setText("Opps, there is no internet connection.");
                }
        }

        private void getCheckQR(String qrCode){
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());

                User user = DatabaseService.getInstance().getMe();
                restClient.getIceBreakerApi().getUserFromQR(qrCode, Long.toString(user.getUID()))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<IceBreakerFriendDetailResponse>() {
                                @Override
                                public void call(final IceBreakerFriendDetailResponse response) {
                                        if (response.status.equals("success")) {

                                                if (response.details != null && response.details.size() > 0) {

                                                        mainHandler.post(new Runnable() { //TODO need to check whether will there be a lag that this will execute first
                                                                @Override
                                                                public void run() {
                                                                        FriendDetailFromQRItem user = response.details.get(0);
                                                                        if(Long.parseLong(user.userId) == me.getUID()){
                                                                                showDialog(null, false, "You cannot add yourself.");
                                                                        }
                                                                        else {
                                                                                if (!IceBreakerService.getInstance().isFriend(user)) {
                                                                                        showDialog(user, true, "");
                                                                                } else {
                                                                                        showDialog(null, false, user.name + " have already been added.");
                                                                                }
                                                                        }

                                                                }
                                                        });
                                                }
                                                else{
                                                        mainHandler.post(new Runnable() { //TODO need to check whether will there be a lag that this will execute first
                                                                @Override
                                                                public void run() {
                                                                        showDialog(null, false, "This is not a valid QR code.");
                                                                }
                                                        });
                                                }
                                        }
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
//                                        getView().msgLL.setVisibility(View.VISIBLE);
                                        Log.e("Mobisys: ", "cannot get qr details", throwable);
                                }
                        });
        }

        //once a qr code is scanned and it's valid, this will be call to add the friend to the back end
        private void addFriend(FriendDetailFromQRItem friend){
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());

                restClient.getIceBreakerApi().addFriend(Long.toString(me.getUID()), friend.userId)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<IceBreakerAddFriendResponse>() {
                                @Override
                                public void call(final IceBreakerAddFriendResponse response) {
                                        if (response.status.equals("success")) {
                                                if (response.details != null) { //reupdate the database to ensure all are sync
                                                        IceBreakerService.getInstance().updateFriendList(response.details);

                                                        //we need to update the view (wave and counter of friends) after we successfully added a new friend
                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                        if (getView() != null) {
                                                                                SharedPreferences preferences = getView().getContext()
                                                                                        .getSharedPreferences("MOBICOM_ICE_BREAKER_TOTAL_USER", Context.MODE_PRIVATE);
                                                                                int totalUser = preferences.getInt("total_users", 0);
                                                                                //to set the number of friends as text (numeric count)
                                                                                friends = IceBreakerService.getInstance().getAllFriends();
                                                                                if (friends != null) {
                                                                                        getView().currentFriendTV.setText("" + friends.size());
                                                                                }

                                                                                if (totalUser != 0 && friends != null) {
                                                                                        getView().waveLoadingView.setProgressValue(
                                                                                                (int) (((double) friends.size() / totalUser) * 100d));
                                                                                } else {
                                                                                        getView().waveLoadingView.setProgressValue(0);
                                                                                }
                                                                        }
                                                                }
                                                        });
                                                }
                                        }
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot add friend", throwable);
                                }
                        });
        }

        //this helps to show detail of the friend scanned from the QR code
        public void showDialog(final FriendDetailFromQRItem friend, boolean isValid, String msg){

                if(getView() == null) return; //view lost
                final Dialog dialog = new Dialog(getView().getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_box_icebreaker);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.setCancelable(false);

                Button closeBtn = (Button) dialog.findViewById(R.id.closeBtn);
                TextView messageTV = (TextView) dialog.findViewById(R.id.message);
                final ImageView avatarIV = (ImageView) dialog.findViewById(R.id.avatar_image);
                final TextView nameShortFormTV = (TextView) dialog.findViewById(R.id.name_short_form);

                if(isValid) {
                        String interest = friend.interests;
                        String intro = "Hi, I am " + friend.name;
                        if(friend != null && friend.organisation != null && !friend.organisation.isEmpty()){
                                intro += " from " + friend.organisation;
                        }

                        if(friend != null && friend.interests != null && !friend.interests.isEmpty()){
                                intro +=  "\n My interest(s) are " +interest;
                        }

                        messageTV.setText(intro);

                        if (friend != null && friend.avatar != null && !friend.avatar.equals("")) {
                                Picasso.with(getView().getContext()).load(Util.getPhotoUrlFromId(friend.avatar, 256))
                                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                                        .networkPolicy(NetworkPolicy.NO_CACHE)
                                        .into(avatarIV, new com.squareup.picasso.Callback() {
                                                @Override
                                                public void onSuccess() {
                                                }

                                                @Override
                                                public void onError() { //when failed to load, draw a rect on top of the noavatar and add the name short form
                                                        GameService.getInstance().drawNameShort(friend.name, avatarIV, nameShortFormTV);
                                                }
                                        });
                        }
                        else{
                                GameService.getInstance().drawNameShort(friend.name, avatarIV, nameShortFormTV);
                        }

                        //only increment the ice breaker user count when it is a valid friend and user haven't friend he/she yet
//                        MasterPointService.getInstance().addPoint(MasterPointService.getInstance().ICE_BREAKER, getView().container);

                        addFriend(friend);
                }
                else{
                        avatarIV.setVisibility(View.INVISIBLE);
                        messageTV.setText(msg);
                }

                closeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                dialog.dismiss();
                        }
                });

                dialog.show();
        }

        //this function call the server to get the list of friends that user had made from ice breaker
        private void loadFriendList(){
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());

                restClient.getIceBreakerApi().getFriendList(Long.toString(me.getUID()))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<IceBreakerGetFriendListResponse>() {
                                @Override
                                public void call(final IceBreakerGetFriendListResponse iceBreakerGetFriendListResponse) {
                                        if (iceBreakerGetFriendListResponse.status.equals("success")) {
                                                if (iceBreakerGetFriendListResponse.friends != null) {
                                                        IceBreakerService.getInstance().updateFriendList(iceBreakerGetFriendListResponse.friends);

                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                        if (getView() != null) {
                                                                                SharedPreferences preferences = getView().getContext()
                                                                                        .getSharedPreferences("MOBICOM_ICE_BREAKER_TOTAL_USER", Context.MODE_PRIVATE);
                                                                                preferences
                                                                                        .edit()
                                                                                        .clear()
                                                                                        .putInt("total_users", iceBreakerGetFriendListResponse.total_users)
                                                                                        .commit();


                                                                                //to set the number of friends as text (numeric count)
                                                                                friends = IceBreakerService.getInstance().getAllFriends();
                                                                                if (friends != null) {
                                                                                        getView().currentFriendTV.setText("" + friends.size());
                                                                                }

                                                                                if (iceBreakerGetFriendListResponse.total_users != 0 && friends != null) {
                                                                                        getView().waveLoadingView.setProgressValue(
                                                                                                (int) (((double) friends.size() / iceBreakerGetFriendListResponse.total_users) * 100d));
                                                                                } else {
                                                                                        getView().waveLoadingView.setProgressValue(0);
                                                                                }
                                                                        }
                                                                }
                                                        });
                                                }

                                        }
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot get friends", throwable);
                                }
                        });
        }

        @Subscribe
        public void badgeNotiEvent(BadgeNotiEvent event){
                MasterPointService.getInstance().getBadgesAPI();
                MasterPointService.getInstance().showToolTips(getView().container, event.badgeName);
        }
}
