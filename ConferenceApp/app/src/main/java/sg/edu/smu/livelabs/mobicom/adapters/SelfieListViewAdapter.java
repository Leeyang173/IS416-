package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import rx.functions.Action0;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.net.item.Selfie;
import sg.edu.smu.livelabs.mobicom.presenters.SelfieListPresenter;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.EVAPromotionService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.util.Util;

/**
 * Created by smu on 3/11/15.
 */
public class SelfieListViewAdapter extends UltimateViewAdapter<SelfieListViewAdapter.ViewHolder> {

    private Context context;
    private List<Selfie> selfies;
    private SelfieListPresenter listener;
    private View parentView;

    public SelfieListViewAdapter(Context context, SelfieListPresenter listener, View parentView){
        this.context = context;
        selfies = new ArrayList<>();
        this.listener = listener;
        this.parentView = parentView;
    }

    public void setData(List<Selfie> selfies){
        this.selfies.clear();
        this.selfies.addAll(selfies);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder getViewHolder(View view) {
        return new ViewHolder(view, false);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(context).inflate(R.layout.selfie_list_item, parent, false);
        return new ViewHolder(v, true);
    }

    @Override
    public int getAdapterItemCount() {
        return selfies == null ? 0 : selfies.size();
    }

    @Override
    public long generateHeaderId(int position) {
        if (customHeaderView != null)
            position--;

//        return position;

        if (position < selfies.size()){
            Selfie selfie = selfies.get(position);
            if (selfie != null) return Long.valueOf(selfie.id);
            return 0;
        }
        return -1;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position < getItemCount() &&
                (customHeaderView != null ? position <= selfies.size() : position < selfies.size()) &&
                (customHeaderView != null ? position > 0 : true)) {
            Selfie selfie = this.getItem(position);
            holder.setData(selfie);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(context).inflate(R.layout.selfie_list_header, parent, false);
        return new ViewHolderHeader(v);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (customHeaderView != null)
            position--;
        if (position >= selfies.size()) return;
        Selfie selfie = selfies.get(position);
        ViewHolderHeader headerViewHolder = ((ViewHolderHeader) holder);
        headerViewHolder.setData(selfie);
    }

    public void insertData(List<Selfie> newSelfies) {
        List<Selfie> newSelfiesTmp = new ArrayList<>();
        for(Selfie s: newSelfies){
            boolean isExist = false;
            int position = 0;
            for(Selfie selfie: this.selfies){
                if(selfie.id.equals(s.id)){
                    isExist = true;
                    break;
                }
                position++;
            }

            if(isExist){
                this.selfies.remove(position);
                this.selfies.add(position, s);
                newSelfiesTmp.add(s);
            }
        }
        notifyDataSetChanged();

        newSelfies.removeAll(newSelfiesTmp);
//        for (Selfie selfie : newSelfies) {
//            insertFirstInternal(this.selfies, selfie);
//        }
        insertInternal(newSelfies, this.selfies);
    }

    public Selfie getLastItem() {
        return selfies.get(selfies.size() - 1);
    }

    public Selfie getItem(int position) {
        return selfies.get(customHeaderView != null ? position - 1 : position);
    }

    public void removeItemAt(int position){
        removeInternal(this.selfies, position);
    }

    public List<Selfie> getAllItems() {
        return selfies;
    }

    public class ViewHolder extends UltimateRecyclerviewViewHolder implements View.OnClickListener{

        @Bind(R.id.img)
        public ImageView img;
        @Bind(R.id.reportBtn)
        public ImageView reportBtn;
        @Bind(R.id.likeBtn)
        public ImageView likeBtn;
        @Bind(R.id.heart_img)
        public ImageView heartImg;
        @Bind(R.id.moreBtn)
        public ImageView moreBtn;
        @Bind(R.id.likes)
        public TextView like;
        @Bind(R.id.description)
        public TextView description;


        private Selfie selfie;

        public ViewHolder(View itemView, boolean isNew) {
            super(itemView);
            if (isNew){
                ButterKnife.bind(this, itemView);
                UIHelper.getInstance().setTypeface(description);
                UIHelper.getInstance().setTypeface(like);
                likeBtn.setOnClickListener(this);
                reportBtn.setOnClickListener(this);
                moreBtn.setOnClickListener(this);
                like.setOnClickListener(this);
                img.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id){
                case R.id.likeBtn:
                    UIHelper.getInstance().showProgressDialog(context, "Processing...", false);
                    if ("yes".equals(selfie.likeStatus)){
                        EVAPromotionService.getInstance().unlike(selfie, false);
                    } else {
                        EVAPromotionService.getInstance().like(selfie, false, parentView);
                    }
                    String cmpId = EVAPromotionService.getInstance().currentPromotion.id;
//                    EVAPromotionService.getInstance().selfieTracking("605", cmpId, selfie.id, "like");
                    break;
                case R.id.likes:
                    if (selfie.likes > 0){
                        listener.goToLikerScreen(getAdapterPosition());
                    }
                    break;
                case R.id.reportBtn:
                    Resources rs = context.getResources();
                    if ("yes".equals(selfie.reportStatus)){
                        UIHelper.getInstance().showAlert(context, rs.getString(R.string.reported));
                        return;
                    }
                    UIHelper.getInstance().showConfirmAlert(context, context.getString(R.string.app_name), rs.getString(R.string.confirm_report_photo), "Yes", "No", new Action0() {
                        @Override
                        public void call() {
                            UIHelper.getInstance().showAlert(context, context.getResources().getString(R.string.thanks_report));
                            EVAPromotionService.getInstance().report(selfie, false);
                            reportBtn.setSelected(true);
                        }
                    }, new Action0() {
                        @Override
                        public void call() {

                        }
                    });
                    String cmpId1 = EVAPromotionService.getInstance().currentPromotion.id;
//                    EVAPromotionService.getInstance().selfieTracking("605", cmpId1, selfie.id, "report");
                    break;
                case R.id.img:
                    listener.goToFullScreen(getAdapterPosition());
                    break;
                case R.id.moreBtn:
                    listener.editSelfie(selfie, getAdapterPosition(), img);
            }
        }

        public void setData(Selfie selfie) {
            this.selfie = selfie;
            Picasso.with(context).cancelRequest(img);
            if (selfie.imageId != null && !selfie.imageId.isEmpty()){
                Picasso.with(context)
                        .load(Util.getPhotoUrlFromId(selfie.imageId, 512))
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .placeholder(R.drawable.transparent)
                        .into(img);
            } else {
                img.setImageResource(R.drawable.transparent);
            }

            if (DatabaseService.getInstance().getMe().getUID() == selfie.userId){
                reportBtn.setVisibility(View.GONE);
            }else{
                reportBtn.setVisibility(View.VISIBLE);
            }

            description.setText(selfie.description);
            int likeNo = selfie.likes;
            if (likeNo < 1){
                like.setText("");
                heartImg.setVisibility(View.INVISIBLE);
            } else if (likeNo < 2){
                like.setText(likeNo + " like");
                heartImg.setVisibility(View.VISIBLE);
            } else {
                like.setText(likeNo + " likes");
                heartImg.setVisibility(View.VISIBLE);
            }
            if ("yes".equals(selfie.likeStatus)){
                likeBtn.setSelected(true);
            } else {
                likeBtn.setSelected(false);
            }
            if ("yes".equals(selfie.reportStatus)){
                reportBtn.setSelected(true);
            } else {
                reportBtn.setSelected(false);
            }
        }
    }

    public class ViewHolderHeader extends RecyclerView.ViewHolder{

        @Bind(R.id.avatar_image)
        public CircleImageView avatar;
        @Bind(R.id.name)
        public TextView name;
        @Bind(R.id.email)
        public TextView email;
        @Bind(R.id.name_short_form)
        public TextView nameShortFormTV;
        @Bind(R.id.time)
        public TextView timeText;

        public Selfie selfie;


        public ViewHolderHeader(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            UIHelper.getInstance().setTypeface(name, email, timeText);
        }

        public void setData(final Selfie selfie) {
            this.selfie = selfie;
            if (selfie.userAvatar == null || selfie.userAvatar.isEmpty()){
                GameService.getInstance().drawNameShort(selfie.username, avatar, nameShortFormTV);
            } else {
                try {
                    Picasso.with(context)
                            .load(Util.getPhotoUrlFromId(selfie.userAvatar, 96))
                            .noFade()
                            .placeholder(R.drawable.empty_profile)
                            .into(avatar, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    nameShortFormTV.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError() { //when failed to load, draw a rect on top of the noavatar and add the name short form
                                    GameService.getInstance().drawNameShort(selfie.username, avatar, nameShortFormTV);
                                }
                            });
                }
                catch(Throwable e){//catch OOM
                    System.gc();
                }
            }
            name.setText(selfie.username);
            email.setText(selfie.email);
            long time = (System.currentTimeMillis() - selfie.createdTime.getTime()) / 1000;
            if (time < 60){
                if(time >= 0) //to avoid negative value
                    timeText.setText(time+"s");
                else{
                    timeText.setText("0s");
                }
            } else {
                time = time/60;
                if (time < 60){
                    timeText.setText(time+"m");
                } else{
                    time = time/60;
                    if (time > 24){
                        long day = time / 24;
                        if(day < 7){
                            timeText.setText(day+"d");
                        } else {
                            if (day > 7){
                                long week = day / 7;
                                timeText.setText(week+"w");
                            }
                        }
                    } else {
                        timeText.setText(time+"h");
                    }
                }
            }
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
}
