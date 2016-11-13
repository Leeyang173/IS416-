package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import de.hdodenhof.circleimageview.CircleImageView;
import mortar.MortarScope;
import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.busEvents.RecommendedUserEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.ReloadRecommendedUserEvent;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.AttendeeEntity;
import sg.edu.smu.livelabs.mobicom.services.AttendeesService;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.views.RecommendView;
import sg.edu.smu.livelabs.mobicom.views.UserInfoPopup;

/**
 * Created by smu on 13/4/16.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MessagePresenter.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(RecommendPresenter.class)
@Layout(R.layout.recommend_view)
public class RecommendPresenter extends ViewPresenter<RecommendView> implements View.OnClickListener{
    private static final int MAX_IMAGE = 10;
    private final double[] POSITIONS = new double[]{
            60, 170,
            60, 354,
            80, 266,
            114, 400,
            146, 204,
            170, 364,
            220, 110,
            260, 196,
            272, 302,
            272, 412
        };
    private List<Point> points;
    public int width, height;
    public int centerH, centerW;
    private int imageSize;
    public Canvas canvas;
    public Paint paint;
    private Context context;
    public Handler handler;
    private Random random;
    private Bus bus;
    private List<AttendeeEntity> attendeeEntities;

    public RecommendPresenter(Bus bus){
        random = new Random();
        this.bus = bus;
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        handler = new Handler();
        context = getView().getContext();
        paint = new Paint();
        paint.setColor(Color.rgb(0, 120, 215));
        paint.setStrokeWidth(10);
        AttendeesService.getInstance().exceptedUsers = "";
        User me = DatabaseService.getInstance().getMe();
        String avatarId = me.getAvatar();
        getView().refreshBt.setOnClickListener(this);
        UploadFileService.getInstance().loadAvatar(getView().myImage, avatarId,
                getView().emptyProfileTV, me.getName());
    }

    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        bus.register(this);
    }

    @Override
    protected void onExitScope() {
        bus.unregister(this);
        super.onExitScope();
    }

    private void init() {
        width = getView().linesImg.getWidth();
        height = getView().linesImg.getHeight();
        imageSize = getView().myImage.getWidth();
        centerH = height / 2;
        centerW = width / 2;
        if (height <= 0) return;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        getView().linesImg.setImageBitmap(bitmap);
        points = new ArrayList<>();
        int numberOfPoints = POSITIONS.length;
        for (int i = 0; i < numberOfPoints; i+=2){
            int x = (int)(width * (POSITIONS[i] / 320)); //POSITION IOS 320X456
            int y = (int)(height * ((POSITIONS[i+1] - 64) / 456));
            points.add(new Point(x, y));
        }
    }

    private void setImageLocation(final CircleImageView imageView, int x , int y){
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        int left = x;
        int top = y;
        int right = width - x - imageSize;
        int bottom = height - y - imageSize;
        layoutParams.setMargins(left, top, right, bottom);
        imageView.setLayoutParams(layoutParams);
        canvas.drawLine(centerW, centerH, left + imageSize/2, top + imageSize/2, paint);
    }

    private void next(List<AttendeeEntity> attendeeEntities){
        if (attendeeEntities == null || attendeeEntities.isEmpty()) return;
        this.attendeeEntities = attendeeEntities;
        if (canvas == null){
            init();
        }
        if (canvas != null){
            canvas.drawColor(Color.WHITE);
            int n = attendeeEntities.size();
            List<Point> temps = new ArrayList<>();
            int validPoint = MAX_IMAGE;
            temps.addAll(points);
            if (n > MAX_IMAGE){
                n = MAX_IMAGE;
            }
            for (int i = 0; i < n; i++){
                int index = random.nextInt(validPoint);
                validPoint --;
                Point p = temps.get(index);
                temps.remove(index);
                AttendeeEntity attendeeEntity = attendeeEntities.get(i);
                RecommendView.RecommendUserView view = getView().friendImgs[i];
                view.setVisibility(View.VISIBLE);
                view.setData(attendeeEntity.getAvatar(), attendeeEntity.getFirstName(),
                        attendeeEntity.getName(), p);
            }
            for (int i = n; i < MAX_IMAGE; i++){
                getView().friendImgs[i].setVisibility(View.GONE);
            }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setLocation();
                }
            }, 200);
        }
    }

    private void setLocation() {
        for (int i = 0; i < MAX_IMAGE; i++){
            RecommendView.RecommendUserView view = getView().friendImgs[i];
            if (view.getVisibility() == View.VISIBLE){
                view.setLocation();
            }
        }
    }

    @Subscribe
    public void recommendedUserEvent(RecommendedUserEvent event){
        next(event.result);
        UIHelper.getInstance().dismissProgressDialog();
        getView().refreshBt.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void reloadEvent(ReloadRecommendedUserEvent event){
        UIHelper.getInstance().showProgressDialog(context, context.getString(R.string.progressing), true);
        AttendeesService.getInstance().getRecommendedUser();//TODO
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.refresh_bt:
                reloadEvent(null);
                getView().refreshBt.setVisibility(View.INVISIBLE);
                TrackingService.getInstance().sendTracking("211", "messages", "recommend", "refresh", "", "");
                break;
            case R.id.friend1_layout:
                new UserInfoPopup(context, attendeeEntities.get(0));
                break;
            case R.id.friend2_layout:
                new UserInfoPopup(context, attendeeEntities.get(1));
                break;
            case R.id.friend3_layout:
                new UserInfoPopup(context, attendeeEntities.get(2));
                break;
            case R.id.friend4_layout:
                new UserInfoPopup(context, attendeeEntities.get(3));
                break;
            case R.id.friend5_layout:
                new UserInfoPopup(context, attendeeEntities.get(4));
                break;
            case R.id.friend6_layout:
                new UserInfoPopup(context, attendeeEntities.get(5));
                break;
            case R.id.friend7_layout:
                new UserInfoPopup(context, attendeeEntities.get(6));
                break;
            case R.id.friend8_layout:
                new UserInfoPopup(context, attendeeEntities.get(7));
                break;
            case R.id.friend9_layout:
                new UserInfoPopup(context, attendeeEntities.get(8));
                break;
            case R.id.friend10_layout:
                new UserInfoPopup(context, attendeeEntities.get(9));
                break;
        }
    }
}
