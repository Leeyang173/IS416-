package sg.edu.smu.livelabs.mobicom.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.flyco.dialog.entity.DialogMenuItem;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.utils.CornerUtils;
import com.flyco.dialog.widget.base.BottomBaseDialog;

import java.util.ArrayList;

import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;

/**
 * Dialog like iOS ActionSheet
 */
@SuppressWarnings("deprecation")
public class MyActionSheetDialog extends BottomBaseDialog<MyActionSheetDialog> {
    private ListView mLv;
    private TextView mTvTitle;
    /** title underline*/
    private View mVLineTitle;
    /** mCancel button */
    private TextView mTvCancel;
    /** corner radius,dp */
    private float mCornerRadius;
    /** title background color */
    private int mTitleBgColor;
    /** title text */
    private String mTitle;
    /** title height */
    private float mTitleHeight;
    /** title textcolor */
    private int mTitleTextColor;
    /** title textsize */
    private float mTitleTextSize;
    /** ListView background color */
    private int mLvBgColor ;
    /** divider color(ListView divider) */
    private int mDividerColor ;
    /** divider height(ListView divider) */
    private float mDividerHeight ;
    /** item press color(ListView item) */
    private int mItemPressColor ;
    /** item textcolor(ListView item) */
    private int mItemTextColor;
    /** item textsize(ListView item) */
    private float mItemTextSize;
    /** item height(ListView item */
    private float mItemHeight;
    /** enable title show */
    private boolean mIsTitleShow;
    /*** cancel btn text*/
    private String mCancelText ;
    /** cancel btn text color */
    private int mCancelTextColor;
    /** cancel btn text size */
    private float mCancelTextSize;
    /** adapter */
    private BaseAdapter mAdapter;
    /** operation items */
    private ArrayList<DialogMenuItem> mContents = new ArrayList<>();
    private OnOperItemClickL mOnOperItemClickL;
    private LayoutAnimationController mLac;

    public void setOnOperItemClickL(OnOperItemClickL onOperItemClickL) {
        mOnOperItemClickL = onOperItemClickL;
    }

    public MyActionSheetDialog(Context context, ArrayList<DialogMenuItem> baseItems, View animateView) {
        super(context, animateView);
        mContents.addAll(baseItems);
        init();
    }

    public MyActionSheetDialog(Context context, String[] items, View animateView) {
        super(context, animateView);
        mContents = new ArrayList<>();
        for (String item : items) {
            DialogMenuItem customBaseItem = new DialogMenuItem(item, 0);
            mContents.add(customBaseItem);
        }
        init();
    }

    public MyActionSheetDialog(Context context, BaseAdapter adapter, View animateView) {
        super(context, animateView);
        mAdapter = adapter;
        init();
    }

    private void init() {
        Resources rs = getContext().getResources();
        mTitleBgColor = rs.getColor(R.color.dialog_background_color);
        mTitleTextColor = rs.getColor(R.color.dialog_title_color_default);
        mLvBgColor = mTitleBgColor;
        mDividerColor = rs.getColor(R.color.divider);
        mItemPressColor = rs.getColor(R.color.dialog_btn_press_color);
        mItemTextColor = rs.getColor(R.color.dialog_btn_txt_color_default);
        mCancelTextColor = mItemTextColor;
        mCornerRadius = 10;
        mTitle = "";
        mTitleHeight = 48;
        mTitleTextSize = 17f;
        mDividerHeight = 0.8f;
        mItemTextSize = 17f;
        mCancelTextSize = 17f;
        mItemHeight = 48;
        mIsTitleShow = false;
        mCancelText = "Cancel";
        widthScale(0.95f);
        /** LayoutAnimation */
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF,
                0f, Animation.RELATIVE_TO_SELF, 6f, Animation.RELATIVE_TO_SELF, 0);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setDuration(350);
        animation.setStartOffset(150);
        mLac = new LayoutAnimationController(animation, 0.12f);
        mLac.setInterpolator(new DecelerateInterpolator());


    }

    @Override
    public View onCreateView() {
        LinearLayout ll_container = new LinearLayout(mContext);
        ll_container.setOrientation(LinearLayout.VERTICAL);
        ll_container.setBackgroundColor(Color.TRANSPARENT);

        /** title */
        mTvTitle = new TextView(mContext);
        mTvTitle.setGravity(Gravity.CENTER);
        mTvTitle.setPadding(dp2px(10), dp2px(5), dp2px(10), dp2px(5));

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.topMargin = dp2px(20);

        ll_container.addView(mTvTitle, params);

        /** title underline */
        mVLineTitle = new View(mContext);
        ll_container.addView(mVLineTitle);

        /** listview */
        mLv = new ListView(mContext);
        mLv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1));
        mLv.setCacheColorHint(Color.TRANSPARENT);
        mLv.setFadingEdgeLength(0);
        mLv.setVerticalScrollBarEnabled(false);
        mLv.setSelector(new ColorDrawable(Color.TRANSPARENT));

        ll_container.addView(mLv);

        /** mCancel btn */
        mTvCancel = new TextView(mContext);
        mTvCancel.setGravity(Gravity.CENTER);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp2px(7);
        lp.bottomMargin = dp2px(7);
        mTvCancel.setLayoutParams(lp);

        ll_container.addView(mTvCancel);

        //font
        UIHelper.getInstance().setExo2TypeFace(mTvCancel, true);
        UIHelper.getInstance().setTypeface(mTvTitle);
        return ll_container;
    }

    @Override
    public void setUiBeforShow() {
        /** title */
        float radius = dp2px(mCornerRadius);
        mTvTitle.setHeight(dp2px(mTitleHeight));
        mTvTitle.setBackgroundDrawable(CornerUtils.cornerDrawable(mTitleBgColor, new float[]{radius, radius, radius,
                radius, 0, 0, 0, 0}));
        mTvTitle.setText(mTitle);
        mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTitleTextSize);
        mTvTitle.setTextColor(mTitleTextColor);
        mTvTitle.setVisibility(mIsTitleShow ? View.VISIBLE : View.GONE);

        /** title underline */
        mVLineTitle.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, dp2px(mDividerHeight)));
        mVLineTitle.setBackgroundColor(mDividerColor);
        mVLineTitle.setVisibility(mIsTitleShow ? View.VISIBLE : View.GONE);

        /** mCancel btn */
        mTvCancel.setHeight(dp2px(mItemHeight));
        mTvCancel.setText(mCancelText);
        mTvCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, mCancelTextSize);
        mTvCancel.setTextColor(mCancelTextColor);
        mTvCancel.setBackgroundDrawable(CornerUtils.listItemSelector(radius, mLvBgColor, mItemPressColor, 1, 0));

        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        /** listview */
        mLv.setDivider(new ColorDrawable(mDividerColor));
        mLv.setDividerHeight(dp2px(mDividerHeight));

        if (mIsTitleShow) {
            mLv.setBackgroundDrawable(CornerUtils.cornerDrawable(mLvBgColor, new float[]{0, 0, 0, 0, radius, radius, radius,
                    radius}));
        } else {
            mLv.setBackgroundDrawable(CornerUtils.cornerDrawable(mLvBgColor, radius));
        }

        if (mAdapter == null) {
            mAdapter = new ListDialogAdapter();
        }

        mLv.setAdapter(mAdapter);
        mLv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mOnOperItemClickL != null) {
                    mOnOperItemClickL.onOperItemClick(parent, view, position, id);
                }
            }
        });

        mLv.setLayoutAnimation(mLac);

    }


    public MyActionSheetDialog titleBgColor(int titleBgColor) {
        mTitleBgColor = titleBgColor;
        return this;
    }


    public MyActionSheetDialog title(String title) {
        mTitle = title;
        return this;
    }


    public MyActionSheetDialog titleHeight(float titleHeight) {
        mTitleHeight = titleHeight;
        return this;
    }

    public MyActionSheetDialog titleTextSize_SP(float titleTextSize_SP) {
        mTitleTextSize = titleTextSize_SP;
        return this;
    }

    public MyActionSheetDialog titleTextColor(int titleTextColor) {
        mTitleTextColor = titleTextColor;
        return this;
    }

    public MyActionSheetDialog isTitleShow(boolean isTitleShow) {
        mIsTitleShow = isTitleShow;
        return this;
    }

    public MyActionSheetDialog lvBgColor(int lvBgColor) {
        mLvBgColor = lvBgColor;
        return this;
    }

    public MyActionSheetDialog cornerRadius(float cornerRadius_DP) {
        mCornerRadius = cornerRadius_DP;
        return this;
    }

    public MyActionSheetDialog dividerColor(int dividerColor) {
        mDividerColor = dividerColor;
        return this;
    }

    public MyActionSheetDialog dividerHeight(float dividerHeight_DP) {
        mDividerHeight = dividerHeight_DP;
        return this;
    }

    public MyActionSheetDialog itemPressColor(int itemPressColor) {
        mItemPressColor = itemPressColor;
        return this;
    }

    public MyActionSheetDialog itemTextColor(int itemTextColor) {
        mItemTextColor = itemTextColor;
        return this;
    }

    public MyActionSheetDialog itemTextSize(float itemTextSize_SP) {
        mItemTextSize = itemTextSize_SP;
        return this;
    }

    public MyActionSheetDialog itemHeight(float itemHeight_DP) {
        mItemHeight = itemHeight_DP;
        return this;
    }

    public MyActionSheetDialog layoutAnimation(LayoutAnimationController lac) {
        mLac = lac;
        return this;
    }

    public MyActionSheetDialog cancelText(String cancelText) {
        mCancelText = cancelText;
        return this;
    }

    public MyActionSheetDialog cancelText(int cancelTextColor) {
        mCancelTextColor = cancelTextColor;
        return this;
    }

    public MyActionSheetDialog cancelTextSize(float cancelTextSize) {
        mCancelTextSize = cancelTextSize;
        return this;
    }

    class ListDialogAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mContents.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressWarnings("deprecation")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final DialogMenuItem item = mContents.get(position);

            LinearLayout llItem = new LinearLayout(mContext);
            llItem.setOrientation(LinearLayout.HORIZONTAL);
            llItem.setGravity(Gravity.CENTER_VERTICAL);

            ImageView ivItem = new ImageView(mContext);
            ivItem.setPadding(0, 0, dp2px(15), 0);
            llItem.addView(ivItem);

            TextView tvItem = new TextView(mContext);
            tvItem.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            tvItem.setSingleLine(true);
            tvItem.setGravity(Gravity.CENTER);
            tvItem.setTextColor(mItemTextColor);
            tvItem.setTextSize(TypedValue.COMPLEX_UNIT_SP, mItemTextSize);
            tvItem.setHeight(dp2px(mItemHeight));
            UIHelper.getInstance().setExo2TypeFace(tvItem, false);
            llItem.addView(tvItem);
            float radius = dp2px(mCornerRadius);
            if (mIsTitleShow) {
                llItem.setBackgroundDrawable((CornerUtils.listItemSelector(radius, Color.TRANSPARENT, mItemPressColor,
                        position == mContents.size() - 1)));
            } else {
                llItem.setBackgroundDrawable(CornerUtils.listItemSelector(radius, Color.TRANSPARENT, mItemPressColor,
                        mContents.size(), position));
            }

            ivItem.setImageResource(item.mResId);
            tvItem.setText(item.mOperName);
            ivItem.setVisibility(item.mResId == 0 ? View.GONE : View.VISIBLE);

            return llItem;
        }
    }
}
