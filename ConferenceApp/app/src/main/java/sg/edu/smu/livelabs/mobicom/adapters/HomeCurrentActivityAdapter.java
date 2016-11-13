package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.net.item.BannerItem;

/**
 * Created by smu on 21/4/16.
 */
public class HomeCurrentActivityAdapter extends BaseAdapter {
    private List<BannerItem> bannerItems;
    private Context context;
    public HomeCurrentActivityAdapter(Context context, List<BannerItem> bannerItems){
        this.context = context;
        this.bannerItems = bannerItems;
    }

    @Override
    public int getCount() {
        return bannerItems.size();
    }

    @Override
    public BannerItem getItem(int position) {
        if(bannerItems != null && bannerItems.size() >0)
            return bannerItems.get(position);
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.home_current_activity_item, parent, false);

            holder.view = view;

            view.setTag(holder);
        }
        else{
            holder = (ViewHolder) view.getTag();
        }

        holder.banner = (ImageView) view.findViewById(R.id.banner);
        BannerItem item = bannerItems.get(getItemViewType(position));
        holder.item = item;

        if(holder.item != null) {
            if (!holder.item.bannerImage.equals("")) {
                Picasso.with(context).load(holder.item.bannerImage)
//                        .resize(400, 99)
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .placeholder(R.drawable.banner_default).into(holder.banner);
            }

        }

        return view;
    }

    public class ViewHolder{
        ImageView banner;
        View view;
        BannerItem item;
    }
}