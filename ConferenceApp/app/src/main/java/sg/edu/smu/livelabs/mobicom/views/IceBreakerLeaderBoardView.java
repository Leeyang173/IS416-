package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.IceBreakerLeaderBoardPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.IceBreakerLeaderBoardScreenComponent;

/**
 * Created by smu on 22/2/16.
 */
@AutoInjector(IceBreakerLeaderBoardPresenter.class)
public class IceBreakerLeaderBoardView extends LinearLayout {
    @Inject
    public IceBreakerLeaderBoardPresenter presenter;

    @Bind(R.id.leaderboard)
    public CustomGridView leaderboardGV;

    @Bind(R.id.your_friends)
    public ListView yourFriendsLV;
    @Bind(R.id.your_friends_text)
    public TextView yourFriendTV;

    @Bind(R.id.no_friend)
    public TextView noFriendTV;

    @Bind(R.id.no_leaderboard)
    public TextView noLeaderBoardTV;


    public IceBreakerLeaderBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<IceBreakerLeaderBoardScreenComponent>getDaggerComponent(context).inject(this);
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
    }
}
