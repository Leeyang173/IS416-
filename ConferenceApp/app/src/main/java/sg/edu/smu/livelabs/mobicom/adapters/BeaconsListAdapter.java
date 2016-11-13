package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.models.BeaconOccurrences;
import sg.edu.smu.livelabs.mobicom.models.data.BeaconEntity;
import sg.edu.smu.livelabs.mobicom.services.BeaconsService;
import sg.edu.smu.livelabs.mobicom.views.BeaconView;

/**
 * Created by johnlee on 18/2/16.
 */
public class BeaconsListAdapter extends BaseAdapter {

    private List<BeaconOccurrences> beaconOccurrences;
    private Bus bus;
    private BeaconView beaconView;
    private List<BeaconEntity> beaconEntities;
    private int numberOfNearByBeacons;
    private ViewHolder holder;

    //beaconOccurrences will be the top 3 nearest beacons
    public BeaconsListAdapter(BeaconView beaconView,  Bus bus, List<BeaconEntity> beaconEntities){
        this.beaconView = beaconView;
        this.beaconOccurrences = new ArrayList<>();
        this.bus = bus;
        this.numberOfNearByBeacons = 0;
        this.beaconEntities = beaconEntities;
    }

    @Override
    public int getCount() {
        return beaconEntities.size();
    }

    @Override
    public BeaconEntity getItem(int position) {
        return beaconEntities.get(position);
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

        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            LayoutInflater vi = (LayoutInflater)beaconView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.list_item_beacon, parent, false);

            holder.view = view;
            holder.star1 = (ImageView) view.findViewById(R.id.star1);
            holder.star2 = (ImageView) view.findViewById(R.id.star2);
            holder.star3 = (ImageView) view.findViewById(R.id.star3);
            holder.star4 = (ImageView) view.findViewById(R.id.star4);
            holder.star5 = (ImageView) view.findViewById(R.id.star5);
            holder.init();
            view.setTag(holder);
        }
        else{
            holder = (ViewHolder) view.getTag();
        }

        holder.titleLL = (LinearLayout) view.findViewById(R.id.title_container);
        holder.titleTV = (TextView) view.findViewById(R.id.title);
        holder.nameTV = (TextView) view.findViewById(R.id.beacon_name);
        holder.avgRating = (TextView) view.findViewById(R.id.avg_rating);
        holder.imageIV = (ImageView) view.findViewById(R.id.arrow);
        holder.b = beaconEntities.get(position);
//        if(beaconEntities.get(position).getPaperName().length() > 10 ){
            SpannableString text = new SpannableString(beaconEntities.get(position).getPaperName());// + " " + beaconEntities.get(position).getMajor());
            text.setSpan(new AbsoluteSizeSpan(beaconView.getContext().getResources().getDimensionPixelSize(R.dimen.text_size_super_large)), 0, beaconEntities.get(position).getCapChar(), //10,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setSpan(new ForegroundColorSpan(beaconView.getContext().getResources().getColor(R.color.colorPrimary)), 0, beaconEntities.get(position).getCapChar(),//10,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.nameTV.setText(text, TextView.BufferType.SPANNABLE);
//        }
//        else{
//            holder.nameTV.setText(beaconEntities.get(position).getPaperName() + " " + beaconEntities.get(position).getMajor());
//        }

        holder.avgRating.setText("Avg: " + String.format("%.2f", beaconEntities.get(position).getAvgRating() == null ? 0: beaconEntities.get(position).getAvgRating()));
        holder.setSelectedStar(beaconEntities.get(position).getRate() == null ? 0 : beaconEntities.get(position).getRate());

        holder.titleTV.setPadding(35,2,35,2);
        if(getItemViewType(position) == 0) {
            if(numberOfNearByBeacons == 0){
                holder.titleLL.setVisibility(View.VISIBLE);
                holder.titleTV.setText("All Demos");
            }
            else if(numberOfNearByBeacons > 0){
                holder.titleLL.setVisibility(View.VISIBLE);
                holder.titleTV.setText("Nearby Demos");
            }
            else{
                holder.titleLL.setVisibility(View.GONE);
            }
        }
        else{
            if(numberOfNearByBeacons > 0 && getItemViewType(position) == numberOfNearByBeacons) {
                holder.titleLL.setVisibility(View.VISIBLE);
                holder.titleTV.setText("All Demos");
            }
            else{
                holder.titleLL.setVisibility(View.GONE);
            }
        }

        int paddingMax = (int) beaconView.getContext().getResources().getDimension(R.dimen.beacon_max_top_padding);
        int paddingMin = (int) beaconView.getContext().getResources().getDimension(R.dimen.beacon_min_top_padding);

        if(numberOfNearByBeacons > 0 && getItemViewType(position) < numberOfNearByBeacons){
            holder.nameTV.setPadding(45,paddingMax,10,paddingMax - 5);
            holder.imageIV.setPadding(15,paddingMax,15,paddingMax - 5);
        }
        else{
            holder.nameTV.setPadding(45,paddingMin,10,paddingMin - 5);
            holder.imageIV.setPadding(15,paddingMin,15,paddingMin - 5);
        }

        return view;
    }


    public void updateBeaconPosition(List<BeaconEntity> beaconEntities, int numberOfNearByBeacons){
        this.beaconEntities = beaconEntities;
        this.numberOfNearByBeacons = numberOfNearByBeacons;
        notifyDataSetChanged();
    }

    public void update(List<BeaconEntity> beaconEntities){
        System.out.println("BeaconRefreshEvent line 3");
        this.beaconEntities.clear();
        this.beaconEntities = beaconEntities;
        notifyDataSetChanged();
    }

    public void update2(List<BeaconEntity> beaconEntities){
        this.beaconEntities = beaconEntities;
        notifyDataSetChanged();
    }

    private class ViewHolder {
        LinearLayout titleLL;
        TextView titleTV;
        TextView nameTV;
        ImageView imageIV;
        View view;
        public ImageView star1;
        public ImageView star2;
        public ImageView star3;
        public ImageView star4;
        public ImageView star5;
        public ImageView[] stars;
        public BeaconEntity b;
        public TextView avgRating;

        public void init(){
            stars = new ImageView[]{star1, star2, star3, star4, star5};

            //have to update the rating based on the star clicked
            star1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateBeaconRating(b, 1);
                    BeaconsService.getInstance().rateBeacon(b, 1);
                }
            });

            star2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateBeaconRating(b, 2);
                    BeaconsService.getInstance().rateBeacon(b, 2);
                }
            });

            star3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateBeaconRating(b, 3);
                    BeaconsService.getInstance().rateBeacon(b, 3);
                }
            });

            star4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateBeaconRating(b, 4);
                    BeaconsService.getInstance().rateBeacon(b, 4);
                }
            });

            star5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateBeaconRating(b, 5);
                    BeaconsService.getInstance().rateBeacon(b, 5);
                }
            });
        }

        /**
         * This helps to record how many star for the rating is/are being selected
         * @param rate
         */
        private void setSelectedStar(int rate){
            if(stars == null){
                init();
            }

            if (rate > 5){
                rate = 5;
            }
            for (int i = 0; i < rate; i++){
                stars[i].setSelected(true);
            }
            for (int i = rate; i < 5; i++){
                stars[i].setSelected(false);
            }
        }

        private void updateBeaconRating(final BeaconEntity b, final int rating){
            BeaconsService.getInstance().updateBeaconRating(b, rating);
            setSelectedStar(rating);
        }
    }

    public List<BeaconEntity> getBeaconEntities() {
        return beaconEntities;
    }
}
