package sg.edu.smu.livelabs.mobicom.services;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.Query;
import sg.edu.smu.livelabs.mobicom.models.data.SurveyEntity;
import sg.edu.smu.livelabs.mobicom.models.data.SurveyEntityDao;
import sg.edu.smu.livelabs.mobicom.net.item.SurveyItem;

/**
 * Created by smu on 28/2/16.
 */
public class SurveyService extends GeneralService {
    private static final SurveyService instance = new SurveyService();
    public static SurveyService getInstance(){return instance;}
    private SurveyEntityDao surveyEntityDao;

    public void init(Context context){
        this.context = context;
        surveyEntityDao = DatabaseService.getInstance().getSurveyEntityDao();
    }

    public List<SurveyEntity> updateSurveyList(List<SurveyItem> surveyItems){
        List<SurveyEntity> surveyEntities = new ArrayList<SurveyEntity>();

        try{
            for(SurveyItem survey: surveyItems){
                SurveyEntity surveyEntity = new SurveyEntity();
                surveyEntity.setId(Long.parseLong(survey.id));
                surveyEntity.setEndTime(survey.endTime);
                surveyEntity.setStartTime(survey.startTime);
                surveyEntity.setStatus(survey.status);
                surveyEntity.setTitle(survey.title);
                surveyEntity.setType(survey.type);
                surveyEntities.add(surveyEntity);
            }

            surveyEntityDao.deleteAll();
            surveyEntityDao.insertOrReplaceInTx(surveyEntities);
        }
        catch (Exception e){

        }

        return surveyEntities;
    }

    public SurveyEntity getSurvey(long surveyId){
        Query<SurveyEntity> query = surveyEntityDao.queryBuilder().where(SurveyEntityDao.Properties.Id.eq(surveyId)).limit(1).build();
        List<SurveyEntity> surveyEntities = query.list();
        if (surveyEntities.size() > 0) {
            return surveyEntities.get(0);
        }
        return null;
    }

    public SurveyEntity getSurvey(){
        Query<SurveyEntity> query = surveyEntityDao.queryBuilder()
                .orderDesc(SurveyEntityDao.Properties.LastUpdated).limit(1).build();
        List<SurveyEntity> surveyEntities = query.list();
        if (surveyEntities.size() > 0) {
            return surveyEntities.get(0);
        }
        return null;
    }

    public List<SurveyEntity> getSurveys(){
        return surveyEntityDao.queryBuilder().list();
    }
}
