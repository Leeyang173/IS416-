package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.ToolTipsWindow;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.net.item.FavoriteItem;
import sg.edu.smu.livelabs.mobicom.presenters.FavoriteItemPresenter;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.util.Util;
import sg.edu.smu.livelabs.mobicom.views.FavoriteItemView;

/**
 * Created by johnlee on 18/2/16.
 */
public class FavoriteItemGridAdapter extends BaseAdapter {

    private Context c;
    private List<FavoriteItem> favoriteItems;
    private User me;
    private int totalUser;
    private ToolTipsWindow toolTipsWindow;
    private FavoriteItemView favoriteItemView;
    private int screenWidth;
    private int screenHeight;
    private FavoriteItemPresenter.onVoteClickListener listener;
    private List<String> myVote;

    public FavoriteItemGridAdapter(FavoriteItemView favoriteItemView, List<FavoriteItem> favoriteItems, User me, int totalUser,
                                   int screenWidth, int screenHeight,
                                   String[] myVote, FavoriteItemPresenter.onVoteClickListener listener){
        this.c = favoriteItemView.getContext();
        this.favoriteItems = favoriteItems;
        this.totalUser = totalUser;
        this.me = me;
        this.favoriteItemView = favoriteItemView;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        toolTipsWindow = new ToolTipsWindow(c, "You cannot vote for yourself");
        this.listener = listener;
        this.myVote = new ArrayList<String>();

        for(String id:myVote){
            this.myVote.add(id);
        }
//        this.myVote = myVote;
    }

    @Override
    public int getCount() {
        return favoriteItems == null? 0:favoriteItems.size();
    }

    @Override
    public FavoriteItem getItem(int position) {
        return favoriteItems == null? null:favoriteItems.get(position);
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

    public List<FavoriteItem> getFavoriteList() {
        return favoriteItems;
    }

    public FavoriteItem addLike(int position, View v){
        FavoriteItem f = favoriteItems.get(getItemViewType(position));
        f.count = f.count + 1;
        f.isLiked = true;
        myVote.add(f.id);

        favoriteItems.remove(getItemViewType(position));
        favoriteItems.add(getItemViewType(position), f);
//        notifyDataSetChanged();
        ((ViewHolder) v.getTag()).favorite.isLiked = true;
        ((ViewHolder) v.getTag()).container.setBackgroundResource(R.drawable.round_rect_shape_light_orange);
        return f;
    }

    public FavoriteItem deductLike(int position, View v){
        FavoriteItem f = favoriteItems.get(getItemViewType(position));
        f.count = f.count - 1;
        if(f.count < 0){
            f.count = 0;
        }
        f.isLiked = false;

        for(String id: myVote){
            if(id.equals(f.id)){
                myVote.remove(id);
                break;
            }
        }

        favoriteItems.remove(getItemViewType(position));
        favoriteItems.add(getItemViewType(position), f);
//        notifyDataSetChanged();
        ((ViewHolder) v.getTag()).favorite.isLiked = false;
        ((ViewHolder) v.getTag()).container.setBackgroundResource(R.drawable.round_rect_shape);
        return f;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            final FavoriteItem favorite = favoriteItems.get(getItemViewType(position));

            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = vi.inflate(R.layout.grid_item_favorite, parent, false);
            holder = new ViewHolder();

            holder.container = (LinearLayout) convertView.findViewById(R.id.container);
            holder.avatarIV = (ImageView) convertView.findViewById(R.id.avatar_image);
            holder.nameTV = (TextView) convertView.findViewById(R.id.name);
            holder.nameShortFormTV = (TextView) convertView.findViewById(R.id.name_short_form);
            holder.likesTV = (TextView) convertView.findViewById(R.id.likes);
            Picasso.with(c).cancelRequest(holder.avatarIV);
            String nameShortFormTmp = "";
            String[] tmp = favorite.name.trim().split(" ");
            for(int i =0; i< tmp.length; i++){
                if(i == 2){//only capture the first char of the first 2 word/name
                    break;
                }
                if(tmp[i].length() > 0)
                    nameShortFormTmp += tmp[i].charAt(0);

            }

            holder.nameShortForm = nameShortFormTmp.toUpperCase();
            holder.favorite = favorite;


            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.nameTV.setText(holder.favorite.name);
        holder.likesTV.setText("Votes: " + holder.favorite.count);
//            likesTV.setTextColor(getColorTier(favoriteItems.get(position).count));
//            likesTV.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX,
//                    getFontSizeTier(favoriteItems.get(position).count), c.getResources().getDisplayMetrics()));

        if(holder.favorite.avatar != null && !holder.favorite.avatar.isEmpty()){
            Picasso.with(c).load(Util.getPhotoUrlFromId(holder.favorite.avatar, 96))
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .placeholder(R.drawable.noavatar).into(holder.avatarIV, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() { //when failed to load, draw a rect on top of the noavatar and add the name short form
                    GameService.getInstance().drawNameShort(holder.favorite.name, holder.avatarIV, holder.nameShortFormTV);
                }
            });
        }
        else{
            GameService.getInstance().drawNameShort(holder.favorite.name, holder.avatarIV, holder.nameShortFormTV);
        }

        holder.favorite.isLiked = false;
        for(String id: myVote){
            if(id.trim().equals(holder.favorite.id)){
                holder.favorite.isLiked = true;
                break;
            }
        }

        if(holder.favorite.isLiked){
            holder.container.setBackgroundResource(R.drawable.round_rect_shape_light_orange);
        }
        else{
            holder.container.setBackgroundResource(R.drawable.round_rect_shape);
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!holder.favorite.id.equals(Long.toString(me.getUID()))) { //if it is not the user itself, then he/she can vote (no voting of onesel
                    listener.onBtnClick(getItemViewType(position));
                } else {
                    toolTipsWindow.showToolTip(favoriteItemView, screenWidth, screenHeight);
                }
            }
        });

        return convertView;
    }



    public static class ViewHolder {
        public  LinearLayout container;
        ImageView avatarIV;
        TextView nameTV;
        TextView nameShortFormTV;
        TextView likesTV;
        String nameShortForm;
        public View view;
        public FavoriteItem favorite;
    }


    public void updates(List<FavoriteItem> favoriteItems){
        this.favoriteItems = favoriteItems;
        this.notifyDataSetChanged();
    }

}
