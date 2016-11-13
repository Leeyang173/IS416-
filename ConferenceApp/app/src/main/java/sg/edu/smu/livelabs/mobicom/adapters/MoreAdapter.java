package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import flow.Flow;
import sg.edu.smu.livelabs.mobicom.ActionBarOwner;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.MainActivity;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.presenters.AgendaPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.MorePresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.AboutUsScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.BeaconScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.FeedbackScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ForumScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.InboxScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.LeaderboardMainScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.MemoriesMainScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.OrganizersScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.PrizesScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ProfileScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SponsorScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.SurveyScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.VotingListScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.VotingScreen;
import sg.edu.smu.livelabs.mobicom.services.AgendaService;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.ForumService;
import sg.edu.smu.livelabs.mobicom.services.MemoriesService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;

/**
 * Created by smu on 4/4/16.
 */
public class MoreAdapter extends BaseAdapter {
    public List<Item> items;
    public static final int AGENDA = 0;
    public static final int DEMO = 1;
    public static final int MEMORIES = 2;
    public static final int PAPERS = 3;
    public static final int ATTENDEES = 4;
    public static final int FORUM = 5;
    public static final int MESSAGE = 6;
    public static final int NOTIFICATION = 7;
    public static final int GAMES = 8;
    public static final int LEADERBOARD = 9;
    public static final int POLL = 10;
    public static final int PRIZES = 11;
    public static final int PROFILE = 12;
    public static final int SURVEY = 13;
    public static final int ORGANISERS = 14;
    public static final int FEEDBACK = 15;
    public static final int SPONSOR = 16;
    public static final int YOUR_SG = 17;
    public static final int ABOUT_US = 18;
//    public static final int AWARDS = 19;

    private Context context;
    private MainActivity mainActivity;
    public MoreAdapter(Context context, MainActivity mainActivity){
        this.context = context;
        this.mainActivity = mainActivity;
        this.items = new ArrayList<>();
        this.items.add(new Item("Demos", DEMO, R.drawable.icon_demo,ContextCompat.getColor(context, R.color.colorPrimary)));
        this.items.add(new Item("Memories", MEMORIES,R.drawable.icon_memories, ContextCompat.getColor(context, R.color.colorPrimary)));
        this.items.add(new Item("Papers", PAPERS,R.drawable.icon_paper_white, ContextCompat.getColor(context, R.color.colorPrimary)));
        this.items.add(new Item("Attendees", ATTENDEES,R.drawable.icon_attendee, ContextCompat.getColor(context, R.color.colorPrimary)));
        this.items.add(new Item("Forum", FORUM,R.drawable.icon_forum, ContextCompat.getColor(context, R.color.colorPrimary)));
        this.items.add(new Item("Notification", NOTIFICATION,R.drawable.icon_notification, ContextCompat.getColor(context, R.color.colorPrimary)));
        this.items.add(new Item("Leaderboard", LEADERBOARD,R.drawable.icon_ranking, ContextCompat.getColor(context, R.color.colorPrimary)));
        this.items.add(new Item("Poll", POLL,R.drawable.icon_poll, ContextCompat.getColor(context, R.color.colorPrimary)));
//        this.items.add(new Item("Prizes", PRIZES, R.drawable.icon_prizes, ContextCompat.getColor(context, R.color.colorPrimary)));
        this.items.add(new Item("Profile", PROFILE, R.drawable.icon_profile_default, ContextCompat.getColor(context, R.color.colorPrimary)));
        this.items.add(new Item("Survey", SURVEY, R.drawable.icon_survey, ContextCompat.getColor(context, R.color.colorPrimary)));
        this.items.add(new Item("Committee", ORGANISERS, R.drawable.icon_organisers_white, ContextCompat.getColor(context, R.color.colorPrimary)));
        this.items.add(new Item("Feedback", FEEDBACK, R.drawable.icon_feedback, ContextCompat.getColor(context, R.color.colorPrimary)));
        this.items.add(new Item("With Thanks", SPONSOR, R.drawable.icon_sponsors_white, ContextCompat.getColor(context, R.color.colorPrimary)));
//        this.items.add(new Item("Your SG", YOUR_SG, R.drawable.icon_sg, ContextCompat.getColor(context, R.color.colorPrimary)));
        this.items.add(new Item("About Us", ABOUT_US, R.drawable.icon_aboutus, ContextCompat.getColor(context, R.color.colorPrimary)));
        this.items.add(new Item("Games", GAMES, R.drawable.icon_game, ContextCompat.getColor(context, R.color.colorPrimary)));
//        this.items.add(new Item("Awards", AWARDS, R.drawable.icon_award, ContextCompat.getColor(context, R.color.colorPrimary)));
        Collections.sort(items, new Comparator<Item>() {
            @Override
            public int compare(Item item1, Item item2) {
                return item1.name.compareTo(item2.name);
            }
        });
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public String getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= getCount()) return null;
        Item item = items.get(position);
        ViewHolder viewHolder;
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.more_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.icon.setImageResource(item.image);
        viewHolder.title.setText(item.name);
        viewHolder.bgLV.setBackgroundColor(item.color);
        viewHolder.id = item.id;
        return convertView;
    }

    public class ViewHolder implements View.OnClickListener{
        @Bind(R.id.icon_img)
        public ImageView icon;
        @Bind(R.id.title_txt)
        public TextView title;
        public int id;
        @Bind(R.id.background_view)
        public LinearLayout bgLV;
        
        public ViewHolder(View view){
            ButterKnife.bind(this,view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            App.getInstance().currentPresenter = "MorePresenter";
            switch (id){
                case AGENDA:
                    TrackingService.getInstance().sendTracking("502", "more", "agenda", " ", " ", " ");
                    mainActivity.setCurrentTab(MainActivity.AGENDA_TAB, AgendaPresenter.FULL_AGENDA);
                    break;
                case GAMES:
                    TrackingService.getInstance().sendTracking("503", "more", "games", " ", " ", " ");
                    mainActivity.setCurrentTab(MainActivity.GAMES_TAB, -1);
//                    Flow.get(context).set(new GamesScreen());
                    break;
                case ORGANISERS:
                    TrackingService.getInstance().sendTracking("504", "more", "committee", " ", " ", " ");
                    Flow.get(context).set(new OrganizersScreen());
                    break;
                case YOUR_SG:
                    TrackingService.getInstance().sendTracking("505", "more", "your SG", " ", " ", " ");
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.yoursingapore.com/en.html"));
                    mainActivity.startActivity(browserIntent);
//                    Flow.get(context).set(new YourSGScreen());
                    break;
                case PROFILE:
                    TrackingService.getInstance().sendTracking("506", "more", "profile", " ", " ", " ");
                    Flow.get(context).set(new ProfileScreen(true, ""));
                    break;
                case SPONSOR:
                    TrackingService.getInstance().sendTracking("507", "more", "with thanks", " ", " ", " ");
                    Flow.get(context).set(new SponsorScreen());
                    break;
                case LEADERBOARD:
                    TrackingService.getInstance().sendTracking("508", "more", "leaderboard", " ", " ", " ");
//                    Flow.get(context).set(new LeaderboardScreen());
                    Flow.get(context).set(new LeaderboardMainScreen(0, MorePresenter.NAME));
                    break;
                case PAPERS:
                    TrackingService.getInstance().sendTracking("509", "more", "papers", " ", " ", " ");
                    mainActivity.setCurrentTab(MainActivity.AGENDA_TAB, AgendaPresenter.PAPER);
                    break;
                case FORUM:
                    TrackingService.getInstance().sendTracking("510", "more", "forum", " ", " ", " ");
                    Flow.get(context).set(new ForumScreen());
                    UIHelper.getInstance().showProgressDialog(context, context.getString(R.string.progressing), true);
                    AgendaService.getInstance().getMainTopic();
                    ForumService.getInstance().getTopics();
                    break;
                case MESSAGE:
                    TrackingService.getInstance().sendTracking("511", "more", "messages", " ", " ", " ");
                    mainActivity.setCurrentTab(MainActivity.MESSAGE_TAB, ActionBarOwner.Config.MIDDLE_FOCUS);
                    break;
                case ATTENDEES:
                    TrackingService.getInstance().sendTracking("512", "more", "attendees", " ", " ", " ");
                    mainActivity.setCurrentTab(MainActivity.MESSAGE_TAB, ActionBarOwner.Config.LEFT_FOCUS);
                    break;
                case DEMO:
                    TrackingService.getInstance().sendTracking("513", "more", "demo", " ", " ", " ");
                    Flow.get(context).set(new BeaconScreen());
                    break;
                case POLL:
                    TrackingService.getInstance().sendTracking("514", "more", "poll", " ", " ", " ");
                    if (DatabaseService.getInstance().getMe().isModerator())
                        Flow.get(context).set(new VotingScreen(0, "Polling"));
                    else
                        Flow.get(context).set(new VotingListScreen());
                    break;
                case PRIZES:
                    TrackingService.getInstance().sendTracking("515", "more", "prizes", " ", " ", " ");
                    Flow.get(context).set(new PrizesScreen());
                    break;
                case SURVEY:
                    TrackingService.getInstance().sendTracking("516", "more", "survey", " ", " ", " ");
                    Flow.get(context).set(new SurveyScreen());
                    break;
                case FEEDBACK:
                    Flow.get(context).set(new FeedbackScreen());
                    break;
                case MEMORIES:
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.YEAR, 2016);
                    c.set(Calendar.MONTH, Calendar.OCTOBER);
                    c.set(Calendar.DAY_OF_MONTH, 3);
                    MemoriesService.getInstance().currentSelectedDate = c.getTime(); //new Date();
                    MemoriesService.getInstance().startDate = c.getTime();
//                    TrackingService.getInstance().sendTracking("516", "more", "survey", " ", " ", " ");
                    Flow.get(context).set(new MemoriesMainScreen());
                    break;
                case NOTIFICATION:
                    Flow.get(context).set(new InboxScreen());
                    break;
                case ABOUT_US:
                    Flow.get(context).set(new AboutUsScreen());
                    break;
//                case AWARDS:
//                    Flow.get(context).set(new AwardsScreen());
//                    break;
            }
        }
    }

    public class Item{
        String name;
        int image;
        int color;
        int id;
        public Item(String name, int id, int image, int color){
            this.name = name;
            this.image = image;
            this.color = color;
            this.id = id;
        }
    }
}
