package sg.edu.smu.livelabs.mobicom.net.api;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;
import sg.edu.smu.livelabs.mobicom.net.response.SurveyResponse;

/**
 * Created by smu on 22/1/16.
 */
public interface SurveyApi {

    @FormUrlEncoded
    @POST("get_all_surveys")
    Observable<SurveyResponse> getSurvey(@Field("last_modified_time") String lastModifiedDate, @Field("user_id") String userId);

    @FormUrlEncoded
    @POST("get_survey_details")
    Observable<SurveyResponse> getSurveyDetails(@Field("user_id") String userId, @Field("survey_id") String surveyId);
}
