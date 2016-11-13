package sg.edu.smu.livelabs.mobicom.busEvents;

import sg.edu.smu.livelabs.mobicom.models.data.ChatMessageEntity;

/**
 * Created by Aftershock PC on 1/8/2015.
 */
public class ChatMessageSentEvent {
    public ChatMessageEntity chatMessageEntity;
    public boolean result;
    public String msg;

    public ChatMessageSentEvent(ChatMessageEntity chatMessageEntity,
                                boolean result, String msg) {
        this.chatMessageEntity = chatMessageEntity;
        this.result = result;
        this.msg = msg;
    }

}
