package sg.edu.smu.livelabs.mobicom.mortar;

import android.content.Context;
import android.util.AttributeSet;

import flow.path.Path;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.flow.FramePathContainerView;
import sg.edu.smu.livelabs.mobicom.flow.SimplePathContainer;

/**
 * Created by Aftershock PC on 30/6/2015.
 */
public class MortarPathContainerView extends FramePathContainerView {

    public MortarPathContainerView(Context context, AttributeSet attrs) {
        super(context, attrs, new SimplePathContainer(R.id.screen_switcher_tag, Path.contextFactory(new BasicMortarContextFactory(new ScreenScoper()))));
    }
}
