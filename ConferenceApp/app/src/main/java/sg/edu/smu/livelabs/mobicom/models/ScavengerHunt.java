package sg.edu.smu.livelabs.mobicom.models;

import java.util.Date;

/**
 * Created by johnlee on 22/2/16.
 */
public class ScavengerHunt {

    private int huntId;
    private String huntTitle;
    private String huntDetails;
    private String huntMap;
    private Date startTime;
    private Date endTime;
    private boolean status; //for completed or not
    private String huntProfilePhoto;
    private String summary;

    public ScavengerHunt() {
    }

    public ScavengerHunt(int huntId, String huntTitle, String huntDetails,  String huntMap, Date startTime, Date endTime, boolean status, String huntProfilePhoto, String summary) {
        this.huntId = huntId;
        this.huntTitle = huntTitle;
        this.huntDetails = huntDetails;
        this.huntMap = huntMap;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.huntProfilePhoto = huntProfilePhoto;
        this.summary = summary;
    }

    public int getHuntId() {
        return huntId;
    }

    public void setHuntId(int huntId) {
        this.huntId = huntId;
    }

    public String getHuntTitle() {
        return huntTitle;
    }

    public void setHuntTitle(String huntTitle) {
        this.huntTitle = huntTitle;
    }

    public String getHuntDetails() {
        return huntDetails;
    }

    public void setHuntDetails(String huntDetails) {
        this.huntDetails = huntDetails;
    }

    public String getHuntMap() {
        return huntMap;
    }

    public void setHuntMap(String huntMap) {
        this.huntMap = huntMap;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getHuntProfilePhoto() {
        return huntProfilePhoto;
    }

    public void setHuntProfilePhoto(String huntProfilePhoto) {
        this.huntProfilePhoto = huntProfilePhoto;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
