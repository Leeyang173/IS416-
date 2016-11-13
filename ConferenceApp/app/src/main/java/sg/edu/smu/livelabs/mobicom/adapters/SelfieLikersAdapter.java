package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
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
import sg.edu.smu.livelabs.mobicom.net.response.SelfieUserResponse;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.util.Util;

/**
 * Created by smu on 12/11/15.
 */
public class SelfieLikersAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<SelfieUserResponse> selfieUserResponses;
    private LikerListener listener;
    public SelfieLikersAdapter(Context context, LikerListener listener){
        this.context = context;
        this.selfieUserResponses = new ArrayList<>();
        this.listener = listener;
    }

    public void setData(List<SelfieUserResponse> selfieUserResponses){
        this.selfieUserResponses.clear();
        this.selfieUserResponses.addAll(selfieUserResponses);
        notifyDataSetChanged();
    }

    public ArrayList<SelfieUserResponse> getAllData(){
        return selfieUserResponses;
    }

    @Override
    public int getCount() {
        return selfieUserResponses.size();
    }

    @Override
    public Object getItem(int position) {
        if (position > getCount()) return null;
        return selfieUserResponses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.selfie_search_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        SelfieUserResponse user = selfieUserResponses.get(position);
        holder.setData(user);
        return convertView;
    }

    class ViewHolder implements View.OnClickListener{

        @Bind(R.id.avatar_image)
        public ImageView userImage;

        @Bind(R.id.username_text)
        public TextView usernameTV;

        @Bind(R.id.email_text)
        public TextView emailTV;
        @Bind(R.id.photo_text)
        public TextView photoText;

        @Bind(R.id.name_short_form)
        public TextView nameShortFormTV;



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
            photoText.setText(userResponse.getImageCount());
        }

        @Override
        public void onClick(View v) {
            User user = new User();
            user.setUID(userResponse.id);
            user.setName(userResponse.name);
            user.setEmail(userResponse.email);
            user.setAvatar(userResponse.avatar);
            listener.goToUserDetail(user);
        }
    }

    public interface LikerListener{
        void goToUserDetail(User user);
    }
}
