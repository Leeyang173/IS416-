package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.models.data.SurveyEntity;
import sg.edu.smu.livelabs.mobicom.views.SurveyView;

/**
 * Created by johnlee on 18/2/16.
 */
public class SurveyListAdapter extends BaseAdapter {

    private  List<SurveyEntity> surveys;
    private SurveyView surveyView;

    public SurveyListAdapter(SurveyView surveyView, List<SurveyEntity> surveys){
        this.surveyView = surveyView;
        this.surveys = surveys;

    }

    @Override
    public int getCount() {
        return surveys.size();
    }

    @Override
    public SurveyEntity getItem(int position) {
        if(position == 0){
            return null;
        }
        return surveys.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {

        return getCount();
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            LayoutInflater vi = (LayoutInflater)surveyView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.list_item_survey, parent, false);

            // set value
            TextView surveyTitle = (TextView) view.findViewById(R.id.title);
            surveyTitle.setText(surveys.get(position).getTitle());

            Date currentDate= new java.util.Date();

            if(surveys.get(position).getEndTime() != null &&  currentDate.after(surveys.get(position).getEndTime())){ //null will be for survey that are not from BEP
                view.setEnabled(false);
                surveyTitle.setText("[Expired] " + surveys.get(position).getTitle());
                surveyTitle.setTextColor(surveyView.getContext().getResources().getColor(R.color.grey));
            }
        }

        return view;
    }


    public void updates(List<SurveyEntity> surveys){
        this.surveys = surveys;
        this.notifyDataSetChanged();
        this.notifyDataSetInvalidated();
    }

}
