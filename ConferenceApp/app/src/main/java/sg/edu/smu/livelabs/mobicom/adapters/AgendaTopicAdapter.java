package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.models.data.EventEntity;
import sg.edu.smu.livelabs.mobicom.services.AgendaService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;

/**
 * Created by smu on 26/2/16.
 */
public class AgendaTopicAdapter extends BaseAdapter {
    private List<EventEntity> topics;
    private Context context;
    private AgendaAdapter.AgendaListener listener;
    private boolean isClickable;

    public AgendaTopicAdapter(Context context, List<EventEntity> topics,
                              AgendaAdapter.AgendaListener listener,
                              boolean isClickable){
        this.topics = topics;
        this.context = context;
        this.listener = listener;
        this.isClickable = isClickable;
        if (topics == null){
            this.topics = new ArrayList<>();
        }
    }
    @Override
    public int getCount() {
        return topics.size();
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
        ViewHolder VH;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.agenda_topic_item, parent, false);
            VH = new ViewHolder(convertView);
            convertView.setTag(VH);
        }
        EventEntity agendaTopic = topics.get(position);
        VH = (ViewHolder) convertView.getTag();
        VH.setData(agendaTopic, position);

        return convertView;
    }

    public class ViewHolder implements View.OnClickListener{
        @Bind(R.id.description_txt)
        public TextView descriptionTxt;
        @Bind(R.id.title_txt)
        public TextView titleTxt;
        @Bind(R.id.rating_txt)
        public TextView ratingText;
        @Bind(R.id.more_iv)
        public ImageView moreIV;
        private EventEntity topic;
        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
            UIHelper.getInstance().setBoldTypeface(titleTxt);
            UIHelper.getInstance().setItalicTypeface(descriptionTxt);
            UIHelper.getInstance().setTypeface(ratingText);
            if (isClickable){
                view.setOnClickListener(this);
            } else {
                moreIV.setVisibility(View.GONE);
            }

        }

        public void setData(EventEntity topic, int position) {
            this.topic = topic;
            titleTxt.setText(topic.getTitle());
            descriptionTxt.setText(topic.getDescription());
            if (AgendaService.RATING.equals(topic.getRatingQuizStatus())){
                ratingText.setVisibility(View.VISIBLE);
                ratingText.setText("Rating is available.");
            }
            else if(AgendaService.QUIZ.equals(topic.getRatingQuizStatus())){
                ratingText.setVisibility(View.VISIBLE);
                ratingText.setText("Quiz is available.");
            }else if(AgendaService.RATING_QUIZ.equals(topic.getRatingQuizStatus())){
                ratingText.setVisibility(View.VISIBLE);
                ratingText.setText("Quiz and Rating are available.");
            } else {
                ratingText.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            listener.goToDetailPage(topic);
            TrackingService.getInstance().sendTracking("104", "agenda", Long.toString(topic.getServerId()), "", "", "");
        }
    }

}
