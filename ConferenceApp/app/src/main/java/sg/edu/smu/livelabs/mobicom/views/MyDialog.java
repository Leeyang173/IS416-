package sg.edu.smu.livelabs.mobicom.views;

/**
 * Created by smu on 2/2/16.
 */

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.flyco.dialog.utils.CornerUtils;
import com.flyco.dialog.widget.internal.BaseAlertDialog;

import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;


@SuppressWarnings("deprecation")
public class MyDialog extends BaseAlertDialog<MyDialog> {
    /** title underline */
    private View mVLineTitle;
    /** vertical line between btns */
    private View mVLineVertical;
    /** vertical line between btns */
    private View mVLineVertical2;
    /** horizontal line above btns */
    private View mVLineHorizontal;
    /** title underline color */
    private int mTitleLineColor = Color.parseColor("#61AEDC");
    /** title underline height*/
    private float mTitleLineHeight = 1f;
    /** btn divider line color */
    private int mDividerColor = Color.parseColor("#DCDCDC");

    public static final int STYLE_ONE = 0;
    public static final int STYLE_TWO = 1;
    private int mStyle = STYLE_ONE;

    public MyDialog(Context context) {
        super(context);
        Resources rs = mContext.getResources();
        /** default value*/
        mTitleTextColor = rs.getColor(R.color.dialog_title_color_default);
        mTitleTextSize = 20f;
        mContentTextColor = rs.getColor(R.color.dialog_content_color_default);
        mContentTextSize = 17f;
        mLeftBtnTextColor = rs.getColor(R.color.dialog_btn_txt_color_default);
        mRightBtnTextColor = rs.getColor(R.color.dialog_btn_txt_color_default);
        mMiddleBtnTextColor = rs.getColor(R.color.dialog_btn_txt_color_default);
        mCornerRadius = 10;
        /** default value*/
    }

    @Override
    public View onCreateView() {
        /** title */
        mTvTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mLlContainer.addView(mTvTitle);

        /** title underline */
        mVLineTitle = new View(mContext);
        mLlContainer.addView(mVLineTitle);

        /** content */
        mTvContent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mLlContainer.addView(mTvContent);

        mVLineHorizontal = new View(mContext);
        mVLineHorizontal.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        mLlContainer.addView(mVLineHorizontal);

        /** btns */
        mTvBtnLeft.setLayoutParams(new LinearLayout.LayoutParams(0, dp2px(45), 1));
        mLlBtns.addView(mTvBtnLeft);

        mVLineVertical = new View(mContext);
        mVLineVertical.setLayoutParams(new LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT));
        mLlBtns.addView(mVLineVertical);

        mTvBtnMiddle.setLayoutParams(new LinearLayout.LayoutParams(0, dp2px(45), 1));
        mLlBtns.addView(mTvBtnMiddle);

        mVLineVertical2 = new View(mContext);
        mVLineVertical2.setLayoutParams(new LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT));
        mLlBtns.addView(mVLineVertical2);

        mTvBtnRight.setLayoutParams(new LinearLayout.LayoutParams(0, dp2px(45), 1));
        mLlBtns.addView(mTvBtnRight);

        mLlContainer.addView(mLlBtns);
        //my custom
        UIHelper.getInstance().setExo2TypeFace(mTvTitle, true);
        UIHelper.getInstance().setExo2LightTypeFace(mTvBtnLeft, mTvBtnRight, mTvBtnMiddle);
        UIHelper.getInstance().setTypeface(mTvContent);


        return mLlContainer;
    }

    @Override
    public void setUiBeforShow() {
        super.setUiBeforShow();

        /** title */
        if (mStyle == STYLE_ONE) {
            mTvTitle.setMinHeight(dp2px(48));
            mTvTitle.setGravity(Gravity.CENTER_VERTICAL);
            mTvTitle.setPadding(dp2px(15), dp2px(5), dp2px(0), dp2px(5));
            mTvTitle.setVisibility(mIsTitleShow ? View.VISIBLE : View.GONE);
        } else if (mStyle == STYLE_TWO) {
            mTvTitle.setGravity(Gravity.CENTER);
            mTvTitle.setPadding(dp2px(0), dp2px(15), dp2px(0), dp2px(0));
        }

        /** title underline */
        mVLineTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                dp2px(mTitleLineHeight)));
        mVLineTitle.setBackgroundColor(mTitleLineColor);
        mVLineTitle.setVisibility(mIsTitleShow && mStyle == STYLE_ONE ? View.VISIBLE : View.GONE);

        /** content */
        if (mStyle == STYLE_ONE) {
            mTvContent.setPadding(dp2px(15), dp2px(10), dp2px(15), dp2px(10));
            mTvContent.setMinHeight(dp2px(68));
            mTvContent.setGravity(mContentGravity);
        } else if (mStyle == STYLE_TWO) {
            mTvContent.setPadding(dp2px(15), dp2px(7), dp2px(15), dp2px(20));
            mTvContent.setMinHeight(dp2px(56));
            mTvContent.setGravity(Gravity.CENTER);
        }

        /** btns */
        mVLineHorizontal.setBackgroundColor(mDividerColor);
        mVLineVertical.setBackgroundColor(mDividerColor);
        mVLineVertical2.setBackgroundColor(mDividerColor);

        if (mBtnNum == 1) {
            mTvBtnLeft.setVisibility(View.GONE);
            mTvBtnRight.setVisibility(View.GONE);
            mVLineVertical.setVisibility(View.GONE);
            mVLineVertical2.setVisibility(View.GONE);
        } else if (mBtnNum == 2) {
            mTvBtnMiddle.setVisibility(View.GONE);
            mVLineVertical.setVisibility(View.GONE);
        }

        /**set background color and corner radius */
        float radius = dp2px(mCornerRadius);
        mLlContainer.setBackgroundDrawable(CornerUtils.cornerDrawable(mBgColor, radius));
        mTvBtnLeft.setBackgroundDrawable(CornerUtils.btnSelector(radius, mBgColor, mBtnPressColor, 0));
        mTvBtnRight.setBackgroundDrawable(CornerUtils.btnSelector(radius, mBgColor, mBtnPressColor, 1));
        mTvBtnMiddle.setBackgroundDrawable(CornerUtils.btnSelector(mBtnNum == 1 ? radius : 0, mBgColor, mBtnPressColor, -1));
    }

    /** set style(设置style) */
    public MyDialog style(int style) {
        this.mStyle = style;
        return this;
    }

    /** set title underline color */
    public MyDialog titleLineColor(int titleLineColor) {
        this.mTitleLineColor = titleLineColor;
        return this;
    }

    /** set title underline height */
    public MyDialog titleLineHeight(float titleLineHeight_DP) {
        this.mTitleLineHeight = titleLineHeight_DP;
        return this;
    }

    /** set divider color between btns */
    public MyDialog dividerColor(int dividerColor) {
        this.mDividerColor = dividerColor;
        return this;
    }

    public void setLink(final MainActivity mainActivity, final String url){
        mTvContent.setClickable(true);
        mTvContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(url != null && !url.isEmpty()) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    mainActivity.startActivity(browserIntent);
                }
            }
        });
    }
}

