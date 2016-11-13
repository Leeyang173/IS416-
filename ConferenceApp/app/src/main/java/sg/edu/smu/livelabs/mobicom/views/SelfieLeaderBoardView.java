package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.models.SelfieLine;
import sg.edu.smu.livelabs.mobicom.net.item.Selfie;
import sg.edu.smu.livelabs.mobicom.net.response.SelfiePhotosResponse;
import sg.edu.smu.livelabs.mobicom.presenters.SelfieHomePresenter;
import sg.edu.smu.livelabs.mobicom.util.Util;

/**
 * Created by smu on 3/11/15.
 */
public class SelfieLeaderBoardView implements View.OnClickListener{
    @Bind(R.id.title)
    public TextView title;
    @Bind(R.id.img1)
    public ImageView imageView1;
    @Bind(R.id.likes1)
    public TextView likes1;
    @Bind(R.id.name1)
    public TextView name1;
    @Bind(R.id.metal_1)
    public ImageView metal1;

    @Bind(R.id.img2)
    public ImageView imageView2;
    @Bind(R.id.likes2)
    public TextView likes2;
    @Bind(R.id.name2)
    public TextView name2;
    @Bind(R.id.metal_2)
    public ImageView metal2;

    @Bind(R.id.img3)
    public ImageView imageView3;
    @Bind(R.id.likes3)
    public TextView likes3;
    @Bind(R.id.name3)
    public TextView name3;
    @Bind(R.id.metal_3)
    public ImageView metal3;


    //2 center
    @Bind(R.id.img4)
    public ImageView imageView4;
    @Bind(R.id.likes4)
    public TextView likes4;
    @Bind(R.id.name4)
    public TextView name4;
    @Bind(R.id.metal_4)
    public ImageView metal4;

    @Bind(R.id.top_layout)
    public LinearLayout topLayout;
    @Bind(R.id.top2_center_layout)
    public RelativeLayout top2CenterLayout;
    @Bind(R.id.top2_layout)
    public LinearLayout top2Layout;
    @Bind(R.id.top3_layout)
    public RelativeLayout top3Layout;


    public SelfieLine selfieLine;
    private Context context;
    private SelfieHomePresenter listener;
    public View view;

    public SelfieLeaderBoardView(View itemView, Context context) {
        ButterKnife.bind(this, itemView);
        this.view = itemView;
        UIHelper.getInstance().setTypeface(title, name1, name2, name3, likes1, likes2, likes3);
        imageView1.setOnClickListener(this);
        imageView2.setOnClickListener(this);
        imageView3.setOnClickListener(this);
        imageView4.setOnClickListener(this);
        this.context = context;
    }

    public void setData(SelfiePhotosResponse leaderboard, boolean enableMetal){
        if (leaderboard.selfies.size() > 1){
            final int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 325, context.getResources().getDisplayMetrics());
            this.view.setLayoutParams(
                    new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            px));
        } else {
            final int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 215, context.getResources().getDisplayMetrics());
            this.view.setLayoutParams(
                    new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            px));
        }
        if (enableMetal){
            metal1.setVisibility(View.VISIBLE);
            metal2.setVisibility(View.VISIBLE);
            metal3.setVisibility(View.VISIBLE);
            metal4.setVisibility(View.VISIBLE);
        } else {
            metal1.setVisibility(View.GONE);
            metal2.setVisibility(View.GONE);
            metal3.setVisibility(View.GONE);
            metal4.setVisibility(View.GONE);
        }
        title.setText(leaderboard.title);
        this.selfieLine = new SelfieLine();
        int count = leaderboard.selfies.size();
        if (count > 0){
            this.selfieLine.setFrist(leaderboard.selfies.get(0));
            setData(selfieLine.getFrist(), imageView1, name1, likes1);
            if (count > 1){
                if (count > 2){
                    this.selfieLine.setSecond(leaderboard.selfies.get(1));
                    setData(selfieLine.getSecond(), imageView2, name2, likes2);
                    this.selfieLine.setThird(leaderboard.selfies.get(2));
                    setData(selfieLine.getThird(), imageView3, name3, likes3);
                    top2Layout.setVisibility(View.VISIBLE);
                    top2CenterLayout.setVisibility(View.GONE);
                } else {
                    this.selfieLine.setSecond(leaderboard.selfies.get(1));
                    setData(selfieLine.getSecond(), imageView4, name4, likes4);
                    top2Layout.setVisibility(View.GONE);
                    top2CenterLayout.setVisibility(View.VISIBLE);
                }
            } else {
                top2Layout.setVisibility(View.GONE);
                top2CenterLayout.setVisibility(View.GONE);
            }
        } else {
            topLayout.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.img1:
                listener.openTopListView(0);
                break;
            case R.id.img4:
            case R.id.img2:
                listener.openTopListView(1);
                break;
            case R.id.img3:
                listener.openTopListView(2);
                break;
        }
    }

    public void setListener(SelfieHomePresenter selfieHomePresenter){
        this.listener = selfieHomePresenter;
    }

    private void setData(Selfie selfie, ImageView img, TextView name, TextView like){
        if (selfie != null){
            if (selfie.imageId != null && !selfie.imageId.isEmpty()) {
                try {
                    Picasso.with(context)
                            .load(Util.getPhotoUrlFromId(selfie.imageId, 512))
                            .noFade()
                            .placeholder(R.drawable.placeholder).into(img);
                }
                catch(Throwable e){
                    Log.d("AAA", "SelfieLeaderboardView:" + e.toString());
                }
            } else {
                img.setImageResource(R.drawable.placeholder);
            }
            like.setText(selfie.getLikesCount());
            name.setText(selfie.email);
        } else {
            img.setImageResource(R.drawable.placeholder);
            like.setText("");
            name.setText("");
        }
    }
}
