package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.models.data.InterestsEntity;

/**
 * Created by johnlee on 18/2/16.
 */
public class InterestGridAdapter extends BaseAdapter {

    private Context c;
    private List<InterestsEntity> interest;
    private Boolean isAttendee;
    private String[] userInterests;
    private List<String> userInterestsList;
    private List<isSelectedBooean> isSelected;
    private float scale;

    public InterestGridAdapter(Context c, List<InterestsEntity> interest,  Boolean isAttendee, String[] userInterests,
                               float scale){
        this.c = c;
        this.interest = interest;
        this.scale =scale;
        //added a list empty item => for edit button

        userInterestsList = new ArrayList<>();
        if(userInterests == null){
            userInterests = new String[0]; //put it as a empty array
        }
        for(String i: userInterests){
            userInterestsList.add(i);
        }

        this.isAttendee = isAttendee;
        this.userInterests = userInterests;
        isSelected = new ArrayList<isSelectedBooean>();

        //initialize all to false
        for(InterestsEntity i: interest){
            isSelected.add(new isSelectedBooean(false));
        }

        if(userInterests!= null){
            for(int i=0; i< userInterests.length; i++){
                if(userInterests[i].charAt(0) == ' '){
                    userInterests[i] = userInterests[i].replaceFirst(" ", "");
                }
            }
        }


        if(isAttendee){ //remove those interest that are not the attendee interest
            List<InterestsEntity> interestTmp = new ArrayList<>();
            for(String userInterest: userInterests){
                for(int index = 0; index < interest.size(); index++ ){
                    if(interest.get(index).getInterest().equals(userInterest)){
                        interestTmp.add(interest.get(index));
                        break;
                    }
                }
            }

            this.interest = interestTmp;
        }
    }

    @Override
    public int getCount() {
        return interest.size();
    }

    @Override
    public String getItem(int position) {
        return interest.get(position).getInterest();
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

    public List<InterestsEntity> getInterest() {
        return interest;
    }

    public String getSelectedInterest() {
        String interestString = "";
        for (int i = 0; i < isSelected.size(); i++) {
            if (isSelected.get(i).isSelected) {
                if (interestString.equals("")) {
                    interestString += interest.get(i).getInterest();
                } else {
                    interestString += "," + interest.get(i).getInterest();
                }
            }
        }

        if (interestString.equals("")) {
            interestString = " ";
        }

        return interestString;
    }

    public List<String> getSelectedInterestInListForm() {
        List<String> interestString = new ArrayList<>();
        for(int i=0; i< isSelected.size(); i++){
            if(isSelected.get(i).isSelected){
                interestString.add(interest.get(i).getInterest());
            }
        }

        return interestString;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = convertView;

        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.list_item_interest, parent, false);

            holder.view = view;

            view.setTag(holder);
        }
        else{
            holder = (ViewHolder) view.getTag();
        }

        holder.interestCB = (CheckBox) view.findViewById(R.id.checkbox);
        holder.interestTV = (TextView) view.findViewById(R.id.interest_tv);
        holder.interestCB.setScaleX(scale);
        holder.interestCB.setScaleY(scale);
        holder.interestTV.setText(interest.get(position).getInterest());

        for(String interestString: userInterests){
            if(interest.get(getItemViewType(position)).getInterest().equals(interestString) ){
                isSelected.get(getItemViewType(position)).isSelected = true;
                holder.interestCB.setChecked(true);
                break;
            }
        }

        if(!isAttendee){
            holder.interestCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    isSelected.get(getItemViewType(position)).isSelected  = isChecked;
                }
            });

            holder.interestTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isChecked = holder.interestCB.isChecked();
                    holder.interestCB.setChecked(!isChecked);
                    isSelected.get(getItemViewType(position)).isSelected  = !isChecked;
                }
            });
        }
        else{
            holder.interestCB.setEnabled(false);
            holder.interestCB.setTextColor(c.getResources().getColor(R.color.black));
        }

//        if (view == null) {
//            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//            view = vi.inflate(R.layout.list_item_interest, parent, false);
//
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//
//            CheckBox interestCB = (CheckBox) view.findViewById(R.id.checkbox);
//            interestCB.setLayoutParams(layoutParams);
//            TextView interestTV = (TextView) view.findViewById(R.id.interest_tv);
//
//            interestTV.setText(interest.get(position).getInterest());
//
//            for(String interestString: userInterests){
//                if(interest.get(position).getInterest().equals(interestString) ){
//                    isSelected.get(position).isSelected = true;
//                    interestCB.setChecked(true);
//                    break;
//                }
//            }
//
//            if(!isAttendee){
//                interestCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        isSelected.get(position).isSelected  = isChecked;
//                    }
//                });
//            }
//            else{
//                interestCB.setEnabled(false);
//                interestCB.setTextColor(c.getResources().getColor(R.color.black));
//            }
//
//
//        }

        return view;
    }

    //update new interests
    public void updates(List<InterestsEntity> interest){
        this.interest = interest;
        this.notifyDataSetChanged();
        this.notifyDataSetInvalidated();
    }


    public class isSelectedBooean{
        public Boolean isSelected;
        public isSelectedBooean(Boolean isSelected){
            this.isSelected = isSelected;
        }
    }

    private class ViewHolder{
        public CheckBox interestCB;
        public TextView interestTV;
        public isSelectedBooean isSelected;
        View view;
    }
}
