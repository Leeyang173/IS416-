package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.models.data.EventEntity;

/**
 * Created by smu on 8/6/16.
 */
public class AgendaWorkshopPaperAdapter extends BaseAdapter {
    private Context context;
    private List<EventEntity> eventEntities;

    public AgendaWorkshopPaperAdapter(Context context, List<EventEntity> eventEntities){
        this.context = context;
        this.eventEntities = eventEntities;
    }

    @Override
    public int getCount() {
        return eventEntities.size();
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
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.agenda_workshop_paper_item, parent, false);
            VH = new ViewHolder(convertView);
            convertView.setTag(VH);
        } else {
            VH = (ViewHolder) convertView.getTag();
        }
        EventEntity eventEntity = eventEntities.get(position);
        VH.titleTV.setText(eventEntity.getTitle());
        VH.descriptionTV.setText(eventEntity.getDescription());
        return convertView;
    }

    public class ViewHolder{
        public TextView titleTV;
        public TextView descriptionTV;
        public ViewHolder(View view){
            titleTV = (TextView) view.findViewById(R.id.title_txt);
            descriptionTV = (TextView) view.findViewById(R.id.description_txt);
            UIHelper.getInstance().setBoldTypeface(titleTV);
            UIHelper.getInstance().setTypeface(descriptionTV);

        }
    }
}
