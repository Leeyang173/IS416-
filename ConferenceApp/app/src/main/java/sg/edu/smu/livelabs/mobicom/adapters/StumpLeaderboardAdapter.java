package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.net.item.SponsorItem;
import sg.edu.smu.livelabs.mobicom.net.item.StumpLeaderboardItem;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.util.Util;

/**
 * Created by johnlee on 18/2/16.
 */
public class StumpLeaderboardAdapter extends BaseAdapter {

    private Bus bus;
    private Context c;
    private List<StumpLeaderboardItem> leaderboard;
    private int userPosition;
    private boolean showLeaderboard;
    private List<SponsorItem> sponsors;

    public StumpLeaderboardAdapter(Context c, List<StumpLeaderboardItem> leaderboard,
                                   List<SponsorItem> sponsors, Bus bus, int myPosition, boolean showLeaderboard){
        this.leaderboard = leaderboard;
        this.bus = bus;
        this.c = c;
        this.userPosition = myPosition;
        this.showLeaderboard = showLeaderboard;
        this.sponsors = sponsors;
//        sponsors.add(new Sponsers(R.drawable.game_coolfie, "xxxx"));
//        sponsors.add(new Sponsers(R.drawable.game_demo, "asdas"));
//        sponsors.add(new Sponsers(R.drawable.game_icebreaker, "asdas"));
//        sponsors.add(new Sponsers(R.drawable.game_coolfie, "xx21321xx"));
//        sponsors.add(new Sponsers(R.drawable.game_coolfie, "wdq"));
//        sponsors.add(new Sponsers(R.drawable.game_coolfie, "wqeqe qeq"));
//        sponsors.add(new Sponsers(R.drawable.game_coolfie, "qew123123 12313"));
//        sponsors.add(new Sponsers(R.drawable.game_coolfie, "xxasdas adaxx"));
//        sponsors.add(new Sponsers(R.drawable.game_coolfie, "xxxx"));
//        sponsors.add(new Sponsers(R.drawable.game_demo, "asdas"));
//        sponsors.add(new Sponsers(R.drawable.game_icebreaker, "asdas"));
//        sponsors.add(new Sponsers(R.drawable.game_coolfie, "xx21321xx"));
//        sponsors.add(new Sponsers(R.drawable.game_coolfie, "xxxx"));
//        sponsors.add(new Sponsers(R.drawable.game_demo, "asdas"));
//        sponsors.add(new Sponsers(R.drawable.game_icebreaker, "asdas"));
//        sponsors.add(new Sponsers(R.drawable.game_coolfie, "xx21321xx"));
    }

    @Override
    public int getCount() {
        if(showLeaderboard) {
            return leaderboard.size();
        }
        else{
            return sponsors.size();
        }
    }

    @Override
    public Object getItem(int position) {
        if(showLeaderboard) {
            if (leaderboard == null) {
                return null;
            }
            return leaderboard.get(position);
        }
        else{
            if (sponsors == null) {
                return null;
            }
            return sponsors.get(position);
        }
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


        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(showLeaderboard)
                view = vi.inflate(R.layout.list_item_stump_leaderboard, parent, false);
            else
                view = vi.inflate(R.layout.list_item_stump_sponser_board, parent, false);

            holder.view = view;
            view.setTag(holder);
        }
        else{
            holder = (ViewHolder) view.getTag();
        }

        if(showLeaderboard){
            holder.person = leaderboard.get(position);

            // set value
            holder.nameShortFormTV = (TextView) view.findViewById(R.id.name_short_form);
            holder.nameTV = (TextView) view.findViewById(R.id.name);
            holder.scoreTV = (TextView) view.findViewById(R.id.score);
            holder.avatarIV = (ImageView) view.findViewById(R.id.avatar_image);

            holder.nameTV.setText(holder.person.name);
            holder.scoreTV.setText("" + holder.person.score);
            Picasso.with(c).cancelRequest(holder.avatarIV);
            boolean isBigTmp = false;
            if(position == userPosition){
                isBigTmp = true;
            }

            final boolean isBig = isBigTmp;
            final int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    c.getResources().getDimension(R.dimen.item_height_small),
                    c.getResources().getDisplayMetrics());

            if(holder.person.avatar != null && !holder.person.avatar.isEmpty()){
                holder.nameShortFormTV.setVisibility(View.GONE);
                try {
                    Picasso.with(c).
                            load(Util.getPhotoUrlFromId(holder.person.avatar, 96))
                            .resize((int) c.getResources().getDimension(R.dimen.item_height_small),
                                    (int) c.getResources().getDimension(R.dimen.item_height_small))
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .placeholder(R.drawable.empty_profile).into(holder.avatarIV, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            if (isBig) {
                                RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams((int) c.getResources().getDimension(R.dimen.item_height_large),
                                        (int) c.getResources().getDimension(R.dimen.item_height_large));
                                holder.avatarIV.setLayoutParams(layout);
                            }
                            holder.nameShortFormTV.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() { //when failed to load, draw a rect on top of the noavatar and add the name short form

                            GameService.getInstance().drawNameShort(holder.person.name, holder.avatarIV, holder.nameShortFormTV);
                            if (isBig) {

                                RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams((int) c.getResources().getDimension(R.dimen.item_height_large)
                                        , (int) c.getResources().getDimension(R.dimen.item_height_large));
                                holder.avatarIV.setLayoutParams(layout);
                            }
                        }
                    });
                }
                catch(Throwable e){
                    Log.d("AAA", "StumpLeaderboardAdapter" + e.toString());
                }
            }
            else{

                GameService.getInstance().drawNameShort(holder.person.name, holder.avatarIV, holder.nameShortFormTV);
                if(isBig) {
                    RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams((int)c.getResources().getDimension(R.dimen.item_height_large),
                            (int)c.getResources().getDimension(R.dimen.item_height_large));
                    holder.avatarIV.setLayoutParams(layout);
                }
            }
        }
        else{
//            holder.nameTV = (TextView) view.findViewById(R.id.name);
            holder.sponsorIV = (ImageView) view.findViewById(R.id.image);

            if(sponsors != null) {
                holder.sponsors = sponsors.get(position);
//                holder.nameTV.setText(holder.sponsors.name);
                if(getItemViewType(position) == 0){
                    Picasso.with(c).
                            load(holder.sponsors.defaultImage) //load from resource
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .placeholder(R.drawable.default_game).into(holder.sponsorIV);
                }
                else{
                    Picasso.with(c).
                            load(holder.sponsors.image)
                            .resize((int) c.getResources().getDimension(R.dimen.item_height),
                                    (int) c.getResources().getDimension(R.dimen.item_height))
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .placeholder(R.drawable.default_game).into(holder.sponsorIV);
                }

            }

        }

        return view;
    }

    public int getUserPosition(){
        return userPosition;
    }

    public void updates(List<StumpLeaderboardItem> leaderboard, int myPosition){
        this.leaderboard.clear();
        this.leaderboard = leaderboard;
        this.userPosition = myPosition;
        this.notifyDataSetChanged();
        this.notifyDataSetInvalidated();
    }

    private class ViewHolder{
        public ImageView avatarIV;
        public TextView scoreTV;
        public TextView nameTV;
        public TextView nameShortFormTV;

        public ImageView sponsorIV;
        public StumpLeaderboardItem person;
        public SponsorItem sponsors;
        View view;
    }


    private Bitmap drawRectToAvatar(){
        BitmapFactory.Options myOptions = new BitmapFactory.Options();
        myOptions.inDither = true;
        myOptions.inScaled = false;
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important
        myOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeResource(c.getResources(), R.drawable.next,myOptions);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(c.getResources().getColor(R.color.avatar_grey));

        Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                c.getResources().getDimension(R.dimen.item_height),
                c.getResources().getDisplayMetrics());
        Canvas canvas = new Canvas(mutableBitmap);
        canvas.drawRect(0, 0, px, px, paint);

        return mutableBitmap;
    }

}
