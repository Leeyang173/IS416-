package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.models.QuizSet;
import sg.edu.smu.livelabs.mobicom.models.data.IceBreakerFriendsEntity;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.util.Util;
import sg.edu.smu.livelabs.mobicom.views.IceBreakerLeaderBoardView;

/**
 * Created by johnlee on 18/2/16.
 */
public class IceBreakerFriendsListAdapter extends BaseAdapter {

    private QuizSet quiz;
    private Bus bus;
    private IceBreakerLeaderBoardView iceBreakerLeaderBoardView;
    private List<IceBreakerFriendsEntity> friends;
    private int screenWidth;
    private int screenHeight;
    private MainActivity mainActivity;
    private ConnectivityManager cm;

    public IceBreakerFriendsListAdapter(IceBreakerLeaderBoardView iceBreakerLeaderBoardView, List<IceBreakerFriendsEntity> friends, Bus bus,
                                        int screenWidth, int screenHeight, MainActivity mainActivity, ConnectivityManager cm){
        this.friends = friends;
        this.bus = bus;
        this.iceBreakerLeaderBoardView = iceBreakerLeaderBoardView;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.mainActivity = mainActivity;
        this.cm = cm;
    }

    @Override
    public int getCount() {
        return friends.size();
    }

    @Override
    public IceBreakerFriendsEntity getItem(int position) {
        if(friends == null){
            return null;
        }
        return friends.get(position);
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
            LayoutInflater vi = (LayoutInflater)iceBreakerLeaderBoardView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.list_item_ice_breaker_friends, parent, false);

            holder.view = view;

            view.setTag(holder);
        }
        else{
            holder = (ViewHolder) view.getTag();
        }

//        if (view == null) {
//            LayoutInflater vi = (LayoutInflater)iceBreakerLeaderBoardView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            view = vi.inflate(R.layout.list_item_ice_breaker_friends, parent, false);

            // set value
        holder.nameShortFormTV = (TextView) view.findViewById(R.id.name_short_form);
        holder.nameTV = (TextView) view.findViewById(R.id.name);
        holder.companyTV = (TextView) view.findViewById(R.id.company);
        holder.avatarIV = (CircleImageView) view.findViewById(R.id.avatar_image);
        holder.addToPhoneBtn = (Button)view.findViewById(R.id.add_to_phone);
        Picasso.with(iceBreakerLeaderBoardView.getContext()).cancelRequest(holder.avatarIV);
        final IceBreakerFriendsEntity friend = friends.get(position);


        if(friend.getAvatarId() != null && !friend.getAvatarId().isEmpty()) {
            holder.nameShortFormTV.setVisibility(View.GONE);
            try {
                Picasso.with(iceBreakerLeaderBoardView.getContext()).
                        load(Util.getPhotoUrlFromId(friend.getAvatarId(), 96))
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .placeholder(R.drawable.empty_profile).into(holder.avatarIV, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        holder.nameShortFormTV.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() { //when failed to load, draw a rect on top of the noavatar and add the name short form
                        GameService.getInstance().drawNameShort(friend.getName(), holder.avatarIV, holder.nameShortFormTV);
                    }
                });
            }
            catch (Throwable e){
                Log.d("AAA", "OOM:IceBreakerFriendsListAdapter:" + e.toString());
            }
        }
        else{
            GameService.getInstance().drawNameShort(friend.getName(), holder.avatarIV, holder.nameShortFormTV);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GameService.getInstance().showDialogIceBreaker(friend.getUserId(),
                        friend.getName(), friend.getAvatarId(), iceBreakerLeaderBoardView.getContext(),
                        cm, mainActivity);
            }
        });


        holder.nameTV.setText(friend.getName());
        holder.companyTV.setText(friend.getDesig());

        holder.addToPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(friend); //popup
            }
        });

//        }

        return view;
    }

    private class ViewHolder{
        public CircleImageView avatarIV;
        public TextView companyTV;
        public TextView nameTV;
        public TextView nameShortFormTV;
        public Button addToPhoneBtn;
        View view;
    }

    private Bitmap drawRectToAvatar(){
        BitmapFactory.Options myOptions = new BitmapFactory.Options();
        myOptions.inDither = true;
        myOptions.inScaled = false;
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important
        myOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeResource(iceBreakerLeaderBoardView.getContext().getResources(), R.drawable.next,myOptions);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(iceBreakerLeaderBoardView.getContext().getResources().getColor(R.color.avatar_grey));

        Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                iceBreakerLeaderBoardView.getContext().getResources().getDimension(R.dimen.item_height),
                iceBreakerLeaderBoardView.getContext().getResources().getDisplayMetrics());
        Canvas canvas = new Canvas(mutableBitmap);
        canvas.drawRect(0, 0, px, px, paint);
        return mutableBitmap;
    }


    public void updates(List<IceBreakerFriendsEntity> friends){
        this.friends = friends;
        this.notifyDataSetChanged();
        this.notifyDataSetInvalidated();
    }

    //this function open the phone contact
    private void showDialog(IceBreakerFriendsEntity friend){

        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.NAME, friend.getName());
        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, friend.getEmailId())
                .putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK);
        iceBreakerLeaderBoardView.getContext().startActivity(intent);

    }

}
