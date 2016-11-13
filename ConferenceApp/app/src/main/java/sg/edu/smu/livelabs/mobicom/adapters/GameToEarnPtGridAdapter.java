package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.models.GameToEarnPoint;

/**
 * Created by johnlee on 18/2/16.
 */
public class GameToEarnPtGridAdapter extends BaseAdapter {

    private Context c;
    private List<GameToEarnPoint> gameToEarnPoints;
    private float dpWidth;

    public GameToEarnPtGridAdapter(Context c, List<GameToEarnPoint> gameToEarnPoints, float dpWidth){
        this.c = c;
        this.gameToEarnPoints = gameToEarnPoints;
        this.dpWidth = dpWidth;
    }

    @Override
    public int getCount() {
        return gameToEarnPoints.size();
    }

    @Override
    public Object getItem(int position) {
        return gameToEarnPoints.get(position);
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

        if (view == null) {
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = vi.inflate(R.layout.grid_item_games, parent, false);


            TextView gamesTV = (TextView) view.findViewById(R.id.game_title);
            ImageView gamesIV = (ImageView) view.findViewById(R.id.game_icon);

            Transformation transformation = new Transformation() {

                @Override
                public Bitmap transform(Bitmap source) {
                    int targetWidth = (int)(dpWidth / (float)3);

                    double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
                    int targetHeight = (int) (targetWidth * aspectRatio);
                    Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
                    if (result != source) {
                        // Same bitmap is returned if sizes are the same
                        source.recycle();
                    }
                    return result;
                }

                @Override
                public String key() {
                    return "transformation" + " desiredWidth";
                }
            };

            int iconResource = 0;

            if(position == 0){
                iconResource = R.drawable.profile;
            }
            else if(position == 1){
                iconResource = R.drawable.like;
            }
            else if(position == 2){
                iconResource = R.drawable.chat;
            }
            else if(position == 3){
                iconResource = R.drawable.plus;
            }
            else if(position == 4){
                iconResource = R.drawable.quiz;
            }
            else if(position == 5){
                iconResource = R.drawable.surveys;
            }

            Picasso.with(c).load(iconResource)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .resize((int) (dpWidth / (float) 3), 0).into(gamesIV);
            gamesTV.setText(gameToEarnPoints.get(position).getTitle());


        }

        return view;
    }


    public void updates(List<GameToEarnPoint> gameToEarnPoints){
        this.gameToEarnPoints = gameToEarnPoints;
        this.notifyDataSetChanged();
        this.notifyDataSetInvalidated();
    }
}
