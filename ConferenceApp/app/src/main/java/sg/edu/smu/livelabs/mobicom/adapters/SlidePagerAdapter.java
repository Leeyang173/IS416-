package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import flow.path.Path;
import mortar.MortarScope;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.mortar.ScreenScoper;

/**
 * @author Lukasz Piliszczuk - lukasz.pili@gmail.com
 */
public class SlidePagerAdapter extends PagerAdapter {

    private final Context context;
    private final List<Path> screens;
    private final ScreenScoper screenScoper;

    private boolean viewCacheEnabled;
    private List<View> cachedViews;

    public SlidePagerAdapter(Context context, Path... screens) {
        this.context = context;
        this.screens = Arrays.asList(screens);
        screenScoper = new ScreenScoper();
    }

    public void enableViewCache(boolean enabled) {
        viewCacheEnabled = enabled;
        if (enabled) {
            cachedViews = new ArrayList<>(getCount());
            for (int i = 0; i < getCount(); i++) {
                cachedViews.add(null);
            }
        } else {
            cachedViews = null;
        }
    }

    @Override
    public int getCount() {
        return screens.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View newChild = null;
        if (viewCacheEnabled && cachedViews != null) {
            newChild = cachedViews.get(position);
        }
        if (newChild == null) {
            Path screen = screens.get(position);
            String scopeName = String.format("%s_%d", screen.getClass().getName(), position);
            MortarScope screenScope = screenScoper.getScreenScope(context, scopeName, screen);
            Context screenContext = screenScope.createContext(context);

            Layout layout = screen.getClass().getAnnotation(Layout.class);
            if (layout == null) {
                throw new IllegalStateException("@Layout annotation is missing on screen");
            }

            newChild = LayoutInflater.from(screenContext).inflate(layout.value(), container, false);
            container.addView(newChild);
            if (viewCacheEnabled) {
                cachedViews.set(position, newChild);
            }
        }
        return newChild;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (!viewCacheEnabled) {
            View view = ((View) object);
            MortarScope screenScope = MortarScope.getScope(view.getContext());
            container.removeView(view);
            screenScope.destroy();
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }
}
