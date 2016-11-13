package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.net.item.StumpItem;

/**
 * Created by johnlee on 18/2/16.
 */
public class StumpListAdapter extends BaseAdapter {

    private  List<StumpItem> stumps;
    private Context c;

    public StumpListAdapter(Context c, List<StumpItem> stumps){
        this.c = c;
        this.stumps = stumps;
    }

    @Override
    public int getCount() {
        return stumps.size();
    }

    @Override
    public StumpItem getItem(int position) {
        if(stumps == null){
            return null;
        }
        return stumps.get(position);
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

        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.list_item_stump, parent, false);

            holder.view = view;
            holder.stump = stumps.get(getItemViewType(position));
            holder.stumpIV = (ImageView)view.findViewById(R.id.image);
            holder.titleTV = (TextView)view.findViewById(R.id.title);
            holder.desTV = (TextView)view.findViewById(R.id.summary);
            view.setTag(holder);
        }
        else{
            holder = (ViewHolder) view.getTag();
        }

        if(holder.stump.imageURL != null && !holder.stump.imageURL.isEmpty()){
            Picasso.with(c).
                    load(holder.stump.imageURL)
                    .resize(96,96)
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .placeholder(R.drawable.default_game).into(holder.stumpIV);
        }

        holder.titleTV.setText(holder.stump.title);
        holder.desTV.setText(holder.stump.description);
        return view;
    }


    public void updates(List<StumpItem> stumps){
        this.stumps = stumps;
        this.notifyDataSetChanged();
        this.notifyDataSetInvalidated();
    }


    private class ViewHolder {
        ImageView stumpIV;
        TextView titleTV;
        TextView desTV;
        StumpItem stump;
        View view;

    }
}
