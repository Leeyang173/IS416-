package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.presenters.ForumPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ForumScreenComponent;

/**
 * Created by smu on 23/4/16.
 */
@AutoInjector(ForumPresenter.class)
public class ForumView extends RelativeLayout {
    @Inject
    public ForumPresenter presenter;
    @Bind(R.id.search_text)
    public EditText searchText;
    @Bind(R.id.new_text)
    public TextView newText;
    @Bind(R.id.new_layout)
    public RelativeLayout newBtn;
    @Bind(R.id.listview)
    public UltimateRecyclerView listView;

    @Bind(R.id.new_topic_layout)
    public RelativeLayout newTopicLayout;
    @Bind(R.id.overlay_view)
    public View overlayView;
    @Bind(R.id.topic_edit_text)
    public EditText topicEdit;
    @Bind(R.id.enter_topic)
    public ImageView submitTopic;

    public ForumView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<ForumScreenComponent>getDaggerComponent(context).inject(this);
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
        super.onFinishInflate();
        ButterKnife.bind(this);
        UIHelper.getInstance().setTypeface(newText, searchText, topicEdit);
    }
}
