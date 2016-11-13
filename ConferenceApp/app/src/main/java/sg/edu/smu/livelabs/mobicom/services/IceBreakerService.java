package sg.edu.smu.livelabs.mobicom.services;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import sg.edu.smu.livelabs.mobicom.models.data.IceBreakerFriendsEntity;
import sg.edu.smu.livelabs.mobicom.models.data.IceBreakerFriendsEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.IceBreakerLeaderBoardEntity;
import sg.edu.smu.livelabs.mobicom.models.data.IceBreakerLeaderBoardEntityDao;
import sg.edu.smu.livelabs.mobicom.net.item.FriendDetailFromQRItem;
import sg.edu.smu.livelabs.mobicom.net.item.IceBreakerDetailItem;
import sg.edu.smu.livelabs.mobicom.net.item.IceBreakerLeaderBoardItem;

/**
 * Created by smu on 28/2/16.
 */
public class IceBreakerService extends GeneralService {
    private static final IceBreakerService instance = new IceBreakerService();
    public static IceBreakerService getInstance(){return instance;}
    private IceBreakerFriendsEntityDao iceBreakerFriendsEntityDao;
    private IceBreakerLeaderBoardEntityDao iceBreakerLeaderBoardEntityDao;

    public void init(Context context){
        this.context = context;
        iceBreakerFriendsEntityDao = DatabaseService.getInstance().getIceBreakerFriendsEntityDao();
        iceBreakerLeaderBoardEntityDao = DatabaseService.getInstance().getIceBreakerLeaderBoardEntityDao();
    }

    public List<IceBreakerFriendsEntity> updateFriendList(List<IceBreakerDetailItem> friendsItems){
        List<IceBreakerFriendsEntity> iceBreakerFriendsEntities = new ArrayList<IceBreakerFriendsEntity>();

        try{
            for(IceBreakerDetailItem friendsItem: friendsItems){
                IceBreakerFriendsEntity iceBreakerFriendsEntity = new IceBreakerFriendsEntity();
                iceBreakerFriendsEntity.setId(Long.parseLong(friendsItem.friendId));
                iceBreakerFriendsEntity.setUserId(Long.parseLong(friendsItem.friendId));
                iceBreakerFriendsEntity.setName(friendsItem.name);
                iceBreakerFriendsEntity.setAvatarId(friendsItem.avatar);
                iceBreakerFriendsEntity.setEmailId(friendsItem.email);
                iceBreakerFriendsEntity.setDesig(friendsItem.organisation);
                iceBreakerFriendsEntity.setQrCode(friendsItem.qrCode);
                iceBreakerFriendsEntities.add(iceBreakerFriendsEntity);
            }

            iceBreakerFriendsEntityDao.deleteAll(); //safe clean
            iceBreakerFriendsEntityDao.insertOrReplaceInTx(iceBreakerFriendsEntities);
        }
        catch (Exception e){

        }
        return iceBreakerFriendsEntities;
    }

    //adding one friend
    public void addFriend(FriendDetailFromQRItem friendsItem){

        try{
            IceBreakerFriendsEntity iceBreakerFriendsEntity = new IceBreakerFriendsEntity();
            iceBreakerFriendsEntity.setId(Long.parseLong(friendsItem.userId));
            iceBreakerFriendsEntity.setUserId(Long.parseLong(friendsItem.userId));
            iceBreakerFriendsEntity.setName(friendsItem.name);
            iceBreakerFriendsEntity.setAvatarId(friendsItem.avatar);
            iceBreakerFriendsEntity.setEmailId(friendsItem.email);
            iceBreakerFriendsEntity.setDesig(friendsItem.desig);
            iceBreakerFriendsEntity.setQrCode(friendsItem.qrCode);

            iceBreakerFriendsEntityDao.insertOrReplace(iceBreakerFriendsEntity);
        }
        catch (Exception e){

        }
    }

    public List<IceBreakerFriendsEntity> getAllFriends(){
        return iceBreakerFriendsEntityDao.queryBuilder().orderAsc(IceBreakerFriendsEntityDao.Properties.Name).list();
    }

    public List<IceBreakerLeaderBoardEntity> updateleaderBoard(List<IceBreakerLeaderBoardItem> iceBreakerLeaderBoardItems){
        List<IceBreakerLeaderBoardEntity> iceBreakerLeaderBoardEntities = new ArrayList<IceBreakerLeaderBoardEntity>();

        try{
            for(IceBreakerLeaderBoardItem leaderBoardItem: iceBreakerLeaderBoardItems){
                IceBreakerLeaderBoardEntity leaderBoard = new IceBreakerLeaderBoardEntity();
                leaderBoard.setId(Long.parseLong(leaderBoardItem.userId));
                leaderBoard.setUserId(Long.parseLong(leaderBoardItem.userId));
                leaderBoard.setName(leaderBoardItem.name);
                leaderBoard.setAvatarId(leaderBoardItem.avatar);
                leaderBoard.setEmailId(leaderBoardItem.email);
                leaderBoard.setDesig(leaderBoardItem.desig);
                leaderBoard.setCount(Integer.parseInt(leaderBoardItem.count));
                iceBreakerLeaderBoardEntities.add(leaderBoard);
            }

            iceBreakerLeaderBoardEntityDao.deleteAll(); //safe clean
            iceBreakerLeaderBoardEntityDao.insertOrReplaceInTx(iceBreakerLeaderBoardEntities);
        }
        catch (Exception e){

        }

        return iceBreakerLeaderBoardEntities;
    }

    public List<IceBreakerLeaderBoardEntity> getLeaderBoard(){
        return iceBreakerLeaderBoardEntityDao.queryBuilder().orderDesc(IceBreakerLeaderBoardEntityDao.Properties.Count).list();
    }

    public boolean isFriend(FriendDetailFromQRItem user){
        List<IceBreakerFriendsEntity> i = iceBreakerFriendsEntityDao.queryBuilder()
                .where(IceBreakerFriendsEntityDao.Properties.Id.eq(Long.parseLong(user.userId))).list();

        if(i != null && i.size() > 0){
            return true;
        }
        return false;
    }
}
