package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import sg.edu.smu.livelabs.mobicom.net.item.MemoriesItem;
import sg.edu.smu.livelabs.mobicom.presenters.MemoriesListPresenter;
import sg.edu.smu.livelabs.mobicom.util.Util;

/**
 * Created by smu on 3/11/15.
 */
public class MemoriesListViewAdapter extends UltimateViewAdapter<MemoriesListViewAdapter.ViewHolder> {

    private Context context;
    private List<MemoriesItem> images;
    private MemoriesListPresenter listener;
    private View parentView;

    public MemoriesListViewAdapter(Context context, MemoriesListPresenter listener, View parentView){
        this.context = context;
        images = new ArrayList<>();
        this.listener = listener;
        this.parentView = parentView;
    }

    public void setData(List<MemoriesItem> images){
        this.images.clear();
        this.images.addAll(images);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder getViewHolder(View view) {
        return new ViewHolder(view, false);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(context).inflate(R.layout.memories_list_item, parent, false);
        return new ViewHolder(v, true);
    }

    @Override
    public int getAdapterItemCount() {
        return images == null ? 0 : images.size();
    }

    @Override
    public long generateHeaderId(int position) {
        if (customHeaderView != null)
            position--;

//        return position;

        if (position < images.size()){
            MemoriesItem image = images.get(position);
            if (image != null) return Long.valueOf(image.id);
            return 0;
        }
        return -1;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position < getItemCount() &&
                (customHeaderView != null ? position <= images.size() : position < images.size()) &&
                (customHeaderView != null ? position > 0 : true)) {
            MemoriesItem image = this.getItem(position);
            holder.setData(image);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(context).inflate(R.layout.selfie_list_header, parent, false);
        return new ViewHolderHeader(v);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (customHeaderView != null)
            position--;
        if (position >= images.size()) return;
        MemoriesItem image = images.get(position);
        ViewHolderHeader headerViewHolder = ((ViewHolderHeader) holder);
        headerViewHolder.setData(image);
    }

    public void insertData(List<MemoriesItem> newSelfies) {
        List<MemoriesItem> newSelfiesTmp = new ArrayList<>();
        for(MemoriesItem s: newSelfies){
            boolean isExist = false;
            int position = 0;
            for(MemoriesItem image: this.images){
                if(image.id.equals(s.id)){
                    isExist = true;
                    break;
                }
                position++;
            }

            if(isExist){
                this.images.remove(position);
                this.images.add(position, s);
                newSelfiesTmp.add(s);
            }
        }
        notifyDataSetChanged();

        newSelfies.removeAll(newSelfiesTmp);
//        for (MemoriesItem image : newSelfies) {
//            insertFirstInternal(this.images, image);
//        }
        insertInternal(newSelfies, this.images);
    }

    public MemoriesItem getLastItem() {
        return images.get(images.size() - 1);
    }

    public MemoriesItem getItem(int position) {
        return images.get(customHeaderView != null ? position - 1 : position);
    }

    public void removeItemAt(int position){
        removeInternal(this.images, position);
    }

    public List<MemoriesItem> getAllItems() {
        return images;
    }

    public class ViewHolder extends UltimateRecyclerviewViewHolder implements View.OnClickListener{

        @Bind(R.id.img)
        public ImageView img;
        @Bind(R.id.description)
        public TextView description;


        private MemoriesItem image;

        public ViewHolder(View itemView, boolean isNew) {
            super(itemView);
            if (isNew){
                ButterKnife.bind(this, itemView);
                UIHelper.getInstance().setTypeface(description);
                img.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id){
                case R.id.img:
                    listener.goToFullScreen(getAdapterPosition());
                    break;
            }
        }

        public void setData(MemoriesItem image) {
            this.image = image;
            Picasso.with(context).cancelRequest(img);
            if (image.image != null && !image.image.isEmpty()){
                try {
                    Picasso.with(context)
                            .load(Util.getPhotoUrlFromId(image.image, 512))
                            .placeholder(R.drawable.placeholder)
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .into(img);
                }
                catch(Throwable e){//catch OOM
                    System.gc();
                }
            } else {
                img.setImageResource(R.drawable.placeholder);
            }



            description.setText(image.caption);

        }
    }

    public class ViewHolderHeader extends RecyclerView.ViewHolder{


        public MemoriesItem selfie;


        public ViewHolderHeader(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setData(final MemoriesItem selfie) {
            this.selfie = selfie;

        }
    }

}
