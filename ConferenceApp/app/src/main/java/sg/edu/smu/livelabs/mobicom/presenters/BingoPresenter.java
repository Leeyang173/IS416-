package sg.edu.smu.livelabs.mobicom.presenters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.isseiaoki.simplecropview.CropImageView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
import flow.Flow;
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
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.BingoGridAdapter;
import sg.edu.smu.livelabs.mobicom.adapters.FavoriteItemGridAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UnregisterVotingEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateNetworkEvent;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.GameListEntity;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.item.BingoItem;
import sg.edu.smu.livelabs.mobicom.net.item.FavoriteItem;
import sg.edu.smu.livelabs.mobicom.net.item.Selfie;
import sg.edu.smu.livelabs.mobicom.net.response.BingoResponse;
import sg.edu.smu.livelabs.mobicom.net.response.FavoriteDetailsResponse;
import sg.edu.smu.livelabs.mobicom.net.response.FormResponse;
import sg.edu.smu.livelabs.mobicom.net.response.ProfileResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SelfieCameraScreen;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.util.Util;
import sg.edu.smu.livelabs.mobicom.views.BingoView;
import sg.edu.smu.livelabs.mobicom.views.MyCropImageView;
import sg.edu.smu.livelabs.mobicom.views.QuizView;

/**
 * Created by smu on 22/2/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(BingoPresenter.class)
@Layout(R.layout.bingo_view)
public class BingoPresenter extends ViewPresenter<BingoView> implements  MyCropImageView.ReloadingImageListener {

        private ActionBarOwner actionBarOwner;
        private final RestClient restClient;
        private Bus bus;
        public static String NAME = "BingoPresenter";

        private User me;
        private  ConnectivityManager cm;
        private MainActivity mainActivity;
        private boolean isLoaded; //to determine whether have the quiz been loaded
        private boolean isFirstTime = true;
        private int screenWidth;

        private List<BingoItem> bingoItems;
        private BingoGridAdapter adapter;
        private int selectedPosition = 0;

        /**
         *
         * @param restClient
         * @param bus
         * @param actionBarOwner
         */
        public BingoPresenter(RestClient restClient, Bus bus, ActionBarOwner actionBarOwner, MainActivity mainActivity) {
                this.restClient = restClient;
                this.bus = bus;
                this.actionBarOwner = actionBarOwner;
                this.mainActivity = mainActivity;
                this.isLoaded = false;
        }

        @Override
        protected void onLoad(Bundle savedInstanceState) {
                super.onLoad(savedInstanceState);
                Log.d(App.APP_TAG, "BingoPresenter onload");

                me = DatabaseService.getInstance().getMe();
                bingoItems = new ArrayList<>();

                WindowManager wm = (WindowManager) getView().getContext().getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                screenWidth = size.x;

                cm = (ConnectivityManager) getView().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                GameListEntity game = GameService.getInstance().getGameByKeyword("photo_bingo");
                if(game != null &&  !game.getGameName().isEmpty()){
                        actionBarOwner.setConfig(new ActionBarOwner.Config(true, game.getGameName(), null));
                }
                else{
                        actionBarOwner.setConfig(new ActionBarOwner.Config(true, "Photo Bingo", null));
                }

                if(game != null) {
                        if (game.getDescription() != null && !game.getDescription().isEmpty())
                                getView().description.setText(game.getDescription());
                }
 
                getView().bingoGV.setColumnWidth(screenWidth/5);
                getView().myCropImageView.setListener(this, mainActivity, CropImageView.CropMode.SQUARE);
                if(cm.getActiveNetworkInfo() != null){
                        loadBingo();
                        isFirstTime = false;
                }
                else{

                        getView().messageTV.setVisibility(View.VISIBLE);
                        getView().messageTV.setText("Bingo cannot be loaded without Internet connection");
                }

                App.getInstance().startNetworkMonitoringReceiver();
        }

        @Override
        protected void onEnterScope(MortarScope scope) {
                super.onEnterScope(scope);
                bus.register(this);
                App.getInstance().setPrevious();
                App.getInstance().currentPresenter = NAME;
                mainActivity.setVisibleBottombar(View.GONE);
        }

        @Override
        protected void onExitScope() {
                super.onExitScope();
                bus.unregister(this);
                if(!GameService.getInstance().fromGames(App.getInstance().previousPresenter)){
                        mainActivity.setVisibleBottombar(View.VISIBLE);
                }
                if (NAME.equals(App.getInstance().currentPresenter)){
                        App.getInstance().currentPresenter = "";
                }
        }

        @Subscribe
        public void unregisterVotingReceiver(UnregisterVotingEvent event){
                if(event.toUnregister) {
                        App.getInstance().stopNetworkMonitoringReceiver();
                }
                else{
                        App.getInstance().startNetworkMonitoringReceiver();
                }
        }


        @Subscribe
        public void updateNetwork(UpdateNetworkEvent event) {
                if (event.isConnected && !isLoaded && isFirstTime) {

                        isFirstTime = false;
                }
        }

        private void loadBingo(){
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());

                restClient.getBingoApi().getBingo(Long.toString(me.getUID()))
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<BingoResponse>() {
                                @Override
                                public void call(final BingoResponse response) {
                                        if (response.status.equals("success")) {
                                                if(response.bingoItems != null && response.bingoItems.size() > 0 && hasView()){
                                                        bingoItems = response.bingoItems;
                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                        adapter = new BingoGridAdapter(getView().getContext(), response.bingoItems, screenWidth,
                                                                                new onClickListener(){
                                                                                        @Override
                                                                                        public void onBtnClick(final int position) {
                                                                                               if(bingoItems.get(position).imageId == null){
                                                                                                       selectedPosition = position;
                                                                                                       getView().myCropImageView.uploadImage();
                                                                                               }
                                                                                                else{
                                                                                                       //show dialog
                                                                                                       showDialog(position);
                                                                                               }
                                                                                        }
                                                                                });

                                                                        getView().bingoGV.setAdapter(adapter);
                                                                }
                                                        });

                                                }
                                        }
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot get bingo list", throwable);
                                }
                        });

        }

        public void updateBingo(String id, String imageId){
                final Handler mainHandler = new Handler(getView().getContext().getMainLooper());

                restClient.getBingoApi().updateBingo(Long.toString(me.getUID()), imageId, id)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Action1<BingoResponse>() {
                                @Override
                                public void call(final BingoResponse response) {
                                        if (response.status.equals("success")) {
                                                if(response.bingoItems != null && response.bingoItems.size() > 0 && hasView()){
                                                        bingoItems = response.bingoItems;
                                                        mainHandler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                        adapter.updates(bingoItems);
                                                                        getView().bingoGV.setAdapter(adapter);
                                                                }
                                                        });

                                                }
                                        }
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                        Log.e("Mobisys: ", "cannot update bingo list", throwable);
                                }
                        });
        }

        public void showDialog(final int position){
                if(!hasView()) return;

                final Dialog dialog = new Dialog(getView().getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_box_bingo);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.setCancelable(true);

                Button reupload = (Button) dialog.findViewById(R.id.reuploadBtn);
                TextView textTV = (TextView) dialog.findViewById(R.id.text);
                final ImageView image = (ImageView) dialog.findViewById(R.id.image);

                textTV.setText(bingoItems.get(position).text);
                try {
                        Picasso.with(getView().getContext()).load(Util.getPhotoUrlFromId(bingoItems.get(position).imageId, 512))
                                .resize((int) (screenWidth * 0.9), (int) (screenWidth * 0.9))
                                .centerCrop()
                                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                .networkPolicy(NetworkPolicy.NO_CACHE).placeholder(R.drawable.placeholder).into(image);
                }
                catch (OutOfMemoryError error){}

                reupload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                selectedPosition = position;
                                getView().myCropImageView.uploadImage();
                                dialog.dismiss();
                        }
                });

                dialog.show();
        }

        @Subscribe
        public void badgeNotiEvent(BadgeNotiEvent event){
                MasterPointService.getInstance().getBadgesAPI();
                MasterPointService.getInstance().showToolTips(getView().mainContainer, event.badgeName);
        }

        public interface onClickListener {
                public abstract void onBtnClick(int position);
        }

        @Override
        public void setNewImage(String avatarId) {
                getView().mainContainer.setVisibility(View.VISIBLE);
                updateBingo(bingoItems.get(selectedPosition).id, avatarId);
        }

        @Override
        public void hideMainLayout() {
                getView().mainContainer.setVisibility(View.GONE);
        }
}
