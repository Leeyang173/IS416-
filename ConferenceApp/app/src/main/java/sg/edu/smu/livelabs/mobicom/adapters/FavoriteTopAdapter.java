package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.net.item.FavoriteItem;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.util.Util;

/**
 * Created by smu on 27/10/15.
 */
public class FavoriteTopAdapter extends BaseAdapter {
    private Context context;
    private List<FavoriteItem> items;
    private  MainActivity  mainActivity;

    public FavoriteTopAdapter(Context context, MainActivity mainActivity, List<FavoriteItem> items){
        this.context = context;
        this.mainActivity = mainActivity;
        this.items = items;
    }

//    public void setItems(List<FavoriteItem> items){
//        this.items.clear();
//        this.items = items;
//        notifyDataSetChanged();
//        notifyDataSetInvalidated();
//    }

    public void updateItems(List<FavoriteItem> items){
        this.items.clear();
        this.items = items;
        notifyDataSetChanged();
        notifyDataSetInvalidated();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public FavoriteItem getItem(int position) {
        if (items == null || items.size() <= position)
            return null;
        return items.get(position);
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        if (convertView == null){
//            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_favorite_top, null);
//            ViewHolder VH = new ViewHolder(convertView);
//            convertView.setTag(VH);
//        }
//        final ViewHolder VH = (ViewHolder) convertView.getTag();

        View view = convertView;
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.list_item_favorite_top, parent, false);

            holder.view = view;

            view.setTag(holder);
        }
        else{
            holder = (ViewHolder) view.getTag();
        }

        holder.nameShortFormTV = (TextView) view.findViewById(R.id.name_short_form);
        holder.ranking = (TextView) view.findViewById(R.id.ranking);
        holder.name = (TextView) view.findViewById(R.id.name);
        holder.vote = (TextView) view.findViewById(R.id.vote);
        holder.rankingContainer = (LinearLayout) view.findViewById(R.id.ranking_container);
        holder.avatar = (CircleImageView) view.findViewById(R.id.avatar_image);
        final FavoriteItem item = items.get(getItemViewType(position));
        holder.items = item;
        Picasso.with(context).cancelRequest(holder.avatar);

        if (holder.items != null){
            holder.name.setText("" + holder.items.name);
            holder.vote.setText("Votes: " + holder.items.count);

            if (holder.items.avatar != null && !holder.items.avatar.isEmpty()){
                Picasso.with(context)
                        .load(Util.getPhotoUrlFromId(holder.items.avatar, 96))
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .noFade().placeholder(R.drawable.icon_no_profile)
                        .into(holder.avatar, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                holder.nameShortFormTV.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() { //when failed to load, draw a rect on top of the noavatar and add the name short form
                                GameService.getInstance().drawNameShort(holder.items.name, holder.avatar, holder.nameShortFormTV);
                            }
                        });
            } else {
                GameService.getInstance().drawNameShort(holder.items.name, holder.avatar, holder.nameShortFormTV);
            }

            holder.ranking.setText("" + (position+1));

            if(position == 0){
                holder.rankingContainer.setBackgroundResource(R.drawable.round_right_rect_shape_blue);
            }
            else if(position == 1){
                holder.rankingContainer.setBackgroundResource(R.drawable.round_right_rect_shape_orange);
            }
            else if(position == 2){
                holder.rankingContainer.setBackgroundResource(R.drawable.round_right_rect_shape_green);
            }

        }
        return view;
    }

    private class ViewHolder{
        public TextView ranking;
        public TextView vote;
        public TextView nameShortFormTV;
        public TextView name;
        public ImageView avatar;
        public LinearLayout rankingContainer;

        public FavoriteItem items;
        public View view;

    }

    private Bitmap drawRectToAvatar(){
        BitmapFactory.Options myOptions = new BitmapFactory.Options();
        myOptions.inDither = true;
        myOptions.inScaled = false;
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important
        myOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.next,myOptions);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(context.getResources().getColor(R.color.dark_grey));

        Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                context.getResources().getDimension(R.dimen.item_height_large),
                context.getResources().getDisplayMetrics());
        Canvas canvas = new Canvas(mutableBitmap);
        canvas.drawRect(0, 0, px, px, paint);
        return mutableBitmap;
    }
}
