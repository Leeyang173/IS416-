package sg.edu.smu.livelabs.mobicom;

import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import sg.edu.smu.livelabs.mobicom.util.Util;

/**
 * Created by Aftershock PC on 9/7/2015.
 */
public class
        ActionBarController {
    private Toolbar toolbar;
    private MainActivity activity;

    private ImageButton backBtn;
    private ImageButton menuIconBtn;
    private Button menuTxtBtn;

    private CircleImageView appIconIV;
    private LinearLayout backLayout;
    private TextView appTitleTxt;

    private Button leftBtn;
    private Button rightBtn;
    private Button middleBtn;
    private LinearLayout centerControlLayout;
    private int focus;

    public ActionBarController(MainActivity activity, Toolbar toolbar, UIHelper uiHelper) {
        this.toolbar = toolbar;
        this.activity = activity;
        this.backBtn = (ImageButton) toolbar.findViewById(R.id.back_btn);
        this.appIconIV = (CircleImageView) toolbar.findViewById(R.id.app_icon);
        this.menuIconBtn = (ImageButton) toolbar.findViewById(R.id.menu_icon_btn);
        this.menuTxtBtn = (Button) toolbar.findViewById(R.id.menu_txt_btn);
        this.appTitleTxt = (TextView) toolbar.findViewById(R.id.toolbar_title);
        this.backLayout = (LinearLayout) toolbar.findViewById(R.id.back_layout);
        leftBtn = (Button) toolbar.findViewById(R.id.left_btn);
        rightBtn = (Button) toolbar.findViewById(R.id.right_btn);
        middleBtn = (Button) toolbar.findViewById(R.id.middle_btn);
        centerControlLayout = (LinearLayout) toolbar.findViewById(R.id.center_control);
        leftBtn.setSelected(true);

        uiHelper.setExo2TypeFace(menuTxtBtn, false);
        uiHelper.setExo2TypeFace(leftBtn, false);
        uiHelper.setExo2TypeFace(rightBtn, false);
        uiHelper.setExo2TypeFace(middleBtn, false);
        uiHelper.setExo2TypeFace(appTitleTxt, true);

        this.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActionBarController.this.activity.onBackPressed();
            }
        });
        this.backLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActionBarController.this.activity.onBackPressed();
            }
        });
    }

    public void changeBackBtn(boolean enabled) {
        if (enabled) {
            backBtn.setVisibility(View.VISIBLE);
        } else {
            backBtn.setVisibility(View.GONE);
        }
    }

    public void changeIcon(boolean enabled, String imageId) {
        if (enabled) {
            if (imageId != null && !imageId.isEmpty()) {
                appIconIV.setVisibility(View.VISIBLE);
                Picasso.with(activity.getContext()).cancelRequest(appIconIV);
                Picasso.with(activity.getContext()).load(Util.getPhotoUrlFromId(imageId, 96)).noFade().placeholder(R.drawable.logo).into(appIconIV);
            } else {
                appIconIV.setImageResource(R.drawable.logo);
            }
        }else {
            appIconIV.setVisibility(View.GONE);
        }
    }

    public void changeTitle(CharSequence title) {
        if (title == null || title.length() == 0) {
            appTitleTxt.setText("");
        } else {
            appTitleTxt.setText(title);
        }
        disableCenterControl();
    }

    public void setTitleAction(final ActionBarOwner.MenuAction menuAction){
        if (menuAction != null){
            appTitleTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    menuAction.action.call();
                }
            });
        } else {
            appTitleTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }
    }

    public void setupMenu(final ActionBarOwner.MenuAction menuAction) {
        if (menuAction == null) {
            disabledMenuIconBtn();
            disableMenuTxtBtn();
        } else {
            if (menuAction.title != null && menuAction.title.length() > 0) {
                disabledMenuIconBtn();
                menuTxtBtn.setText(menuAction.title);
                menuTxtBtn.setVisibility(View.VISIBLE);
                menuTxtBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        menuAction.action.call();
                    }
                });
            } else {
                disableMenuTxtBtn();
                menuIconBtn.setVisibility(View.VISIBLE);
                menuIconBtn.setImageResource(menuAction.iconResource);
                menuIconBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        menuAction.action.call();;
                    }
                });
            }
        }
    }

    private void disableMenuTxtBtn() {
        menuTxtBtn.setVisibility(View.GONE);
        menuTxtBtn.setOnClickListener(null);
    }

    private void disabledMenuIconBtn() {
        menuIconBtn.setVisibility(View.GONE);
        menuIconBtn.setOnClickListener(null);
    }

    private void disableCenterControl(){
        appTitleTxt.setVisibility(View.VISIBLE);
        centerControlLayout.setVisibility(View.GONE);
        leftBtn.setOnClickListener(null);
        rightBtn.setOnClickListener(null);
        middleBtn.setOnClickListener(null);

    }

    public void enableCenterControl(final ActionBarOwner.MenuAction left, final ActionBarOwner.MenuAction middle,
                                    final ActionBarOwner.MenuAction right, int focus){
        centerControlLayout.setVisibility(View.VISIBLE);
        appTitleTxt.setVisibility(View.GONE);
        if (left != null){
            leftBtn.setVisibility(View.VISIBLE);
            leftBtn.setText(left.title);
            leftBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    leftBtn.setSelected(true);
                    middleBtn.setSelected(false);
                    rightBtn.setSelected(false);
                    left.action.call();
                }
            });
        } else {
            leftBtn.setVisibility(View.GONE);
        }
        if(middle != null){
            middleBtn.setVisibility(View.VISIBLE);
            middleBtn.setText(middle.title);
            middleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    leftBtn.setSelected(false);
                    middleBtn.setSelected(true);
                    rightBtn.setSelected(false);
                    middle.action.call();
                }
            });
        } else {
            middleBtn.setVisibility(View.GONE);
        }
        if(rightBtn != null){
            rightBtn.setVisibility(View.VISIBLE);
            rightBtn.setText(right.title);
            rightBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    leftBtn.setSelected(false);
                    middleBtn.setSelected(false);
                    rightBtn.setSelected(true);
                    right.action.call();
                }
            });
        } else {
            rightBtn.setVisibility(View.GONE);
        }
        switch (focus){
            case ActionBarOwner.Config.LEFT_FOCUS:
                leftBtn.setSelected(true);
                middleBtn.setSelected(false);
                rightBtn.setSelected(false);
                break;
            case ActionBarOwner.Config.MIDDLE_FOCUS:
                leftBtn.setSelected(false);
                middleBtn.setSelected(true);
                rightBtn.setSelected(false);
                break;
            case ActionBarOwner.Config.RIGHT_FOCUS:
                leftBtn.setSelected(false);
                middleBtn.setSelected(false);
                rightBtn.setSelected(true);
                break;
            default:
                leftBtn.setSelected(false);
                middleBtn.setSelected(false);
                rightBtn.setSelected(false);
                break;
        }
    }

    public void unselectAll(){
        leftBtn.setSelected(false);
        middleBtn.setSelected(false);
        rightBtn.setSelected(false);
    }

    public void reset(){
        leftBtn.setSelected(true);
        middleBtn.setSelected(false);
        rightBtn.setSelected(false);
    }
}
