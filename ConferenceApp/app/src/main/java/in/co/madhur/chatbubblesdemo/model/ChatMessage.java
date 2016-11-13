package in.co.madhur.chatbubblesdemo.model;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by madhur on 17/01/15.
 */
public class ChatMessage {
    private String sender;
    private String messageText;
    private UserType userType;
    private Status messageStatus;
    private Date messageDate;

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(messageTime);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        messageDate = c.getTime();
    }

    private long messageTime;

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public void setMessageStatus(Status messageStatus) {
        this.messageStatus = messageStatus;
    }

    public String getMessageText() {

        return messageText;
    }

    public UserType getUserType() {
        return userType;
    }

    public Status getMessageStatus() {
        return messageStatus;
    }

    public Date getMessageDate() {
        return messageDate;
    }
}
