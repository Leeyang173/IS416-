package sg.edu.smu.livelabs.mobicom.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sg.edu.smu.livelabs.mobicom.models.data.AttendeeEntity;
import sg.edu.smu.livelabs.mobicom.models.data.EventEntity;
import sg.edu.smu.livelabs.mobicom.models.data.GameEventEntity;
import sg.edu.smu.livelabs.mobicom.services.AgendaService;
import sg.edu.smu.livelabs.mobicom.services.AttendeesService;

/**
 * Created by smu on 26/2/16.
 */
public class AgendaEvent {

    private boolean currentEvent;
    private String startTimeStr;
    private EventEntity eventEntity;
    private List<EventEntity> subEvents;
    private AttendeeEntity attendeeEntity;
    private List<GameEventEntity> games;
    private List<AttendeeEntity> attendeeEntities;

    public AgendaEvent(){
        subEvents = new ArrayList<>();
    }

    public AgendaEvent(EventEntity eventEntity){
        startTimeStr = AgendaService.simpleTimeFormat.format(eventEntity.getStartTime());
        this.eventEntity = eventEntity;
        subEvents = new ArrayList<>();
        currentEvent = false;
    }

    public void setEventEntity(EventEntity eventEntity){
        startTimeStr = AgendaService.simpleTimeFormat.format(eventEntity.getStartTime());
        this.eventEntity = eventEntity;
    }

    public void addSubEvents(EventEntity eventEntity){
        subEvents.add(eventEntity);
    }

    public EventEntity getEventEntity(){
        return eventEntity;
    }

    public long getId() {
        return eventEntity.getId();
    }

    public String getTitle() {
        return eventEntity.getTitle();
    }

    public String getDescription() {
        return eventEntity.getDescription();
    }

    public Date getStartTime() {
        return eventEntity.getStartTime();
    }

    public String getRoom() {
        return eventEntity.getRoom();
    }

    public long getServerId() {
        return eventEntity.getServerId();
    }

    public List<EventEntity> getSubEvents() {
        return subEvents;
    }

    public String getStartTimeStr() {
        return startTimeStr;
    }

    public boolean isChecked() {
        return eventEntity.getChecked();
    }

    public boolean isMyLike() {
        return eventEntity.getMyLike();
    }

    public int getLikeCount(){
        return eventEntity.getLikesCount();
    }

    public String getEventType(){
        return eventEntity.getEventType();
    }

    public String getRate(){
        String str;
        if (eventEntity.getRating() <= 0){
            str = "N/A";
        } else {
            str = String.format("%.1f", eventEntity.getRating());
        }
        return str;
    }

    public String getCorrectAnswerNo(){
        String str;
        if (eventEntity.getCorrectAnswers() <= 0){
            str = "N/A";
        } else {
            str = String.format("%.0f", eventEntity.getCorrectAnswers()) + "%";
        }
        return str;
    }

    public String getCommentCount(){
        String str;
        if (eventEntity.getCommentCount() <= 0){
            str = "N/A";
        } else {
            str = String.format("%.0f", eventEntity.getCommentCount());
        }
        return str;
    }

    public boolean isCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(boolean currentEvent) {
        this.currentEvent = currentEvent;
    }

    public AttendeeEntity getKeynoteUser(){
        if (attendeeEntity == null ){
            String uid = eventEntity.getKeynoteUserId();
            if (uid != null && !uid.isEmpty()){
                attendeeEntity = AttendeesService.getInstance().getAttendeesByUID(Long.valueOf(uid));
            }
        }
        return attendeeEntity;
    }

    public List<AttendeeEntity> getKeynoteUsers(){
        if (attendeeEntities == null ){
            String uid = eventEntity.getKeynoteUserId();//"10029,10010,10001";
            String[] uids = uid.split(",");
            if (uids != null && uid.length() > 0){
                List<Long> uidList = new ArrayList<>();
                for(int i=0; i< uids.length; i++){
                    uidList.add(Long.valueOf(uids[i]));
                }
                attendeeEntities = AttendeesService.getInstance().getAttendeesByUIDs(uidList);
            }
        }
        return attendeeEntities;
    }

    public List<GameEventEntity> getGames(){
        if (games == null){
            games = AgendaService.getInstance().getGamesByEventID(eventEntity.getServerId());
        }
        return games;
    }

}
