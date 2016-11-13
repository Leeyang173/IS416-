package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.ToolTipsWindow;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.net.item.BingoItem;
import sg.edu.smu.livelabs.mobicom.net.item.FavoriteItem;
import sg.edu.smu.livelabs.mobicom.presenters.BingoPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.FavoriteItemPresenter;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.util.Util;
import sg.edu.smu.livelabs.mobicom.views.FavoriteItemView;

/**
 * Created by johnlee on 18/2/16.
 */
public class BingoGridAdapter extends BaseAdapter {

    private Context c;
    private List<BingoItem> bingoItems;
    private BingoPresenter.onClickListener listener;
    private int screenWidth;

    public BingoGridAdapter(Context c, List<BingoItem> bingoItems, int screenWidth, BingoPresenter.onClickListener listener){
        this.c = c;
        this.bingoItems = bingoItems;
        this.listener = listener;
        this.screenWidth = screenWidth;
    }

    @Override
    public int getCount() {
        return bingoItems == null? 0:bingoItems.size();
    }

    @Override
    public BingoItem getItem(int position) {
        return bingoItems == null? null:bingoItems.get(position);
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
    public boolean areAllItemsEnabled()
    {
        return false;
    }

    @Override
    public boolean isEnabled(int position)
    {
        return false;
    }

    public List<BingoItem> getBingoItems() {
        return bingoItems;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = vi.inflate(R.layout.grid_item_bingo, parent, false);
            holder = new ViewHolder();

            holder.layout = (RelativeLayout)convertView.findViewById(R.id.content_layout);
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            holder.keyword = (TextView) convertView.findViewById(R.id.keyword);
            holder.overlay = (View)convertView.findViewById(R.id.overlay);
            Picasso.with(c).cancelRequest(holder.image);

            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams((int)(screenWidth /5), (int)(screenWidth /5));
            holder.layout.setLayoutParams(layout);
            holder.layout.setPadding(4,4,4,4);
            holder.image.setLayoutParams(layout);

            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onBtnClick(getItemViewType(position));
                }
            });

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.bingoItem = bingoItems.get(getItemViewType(position));

        holder.keyword.setText(holder.bingoItem.text);

        if(holder.bingoItem.imageId != null && !holder.bingoItem.imageId.isEmpty()){
            try {
                Picasso.with(c).load(Util.getPhotoUrlFromId(holder.bingoItem.imageId, 96))
                        .resize(96,96)
                        .centerCrop()
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .networkPolicy(NetworkPolicy.NO_CACHE).into(holder.image, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.keyword.setVisibility(View.GONE);
                        holder.overlay.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        Picasso.with(c).load(R.drawable.placeholder)
                                .resize(96,96)
                                .centerCrop()
                                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                .networkPolicy(NetworkPolicy.NO_CACHE).into(holder.image);
                    }
                });
            }
            catch (OutOfMemoryError e){
            }
        }

        return convertView;
    }



    public static class ViewHolder {
        RelativeLayout layout;
        ImageView image;
        TextView keyword;
        View overlay;
        public View view;
        public BingoItem bingoItem;
    }


    public void updates(List<BingoItem> bingoItems){
        this.bingoItems = bingoItems;
        this.notifyDataSetChanged();
    }

}
