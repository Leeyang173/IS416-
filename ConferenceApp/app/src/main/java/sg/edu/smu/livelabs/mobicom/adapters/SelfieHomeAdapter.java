package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.models.SelfieLine;
import sg.edu.smu.livelabs.mobicom.net.item.Selfie;
import sg.edu.smu.livelabs.mobicom.util.Util;

/**
 * Created by smu on 2/11/15.
 */
public class SelfieHomeAdapter extends UltimateViewAdapter<SelfieHomeAdapter.ViewHolder> {
    private SelfieHomeListener listener;
    private List<SelfieLine> selfieLines;
    private Context context;

    public SelfieHomeAdapter(Context context, SelfieHomeListener listener){
        this.listener = listener;
        this.context = context;
        selfieLines = new ArrayList<>();
    }

    public void reset(){
        selfieLines.clear();
        notifyDataSetChanged();
    }

    public void setData(List<Selfie> selfies){
        selfieLines.clear();
        int count = selfies.size();
        int i = 0;
        while (i + 2 < count){
            selfieLines.add(new SelfieLine(selfies.get(i),
                    selfies.get(i + 1),
                    selfies.get( i + 2)));
            i+=3;
        }
        int remain = count - i;
        if (remain == 1){
            selfieLines.add(new SelfieLine(selfies.get(i), null, null));
        } else if (remain == 2){
            selfieLines.add(new SelfieLine(selfies.get(i),
                    selfies.get(i + 1),
                    null));
        }
        notifyDataSetChanged();
    }

    public void insertData(List<Selfie> selfies){
        if (selfies == null || selfies.size() < 1) return;

        int count = selfies.size();
        SelfieLine lastLine = selfieLines.get(selfieLines.size() - 1);
        int index = 0;
        if (lastLine.getSecond() == null){
            lastLine.setSecond(selfies.get(index));
            index++;
            if (index >= count) {
                notifyDataSetChanged();
                return;
            }
        }
        if (lastLine.getThird() == null){
            lastLine.setThird(selfies.get(index));
            index++;
            if (index >= count) {
                notifyDataSetChanged();
                return;
            }
        }
        while (index + 2 < count){
            insertInternal(selfieLines,
                    new SelfieLine(selfies.get(index), selfies.get(index + 1), selfies.get(index + 2)),
                    getAdapterItemCount());
            index+= 3;
        }

        int remain = count - index;
        if (remain == 1){
            insertInternal(selfieLines,
                    new SelfieLine(selfies.get(index), null, null),
                    getAdapterItemCount());
        } else if (remain == 2) {
            insertInternal(selfieLines,
                    new SelfieLine(selfies.get(index), selfies.get(index + 1), null),
                    getAdapterItemCount());
        }
    }

    @Override
    public int getAdapterItemCount() {
        return selfieLines == null ? 0 : selfieLines.size();
    }

    @Override
    public ViewHolder getViewHolder(View view) {
        return new ViewHolder(view, false);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selfie_recent_line, parent, false);
        ViewHolder holder = new ViewHolder(view, true);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position < getItemCount() &&
                (customHeaderView != null ? position <= selfieLines.size() : position < selfieLines.size()) &&
                (customHeaderView != null ? position > 0 : true)) {
            SelfieLine selfieLine = selfieLines.get(customHeaderView != null ? position - 1 : position);
            holder.setData(selfieLine);
        }
    }

    @Override
    public long generateHeaderId(int position) {
        return 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
//        View v = LayoutInflater.from(viewGroup.getContext())
//                .inflate(R.layout.selfie_recent_header, viewGroup, false);
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.connect_friend_list_header, viewGroup, false);
        return new HeaderViewHolder(v);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (customHeaderView != null)
            position--;
        if (position >= selfieLines.size()) return;
        HeaderViewHolder headerViewHolder =  ((HeaderViewHolder)viewHolder);
        Resources resources = context.getResources();
        headerViewHolder.typeText.setText(resources.getString(R.string.recent));
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder{
        @Bind(R.id.type_txt)
        public TextView typeText;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            UIHelper.getInstance().setTypeface(typeText);
        }
    }

    public class ViewHolder extends UltimateRecyclerviewViewHolder implements View.OnClickListener {
        @Bind(R.id.selfie1_layout)
        public RelativeLayout selfie1Layout;
        @Bind(R.id.img1)
        public ImageView imageView1;

        @Bind(R.id.selfie2_layout)
        public RelativeLayout selfie2Layout;
        @Bind(R.id.img2)
        public ImageView imageView2;

        @Bind(R.id.selfie3_layout)
        public RelativeLayout selfie3Layout;
        @Bind(R.id.img3)
        public ImageView imageView3;


        public ViewHolder(View itemView, boolean iscreate) {
            super(itemView);
            if (iscreate){
                ButterKnife.bind(this, itemView);
                imageView1.setOnClickListener(this);
                imageView2.setOnClickListener(this);
                imageView3.setOnClickListener(this);
            }

        }

        public void setData(SelfieLine selfieLine){
            setData(selfieLine.getFrist(), imageView1, selfie1Layout);
            setData(selfieLine.getSecond(), imageView2, selfie2Layout);
            setData(selfieLine.getThird(), imageView3, selfie3Layout);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition() - 1;
            int id = v.getId();
            switch (id){
                case R.id.img1:
                    listener.openListView(position * 3);
                    break;
                case R.id.img2:
                    listener.openListView(position * 3 + 1);
                    break;
                case R.id.img3:
                    listener.openListView(position * 3 + 2);
                    break;
            }
        }

        private void setData(Selfie selfie, ImageView imageView, RelativeLayout layout){
            if (selfie != null){
                Picasso.with(context).cancelRequest(imageView);
                if (selfie.imageId != null && !selfie.imageId.isEmpty()) {
                    try{
                        Picasso.with(context)
                                .load(Util.getPhotoUrlFromId(selfie.imageId, 256))
                                .resize(110, 110)
                                .centerCrop()
                                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                                .networkPolicy(NetworkPolicy.NO_CACHE)
                                .noFade().placeholder(R.drawable.placeholder)
                                .into(imageView);
                    }
                    catch(Throwable e){//catch OOM
                        System.gc();
                    }
                } else {
                    imageView.setImageResource(R.drawable.placeholder);
                }
                layout.setVisibility(View.VISIBLE);
            } else {
                layout.setVisibility(View.INVISIBLE);
                imageView.setImageResource(R.drawable.placeholder);
            }
        }
    }

    public interface SelfieHomeListener{
        void likeClick(Selfie selfie, boolean isTopPhoto);
        void openListView(int currentSelfie);
    }
}
