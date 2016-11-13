package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import sg.edu.smu.livelabs.mobicom.presenters.ChatListPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ChatListScreenComponent;

/**
 * Created by smu on 8/3/16.
 */
@AutoInjector(ChatListPresenter.class)
public class ChatListView extends LinearLayout {
    @Inject
    public ChatListPresenter presenter;

    @Bind(R.id.search_text)
    public EditText searchText;
    @Bind(R.id.add_group_text)
    public TextView addGroupText;
    @Bind(R.id.add_group_chat)
    public RelativeLayout addGroupBtn;
    @Bind(R.id.chat_list)
    public UltimateRecyclerView chatList;

    public ChatListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<ChatListScreenComponent>getDaggerComponent(context).inject(this);
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
        UIHelper.getInstance().setTypeface(addGroupText, searchText);
    }
}
