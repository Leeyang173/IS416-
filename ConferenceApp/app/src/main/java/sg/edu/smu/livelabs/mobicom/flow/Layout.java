package sg.edu.smu.livelabs.mobicom.flow;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by Aftershock PC on 30/6/2015.
 */
@Retention(RUNTIME) @Target(TYPE)
public @interface Layout {
    int value();
}
