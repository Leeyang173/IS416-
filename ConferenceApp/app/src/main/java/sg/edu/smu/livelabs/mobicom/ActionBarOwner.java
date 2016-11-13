package sg.edu.smu.livelabs.mobicom;

import android.content.Context;
import android.os.Bundle;

import mortar.Presenter;
import mortar.bundler.BundleService;
import rx.functions.Action0;

import static mortar.bundler.BundleService.getBundleService;

/**
 * Created by Aftershock PC on 9/7/2015.
 */
public class ActionBarOwner extends Presenter<ActionBarOwner.Activity> {
    public interface Activity {
        void setShowHomeEnabled(boolean enabled, String homeImageId);

        void setUpButtonEnabled(boolean enabled);

        void setTitle(CharSequence title, MenuAction titleAction, MenuAction leftAction,
                      MenuAction middleAction, MenuAction rightAction, int focus);

        void setMenu(MenuAction action);

        Context getContext();
    }

    public static class Config {
        public static final int LEFT_FOCUS = 0;
        public static final int MIDDLE_FOCUS = 1;
        public static final int RIGHT_FOCUS = 2;
        private static final String DEFAULT_TITLE = "Mobicom";
        public final boolean showHomeEnabled;
        public final String homeImageId;
        public final boolean upButtonEnabled;
        public final CharSequence title;
        public final MenuAction action;
        public final MenuAction titleAction;
        public final MenuAction leftAction;
        public final MenuAction middleAction;
        public final MenuAction rightAction;
        public final int focus;

        public Config(boolean upButtonEnabled, String homeImageId, MenuAction leftAction, MenuAction rightAction,
                      MenuAction action) {
            this.upButtonEnabled = upButtonEnabled;
            this.titleAction = null;
            this.title = null;
            this.leftAction = leftAction;
            this.rightAction = rightAction;
            this.middleAction = null;
            this.action = action;
            this.focus = -1;
            if (homeImageId != null && !homeImageId.isEmpty()){
                this.showHomeEnabled = true;
                this.homeImageId = homeImageId;
            } else {
                this.showHomeEnabled = false;
                this.homeImageId = "";
            }
        }

        public Config(boolean upButtonEnabled, String homeImageId, MenuAction leftAction, MenuAction rightAction,
                      MenuAction action, int focus) {
            this.upButtonEnabled = upButtonEnabled;
            this.titleAction = null;
            this.title = null;
            this.leftAction = leftAction;
            this.rightAction = rightAction;
            this.middleAction = null;
            this.action = action;
            this.focus = focus;
            if (homeImageId != null && !homeImageId.isEmpty()){
                this.showHomeEnabled = true;
                this.homeImageId = homeImageId;
            } else {
                this.showHomeEnabled = false;
                this.homeImageId = "";
            }
        }

        public Config(boolean upButtonEnabled, MenuAction leftAction, MenuAction middleAction,
                      MenuAction rightAction, int focus) {
            this.upButtonEnabled = upButtonEnabled;
            this.titleAction = null;
            this.leftAction = leftAction;
            this.rightAction = rightAction;
            this.middleAction = middleAction;
            this.action = null;
            this.title = null;
            this.showHomeEnabled = false;
            this.homeImageId = "";
            this.focus = focus;
        }

        public Config(boolean upButtonEnabled, MenuAction leftAction, MenuAction rightAction, int focus) {
            this.upButtonEnabled = upButtonEnabled;
            this.titleAction = null;
            this.leftAction = leftAction;
            this.middleAction = null;
            this.rightAction = rightAction;
            this.action = null;
            this.title = null;
            this.showHomeEnabled = false;
            this.homeImageId = "";
            this.focus = focus;
        }

        public Config(boolean upButtonEnabled, String homeImageId, MenuAction titleAction,
                      MenuAction action) {
            this.upButtonEnabled = upButtonEnabled;
            this.titleAction = titleAction;
            this.rightAction = null;
            this.middleAction = null;
            this.leftAction = null;
            this.action = action;
            this.focus = -1;
            if (titleAction != null){
                this.title = titleAction.title;
            } else {
                this.title = DEFAULT_TITLE;
            }
            if (homeImageId != null && !homeImageId.isEmpty()){
                this.showHomeEnabled = true;
                this.homeImageId = homeImageId;
            } else {
                this.showHomeEnabled = false;
                this.homeImageId = "";
            }
        }

        public Config(boolean upButtonEnabled, CharSequence title,
                      MenuAction action) {
            this.showHomeEnabled = false;
            this.homeImageId = "";
            this.upButtonEnabled = upButtonEnabled;
            this.title = title;
            this.action = action;
            this.titleAction = null;
            this.rightAction = null;
            this.leftAction = null;
            this.middleAction = null;
            this.focus = -1;
        }

        public Config(boolean upButtonEnabled,
                      MenuAction action) {
            this.showHomeEnabled = false;
            this.homeImageId = "";
            this.upButtonEnabled = upButtonEnabled;
            this.title = DEFAULT_TITLE;
            this.action = action;
            this.titleAction = null;
            this.rightAction = null;
            this.middleAction = null;
            this.leftAction = null;
            this.focus = -1;
        }

        public Config(boolean upButtonEnabled) {
            this.showHomeEnabled = false;
            this.homeImageId = "";
            this.upButtonEnabled = upButtonEnabled;
            this.title = DEFAULT_TITLE;
            this.action = null;
            this.titleAction = null;
            this.rightAction = null;
            this.middleAction = null;
            this.leftAction = null;
            this.focus = -1;
        }


        public Config withAction(MenuAction action) {
            return new Config(upButtonEnabled, title, action);
        }
    }

    public static class MenuAction {
        public CharSequence title;
        public int iconResource;
        public final Action0 action;

        public MenuAction(CharSequence title, Action0 action) {
            this.title = title;
            this.action = action;
        }

        public MenuAction(int iconResource, Action0 action) {
            this.iconResource = iconResource;
            this.action = action;
        }
    }

    private Config config;

    public ActionBarOwner() {
    }

    @Override
    public void onLoad(Bundle savedInstanceState) {
        if (config != null) update();
    }

    public void setConfig(Config config) {
        this.config = config;
        update();
    }

    public Config getConfig() {
        return config;
    }

    @Override
    protected BundleService extractBundleService(Activity activity) {
        return getBundleService(activity.getContext());
    }

    private void update() {
        if (!hasView()) return;
        Activity activity = getView();

        activity.setShowHomeEnabled(config.showHomeEnabled, config.homeImageId);
        activity.setUpButtonEnabled(config.upButtonEnabled);
        activity.setMenu(config.action);
        activity.setTitle(config.title, config.titleAction,
                config.leftAction, config.middleAction, config.rightAction, config.focus);
    }
}
