package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.net.item.EVAPromotionItem;
import sg.edu.smu.livelabs.mobicom.net.item.Selfie;
import sg.edu.smu.livelabs.mobicom.net.response.SelfieUserResponse;
import sg.edu.smu.livelabs.mobicom.services.EVAPromotionService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.util.Util;

/**
 * Created by johnlee on 20/10/15.
 */
public class SelfieSearchAdapter extends BaseAdapter {
    private final static int TYPE = 1;
    private final static int VIEW = 2;
    private final static int TYPE_USER  = 1;
    private final static int TYPE_PHOTO = 2;
    private Context context;
    private List<Object> objects;
    private SelfieSearchListener listenter;

    public SelfieSearchAdapter(Context context, SelfieSearchListener listenter) {
        this.context = context;
        objects = new ArrayList<>();
        this.listenter = listenter;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        if (position > objects.size()) return null;
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void setData(List<Object> data){
        objects.clear();
        objects.addAll(data);
        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Object o = objects.get(position);
        if (o instanceof SelfieUserResponse){
            ViewHolder holder;
            if (convertView == null || (int)convertView.getTag(R.string.type) == TYPE_PHOTO) {
                convertView = LayoutInflater.from(context).inflate(R.layout.selfie_search_item, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(R.string.type, TYPE_USER);
                convertView.setTag(R.string.view, holder);
            } else {
                holder = (ViewHolder) convertView.getTag(R.string.view);
            }
            holder.setData((SelfieUserResponse) o);
        } else if (o instanceof Selfie){
            ViewHolder2 holder;
            if (convertView == null || (int)convertView.getTag(R.string.type) == TYPE_USER) {
                convertView = LayoutInflater.from(context).inflate(R.layout.selfie_search_photo_item, parent, false);
                holder = new ViewHolder2(convertView);
                convertView.setTag(R.string.type, TYPE_PHOTO);
                convertView.setTag(R.string.view, holder);
            } else {
                holder = (ViewHolder2) convertView.getTag(R.string.view);
            }
            holder.setData((Selfie) o, position);
        }
        return convertView;
    }

    class ViewHolder implements View.OnClickListener{

        @Bind(R.id.avatar_image)
        public ImageView userImage;

        @Bind(R.id.name_short_form)
        public TextView nameShortFormTV;

        @Bind(R.id.username_text)
        public TextView usernameTV;

        @Bind(R.id.email_text)
        public TextView emailTV;

        @Bind(R.id.photo_text)
        public TextView photoTV;

        private SelfieUserResponse userResponse;

        ViewHolder(View view){
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
            UIHelper.getInstance().setTypeface(usernameTV, emailTV);
        }

        public void setData(final SelfieUserResponse userResponse){
            this.userResponse = userResponse;
            usernameTV.setText(userResponse.name);
            emailTV.setText(userResponse.email);
            Picasso.with(context).cancelRequest(userImage);
            if (userResponse.avatar != null && !userResponse.avatar.isEmpty()){
                Picasso.with(context)
                        .load(Util.getPhotoUrlFromId(userResponse.avatar, 96))
                        .noFade()
                        .placeholder(R.drawable.empty_profile)
                        .into(userImage, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                nameShortFormTV.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() { //when failed to load, draw a rect on top of the noavatar and add the name short form
                                GameService.getInstance().drawNameShort(userResponse.name, userImage, nameShortFormTV);
                            }
                        });
            } else {
//                userImage.setImageResource(R.drawable.empty_profile);
                GameService.getInstance().drawNameShort(userResponse.name, userImage, nameShortFormTV);
            }
            photoTV.setText(userResponse.getImageCount0());
        }

        @Override
        public void onClick(View v) {
            User user = new User();
            user.setUID(userResponse.id);
            user.setName(userResponse.name);
            user.setEmail(userResponse.email);
            user.setAvatar(userResponse.avatar);
            listenter.openUserProfile(user);
        }
    }

    public class ViewHolder2 implements View.OnClickListener{

        @Bind(R.id.img)
        public ImageView img;
        @Bind(R.id.token)
        public TextView token;
        @Bind(R.id.heart_img)
        public ImageView heartImg;
        @Bind(R.id.likes)
        public TextView like;
        @Bind(R.id.description)
        public TextView descriptionText;
        @Bind(R.id.user_name)
        public TextView userNameText;
        @Bind(R.id.promotion_name)
        public TextView promotionName;

        private Selfie selfie;
        private int position;

        public ViewHolder2(View itemView) {
            ButterKnife.bind(this, itemView);
//            UIHelper.getInstance().setTypeface(like, true);
//            UIHelper.getInstance().setTypeface(token, true);
//            UIHelper.getInstance().setTypeface(promotionName, true);
            UIHelper.getInstance().setTypeface(descriptionText);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listenter.openListView(this.position);
        }

        public void setData(Selfie selfie, int position) {
            this.selfie = selfie;
            this.position = position;
            if (selfie.imageId != null || !selfie.imageId.isEmpty()){
                Picasso.with(context).cancelRequest(img);
                Picasso.with(context)
                        .load(Util.getPhotoUrlFromId(selfie.imageId, 256))
                        .noFade()
                        .placeholder(R.drawable.placeholder)
                        .into(img);
            } else {
                img.setImageResource(R.drawable.placeholder);
            }
            String des = selfie.description;
            if (des == null || des.equals("null")){
                des = "";
            }
            String description = String.format("<b>Details:</b> %s", des);
            String name = String.format("<b>By:</b> %s", selfie.username);
            EVAPromotionItem promotion = EVAPromotionService.getInstance().getEVAPromotion(selfie.promotionId);
            if (promotion == null){
                promotionName.setText("");
            } else {
                promotionName.setText(promotion.name);
            }
            descriptionText.setText(Html.fromHtml(description));
            userNameText.setText(Html.fromHtml(name));
            token.setText(selfie.token);
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
        }
    }

    public interface SelfieSearchListener{
        void openListView(int currentSelfie);
        void openUserProfile(User user);
    }
}
