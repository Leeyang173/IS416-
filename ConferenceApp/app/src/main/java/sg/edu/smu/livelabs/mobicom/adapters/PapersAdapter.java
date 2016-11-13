package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.models.data.PaperEntity;

/**
 * Created by smu on 15/3/16.
 */
public class PapersAdapter extends UltimateViewAdapter<PapersAdapter.ViewHolder> {

    private List<PaperEntity> paperEntities;
    private Context context;
    private PapersListener papersListener;

    public PapersAdapter(Context context, PapersListener papersListener){
        paperEntities = new ArrayList<>();
        this.context = context;
        this.papersListener = papersListener;
    }

    public void setData(List<PaperEntity> papers){
        paperEntities.clear();
        paperEntities.addAll(papers);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder getViewHolder(View view) {
        return new ViewHolder(view, false);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.paper_item, parent, false);
        return new ViewHolder(view, true);
    }

    @Override
    public int getAdapterItemCount() {
        return paperEntities.size();
    }

    @Override
    public long generateHeaderId(int position) {
        return -1;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position < getItemCount() &&
                (customHeaderView != null ? position <= paperEntities.size() : position < paperEntities.size()) &&
                (customHeaderView != null ? position > 0 : true)) {
            int realPosition = customHeaderView != null ? position - 1 : position;
            PaperEntity paperEntity = paperEntities.get(realPosition);
            holder.setData(paperEntity);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        return null;
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @Bind(R.id.title_txt)
        public TextView titleText;
        @Bind(R.id.author_txt)
        public TextView authorText;
        @Bind(R.id.download_btn)
        public ImageView downloadBtn;
        private PaperEntity paperEntity;

        public ViewHolder(View itemView, boolean isItem) {
            super(itemView);
            if (isItem) {
                ButterKnife.bind(this, itemView);
                UIHelper.getInstance().setBoldTypeface(titleText);
                UIHelper.getInstance().setTypeface(authorText);
                downloadBtn.setOnClickListener(this);
            }
        }

        public void setData(PaperEntity paperEntity){
            this.paperEntity = paperEntity;
            titleText.setText(paperEntity.getTitle());
            authorText.setText(paperEntity.getAuthor());
        }

        @Override
        public void onClick(View v) {
            papersListener.openPaper(paperEntity.getUrl());
        }
    }

    public interface PapersListener{
        void openPaper(String url);
    }
}
