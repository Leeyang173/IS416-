package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Date;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.busEvents.MemoriesResetHomeEvent;
import sg.edu.smu.livelabs.mobicom.flow.HandlesBack;
import sg.edu.smu.livelabs.mobicom.presenters.MemoriesMainPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.MemoriesMainScreenComponent;

/**
 * Created by smu on 26/10/15.
 */
@AutoInjector(MemoriesMainPresenter.class)
public class MemoriesMainView extends RelativeLayout implements HandlesBack {
    @Inject
    public MemoriesMainPresenter presenter;
    @Bind(R.id.viewpager)
    public NoneSwipeableViewPager pager;

    @Bind(R.id.date0)
    public View date0;
    @Bind(R.id.date1)
    public View date1;
    @Bind(R.id.date2)
    public View date2;
    @Bind(R.id.date3)
    public View date3;
    @Bind(R.id.date4)
    public View date4;
    @Bind(R.id.date5)
    public View date5;
    @Bind(R.id.date6)
    public View date6;
    public DateView[] dateViews;

    private DateView selectedView;

    public MemoriesMainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<MemoriesMainScreenComponent>getDaggerComponent(context).inject(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        presenter.takeView(this);
        presenter.getBus().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        presenter.dropView(this);
        presenter.getBus().unregister(this);
    }

    @Override
    protected void onFinishInflate() {
        ButterKnife.bind(this);
        super.onFinishInflate();
        dateViews = new DateView[]{
                new DateView(date0, 0),
                new DateView(date1, 1),
                new DateView(date2, 2),
                new DateView(date3, 3),
                new DateView(date4, 4),
                new DateView(date5, 5),
                new DateView(date6, 6)};

    }

    @Override
    public boolean onBackPressed() {
        return presenter.goBack();
    }

    public class DateView implements View.OnClickListener{
        public LinearLayout linearLayout;
        public TextView weekdayTV;
        public TextView monthDayTV;
        private int index;
        public Date date;

        public DateView(View view, int index){
            this.index = index;
            this.linearLayout = (LinearLayout) view;
            weekdayTV = (TextView) view.findViewById(R.id.weekday);
            monthDayTV = (TextView) view.findViewById(R.id.month_day);
            UIHelper.getInstance().setTypeface(weekdayTV, monthDayTV);
            view.setOnClickListener(this);
        }

        public void setData(String weekday, String monthDay, boolean enable, Date date){
            weekdayTV.setText(weekday);
            monthDayTV.setText(monthDay);
            setEnabled(enable);
            this.date = date;

        }

        public void setSelected(boolean selected){
            weekdayTV.setSelected(selected);
            monthDayTV.setSelected(selected);
            linearLayout.setSelected(selected);
        }

        public void setEnabled(boolean enable){
            weekdayTV.setEnabled(enable);
            monthDayTV.setEnabled(enable);
            linearLayout.setEnabled(enable);
            linearLayout.setClickable(enable);
        }

        @Override
        public void onClick(View v) {
            if (!linearLayout.isSelected()){
//                presenter.changeTo(index);
//                MemoriesService.getInstance().currentPromotion.id="2";
                presenter.getBus().post(new MemoriesResetHomeEvent( date,index));
            }
        }
    }
}
