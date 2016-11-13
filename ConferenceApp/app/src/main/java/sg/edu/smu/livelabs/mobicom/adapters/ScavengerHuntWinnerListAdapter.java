package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.views.ScavengerHuntDetailView;

/**
 * Created by johnlee on 18/2/16.
 */
public class ScavengerHuntWinnerListAdapter extends BaseAdapter {

    private  List<String> winners;
    private ScavengerHuntDetailView scavengerHuntDetailView;
    private Bus bus;

    public ScavengerHuntWinnerListAdapter(ScavengerHuntDetailView scavengerHuntDetailView, List<String> winners, Bus bus){
        this.scavengerHuntDetailView = scavengerHuntDetailView;
        this.bus = bus;
        this.bus.register(this);
        this.winners = winners;
    }


    @Override
    public int getCount() {
        return winners.size();
    }

    @Override
    public String getItem(int position) {
        if(position == 0){
            return null;
        }
        return winners.get(position);
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
            ViewHolder holder = new ViewHolder();
            LayoutInflater vi = (LayoutInflater)scavengerHuntDetailView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.list_item_scavenger_winner, parent, false);

            holder.view = view;
            holder.winnerTV = (TextView)view.findViewById(R.id.winner);
            holder.positionIV = (ImageView)view.findViewById(R.id.position);

            if(winners.get(position) != ""){
                holder.winnerTV.setText(winners.get(position));
            }

            Picasso.with(scavengerHuntDetailView.getContext()).load(R.drawable.icon_winner).into(holder.positionIV);

            view.setTag(holder);

        }

        return view;
    }


    public void updates(List<String> winners){
        this.winners = winners;
        this.notifyDataSetChanged();
        this.notifyDataSetInvalidated();
    }


    private class ViewHolder {
        ImageView positionIV;
        TextView winnerTV;
        View view;

    }
}
