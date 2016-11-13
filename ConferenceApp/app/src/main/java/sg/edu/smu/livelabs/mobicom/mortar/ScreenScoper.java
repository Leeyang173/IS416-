package sg.edu.smu.livelabs.mobicom.mortar;

import android.content.Context;
import android.util.Log;

import automortar.ScreenComponentFactory;
import flow.path.Path;
import mortar.MortarScope;
import sg.edu.smu.livelabs.mobicom.DaggerService;

/**
 * Created by Aftershock PC on 30/6/2015.
 */
public class ScreenScoper {

    public MortarScope getScreenScope(Context context, String name, Path path) {
        MortarScope parentScope = MortarScope.getScope(context);
        if (parentScope == null) {
            Log.d("YYY:", parentScope + "is null: " + context + ", " + name + ", " + path);
        }
        MortarScope childScope = parentScope.findChild(name);
        if (childScope != null) {
            return childScope;
        }

        if (!(path instanceof ScreenComponentFactory)) {
            throw new IllegalStateException("Path must imlement ComponentFactory");
        }
        ScreenComponentFactory screenComponentFactory = (ScreenComponentFactory) path;
        Object component = screenComponentFactory.createComponent(parentScope.getService(DaggerService.SERVICE_NAME));

        MortarScope.Builder builder = parentScope.buildChild()
                .withService(DaggerService.SERVICE_NAME, component);

        return builder.build(name);
    }
}
