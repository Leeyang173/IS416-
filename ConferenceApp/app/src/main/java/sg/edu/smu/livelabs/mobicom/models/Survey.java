package sg.edu.smu.livelabs.mobicom.models;

/**
 * Created by johnlee on 22/2/16.
 */
public class Survey {

    private int surveyId;
    private String surveyURL;
    private String surveyTitle;

    public Survey() {
    }

    public Survey(int surveyId, String surveyURL, String surveyTitle) {
        this.surveyId = surveyId;
        this.surveyURL = surveyURL;
        this.surveyTitle = surveyTitle;
    }

    public String getSurveyURL() {
        return surveyURL;
    }

    public void setSurveyURL(String surveyURL) {
        this.surveyURL = surveyURL;
    }

    public int getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(int surveyId) {
        this.surveyId = surveyId;
    }

    public String getSurveyTitle() {
        return surveyTitle;
    }

    public void setSurveyTitle(String surveyTitle) {
        this.surveyTitle = surveyTitle;
    }
}
