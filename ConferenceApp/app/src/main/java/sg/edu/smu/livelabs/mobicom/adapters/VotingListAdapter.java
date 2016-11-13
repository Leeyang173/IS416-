package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.net.item.PollingItem;

/**
 * Created by johnlee on 18/2/16.
 */
public class VotingListAdapter extends BaseAdapter {

    private List<PollingItem> polling;
    private Context c;

    public VotingListAdapter(Context c, List<PollingItem> polling){
        this.c = c;
        this.polling = polling;

    }

    @Override
    public int getCount() {
        return polling.size();
    }

    @Override
    public PollingItem getItem(int position) {
        if(position == 0){
            return null;
        }
        return polling.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {

        return getCount();
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.list_item_voting, parent, false);

            // set value
            TextView votingTitle = (TextView) view.findViewById(R.id.title);
            votingTitle.setText(polling.get(position).title);

//            Date currentDate= new Date();
//
//            if(surveys.get(position).getEndTime() != null &&  currentDate.after(surveys.get(position).getEndTime())){ //null will be for survey that are not from BEP
//                view.setEnabled(false);
//                surveyTitle.setText("[Expired] " + surveys.get(position).getTitle());
//                surveyTitle.setTextColor(surveyView.getContext().getResources().getColor(R.color.grey));
//            }
        }

        return view;
    }


    public void updates(List<PollingItem> polling){
        this.polling = polling;
        this.notifyDataSetChanged();
        this.notifyDataSetInvalidated();
    }

}
