package sg.edu.smu.livelabs.mobicom;

/**
 * Created by johnlee on 16/3/16.
 */


import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import sg.edu.smu.livelabs.mobicom.services.MasterPointService;

public class ToolTipsWindow {

    private static final int MSG_DISMISS_TOOLTIP = 100;
    private Context ctx;
    private PopupWindow tipWindow;
    private View contentView;
    private LayoutInflater inflater;
    private MasterPointService.TooltipCount callback;

    public ToolTipsWindow(Context ctx) {
        this.ctx = ctx;
        tipWindow = new PopupWindow(ctx);

        inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.tooltips, null);
    }

    public ToolTipsWindow(Context ctx, String msg) {
        this.ctx = ctx;
        tipWindow = new PopupWindow(ctx);

        inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.tooltips, null);
        TextView msgTV = (TextView)contentView.findViewById(R.id.tooltip_text);
        msgTV.setText(msg);
    }

    public ToolTipsWindow(Context ctx, String msg, MasterPointService.TooltipCount callback) {
        this.ctx = ctx;
        tipWindow = new PopupWindow(ctx);
        this.callback = callback;

        inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.tooltips, null);
        TextView msgTV = (TextView)contentView.findViewById(R.id.tooltip_text);
        msgTV.setText(msg);
    }

    /**
     *
     * @param width: screen width
     * @param height: screen height
     */
    public void showToolTip(final View view, int width, int height) {
        tipWindow.setHeight(LayoutParams.WRAP_CONTENT);
        tipWindow.setWidth(LayoutParams.WRAP_CONTENT);

        tipWindow.setContentView(contentView);


        // Call view measure to calculate how big your view should be.
        contentView.measure(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        int contentViewHeight = contentView.getMeasuredHeight();
        int contentViewWidth = contentView.getMeasuredWidth();
        // In this case , i dont need much calculation for x and y position of
        // tooltip
        // For cases if anchor is near screen border, you need to take care of
        // direction as well
        // to show left, right, above or below of anchor view
        final int position_x = width - contentViewWidth;
        final int position_y = (int) (height * 1) - contentViewHeight;

        tipWindow.setBackgroundDrawable(new BitmapDrawable());

        tipWindow.setAnimationStyle(R.style.animationName);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                try {
                    tipWindow.showAtLocation(view, Gravity.NO_GRAVITY, position_x, position_y);
                }catch(Exception e){
                    Log.d(App.APP_TAG, e.toString());
                }
            }
        }, 1500);


        tipWindow.setOutsideTouchable(false);
        tipWindow.setTouchable(true);
        tipWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                dismissTooltip();
                return false;
            }
        });
        tipWindow.setFocusable(false);
        // send message to handler to dismiss tipWindow after X milliseconds
        handler.sendEmptyMessageDelayed(MSG_DISMISS_TOOLTIP, 4000);
    }


    public void showToolTip(View view) {
        tipWindow.setHeight(LayoutParams.WRAP_CONTENT);
        tipWindow.setWidth(LayoutParams.WRAP_CONTENT);

        tipWindow.setContentView(contentView);


// Call view measure to calculate how big your view should be.
        contentView.measure(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        int contentViewHeight = contentView.getMeasuredHeight();
        int contentViewWidth = contentView.getMeasuredWidth();
// In this case , i dont need much calculation for x and y position of
// tooltip
// For cases if anchor is near screen border, you need to take care of
// direction as well
// to show left, right, above or below of anchor view
        int position_x = view.getWidth() - contentViewWidth;
        int position_y = (int) (view.getHeight() * 1) - contentViewHeight;

        tipWindow.setBackgroundDrawable(new BitmapDrawable());

        tipWindow.setAnimationStyle(R.style.animationName);
        tipWindow.showAtLocation(view, Gravity.NO_GRAVITY, position_x,
                position_y);

        tipWindow.setOutsideTouchable(false);
        tipWindow.setTouchable(true);
        tipWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                dismissTooltip();
                return false;
            }
        });
        tipWindow.setFocusable(false);



// send message to handler to dismiss tipWindow after X milliseconds
        handler.sendEmptyMessageDelayed(MSG_DISMISS_TOOLTIP, 2000);
    }

    public boolean isTooltipShown() {
        if (tipWindow != null && tipWindow.isShowing())
            return true;
        return false;
    }

    public void dismissTooltip() {
        if (tipWindow != null && tipWindow.isShowing()) {
            try {
                tipWindow.dismiss();
                if(callback != null) {
                    callback.reduceCallBack();
                }
            }catch (Exception e){
                Log.d(App.APP_TAG, e.toString());
            }
        }
    }

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_DISMISS_TOOLTIP:
                    if (tipWindow != null && tipWindow.isShowing()) {
                        try {
                            tipWindow.dismiss();
                            if (callback != null) {
                                callback.reduceCallBack();
                            }
                        }catch (Exception e){
                            Log.d(App.APP_TAG, e.toString());
                        }
                    }
                    break;
            }
        };
    };

}
