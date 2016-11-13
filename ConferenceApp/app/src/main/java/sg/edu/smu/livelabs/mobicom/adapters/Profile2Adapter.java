package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.models.data.InterestsEntity;

/**
 * Created by smu on 26/4/16.
 */
public class Profile2Adapter extends BaseAdapter {
    private List<InterestsEntity> interests;
    private Context context;
    private Profile2InterestListener listener;

    public Profile2Adapter(Context context, Profile2InterestListener listener){
        this.context = context;
        interests = new ArrayList<>();
        this.listener = listener;
    }

    public void setData(List<InterestsEntity> interests){
        if (interests == null) return;
        this.interests.clear();
        this.interests.addAll(interests);
    }

    @Override
    public int getCount() {
        return interests.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= getCount()) return null;
        InterestsEntity item = interests.get(position);
        ViewHolder viewHolder;
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.profile2__interest_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.setData(item);
        return convertView;
    }

    public class ViewHolder implements View.OnClickListener{
        @Bind(R.id.title_tv)
        public TextView titleTV;
        @Bind(R.id.content_layout)
        public RelativeLayout contentLayout;
        private InterestsEntity entity;
        public ViewHolder(View view){
            ButterKnife.bind(this, view);
            contentLayout.setOnClickListener(this);
        }

        public void setData(InterestsEntity entity){
            this.entity = entity;
            titleTV.setText(entity.getInterest());
            contentLayout.setSelected(listener.isMyInterest(entity.getInterest()));

        }

        @Override
        public void onClick(View v) {
            if (contentLayout.isSelected()){
                contentLayout.setSelected(false);
                listener.removeInterest(entity.getInterest());
            } else {
                contentLayout.setSelected(true);
                listener.addInterest(entity.getInterest());
            }
        }
    }

    public interface Profile2InterestListener{
        void addInterest(String interest);
        void removeInterest(String interest);
        boolean isMyInterest(String interest);
    }
}
