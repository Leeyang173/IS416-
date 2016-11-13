package sg.edu.smu.livelabs.mobicom.busEvents;

import sg.edu.smu.livelabs.mobicom.models.data.ChatMessageEntity;

/**
 * Created by Aftershock PC on 1/8/2015.
 */
public class ChatMessageDeleted {
    public ChatMessageEntity entity;

    public ChatMessageDeleted(ChatMessageEntity entity) {
        this.entity = entity;
    }
}
