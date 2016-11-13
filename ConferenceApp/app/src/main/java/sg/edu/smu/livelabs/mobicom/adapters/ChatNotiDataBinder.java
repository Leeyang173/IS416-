package sg.edu.smu.livelabs.mobicom.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateDifferentViewTypeAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.multiViewTypes.DataBinder;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.co.madhur.chatbubblesdemo.model.ChatMessage;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.R;

/**
 * Created by Aftershock PC on 2/8/2015.
 */
public class ChatNotiDataBinder extends DataBinder<ChatNotiDataBinder.ViewHolder> {
    private List<ChatMessage> chatMessages;
    private UltimateDifferentViewTypeAdapter dataBindAdapter;

    public ChatNotiDataBinder(UltimateDifferentViewTypeAdapter dataBindAdapter, List<ChatMessage> chatMessages) {
        super(dataBindAdapter);
        this.chatMessages = chatMessages;
        this.dataBindAdapter = dataBindAdapter;
    }

    @Override
    public ViewHolder newViewHolder(ViewGroup viewGroup) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_date_layout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void bindViewHolder(ViewHolder viewHolder, int position) {
        int binPosition = dataBindAdapter.getBinderPosition(position);
        if (binPosition < chatMessages.size()){
            ChatMessage message = chatMessages.get(binPosition);
            viewHolder.notiTextView.setText(message.getMessageText());
        }

    }

    @Override
    public int getItemCount() {
        return chatMessages == null ? 0 : chatMessages.size();
    }

    static class ViewHolder extends UltimateRecyclerviewViewHolder {

        @Bind(R.id.date_txt)
        public TextView notiTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            UIHelper.getInstance().setTypeface(notiTextView);
        }
    }
}
