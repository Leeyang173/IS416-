package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
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
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.net.item.Selfie;
import sg.edu.smu.livelabs.mobicom.services.EVAPromotionService;
import sg.edu.smu.livelabs.mobicom.util.Util;

/**
 * Created by johnlee on 20/10/15.
 */
public class SelfieProfileAdapter extends BaseAdapter {
    private Context context;
    private List<Selfie> photos;
    private SelfieProfileListener listener;

    public SelfieProfileAdapter(Context context, List<Selfie> photos, SelfieProfileListener listener) {
        super();
        this.context = context;
        this.photos = photos;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public Selfie getItem(int position) {
        if (position > getCount()) return null;
        return photos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void append(List<Selfie> newPhotos){
        photos.addAll(newPhotos);
        this.notifyDataSetChanged();
    }

    public void setData(List<Selfie> newPhotos){
        photos.clear();
        photos.addAll(newPhotos);
        this.notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder VH;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.selfie_profile_photo_item, parent, false);
            VH = new ViewHolder(convertView);
            convertView.setTag(VH);
        } else {
            VH = (ViewHolder) convertView.getTag();
        }
        VH.setData(position);
        return convertView;
    }

    class ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        @Bind(R.id.photo)
        public ImageView imageView;
        @Bind(R.id.photo_token)
        public TextView photoTokenTV;
        @Bind(R.id.deleted_by_admin)
        public TextView deletedByAdmin;
        @Bind(R.id.overlay)
        public View overlay;
        private int position;
        private Selfie selfie;

        ViewHolder(View covertView){
            ButterKnife.bind(this, covertView);
            covertView.setOnClickListener(this);
            covertView.setOnLongClickListener(this);
            UIHelper.getInstance().setTypeface(photoTokenTV);
            UIHelper.getInstance().setTypeface(deletedByAdmin);
        }

        public void setData(int position) {
            this.position = position;
            this.selfie = photos.get(position);
            Picasso.with(context).cancelRequest(imageView);
            overlay.setVisibility(View.VISIBLE);
            if (selfie.imageId != null && !selfie.imageId.isEmpty()){
                try {
                    Picasso.with(context).load(Util.getPhotoUrlFromId(selfie.imageId, 256))
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .resize(110, 110)
                            .centerCrop()
                            .noFade().placeholder(R.drawable.placeholder)
                            .into(imageView);
                }
                catch (Throwable e){
                    Log.d("AAA", "SelfieProfileAdapter:ViewHolder:"+e.toString());
                }
            } else {
                imageView.setImageResource(R.drawable.placeholder);
            }
            photoTokenTV.setText(selfie.token);
            if ("inactive".equals(selfie.status)){
                deletedByAdmin.setVisibility(View.VISIBLE);
            } else {
                deletedByAdmin.setVisibility(View.INVISIBLE);
            }

        }

        @Override
        public void onClick(View v) {

            listener.openListView(position);

        }

        @Override
        public boolean onLongClick(View v) {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            Selfie photo = photos.get(position);
            ClipData clip = ClipData.newPlainText("Coolfie", photo.token);
            clipboard.setPrimaryClip(clip);
            UIHelper.getInstance().showAlert(context, String.format(context.getResources().getString(R.string.token_has_copied), photo.token));
            String cmpID = EVAPromotionService.getInstance().currentPromotion.id;
//            EVAPromotionService.getInstance().selfieTracking("610", cmpID, photo.id, photo.token);

            return true;
        }
    }

    public interface SelfieProfileListener{
        void openListView(int currentSelfie);
    }

}
