package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.content.SharedPreferences;
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
import sg.edu.smu.livelabs.mobicom.presenters.GamesPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.GamesScreenComponent;

/**
 * Created by smu on 22/2/16.
 */
@AutoInjector(GamesPresenter.class)
public class GamesView extends LinearLayout {
    @Inject
    public GamesPresenter presenter;

    @Bind(R.id.list)
    public ListView gameList;

    @Bind(R.id.no_result_text)
    public TextView msgTV;

    @Bind(R.id.instruction_view)
    public InstructionView instructionView;

    public GamesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<GamesScreenComponent>getDaggerComponent(context).inject(this);
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
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("Mobicom_GameFirstTime", Context.MODE_PRIVATE);
        if(!sharedPreferences.contains("isOpeningGamePageFirstTime")){
            instructionView.setContent("I'm back.",
                    getContext().getString(R.string.game_guide),
                    50);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isOpeningGamePageFirstTime", false);
            editor.commit();
        }
        else{
            instructionView.setVisibility(GONE);
        }
    }
}
