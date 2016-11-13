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
import sg.edu.smu.livelabs.mobicom.presenters.SelfieFullScreenPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SelfieFullScreenScreenComponent;

/**
 * Created by smu on 13/11/15.
 */
@AutoInjector(SelfieFullScreenPresenter.class)
public class SelfieFullScreenView extends RelativeLayout implements HandlesBack {
    @Inject
    public SelfieFullScreenPresenter presenter;
    @Bind(R.id.pager)
    public ViewPager pager;
    @Bind(R.id.button_layout)
    public RelativeLayout buttonLayout;
    @Bind(R.id.parent)
    public RelativeLayout parent;

    @Bind(R.id.like)
    public ImageView like;
    @Bind(R.id.download)
    public ImageView download;
    @Bind(R.id.report)
    public ImageView report;
    @Bind(R.id.editBtn)
    public ImageView editBtn;
    @Bind(R.id.description)
    public TextView description;
    @Bind(R.id.likeno)
    public TextView likeno;
    @Bind(R.id.token)
    public TextView tokenText;
    @Bind(R.id.date_text)
    public TextView dateText;

    public SelfieFullScreenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<SelfieFullScreenScreenComponent>getDaggerComponent(context).inject(this);
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
        UIHelper.getInstance().setTypeface(description, dateText, likeno);
        like.setOnClickListener(presenter);
        download.setOnClickListener(presenter);
        report.setOnClickListener(presenter);
        editBtn.setOnClickListener(presenter);
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
