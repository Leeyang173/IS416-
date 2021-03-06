package sg.edu.smu.livelabs.mobicom.models.data;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "EVENT_ENTITY".
 */
public class EventEntity {

    private Long id;
    private Long serverId;
    private String title;
    private String description;
    private String keynoteUserId;
    private Long eventDate;
    private java.util.Date startTime;
    private java.util.Date endTime;
    private String room;
    private Long parentId;
    private Long paperId;
    private String eventType;
    private String status;
    private Double rating;
    private Double correctAnswers;
    private Double commentCount;
    private Integer myRate;
    private Boolean checked;
    private Boolean synced;
    private String topicHandle;
    private String keynoteUserDetail;
    private String ratingQuizStatus;
    private Boolean myLike;
    private Integer likesCount;

    public EventEntity() {
    }

    public EventEntity(Long id) {
        this.id = id;
    }

    public EventEntity(Long id, Long serverId, String title, String description, String keynoteUserId, Long eventDate, java.util.Date startTime, java.util.Date endTime, String room, Long parentId, Long paperId, String eventType, String status, Double rating, Double correctAnswers, Double commentCount, Integer myRate, Boolean checked, Boolean synced, String topicHandle, String keynoteUserDetail, String ratingQuizStatus, Boolean myLike, Integer likesCount) {
        this.id = id;
        this.serverId = serverId;
        this.title = title;
        this.description = description;
        this.keynoteUserId = keynoteUserId;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.parentId = parentId;
        this.paperId = paperId;
        this.eventType = eventType;
        this.status = status;
        this.rating = rating;
        this.correctAnswers = correctAnswers;
        this.commentCount = commentCount;
        this.myRate = myRate;
        this.checked = checked;
        this.synced = synced;
        this.topicHandle = topicHandle;
        this.keynoteUserDetail = keynoteUserDetail;
        this.ratingQuizStatus = ratingQuizStatus;
        this.myLike = myLike;
        this.likesCount = likesCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKeynoteUserId() {
        return keynoteUserId;
    }

    public void setKeynoteUserId(String keynoteUserId) {
        this.keynoteUserId = keynoteUserId;
    }

    public Long getEventDate() {
        return eventDate;
    }

    public void setEventDate(Long eventDate) {
        this.eventDate = eventDate;
    }

    public java.util.Date getStartTime() {
        return startTime;
    }

    public void setStartTime(java.util.Date startTime) {
        this.startTime = startTime;
    }

    public java.util.Date getEndTime() {
        return endTime;
    }

    public void setEndTime(java.util.Date endTime) {
        this.endTime = endTime;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getPaperId() {
        return paperId;
    }

    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Double getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(Double correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public Double getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Double commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getMyRate() {
        return myRate;
    }

    public void setMyRate(Integer myRate) {
        this.myRate = myRate;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public Boolean getSynced() {
        return synced;
    }

    public void setSynced(Boolean synced) {
        this.synced = synced;
    }

    public String getTopicHandle() {
        return topicHandle;
    }

    public void setTopicHandle(String topicHandle) {
        this.topicHandle = topicHandle;
    }

    public String getKeynoteUserDetail() {
        return keynoteUserDetail;
    }

    public void setKeynoteUserDetail(String keynoteUserDetail) {
        this.keynoteUserDetail = keynoteUserDetail;
    }

    public String getRatingQuizStatus() {
        return ratingQuizStatus;
    }

    public void setRatingQuizStatus(String ratingQuizStatus) {
        this.ratingQuizStatus = ratingQuizStatus;
    }

    public Boolean getMyLike() {
        return myLike;
    }

    public void setMyLike(Boolean myLike) {
        this.myLike = myLike;
    }

    public Integer getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }

}
