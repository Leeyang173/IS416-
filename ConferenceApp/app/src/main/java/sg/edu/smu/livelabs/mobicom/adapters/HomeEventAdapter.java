package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.models.AgendaEvent;

/**
 * Created by smu on 21/4/16.
 */
public class HomeEventAdapter extends BaseAdapter {
    private List<AgendaEvent> events;
    private Context context;
    public HomeEventAdapter(Context context){
        this.context = context;
        events = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void setData(List<AgendaEvent> entities){
        if (entities != null){
            this.events.clear();
            this.events.addAll(entities);
            notifyDataSetChanged();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position > events.size()) return null;
        AgendaEvent agendaEvent = events.get(position);
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.home_event_tem, parent, false);
        }
        View backgroundView = convertView.findViewById(R.id.background_view);
        if (agendaEvent.isCurrentEvent()){
            backgroundView.setAlpha(1.0f);
        } else {
            backgroundView.setAlpha(0.6f);
        }
        TextView timeTV = (TextView) convertView.findViewById(R.id.time_tv);
        TextView roomNoTV = (TextView) convertView.findViewById(R.id.room_no_tv);
        TextView titleTV = (TextView) convertView.findViewById(R.id.title_tv);
        TextView descriptionTV = (TextView) convertView.findViewById(R.id.description_tv);
        UIHelper uiHelper = UIHelper.getInstance();
        uiHelper.setTypeface(roomNoTV, descriptionTV);
        uiHelper.setBoldTypeface(titleTV);
        uiHelper.setBoldTypeface(timeTV);
        timeTV.setText(agendaEvent.getStartTimeStr());
        roomNoTV.setText(agendaEvent.getRoom());
        titleTV.setText(agendaEvent.getTitle());
        descriptionTV.setText(agendaEvent.getDescription());
        return convertView;
    }
}
