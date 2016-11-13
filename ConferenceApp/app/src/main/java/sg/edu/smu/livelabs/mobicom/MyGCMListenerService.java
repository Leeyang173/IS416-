package sg.edu.smu.livelabs.mobicom;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.squareup.otto.Bus;

import java.util.Collections;
import java.util.List;

import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.ScavengerUpdateDetailEvent;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.ChatMessageEntity;
import sg.edu.smu.livelabs.mobicom.models.data.EventEntity;
import sg.edu.smu.livelabs.mobicom.net.ChatMessageNoti;
import sg.edu.smu.livelabs.mobicom.net.noties.ActivePaperEventNoti;
import sg.edu.smu.livelabs.mobicom.net.noties.BEPNoti;
import sg.edu.smu.livelabs.mobicom.net.noties.BEPSurveyNoti;
import sg.edu.smu.livelabs.mobicom.net.noties.BadgeNoti;
import sg.edu.smu.livelabs.mobicom.net.noties.GroupInfoChangedNoti;
import sg.edu.smu.livelabs.mobicom.net.noties.GroupInvitedNoti;
import sg.edu.smu.livelabs.mobicom.net.noties.GroupMemberChangedNoti;
import sg.edu.smu.livelabs.mobicom.net.noties.InboxNoti;
import sg.edu.smu.livelabs.mobicom.net.noties.OtherNoti;
import sg.edu.smu.livelabs.mobicom.net.noties.PollingNoti;
import sg.edu.smu.livelabs.mobicom.net.noties.ScavengerNoti;
import sg.edu.smu.livelabs.mobicom.presenters.ChatPresenter;
import sg.edu.smu.livelabs.mobicom.services.AgendaService;
import sg.edu.smu.livelabs.mobicom.services.BEPService;
import sg.edu.smu.livelabs.mobicom.services.ChatService;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.ScavengerService;

/**
 * Created by smu on 18/1/16.
 */
public class MyGCMListenerService extends GcmListenerService {
    public static final String GCM_TAG = App.APP_TAG + "_GCM";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);
        Log.d(GCM_TAG, "FROM: " + from + ", Bundle: " + data);
        Context context = getApplicationContext();
//        AppNotifications.sendSimpleNotification(context, "testing - hard code", 1);
        try {
            int notificationType = Integer.valueOf(data.getString("notification_type"));
            App app = (App) getApplication();
            System.out.println("Notification: " + notificationType);
            switch (notificationType) {
                case 1://Chat message coming
                {
                    ChatMessageNoti noti = app.getGson().fromJson(data.getString("notification_content"), ChatMessageNoti.class);
                    ChatService.UnreadMessageInfo unreadMessageInfo = ChatService.getInstance().getAllUnreadMessages();
                    ChatMessageEntity chatMessageEntity = ChatService.getInstance().processChatMessageFromNoti(noti);
                    if (chatMessageEntity == null) break;
                    long currentChatId = App.getInstance().currentChat;
                    long curreentChatType = App.getInstance().currentChatType;
                    if (app.getMainActivity() != null) {
                        if (curreentChatType == ChatPresenter.GROUP_TYPE && currentChatId == noti.groupId) break;
                        if (curreentChatType == ChatPresenter.SINGLE_TYPE && noti.groupId == 0 && currentChatId == noti.fromUser) break;
                    }
                    unreadMessageInfo.totalUnread++;
                    unreadMessageInfo.totalChats.add(chatMessageEntity.getChatRoomId());
                    List<String> notiContents = ChatService.getInstance().generateChatNotiContent(unreadMessageInfo);
                    if (notiContents.size() == 0) {
                        notiContents.add(noti.content);
                    } else {
                        notiContents.add(0, noti.content);
                    }
                    Collections.reverse(notiContents);
                    AppNotifications.sendChatNotification(context, notiContents,
                            String.format("%d messages from %d chats.", unreadMessageInfo.totalUnread,
                                    unreadMessageInfo.totalChats.size()), noti.groupId, noti.fromUser);
                    final long unreadMessage = unreadMessageInfo.totalUnread;
                    if (unreadMessage > 0 && App.getInstance() != null && App.getInstance().getMainActivity() != null){
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                App.getInstance().getMainActivity().setMessageBag(unreadMessage);
                            }
                        });
                    }
                }
                break;
                case 3://invited to an event
                    break;
                case 4://Invited to new group
                {
                    GroupInvitedNoti noti = app.getGson().fromJson(data.getString("notification_content"), GroupInvitedNoti.class);
                    int id = Long.valueOf(noti.groupId).hashCode();
                    AppNotifications.sendGroupInvitedNotification(context, noti.content, id, noti.groupId);
                    ChatService.getInstance().syncAllChats();
                }
                break;
                case 5://Member list changed
                {
                    GroupMemberChangedNoti noti = app.getGson().fromJson(data.getString("notification_content"), GroupMemberChangedNoti.class);
                    if (noti.userId == DatabaseService.getInstance().getMe().getUID() && (ChatService.NOTI_ADDED.equals(noti.actionType) || ChatService.NOTI_REMOVED.equals(noti.actionType))) {
                        AppNotifications.sendGroupInvitedNotification(context, noti.content, Long.valueOf(noti.groupId).hashCode(), noti.groupId);
                    }
                    ChatService.getInstance().processGroupMemberChanged(noti);
                }
                break;
                case 6://Group info changed
                {
                    GroupInfoChangedNoti noti = app.getGson().fromJson(data.getString("notification_content"), GroupInfoChangedNoti.class);
                    ChatService.getInstance().processGroupInfoChanged(noti);
                }
                break;
                case 7://request friend
                    break;
                case 8://invited to an recurring event
                    break;
                case 10: //System notification
                {
                    OtherNoti otherNoti = app.getGson().fromJson(data.getString("notification_content"), OtherNoti.class);
                    AppNotifications.sendOtherNotification(context, null, otherNoti.content, AppNotifications.SYSTEM_NOTIFICATION_ID, "System noti");
                }
                break;
                case 11://general
                {

                    BEPNoti bepNoti = app.getGson().fromJson(data.getString("notification_content"), BEPNoti.class);
                    BEPService.getInstance().trackReceivedNotification(Integer.toString(bepNoti.id), Long.toString(notificationType));
                    Bundle dataGeneralNoti = new Bundle();
                    dataGeneralNoti.putLong("notification_type", notificationType);
                    dataGeneralNoti.putLong("id", bepNoti.id);
                    AppNotifications.sendDataNotification(context, bepNoti.from, bepNoti.content, AppNotifications.BEP_GENERAL_NOTIFICATION_ID, "BEP noti", dataGeneralNoti);
                    AgendaService.getInstance().getInbox();
                    if (App.getInstance().getMainActivity() != null){
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                App.getInstance().getMainActivity().setInboxBag();
                            }
                        });
                    }
                }
                break;
                case 12://survey
                {

                    BEPSurveyNoti bepSurveyNoti = app.getGson().fromJson(data.getString("notification_content"), BEPSurveyNoti.class);
                    BEPService.getInstance().trackReceivedNotification(Long.toString(bepSurveyNoti.id), Long.toString(notificationType));
                    Bundle dataSurveyNoti = new Bundle();
                    dataSurveyNoti.putLong("Survey_id", bepSurveyNoti.id);
                    dataSurveyNoti.putLong("notification_type", notificationType);
                    AppNotifications.sendDataNotification(context, bepSurveyNoti.from, bepSurveyNoti.content, AppNotifications.BEP_SURVEY_NOTIFICATION_ID, "BEP survey noti", dataSurveyNoti);
                }
                break;
                case 13://scavenger (slient notification)
                {
                    final ScavengerNoti scaNoti = app.getGson().fromJson(data.getString("notification_content"), ScavengerNoti.class);

                    if(App.getInstance() != null && App.getInstance().currentPresenter.equals("ScavengerHuntDetailPresenter")){
                        Handler mainHandler = new Handler(this.getMainLooper());
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Bus bus = App.getInstance().getBus();
                                bus.post(new ScavengerUpdateDetailEvent(scaNoti.huntAction, scaNoti.huntId, scaNoti.huntGroupId));

                            }
                        });
                    }
                    else{
                        if(scaNoti.huntAction.equals("deleted hunt")){
                            ScavengerService.getInstance().deductPointWhenDisbandNotInHuntPage(scaNoti.huntId);
                        }
                        else if(scaNoti.huntAction.equals("finished")){
                            ScavengerService.getInstance().updateIsComplete(scaNoti.huntId, true);
                        }
                    }
                }
                break;
                case 14://polling
                {
                    User me = DatabaseService.getInstance().getMe();
                    if(me != null && me.getRole() != null){
                        boolean isMeModerator = false;

                        for(String s: me.getRole()){
                            if(s.toLowerCase().equals("moderator")){
                                isMeModerator = true;
                                break;
                            }
                        }

                        if(!isMeModerator){
                            final PollingNoti pollNoti = app.getGson().fromJson(data.getString("notification_content"), PollingNoti.class);
                            Bundle dataPollNoti = new Bundle();
                            dataPollNoti.putLong("poll_id", pollNoti.pollId);
                            AppNotifications.sendDataNotification(context, pollNoti.title, pollNoti.content, AppNotifications.GAME_NOTIFICATION_ID, "Polling noti", dataPollNoti);

                        }
                    }
                }
                break;
                case 15://badge awards (slient notification)
                {
                    final BadgeNoti badgeNoti = app.getGson().fromJson(data.getString("notification_content"), BadgeNoti.class);
                    if(badgeNoti.badgeKey.equals("added")){
                        Handler mainHandler = new Handler(this.getMainLooper());
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Bus bus = App.getInstance().getBus();
                                bus.post(new BadgeNotiEvent(badgeNoti.badgeName));

                            }
                        });
                    }
                }
                break;
                case 16: // event: rating and quiz active
                    ActivePaperEventNoti activePaperEventNoti = app.getGson().fromJson(data.getString("notification_content"), ActivePaperEventNoti.class);
                    EventEntity eventEntity = AgendaService.getInstance().getEventByServerId(activePaperEventNoti.eventID);
                    eventEntity.setRatingQuizStatus(activePaperEventNoti.ratingQuizStatus);
                    AgendaService.getInstance().updateEvent(eventEntity);
                    break;
                case 17:
                    InboxNoti inboxNoti = app.getGson().fromJson(data.getString("notification_content"), InboxNoti.class);
                    if (inboxNoti.content != null){
                        AgendaService.getInstance().getInbox();
                        AppNotifications.sendOtherNotification(context, null, inboxNoti.content, AppNotifications.SYSTEM_NOTIFICATION_ID, "Inbox noti");
                        if (App.getInstance().getMainActivity() != null){
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    App.getInstance().getMainActivity().setInboxBag();
                                }
                            });
                        }
                    }

                    break;
                default:
                {
                    OtherNoti otherNoti = app.getGson().fromJson(data.getString("notification_content"), OtherNoti.class);
                    AppNotifications.sendOtherNotification(context, null, otherNoti.content, AppNotifications.SYSTEM_NOTIFICATION_ID, "System noti");
                }
                break;

            }
        } catch (Exception e) {
            Log.e("XXX:", "Error while processing noti", e);
        }
    }

    public MyGCMListenerService() {
    }

}
