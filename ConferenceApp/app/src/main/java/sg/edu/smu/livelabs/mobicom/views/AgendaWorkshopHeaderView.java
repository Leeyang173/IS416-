package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.AgendaWorkshopPaperAdapter;
import sg.edu.smu.livelabs.mobicom.models.AgendaEvent;
import sg.edu.smu.livelabs.mobicom.util.UtilityUI;

/**
 * Created by smu on 8/6/16.
 */
public class AgendaWorkshopHeaderView {

    @Bind(R.id.time_tv)
    public TextView timeTV;
    @Bind(R.id.room_no_tv)
    public TextView roomNoTV;
    @Bind(R.id.title_tv)
    public TextView titleTV;
    @Bind(R.id.sub_title_tv)
    public TextView subTitleTV;
    @Bind(R.id.paperListView)
    public ListView listView;
    @Bind(R.id.comment_header_tv)
    public TextView commentHeader;
    @Bind(R.id.paper_tv)
    public TextView paperTV;

    @Bind(R.id.be_first_tv)
    public TextView beFirstTV;

    private Context context;
    private AgendaWorkshopPaperAdapter adapter;

    public AgendaWorkshopHeaderView(Context context, View view){
        this.context = context;
        ButterKnife.bind(this, view);
        UIHelper uiHelper = UIHelper.getInstance();
        uiHelper.setBoldTypeface(timeTV);
        uiHelper.setBoldTypeface(roomNoTV);
        uiHelper.setBoldTypeface(titleTV);
        uiHelper.setBoldTypeface(subTitleTV);
        uiHelper.setBoldTypeface(beFirstTV);
        uiHelper.setBoldTypeface(paperTV);
        uiHelper.setTypeface(commentHeader);
    }

    public void setData(AgendaEvent event){
        timeTV.setText(event.getStartTimeStr());
        roomNoTV.setText(event.getRoom());
        titleTV.setText(event.getTitle());
        subTitleTV.setText(event.getDescription());
        adapter = new AgendaWorkshopPaperAdapter(context, event.getSubEvents());
        listView.setAdapter(adapter);

        final Handler mainHandler = new Handler(context.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        UtilityUI.setListViewHeightBasedOnChildren(listView);
                    }
                }, 1000);
            }
        });
    }
}
