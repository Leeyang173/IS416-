package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import flow.Flow;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.flow.HandlesBack;
import sg.edu.smu.livelabs.mobicom.presenters.MemoriesFullScreenPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.MemoriesFullScreenScreenComponent;

/**
 * Created by smu on 13/11/15.
 */
@AutoInjector(MemoriesFullScreenPresenter.class)
public class MemoriesFullScreenView extends RelativeLayout implements HandlesBack {
    @Inject
    public MemoriesFullScreenPresenter presenter;
    @Bind(R.id.pager)
    public ViewPager pager;
    @Bind(R.id.button_layout)
    public RelativeLayout buttonLayout;
    @Bind(R.id.parent)
    public RelativeLayout parent;

    @Bind(R.id.download)
    public ImageView download;
    @Bind(R.id.description)
    public TextView description;

    public MemoriesFullScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<MemoriesFullScreenScreenComponent>getDaggerComponent(context).inject(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        presenter.takeView(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        presenter.dropView(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
//        UIHelper.getInstance().setTypeface(tokenText, true);
        UIHelper.getInstance().setTypeface(description);
        download.setOnClickListener(presenter);
    }

    public void hideOrShowButtonLayout(){
        if (buttonLayout.getVisibility() == VISIBLE){
            buttonLayout.setVisibility(INVISIBLE);
        } else {
            buttonLayout.setVisibility(VISIBLE);
        }
    }

    @Override
    public boolean onBackPressed() {
        presenter.closePage();
        Flow.get(getContext()).goBack();
        return true;
    }
}
