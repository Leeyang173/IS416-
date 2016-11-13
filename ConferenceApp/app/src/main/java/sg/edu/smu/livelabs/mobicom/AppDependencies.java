package sg.edu.smu.livelabs.mobicom;

import com.squareup.otto.Bus;

import sg.edu.smu.livelabs.mobicom.net.RestClient;


/**
 * Created by Aftershock PC on 30/6/2015.
 */
public interface AppDependencies {
    RestClient restClient();
    Bus bus();
}
