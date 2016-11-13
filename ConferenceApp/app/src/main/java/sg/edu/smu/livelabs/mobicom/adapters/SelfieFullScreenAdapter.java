package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.net.item.Selfie;
import sg.edu.smu.livelabs.mobicom.presenters.SelfieFullScreenPresenter;
import sg.edu.smu.livelabs.mobicom.util.Util;

/**
 * Created by smu on 17/11/15.
 */
public class SelfieFullScreenAdapter extends PagerAdapter {
    private Context context;
    private List<Selfie> selfies;
    private SelfieFullScreenPresenter listener;
    public SelfieFullScreenAdapter(Context context, List<Selfie> selfies, SelfieFullScreenPresenter listener){
        this.context = context;
        this.listener = listener;
        this.selfies = selfies;
        this.notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return selfies.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.selfie_full_screen_item, container, false);
        ViewHolder VH = new ViewHolder(view);
        VH.setData(selfies.get(position), position);
        view.setTag(VH);
        ((ViewPager) container).addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);
    }


    public class ViewHolder implements View.OnClickListener{

        @Bind(R.id.image)
        public ImageView image;

        public ViewHolder(View view){
            ButterKnife.bind(this, view);
            image.setOnClickListener(this);
        }

        public void setData(Selfie selfie, int position){
            Picasso.with(context).cancelRequest(image);
            if (selfie.imageId != null && !selfie.imageId.isEmpty()){
                try {
                    Picasso.with(context).load(Util.getPhotoUrlFromId(selfie.imageId, 512))
                            .noFade()
                            .placeholder(R.drawable.placeholder)
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .into(image);
                }
                catch(Throwable e){//catch OOM
                    System.gc();
                }
            }else {
                image.setImageResource(R.drawable.placeholder);
            }
            image.setTag(position);
        }

        @Override
        public void onClick(View v) {
            listener.hideOrShowControlLayout();
        }
    }
}
