package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateDifferentViewTypeAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.co.madhur.chatbubblesdemo.model.ChatMessage;
import in.co.madhur.chatbubblesdemo.model.UserType;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;

/**
 * Created by Aftershock PC on 2/8/2015.
 */
public class ChatMessagesAdapter extends UltimateDifferentViewTypeAdapter {
    private List<ChatMessage> allChatMessages;
    public static SimpleDateFormat df = new SimpleDateFormat("dd MMMM yyyy");
    public static SimpleDateFormat dtf = new SimpleDateFormat("dd MMMM yyyy HH:mm");
    public static String clipboardName = "Chat_clipboard";
    private Context context;

    public ChatMessagesAdapter(Context context, boolean isGroup,
                               List<ChatMessage> allChatMessages, List<ChatMessage> myMessages, List<ChatMessage> otherMessages, List<ChatMessage> notiMessages) {
        this.context = context;
        putBinder(UserType.OTHER, new ChatOtherDataBinder(this, otherMessages, isGroup));
        putBinder(UserType.SELF, new ChatMyselfDataBinder(this, context, myMessages));
        putBinder(UserType.NOTI, new ChatNotiDataBinder(this, notiMessages));
        this.allChatMessages = allChatMessages;
    }

    @Override
    public Enum getEnumFromPosition(int position) {
        ChatMessage chatMessage = allChatMessages.get(position);
        return chatMessage.getUserType();
    }

    @Override
    public Enum getEnumFromOrdinal(int ordinal) {
        return UserType.values()[ordinal];
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(View view) {
        return null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup) {
        return null;
    }

    @Override
    public int getItemCount() {
        return allChatMessages == null ? 0 : allChatMessages.size();
    }

    @Override
    public int getAdapterItemCount() {
        return allChatMessages == null ? 0 : allChatMessages.size();
    }

    @Override
    public long generateHeaderId(int position) {
        if (position >= allChatMessages.size()) return -1;
        ChatMessage message = allChatMessages.get(position);
        return message.getMessageDate().getTime();
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_date_layout, viewGroup, false);
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (position < allChatMessages.size()){
            HeaderViewHolder holder = (HeaderViewHolder) viewHolder;
            holder.dateText.setText(df.format(allChatMessages.get(position).getMessageDate()).toUpperCase());
        }
    }

    public class HeaderViewHolder extends UltimateRecyclerviewViewHolder {

        @Bind(R.id.date_txt)
        public TextView dateText;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            UIHelper.getInstance().setTypeface(dateText);
        }
    }
}
