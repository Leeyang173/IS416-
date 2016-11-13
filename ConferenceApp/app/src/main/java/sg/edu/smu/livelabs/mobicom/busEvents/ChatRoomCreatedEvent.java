package sg.edu.smu.livelabs.mobicom.busEvents;

import sg.edu.smu.livelabs.mobicom.models.data.ChatRoomEntity;

/**
 * Created by Aftershock PC on 27/7/2015.
 */
public class ChatRoomCreatedEvent {
    public ChatRoomEntity chatRoomEntity;
    public String error;

    public ChatRoomCreatedEvent(ChatRoomEntity chatRoomEntity) {
        this.chatRoomEntity = chatRoomEntity;
    }

    public ChatRoomCreatedEvent(ChatRoomEntity chatRoomEntity, String error) {
        this.chatRoomEntity = chatRoomEntity;
        this.error = error;
    }
}
