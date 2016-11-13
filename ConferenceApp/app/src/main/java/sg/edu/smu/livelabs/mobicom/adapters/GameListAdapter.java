package sg.edu.smu.livelabs.mobicom.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import flow.Flow;
import sg.edu.smu.livelabs.mobicom.R;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.GameListEntity;
import sg.edu.smu.livelabs.mobicom.presenters.GamesPresenter;
import sg.edu.smu.livelabs.mobicom.presenters.screen.LeaderboardMainScreen;
import sg.edu.smu.livelabs.mobicom.presenters.screen.ProfileScreen;
import sg.edu.smu.livelabs.mobicom.services.DatabaseService;
import sg.edu.smu.livelabs.mobicom.services.GameService;
import sg.edu.smu.livelabs.mobicom.services.MasterPointService;
import sg.edu.smu.livelabs.mobicom.services.TrackingService;
import sg.edu.smu.livelabs.mobicom.util.Util;
import sg.edu.smu.livelabs.mobicom.views.GamesView;

/**
 * Created by johnlee on 18/2/16.
 */
public class GameListAdapter extends BaseAdapter {

    private List<GameListEntity> games;
    private Bus bus;
    private GamesView gamesView;

    public GameListAdapter(GamesView gamesView, List<GameListEntity> games, Bus bus){
        this.gamesView = gamesView;
        this.games = games;
        this.bus = bus;
    }

    @Override
    public int getCount() {
        return games.size();
    }

    @Override
    public Object getItem(int position) {
        if(position == 0){
            return null;
        }
        return games.get(position);
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
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            LayoutInflater vi = (LayoutInflater)gamesView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(getItemViewType(position) == 0) {
                view = vi.inflate(R.layout.list_item_game_profile, parent, false);

                final User me = DatabaseService.getInstance().getMe();

                final ImageView profilePicIV = (ImageView) view.findViewById(R.id.avatar_image);
                TextView userNameTV = (TextView) view.findViewById(R.id.name);
                TextView totalPointTV = (TextView) view.findViewById(R.id.points);
                final TextView nameShortTV = (TextView) view.findViewById(R.id.name_short_form);
                TextView rankingTV = (TextView) view.findViewById(R.id.ranking);
                Button moreBtn = (Button) view.findViewById(R.id.more_btn);

                if(me != null ){
                    if(me.getAvatar() != null && !me.getAvatar().equals("")){
                        try {
                            Picasso.with(gamesView.getContext()).load(Util.getPhotoUrlFromId(me.getAvatar(), 96))
                                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                                    .networkPolicy(NetworkPolicy.NO_CACHE)
                                    .placeholder(R.drawable.icon_no_profile).into(profilePicIV, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() { //when failed to load, draw a rect on top of the noavatar and add the name short form
                                    GameService.getInstance().drawNameShort(me.getName(), profilePicIV, nameShortTV);
                                }
                            });
                        }
                        catch(Throwable e){
                            Log.d("AAA", "GameListAdapater:" + e.toString());
                        }
                    }
                    else{
                        GameService.getInstance().drawNameShort(me.getName(), profilePicIV, nameShortTV);
                    }

                    if(!me.getName().equals("")){
                        userNameTV.setText(me.getName());
                    }

                    totalPointTV.setText(Integer.toString(MasterPointService.getInstance().getNumberOfUserAchievedBadge()));
                }

                profilePicIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TrackingService.getInstance().sendTracking("402", "games", "profile", " ", " ", " ");
                        Flow.get(gamesView).set(new ProfileScreen(true, ""));
                    }
                });


                SharedPreferences s = gamesView.getContext().getSharedPreferences("Mobicom_MyRankingPref", Context.MODE_PRIVATE);
                int rank = s.getInt("ranking", 0);
                rankingTV.setText("" + rank);


                moreBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TrackingService.getInstance().sendTracking("403", "games", "leaderboard", "", "", "");
//                        Flow.get(gamesView).set(new LeaderboardScreen());
                        Flow.get(gamesView).set(new LeaderboardMainScreen(0, GamesPresenter.NAME));
                    }
                });
            }
            else{
                final GameListEntity game = games.get(position);
                view = vi.inflate(R.layout.list_item_game, parent, false);

                // set value into textview
                TextView gameTitleTV = (TextView) view.findViewById(R.id.game_title);
                TextView gameDesTV = (TextView) view.findViewById(R.id.game_description);
                final ImageView bgIV = (ImageView) view.findViewById(R.id.game_background);


                gameTitleTV.setText(game.getGameName());
                gameDesTV.setText(game.getDescription());

                gameDesTV.setMaxLines(2);
                gameDesTV.setEllipsize(TextUtils.TruncateAt.END);

                if(game.getImageURL() != null && !game.getImageURL().isEmpty()){
//                    System.out.println(">>>>> |" + game.getImageURL() +"|");
//                    Picasso.Builder builder = new Picasso.Builder(gamesView.getContext());
//                    builder.listener(new Picasso.Listener()
//                    {
//                        @Override
//                        public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception)
//                        {
//                            exception.printStackTrace();
//                            System.out.println(">>>>>> " + exception.toString());
//                        }
//                    });
//                    builder.build().load(game.getImageURL()).fit().centerInside().into(bgIV);
//                    Picasso.with(gamesView.getContext()).load(games.get(position-1).getResourceId()).fit().centerInside().into(bgIV);
                    Picasso.with(gamesView.getContext()).load(game.getImageURL())
                            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .fit().centerInside().into(bgIV, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            int defaultGameIcon = R.drawable.default_game;
//                            if(game.getKeyword().equals("ice breaker")){
//                                defaultGameIcon = R.drawable.game_icebreaker;
//                            }
//                            else if(game.getKeyword().equals("scavenger hunt")){
//                                defaultGameIcon = R.drawable.game_scavengerhunt;
//                            }
//                            else if(game.getKeyword().equals("polling")){
//                                defaultGameIcon = R.drawable.game_voting;
//                            }
//                            else if(game.getKeyword().equals("survey")){
//                                defaultGameIcon = R.drawable.game_survey;
//                            }
//                            else if(game.getKeyword().equals("quiz")){
//                                defaultGameIcon = R.drawable.game_quiz;
//                            }
//                            else if(game.getKeyword().equals("demo")){
//                                defaultGameIcon = R.drawable.game_demo;
//                            }
//                            else if(game.getKeyword().equals("coolfie")){
//                                defaultGameIcon = R.drawable.game_coolfie;
//                            }
//                            else if(game.getKeyword().equals("favourite")){
//                                defaultGameIcon = R.drawable.game_voting;
//                            }
//                            else{ //if(game.getId() == 9)
//                                defaultGameIcon = R.drawable.game_rofl;
//                            }

                            Picasso.with(gamesView.getContext()).load(defaultGameIcon)
                                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                                    .networkPolicy(NetworkPolicy.NO_CACHE)
                                    .fit().centerInside().into(bgIV);
                        }
                    });
                }
                else{
                    int defaultGameIcon = 0;
                    if(game.getId() == 1){
                        defaultGameIcon = R.drawable.game_icebreaker;
                    }
                    else if(game.getId() == 2){
                        defaultGameIcon = R.drawable.game_scavengerhunt;
                    }
                    else if(game.getId() == 3){
                        defaultGameIcon = R.drawable.game_survey;
                    }
                    else if(game.getId() == 4){
                        defaultGameIcon = R.drawable.game_survey;
                    }
                    else if(game.getId() == 5){
                        defaultGameIcon = R.drawable.game_quiz;
                    }
                    else if(game.getId() == 6){
                        defaultGameIcon = R.drawable.game_demo;
                    }
                    else if(game.getId() == 7){
                        defaultGameIcon = R.drawable.game_coolfie;
                    }
                    else if(game.getId() == 8){
                        defaultGameIcon = R.drawable.game_voting;
                    }
                    else{ //if(game.getId() == 9)
                        defaultGameIcon = R.drawable.game_rofl;
                    }

                    Picasso.with(gamesView.getContext()).load(defaultGameIcon)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .fit().centerInside().into(bgIV);
                }

            }
        }

        return view;
    }


    public void updates(List<GameListEntity> games){
        this.games = games;
        this.notifyDataSetChanged();
        this.notifyDataSetInvalidated();
    }
}
