package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.presenters.ContactsPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ContactsScreenComponent;

/**
 * Created by smu on 8/3/16.
 */
@AutoInjector(ContactsPresenter.class)
public class ContactsView extends RelativeLayout {
    @Inject
    public ContactsPresenter presenter;

    @Bind(R.id.search_text)
    public EditText searchText;
    @Bind(R.id.search_filter_list)
    public ListView searchFilterListview;

    @Bind(R.id.search_key_layout)
    public RelativeLayout searchKeyLayout;
    @Bind(R.id.contact_list)
    public UltimateRecyclerView contactListview;

    public ContactsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<ContactsScreenComponent>getDaggerComponent(context).inject(this);
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
        UIHelper.getInstance().setTypeface(searchText);
    }
}
