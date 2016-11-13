package sg.edu.smu.livelabs.mobicom.busEvents;


import sg.edu.smu.livelabs.mobicom.models.data.ChatRoomEntity;

/**
 * Created by Aftershock PC on 27/7/2015.
 */
public class ChatRoomUpdatedEvent {
    public ChatRoomEntity chatRoomEntity;
    public String error;

    public ChatRoomUpdatedEvent(ChatRoomEntity chatRoomEntity) {
        this.chatRoomEntity = chatRoomEntity;
    }

    public ChatRoomUpdatedEvent(ChatRoomEntity chatRoomEntity, String error) {
        this.chatRoomEntity = chatRoomEntity;
        this.error = error;
    }
}
