package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.marshalchen.ultimaterecyclerview.UltimateDifferentViewTypeAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.multiViewTypes.DataBinder;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.co.madhur.chatbubblesdemo.AndroidUtilities;
import in.co.madhur.chatbubblesdemo.model.ChatMessage;
import in.co.madhur.chatbubblesdemo.model.Status;
import in.co.madhur.chatbubblesdemo.widgets.Emoji;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.services.ChatService;

/**
 * Created by Aftershock PC on 2/8/2015.
 */
public class ChatMyselfDataBinder extends DataBinder<ChatMyselfDataBinder.ViewHolder> {
    private List<ChatMessage> chatMessages;
    private  static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private Context context;
    private UltimateDifferentViewTypeAdapter dataBindAdapter;

    public ChatMyselfDataBinder(UltimateDifferentViewTypeAdapter dataBindAdapter, Context context, List<ChatMessage> chatMessages) {
        super(dataBindAdapter);
        this.chatMessages = chatMessages;
        this.context = context;
        this.dataBindAdapter = dataBindAdapter;
        SIMPLE_DATE_FORMAT.setTimeZone(ChatService.timeZoneDefault);
    }

    @Override
    public ViewHolder newViewHolder(ViewGroup viewGroup) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_user2_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void bindViewHolder(ViewHolder viewHolder, int position) {
        int binderPosition = dataBindAdapter.getBinderPosition(position);
        if (binderPosition < chatMessages.size()){
            ChatMessage message = chatMessages.get(binderPosition);

            viewHolder.messageTextView.setText(Emoji.replaceEmoji(message.getMessageText(), viewHolder.messageTextView.getPaint().getFontMetricsInt(), AndroidUtilities.dp(16)));
            viewHolder.timeTextView.setText(SIMPLE_DATE_FORMAT.format(message.getMessageTime()));
            viewHolder.message = message;

            if (message.getMessageStatus() == Status.DELIVERED) {
                viewHolder.messageStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_double_tick));
            } else if (message.getMessageStatus() == Status.SENT) {
                viewHolder.messageStatus.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_single_tick));
            }
        }

    }

    @Override
    public int getItemCount() {
        return chatMessages == null ? 0 : chatMessages.size();
    }

    static class ViewHolder extends UltimateRecyclerviewViewHolder {

        @Bind(R.id.user_reply_status)
        public ImageView messageStatus;
        @Bind(R.id.message_text)
        public TextView messageTextView;
        @Bind(R.id.time_text)
        public TextView timeTextView;
        public ChatMessage message;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            UIHelper.getInstance().setTypeface(messageTextView, timeTextView);
            itemView.setLongClickable(true);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ClipboardManager cManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData cData = ClipData.newPlainText(ChatMessagesAdapter.clipboardName, message.getMessageText());
                    cManager.setPrimaryClip(cData);
                    Toast.makeText(getContext(), "A message is Copied.", Toast.LENGTH_LONG).show();
                    return true;
                }
            });
        }
    }
}
