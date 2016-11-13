package sg.edu.smu.livelabs.mobicom.net.response;

import java.util.List;

import sg.edu.smu.livelabs.mobicom.net.item.IceBreakerDetailItem;

/**
 * Created by smu on 15/1/16.
 */
public class IceBreakerGetFriendListResponse {
    public String status;
    public Integer total_users;
    public List<IceBreakerDetailItem> friends;

}
