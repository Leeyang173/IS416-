package sg.edu.smu.livelabs.mobicom.net;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.socialplus.autorest.CommentLikesOperations;
import com.microsoft.socialplus.autorest.CommentLikesOperationsImpl;
import com.microsoft.socialplus.autorest.CommentReportsOperations;
import com.microsoft.socialplus.autorest.CommentReportsOperationsImpl;
import com.microsoft.socialplus.autorest.CommentsOperations;
import com.microsoft.socialplus.autorest.CommentsOperationsImpl;
import com.microsoft.socialplus.autorest.SocialPlusClient;
import com.microsoft.socialplus.autorest.SocialPlusClientImpl;
import com.microsoft.socialplus.autorest.TopicCommentsOperations;
import com.microsoft.socialplus.autorest.TopicCommentsOperationsImpl;
import com.microsoft.socialplus.autorest.TopicLikesOperations;
import com.microsoft.socialplus.autorest.TopicLikesOperationsImpl;
import com.microsoft.socialplus.autorest.TopicReportsOperations;
import com.microsoft.socialplus.autorest.TopicReportsOperationsImpl;
import com.microsoft.socialplus.autorest.TopicsOperations;
import com.microsoft.socialplus.autorest.TopicsOperationsImpl;
import com.microsoft.socialplus.autorest.UserTopicsOperations;
import com.microsoft.socialplus.autorest.UserTopicsOperationsImpl;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.text.SimpleDateFormat;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.fileupload.FileUploadApi;
import sg.edu.smu.livelabs.mobicom.net.api.AgendaApi;
import sg.edu.smu.livelabs.mobicom.net.api.AttendeeApi;
import sg.edu.smu.livelabs.mobicom.net.api.BEPApi;
import sg.edu.smu.livelabs.mobicom.net.api.BadgeApi;
import sg.edu.smu.livelabs.mobicom.net.api.BeaconApi;
import sg.edu.smu.livelabs.mobicom.net.api.BingoApi;
import sg.edu.smu.livelabs.mobicom.net.api.ChatApi;
import sg.edu.smu.livelabs.mobicom.net.api.EVAPromotionAPI;
import sg.edu.smu.livelabs.mobicom.net.api.FavoriteApi;
import sg.edu.smu.livelabs.mobicom.net.api.FeedbackApi;
import sg.edu.smu.livelabs.mobicom.net.api.GameApi;
import sg.edu.smu.livelabs.mobicom.net.api.IceBreakerApi;
import sg.edu.smu.livelabs.mobicom.net.api.MemoriesApi;
import sg.edu.smu.livelabs.mobicom.net.api.PollingApi;
import sg.edu.smu.livelabs.mobicom.net.api.ProfileApi;
import sg.edu.smu.livelabs.mobicom.net.api.QuizApi;
import sg.edu.smu.livelabs.mobicom.net.api.ScavengerApi;
import sg.edu.smu.livelabs.mobicom.net.api.StumpApi;
import sg.edu.smu.livelabs.mobicom.net.api.SurveyApi;
import sg.edu.smu.livelabs.mobicom.net.api.TrackingApi;

/**
 * Created by smu on 21/1/16.
 */
public class RestClient {
    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final String BASE_URL = "https://apollo.smu.edu.sg/mobicom_sandbox/";//"https://mobicom16.winlab.rutgers.edu/mobicom_sandbox/";//DEV
    public static final String TEST_URL = "https://athena.smu.edu.sg/hestia/analytics_sandbox/";//"https://mobicom16.winlab.rutgers.edu/mobicom_sandbox/";//DEV
//    public static final String BASE_URL = "https://mobicom16.winlab.rutgers.edu/mobicom/";//PRODUCT

    public static final String PHOTO_BASE_URL = BASE_URL + "backend/mobicom/assets/";
    public static final String API_BASE_URL = BASE_URL + "backend/mobicom/index.php/";
    public static final String BEP_API_BASE_URL = BASE_URL + "bep/intervention_engine/index.php/";
    public static final String LOC_MAC_URL = TEST_URL + "/smulabs/index.php/Point_location/getUserLocationByMAC";
    public static final String LOC_IP_URL = TEST_URL + "/smulabs/index.php/get_mac_from_ip";

    public static final String WEBVIEW_BASE_URL = BASE_URL + "portal/confwebapp/index.php/";
    public static final String LEADERBOARD_BASE_URL = BASE_URL + "portal/confwebapp/index.php/leaderboard?user_id=";
    public static final String PRIZE_BASE_URL = BASE_URL + "portal/confwebapp/index.php/prizes";
    public static final String SURVEY_RESULT = BASE_URL + "portal/confwebapp/index.php/Survey_result/get_survey_result/";
    public static final String AWARDS_RESULT = BASE_URL + "portal/confwebapp/index.php/award";

//    public static final String SOCIAL_PLUS_URL = "https://api.embeddedsocial.microsoft.com/";//Production
//    public static final String APP_KEY = "f405011c-e767-4768-8c48-8e8cda4fa8b4";//Production
    public static final String SOCIAL_PLUS_URL = "https://ppe.embeddedsocial.microsoft.com/";//Dev
    public static final String APP_KEY = "28a233a9-e11d-4496-a8f5-7437d196a4c2";//Dev

    public static final String KEY = "35uyh4gy89h34g98t7gh349875ygh45g638gybiu643h53t45gt";


    private ChatApi chatApi;
    private AgendaApi agendaApi;
    private FileUploadApi fileUploadApi;
    private ProfileApi profileApi;
    private BeaconApi beaconApi;
    private EVAPromotionAPI selfieAPI;
    private IceBreakerApi iceBreakerApi;
    private PollingApi pollingApi;
    private SurveyApi surveyApi;
    private QuizApi quizApi;
    private BadgeApi badgeApi;
    private AttendeeApi attendeeApi;
    private ScavengerApi scavengerApi;
    private FavoriteApi favoriteApi;
    private GameApi gameApi;
    private BEPApi bepApi;
    private StumpApi stumpApi;
    private TrackingApi trackingApi;
    private FeedbackApi feedbackApi;
    private MemoriesApi memoriesApi;
    private BingoApi bingoApi;

    ////Social plus
    private retrofit2.Retrofit retrofitSocail;
    private SocialPlusClient socialPlusClient;
    private TopicsOperations topicApis;
    private TopicCommentsOperations topicCommentsApis;
    private CommentsOperations commentsApis;
    private TopicReportsOperations topicReportsApis;
    private CommentReportsOperations commentReportsApis;
    private TopicLikesOperations topicLikesApis;
    private CommentLikesOperations commentLikesApis;
    private UserTopicsOperations userTopicsApis;

    public RestClient(){

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .serializeNulls()
                .create();
        OkHttpClient okHttpclient = new OkHttpClient();
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        okHttpclient.setCookieHandler(cookieManager); //finally set the cookie handler on client
//        okHttpclient.interceptors().add(new Interceptor() {
//            @Override
//            public Response intercept(Chain chain) throws IOException {
//                Request.Builder ongoing = chain.request().newBuilder();
//                ongoing.addHeader("Accept", "application/json;versions=1");
//                User user = DatabaseService.getInstance().getMe();
//                if (user.getUID() != -1) {
//                    ongoing.addHeader("Authorization", "somekey");
//                }
//                return chain.proceed(ongoing.build());
//            }
//        });
//        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
//        okHttpclient.interceptors().add(logging);
//        okHttpclient.interceptors().add(new Interceptor() {
//            @Override
//            public Response intercept(Chain chain) throws IOException {
//                Request request = chain.request();
//                HttpUrl url = request.httpUrl().newBuilder()
//                        .addQueryParameter("appversion", App.appVersion)
//                        .addQueryParameter("appid", App.APP_ID + "")
//                        .build();
//                request = request.newBuilder().url(url).build();
//                return chain.proceed(request);
//            }
//        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpclient)
                .build();
        chatApi = retrofit.create(ChatApi.class);
        agendaApi = retrofit.create(AgendaApi.class);
        attendeeApi = retrofit.create(AttendeeApi.class);
        profileApi = retrofit.create(ProfileApi.class);
        beaconApi = retrofit.create(BeaconApi.class);
        selfieAPI = retrofit.create(EVAPromotionAPI.class);
        iceBreakerApi = retrofit.create(IceBreakerApi.class);
        pollingApi = retrofit.create(PollingApi.class);
        surveyApi = retrofit.create(SurveyApi.class);
        quizApi = retrofit.create(QuizApi.class);
        badgeApi = retrofit.create(BadgeApi.class);
        scavengerApi = retrofit.create(ScavengerApi.class);
        favoriteApi = retrofit.create(FavoriteApi.class);
        gameApi = retrofit.create(GameApi.class);
        stumpApi = retrofit.create(StumpApi.class);
        trackingApi = retrofit.create(TrackingApi.class);
        feedbackApi = retrofit.create(FeedbackApi.class);
        memoriesApi = retrofit.create(MemoriesApi.class);
        bingoApi = retrofit.create(BingoApi.class);

        Retrofit retrofitBEP = new Retrofit.Builder()
                .baseUrl(BEP_API_BASE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpclient)
                .build();
        bepApi = retrofitBEP.create(BEPApi.class);

        Retrofit retrofitUploadFile = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
//        retrofitUploadFile.client().interceptors().add(logging);
        fileUploadApi = retrofitUploadFile.create(FileUploadApi.class);



        //// social plus
        okhttp3.logging.HttpLoggingInterceptor logging1 = new okhttp3.logging.HttpLoggingInterceptor();
        logging1.setLevel(okhttp3.logging.HttpLoggingInterceptor.Level.BODY);
        okhttp3.OkHttpClient.Builder httpClient = new okhttp3.OkHttpClient.Builder();
        httpClient.addInterceptor(logging1);
        retrofitSocail = new retrofit2.Retrofit.Builder()
                .baseUrl(SOCIAL_PLUS_URL)
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        socialPlusClient = new SocialPlusClientImpl();
        topicApis = new TopicsOperationsImpl(retrofitSocail, socialPlusClient);
        topicCommentsApis = new TopicCommentsOperationsImpl(retrofitSocail, socialPlusClient);
        commentsApis = new CommentsOperationsImpl(retrofitSocail, socialPlusClient);
        topicReportsApis = new TopicReportsOperationsImpl(retrofitSocail, socialPlusClient);
        commentReportsApis = new CommentReportsOperationsImpl(retrofitSocail, socialPlusClient);
        topicLikesApis = new TopicLikesOperationsImpl(retrofitSocail, socialPlusClient);
        commentLikesApis = new CommentLikesOperationsImpl(retrofitSocail, socialPlusClient);
        userTopicsApis = new UserTopicsOperationsImpl(retrofitSocail, socialPlusClient);

    }

    public AgendaApi getAgendaApi() {return agendaApi;}

    public ChatApi getChatApi(){
        return chatApi;
    }

    public FileUploadApi getFileUploadApi() {
        return fileUploadApi;
    }

    public ProfileApi getProfileApi() {
        return profileApi;
    }

    public BeaconApi getBeaconApi() {
        return beaconApi;
    }

    public EVAPromotionAPI getSelfieAPI() {
        return selfieAPI;
    }

    public IceBreakerApi getIceBreakerApi() {
        return iceBreakerApi;
    }

    public PollingApi getPollingApi() {
        return pollingApi;
    }

    public SurveyApi getSurveyApi() {
        return surveyApi;
    }

    public QuizApi getQuizApi() {
        return quizApi;
    }

    public BadgeApi getBadgeApi() {
        return badgeApi;
    }

    public AttendeeApi getAttendeeApi(){return attendeeApi;}

    public ScavengerApi getScavengerApi() {
        return scavengerApi;
    }

    public FavoriteApi getFavoriteApi() {
        return favoriteApi;
    }

    //social plus
    public TopicsOperations getTopicApis(){
        return topicApis;
    }

    public TopicCommentsOperations getTopicCommentsApis(){
        return topicCommentsApis;
    }

    public CommentsOperations getCommentsApis(){
        return commentsApis;
    }

    public TopicReportsOperations getTopicReportsApis(){
        return topicReportsApis;
    }

    public CommentReportsOperations getCommentReportsApis(){
        return commentReportsApis;
    }

    public TopicLikesOperations getTopicLikesApis(){return topicLikesApis;}

    public CommentLikesOperations getCommentLikesApis(){
        return commentLikesApis;
    }

    public UserTopicsOperations getUserTopicsApis(){return userTopicsApis;}
    //

    public GameApi getGameApi() {
        return gameApi;
    }

    public BEPApi getBepApi() {
        return bepApi;
    }

    public StumpApi getStumpApi() {
        return stumpApi;
    }

    public TrackingApi getTrackingApi() {
        return trackingApi;
    }

    public FeedbackApi getFeedbackApi(){
        return feedbackApi;
    }

    public MemoriesApi getMemoriesApi() {
        return memoriesApi;
    }

    public BingoApi getBingoApi() {
        return bingoApi;
    }
}
