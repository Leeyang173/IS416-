package sg.edu.smu.livelabs.mobicom.services;

import android.content.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sg.edu.smu.livelabs.mobicom.models.data.ScavengerEntity;
import sg.edu.smu.livelabs.mobicom.models.data.ScavengerEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.ScavengerGroupDetailEntity;
import sg.edu.smu.livelabs.mobicom.models.data.ScavengerGroupDetailEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.ScavengerWinnersEntity;
import sg.edu.smu.livelabs.mobicom.models.data.ScavengerWinnersEntityDao;
import sg.edu.smu.livelabs.mobicom.net.item.RecentWinnersItem;
import sg.edu.smu.livelabs.mobicom.net.item.ScavengerHuntItem;
import sg.edu.smu.livelabs.mobicom.net.item.UserProfileItem;

/**
 * Created by smu on 28/2/16.
 */
public class ScavengerService extends GeneralService {
    private static final ScavengerService instance = new ScavengerService();
    public static ScavengerService getInstance(){return instance;}
    private ScavengerGroupDetailEntityDao scavengerGroupDetailEntityDao;
    private ScavengerEntityDao scavengerEntityDao;
    private ScavengerWinnersEntityDao scavengerWinnersEntityDao;

    public void init(Context context){
        this.context = context;
        scavengerGroupDetailEntityDao = DatabaseService.getInstance().getScavengerGroupDetailEntityDao();
        scavengerEntityDao = DatabaseService.getInstance().getScavengerEntityDao();
        scavengerWinnersEntityDao = DatabaseService.getInstance().getScavengerWinnersEntityDAO();
    }

    public List<ScavengerEntity> updateScavengerHuntList(List<ScavengerHuntItem> scavengerHunts){
        List<ScavengerEntity> scavengerEntitiesExisting = scavengerEntityDao.queryBuilder().build().list();
        List<ScavengerEntity> scavengerEntities = new ArrayList<ScavengerEntity>();
        List<ScavengerWinnersEntity> scavengerWinnersEntities = new ArrayList<>();

        if(scavengerEntitiesExisting.size() > 0){ //means there are hunt that are loaded
//            deleteHunts(scavengerHunts);

            for(ScavengerHuntItem s: scavengerHunts){
                boolean isExisted = false; //to indicate whether this hunt had been loaded before
                for(ScavengerEntity hunt: scavengerEntitiesExisting){
                    if(hunt.getId() == Long.parseLong(s.id)){//update existing hunt
                        hunt.setId(Long.parseLong(s.id));
                        hunt.setDescription(s.description);
                        hunt.setTitle(s.title);
                        hunt.setEndTime(s.endTime);
                        hunt.setHuntId(Long.parseLong(s.id));
                        hunt.setIconId(s.icon);
                        if(s.hasCompleted.toLowerCase().equals("true")) {
                            hunt.setIsCompleted(true);
                        }
                        else{
                            hunt.setIsCompleted(false);
                        }
                        //dun mess up with any existing status
//                        hunt.setIsStarted(false);
//                        hunt.setIsSubmitted(false);
                        hunt.setStartTime(s.startTime);
                        hunt.setPhoto(s.hintImage);
                        hunt.setType(s.type);
                        hunt.setUserReequiredCount(Integer.parseInt(s.userRequiredCount));
                        hunt.setQrCode(s.qrCode);
                        hunt.setInsertTime(s.insertTime);
                        hunt.setLastUpdate(s.lastModifiedTime);
                        scavengerEntities.add(hunt);
                        isExisted = true;
                        break;
                    }
                }

                if(!isExisted){ //new hunt
                    ScavengerEntity scavengerEntity = new ScavengerEntity();
                    scavengerEntity.setId(Long.parseLong(s.id));
                    scavengerEntity.setDescription(s.description);
                    scavengerEntity.setTitle(s.title);
                    scavengerEntity.setEndTime(s.endTime);
                    scavengerEntity.setHuntId(Long.parseLong(s.id));
                    scavengerEntity.setIconId(s.icon);
                    scavengerEntity.setIsCompleted(false);
                    scavengerEntity.setIsStarted(false);
                    scavengerEntity.setIsSubmitted(false);
                    scavengerEntity.setStartTime(s.startTime);
                    scavengerEntity.setPhoto(s.hintImage);
                    scavengerEntity.setType(s.type);
                    scavengerEntity.setUserReequiredCount(Integer.parseInt(s.userRequiredCount));
                    scavengerEntity.setQrCode(s.qrCode);
                    scavengerEntity.setInsertTime(s.insertTime);
                    scavengerEntity.setLastUpdate(s.lastModifiedTime);
                    scavengerEntities.add(scavengerEntity);
                }

                for(RecentWinnersItem r : s.recentWinners){
                    for(UserProfileItem winner: r.users){
                        ScavengerWinnersEntity winnersEntity = new ScavengerWinnersEntity(Long.parseLong(s.id),
                                winner.name, winner.email, winner.avatar);
                        scavengerWinnersEntities.add(winnersEntity);
                    }
                }
            }
            updateWinners(scavengerWinnersEntities);
            scavengerEntityDao.deleteAll();
            scavengerEntityDao.insertOrReplaceInTx(scavengerEntities);

        }
        else{

            for(ScavengerHuntItem s: scavengerHunts){
                ScavengerEntity scavengerEntity = new ScavengerEntity();
                scavengerEntity.setId(Long.parseLong(s.id));
                scavengerEntity.setDescription(s.description);
                scavengerEntity.setTitle(s.title);
                scavengerEntity.setEndTime(s.endTime);
                scavengerEntity.setHuntId(Long.parseLong(s.id));
                scavengerEntity.setIconId(s.icon);
                if(s.hasCompleted.toLowerCase().equals("true")) {
                    scavengerEntity.setIsCompleted(true);
                }
                else{
                    scavengerEntity.setIsCompleted(false);
                }
                scavengerEntity.setIsStarted(false);
                scavengerEntity.setIsSubmitted(false);
                scavengerEntity.setStartTime(s.startTime);
                scavengerEntity.setPhoto(s.hintImage);
                scavengerEntity.setType(s.type);
                scavengerEntity.setUserReequiredCount(Integer.parseInt(s.userRequiredCount));
                scavengerEntity.setQrCode(s.qrCode);
                scavengerEntity.setInsertTime(s.insertTime);
                scavengerEntity.setLastUpdate(s.lastModifiedTime);
                scavengerEntities.add(scavengerEntity);

                for(RecentWinnersItem r : s.recentWinners){
                    for(UserProfileItem winner: r.users){
                        ScavengerWinnersEntity winnersEntity = new ScavengerWinnersEntity(Long.parseLong(s.id),
                                winner.name, winner.email, winner.avatar);
                        scavengerWinnersEntities.add(winnersEntity);
                    }
                }
            }
            updateWinners(scavengerWinnersEntities);
            scavengerEntityDao.insertOrReplaceInTx(scavengerEntities);


        }

        return scavengerEntities;
    }

    public void deleteHunts(List<ScavengerHuntItem> newHunts){
        List<ScavengerEntity> hunts = getAllScavengerHunt();//current existing hunt in local DB
        List<ScavengerEntity> huntsToDelete = new ArrayList<>();
        List<ScavengerWinnersEntity> huntsWinnerToDelete = new ArrayList<>();
        for(ScavengerEntity h: hunts){
            boolean isNotThere = true;
            for(ScavengerHuntItem newHunt :newHunts) {
                if (h.getHuntId() == Long.parseLong(newHunt.id)){
                    isNotThere = false;
                    break;
                }
            }

            if(isNotThere) {
                huntsWinnerToDelete.addAll(getWinnersByHunt(h.getHuntId()));
                huntsToDelete.add(h);
            }
        }

        deleteHuntsWinnerst(huntsWinnerToDelete);
        scavengerEntityDao.deleteInTx(huntsToDelete);
    }

    public void updateWinners(List<ScavengerWinnersEntity> scavengerWinnersEntities){
        scavengerWinnersEntityDao.deleteAll();
        scavengerWinnersEntityDao.insertOrReplaceInTx(scavengerWinnersEntities);
    }

    public List<ScavengerWinnersEntity> getWinnersByHunt(long huntId){
        List<ScavengerWinnersEntity> w =  scavengerWinnersEntityDao.queryBuilder().where(ScavengerWinnersEntityDao.Properties.HuntId.eq(huntId)).build().list();
        return w;
    }

    public void deleteHuntsWinnerst(List<ScavengerWinnersEntity> toDelete){
        scavengerWinnersEntityDao.deleteInTx(toDelete);
    }

    public void updateScavengerHuntList2(List<ScavengerEntity> scavengerHunts){
            scavengerEntityDao.insertOrReplaceInTx(scavengerHunts);
    }

    public List<ScavengerEntity> getAllScavengerHunt(){
        return scavengerEntityDao.queryBuilder().list();
    }

    public ScavengerEntity getScavengerHunt(long huntId){
        List<ScavengerEntity> scavengerEntities = scavengerEntityDao.queryBuilder().where(ScavengerEntityDao.Properties.HuntId.eq(huntId))
                .limit(1).build().list();
        if(scavengerEntities != null && scavengerEntities.size() >0 ){
            return scavengerEntities.get(0);
        }
        return null;
    }

    public ScavengerEntity getLastUpdateScavengerHunt(){
        List<ScavengerEntity> s =  scavengerEntityDao.queryBuilder().orderDesc(ScavengerEntityDao.Properties.LastUpdate).limit(1).build().list();
        if(s != null && s.size() > 0){
            return s.get(0);
        }
        return null;
    }

    /**
     * Update the db, so when user reopen the hunt, we can know that this hunt had already form the group and the hunt is going on until end time
     * @param huntId
     * @param isStarted
     */
    public void updateIsStarted(long huntId, boolean isStarted){
        List<ScavengerEntity> scavengerEntities = scavengerEntityDao.queryBuilder()
                .where(ScavengerEntityDao.Properties.HuntId.eq(huntId)).limit(1).build().list();
        if(scavengerEntities!= null && scavengerEntities.size() > 0){
            ScavengerEntity s = scavengerEntities.get(0);
            s.setIsStarted(isStarted);
            scavengerEntityDao.insertOrReplace(s);
        }
    }


    /**
     * To get from db whether the hunt started or not
     */
    public boolean isHuntStarted(long huntId){
        List<ScavengerEntity> scavengerEntities = scavengerEntityDao.queryBuilder()
                .where(ScavengerEntityDao.Properties.HuntId.eq(huntId)).limit(1).build().list();

        if(scavengerEntities != null && scavengerEntities.size() > 0){
            return scavengerEntities.get(0).getIsStarted();
        }
        return false;
    }


    /**
     * Update the db, whether the current user have scanned/submitted the qr code or not
     * @param huntId
     * @param isSubmitted
     */
    public void updateIsSubmitted(long huntId, boolean isSubmitted){
        List<ScavengerEntity> scavengerEntities = scavengerEntityDao.queryBuilder()
                .where(ScavengerEntityDao.Properties.HuntId.eq(huntId)).limit(1).build().list();
        if(scavengerEntities!= null && scavengerEntities.size() > 0){
            ScavengerEntity s = scavengerEntities.get(0);
            s.setIsSubmitted(isSubmitted);
            scavengerEntityDao.insertOrReplace(s);
        }
    }

    /**
     * To get from db whether the hunt started or not
     */
    public boolean isHuntSubmitted(long huntId){
        List<ScavengerEntity> scavengerEntities = scavengerEntityDao.queryBuilder()
                .where(ScavengerEntityDao.Properties.HuntId.eq(huntId)).limit(1).build().list();

        if(scavengerEntities != null && scavengerEntities.size() > 0){
            return scavengerEntities.get(0).getIsSubmitted();
        }
        return false;
    }

    /**
     * Update to db that the hunt have been completed by user
     * @param huntId
     */
    public void updateIsComplete(long huntId, boolean isCompleted){
        List<ScavengerEntity> scavengerEntities = scavengerEntityDao.queryBuilder()
                .where(ScavengerEntityDao.Properties.HuntId.eq(huntId)).limit(1).build().list();
        if(scavengerEntities!= null && scavengerEntities.size() > 0){
            ScavengerEntity s = scavengerEntities.get(0);
            s.setIsCompleted(isCompleted);
            scavengerEntityDao.insertOrReplace(s);
        }
    }

    public ScavengerGroupDetailEntity getMember(long userId, long huntId){
        List<ScavengerGroupDetailEntity> scavengerGroupDetailEntities = scavengerGroupDetailEntityDao.queryBuilder().where(ScavengerGroupDetailEntityDao.Properties.HuntId.eq(huntId),
                ScavengerGroupDetailEntityDao.Properties.UserId.eq(userId)).limit(1).build().list();

        if(scavengerGroupDetailEntities != null && scavengerGroupDetailEntities.size() > 0){
            return scavengerGroupDetailEntities.get(0);
        }
        return null;
    }

    /**
     * Add member to the hunt
     * @param huntId
     * @param groupId
     */
    public void addGroupMember(long huntId, long groupId, long userId, String name, String avatarId, boolean isSubmitted){

        //before adding need to check whether user have been added to the local db or not
        //if so, need to remove it
        List<ScavengerGroupDetailEntity> duplicate = scavengerGroupDetailEntityDao.queryBuilder().where(
                ScavengerGroupDetailEntityDao.Properties.HuntId.eq(huntId),
                ScavengerGroupDetailEntityDao.Properties.UserId.eq(userId)).build().list();
        System.out.println("Add to group DB " + duplicate.size() + " >>>> " + name + " userId: " + userId);
        scavengerGroupDetailEntityDao.deleteInTx(duplicate);
//        for(ScavengerGroupDetailEntity duplicate: scavengerGroupDetailEntities){
//            scavengerGroupDetailEntityDao.delete(duplicate);
//        }

        Date date = new Date();

        ScavengerGroupDetailEntity s = new ScavengerGroupDetailEntity();
        s.setHuntId(huntId);
        s.setAvatarId(avatarId);
        s.setUserId(userId);
        s.setName(name);
        s.setGroupId(groupId);
        s.setIsSubmitted(isSubmitted);
        s.setInsertTime(date);

        scavengerGroupDetailEntityDao.insertOrReplace(s);



        List<ScavengerGroupDetailEntity> ss = scavengerGroupDetailEntityDao.queryBuilder()
                .where(ScavengerGroupDetailEntityDao.Properties.HuntId.eq(huntId))
                .build().list();
        for(ScavengerGroupDetailEntity t: ss){
            System.out.println("What left in db >>>>> "  + t.getHuntId() + " " + t.getUserId() + " " +  t.getName());
        }
    }

    /**
     * To update the member (group id) after calling addGroupMember (only after QR code scanning) as the group id is given by server
     * @param huntId
     * @param userId
     * @param groupId
     */
    public void updateGroupMember(long huntId, long userId, long groupId, boolean isSubmitted){
        List<ScavengerGroupDetailEntity> scavengerGroupDetailEntities = scavengerGroupDetailEntityDao.queryBuilder().where(ScavengerGroupDetailEntityDao.Properties.HuntId.eq(huntId),
                ScavengerGroupDetailEntityDao.Properties.UserId.eq(userId)).limit(1).build().list();

        if(scavengerGroupDetailEntities!= null && scavengerGroupDetailEntities.size() >0){
            ScavengerGroupDetailEntity s = scavengerGroupDetailEntities.get(0);
            scavengerGroupDetailEntityDao.delete(s);
            s.setGroupId(groupId);
            s.setIsSubmitted(isSubmitted);
            scavengerGroupDetailEntityDao.insertOrReplace(s);
        }
    }

    public List<ScavengerGroupDetailEntity> getGroupMemberOfTheHunt(long huntId){
        return scavengerGroupDetailEntityDao.queryBuilder().where(ScavengerGroupDetailEntityDao.Properties.HuntId.eq(huntId)).build().list();
    }

    public boolean isEveryoneDone(long huntId){
        List<ScavengerGroupDetailEntity> s = scavengerGroupDetailEntityDao.queryBuilder().where(ScavengerGroupDetailEntityDao.Properties.HuntId.eq(huntId)).build().list();
        if(s.size() > 1){ //ensure group already formed
            boolean isAllSubmitted = false;
            int count = 0;
            for(ScavengerGroupDetailEntity member: s){
                if(member.getIsSubmitted()){
                    count++;
                }
            }

            if(count >1){
                return true;
            }
            else{ //not all done yet
                return false;
            }
        }
        else{
            return false;
        }
    }

    /**
     * This is to disband all members of the hunt
     * @param huntId
     */
    public void deleteAllGroupMemberOfAHunt(long huntId){
        List<ScavengerGroupDetailEntity> s = scavengerGroupDetailEntityDao.queryBuilder().where(ScavengerGroupDetailEntityDao.Properties.HuntId.eq(huntId)).build().list();
        scavengerGroupDetailEntityDao.deleteInTx(s);
    }

    /**
     * This is to disband all members of the hunt
     * @param huntId
     */
    public void deleteGroupMemberOfAHunt(long huntId, long userId){
        List<ScavengerGroupDetailEntity> s = scavengerGroupDetailEntityDao.queryBuilder()
                .where(ScavengerGroupDetailEntityDao.Properties.HuntId.eq(huntId),
                        ScavengerGroupDetailEntityDao.Properties.UserId.eq(userId)).build().list();
        scavengerGroupDetailEntityDao.deleteInTx(s);
    }

    /**
     * This is to disband all members of the hunt
     * @param entity
     */
    public void deleteGroupMemberOfAHuntByEntity(ScavengerGroupDetailEntity entity){
        scavengerGroupDetailEntityDao.delete(entity);
    }

    public boolean hasUserSubmitted(long huntId){
        List<ScavengerEntity> s = scavengerEntityDao.queryBuilder()
                .where(ScavengerEntityDao.Properties.HuntId.eq(huntId)).build().list();

        if(s != null && s.size() > 0){
            return s.get(0).getIsSubmitted();
        }
        return false;
    }

    /**
     * This function allow call from notification (disband) to deduct the point if user have scanned before
     * as well as to reset all the status
     * @param huntId
     */
    public void deductPointWhenDisbandNotInHuntPage(long huntId){
        boolean t = isHuntSubmitted(huntId);
        if(t){
            updateIsSubmitted(huntId, false);
            updateIsComplete(huntId, false);
            updateIsStarted(huntId, false);
//            MasterPointService.getInstance().deductPoint(MasterPointService.getInstance().SCAVENGER_HUNT);
        }
    }
}
