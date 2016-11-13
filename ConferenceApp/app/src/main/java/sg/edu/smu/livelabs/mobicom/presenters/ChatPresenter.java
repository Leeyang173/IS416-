package sg.edu.smu.livelabs.mobicom.presenters;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.marshalchen.ultimaterecyclerview.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
import de.greenrobot.dao.query.LazyList;
import flow.Flow;
import in.co.madhur.chatbubblesdemo.model.ChatMessage;
import in.co.madhur.chatbubblesdemo.model.Status;
import in.co.madhur.chatbubblesdemo.model.UserType;
import jp.wasabeef.recyclerview.animators.FlipInTopXAnimator;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.functions.Action0;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.AppDependencies;
import sg.edu.smu.livelabs.mobicom.DaggerScope;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.adapters.ChatMessagesAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.BadgeNotiEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatMessageDeleted;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatMessageReceivedEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatMessageSentEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatRoomMessageSynced;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatRoomUpdatedEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateScreenEvent;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.AttendeeEntity;
import sg.edu.smu.livelabs.mobicom.models.data.ChatMessageEntity;
import sg.edu.smu.livelabs.mobicom.models.data.ChatRoomEntity;
import sg.edu.smu.livelabs.mobicom.presenters.screen.AddGroupChatScreen;
import sg.edu.smu.livelabs.mobicom.services.AttendeesService;
import sg.edu.smu.livelabs.mobicom.services.ChatService;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.services.ScreenService;
import sg.edu.smu.livelabs.mobicom.views.ChatView;
import sg.edu.smu.livelabs.mobicom.views.UserInfoPopup;

/**
 * Created by Aftershock PC on 21/7/2015.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(ChatPresenter.class)
@Layout(R.layout.chat_view)
public class ChatPresenter extends ViewPresenter<ChatView> {
    public static final String NAME = "ChatPresenter";
    public static final int GROUP_TYPE = 1;
    public static final int SINGLE_TYPE = 2;
    private final ActionBarOwner actionBarOwner;
    private final Bus bus;
    private ScreenService screenService;
    private final MainActivity mainActivity;
    private ChatRoomEntity chatRoomEntity;

    private AttendeeEntity attendeeEntity;
    private HashMap<Long, String> members;

//    private ChatListAdapter listAdapter;
    private ChatMessagesAdapter listAdapter;
    private Context context;
    private EditText chatEditText;
    //For testing only, this should be read from database;
    private ArrayList<ChatMessage> chatMessages;
    private ArrayList<ChatMessage> myMessages;
    private ArrayList<ChatMessage> otherMessages;
    private ArrayList<ChatMessage> notiMessages;

    public ChatPresenter(ActionBarOwner actionBarOwner, Bus bus, MainActivity mainActivity, ScreenService screenService,
                         @ScreenParam ChatRoomEntity chatRoomEntity
    ) {
        this.actionBarOwner = actionBarOwner;
        this.bus = bus;
        this.screenService = screenService;
        this.mainActivity = mainActivity;
        this.chatRoomEntity = chatRoomEntity;
        if (chatRoomEntity.getIsGroup()) {
            members = new HashMap<>();
            String[] userIds = chatRoomEntity.getUserIds().split(",");
            String[] userNames = chatRoomEntity.getUserNames().split(",");
            for (int i = 0; i < userIds.length; i++) {
                members.put(Long.valueOf(userIds[i]), userNames[i]);
                Log.d(App.APP_TAG, "994 " + userIds[i] + ": " + userNames[i]);
            }
        } else {
            attendeeEntity = AttendeesService.getInstance().getAttendeesByUID(chatRoomEntity.getServerId());
        }
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        reload();
    }

    private void reload(){
        if (!hasView()) {
            return;
        }
        context = getView().getContext();
        chatEditText = getView().chatEditText1;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        getView().chatListView.setLayoutManager(linearLayoutManager);

        chatMessages = new ArrayList<>();
        myMessages = new ArrayList<>();
        otherMessages = new ArrayList<>();
        notiMessages = new ArrayList<>();

        listAdapter = new ChatMessagesAdapter(getView().getContext(), chatRoomEntity.getIsGroup(),
                chatMessages, myMessages, otherMessages, notiMessages);
        getView().listAdapter= listAdapter ;
        getView().chatListView.setAdapter(listAdapter);
        StickyRecyclerHeadersDecoration headersDecor = new StickyRecyclerHeadersDecoration(listAdapter);
        getView().chatListView.addItemDecoration(headersDecor);
        getView().chatListView.disableLoadmore();
        getView().chatListView.setItemAnimator(new FlipInTopXAnimator());

        if (chatRoomEntity.getIsGroup()) {
            boolean isActive = ChatService.ACTIVE.equals(chatRoomEntity.getStatus());
            if (isActive){
                actionBarOwner.setConfig(new ActionBarOwner.Config(true, chatRoomEntity.getAvatar(),
                        new ActionBarOwner.MenuAction(chatRoomEntity.getTitle(), new Action0() {
                            @Override
                            public void call() {
                                Flow.get(context).set(new AddGroupChatScreen(chatRoomEntity));
                            }
                        }),new ActionBarOwner.MenuAction(R.drawable.icon_edit_white, new Action0() {
                            @Override
                            public void call() {
                                Flow.get(context).set(new AddGroupChatScreen(chatRoomEntity));
                            }
                        })
                ));
            } else {
                actionBarOwner.setConfig(new ActionBarOwner.Config(true, chatRoomEntity.getTitle(), null));
            }
            App.getInstance().currentChatType = GROUP_TYPE;
        } else {
            final AttendeeEntity attendeeEntity = AttendeesService.getInstance()
                    .getAttendeesByUID(chatRoomEntity.getServerId());
            if (attendeeEntity.getAvatar() != null && !attendeeEntity.getAvatar().equals(chatRoomEntity.getAvatar()) ){
                chatRoomEntity.setAvatar(attendeeEntity.getAvatar());
                DatabaseService.getInstance().getChatRoomEntityDao().update(chatRoomEntity);
            }
            actionBarOwner.setConfig(new ActionBarOwner.Config(true, attendeeEntity.getAvatar(),
                    new ActionBarOwner.MenuAction(chatRoomEntity.getTitle(), new Action0() {
                        @Override
                        public void call() {
                            new UserInfoPopup(context, attendeeEntity);
                        }
                    }),null));
            App.getInstance().currentChatType = SINGLE_TYPE;
        }
        App.getInstance().currentChat = chatRoomEntity.getServerId();
        getView().setMainActivity(mainActivity);
        getView().chatEditText1.setOnKeyListener(keyListener);
        getView().enterChatView1.setOnClickListener(clickListener);
        getView().chatEditText1.addTextChangedListener(watcher1);

        refreshLocal();
        ChatService.getInstance().syncChatroomMessage(chatRoomEntity);
        if (chatRoomEntity.getIsGroup() && !ChatService.ACTIVE.equals(chatRoomEntity.getStatus())) {
            getView().sendChatLayout.setVisibility(View.INVISIBLE);
            getView().disableText.setVisibility(View.VISIBLE);
            if (ChatService.REMOVED.equals(chatRoomEntity.getStatus())){
                getView().disableText.setText("You have been removed from this chat.");
            } else {
                getView().disableText.setText("You have left the chat.");
            }
        } else {
            getView().sendChatLayout.setVisibility(View.VISIBLE);
            getView().disableText.setVisibility(View.INVISIBLE);
        }
        if (chatRoomEntity.getIsGroup() && getView().sendChatLayout.getVisibility() == View.VISIBLE){
            String[] memberStatuses = chatRoomEntity.getMemberStatus().split(",");
            int numActive = 0;
            for (String memberStatus : memberStatuses){
                if("active".equals(memberStatus)){
                    numActive++;
                }
            }
            if (numActive == 1){
                getView().sendChatLayout.setVisibility(View.INVISIBLE);
                getView().disableText.setVisibility(View.VISIBLE);
                getView().disableText.setText("No one is home.");
            }
        }
//        else {
//            long friendId = chatRoomEntity.getServerId();
//            if(!DatabaseService.getInstance().isFriend(friendId)){
//                getView().sendChatLayout.setVisibility(View.INVISIBLE);
//                getView().disableText.setVisibility(View.VISIBLE);
//                getView().disableText.setText("Chat is disabled. This person is not your friend.");
//            }
//        }
        getView().scrollToBottom();
        NotificationManager mNotificationManager =
                (NotificationManager) getView().getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(1);
    }


    @Override
    protected void onEnterScope(MortarScope scope) {
        super.onEnterScope(scope);
        bus.register(this);
        mainActivity.setVisibleBottombar(View.GONE);
        App.getInstance().currentPresenter = NAME;
    }

    @Override
    protected void onExitScope() {
        super.onExitScope();
        bus.unregister(this);
        App.getInstance().currentChat = -1;
        App.getInstance().currentChatType = -1;
        if (NAME.equals(App.getInstance().currentPresenter)){
            App.getInstance().currentPresenter = "";
        }
    }

    @Subscribe public void updateScreen(UpdateScreenEvent event){
        this.chatRoomEntity = (ChatRoomEntity)event.data;
        if (chatRoomEntity.getIsGroup()) {
            members = new HashMap<>();
            String[] userIds = chatRoomEntity.getUserIds().split(",");
            String[] userNames = chatRoomEntity.getUserNames().split(",");
            for (int i = 0; i < userIds.length; i++) {
                members.put(Long.valueOf(userIds[i]), userNames[i]);
            }
        } else {
            attendeeEntity = AttendeesService.getInstance().getAttendeesByUID(chatRoomEntity.getServerId());
        }
        reload();
    }

    @Subscribe public void chatRoomUpdated(ChatRoomUpdatedEvent event) {
        if (event.chatRoomEntity == null) {
            return;
        }
        if (event.chatRoomEntity.getId().equals(chatRoomEntity.getId())) {
            chatRoomEntity = event.chatRoomEntity;
            if (ChatService.ACTIVE.equals(chatRoomEntity.getStatus())) {
                if (chatRoomEntity.getIsGroup()) {
                    boolean isActive = ChatService.ACTIVE.equals(chatRoomEntity.getStatus());
                    if (isActive){
                        actionBarOwner.setConfig(new ActionBarOwner.Config(true, chatRoomEntity.getAvatar(),
                                new ActionBarOwner.MenuAction(chatRoomEntity.getTitle(), new Action0() {
                                    @Override
                                    public void call() {
                                        Flow.get(context).set(new AddGroupChatScreen(chatRoomEntity));
                                    }
                                }),new ActionBarOwner.MenuAction(R.drawable.icon_edit_white, new Action0() {
                            @Override
                            public void call() {
                                Flow.get(context).set(new AddGroupChatScreen(chatRoomEntity));
                            }
                        })
                        ));
                    } else {
                        actionBarOwner.setConfig(new ActionBarOwner.Config(true, chatRoomEntity.getTitle(), null));
                    }

                    App.getInstance().currentChatType = GROUP_TYPE;
                } else {
                    actionBarOwner.setConfig(new ActionBarOwner.Config(true, chatRoomEntity.getTitle(), null));
                    App.getInstance().currentChatType = SINGLE_TYPE;
                }
                getView().sendChatLayout.setVisibility(View.VISIBLE);
                getView().disableText.setVisibility(View.INVISIBLE);
            } else {
                actionBarOwner.setConfig(new ActionBarOwner.Config(true, chatRoomEntity.getTitle(), null));
                getView().sendChatLayout.setVisibility(View.INVISIBLE);
                getView().disableText.setVisibility(View.VISIBLE);
                if (ChatService.REMOVED.equals(chatRoomEntity.getStatus())){
                    getView().disableText.setText("You had been removed from this chat.");
                } else {
                    getView().disableText.setText("You had left the chat.");
                }
            }
            String[] memberStatuses = chatRoomEntity.getMemberStatus().split(",");
            int numActive = 0;
            for (String memberStatus : memberStatuses){
                if("active".equals(memberStatus)){
                    numActive++;
                }
            }
            if (numActive == 1){
                UIHelper.getInstance().showAlert(context, "No one is home.");
                getView().sendChatLayout.setVisibility(View.INVISIBLE);
                getView().disableText.setVisibility(View.VISIBLE);
                getView().disableText.setText("No one is home.");
            }
            refreshLocal();
        }
    }

    @Subscribe public void chatMessageReceived(ChatMessageReceivedEvent event) {
        if (event.chatMessageEntity.getChatRoomId().equals(chatRoomEntity.getId())) {
            refreshLocal();
        } else {
            Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            MediaPlayer mediaPlayer = new MediaPlayer();

            try {
                mediaPlayer.setDataSource(getView().getContext(), defaultRingtoneUri);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
                mediaPlayer.prepare();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp)
                    {
                        mp.release();
                    }
                });
                mediaPlayer.start();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Subscribe
    public void chatMessageSent(final ChatMessageSentEvent chatMessageSentEvent) {
        if (chatMessageSentEvent.result) {
            if (chatMessageSentEvent.chatMessageEntity.getChatRoomId().equals(chatRoomEntity.getId())) {
                refreshLocal();
            }
            if (chatMessageSentEvent.msg != null && !chatMessageSentEvent.msg.isEmpty()){
                UIHelper.getInstance().showAlert(context, chatMessageSentEvent.msg);
            }
        } else {
            ChatService.getInstance().deleteChatMessage(chatMessageSentEvent.chatMessageEntity);
            if (chatMessageSentEvent.msg != null && !chatMessageSentEvent.msg.isEmpty()){
                UIHelper.getInstance().showAlert(context, chatMessageSentEvent.msg);
                getView().sendChatLayout.setVisibility(View.INVISIBLE);
                getView().disableText.setVisibility(View.VISIBLE);
                getView().disableText.setText(context.getString(R.string.disable_chat_group_send));
                return;
            } else {
                UIHelper.getInstance().showConfirmAlert(context,
                        getView().getContext().getResources().getString(R.string.app_name),
                        "Unable to send message \"" + chatMessageSentEvent.chatMessageEntity.getMsg() + "\". Please check your internet connection and retry.",
                        "Retry",
                        "Remove",
                        new Action0() {
                            @Override
                            public void call() {
                                ChatService.getInstance().resend(chatMessageSentEvent.chatMessageEntity, chatRoomEntity);
                            }
                        }, new Action0() {
                            @Override
                            public void call() {
                                ChatService.getInstance().deleteChatMessage(chatMessageSentEvent.chatMessageEntity);
                            }
                        });
            }
        }
    }
    @Subscribe public void chatMessageDeleted(ChatMessageDeleted event) {
        if (event.entity.getChatRoomId().equals(chatRoomEntity.getId())) {
            refreshLocal();
        }
    }

    @Subscribe public void chatRoomMessageSynced(ChatRoomMessageSynced event) {
        if (event.result && event.chatRoomEntity.getId().equals(chatRoomEntity.getId())) {
            refreshLocal();
        }
    }

    public Bus getBus() {
        return bus;
    }
    private EditText.OnKeyListener keyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Perform action on key press
                EditText editText = (EditText) v;
                if (v == getView().chatEditText1) {
                    sendMessage(editText.getText().toString());
                }
                getView().chatEditText1.setText("");

                return true;
            }
            return false;

        }
    };

    private ImageView.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == getView().enterChatView1) {
                sendMessage(getView().chatEditText1.getText().toString());
            }
            getView().chatEditText1.setText("");
        }
    };

    private final TextWatcher watcher1 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            if (getView().chatEditText1.getText().toString().equals("")) {

            } else {
                getView().enterChatView1.setImageResource(R.drawable.ic_chat_send);

            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable.length() == 0) {
                getView().enterChatView1.setImageResource(R.drawable.ic_chat_send);
            } else {
                getView().enterChatView1.setImageResource(R.drawable.ic_chat_send_active);
            }
        }
    };

    private void refreshLocal() {
        LazyList<ChatMessageEntity> chatMessageEntities = ChatService.getInstance().getAllChatMessages(chatRoomEntity.getId());
        if (chatMessageEntities != null) {
            chatMessages.clear();
            myMessages.clear();
            otherMessages.clear();
            notiMessages.clear();
            User me = DatabaseService.getInstance().getMe();
            for (ChatMessageEntity chatMessageEntity : chatMessageEntities) {
                ChatMessage chatMessage = new ChatMessage();
                if (chatMessageEntity.getIsNotification()) {
                    chatMessage.setUserType(UserType.NOTI);
                    notiMessages.add(chatMessage);
                } else {
                    boolean isMe = Long.valueOf(chatMessageEntity.getUserId()) == me.getUID();
                    if (isMe) {
                        chatMessage.setMessageStatus(chatMessageEntity.getStatus() == 1 ? Status.DELIVERED : Status.SENT);
                        chatMessage.setUserType(UserType.SELF);
                        myMessages.add(chatMessage);
                    } else {
                        chatMessage.setUserType(UserType.OTHER);
                        long senderId = chatMessageEntity.getUserId();
                        if (chatRoomEntity.getIsGroup()) {
                            chatMessage.setSender(members.get(senderId));
                        } else if (attendeeEntity != null) {
                            chatMessage.setSender(attendeeEntity.getName());
                        } else {
                            chatMessage.setSender("");
                        }
                        otherMessages.add(chatMessage);
                    }
                }
                chatMessage.setMessageText(chatMessageEntity.getMsg());
                chatMessage.setMessageTime(chatMessageEntity.getLastUpdated().getTime());
                chatMessages.add(chatMessage);
            }
            listAdapter.notifyDataSetChanged();
        }
        chatRoomEntity.setUnread(0);
        ChatService.getInstance().addUpdateChatRoom(chatRoomEntity);
        ChatService.getInstance().changeChatroomMessageToReadStatus(chatRoomEntity.getId());
        getView().scrollToBottom();
    }

    private void sendMessage(final String messageText) {
        if (messageText.trim().length() == 0)
            return;

        final ChatMessage message = new ChatMessage();
        message.setMessageStatus(Status.SENT);
        message.setMessageText(messageText);
        message.setUserType(UserType.SELF);
        message.setMessageTime(Calendar.getInstance(ChatService.timeZone).getTimeInMillis());
        myMessages.add(message);
        listAdapter.insertLastInternal(chatMessages, message);
        getView().scrollToBottom();

        ChatService.getInstance().chat(messageText, chatRoomEntity);
    }

    @Subscribe
    public void badgeNotiEvent(BadgeNotiEvent event){
        MasterPointService.getInstance().getBadgesAPI();
        MasterPointService.getInstance().showToolTips(getView().chatListView, event.badgeName);
    }
}
