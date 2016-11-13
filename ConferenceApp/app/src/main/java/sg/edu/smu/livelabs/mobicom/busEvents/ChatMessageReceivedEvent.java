package sg.edu.smu.livelabs.mobicom.busEvents;

import sg.edu.smu.livelabs.mobicom.models.data.ChatMessageEntity;
import sg.edu.smu.livelabs.mobicom.net.ChatMessageNoti;

/**
 * Created by Aftershock PC on 28/7/2015.
 */
public class ChatMessageReceivedEvent {
    public ChatMessageNoti chatMessageNoti;
    public ChatMessageEntity chatMessageEntity;
    public ChatMessageReceivedEvent(ChatMessageNoti chatMessageNoti, ChatMessageEntity chatMessageEntity) {
        this.chatMessageNoti = chatMessageNoti;
        this.chatMessageEntity = chatMessageEntity;
    }
}
