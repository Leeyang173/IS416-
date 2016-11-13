package sg.edu.smu.livelabs.mobicom.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.Bind;
import butterknife.ButterKnife;
import in.co.madhur.chatbubblesdemo.AndroidUtilities;
import in.co.madhur.chatbubblesdemo.Constants;
import in.co.madhur.chatbubblesdemo.NotificationCenter;
import in.co.madhur.chatbubblesdemo.widgets.Emoji;
import in.co.madhur.chatbubblesdemo.widgets.EmojiView;
import in.co.madhur.chatbubblesdemo.widgets.SizeNotifierRelativeLayout;
import rx.functions.Action1;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.DaggerService;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.ChatMessagesAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.MainActivityPauseEvent;
import sg.edu.smu.livelabs.mobicom.presenters.ChatPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ChatScreenComponent;
import sg.edu.smu.livelabs.mobicom.util.Util;

/**
 * Created by Aftershock PC on 21/7/2015.
 */
@AutoInjector(ChatPresenter.class)
public class ChatView extends LinearLayout implements SizeNotifierRelativeLayout.SizeNotifierRelativeLayoutDelegate, NotificationCenter.NotificationCenterDelegate {
    @Bind(R.id.chat_list_view)
    public UltimateRecyclerView chatListView;
    @Bind(R.id.chat_edit_text1)
    public EditText chatEditText1;
    @Bind(R.id.enter_chat1)
    public ImageView enterChatView1;
    @Bind(R.id.emojiButton)
    public ImageView emojiButton;
    @Bind(R.id.chat_layout)
    public SizeNotifierRelativeLayout sizeNotifierRelativeLayout;
    @Bind(R.id.send_chat_layout)
    public View sendChatLayout;
    @Bind(R.id.info_disable)
    public TextView disableText;

    private int keyboardHeight;
    private boolean keyboardVisible;
    private MainActivity mainActivity;
    private boolean showingEmoji;
    private EmojiView emojiView;
    private WindowManager.LayoutParams windowLayoutParams;
//    public ChatListAdapter listAdapter;
    public ChatMessagesAdapter listAdapter;

    @Inject
    ChatPresenter presenter;

    public ChatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DaggerService.<ChatScreenComponent>getDaggerComponent(getContext()).inject(this);
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        AndroidUtilities.statusBarHeight = Util.getStatusBarHeight(getContext());
        chatEditText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showingEmoji)
                    hideEmojiPopup();
            }
        });
        emojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndShowEmoJiPopup();
//                showEmojiPopup(!showingEmoji);
            }
        });
//        listAdapter = new ChatListAdapter(new ArrayList<ChatMessage>(), getContext());
//        chatListView.setAdapter(listAdapter);
        sizeNotifierRelativeLayout.delegate = this;
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
        presenter.takeView(this);
        presenter.getBus().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        presenter.dropView(this);
        hideAllKeyboards();
        presenter.getBus().unregister(this);
        super.onDetachedFromWindow();

    }

    @Subscribe public void mainActivityPaused(MainActivityPauseEvent event) {
        hideAllKeyboards();
    }

    private void hideAllKeyboards() {
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
        removeEmojiWindow();
        chatEditText1.clearFocus();
        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(chatEditText1.getWindowToken(), 0);
    }

    @Override
    protected void onFinishInflate() {
        ButterKnife.bind(this);
        super.onFinishInflate();
        UIHelper.getInstance().setTypeface(disableText);
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.emojiDidLoaded) {
            if (emojiView != null) {
                emojiView.invalidateViews();
            }

            if (chatListView != null) {
                chatListView.invalidate();
            }
        }
    }

    @Override
    public void onSizeChanged(int height) {
        Rect localRect = new Rect();
        mainActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);

        WindowManager wm = (WindowManager) App.getInstance().getSystemService(Activity.WINDOW_SERVICE);
        if (wm == null || wm.getDefaultDisplay() == null) {
            return;
        }

        if (height > AndroidUtilities.dp(50) && keyboardVisible) {
            keyboardHeight = height;
            App.getInstance().getSharedPreferences("emoji", 0).edit().putInt("kbd_height", keyboardHeight).commit();
        }


        if (showingEmoji) {
            int newHeight = 0;

            newHeight = keyboardHeight;

            if (windowLayoutParams.width != AndroidUtilities.displaySize.x || windowLayoutParams.height != newHeight) {
                windowLayoutParams.width = AndroidUtilities.displaySize.x;
                windowLayoutParams.height = newHeight;

                wm.updateViewLayout(emojiView, windowLayoutParams);
                if (!keyboardVisible) {
                    sizeNotifierRelativeLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            if (sizeNotifierRelativeLayout != null) {
                                sizeNotifierRelativeLayout.setPadding(0, 0, 0, windowLayoutParams.height);
                                sizeNotifierRelativeLayout.requestLayout();
                            }
                        }
                    });
                }
            }
        }


        boolean oldValue = keyboardVisible;
        keyboardVisible = height > 0;
        if (keyboardVisible && sizeNotifierRelativeLayout.getPaddingBottom() > 0) {
            showEmojiPopup(false);
        } else if (!keyboardVisible && keyboardVisible != oldValue && showingEmoji) {
            showEmojiPopup(false);
        }
    }

    private void showEmojiPopup(final boolean show) {
        showingEmoji = show;

        if (show) {
            if (emojiView == null) {
                emojiView = new EmojiView(mainActivity);

                emojiView.setListener(new EmojiView.Listener() {
                    public void onBackspace() {
                        chatEditText1.dispatchKeyEvent(new KeyEvent(0, 67));
                    }

                    public void onEmojiSelected(String symbol) {
                        int i = chatEditText1.getSelectionEnd();
                        if (i < 0) {
                            i = 0;
                        }
                        try {
                            CharSequence localCharSequence = Emoji.replaceEmoji(symbol, chatEditText1.getPaint().getFontMetricsInt(), AndroidUtilities.dp(20));
                            chatEditText1.setText(chatEditText1.getText().insert(i, localCharSequence));
                            int j = i + localCharSequence.length();
                            chatEditText1.setSelection(j, j);
                        } catch (Exception e) {
                            Log.e(Constants.TAG, "Error showing emoji");
                        }
                    }
                });


                windowLayoutParams = new WindowManager.LayoutParams();
                windowLayoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
                if (Build.VERSION.SDK_INT >= 21) {
                    windowLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
                } else {
                    windowLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
                    windowLayoutParams.token = mainActivity.getWindow().getDecorView().getWindowToken();
                }
                windowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            }

            final int currentHeight;

            if (keyboardHeight <= 0)
                keyboardHeight = App.getInstance().getSharedPreferences("emoji", 0).getInt("kbd_height", AndroidUtilities.dp(200));

            currentHeight = keyboardHeight;

            WindowManager wm = (WindowManager) App.getInstance().getSystemService(Activity.WINDOW_SERVICE);

            windowLayoutParams.height = currentHeight;
            windowLayoutParams.width = AndroidUtilities.displaySize.x;

            try {
                if (emojiView.getParent() != null) {
                    wm.removeViewImmediate(emojiView);
                }
            } catch (Exception e) {
                Log.e(Constants.TAG, e.getMessage());
            }

            try {
                wm.addView(emojiView, windowLayoutParams);
            } catch (Exception e) {
                Log.e(Constants.TAG, e.getMessage());
                return;
            }

            if (!keyboardVisible) {
                if (sizeNotifierRelativeLayout != null) {
                    sizeNotifierRelativeLayout.setPadding(0, 0, 0, currentHeight);
                }
                return;
            }
        }
        else {
            removeEmojiWindow();
            if (sizeNotifierRelativeLayout != null) {
                sizeNotifierRelativeLayout.post(new Runnable() {
                    public void run() {
                        if (sizeNotifierRelativeLayout != null) {
                            sizeNotifierRelativeLayout.setPadding(0, 0, 0, 0);
                        }
                    }
                });
            }
        }
    }

    private void removeEmojiWindow() {
        if (emojiView == null) {
            return;
        }
        try {
            if (emojiView.getParent() != null) {
                WindowManager wm = (WindowManager) App.getInstance().getSystemService(Context.WINDOW_SERVICE);
                wm.removeViewImmediate(emojiView);
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, e.getMessage());
        }
    }

    public void hideEmojiPopup() {
        if (showingEmoji) {
            showEmojiPopup(false);
        }
    }

    public void scrollToBottom() {
        chatListView.scrollVerticallyToPosition(listAdapter.getItemCount() - 1);
        int height = windowLayoutParams == null ? AndroidUtilities.displaySize.y : windowLayoutParams.height;
        chatListView.showToolbar(mainActivity.getToolbar(), chatListView, height);
    }

    private void checkPermissionAndShowEmoJiPopup(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(mainActivity)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + mainActivity.getPackageName()));
                mainActivity.startActivityForResult(intent, MainActivity.OVERLAY_PERMISSION_REQ_CODE);
                mainActivity.permissionCallback = new Action1<String>() {
                    @Override
                    public void call(String s) {
                        showEmojiPopup(!showingEmoji);
                    }
                };
            } else {
                showEmojiPopup(!showingEmoji);
            }
        } else {
            showEmojiPopup(!showingEmoji);
        }
    }
}
