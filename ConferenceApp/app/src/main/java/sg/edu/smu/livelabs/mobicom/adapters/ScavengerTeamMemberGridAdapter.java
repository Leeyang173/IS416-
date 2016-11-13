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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.models.TeamMember;
import sg.edu.smu.livelabs.mobicom.presenters.ScavengerHuntDetailPresenter;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.util.Util;

/**
 * Created by johnlee on 18/2/16.
 */
public class ScavengerTeamMemberGridAdapter extends BaseAdapter {

    private Context c;
    private List<TeamMember> teamMember;
    private int maxTeamMember;
    private boolean isMember;
    private boolean isHuntStarted;
    private ScavengerHuntDetailPresenter.BtnClickListener mClickListener = null;

    public ScavengerTeamMemberGridAdapter(Context c, List<TeamMember> teamMember,  int maxTeamMember, boolean isMember, boolean isHuntStarted
            , ScavengerHuntDetailPresenter.BtnClickListener listener){
        this.c = c;
        this.teamMember = teamMember;
        this.maxTeamMember = maxTeamMember;
        this.mClickListener = listener;
        this.isMember = isMember;
        this.isHuntStarted = isHuntStarted;
    }

    @Override
    public int getCount() {
        return teamMember.size();
    }

    @Override
    public TeamMember getItem(int position) {
        return teamMember == null ? null : teamMember.get(position);
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
        return true;
    }

    @Override
    public boolean isEnabled(int position)
    {
        return true;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = vi.inflate(R.layout.grid_item_scavenger_team_member, parent, false);

            final ImageView avatar = (ImageView) view.findViewById(R.id.avatar_image);
            TextView nameTV = (TextView) view.findViewById(R.id.name);
            final TextView nameShortFormTV = (TextView) view.findViewById(R.id.name_short_form);
            Button removeBtn = (Button) view.findViewById(R.id.remove);
            Button addBtn = (Button) view.findViewById(R.id.add);
            removeBtn.setTag(position);
            addBtn.setTag(position);


            final TeamMember member = teamMember.get(position);

            if(member != null){ //for current user or added user


                if(member.getUser().getAvatar() != null && !member.getUser().getAvatar().isEmpty()){
                    try {
                        Picasso.with(c).load(Util.getPhotoUrlFromId(member.getUser().getAvatar(), 96))
                                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                .networkPolicy(NetworkPolicy.NO_CACHE)
                                .placeholder(R.drawable.icon_no_profile).into(avatar, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                                nameShortFormTV.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {
                                GameService.getInstance().drawNameShort(member.getUser().getName(), avatar, nameShortFormTV);
                            }
                        });
                        nameShortFormTV.setVisibility(View.GONE);
                    }
                    catch(Throwable e){
                        GameService.getInstance().drawNameShort(member.getUser().getName(), avatar, nameShortFormTV);
                        Log.d("AAA", "ScavengerTeamMemberGridAdapter:" + e.toString());
                    }
                }
                else{
                    GameService.getInstance().drawNameShort(member.getUser().getName(), avatar, nameShortFormTV);
                }

                nameTV.setText(member.getUser().getName());
                nameTV.setMaxLines(2);

                //only not owner can remove
                if(getItemViewType(position) != 0) {
                    addBtn.setVisibility(View.GONE);
                    removeBtn.setVisibility(View.VISIBLE);

                    if(isHuntStarted){
                        removeBtn.setBackgroundResource(R.drawable.custom_button_grey);
                        removeBtn.setEnabled(false);
                    }
                    else{
                        removeBtn.setBackgroundResource(R.drawable.custom_button);
                        removeBtn.setEnabled(true);
                    }
                }

                if(getItemViewType(position) == 0){
                    addBtn.setVisibility(View.GONE);
//                    removeBtn.setVisibility(View.GONE);
                }

            }
            else{
                addBtn.setVisibility(View.VISIBLE);
//                removeBtn.setVisibility(View.GONE);
            }

            removeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mClickListener != null)
                        mClickListener.onBtnClick((Integer) v.getTag());
                }
            });


            addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mClickListener != null)
                        mClickListener.onAddBtnClick((Integer) v.getTag());
                }
            });

            if(isMember){
                removeBtn.setVisibility(View.GONE);
                addBtn.setVisibility(View.GONE);
            }
        }

        return view;
    }

    //update new team members
    public void updates(List<TeamMember> teamMember){
        this.teamMember = teamMember;
        this.notifyDataSetChanged();
    }

    public void addMember(TeamMember member, int position){
        this.teamMember.remove(position);
        this.teamMember.add(position, member);
        this.notifyDataSetChanged();
        this.notifyDataSetInvalidated();
    }

    public long removeMember(int position){
        long oldMemberUserId = this.teamMember.get(position).getUser().getUID();
        this.teamMember.remove(position);
        this.teamMember.add(position, null);
        this.notifyDataSetChanged();
        this.notifyDataSetInvalidated();
        return oldMemberUserId;
    }

    public void disband(){
        for(int i=0; i< teamMember.size(); i++){
            if(i != 0 ){
                teamMember.remove(i);
                teamMember.add(i, null);
            }
        }
    }

    public List<TeamMember> getTeamMember() {
        return teamMember;
    }

    public int getNumberOfMember(){
        int count = 0;
        for(TeamMember member: teamMember){
            if(member != null){
                count++;
            }
        }

        return count;
    }

    public int getMaxTeamMember() {
        return maxTeamMember;
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
