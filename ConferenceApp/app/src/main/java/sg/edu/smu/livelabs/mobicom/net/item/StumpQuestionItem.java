package sg.edu.smu.livelabs.mobicom.net.item;

import com.google.gson.annotations.SerializedName;

/**
 * Created by smu on 15/1/16.
 */
public class StumpQuestionItem {
    @SerializedName("ques_title")
    public String title;
    @SerializedName("ques_options")
    public String quesOptions;
    public String answer;
    public StumpQuestionItem() {
    }

    public String[] getQuesOption(){
        if(quesOptions != null){
            return quesOptions.split(",");
        }

        return null;
    }
}
