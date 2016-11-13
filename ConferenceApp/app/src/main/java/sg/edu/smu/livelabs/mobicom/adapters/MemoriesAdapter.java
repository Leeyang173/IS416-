package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.content.res.Resources;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import flow.Flow;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.net.item.EVAPromotionItem;
import sg.edu.smu.livelabs.mobicom.presenters.screen.MemoriesMainScreen;
import sg.edu.smu.livelabs.mobicom.services.MemoriesService;

/**
 * Created by smu on 27/10/15.
 */
public class MemoriesAdapter extends BaseAdapter {
    private Context context;
    private List<EVAPromotionItem> evaPromotions;

    public MemoriesAdapter(Context context){
        this.context = context;
    }

    public void setPromotions(List<EVAPromotionItem> evaPromotions){
        this.evaPromotions = evaPromotions;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return evaPromotions == null ? 0 : evaPromotions.size();
    }

    @Override
    public EVAPromotionItem getItem(int position) {
        if (evaPromotions == null || evaPromotions.size() <= position)
            return null;
        return evaPromotions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.memories_item, null);
            ViewHolder VH = new ViewHolder(convertView);
            convertView.setTag(VH);
        }
        ViewHolder VH = (ViewHolder) convertView.getTag();
        EVAPromotionItem promotion = getItem(position);
        if (promotion != null){
            VH.title.setText(promotion.name);
            VH.promotionTime.setText(promotion.timeString);
            Picasso.with(context).cancelRequest(VH.img);
            if (promotion.image != null && !promotion.image.isEmpty()){
                Picasso.with(context)
//                        .load(RestClient.BASE_URL + promotion.image)
                        .load(promotion.image)
                        .resize(300,
                                200)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .noFade().placeholder(R.drawable.placeholder)
                        .into(VH.img);
            } else {
                VH.img.setImageResource(R.drawable.placeholder);
            }
            VH.promotion = promotion;
            if (promotion.promotionType == MemoriesService.TYPE_SELFIE_NEXT_COMING){
                VH.description.setVisibility(View.VISIBLE);
                VH.promotionImgNum.setVisibility(View.INVISIBLE);
                VH.description.setText(promotion.description);
                Resources rs = context.getResources();
                VH.title.setTextColor(rs.getColor(R.color.grey));
                VH.promotionTime.setTextColor(rs.getColor(R.color.grey));
            } else {
                VH.promotionImgNum.setText(promotion.description);
                VH.description.setVisibility(View.INVISIBLE);
                VH.promotionImgNum.setVisibility(View.VISIBLE);
            }
        }
        return convertView;
    }

    class ViewHolder implements View.OnClickListener{
        @Bind(R.id.promotion_title)
        public TextView title;
        @Bind(R.id.promotion_description)
        public TextView description;
        @Bind(R.id.promotion_time)
        public TextView promotionTime;
        @Bind(R.id.promotion_img_num)
        public TextView promotionImgNum;
        @Bind(R.id.promotion_img)
        public ImageView img;

        public EVAPromotionItem promotion;
        public ViewHolder(View view){
            ButterKnife.bind(this, view);
//            UIHelper.getInstance().setTypeface(title, true);
//            UIHelper.getInstance().setTypeface(description, true);
//            UIHelper.getInstance().setTypeface(promotionImgNum, true);
//            UIHelper.getInstance().setTypeface(promotionTime);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int type = promotion.promotionType;
//            MemoriesService.getInstance().currentPromotion = promotion;
            switch (type){
                case MemoriesService.TYPE_SELFIE_NEXT_COMING:
                    UIHelper.getInstance().showAlert(context, context.getResources().getString(R.string.cmp_start_from) + promotion.startStr);
                    break;
                case MemoriesService.TYPE_SELFIE_PAST:
                case MemoriesService.TYPE_SELFIE_CURRENT:
                    Flow.get(context).set(new MemoriesMainScreen());
                    break;
            }
        }
    }
}
