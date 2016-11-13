package sg.edu.smu.livelabs.mobicom.busEvents;

import sg.edu.smu.livelabs.mobicom.models.data.ChatRoomEntity;

/**
 * Created by Aftershock PC on 1/8/2015.
 */
public class ChatRoomMessageSynced {
    public ChatRoomEntity chatRoomEntity;
    public boolean result;

    public ChatRoomMessageSynced(ChatRoomEntity chatRoomEntity, boolean result) {
        this.chatRoomEntity = chatRoomEntity;
        this.result = result;
    }
}
