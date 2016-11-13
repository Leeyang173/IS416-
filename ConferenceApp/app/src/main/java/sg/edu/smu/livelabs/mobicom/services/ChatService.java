package sg.edu.smu.livelabs.mobicom.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.squareup.otto.Bus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import de.greenrobot.dao.query.LazyList;
import de.greenrobot.dao.query.QueryBuilder;
import me.kaede.tagview.Tag;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.SessionServices;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatMessageDeleted;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatMessageReceivedEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatMessageSentEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatRoomCreatedEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatRoomDeleted;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatRoomLeft;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatRoomMessageSynced;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatRoomUpdateCompletedEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatRoomUpdatedEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatRoomsSyncEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.LoginFail;
import sg.edu.smu.livelabs.mobicom.busEvents.LoginSuccessFul;
import sg.edu.smu.livelabs.mobicom.models.AttendeeTag;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.AttendeeEntity;
import sg.edu.smu.livelabs.mobicom.models.data.ChatMessageEntity;
import sg.edu.smu.livelabs.mobicom.models.data.ChatMessageEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.ChatRoomEntity;
import sg.edu.smu.livelabs.mobicom.models.data.ChatRoomEntityDao;
import sg.edu.smu.livelabs.mobicom.net.ChatMessageNoti;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.net.api.ChatApi;
import sg.edu.smu.livelabs.mobicom.net.noties.GroupInfoChangedNoti;
import sg.edu.smu.livelabs.mobicom.net.noties.GroupMemberChangedNoti;
import sg.edu.smu.livelabs.mobicom.net.response.ChatMessageSent;
import sg.edu.smu.livelabs.mobicom.net.response.ChatMessages;
import sg.edu.smu.livelabs.mobicom.net.response.ChatRoomSyncResponse;
import sg.edu.smu.livelabs.mobicom.net.response.GroupChatRoomInfo;
import sg.edu.smu.livelabs.mobicom.net.response.GroupDetailsResponse;
import sg.edu.smu.livelabs.mobicom.net.response.LoginResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse;
import sg.edu.smu.livelabs.mobicom.net.response.SimpleResponse2;
import sg.edu.smu.livelabs.mobicom.net.response.SingleChatRoomInfo;
import sg.edu.smu.livelabs.mobicom.net.response.UserResponse;

/**
 * Created by smu on 22/1/16.
 */
public class ChatService extends GeneralService{
    //friends
    public static final String ACTIVE = "active";
    public static final String INACTIVE = "inactive";
    public static final String REMOVED = "removed";
    public static final String DELETED = "deleted";

    public static final String NOTI_ADDED = "added";
    public static final String NOTI_REMOVED = "removed";
    public static final String NOTI_LEFT = "left";
    //chat
    public static final int SENT = 0;
    public static final int READ = 1;
    public static final int UN_READ = 2;
    public static TimeZone timeZone = TimeZone.getTimeZone("GMT-4:00");
    public static TimeZone timeZoneDefault = TimeZone.getDefault();
    public static SimpleDateFormat dfUTC0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //login retry
    private int retryLogin = 0;
    private static final int MAX_RETRY = 2;

    private static final ChatService instance = new ChatService();
    public static ChatService getInstance(){return instance;}

    private ChatApi chatApi;

    private ChatRoomEntityDao chatRoomEntityDao;
    private ChatMessageEntityDao chatMessageEntityDao;

    public void init(Context context, Bus bus, ChatApi chatApi){
        this.context = context;
        this.bus = bus;
        this.chatApi = chatApi;
        chatMessageEntityDao = DatabaseService.getInstance().getChatMessageEntityDao();
        chatRoomEntityDao = DatabaseService.getInstance().getChatRoomEntityDao();
        dfUTC0.setTimeZone(timeZone);
        Log.d(App.APP_TAG, "Timezone_chat: " + timeZone.getID());
    }

    public List<ChatRoomEntity> getAllChatRooms() {
        return chatRoomEntityDao.queryBuilder()
                .where(ChatRoomEntityDao.Properties.Status.notEq(DELETED),
                        ChatRoomEntityDao.Properties.Owner.eq(DatabaseService.getInstance().getMe().getUID()),
                        ChatRoomEntityDao.Properties.UserIds.isNotNull())
                .orderDesc(ChatRoomEntityDao.Properties.LastMessageTime)
                .build()
                .forCurrentThread()
                .list();
    }

    public void syncAllChats() {
        List<ChatRoomEntity> rooms = chatRoomEntityDao
                .queryBuilder()
                .orderDesc(ChatRoomEntityDao.Properties.LastUpdated)
                .limit(1)
                .build()
                .forCurrentThread()
                .list();
        Date lastUpdated = rooms !=null && rooms.size() > 0 ? rooms.get(0).getLastUpdated() : new Date(0);
        if (lastUpdated == null) {
            //This should not happen, but in case it does, we reset all chat tables;
            lastUpdated = new Date(0);
            chatRoomEntityDao.deleteAll();
            chatMessageEntityDao.deleteAll();
        }
        String lastUpdateTimeString = dfUTC0.format(lastUpdated);
        chatApi.syncChatRooms(DatabaseService.getInstance().getMe().getUID(), lastUpdateTimeString)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Action1<ChatRoomSyncResponse>() {
                    @Override
                    public void call(ChatRoomSyncResponse chatRoomSyncResponse) {
                        if ("success".equals(chatRoomSyncResponse.status)) {
                            long me = DatabaseService.getInstance().getMe().getUID();
                            for (SingleChatRoomInfo singleChatInfo : chatRoomSyncResponse.singleChats) {
                                ChatRoomEntity singleChat = findSingleChatRoom(singleChatInfo.friendId, true);
                                if (singleChat != null) {//Single chat can be null if we cannot find this friend in our friend list
                                    singleChat.setStatus(singleChatInfo.status);
                                    Date date = new Date();
                                    try {
                                        date = dfUTC0.parse(singleChatInfo.lastUpdated);

                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    singleChat.setLastUpdated(date);
                                    addUpdateChatRoom(singleChat);
                                    syncChatroomMessage(singleChat);
                                }
                            }
                            for (GroupChatRoomInfo groupChatInfo : chatRoomSyncResponse.groupChats) {
                                ChatRoomEntity groupChat = findGroupChatRoom(groupChatInfo.groupId, true);
                                groupChat.setTitle(groupChatInfo.title);
                                groupChat.setAvatar(groupChatInfo.avatar);
                                List<GroupChatRoomInfo.Member> members = groupChatInfo.members;
                                StringBuilder userIdsBuilder = new StringBuilder();
                                StringBuilder userNamesBuilder = new StringBuilder();
                                StringBuilder memberStatusBuilder = new StringBuilder();
                                StringBuilder emailBuilder = new StringBuilder();
                                StringBuilder avatarIdBuilder = new StringBuilder();
                                Long admin = null;
                                String status = ACTIVE;

                                for (GroupChatRoomInfo.Member member : members) {
                                    userIdsBuilder.append(member.userId).append(",");
                                    userNamesBuilder.append(member.name).append(",");
                                    memberStatusBuilder.append(member.status).append(",");
                                    emailBuilder.append(member.email).append(",");
                                    if (member.avatarId == null || member.avatarId.isEmpty()) {
                                        avatarIdBuilder.append("-1,");
                                    } else {
                                        avatarIdBuilder.append(member.avatarId).append(",");
                                    }

                                    if ("t".equals(member.adminStatus)) {
                                        admin = member.userId;
                                    }
                                    if (member.userId == me) {
                                        status = member.status;
                                    }
                                }
                                final String userIds = userIdsBuilder.charAt(userIdsBuilder.length() - 1) == ',' ? userIdsBuilder.substring(0, userIdsBuilder.length() - 1) : userIdsBuilder.toString();
                                final String userNames = userNamesBuilder.charAt(userNamesBuilder.length() - 1) == ',' ? userNamesBuilder.substring(0, userNamesBuilder.length() - 1) : userNamesBuilder.toString();
                                final String memberStatus = memberStatusBuilder.charAt(memberStatusBuilder.length() - 1) == ',' ? memberStatusBuilder.substring(0, memberStatusBuilder.length() - 1) : memberStatusBuilder.toString();
                                final String emails = emailBuilder.charAt(emailBuilder.length() - 1) == ',' ? emailBuilder.substring(0, emailBuilder.length() - 1) : emailBuilder.toString();
                                final String avatarIds = avatarIdBuilder.charAt(avatarIdBuilder.length() - 1) == ',' ? avatarIdBuilder.substring(0, avatarIdBuilder.length() - 1) : avatarIdBuilder.toString();
                                groupChat.setUserIds(userIds);
                                groupChat.setUserNames(userNames);
                                groupChat.setMemberStatus(memberStatus);
                                groupChat.setEmails(emails);
                                groupChat.setAvatarIds(avatarIds);
                                groupChat.setAdmin(admin);
                                Date date = new Date();
                                try {
                                    date = dfUTC0.parse(groupChatInfo.lastUpdated);

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                groupChat.setLastUpdated(date);
                                groupChat.setStatus(status);
                                addUpdateChatRoom(groupChat);
                                syncChatroomMessage(groupChat);
                            }
                            post(new ChatRoomsSyncEvent(true));
                        } else {
                            post(new ChatRoomsSyncEvent(false));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("XXX:", "Cannot sync chat rooms", throwable);
                        post(new ChatRoomsSyncEvent(false));
                    }
                });
    }

    public void addUpdateChatRoom(ChatRoomEntity entity) {
        entity.setOwner(DatabaseService.getInstance().getMe().getUID());
        chatRoomEntityDao.insertOrReplace(entity);
    }

    public ChatRoomEntity findSingleChatRoom(long friendId, boolean create) {
        AttendeeEntity friend = AttendeesService.getInstance().getAttendeesByUID(friendId);
        if (friend != null) {
            return findSingleChatRoom(friend, create);
        }
        return null;
    }

    public ChatRoomEntity findGroupChatRoom(long serverId, boolean create) {
        ChatRoomEntity chatRoomEntity = chatRoomEntityDao
                .queryBuilder()
                .where(ChatRoomEntityDao.Properties.ServerId.eq(serverId),
                        ChatRoomEntityDao.Properties.IsGroup.eq(true))
                .unique();
        if (chatRoomEntity == null && create) {
            chatRoomEntity = new ChatRoomEntity(null, null, null,
                    null, serverId, null, true, null, null, null, null, null, null, null, null,
                    DatabaseService.getInstance().getMe().getUID(), 0, ACTIVE, null);
            chatRoomEntityDao.insert(chatRoomEntity);
        }
        return chatRoomEntity;
    }

    public ChatRoomEntity findSingleChatRoom(AttendeeEntity friend, boolean create) {
        ChatRoomEntity chatRoomEntity = null;
        List<ChatRoomEntity> chatRoomEntities = chatRoomEntityDao
                .queryBuilder()
                .where(ChatRoomEntityDao.Properties.ServerId.eq(friend.getUID()),
                        ChatRoomEntityDao.Properties.IsGroup.eq(false))
                .list();
        if (chatRoomEntities != null && chatRoomEntities.size() > 0){
            chatRoomEntity = chatRoomEntities.get(0);
        }
        if (chatRoomEntity == null) {
            chatRoomEntity = new ChatRoomEntity(null, friend.getName(), friend.getAvatar(),
                    friend.getUID().toString(), friend.getUID(), DatabaseService.getInstance().getMe().getUID(),
                    false, null, friend.getName(), "PhoneNumber", friend.getEmail(),
                    friend.getAvatar(), null, null,  null, DatabaseService.getInstance().getMe().getUID(), 0, ACTIVE, ACTIVE);
            chatRoomEntityDao.insert(chatRoomEntity);
        }
        return chatRoomEntity;
    }

    public void leaveChatRoom(final ChatRoomEntity chatRoomEntity) {
        chatApi
                .exitGroupChat(DatabaseService.getInstance().getMe().getUID(), chatRoomEntity.getServerId().toString())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<SimpleResponse>() {
                    @Override
                    public void call(SimpleResponse simpleResponse) {
                        if ("success".equals(simpleResponse.status)) {
                            chatRoomEntity.setStatus(INACTIVE);
                            addUpdateChatRoom(chatRoomEntity);
                            post(new ChatRoomLeft(chatRoomEntity, null));
                        } else {
                            post(new ChatRoomLeft(null, null));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("XXX:", "Cannot leave chat room", throwable);
                        post(new ChatRoomLeft(null, throwable.toString()));
                    }
                });
    }

    public void deleteChatRoom(ChatRoomEntity chatRoomEntity) {
        if (chatRoomEntity.getIsGroup()) {
            deleteGroupChat(chatRoomEntity);
        } else {
            deleteSingleChat(chatRoomEntity);
        }
    }

    public void deleteSingleChat(final ChatRoomEntity chatRoomEntity) {
        chatApi
                .deleteSingleChat(DatabaseService.getInstance().getMe().getUID(), chatRoomEntity.getServerId().toString())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<SimpleResponse>() {
                    @Override
                    public void call(SimpleResponse simpleResponse) {
                        if ("success".equals(simpleResponse.status)) {
                            chatRoomEntity.setStatus(DELETED);
                            deleteChatRoomInDb(chatRoomEntity);
                            post(new ChatRoomDeleted(chatRoomEntity, null));
                        } else {
                            post(new ChatRoomDeleted(null, null));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("XXX:", "Cannot delete chat room", throwable);
                        post(new ChatRoomDeleted(null, throwable.toString()));
                    }
                });
    }

    public void deleteGroupChat(final ChatRoomEntity chatRoomEntity) {
        chatApi
                .deleteGroupChat(DatabaseService.getInstance().getMe().getUID(), chatRoomEntity.getServerId().toString())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<SimpleResponse>() {
                    @Override
                    public void call(SimpleResponse simpleResponse) {
                        if ("success".equals(simpleResponse.status)) {
                            chatRoomEntity.setStatus(DELETED);
                            deleteChatRoomInDb(chatRoomEntity);
                            post(new ChatRoomDeleted(chatRoomEntity, null));
                        } else {
                            post(new ChatRoomDeleted(null, null));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("XXX:", "Cannot delete chat room", throwable);
                        post(new ChatRoomDeleted(null, throwable.toString()));
                    }
                });
    }

    private void deleteChatRoomInDb(ChatRoomEntity chatRoomEntity) {
        chatRoomEntityDao.delete(chatRoomEntity);
        chatMessageEntityDao
                .queryBuilder()
                .where(ChatMessageEntityDao.Properties.ChatRoomId.eq(chatRoomEntity.getId()))
                .buildDelete()
                .forCurrentThread()
                .executeDeleteWithoutDetachingEntities();
    }

    private void addChatMessage(ChatMessageEntity entity) {
        entity.setOwner(DatabaseService.getInstance().getMe().getUID());
        if (entity.getServerId() != null && entity.getServerId() > 0) {
            DatabaseService.getInstance()
                    .getChatMessageEntityDao()
                    .queryBuilder()
                    .where(ChatMessageEntityDao.Properties.ServerId.eq(entity.getServerId()))
                    .buildDelete()
                    .forCurrentThread()
                    .executeDeleteWithoutDetachingEntities();
        }
        DatabaseService.getInstance().getChatMessageEntityDao().insertOrReplace(entity);
    }

    private void syncChatRoom(long serverGroupId) {
        final ChatRoomEntity chatRoomEntity = DatabaseService.getInstance().getChatRoomEntityDao()
                .queryBuilder()
                .where(
                        ChatRoomEntityDao.Properties.ServerId.eq(serverGroupId),
                        ChatRoomEntityDao.Properties.IsGroup.eq(true))
                .build()
                .forCurrentThread()
                .unique();
        if (chatRoomEntity == null) {
            return;
        }
        chatApi
                .getChatRoomDetails(DatabaseService.getInstance().getMe().getUID(), chatRoomEntity.getServerId())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Action1<GroupDetailsResponse>() {
                    @Override
                    public void call(GroupDetailsResponse groupDetailsResponse) {
                        GroupChatRoomInfo groupChatInfo = groupDetailsResponse.chatRoomInfo;
                        chatRoomEntity.setTitle(groupChatInfo.title);
                        chatRoomEntity.setAvatar(groupChatInfo.avatar);
                        long me = DatabaseService.getInstance().getMe().getUID();
                        List<GroupChatRoomInfo.Member> members = groupChatInfo.members;
                        StringBuilder userIdsBuilder = new StringBuilder();
                        StringBuilder userNamesBuilder = new StringBuilder();
                        StringBuilder memberStatusBuilder = new StringBuilder();
                        StringBuilder emailBuilder = new StringBuilder();
                        StringBuilder avatarIdBuilder = new StringBuilder();
                        Long admin = null;
                        String status = chatRoomEntity.getStatus();

                        for (GroupChatRoomInfo.Member member : members) {
                            userIdsBuilder.append(member.userId).append(",");
                            userNamesBuilder.append(member.name).append(",");
                            memberStatusBuilder.append(member.status).append(",");
                            emailBuilder.append(member.email).append(",");
                            if (member.avatarId == null || member.avatarId.isEmpty()) {
                                avatarIdBuilder.append("-1,");
                            } else {
                                avatarIdBuilder.append(member.avatarId).append(",");
                            }

                            if ("t".equals(member.adminStatus)) {
                                admin = member.userId;
                            }
                            if (member.userId == me) {
                                status = member.status;
                            }
                        }
                        final String userIds = userIdsBuilder.charAt(userIdsBuilder.length() - 1) == ',' ? userIdsBuilder.substring(0, userIdsBuilder.length() - 1) : userIdsBuilder.toString();
                        final String userNames = userNamesBuilder.charAt(userNamesBuilder.length() - 1) == ',' ? userNamesBuilder.substring(0, userNamesBuilder.length() - 1) : userNamesBuilder.toString();
                        final String memberStatus = memberStatusBuilder.charAt(memberStatusBuilder.length() - 1) == ',' ? memberStatusBuilder.substring(0, memberStatusBuilder.length() - 1) : memberStatusBuilder.toString();
                        final String emails = emailBuilder.charAt(emailBuilder.length() - 1) == ',' ? emailBuilder.substring(0, emailBuilder.length() - 1) : emailBuilder.toString();
                        final String avatarIds = avatarIdBuilder.charAt(avatarIdBuilder.length() - 1) == ',' ? avatarIdBuilder.substring(0, avatarIdBuilder.length() - 1) : avatarIdBuilder.toString();
                        chatRoomEntity.setUserIds(userIds);
                        chatRoomEntity.setUserNames(userNames);
                        chatRoomEntity.setMemberStatus(memberStatus);
                        chatRoomEntity.setEmails(emails);
                        chatRoomEntity.setAvatarIds(avatarIds);
                        chatRoomEntity.setAdmin(admin);
                        Date date = new Date();
                        try {
                            date = dfUTC0.parse(groupChatInfo.lastUpdated);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        chatRoomEntity.setLastUpdated(date);
                        chatRoomEntity.setStatus(status);
                        if (chatRoomEntity.getUnread() == null) {
                            chatRoomEntity.setUnread(1);
                        } else {
                            chatRoomEntity.setUnread(chatRoomEntity.getUnread() + 1);
                        }
                        addUpdateChatRoom(chatRoomEntity);
                        post(new ChatRoomUpdatedEvent(chatRoomEntity));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("XXX:", "Cannot get group details", throwable);
                    }
                });
    }

    public void processGroupMemberChanged(GroupMemberChangedNoti noti) {
        ChatRoomEntity chatRoomEntity = findGroupChatRoom(noti.groupId, false);
        if (chatRoomEntity != null) {
            Date utcTimeStamp = new Date();
            try {
                utcTimeStamp = dfUTC0.parse(noti.timestamp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            final ChatMessageEntity chatMessageEntity = new ChatMessageEntity(null, noti.content,
                    0l, utcTimeStamp,
                    chatRoomEntity.getId(),
                    DatabaseService.getInstance().getMe().getUID(), 1, 0l, true);
            addChatMessage(chatMessageEntity);

            if (noti.userId == DatabaseService.getInstance().getMe().getUID()) {
                if (NOTI_REMOVED.equals(noti.actionType)) {
                    chatRoomEntity.setStatus(REMOVED);
                } else if (NOTI_ADDED.equals(noti.actionType)){
                    chatRoomEntity.setStatus(ACTIVE);
                }
                addUpdateChatRoom(chatRoomEntity);
            }
            syncChatRoom(noti.groupId);
        } else {
            syncAllChats();
        }
    }

    public void processGroupInfoChanged(GroupInfoChangedNoti noti) {
        syncChatRoom(noti.groupId);
    }

    public class UnreadMessageInfo {
        public static final int NOTI_DISPLAY = 7;
        public List<ChatMessageEntity> latestUnreadMessages;
        public int totalUnread;
        public HashSet<Long> totalChats;

        public UnreadMessageInfo(List<ChatMessageEntity> latestUnreadMessages, int totalUnread, HashSet<Long> totalChats) {
            this.latestUnreadMessages = latestUnreadMessages;
            this.totalUnread = totalUnread;
            this.totalChats = totalChats;
        }
    }

    public UnreadMessageInfo getAllUnreadMessages() {
        LazyList<ChatMessageEntity> allMsgs = DatabaseService.getInstance()
                .getChatMessageEntityDao().queryBuilder()
                .where(ChatMessageEntityDao.Properties.Status.eq(UN_READ))
                .orderDesc(ChatMessageEntityDao.Properties.LastUpdated)
                .listLazyUncached();
        List<ChatMessageEntity> latestMsgs = new ArrayList<>();
        int latestCount = allMsgs.size() > (UnreadMessageInfo.NOTI_DISPLAY - 1) ? UnreadMessageInfo.NOTI_DISPLAY - 1
                : allMsgs.size();
        HashSet<Long> chats = new HashSet<>();
        for (int i = 0, size = allMsgs.size(); i < size; i++) {
            ChatMessageEntity msg = allMsgs.get(i);
            if (i < latestCount) {
                latestMsgs.add(msg);
            }
            chats.add(msg.getChatRoomId());
        }
        return new UnreadMessageInfo(latestMsgs, allMsgs.size(), chats);
    }

    public ArrayList<String> generateChatNotiContent(UnreadMessageInfo info) {
        ArrayList<String> result = new ArrayList<>();
        if (info.totalUnread > 0) {
            List<ChatMessageEntity> latestMsgs = info.latestUnreadMessages;
            ChatRoomEntityDao chatRoomEntityDao = DatabaseService.getInstance().getChatRoomEntityDao();
            StringBuilder sb = new StringBuilder();
            for (ChatMessageEntity msg : latestMsgs) {
                ChatRoomEntity chatRoom = chatRoomEntityDao.load(msg.getChatRoomId());
                if (chatRoom != null) {
                    if (chatRoom.getIsGroup()) {
                        Long fromId = msg.getUserId();
                        String[] userIds = chatRoom.getUserIds().split(",");
                        String[] usernames = chatRoom.getUserNames().split(",");
                        for (int i = 0; i < userIds.length; i++) {
                            if (fromId.equals(Long.valueOf(userIds[i]))) {
                                String s = String.format("%s @ %s: %s", usernames[i],
                                        chatRoom.getTitle(), msg.getMsg());
                                result.add(s);
                                break;
                            }
                        }

                    } else {
                        result.add(String.format("%s: %s", chatRoom.getTitle(), msg.getMsg()));
                    }
                }
            }
        }
        return result;
    }

    public void changeChatroomMessageToReadStatus(Long chatRoomId) {
        SQLiteDatabase db = DatabaseService.getInstance().getDatabase();
        ContentValues values = new ContentValues();
        values.put("STATUS", READ);
        db.update("CHAT_MESSAGE_ENTITY", values, "CHAT_ROOM_ID = ? AND STATUS = 2", new String[]{chatRoomId.toString()});
    }

    public long getNumberUnreadChatRoom(){
        return DatabaseService.getInstance()
                .getChatRoomEntityDao().queryBuilder()
                .where(ChatRoomEntityDao.Properties.Unread.gt(0))
                .count();
    }

    public List<ChatRoomEntity> getActiveGroupChatRooms() {
        QueryBuilder<ChatRoomEntity> queryBuilder = DatabaseService.getInstance().getChatRoomEntityDao()
                .queryBuilder();
        return queryBuilder
                .where(ChatRoomEntityDao.Properties.Status.eq(ACTIVE),
                        ChatRoomEntityDao.Properties.IsGroup.eq(true),
                        ChatRoomEntityDao.Properties.MemberStatus.like("%active%active%"),
                        ChatRoomEntityDao.Properties.Owner.eq(DatabaseService.getInstance().getMe().getUID()),
                        ChatRoomEntityDao.Properties.UserIds.isNotNull())
                .orderAsc(ChatRoomEntityDao.Properties.Title)
                .build()
                .forCurrentThread()
                .list();
    }

    public void syncChatroomMessage(final ChatRoomEntity chatRoomEntity) {
        if (ACTIVE.equals(chatRoomEntity.getStatus())) {
//            Date lastMessageTime = chatRoomEntity.getLastMessageTime() != null ? chatRoomEntity.getLastMessageTime() : new Date(0);
            Date lastMessageTime;
            boolean isFrom1970 = false;
            String dateQuery;
            if(chatRoomEntity.getLastMessageTime() != null){
                lastMessageTime = chatRoomEntity.getLastMessageTime();
                isFrom1970 = false;
                dateQuery = dfUTC0.format(lastMessageTime);
            } else {
                lastMessageTime = new Date(0);
                isFrom1970 = true;
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateQuery = df.format(lastMessageTime);
            }
            final boolean isFirstTimeSync = isFrom1970;
            final Observable<ChatMessages> chatMessags = chatRoomEntity.getIsGroup() ?
                    chatApi.getGroupChatMessages(DatabaseService.getInstance().getMe().getUID(), chatRoomEntity.getServerId(), dateQuery) :
                    chatApi.getSingleChatMessages(DatabaseService.getInstance().getMe().getUID(), chatRoomEntity.getServerId(), dateQuery);
            chatMessags
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .subscribe(new Action1<ChatMessages>() {
                        @Override
                        public void call(ChatMessages chatMessages) {
                            if ("success".equals(chatMessages.status)) {
                                long me = DatabaseService.getInstance().getMe().getUID();
                                Date lastMessageTime = null;
                                String lastMessage = null;
                                Long lastUserId = null;
                                int unread = 0;
                                Date date1 = new Date();
                                if (isFirstTimeSync){ //will remove when server fix
                                    for (ChatMessages.Message messageInfo : chatMessages.messages) {
                                        try {
                                            date1 = dfUTC0.parse(messageInfo.messageTime);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        ChatMessageEntity entity = new ChatMessageEntity(null, messageInfo.message, messageInfo.from, date1, chatRoomEntity.getId(), DatabaseService.getInstance().getMe().getUID(), READ, messageInfo.id, false);
                                        addChatMessage(entity);
                                        if (lastMessageTime == null || lastMessageTime.before(date1)) {
                                            lastMessageTime = date1;
                                            lastMessage = messageInfo.message;
                                            lastUserId = messageInfo.from;
                                        }
                                    }
                                } else {
                                    for (ChatMessages.Message messageInfo : chatMessages.messages) {
                                        try {
                                            date1 = dfUTC0.parse(messageInfo.messageTime);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        if (lastMessageTime == null || lastMessageTime.before(date1)) {
                                            lastMessageTime = date1;
                                            lastMessage = messageInfo.message;
                                            lastUserId = messageInfo.from;
                                        }
                                        if (me != messageInfo.from) {
                                            ChatMessageEntity entity = new ChatMessageEntity(null, messageInfo.message, messageInfo.from, date1, chatRoomEntity.getId(), DatabaseService.getInstance().getMe().getUID(), UN_READ, messageInfo.id, false);
                                            addChatMessage(entity);
                                            unread++;
                                        } else {
                                            ChatMessageEntity entity = new ChatMessageEntity(null, messageInfo.message, messageInfo.from, date1, chatRoomEntity.getId(), DatabaseService.getInstance().getMe().getUID(), READ, messageInfo.id, false);
                                            addChatMessage(entity);
                                        }
                                    }
                                }

                                if (lastMessage != null) {
                                    chatRoomEntity.setLastMessage(lastMessage);
                                    chatRoomEntity.setLastMessageTime(lastMessageTime);
                                    chatRoomEntity.setLastUserId(lastUserId);
                                }
                                if (unread != 0) {
                                    chatRoomEntity.setUnread(unread);
                                }
                                addUpdateChatRoom(chatRoomEntity);
                                if (chatMessages.messages != null && chatMessages.messages.size() > 0) {
                                    post(new ChatRoomMessageSynced(chatRoomEntity, true));
                                }
                            } else {
                                post(new ChatRoomMessageSynced(chatRoomEntity, false));
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.e("XXX:", "Cannot sync chat message", throwable);
                            post(new ChatRoomMessageSynced(chatRoomEntity, false));
                        }
                    });
        }
    }

    public void chat(String message, ChatRoomEntity chatRoomEntity) {
        long me = DatabaseService.getInstance().getMe().getUID();
        final ChatMessageEntity chatMessageEntity = new ChatMessageEntity(null, message, me, Calendar.getInstance(timeZone).getTime(), chatRoomEntity.getId(), me, SENT, 0l, false);
        addChatMessage(chatMessageEntity);
        resend(chatMessageEntity, chatRoomEntity);
    }

    public void resend(final ChatMessageEntity chatMessageEntity, final ChatRoomEntity chatRoomEntity) {
        try{
            final long me = DatabaseService.getInstance().getMe().getUID();
            Observable<ChatMessageSent> chatMessageSent = chatRoomEntity.getIsGroup() ?
                    chatApi.sendChat(DatabaseService.getInstance().getMe().getUID(), chatMessageEntity.getId(), chatMessageEntity.getMsg(), 0, chatRoomEntity.getServerId())
                    : chatApi.sendChat(DatabaseService.getInstance().getMe().getUID(), chatMessageEntity.getId(), chatMessageEntity.getMsg(), chatRoomEntity.getServerId(), 0);
            chatMessageSent
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .subscribe(new Action1<ChatMessageSent>() {
                        @Override
                        public void call(ChatMessageSent chatMessageSent) {
                            if ("success".equals(chatMessageSent.status)) {
                                Date lastUpdateTime = new Date();
                                try {
                                    lastUpdateTime = dfUTC0.parse(chatMessageSent.timestamp);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                chatMessageEntity.setLastUpdated(lastUpdateTime);
                                chatMessageEntity.setStatus(READ);
                                chatMessageEntity.setServerId(chatMessageSent.id);
                                addChatMessage(chatMessageEntity);
                                ChatRoomEntity currentChatroom = DatabaseService.getInstance().getChatRoomEntityDao().load(chatRoomEntity.getId());
                                if (currentChatroom != null) {
                                    if (currentChatroom.getLastMessageTime() == null) {
                                        currentChatroom.setLastMessageTime(lastUpdateTime);
                                        currentChatroom.setLastMessage(chatMessageEntity.getMsg());
                                        currentChatroom.setLastUserId(me);
                                        addUpdateChatRoom(currentChatroom);
                                    } else if (currentChatroom.getLastMessageTime().before(lastUpdateTime)) {
                                        currentChatroom.setLastMessageTime(lastUpdateTime);
                                        currentChatroom.setLastMessage(chatMessageEntity.getMsg());
                                        currentChatroom.setLastUserId(me);
                                        addUpdateChatRoom(currentChatroom);
                                    }
                                }
                                if (chatMessageSent.isWarning){
                                    post(new ChatMessageSentEvent(chatMessageEntity, true, chatMessageSent.details));
                                } else {
                                    post(new ChatMessageSentEvent(chatMessageEntity, true, ""));
                                }
                            } else {
                                ChatMessageSentEvent event = new ChatMessageSentEvent(chatMessageEntity, false, chatMessageSent.details);
                                post(event);
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.e("XXX:", "Cannot chat message", throwable);
                            post(new ChatMessageSentEvent(chatMessageEntity, false, "Unable to sent this message. Please try again."));
                        }
                    });
        }catch (Exception e){
            Log.e(App.APP_TAG, "chatService.resend", e);
        }

    }

    public void deleteChatMessage(ChatMessageEntity entity) {
        DatabaseService.getInstance().getChatMessageEntityDao().delete(entity);
        post(new ChatMessageDeleted(entity));
    }

    public LazyList<ChatMessageEntity> getAllChatMessages(long chatRoomId) {
        return DatabaseService.getInstance().getChatMessageEntityDao()
                .queryBuilder()
                .where(
                        ChatMessageEntityDao.Properties.ChatRoomId.eq(chatRoomId),
                        ChatMessageEntityDao.Properties.Owner.eq(DatabaseService.getInstance().getMe().getUID()))
                .orderAsc(ChatMessageEntityDao.Properties.LastUpdated)
                .build()
                .forCurrentThread()
                .listLazy();
    }

    public void createGroupChat(final String title, final String avatarId, List<Tag> members) {
        StringBuilder userIdsBuilder = new StringBuilder();
        StringBuilder userNamesBuilder = new StringBuilder();
        StringBuilder memberStatusBuilder = new StringBuilder();
        StringBuilder emailBuilder = new StringBuilder();
        StringBuilder memberAvatarIdsBuilder = new StringBuilder();

        for (Tag memberTag : members) {
            AttendeeTag member = (AttendeeTag) memberTag;
            userIdsBuilder.append(member.attendee.getUID()).append(",");
            userNamesBuilder.append(member.attendee.getName()).append(",");
            emailBuilder.append(member.attendee.getEmail() + ",");
            memberAvatarIdsBuilder.append(
                    member.attendee.getAvatar() == null? "-1," :  member.attendee.getAvatar()+ ",");
            memberStatusBuilder.append("active,");
        }
        final String userIds = userIdsBuilder.charAt(userIdsBuilder.length() - 1) == ',' ? userIdsBuilder.substring(0, userIdsBuilder.length() - 1) : userIdsBuilder.toString();
        final String userNames = userNamesBuilder.charAt(userNamesBuilder.length() - 1) == ',' ? userNamesBuilder.substring(0, userNamesBuilder.length() - 1) : userNamesBuilder.toString();
        final String memberStatus = memberStatusBuilder.charAt(memberStatusBuilder.length() - 1) == ',' ? memberStatusBuilder.substring(0, memberStatusBuilder.length() - 1) : memberStatusBuilder.toString();
        final String emails = emailBuilder.charAt(emailBuilder.length() - 1) == ',' ? emailBuilder.substring(0, emailBuilder.length() - 1) : emailBuilder.toString();
        final String memberAvatarIds = memberAvatarIdsBuilder.charAt(memberAvatarIdsBuilder.length() - 1) == ',' ? memberAvatarIdsBuilder.substring(0, memberAvatarIdsBuilder.length() - 1) : memberAvatarIdsBuilder.toString();
        chatApi
                .createChatGroup(DatabaseService.getInstance().getMe().getUID(), title, avatarId, userIds)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(new Action1<SimpleResponse>() {
                    @Override
                    public void call(SimpleResponse simpleResponse) {
                        if ("success".equals(simpleResponse.status)) {
                            long groupId = Integer.valueOf(simpleResponse.details);
                            User me = DatabaseService.getInstance().getMe();
                            ChatRoomEntity entity = new ChatRoomEntity(null, title, avatarId,
                                    userIds + "," + me.getUID(), groupId, me.getUID(), true, null,
                                    userNames + "," + me.getName(),
                                    "",
                                    emails + "," + me.getEmail(),
                                    memberAvatarIds + "," + (me.getAvatar() == null ? "-1" : me.getAvatar()),
                                    null, null, Calendar.getInstance(timeZone).getTime(),
                                    me.getUID(), 0, ACTIVE, memberStatus + ",active");
                            addUpdateChatRoom(entity);
                            post(new ChatRoomCreatedEvent(entity));
                        } else {
                            post(new ChatRoomCreatedEvent(null));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("XXX:", "Cannot create chat room", throwable);
                        post(new ChatRoomCreatedEvent(null));
                    }
                });
    }

    public void updateGroupChat(final String title, final String avatarId, List<Tag> members, final ChatRoomEntity chatRoomEntity) {
        StringBuilder userIdsBuilder = new StringBuilder();
        StringBuilder userNamesBuilder = new StringBuilder();
        StringBuilder memberStatusBuilder = new StringBuilder();
        StringBuilder emailBuilder = new StringBuilder();
        StringBuilder memberAvatarIdsBuilder = new StringBuilder();
        for (Tag memberTag : members) {
            AttendeeTag member = (AttendeeTag) memberTag;
            userIdsBuilder.append(member.attendee.getUID()).append(",");
            userNamesBuilder.append(member.attendee.getName()).append(",");
            emailBuilder.append(member.attendee.getEmail()).append(",");
            memberAvatarIdsBuilder.append(member.attendee.getAvatar()).append(",");
            memberStatusBuilder.append("active,");
        }
        final String sendingUserIds = userIdsBuilder.charAt(userIdsBuilder.length() - 1) == ',' ? userIdsBuilder.substring(0, userIdsBuilder.length() - 1) : userIdsBuilder.toString();
        if (chatRoomEntity.getUserIds() != null) {
            String[] userIds = chatRoomEntity.getUserIds().split(",");
            String[] userNames = chatRoomEntity.getUserNames().split(",");
            String[] memberStatuses = chatRoomEntity.getMemberStatus().split(",");
            String newUserIds = userIdsBuilder.toString();
            for (int i = 0; i < userIds.length; i++) {
                String userId = userIds[i];
                String username = userNames[i];
                String memberStatus = memberStatuses[i];
                if (!newUserIds.contains(userId)) {
                    userIdsBuilder.append(userId).append(",");
                    userNamesBuilder.append(username).append(",");
                    if (ACTIVE.equals(memberStatus)) {
                        memberStatusBuilder.append(DELETED).append(",");
                    } else {
                        memberStatusBuilder.append(memberStatus).append(",");
                    }
                }
            }
        }
        final String userIds = userIdsBuilder.charAt(userIdsBuilder.length() - 1) == ',' ? userIdsBuilder.substring(0, userIdsBuilder.length() - 1) : userIdsBuilder.toString();
        final String userNames = userNamesBuilder.charAt(userNamesBuilder.length() - 1) == ',' ? userNamesBuilder.substring(0, userNamesBuilder.length() - 1) : userNamesBuilder.toString();
        final String memberStatus = memberStatusBuilder.charAt(memberStatusBuilder.length() - 1) == ',' ? memberStatusBuilder.substring(0, memberStatusBuilder.length() - 1) : memberStatusBuilder.toString();
        final String emails = emailBuilder.charAt(emailBuilder.length() - 1) == ',' ? emailBuilder.substring(0, emailBuilder.length() - 1) : emailBuilder.toString();
        final String memberAvatarIds = memberAvatarIdsBuilder.charAt(memberAvatarIdsBuilder.length() - 1) == ',' ? memberAvatarIdsBuilder.substring(0, memberAvatarIdsBuilder.length() - 1) : memberAvatarIdsBuilder.toString();
        Observable.zip(chatApi.updateChatGroup(DatabaseService.getInstance().getMe().getUID(), chatRoomEntity.getServerId().toString(), title, avatarId),
                chatApi.updateChatGroup(DatabaseService.getInstance().getMe().getUID(), chatRoomEntity.getServerId().toString(), sendingUserIds),
                new Func2<SimpleResponse2, SimpleResponse2, SimpleResponse2>() {
                    @Override
                    public SimpleResponse2 call(SimpleResponse2 simpleResponse2, SimpleResponse2 simpleResponse) {
                        SimpleResponse2 result = new SimpleResponse2();
                        if ("success".equals(simpleResponse.status) && "success".equals(simpleResponse2.status)) {
                            result.status = "success";
                        } else if ("success".equals(simpleResponse.status)) {
                            result = simpleResponse;
                        } else {
                            result = simpleResponse2;
                        }
                        return result;
                    }
                }).subscribeOn(Schedulers.io())
                .subscribe(new Action1<SimpleResponse2>() {
                    @Override
                    public void call(SimpleResponse2 simpleResponse) {
                        if ("success".equals(simpleResponse.status)) {
                            User me = DatabaseService.getInstance().getMe();
                            String myUID = String.valueOf(me.getUID());
                            if (!chatRoomEntity.getUserIds().contains(myUID)){
                                chatRoomEntity.setTitle(title);
                                chatRoomEntity.setAvatar(avatarId);
                                chatRoomEntity.setUserIds(userIds + "," + me.getUID());
                                chatRoomEntity.setUserNames(userNames + "," + me.getName());
                                chatRoomEntity.setEmails(emails + "," + me.getEmail());
                                chatRoomEntity.setAvatarIds(memberAvatarIds + "," + me.getAvatar());
                                chatRoomEntity.setMemberStatus(memberStatus + "," + "active");
                            } else {
                                chatRoomEntity.setTitle(title);
                                chatRoomEntity.setAvatar(avatarId);
                                chatRoomEntity.setUserIds(userIds);
                                chatRoomEntity.setUserNames(userNames);
                                chatRoomEntity.setEmails(emails);
                                chatRoomEntity.setAvatarIds(memberAvatarIds);
                                chatRoomEntity.setMemberStatus(memberStatus);
                            }
                            chatRoomEntity.setLastUpdated(Calendar.getInstance(timeZone).getTime());
                            addUpdateChatRoom(chatRoomEntity);
                            post(new ChatRoomUpdatedEvent(chatRoomEntity));
                            post(new ChatRoomUpdateCompletedEvent());
                        } else {
                            post(new ChatRoomUpdatedEvent(null));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("XXX:", "Cannot update chat room", throwable);
                        post(new ChatRoomUpdatedEvent(null));
                    }
                });
    }

    public void updateGroupChat(final String title, final String avatarId, final ChatRoomEntity chatRoomEntity) {
        chatApi.updateChatGroup(DatabaseService.getInstance().getMe().getUID(), chatRoomEntity.getServerId().toString(), title, avatarId)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<SimpleResponse2>() {
                    @Override
                    public void call(SimpleResponse2 simpleResponse) {
                        if ("success".equals(simpleResponse.status)) {
                            chatRoomEntity.setTitle(title);
                            chatRoomEntity.setAvatar(avatarId);
                            chatRoomEntity.setLastUpdated(Calendar.getInstance(timeZone).getTime());
                            addUpdateChatRoom(chatRoomEntity);
                            post(new ChatRoomUpdatedEvent(chatRoomEntity));
                            post(new ChatRoomUpdateCompletedEvent());
                        } else {
                            post(new ChatRoomUpdatedEvent(null));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("XXX:", "Cannot update chat room", throwable);
                        post(new ChatRoomUpdatedEvent(null));
                    }
                });
    }

    public ChatMessageEntity processChatMessageFromNoti(final ChatMessageNoti noti) {
        final ChatRoomEntity chatRoomEntity = noti.groupId == 0 ? findSingleChatRoom(noti.fromUser, true) : findGroupChatRoom(noti.groupId, true);
        if (noti.serverId == 0) {
            throw new IllegalArgumentException("Server id cannot be 0");
        }
        if (noti.groupId == 0) {
            AttendeeEntity friendEntity = AttendeesService.getInstance().getAttendeesByUID(noti.fromUser);
            if (friendEntity == null ){ //TODO mobiSys app everyone is friend ContactProvider.CURRENT_FRIEND_STATUS != friendEntity.getStatus()) {
                return null;
            }
        }
        if (chatRoomEntity != null) {
            String content = noti.content;
            int index = content.indexOf(":",0);
            content = content.substring(index + 1);
            content = content.trim();
            if (DatabaseService.getInstance().getMe().getUID() == noti.fromUser) return null;//receive my own chat
            Date chatTime = new Date();
            try {
                chatTime = dfUTC0.parse(noti.timestamp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            final ChatMessageEntity chatMessageEntity = new ChatMessageEntity(null, content,
                    noti.fromUser, chatTime,
                    chatRoomEntity.getId(),
                    DatabaseService.getInstance().getMe().getUID(), UN_READ, noti.serverId, false);
            addChatMessage(chatMessageEntity);
            noti.dbId = chatMessageEntity.getId();
            chatRoomEntity.setLastMessage(content);
            chatRoomEntity.setLastMessageTime(Calendar.getInstance(timeZone).getTime());
            chatRoomEntity.setLastUserId(noti.fromUser);
            addUpdateChatRoom(chatRoomEntity);
            boolean chatRoomNeedUpdated = chatRoomEntity.getUserIds() == null || !chatRoomEntity.getUserIds().contains(noti.fromUser + "");
            if (chatRoomNeedUpdated) {
                chatApi
                        .getChatRoomDetails(DatabaseService.getInstance().getMe().getUID(), chatRoomEntity.getServerId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
                        .subscribe(new Action1<GroupDetailsResponse>() {
                            @Override
                            public void call(GroupDetailsResponse groupDetailsResponse) {
                                GroupChatRoomInfo groupChatInfo = groupDetailsResponse.chatRoomInfo;
                                chatRoomEntity.setTitle(groupChatInfo.title);
                                chatRoomEntity.setAvatar(groupChatInfo.avatar);
                                long me = DatabaseService.getInstance().getMe().getUID();
                                List<GroupChatRoomInfo.Member> members = groupChatInfo.members;
                                StringBuilder userIdsBuilder = new StringBuilder();
                                StringBuilder userNamesBuilder = new StringBuilder();
                                StringBuilder memberStatusBuilder = new StringBuilder();
                                StringBuilder emailBuilder = new StringBuilder();
                                StringBuilder avatarIdBuilder = new StringBuilder();
                                Long admin = null;
                                String status = ACTIVE;

                                for (GroupChatRoomInfo.Member member : members) {
                                    userIdsBuilder.append(member.userId).append(",");
                                    userNamesBuilder.append(member.name).append(",");
                                    memberStatusBuilder.append(member.status).append(",");
                                    emailBuilder.append(member.email).append(",");
                                    if (member.avatarId == null || member.avatarId.isEmpty()){
                                        avatarIdBuilder.append("-1,");
                                    } else {
                                        avatarIdBuilder.append(member.avatarId).append(",");
                                    }
                                    if ("t".equals(member.adminStatus)) {
                                        admin = member.userId;
                                    }
                                    if (member.userId == me) {
                                        status = member.status;
                                    }
                                }
                                final String userIds = userIdsBuilder.charAt(userIdsBuilder.length() - 1) == ',' ? userIdsBuilder.substring(0, userIdsBuilder.length() - 1) : userIdsBuilder.toString();
                                final String userNames = userNamesBuilder.charAt(userNamesBuilder.length() - 1) == ',' ? userNamesBuilder.substring(0, userNamesBuilder.length() - 1) : userNamesBuilder.toString();
                                final String memberStatus = memberStatusBuilder.charAt(memberStatusBuilder.length() - 1) == ',' ? memberStatusBuilder.substring(0, memberStatusBuilder.length() - 1) : memberStatusBuilder.toString();
                                final String emails = emailBuilder.charAt(emailBuilder.length() - 1) == ',' ? emailBuilder.substring(0, emailBuilder.length() - 1) : emailBuilder.toString();
                                final String avatarIds = avatarIdBuilder.charAt(avatarIdBuilder.length() - 1) == ',' ? avatarIdBuilder.substring(0, avatarIdBuilder.length() - 1) : avatarIdBuilder.toString();
                                chatRoomEntity.setUserIds(userIds);
                                chatRoomEntity.setUserNames(userNames);
                                chatRoomEntity.setMemberStatus(memberStatus);
                                chatRoomEntity.setEmails(emails);
                                chatRoomEntity.setAvatarIds(avatarIds);
                                chatRoomEntity.setAdmin(admin);
                                Date date = new Date();
                                try {
                                    date = dfUTC0.parse(groupChatInfo.lastUpdated);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                chatRoomEntity.setLastUpdated(date);
                                chatRoomEntity.setStatus(status);
                                if (chatRoomEntity.getUnread() == null) {
                                    chatRoomEntity.setUnread(1);
                                } else {
                                    chatRoomEntity.setUnread(chatRoomEntity.getUnread() + 1);
                                }
                                addUpdateChatRoom(chatRoomEntity);
                                post(new ChatRoomUpdatedEvent(chatRoomEntity));
                                post(new ChatMessageReceivedEvent(noti, chatMessageEntity));
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Log.e("XXX:", "Cannot get group details", throwable);
                            }
                        });
            } else {
                if (chatRoomEntity.getUnread() == null) {
                    chatRoomEntity.setUnread(1);
                } else {
                    chatRoomEntity.setUnread(chatRoomEntity.getUnread() + 1);
                }
                addUpdateChatRoom(chatRoomEntity);
                post(new ChatMessageReceivedEvent(noti, chatMessageEntity));
            }
            return chatMessageEntity;
        }
        return null;
    }


    public void login2(final String qrCode, final boolean firstTimeLogin, final boolean onResume){
        chatApi.login2(qrCode, RestClient.KEY) //, App.APP_ID, App.appVersion)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LoginResponse>() {
                    @Override
                    public void call(LoginResponse loginResponse) {
                        if ("success".equals(loginResponse.status)) {
                            Log.d(App.APP_TAG, "login: success");
//                            Toast.makeText(context, "login: success", Toast.LENGTH_LONG).show();
                            if (loginResponse.details != null && loginResponse.details.size() > 0) {
                                UserResponse userResponse = loginResponse.details.get(0);
                                User me = new User();
                                me.setUID(userResponse.uid);
                                me.setEmail(userResponse.email);
                                me.setPassword(qrCode);
                                me.setName(userResponse.name);
                                me.setStatus(userResponse.status);
                                me.setCover(userResponse.cover);
                                me.setAvatar(userResponse.avatar);
                                me.setStatus(loginResponse.status);
                                me.setRoleStr(userResponse.role);
                                me.setDesignation(userResponse.designation);
                                me.setSchool(userResponse.school);
                                me.setInterestsStr(userResponse.interests);
                                me.setQrCode(qrCode);
                                me.setUserHandle(userResponse.userHandle);
                                me.setSessionToken(userResponse.sessionToken);
                                DatabaseService.getInstance().setMe(me);
                                MainActivity.mode = MainActivity.ONLINE_MODE;
                                retryLogin = 0;
                                //Syn Data
                                App.getInstance().loginFail = false;
                                App.getInstance().callingLoginFromOnResume = false;
                                bus.post(new LoginSuccessFul(firstTimeLogin, onResume));
                            }
                        } else {
                            if(retryLogin < MAX_RETRY){
                                Handler h = new Handler(Looper.getMainLooper());
                                h.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        retryLogin++;
//                                        Toast.makeText(context, "login: fail " + retryLogin, Toast.LENGTH_LONG).show();
                                        login2(qrCode,firstTimeLogin, onResume);
                                    }
                                }, 1000);
                            }
                            else {
//                                Toast.makeText(context, "login: fail totally " + retryLogin, Toast.LENGTH_LONG).show();
                                retryLogin = 0;
                                Log.d(App.APP_TAG, "login: fail");
                                App.getInstance().loginFail = true;
                                App.getInstance().callingLoginFromOnResume = false;
                                bus.post(new LoginFail(firstTimeLogin));
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(final Throwable throwable) {
                        if(retryLogin < MAX_RETRY){
                            Handler h = new Handler(Looper.getMainLooper());
                            h.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    retryLogin++;
//                                    Toast.makeText(context, "login: fail Throwable" + retryLogin + throwable.toString().substring(0,70), Toast.LENGTH_LONG).show();
                                    login2(qrCode,firstTimeLogin, onResume);
                                }
                            }, 1000);

                        }
                        else {
                            retryLogin = 0;
//                            Toast.makeText(context, "login: fail totally Throwable" + retryLogin + throwable.toString().substring(0,70), Toast.LENGTH_LONG).show();
                            Log.d(App.APP_TAG, throwable.toString());
                            App.getInstance().loginFail = true;
                            App.getInstance().callingLoginFromOnResume = false;
                            bus.post(new LoginFail(firstTimeLogin));
                        }
                    }
                });
    }

    public void updateUser(final User me){
        DatabaseService.getInstance().setMe(me);
        chatApi.updateProfile(String.valueOf(me.getUID()), me.getUserHandle(), me.getSessionToken(),
                me.getName(), me.getSchool(), me.getAvatar(), me.getInterestsStr())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LoginResponse>() {
                    @Override
                    public void call(LoginResponse loginResponse) {
                        Log.d(App.APP_TAG, "Update user detail status: " + loginResponse.status);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(App.APP_TAG, throwable.toString());
                    }
                });

    }

    public void updateUserMac(long userId, String mac, String phoneType){
        chatApi.updateUserMac(userId, mac, phoneType)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe();
    }
}
