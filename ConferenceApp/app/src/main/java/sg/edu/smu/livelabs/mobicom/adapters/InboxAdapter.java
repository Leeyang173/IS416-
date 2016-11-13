package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.dao.query.LazyList;
import de.hdodenhof.circleimageview.CircleImageView;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.models.data.InboxEntity;
import sg.edu.smu.livelabs.mobicom.services.AgendaService;


/**
 * Created by smu on 30/5/16.
 */
public class InboxAdapter extends UltimateViewAdapter<InboxAdapter.ViewHolder> {

    private Context context;
    private LazyList<InboxEntity> inboxEntities;
    public InboxAdapter(Context context){
        this.context = context;
    }

    public void setData(LazyList<InboxEntity> inboxEntities){
        LazyList<InboxEntity> temp = this.inboxEntities;
        this.inboxEntities = inboxEntities;
        this.notifyDataSetChanged();
        if (temp != null){
            temp.close();
        }
    }

    public void closeData() {
        if (this.inboxEntities != null){
            this.inboxEntities.close();
        }
    }

    @Override
    public ViewHolder getViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.inbox_view_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getAdapterItemCount() {
        return inboxEntities == null ? 0 : inboxEntities.size();
    }

    @Override
    public long generateHeaderId(int position) {
        //        if (customHeaderView != null)
//            position--;
//        if (position < events.size()){
//            return events.get(position).getStartTime().getTime();
//        }
        return -1;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position < getItemCount() &&
                (customHeaderView != null ? position <= inboxEntities.size() : position < inboxEntities.size()) &&
                (customHeaderView != null ? position > 0 : true)) {
            int realPosition = customHeaderView != null ? position - 1 : position;
            InboxEntity inboxEntity = inboxEntities.get(realPosition);
            holder.setData(inboxEntity, realPosition);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.agenda_time_header, parent, false);
        return new HeaderViewHolder(v);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
//        if (customHeaderView != null)
//            position--;
//        if (position >= inboxEntities.size()) return;
//        HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
//        headerViewHolder.time.setText(inboxEntities.get(position).getStartTimeStr());
    }



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Bind(R.id.read_status_cv)
        public CircleImageView readStatusView;
        @Bind(R.id.date_tv)
        public TextView dateTV;
        @Bind(R.id.time_tv)
        public TextView timeTV;
        @Bind(R.id.content_tv)
        public TextView contentTv;
        @Bind(R.id.more_btn)
        public ImageView moreBtn;
        @Bind(R.id.background_layout)
        public View backgroundLayout;

        private InboxEntity inboxEntity;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            UIHelper.getInstance().setBoldTypeface(dateTV);
            UIHelper.getInstance().setTypeface(timeTV, contentTv);
            itemView.setOnClickListener(this);
        }

        public void setData(InboxEntity inboxEntity, int position){
            this.inboxEntity = inboxEntity;
            dateTV.setText(AgendaService.simpleDateFormat.format(inboxEntity.getDateTime()));
            timeTV.setText(AgendaService.simpleTimeFormat.format(inboxEntity.getDateTime()));
            contentTv.setText(inboxEntity.getTitle());
            if (position % 2 == 0){
                backgroundLayout.setBackgroundColor(0xfffbfdda);
            } else {
                backgroundLayout.setBackgroundColor(0xffffffff);
            }
            if (inboxEntity.getRead()){
                readStatusView.setVisibility(View.INVISIBLE);
            } else {
                readStatusView.setVisibility(View.VISIBLE);
            }
            if (inboxEntity.getMessage().isEmpty()){
                moreBtn.setVisibility(View.GONE);
            } else {
                moreBtn.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            if (moreBtn.getVisibility() == View.GONE){
                Log.d(App.APP_TAG, "invisible");
                if (!inboxEntity.getRead()){
                    inboxEntity.setRead(true);
                    readStatusView.setVisibility(View.INVISIBLE);
                    AgendaService.getInstance().updateInbox(inboxEntity);
                }
            } else if (moreBtn.isSelected()){
                moreBtn.setSelected(false);
                moreBtn.setBackgroundResource(R.drawable.icon_arrow_right_grey_1);
                contentTv.setText(inboxEntity.getTitle());
            } else {
                moreBtn.setSelected(true);
                moreBtn.setBackgroundResource(R.drawable.icon_arrow_down_grey_1);
                contentTv.setText(inboxEntity.getMessage());
                if (!inboxEntity.getRead()){
                    inboxEntity.setRead(true);
                    readStatusView.setVisibility(View.INVISIBLE);
                    AgendaService.getInstance().updateInbox(inboxEntity);
                }
            }
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.time_header)
        public TextView time;
        public HeaderViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            UIHelper.getInstance().setTypeface(time);
        }
    }
}
