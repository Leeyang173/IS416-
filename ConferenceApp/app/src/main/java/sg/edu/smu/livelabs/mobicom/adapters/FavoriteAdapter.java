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

import butterknife.Bind;
import butterknife.ButterKnife;
import flow.Flow;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.net.item.FavoriteListItem;
import sg.edu.smu.livelabs.mobicom.presenters.screen.FavoriteItemScreen;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;

/**
 * Created by smu on 27/10/15.
 */
public class FavoriteAdapter extends BaseAdapter {
    private Context context;
    private List<FavoriteListItem> items;
    private  MainActivity  mainActivity;

    public FavoriteAdapter(Context context, MainActivity  mainActivity){
        this.context = context;
        this.mainActivity = mainActivity;
    }

    public void setItems(List<FavoriteListItem> items){
        this.items = items;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public FavoriteListItem getItem(int position) {
        if (items == null || items.size() <= position)
            return null;
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.list_favorite_item, null);
            ViewHolder VH = new ViewHolder(convertView);
            convertView.setTag(VH);
        }
        ViewHolder VH = (ViewHolder) convertView.getTag();
        FavoriteListItem items = getItem(position);

        if (items != null){
            VH.title.setText(items.name);
//            VH.promotionTime.setText(items.timeString);
            Picasso.with(context).cancelRequest(VH.img);
            if (items.image != null && !items.image.isEmpty()){
                try {
                    Picasso.with(context)
//                        .load(RestClient.BASE_URL + promotion.image)
                            .load(items.image)
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .noFade().placeholder(R.drawable.generic_img_new)
                            .into(VH.img);
                }
                catch(Throwable e){//catch OOM
                    System.gc();
                }
            } else {
                VH.img.setImageResource(R.drawable.generic_img_new);
            }

            VH.items = items;
            VH.promotionImgNum.setText("Combined votes: " + items.uniqueVotersCount);
            VH.description.setVisibility(View.INVISIBLE);
            VH.promotionImgNum.setVisibility(View.VISIBLE);

        }
        return convertView;
    }

    class ViewHolder implements View.OnClickListener{
        @Bind(R.id.promotion_title)
        public TextView title;
        @Bind(R.id.promotion_description)
        public TextView description;
        @Bind(R.id.promotion_time)
        public TextView promotionTime;
        @Bind(R.id.promotion_img_num)
        public TextView promotionImgNum;
        @Bind(R.id.promotion_img)
        public ImageView img;

        public FavoriteListItem items;
        public ViewHolder(View view){
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            TrackingService.getInstance().sendTracking("421", "games", "fav_comp", items.id, "", "");
            Flow.get(context).set(new FavoriteItemScreen(Long.parseLong(items.id)));
        }
    }
}
