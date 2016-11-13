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
import sg.edu.smu.livelabs.mobicom.models.MemoriesLine;
import sg.edu.smu.livelabs.mobicom.net.item.MemoriesItem;
import sg.edu.smu.livelabs.mobicom.util.Util;

/**
 * Created by smu on 2/11/15.
 */
public class MemoriesHomeAdapter extends UltimateViewAdapter<MemoriesHomeAdapter.ViewHolder> {
    private MemoriesHomeListener listener;
    private List<MemoriesLine> memoriesLines;
    private Context context;

    public MemoriesHomeAdapter(Context context, MemoriesHomeListener listener){
        this.listener = listener;
        this.context = context;
        memoriesLines = new ArrayList<>();
    }

    public void reset(){
        memoriesLines.clear();
        notifyDataSetChanged();
    }

    public void setData(List<MemoriesItem> images){
        memoriesLines.clear();
        int count = images.size();
        int i = 0;
        while (i + 2 < count){
            memoriesLines.add(new MemoriesLine(images.get(i),
                    images.get(i + 1),
                    images.get( i + 2)));
            i+=3;
        }
        int remain = count - i;
        if (remain == 1){
            memoriesLines.add(new MemoriesLine(images.get(i), null, null));
        } else if (remain == 2){
            memoriesLines.add(new MemoriesLine(images.get(i),
                    images.get(i + 1),
                    null));
        }
        notifyDataSetChanged();
    }

    public void insertData(List<MemoriesItem> images){
        if (images == null || images.size() < 1) return;

        int count = images.size();
        MemoriesLine lastLine = memoriesLines.get(memoriesLines.size() - 1);
        int index = 0;
        if (lastLine.getSecond() == null){
            lastLine.setSecond(images.get(index));
            index++;
            if (index >= count) {
                notifyDataSetChanged();
                return;
            }
        }
        if (lastLine.getThird() == null){
            lastLine.setThird(images.get(index));
            index++;
            if (index >= count) {
                notifyDataSetChanged();
                return;
            }
        }
        while (index + 2 < count){
            insertInternal(memoriesLines,
                    new MemoriesLine(images.get(index), images.get(index + 1), images.get(index + 2)),
                    getAdapterItemCount());
            index+= 3;
        }

        int remain = count - index;
        if (remain == 1){
            insertInternal(memoriesLines,
                    new MemoriesLine(images.get(index), null, null),
                    getAdapterItemCount());
        } else if (remain == 2) {
            insertInternal(memoriesLines,
                    new MemoriesLine(images.get(index), images.get(index + 1), null),
                    getAdapterItemCount());
        }
    }

    @Override
    public int getAdapterItemCount() {
        return memoriesLines == null ? 0 : memoriesLines.size();
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
                (customHeaderView != null ? position <= memoriesLines.size() : position < memoriesLines.size()) &&
                (customHeaderView != null ? position > 0 : true)) {
            MemoriesLine memoriesLine = memoriesLines.get(customHeaderView != null ? position - 1 : position);
            holder.setData(memoriesLine);
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
                .inflate(R.layout.memories_list_header, viewGroup, false);
        return new HeaderViewHolder(v);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (customHeaderView != null)
            position--;
        if (position >= memoriesLines.size()) return;
        HeaderViewHolder headerViewHolder =  ((HeaderViewHolder)viewHolder);
        Resources resources = context.getResources();
//        headerViewHolder.typeText.setText(resources.getString(R.string.recent));
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder{
        @Bind(R.id.type_txt)
        public TextView typeText;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            UIHelper.getInstance().setTypeface(typeText);
            typeText.setVisibility(View.GONE);
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

        public void setData(MemoriesLine memoriesLine){
            setData(memoriesLine.getFrist(), imageView1, selfie1Layout);
            setData(memoriesLine.getSecond(), imageView2, selfie2Layout);
            setData(memoriesLine.getThird(), imageView3, selfie3Layout);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
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

        private void setData(MemoriesItem image, ImageView imageView, RelativeLayout layout){
            if (image != null){
                Picasso.with(context).cancelRequest(imageView);
                if (image.image != null && !image.image.isEmpty()) {
                    try{
                        Picasso.with(context)
                                .load(Util.getPhotoUrlFromId(image.image, 256))
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

    public interface MemoriesHomeListener{
        void likeClick(MemoriesItem image, boolean isTopPhoto);
        void openListView(int currentSelfie);
    }
}
