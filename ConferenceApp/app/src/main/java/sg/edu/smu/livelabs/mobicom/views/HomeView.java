package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.presenters.HomePresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.HomeScreenComponent;

/**
 * Created by smu on 21/1/16.
 */
@AutoInjector(HomePresenter.class)
public class HomeView extends RelativeLayout{
    @Inject
    HomePresenter presenter;
    @Bind(R.id.content_layout)
    public LinearLayout content;
    @Bind(R.id.ongoing_txt)
    public TextView ongoingTxt;
    @Bind(R.id.view_all_txt)
    public TextView viewAllTxt;
    @Bind(R.id.current_activity_title)
    public TextView currentActivityTitle;
    @Bind(R.id.current_events_list)
    public ListView currentEventsList;
    @Bind(R.id.current_activity_list)
    public GridView currentActivityList;
    @Bind(R.id.leaderboard_list)
    public ListView leaderboardList;
    @Bind(R.id.instruction_view)
    public InstructionView instructionView;

    public HomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<HomeScreenComponent>getDaggerComponent(context).inject(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        presenter.takeView(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        presenter.dropView(this);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        ButterKnife.bind(this);
        super.onFinishInflate();
        UIHelper.getInstance().setTypeface(ongoingTxt, viewAllTxt, currentActivityTitle);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("Mobicom_GameFirstTime", Context.MODE_PRIVATE);
        if(!sharedPreferences.contains("isOpeningHomePageFirstTime")){
            instructionView.setContent("",
                    getContext().getString(R.string.guide_home),
                    50);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isOpeningHomePageFirstTime", false);
            editor.commit();
        }
        else{
            instructionView.setVisibility(GONE);
        }

    }
}
