package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.presenters.RecommendPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.RecommendScreenComponent;

/**
 * Created by smu on 13/4/16.
 */
@AutoInjector(RecommendPresenter.class)
public class RecommendView extends RelativeLayout {
    @Inject
    RecommendPresenter presenter;
    @Bind(R.id.refresh_bt)
    public TextView refreshBt;
    @Bind(R.id.lines_img)
    public ImageView linesImg;
    @Bind(R.id.empty_profile_tv)
    public TextView emptyProfileTV;
    @Bind(R.id.my_image)
    public CircleImageView myImage;

    @Bind(R.id.friend1_layout)
    public LinearLayout friendLayout1;
    @Bind(R.id.friend1_name)
    public TextView friendnameTV1;
    @Bind(R.id.empty_profile1_tv)
    public TextView emptyProfileTV1;
    @Bind(R.id.friend1_image)
    public CircleImageView friendImg1;

    @Bind(R.id.friend2_layout)
    public LinearLayout friendLayout2;
    @Bind(R.id.friend2_name)
    public TextView friendnameTV2;
    @Bind(R.id.empty_profile2_tv)
    public TextView emptyProfileTV2;
    @Bind(R.id.friend2_image)
    public CircleImageView friendImg2;

    @Bind(R.id.friend3_layout)
    public LinearLayout friendLayout3;
    @Bind(R.id.friend3_name)
    public TextView friendnameTV3;
    @Bind(R.id.empty_profile3_tv)
    public TextView emptyProfileTV3;
    @Bind(R.id.friend3_image)
    public CircleImageView friendImg3;

    @Bind(R.id.friend4_layout)
    public LinearLayout friendLayout4;
    @Bind(R.id.friend4_name)
    public TextView friendnameTV4;
    @Bind(R.id.empty_profile4_tv)
    public TextView emptyProfileTV4;
    @Bind(R.id.friend4_image)
    public CircleImageView friendImg4;


    @Bind(R.id.friend5_layout)
    public LinearLayout friendLayout5;
    @Bind(R.id.friend5_name)
    public TextView friendnameTV5;
    @Bind(R.id.empty_profile5_tv)
    public TextView emptyProfileTV5;
    @Bind(R.id.friend5_image)
    public CircleImageView friendImg5;

    @Bind(R.id.friend6_layout)
    public LinearLayout friendLayout6;
    @Bind(R.id.friend6_name)
    public TextView friendnameTV6;
    @Bind(R.id.empty_profile6_tv)
    public TextView emptyProfileTV6;
    @Bind(R.id.friend6_image)
    public CircleImageView friendImg6;

    @Bind(R.id.friend7_layout)
    public LinearLayout friendLayout7;
    @Bind(R.id.friend7_name)
    public TextView friendnameTV7;
    @Bind(R.id.empty_profile7_tv)
    public TextView emptyProfileTV7;
    @Bind(R.id.friend7_image)
    public CircleImageView friendImg7;

    @Bind(R.id.friend8_layout)
    public LinearLayout friendLayout8;
    @Bind(R.id.friend8_name)
    public TextView friendnameTV8;
    @Bind(R.id.empty_profile8_tv)
    public TextView emptyProfileTV8;
    @Bind(R.id.friend8_image)
    public CircleImageView friendImg8;

    @Bind(R.id.friend9_layout)
    public LinearLayout friendLayout9;
    @Bind(R.id.friend9_name)
    public TextView friendnameTV9;
    @Bind(R.id.empty_profile9_tv)
    public TextView emptyProfileTV9;
    @Bind(R.id.friend9_image)
    public CircleImageView friendImg9;

    @Bind(R.id.friend10_layout)
    public LinearLayout friendLayout10;
    @Bind(R.id.friend10_name)
    public TextView friendnameTV10;
    @Bind(R.id.empty_profile10_tv)
    public TextView emptyProfileTV10;
    @Bind(R.id.friend10_image)
    public CircleImageView friendImg10;

    public RecommendUserView[] friendImgs;


    public RecommendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<RecommendScreenComponent>getDaggerComponent(context).inject(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        presenter.takeView(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        presenter.dropView(this);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        UIHelper uiHelper = UIHelper.getInstance();
        uiHelper.setTypeface(friendnameTV1, friendnameTV2, friendnameTV3, friendnameTV4, friendnameTV5,
                friendnameTV5, friendnameTV6, friendnameTV7, friendnameTV8, friendnameTV9, friendnameTV10);
        uiHelper.setBoldTypeface(emptyProfileTV);
        uiHelper.setBoldTypeface(emptyProfileTV1);
        uiHelper.setBoldTypeface(emptyProfileTV2);
        uiHelper.setBoldTypeface(emptyProfileTV3);
        uiHelper.setBoldTypeface(emptyProfileTV4);
        uiHelper.setBoldTypeface(emptyProfileTV5);
        uiHelper.setBoldTypeface(emptyProfileTV6);
        uiHelper.setBoldTypeface(emptyProfileTV7);
        uiHelper.setBoldTypeface(emptyProfileTV8);
        uiHelper.setBoldTypeface(emptyProfileTV9);
        uiHelper.setBoldTypeface(emptyProfileTV10);
        uiHelper.setBoldTypeface(refreshBt);

        friendImgs = new RecommendUserView[]{
                new RecommendUserView(friendLayout1, friendnameTV1, friendImg1, emptyProfileTV1),
                new RecommendUserView(friendLayout2, friendnameTV2, friendImg2, emptyProfileTV2),
                new RecommendUserView(friendLayout3, friendnameTV3, friendImg3, emptyProfileTV3),
                new RecommendUserView(friendLayout4, friendnameTV4, friendImg4, emptyProfileTV4),
                new RecommendUserView(friendLayout5, friendnameTV5, friendImg5, emptyProfileTV5),
                new RecommendUserView(friendLayout6, friendnameTV6, friendImg6, emptyProfileTV6),
                new RecommendUserView(friendLayout7, friendnameTV7, friendImg7, emptyProfileTV7),
                new RecommendUserView(friendLayout8, friendnameTV8, friendImg8, emptyProfileTV8),
                new RecommendUserView(friendLayout9, friendnameTV9, friendImg9, emptyProfileTV9),
                new RecommendUserView(friendLayout10, friendnameTV10, friendImg10, emptyProfileTV10)
        };
        friendLayout1.setOnClickListener(presenter);
        friendLayout2.setOnClickListener(presenter);
        friendLayout3.setOnClickListener(presenter);
        friendLayout4.setOnClickListener(presenter);
        friendLayout5.setOnClickListener(presenter);
        friendLayout6.setOnClickListener(presenter);
        friendLayout7.setOnClickListener(presenter);
        friendLayout8.setOnClickListener(presenter);
        friendLayout9.setOnClickListener(presenter);
        friendLayout10.setOnClickListener(presenter);
    }

    public class RecommendUserView{
        public LinearLayout friendLayout;
        public TextView friendNameTV;
        public TextView emptyAvatarTV;
        public CircleImageView friendImg;
        public Point point;
        public RecommendUserView(LinearLayout friendLayout, TextView friendNameTV,
                                 CircleImageView friendImg, TextView emptyAvatarTV){
            this.friendLayout = friendLayout;
            this.friendNameTV = friendNameTV;
            this.friendImg = friendImg;
            this.emptyAvatarTV = emptyAvatarTV;
        }

        public void setData(String avatarId, String name, String fullname, Point point){
            UploadFileService.getInstance().loadAvatar(friendImg, avatarId,
                    emptyAvatarTV, fullname);
            friendNameTV.setText(name);
            friendLayout.setVisibility(VISIBLE);
            this.point = point;
        }

        public void setVisibility(int visibility){
            friendLayout.setVisibility(visibility);
        }

        public int getVisibility(){
            return friendLayout.getVisibility();
        }

        public void setLocation(){
            if (point == null) return;
            RelativeLayout.LayoutParams layoutParams =
                    (RelativeLayout.LayoutParams) friendLayout.getLayoutParams();
            int layoutWidth = friendLayout.getWidth();
            int layoutHeight = friendLayout.getHeight();
            int left = point.x - layoutWidth/2;
            int top = point.y - layoutHeight/2;
            int right = presenter.width - point.x - layoutWidth/2;
            int bottom = presenter.height - point.y - layoutHeight/2;
            layoutParams.setMargins(left, top, right, bottom);
            friendLayout.setLayoutParams(layoutParams);
            presenter.canvas.drawLine(presenter.centerW, presenter.centerH,
                    point.x, point.y, presenter.paint);
        }
    }
}
