package sg.edu.smu.livelabs.mobicom.busEvents;

import sg.edu.smu.livelabs.mobicom.models.data.ChatRoomEntity;

/**
 * Created by Aftershock PC on 27/7/2015.
 */
public class ChatRoomLeft {
    public ChatRoomEntity chatRoomEntity;
    public String error;

    public ChatRoomLeft(ChatRoomEntity chatRoomEntity, String error) {
        this.chatRoomEntity = chatRoomEntity;
        this.error = error;
    }
}
