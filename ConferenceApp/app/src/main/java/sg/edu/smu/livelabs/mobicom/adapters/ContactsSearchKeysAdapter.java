package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.models.data.SearchKeyEntity;

/**
 * Created by smu on 14/3/16.
 */
public class ContactsSearchKeysAdapter extends BaseAdapter {
    private Context context;
    private List<SearchKeyEntity> searchKeyEntities;
    public ContactsSearchKeysAdapter(Context context){
        this.context = context;
        this.searchKeyEntities = new ArrayList<>();
    }

    public void setData(List<SearchKeyEntity> searchKeyEntities){
        if (searchKeyEntities != null){
            this.searchKeyEntities.clear();
            this.searchKeyEntities.addAll(searchKeyEntities);
        }
    }
    @Override
    public int getCount() {
        return searchKeyEntities.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder VH;
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.contacts_search_item, parent, false);
            VH = new ViewHolder(convertView);
            convertView.setTag(VH);
        }
        VH = (ViewHolder) convertView.getTag();
        VH.setData(searchKeyEntities.get(position));
        return convertView;
    }

    public class ViewHolder implements View.OnClickListener{
        @Bind(R.id.key_text)
        public TextView keyText;
        @Bind(R.id.checked_btn)
        public ImageView checkedBtn;
        private SearchKeyEntity data;

        public ViewHolder(View view){
            ButterKnife.bind(this, view);
            UIHelper.getInstance().setTypeface(keyText);
            view.setOnClickListener(this);
        }

        public void setData(SearchKeyEntity data){
            this.data = data;
            keyText.setText(data.getKey());
            checkedBtn.setSelected(data.getChecked());
        }

        @Override
        public void onClick(View v) {
            if (data.getChecked()){
                data.setChecked(false);
                checkedBtn.setSelected(false);
            } else {
                data.setChecked(true);
                checkedBtn.setSelected(true);
            }
        }
    }
}
