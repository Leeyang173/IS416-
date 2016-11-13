package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.models.data.BadgeEntity;
import sg.edu.smu.livelabs.mobicom.views.CustomProgressView;

/**
 * Created by johnlee on 18/2/16.
 */
public class BadgesListAdapter extends BaseAdapter {

    private static int MAX_TRY = 3;
    private Bus bus;
    private Context c;
    private List<BadgeEntity> badges;
    private ViewHolder holder;
    private List<Integer> colors;

    public BadgesListAdapter(Context c, Bus bus, List<BadgeEntity> badges){
        this.c = c;
        this.bus = bus;
        this.badges = badges;
        colors = new ArrayList<>();
        colors.add(c.getResources().getColor(R.color.blue));
        colors.add(c.getResources().getColor(R.color.wisteria));
        colors.add(c.getResources().getColor(R.color.pomegranate));
        colors.add(c.getResources().getColor(R.color.emerald));
        colors.add(c.getResources().getColor(R.color.carrot));
        colors.add(c.getResources().getColor(R.color.colorPrimary));
        colors.add(c.getResources().getColor(R.color.red));
        colors.add(c.getResources().getColor(R.color.aqua));
        colors.add(c.getResources().getColor(R.color.teal));
        colors.add(c.getResources().getColor(R.color.oliver));
        colors.add(c.getResources().getColor(R.color.lime));
        colors.add(c.getResources().getColor(R.color.yellow));
        colors.add(c.getResources().getColor(R.color.navy));
        colors.add(c.getResources().getColor(R.color.light_blue));
        colors.add(c.getResources().getColor(R.color.chartreuse));
        colors.add(c.getResources().getColor(R.color.chocolate));
        colors.add(c.getResources().getColor(R.color.coral));
        colors.add(c.getResources().getColor(R.color.darkcyan));
        colors.add(c.getResources().getColor(R.color.deeppink));
        colors.add(c.getResources().getColor(R.color.hotpink));
        colors.add(c.getResources().getColor(R.color.indianred));
        colors.add(c.getResources().getColor(R.color.plum));

    }

    @Override
    public int getCount() {
        return badges.size();
    }

    @Override
    public BadgeEntity getItem(int position) {
        return badges == null ? null: badges.get(position);
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
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        BadgeEntity badge = badges.get(position);
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.list_item_badge_achievement, parent, false);

            holder.view = view;
            holder.color = getRandomColor(position);
            view.setTag(holder);

            holder.mDrawable = c.getResources().getDrawable(R.drawable.star);
//            holder.sourceBitmap = convertDrawableToBitmap(holder.mDrawable);

            holder.mDrawable2 = c.getResources().getDrawable(R.drawable.star_filled);
//            holder.sourceBitmap2 = convertDrawableToBitmap(holder.mDrawable2);

            holder.mDrawableTrophy = c.getResources().getDrawable(R.drawable.trophy_white);
            holder.mDrawableTrophy2 = c.getResources().getDrawable(R.drawable.trophy_white_fill);

            holder.badgeIcon = (ImageView) view.findViewById(R.id.badge_icon);
            holder.progressContainer = (LinearLayout) view.findViewById(R.id.progress_container);
            holder.titleTV = (TextView) view.findViewById(R.id.title);
            holder.desTV = (TextView) view.findViewById(R.id.description);
            holder.starOne = (ImageView) view.findViewById(R.id.star_one);
            holder.starTwo = (ImageView) view.findViewById(R.id.star_two);
            holder.starThree = (ImageView) view.findViewById(R.id.star_three);
            holder.trophy = (ImageView) view.findViewById(R.id.trophy);
            holder.progressBar = new CustomProgressView(c, holder.color, c.getResources().getColor(R.color.transparent));
            holder.progressBar.setBackgroundResource(R.drawable.custom_border);
            holder.progressBar.setLayoutParams(holder.getLayout());
            holder.progressContainer.addView(holder.progressBar);
            holder.progressBar.setBackground(holder.drawBorder());

            if(badge.getImageId() != null && !badge.getImageId().isEmpty()){
                try {
                    Picasso.with(c).load(badge.getImageId()).resize(48, 48).placeholder(R.drawable.default_badge).into(holder.badgeIcon);
                }
                catch (Throwable e){
                    Log.d("AAA", "BadgeListAdapater:" + e.toString());
                }
            }

            holder.starOne.setColorFilter(holder.color);
            holder.starTwo.setColorFilter(holder.color);
            holder.starThree.setColorFilter(holder.color);
            holder.trophy.setColorFilter(holder.color);

            if(badge.getMax() == 1 && badge.getBadgesType() == 2){ //for special badge
                holder.starOne.setVisibility(View.GONE);
                holder.starTwo.setVisibility(View.GONE);
                holder.starThree.setVisibility(View.GONE);
                holder.trophy.setVisibility(View.VISIBLE);
                if(badge.getCountAchieved() == 1){
                    holder.trophy.setImageDrawable(holder.mDrawableTrophy2);
                }
                else{
                    holder.trophy.setImageDrawable(holder.mDrawableTrophy);
                }
            }
            else if(badge.getMax() == 1 && badge.getBadgesType() == 1){ //for normal badge with only 1 star (profile badge)
                holder.starOne.setVisibility(View.GONE);
                holder.starTwo.setVisibility(View.GONE);
                holder.starThree.setVisibility(View.VISIBLE);
                holder.trophy.setVisibility(View.GONE);
            }

            holder.titleTV.setText(badge.getBadges());
            holder.desTV.setText(badge.getDescription());
            holder.desTV.setMaxLines(2);
            holder.desTV.setEllipsize(TextUtils.TruncateAt.END);

            holder.progressBar.setMax(badge.getMax());


        }
        else{
            holder = (ViewHolder) view.getTag();
        }

        //this area highlight the star (filled star) depending on the count
        if(badge.getCountAchieved() == 0){
            holder.starOne.setImageDrawable(holder.mDrawable);
            holder.starTwo.setImageDrawable(holder.mDrawable);
            holder.starThree.setImageDrawable(holder.mDrawable);
        }
        else if(badge.getCountAchieved() == 1){ //this part is tricky, we need to show 2 type, 1 star for profile and normal badge 3-4 stars
            if(badge.getMax() == 1 && badge.getBadgesType() == 1){
                holder.starThree.setImageDrawable(holder.mDrawable2);
            }
            else {
                holder.starOne.setImageDrawable(holder.mDrawable2);
                holder.starTwo.setImageDrawable(holder.mDrawable);
                holder.starThree.setImageDrawable(holder.mDrawable);
            }
        }
        else if(badge.getCountAchieved() == 2) {
            holder.starOne.setImageDrawable(holder.mDrawable2);
            holder.starTwo.setImageDrawable(holder.mDrawable2);
            holder.starThree.setImageDrawable(holder.mDrawable);
        }
        else if(badge.getCountAchieved() == 3){
            holder.starOne.setImageDrawable(holder.mDrawable2);
            holder.starTwo.setImageDrawable(holder.mDrawable2);
            holder.starThree.setImageDrawable(holder.mDrawable2);
        }
        else if(badge.getCountAchieved() == 4){
            holder.starOne.setVisibility(View.GONE);
            holder.starTwo.setVisibility(View.GONE);
            holder.starThree.setVisibility(View.GONE);
            holder.trophy.setVisibility(View.VISIBLE);
            holder.trophy.setImageDrawable(holder.mDrawableTrophy2);
        }



        holder.progressBar.setProgress(badge.getCountAchieved());

        return view;
    }


    public void update(List<BadgeEntity> badges){
        this.badges.clear();
        this.badges.addAll(badges);
        notifyDataSetChanged();
        notifyDataSetInvalidated();
    }



    private class ViewHolder{
        ImageView badgeIcon;
        TextView titleTV;
        TextView desTV;
        ImageView starOne;
        ImageView starTwo;
        ImageView starThree;
        ImageView trophy;
        CustomProgressView progressBar;
        LinearLayout progressContainer;

        Bitmap sourceBitmapTrophy;
        Bitmap sourceBitmap2;
        Bitmap sourceBitmap;

        Bitmap sourceBitmap2Tmp;
        Bitmap sourceBitmapTmp;

        Drawable mDrawable;
        Drawable mDrawable2;
        Drawable mDrawableTrophy;
        Drawable mDrawableTrophy2;
        int color;
        View view;

        public LinearLayout.LayoutParams getLayout(){
            LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            layout.topMargin = 15;
            return layout;
        }

        public GradientDrawable drawBorder(){
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(Color.TRANSPARENT); // Changes this drawbale to use a single color instead of a gradient
            gd.setCornerRadius(0);
            gd.setStroke(2, color);
            return  gd;
        }
    }

    public int getRandomColor(int position){
//        Random r = new Random();
//        int i = r.nextInt(colors.size());
        int pos = position;
        if(pos >= colors.size()){
            pos -= colors.size();
        }
        return colors.get(pos);
    }

    public static Bitmap changeImageColor(Bitmap sourceBitmap, int color) {
        Bitmap resultBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0,
                sourceBitmap.getWidth() - 1,  sourceBitmap.getHeight() - 1);
        Paint p = new Paint();
        ColorFilter filter = new LightingColorFilter(color, 1);
        p.setColorFilter(filter);
        p.setColor(color);

        Canvas canvas = new Canvas(resultBitmap);

        canvas.drawBitmap(resultBitmap, 0, 0, p);

        return resultBitmap;
    }


    public static Drawable covertBitmapToDrawable(Context context, Bitmap bitmap) {
        Drawable d = new BitmapDrawable(context.getResources(), bitmap);
        return d;
    }

    public static Bitmap convertDrawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

        return bitmap;
    }

}
