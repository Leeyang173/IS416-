package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import me.itangqi.waveloadingview.WaveLoadingView;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.presenters.IceBreakerPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.IceBreakerScreenComponent;

/**
 * Created by smu on 22/2/16.
 */
@AutoInjector(IceBreakerPresenter.class)
public class IceBreakerView extends LinearLayout {
    @Inject
    public IceBreakerPresenter presenter;

    @Bind(R.id.qr_code)
    public ImageView qrCodeIV;

    @Bind(R.id.scan)
    public Button scanButton;

    @Bind(R.id.waveLoadingView)
    public WaveLoadingView waveLoadingView;

    @Bind(R.id.no_internet_container)
    public RelativeLayout noInternetContainer;

    @Bind(R.id.leaderboard)
    public ImageView leaderboardIV;

    @Bind(R.id.current_friend)
    public TextView currentFriendTV;

    @Bind(R.id.description)
    public TextView descriptionTV;

    @Bind(R.id.container)
    public RelativeLayout container;

    @Bind(R.id.content_container)
    public LinearLayout containerTV;

    @Bind(R.id.loading)
    public LinearLayout spinnerLL;

    @Bind(R.id.current_friend_container)
    public LinearLayout currentFriendContainer;


    public IceBreakerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<IceBreakerScreenComponent>getDaggerComponent(context).inject(this);
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
