package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.models.data.PaperEventEntity;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;

/**
 * Created by smu on 14/4/16.
 */
public class AgendaPaperAdapter extends UltimateViewAdapter<AgendaPaperAdapter.ViewHolder>{
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM");
    private List<PaperEventEntity> papers;
    private Context context;
    private AgendaPaperListener listener;

    public AgendaPaperAdapter(Context context, AgendaPaperListener listener){
        this.context = context;
        this.listener = listener;
    }

    public void setData(List<PaperEventEntity> papers){
        this.papers = papers;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder getViewHolder(View view) {
        return new ViewHolder(view, false);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.agenda_paper_item, parent, false);
        return new ViewHolder(view, true);
    }

    @Override
    public int getAdapterItemCount() {
        return papers == null ? 0 : papers.size();
    }

    @Override
    public long generateHeaderId(int position) {
        if (position >= papers.size()) return -1;
        return papers.get(position).getSessionServerID();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position < getItemCount() && (customHeaderView != null ? position <= papers.size() : position < papers.size()) && (customHeaderView != null ? position > 0 : true)) {
            PaperEventEntity paper = getItem(position);
            if (paper != null) {
                holder.setData(paper);
            }
        }
    }

    @Override
    public ViewHolderHeader onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.agenda_time_header, parent, false);
        return new ViewHolderHeader(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        PaperEventEntity paper = getItem(position);
        if(paper != null){
            ((ViewHolderHeader)holder).setData(paper);
        }
    }

    public PaperEventEntity getItem(int position) {
        if (customHeaderView != null)
            position--;
        if (position < papers.size())
            return papers.get(position);
        else return null;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @Bind(R.id.icon_epub)
        public ImageView epub;
        @Bind(R.id.icon_pdf)
        public ImageView pdf;
        @Bind(R.id.title_txt)
        public TextView titleTxt;
        private PaperEventEntity data;

        public ViewHolder(View itemView, boolean isItem) {
            super(itemView);
            if (!isItem) return;
            ButterKnife.bind(this, itemView);
            UIHelper.getInstance().setBoldTypeface(titleTxt);
            pdf.setOnClickListener(this);
            epub.setOnClickListener(this);
        }

        public void setData(PaperEventEntity data) {
            this.data = data;
            titleTxt.setText(data.getTitle());
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.icon_epub){
                listener.openEpub(data.getEpubLink());
                TrackingService.getInstance().sendTracking("121", "agenda", "epub", Long.toString(data.getServerId()), "", "");

            } else if (id == R.id.icon_pdf){
                listener.openPDF(data.getPdfLink());
                TrackingService.getInstance().sendTracking("120", "agenda", "pdf", Long.toString(data.getServerId()), "", "");
            }
        }
    }

    class ViewHolderHeader extends RecyclerView.ViewHolder {

        @Bind(R.id.time_header)
        public TextView header;

        public ViewHolderHeader(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            UIHelper.getInstance().setTypeface(header);
        }

        public void setData(PaperEventEntity data) {
            header.setText(String.format("%s - %s", simpleDateFormat.format(data.getEventTime()), data.getSessionTile()));
        }
    }

    public interface AgendaPaperListener{
        void openPDF(String url);
        void openEpub(String url);
    }
}
