package sg.edu.smu.livelabs.mobicom.adapters;

/**
 * Created by johnlee on 23/3/16.
 */
import android.widget.ListAdapter;


import java.util.List;

import sg.edu.smu.livelabs.mobicom.models.Interest;

public interface DemoAdapter extends ListAdapter {

    void appendItems(List<Interest> newItems);

    void setItems(List<Interest> moreItems);
}
