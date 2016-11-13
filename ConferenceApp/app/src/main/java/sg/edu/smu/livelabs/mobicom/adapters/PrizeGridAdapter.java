package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.models.Prize;

/**
 * Created by johnlee on 18/2/16.
 */
public class PrizeGridAdapter extends BaseAdapter {

    private Context c;
    private List<Prize> prizes;
    private int currentPoints;
    private float dpWidth;

    public PrizeGridAdapter(Context c, List<Prize> prizes, int currentPoints, float dpWidth){
        this.c = c;
        this.prizes = prizes;
        this.currentPoints = currentPoints;
        this.dpWidth = dpWidth;
    }

    @Override
    public int getCount() {
        return prizes.size();
    }

    @Override
    public Object getItem(int position) {
        return prizes.get(position);
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

            view = vi.inflate(R.layout.grid_item_prizes, parent, false);


            TextView pointsTV = (TextView) view.findViewById(R.id.points);
            ImageView prizeIV = (ImageView) view.findViewById(R.id.prize_image);

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

            Picasso.with(c).load(prizes.get(position).getPrizeImageURL()).resize((int)(dpWidth / (float)3), 0).into(prizeIV);
            pointsTV.setText(prizes.get(position).getPoints() + " points");

            //set background to white rounded corner shape
            if(currentPoints >= prizes.get(position).getPoints()){
                LinearLayout prizeItemLL = (LinearLayout)view.findViewById(R.id.prize_item_layout);
                prizeItemLL.setBackgroundResource(R.drawable.round_rect_shape);
            }
        }

        return view;
    }


    public void updates(List<Prize> prizes){
        this.prizes = prizes;
        this.notifyDataSetChanged();
        this.notifyDataSetInvalidated();
    }
}
