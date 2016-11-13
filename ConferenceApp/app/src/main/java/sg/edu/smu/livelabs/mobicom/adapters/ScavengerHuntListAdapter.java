package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.busEvents.EndScavengerTimerEvent;
import sg.edu.smu.livelabs.mobicom.models.data.ScavengerEntity;
import sg.edu.smu.livelabs.mobicom.models.data.ScavengerWinnersEntity;
import sg.edu.smu.livelabs.mobicom.services.ScavengerService;
import sg.edu.smu.livelabs.mobicom.views.ScavengerHuntView;

/**
 * Created by johnlee on 18/2/16.
 */
public class ScavengerHuntListAdapter extends BaseAdapter {

    private  List<ScavengerEntity> scavengerHunts;
    private ScavengerHuntView scavengerHuntView;
    private Handler mHandler = new Handler();
    private List<ViewHolder> lstHolders;
    private  List<Timer> timers;
    private Bus bus;

    public ScavengerHuntListAdapter(ScavengerHuntView scavengerHuntView, List<ScavengerEntity> scavengerHunts, Bus bus){
        this.scavengerHuntView = scavengerHuntView;
        this.scavengerHunts = scavengerHunts;
        this.timers = new ArrayList<>();
        lstHolders = new ArrayList<>();
        this.bus = bus;
        this.bus.register(this);
        startUpdateTimer();
    }

    @Subscribe
    public void endScavengerTimer(EndScavengerTimerEvent event){
        for(Timer timer: timers){
            timer.cancel();
            timer.purge();
        }

        bus.unregister(this);
    }

    private Runnable updateRemainingTimeRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (lstHolders) {
                Date date= new java.util.Date();
                long currentTime = date.getTime();
                //date.getTime Returns this {@code Date} as a millisecond value. The value is the number of
                // milliseconds since Jan. 1, 1970, midnight GMT.
                //System.currentTimeMillis(); > this method shouldn't be used for measuring timeouts or
                // other elapsed time measurements, as changing the system time can affect the results.
                //thus we need to use date instead
                for (ViewHolder holder : lstHolders) {
                    holder.updateTimeRemaining(currentTime);
                }
            }
        }
    };

    @Override
    public int getCount() {
        return scavengerHunts.size();
    }

    @Override
    public ScavengerEntity getItem(int position) {
        if(scavengerHunts == null){
            return null;
        }
        return scavengerHunts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {

        return getCount();
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            ViewHolder holder = new ViewHolder();
            LayoutInflater vi = (LayoutInflater)scavengerHuntView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.list_item_scavenger, parent, false);

            holder.view = view;
            holder.scavengerHunt = scavengerHunts.get(position);
            holder.gameIV = (CircleImageView)view.findViewById(R.id.game_image);
            holder.titleTV = (TextView)view.findViewById(R.id.game_title);
            holder.summaryTV = (TextView)view.findViewById(R.id.summary);
            holder.winnerTV = (TextView)view.findViewById(R.id.winners);
            holder.overlayMsgTV = (TextView)view.findViewById(R.id.overlay_msg);
            holder.overlayLL = (LinearLayout)view.findViewById(R.id.overlay);

            holder.summaryTV.setEllipsize(TextUtils.TruncateAt.END);
            holder.setRecentWinners(ScavengerService.getInstance().getWinnersByHunt(holder.scavengerHunt.getHuntId()));

            if(holder.scavengerHunt.getIconId() != null && !holder.scavengerHunt.getIconId().isEmpty()) {
                try {
                    Picasso.with(scavengerHuntView.getContext()).load(holder.scavengerHunt.getIconId())
                            .resize((int) scavengerHuntView.getContext().getResources().getDimension(R.dimen.circle_height),
                                    (int) scavengerHuntView.getContext().getResources().getDimension(R.dimen.circle_height))
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .placeholder(R.drawable.game_scavengerhunt).into(holder.gameIV);
                }
                catch(Throwable e){
                    Log.d("AAA", "OOM:ScavengerHuntAdapter"+e.toString());
                }

            }

//            holder.pointsTV.setText(holder.scavengerHunt.getHuntPoints());
            holder.titleTV.setText(holder.scavengerHunt.getTitle());
            holder.summaryTV.setText(holder.scavengerHunt.getDescription());
            holder.winnerTV.setVisibility(View.VISIBLE);

            Date current = Calendar.getInstance().getTime();
            if(current.after(holder.scavengerHunt.getStartTime()) && current.before(holder.scavengerHunt.getEndTime())
                    && !holder.scavengerHunt.getIsCompleted()){ //game started and user have not yet complete it
                holder.overlayLL.setVisibility(View.GONE);
                view.setEnabled(true);
            }
            else if(current.after(holder.scavengerHunt.getStartTime()) && current.before(holder.scavengerHunt.getEndTime())
                    && holder.scavengerHunt.getIsCompleted()){ //game started and user have COMPLETED it
                holder.overlayLL.setVisibility(View.VISIBLE);
                holder.overlayMsgTV.setText("HUNT COMPLETED");
                view.setEnabled(false);
            }
            else if (current.after(holder.scavengerHunt.getEndTime())){ //game ended
                holder.overlayLL.setVisibility(View.VISIBLE);
                holder.overlayMsgTV.setText("HUNT ENDED");
                holder.overlayLL.setBackgroundResource(R.color.transparent_grey);
                holder.gameIV.setAlpha(0.5f);
                holder.titleTV.setAlpha(0.5f);
                holder.summaryTV.setAlpha(0.5f);
                holder.winnerTV.setAlpha(0.5f);
                view.setEnabled(false);
            }
            else if(current.before(holder.scavengerHunt.getStartTime())){ //not stated yet
                holder.overlayLL.setVisibility(View.VISIBLE);
                holder.updateTimeRemaining(System.currentTimeMillis());
                holder.gameIV.setAlpha(0.5f);
                holder.titleTV.setAlpha(0.5f);
                holder.summaryTV.setAlpha(0.5f);
//                holder.winnerTV.setVisibility(View.GONE);
                view.setEnabled(false);
            }

            view.setTag(holder);
            synchronized (lstHolders) {
                lstHolders.add(holder);
            }

        }

        return view;
    }


    public void updates(List<ScavengerEntity> scavengerHunts){
        this.scavengerHunts = scavengerHunts;
        this.notifyDataSetChanged();
        this.notifyDataSetInvalidated();
    }

    private void startUpdateTimer() {
        Timer tmr = new Timer();
        tmr.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(updateRemainingTimeRunnable);
            }
        }, 1000, 1000);

        timers.add(tmr);
    }


    private class ViewHolder {
        CircleImageView gameIV;
        TextView titleTV;
        TextView summaryTV;
        TextView winnerTV;

        TextView overlayMsgTV;
        LinearLayout overlayLL;

        ScavengerEntity scavengerHunt;
        List<ScavengerWinnersEntity> recentWinners;
        String winners = "";
        View view;

        public void setRecentWinners(List<ScavengerWinnersEntity> recentWinners){
            this.recentWinners = recentWinners;
            for(ScavengerWinnersEntity w: recentWinners){
                winners += w.getName() + "\n";
            }
            winnerTV.setText("Recent Winners:\n" + winners);
        }

        public void updateTimeRemaining(long currentTime) {
            //else add the offset
            long timeDiff = scavengerHunt.getStartTime().getTime() - currentTime;
            if (timeDiff > 0) {
                int seconds = (int) (timeDiff / 1000) % 60;
                int minutes = (int) ((timeDiff / (1000 * 60)) % 60);
                int hours = (int) ((timeDiff / (1000 * 60 * 60)) );
                overlayMsgTV.setText("STARTING in \n" + hours + " hrs " + minutes + " mins " + seconds + " sec");
            } else {

                if(scavengerHunt.getIsCompleted()){ //completed the hunt while game still running in public
                    overlayLL.setVisibility(View.VISIBLE);
                    overlayMsgTV.setText("HUNT COMPLETED");
                    titleTV.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 4f));
                    summaryTV.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 7f));
                    //to reset the images and words to original color
                    gameIV.setAlpha(1f);
                    titleTV.setAlpha(1f);
                    summaryTV.setAlpha(1f);
                    winnerTV.setAlpha(1f);
                    winnerTV.setVisibility(View.VISIBLE);
                    view.setEnabled(false);
                }
                else{
                    overlayLL.setVisibility(View.GONE);
                    titleTV.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 4f));
                    summaryTV.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 7f));
                    //to reset the images and words to original color
                    gameIV.setAlpha(1f);
                    titleTV.setAlpha(1f);
                    summaryTV.setAlpha(1f);
                    winnerTV.setAlpha(1f);
                    winnerTV.setVisibility(View.VISIBLE);
                    view.setEnabled(true);
                }


            }

            long timeEndDiff =  currentTime - scavengerHunt.getEndTime().getTime() ;
            if(timeDiff <= 0 && timeEndDiff > 0){
                titleTV.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 4f));
                summaryTV.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 4f));
                overlayLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 3f));

                overlayLL.setVisibility(View.VISIBLE);
                overlayMsgTV.setText("HUNT ENDED");
                overlayLL.setBackgroundResource(R.color.transparent_grey);

                //to set the images and words to unclickable visual
                gameIV.setAlpha(0.5f);
                titleTV.setAlpha(0.5f);
                summaryTV.setAlpha(0.5f);
                winnerTV.setAlpha(0.5f);
                winnerTV.setVisibility(View.VISIBLE);
                view.setEnabled(false);
            }
        }
    }
}
