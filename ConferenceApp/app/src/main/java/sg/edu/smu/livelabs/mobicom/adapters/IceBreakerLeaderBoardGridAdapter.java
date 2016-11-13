package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.util.TypedValue;
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

import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.models.data.IceBreakerLeaderBoardEntity;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.util.Util;

/**
 * Created by johnlee on 18/2/16.
 */
public class IceBreakerLeaderBoardGridAdapter extends BaseAdapter {

    private Context c;
    private List<IceBreakerLeaderBoardEntity> icebreakerLeaders;
    private MainActivity mainActivity;
    private ConnectivityManager cm;

    public IceBreakerLeaderBoardGridAdapter(Context c, List<IceBreakerLeaderBoardEntity> icebreakerLeaders
            , MainActivity mainActivity, ConnectivityManager cm){
        this.c = c;
        this.icebreakerLeaders = icebreakerLeaders;
        this.mainActivity = mainActivity;
        this.cm = cm;

    }

    @Override
    public int getCount() {
        return icebreakerLeaders.size();
    }

    @Override
    public IceBreakerLeaderBoardEntity getItem(int position) {
        return icebreakerLeaders.get(position);
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

    public List<IceBreakerLeaderBoardEntity> getLeaders() {
        return icebreakerLeaders;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = vi.inflate(R.layout.grid_item_ice_breaker_leaderboard, parent, false);

            final TextView nameShortFormTV = (TextView) view.findViewById(R.id.name_short_form);
            TextView nameTV = (TextView) view.findViewById(R.id.name);
            TextView countTV = (TextView) view.findViewById(R.id.count);
            final ImageView avatarIV = (ImageView) view.findViewById(R.id.avatar_image);
            View divider = (View) view.findViewById(R.id.divider);

            if(position == 0){
                divider.setVisibility(View.GONE);
            }
            else{
                divider.setVisibility(View.VISIBLE);
            }

            final IceBreakerLeaderBoardEntity icebreakerLeader = icebreakerLeaders.get(position);

            Picasso.with(c).cancelRequest(avatarIV);
            if(icebreakerLeader.getAvatarId() != null  && !icebreakerLeader.getAvatarId().equals("")) {
                nameShortFormTV.setVisibility(View.GONE);
                Picasso.with(c).load(Util.getPhotoUrlFromId(icebreakerLeader.getAvatarId(), 96))
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .into(avatarIV, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                nameShortFormTV.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() { //when failed to load, draw a rect on top of the noavatar and add the name short form
                                GameService.getInstance().drawNameShort(icebreakerLeader.getName(), avatarIV, nameShortFormTV);
                            }
                        });
            }
            else{
                GameService.getInstance().drawNameShort(icebreakerLeader.getName(),avatarIV, nameShortFormTV);
            }

            nameTV.setText(icebreakerLeader.getName());
            countTV.setText("" + icebreakerLeader.getCount());

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GameService.getInstance().showDialogIceBreaker(icebreakerLeader.getUserId(),
                            icebreakerLeader.getName(), icebreakerLeader.getAvatarId(), c,
                            cm, mainActivity);
                }
            });

        }

        return view;
    }

    //update new interests
    public void updates(List<IceBreakerLeaderBoardEntity> icebreakerLeaders){
        this.icebreakerLeaders = icebreakerLeaders;
        this.notifyDataSetChanged();
        this.notifyDataSetInvalidated();
    }


    public class isSelectedBooean{
        public Boolean isSelected;
        public isSelectedBooean(Boolean isSelected){
            this.isSelected = isSelected;
        }
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
                c.getResources().getDimension(R.dimen.item_height_large),
                c.getResources().getDisplayMetrics());
        Canvas canvas = new Canvas(mutableBitmap);
        canvas.drawRect(0, 0, px, px, paint);
        return mutableBitmap;
    }

}
