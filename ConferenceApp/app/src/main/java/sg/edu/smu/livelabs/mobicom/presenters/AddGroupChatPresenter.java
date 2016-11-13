package sg.edu.smu.livelabs.mobicom.presenters;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.isseiaoki.simplecropview.CropImageView;
import com.searchView.SearchView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import autodagger.AutoComponent;
import automortar.AutoScreen;
import automortar.ScreenParam;
import flow.Flow;
import me.kaede.tagview.OnTagClickListener;
import me.kaede.tagview.OnTagDeleteListener;
import me.kaede.tagview.Tag;
import me.kaede.tagview.TagView;
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
import sg.edu.smu.livelabs.mobicom.adapters.AddGroupMemberAdapter;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatRoomCreatedEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatRoomDeleted;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatRoomUpdateCompletedEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.ChatRoomUpdatedEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.SaveAndChangeScreenEvent;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.flow.Layout;
import sg.edu.smu.livelabs.mobicom.models.AttendeeTag;
import sg.edu.smu.livelabs.mobicom.models.data.AttendeeEntity;
import sg.edu.smu.livelabs.mobicom.models.data.ChatRoomEntity;
import sg.edu.smu.livelabs.mobicom.services.AttendeesService;
import sg.edu.smu.livelabs.mobicom.services.ChatService;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.ScreenService;
import sg.edu.smu.livelabs.mobicom.util.Util;
import sg.edu.smu.livelabs.mobicom.views.AddGroupChatView;
import sg.edu.smu.livelabs.mobicom.views.MyCropImageView;

/**
 * Created by Aftershock PC on 27/7/2015.
 */
@AutoScreen(
        component = @AutoComponent(dependencies = MainActivity.class, superinterfaces = AppDependencies.class),
        screenAnnotations = Layout.class
)
@DaggerScope(AddGroupChatPresenter.class)
@Layout(R.layout.add_group_chat_view)
public class AddGroupChatPresenter extends ViewPresenter<AddGroupChatView> implements OnTagClickListener
        , OnTagDeleteListener, AddGroupMemberAdapter.AddGroupMemberListener, View.OnClickListener,
        SearchView.OnSearchListener, MyCropImageView.ReloadingImageListener {
    public static final String NAME = "AddGroupChatPresenter";
    private final ActionBarOwner actionBarOwner;
    private final Bus bus;
    private final MainActivity mainActivity;
    private List<AttendeeEntity> members;
    private AddGroupMemberAdapter adapter;
    private TagView tagView;
    private ScrollView scrollView;
    private String groupAvatar = "";
    private Context context;
    private ScreenService screenService;
    private ChatRoomEntity chatRoomEntity;
    private List<AttendeeEntity> currentFriends;
    private CharSequence searchKey;
    private boolean isEditable;

    public AddGroupChatPresenter(MainActivity mainActivity,
                                 ActionBarOwner actionBarOwner, Bus bus,
                                 ScreenService screenService,
                                 @ScreenParam ChatRoomEntity chatRoomEntity) {
        this.actionBarOwner = actionBarOwner;
        this.bus = bus;
        this.mainActivity = mainActivity;
        this.chatRoomEntity = chatRoomEntity;
        this.screenService = screenService;
        searchKey = "";
    }

    @Override
    protected void onLoad(Bundle savedInstanceState) {
        super.onLoad(savedInstanceState);
        if (!hasView()) return;
        context = getView().getContext();
        actionBarOwner.setConfig(new ActionBarOwner.Config(true, chatRoomEntity == null ?
                "New Group" : "Edit Group", new ActionBarOwner.MenuAction("Done", new Action0() {
            @Override
            public void call() {
                if (getView().mainLayout.getVisibility() == View.VISIBLE){
                    confirm();
                } else if(getView().myCropImageView.getVisibility() == View.VISIBLE) {
                    getView().myCropImageView.done();
                }

            }
        })));
        getView().seachView.setOnSearchListener(this);
        adapter = new AddGroupMemberAdapter(getView().getContext(), new ArrayList<AttendeeTag>(), this);
        getView().friendsList.setAdapter(adapter);
        if (chatRoomEntity != null && ChatService.INACTIVE.equals(chatRoomEntity.getStatus())) {
            getView().deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(context)
                            .title(context.getResources().getString(R.string.app_name))
                            .content("Are you sure you want to delete this group? You will lose all the chat messages.")
                            .positiveText("DELETE")
                            .negativeText("Cancel")
                            .positiveColor(Color.DKGRAY)
                            .negativeColor(Color.DKGRAY)
                            .typeface(UIHelper.getInstance().getExo2LightFont(), UIHelper.getInstance().getNormalFont())
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    UIHelper.getInstance().showProgressDialog(context, "Processing...", true);
                                    ChatService.getInstance().deleteChatRoom(chatRoomEntity);
                                }
                            }).show();
                }
            });
            getView().deleteBtn.setVisibility(View.VISIBLE);
        } else {
            getView().deleteBtn.setVisibility(View.GONE);
        }
        tagView = getView().tagView;
        tagView.setOnTagDeleteListener(this);
        tagView.setOnTagClickListener(this);

        scrollView = getView().scrollView;
        getView().avatarIV.setOnClickListener(this);
        getView().cameraBtn.setOnClickListener(this);
        isEditable = chatRoomEntity == null || chatRoomEntity.getAdmin().equals(DatabaseService.getInstance().getMe().getUID());
        Object temp = screenService.pop(AddGroupChatPresenter.class);
        if (temp != null){//restore
            groupAvatar = (String) temp;
            ArrayList<Tag> tags = screenService.pop(AddGroupChatPresenter.class);
            for (Tag tag : tags){
                tagView.addTag(tag);
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        } else if (chatRoomEntity != null) {
            groupAvatar = chatRoomEntity.getAvatar();
            getView().titleTxt.setText(chatRoomEntity.getTitle());
            String[] userIds = chatRoomEntity.getUserIds().split(",");
            String[] userNames = chatRoomEntity.getUserNames().split(",");
            String[] memberStatuses = chatRoomEntity.getMemberStatus().split(",");
            String[] emails = chatRoomEntity.getEmails().split(",");
            String[] memberAvatars = chatRoomEntity.getAvatarIds().split(",");
            for (int i = 0; i < userIds.length; i++) {
                if (ChatService.ACTIVE.equals(memberStatuses[i])) {
                    Long userId = Long.valueOf(userIds[i]);
                    try{
                        addTag(userId, userNames[i], emails[i], memberAvatars[i],"", isEditable);
                    } catch (Exception e){
                        addTag(userId, userNames[i], "", "-1","", isEditable);
                    }
                }
            }
        }
        Picasso.with(getView().getContext()).load(Util.getPhotoUrlFromId(groupAvatar, 96))
                .noFade().placeholder(R.drawable.empty_group)
                .into(getView().avatarIV);
        if (!isEditable) {
            getView().friendsList.setVisibility(View.GONE);
            getView().seachView.setVisibility(View.GONE);
        } else {
            getView().friendsList.setVisibility(View.VISIBLE);
            getView().seachView.setVisibility(View.VISIBLE);
            refresh();
        }
        getView().myCropImageView.setListener(this, mainActivity, CropImageView.CropMode.CIRCLE);
        mainActivity.setVisibleBottombar(View.GONE);
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
        bus.unregister(this);
        if (NAME.equals(App.getInstance().currentPresenter)){
            App.getInstance().currentPresenter = "";
        }
        super.onExitScope();
    }
    @Subscribe
    public void saveAndChangeScreen(SaveAndChangeScreenEvent event){
        screenService.push(AddGroupChatPresenter.class, (ArrayList<Tag>)tagView.getTags());
        screenService.push(AddGroupChatPresenter.class, groupAvatar);
        Flow.get(getView()).set(event.newScreen);
    }

    @Subscribe
    public void chatRoomDeleted(ChatRoomDeleted event) {
        UIHelper.getInstance().dismissProgressDialog();
        Flow.get(getView()).goBack();
        Flow.get(getView()).goBack();
    }

    private void addTag(Long userId, String name, String email, String avatarId, String description, boolean isEditable){
        Tag tag = new AttendeeTag(userId, name, email, avatarId, description);
        if (chatRoomEntity.getAdmin().equals(userId)){
            tag.isDeletable = false;
            tag.layoutColor = 0xffff0000;
            tag.text = name + ": group admin";
        } else {
            tag.isDeletable = isEditable;
        }
        tagView.addTag(tag);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }

//    private void addTag(FriendEntity friend, boolean isEditable) {
//        Tag tag = new AttendeeTag(friend);
//        tag.isDeletable = isEditable;
//        tagView.addTag(tag);
//        scrollView.fullScroll(View.FOCUS_DOWN);
//    }

    private void confirm() {
        if (getView().mainLayout.getVisibility() == View.GONE){
            getView().myCropImageView.done();
        }
        try {
            final String title = getView().titleTxt.getText().toString().trim();
            if (title.isEmpty()) {
                UIHelper.getInstance().showAlert(getView().getContext(), context.getString(R.string.empty_group_title));
                return;
            }
            final List<Tag> tags = tagView.getTags();
            if (tags == null || tags.isEmpty()) {
                UIHelper.getInstance().showAlert(getView().getContext(), context.getString(R.string.no_friends));
                return;
            }
            if (chatRoomEntity != null) {
                UIHelper.getInstance().showProgressDialog(getView().getContext(), context.getString(R.string.progressing), false);
                if (chatRoomEntity.getAdmin().equals(DatabaseService.getInstance().getMe().getUID())) {
                    ChatService.getInstance().updateGroupChat(title, groupAvatar, tags, chatRoomEntity);
                } else {
                    ChatService.getInstance().updateGroupChat(title, groupAvatar, chatRoomEntity);
                }
            } else {
                UIHelper.getInstance().showProgressDialog(getView().getContext(), "Creating new chat group. Please wait...", false);
                ChatService.getInstance().createGroupChat(title, groupAvatar, tags);
            }

        }catch (Exception e){
            UIHelper.getInstance().showAlert(context, context.getResources().getString(R.string.upload_fail));
            e.printStackTrace();
        }

    }

    @Subscribe
    public void chatRoomCreated(ChatRoomCreatedEvent chatRoomCreatedEvent) {
        UIHelper.getInstance().dismissProgressDialog();
        if (chatRoomCreatedEvent.chatRoomEntity != null) {
            screenService.push(AddGroupChatPresenter.class, chatRoomCreatedEvent.chatRoomEntity.getServerId());
            Flow.get(getView()).goBack();
        } else {
            UIHelper.getInstance().showAlert(getView().getContext(), context.getString(R.string.create_group_chat_error));
        }
    }

    @Subscribe
    public void chatRoomUpdated(ChatRoomUpdatedEvent event) {
        UIHelper.getInstance().dismissProgressDialog();
        if (event.chatRoomEntity == null) {
            UIHelper.getInstance().showAlert(getView().getContext(), context.getString(R.string.create_group_chat_error));
        }
    }

    @Subscribe
    public void chatRoomUpdateCompleted(ChatRoomUpdateCompletedEvent event) {
        Flow.get(getView()).goBack();
    }

    private void refresh() {
        if (!isEditable) return;
        members = AttendeesService.getInstance().getAllAttendees();
        HashMap<Long, AttendeeTag> selectedMap = new HashMap<>();
        List<Tag> currentTags = tagView.getTags();
        if (currentTags != null){
            for (Tag tag : currentTags){
                AttendeeTag attendeeTag = ((AttendeeTag)tag);
                attendeeTag.isAdded = true;
                selectedMap.put(attendeeTag.attendee.getUID(), attendeeTag);
            }
        }
        List<AttendeeTag> tags = new ArrayList<>();
        for (AttendeeEntity friendEntity : members){
            Long key = friendEntity.getUID();
            AttendeeTag attendeeTag;
            if (selectedMap.containsKey(key)){
                attendeeTag = selectedMap.get(key);
                attendeeTag.setDescription(friendEntity.getDescription());
            } else {
                attendeeTag = new AttendeeTag(friendEntity.getUID(), friendEntity.getName(),
                        friendEntity.getEmail(), friendEntity.getAvatar(), friendEntity.getDescription());
            }
            attendeeTag.id = Integer.valueOf(friendEntity.getUID().hashCode());
            tags.add(attendeeTag);
        }
        adapter = new AddGroupMemberAdapter(getView().getContext(), tags, this);
        getView().friendsList.setAdapter(adapter);
        adapter.filter(searchKey);
    }

    @Override
    public void onClick(View v) {
        if (v == getView().avatarIV || v == getView().cameraBtn) {
            getView().myCropImageView.uploadImage();
        }
    }

    @Override
    public void addTag(AttendeeTag tag) {
        tag.isAdded = true;
        tagView.addTag(tag);
        scrollView.fullScroll(View.FOCUS_DOWN);
        adapter.notifyDataSetChanged();
//        adapter.filter(searchKey);
    }

    @Override
    public void removeTag(AttendeeTag tag) {
        tag.isAdded = false;
        List<Tag> tags = tagView.getTags();
        int index = 0;
        for (Tag t : tags){
            if (((AttendeeTag)t).attendee.getUID() == tag.attendee.getUID()){
                break;
            }
            index++;
        }
        tagView.remove(index);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSearch(SearchView evaSearchView, CharSequence constraint) {
        searchKey = constraint;
        adapter.filter(searchKey);
    }

    @Override
    public void onSearchHint(SearchView evaSearchView, CharSequence constraint) {

    }

    @Override
    public void onTagClick(Tag tag, int position) {

    }

    @Override
    public void onTagDeleted(Tag tag, int position) {
        if (tag.id == -1) return; // not friend
        ((AttendeeTag)tag).isAdded = false;
        adapter.notifyDataSetChanged();
//        adapter.filter(searchKey);
    }

    @Override
    public void setNewImage(String avatarId) {
        groupAvatar = avatarId;
        getView().mainLayout.setVisibility(View.VISIBLE);
        UploadFileService.getInstance().loadImage(getView().avatarIV,
                R.drawable.empty_profile, avatarId, 96);
    }

    @Override
    public void hideMainLayout() {
        getView().mainLayout.setVisibility(View.GONE);
    }
}
