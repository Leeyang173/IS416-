package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.SwipeableUltimateViewAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.swipe.SwipeLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import flow.Flow;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.fileupload.UploadFileService;
import sg.edu.smu.livelabs.mobicom.models.data.AttendeeEntity;
import sg.edu.smu.livelabs.mobicom.models.data.ChatRoomEntity;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ChatScreen;
import sg.edu.smu.livelabs.mobicom.services.AttendeesService;
import sg.edu.smu.livelabs.mobicom.services.ChatService;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;

/**
 * Created by smu on 8/3/16.
 */
public class ChatListAdapter extends SwipeableUltimateViewAdapter implements Filterable {

    private ChatFilter chatFilter = new ChatFilter();
    private List<ChatRoomEntity> originalChats = new ArrayList<>();
    private List<ChatRoomEntity> chats = new ArrayList<>();
    private Context context;

    public ChatListAdapter(Context context){
        this.context = context;
    }

    public void setChats(List<ChatRoomEntity> chats){
        this.chats.clear();
        this.chats.addAll(chats);
        this.originalChats.clear();
        this.originalChats.addAll(chats);
        notifyDataSetChanged();
    }

    @Override
    public UltimateRecyclerviewViewHolder getViewHolder(View view) {
        return new ViewHolder(view, false);
    }

    @Override
    public UltimateRecyclerviewViewHolder  onCreateViewHolder(ViewGroup viewGroup) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.chat_room_item, viewGroup, false);
        final ViewHolder viewHolder = new ViewHolder(v, true);
        SwipeLayout swipeLayout = viewHolder.swipeLayout;
        swipeLayout.setDragEdge(SwipeLayout.DragEdge.Right);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        return viewHolder;
    }

    @Override
    public int getAdapterItemCount() {
        return chats == null ? 0 : chats.size();
    }

    @Override
    public long generateHeaderId(int position) {
        return -1;
    }

    @Override
    public void onBindViewHolder(UltimateRecyclerviewViewHolder  ultimateRecyclerviewViewHolder, int position) {
        if (position < getItemCount() &&
                (customHeaderView != null ? position <= chats.size() : position < chats.size()) &&
                (customHeaderView != null ? position > 0 : true)) {
            ViewHolder holder = (ViewHolder) ultimateRecyclerviewViewHolder;
            ChatRoomEntity chat = chats.get(customHeaderView != null ? position - 1 : position);
            HashMap<Long, String> memberMap = new HashMap<Long, String>();
            if (chat.getIsGroup()) {
                String[] userIds = chat.getUserIds().split(",");
                String[] names = chat.getUserNames().split(",");
                for (int i = 0; i < userIds.length; i++) {
                    memberMap.put(Long.valueOf(userIds[i]), names[i].trim());
                }
            } else {
                AttendeeEntity friend = AttendeesService.getInstance().getAttendeesByUID(chat.getServerId());
                if (friend != null) {
                    memberMap.put(friend.getUID(), friend.getName());
                }
            }
            memberMap.put(DatabaseService.getInstance().getMe().getUID(), "Me");

            holder.chatEntity = chat;
            holder.nameTxt.setText(chat.getTitle());
            if (chat.getLastMessage() != null && !chat.getLastMessage().isEmpty()) {
                if (chat.getIsGroup()){
                    holder.lastMessageTxt.setText(memberMap.get(chat.getLastUserId()) + ": " + chat.getLastMessage());
                } else {
                    holder.lastMessageTxt.setText(chat.getLastMessage());
                }
            } else {
                holder.lastMessageTxt.setText("");
            }

            if (chat.getIsGroup()) {
                if (ChatService.ACTIVE.equals(chat.getStatus())) {
                    holder.deleteBtn.setText("Leave");
                } else {
                    holder.deleteBtn.setText("Delete");
                }
                UploadFileService.getInstance().loadImage(holder.profileIV, R.drawable.empty_group,
                        chat.getAvatar(), 96);
            } else {
                holder.deleteBtn.setText("Delete");
                UploadFileService.getInstance().loadAvatar(holder.profileIV, chat.getAvatar(),
                        holder.emptyProfileTV, chat.getTitle());
            }
            if (chat.getLastMessageTime() != null) {
                holder.timeTxt.setText(DateUtils.getRelativeTimeSpanString(chat.getLastMessageTime().getTime()));
            } else {
                holder.timeTxt.setText("");
            }
            if (chat.getUnread() != null && chat.getUnread() > 0) {
                holder.badgeTxt.setText(chat.getUnread().toString());
                holder.badgeTxt.setVisibility(View.VISIBLE);
            } else {
                holder.badgeTxt.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
        return null;
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

    }

    public void reset(){
        chats.clear();
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return chatFilter;
    }

    public class ViewHolder extends UltimateRecyclerviewViewHolder implements View.OnClickListener {
        @Bind(R.id.main_container)
        public RelativeLayout mainContainer;
        @Bind(R.id.empty_profile_tv)
        public TextView emptyProfileTV;
        @Bind(R.id.profile_image)
        public ImageView profileIV;
        @Bind(R.id.name_txt)
        public TextView nameTxt;
        @Bind(R.id.last_message_txt)
        public TextView lastMessageTxt;
        @Bind(R.id.time_txt)
        public TextView timeTxt;
        @Bind(R.id.delete)
        public Button deleteBtn;
        @Bind(R.id.text_container)
        public View textContainer;
        @Bind(R.id.badge_txt)
        public TextView badgeTxt;

        private ChatRoomEntity chatEntity;

        public ViewHolder(View itemView, boolean isItem) {
            super(itemView);
            if (isItem) {
                ButterKnife.bind(this, itemView);
                UIHelper uiHelper = UIHelper.getInstance();
                uiHelper.setExo2LightTypeFace(deleteBtn);
                uiHelper.setBoldTypeface(emptyProfileTV);
                uiHelper.setBoldTypeface(nameTxt);
                uiHelper.setItalicTypeface(lastMessageTxt);
                uiHelper.setTypeface( timeTxt, badgeTxt);
                deleteBtn.setOnClickListener(this);
                mainContainer.setOnClickListener(this);
                itemView.setOnClickListener(this);
            }
        }

        public ChatRoomEntity getChatEntity() {
            return chatEntity;
        }

        public void setChatEntity(ChatRoomEntity chatEntity) {
            this.chatEntity = chatEntity;
        }

        @Override
        public void onClick(View v) {
            if (v == deleteBtn) {
                UIHelper.getInstance().showProgressDialog(context, "Processing...", true);
                if (chatEntity.getIsGroup()) {
                    if (ChatService.ACTIVE.equals(chatEntity.getStatus())) {
                        ChatService.getInstance().leaveChatRoom(chatEntity);
                    } else {
                        ChatService.getInstance().deleteChatRoom(chatEntity);
                    }
                } else {
                    ChatService.getInstance().deleteSingleChat(chatEntity);
                }
            } else {
                Flow.get(context).set(new ChatScreen(chatEntity));
                if (chatEntity.getIsGroup()){
                    TrackingService.getInstance().sendTracking("207", "messages", "messages", chatEntity.getServerId().toString(), "", "");
                } else {
                    TrackingService.getInstance().sendTracking("208", "messages", "messages", chatEntity.getServerId().toString(), "", "");
                }
            }
        }
    }

    class ChatFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (TextUtils.isEmpty(constraint)) {
                filterResults.values = originalChats;
                filterResults.count = originalChats.size();
            } else {
                List<ChatRoomEntity> values = new ArrayList<>();
                for (ChatRoomEntity item : originalChats) {
                    if (item.getTitle().toLowerCase().contains(constraint)) {
                        values.add(item);
                    }
                }
                filterResults.count = values.size();
                filterResults.values = values;
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            chats.clear();
            if (constraint.length() == 0){
                chats.addAll(originalChats);
                notifyDataSetChanged();
            } else if (results.count > 0){
                chats.addAll((List<ChatRoomEntity>)results.values);
                notifyDataSetChanged();
            }

        }
    }
}
