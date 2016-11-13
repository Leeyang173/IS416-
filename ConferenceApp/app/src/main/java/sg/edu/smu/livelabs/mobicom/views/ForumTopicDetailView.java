package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.adapters.AgendaCommentAdapter;
import sg.edu.smu.livelabs.mobicom.presenters.ForumTopicDetailPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ForumTopicDetailScreenComponent;

/**
 * Created by smu on 23/4/16.
 */
@AutoInjector(ForumTopicDetailPresenter.class)
public class ForumTopicDetailView extends RelativeLayout {
    @Inject
    ForumTopicDetailPresenter presenter;

    @Bind(R.id.list_view)
    public UltimateRecyclerView listView;

    @Bind(R.id.send_comment_layout)
    public RelativeLayout chatLayout;
    @Bind(R.id.comment_edit_text)
    public EditText commentEdit;
    @Bind(R.id.enter_comment)
    public ImageView enterComment;

    public MainActivity mainActivity;
    public AgendaCommentAdapter adapter;


    public ForumTopicDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<ForumTopicDetailScreenComponent>getDaggerComponent(context).inject(this);
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


//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        super.onLayout(changed, l, t, r, b);
//        Rect rect = new Rect();
//        View rootView = this.getRootView();
//        int usableViewHeight = rootView.getHeight() - AndroidUtilities.statusBarHeight - AndroidUtilities.getViewInset(rootView);
//        this.getWindowVisibleDisplayFrame(rect);
//        int height = (rect.bottom - rect.top);
//        int keyboardHeight = usableViewHeight - height;
//        if (keyboardHeight > 100){
//            listView.scrollVerticallyToPosition(adapter.getItemCount() - 1);
//            listView.showToolbar(mainActivity.getToolbar(), listView, height);
//        }
//    }
}
