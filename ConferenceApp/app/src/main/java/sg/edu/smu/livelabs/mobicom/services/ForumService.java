package sg.edu.smu.livelabs.mobicom.services;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.socialplus.autorest.CommentLikesOperations;
import com.microsoft.socialplus.autorest.CommentReportsOperations;
import com.microsoft.socialplus.autorest.CommentsOperations;
import com.microsoft.socialplus.autorest.TopicCommentsOperations;
import com.microsoft.socialplus.autorest.TopicLikesOperations;
import com.microsoft.socialplus.autorest.TopicReportsOperations;
import com.microsoft.socialplus.autorest.TopicsOperations;
import com.microsoft.socialplus.autorest.UserTopicsOperations;
import com.microsoft.socialplus.autorest.models.BlobType;
import com.microsoft.socialplus.autorest.models.CommentView;
import com.microsoft.socialplus.autorest.models.FeedResponseCommentView;
import com.microsoft.socialplus.autorest.models.FeedResponseTopicView;
import com.microsoft.socialplus.autorest.models.PostCommentRequest;
import com.microsoft.socialplus.autorest.models.PostCommentResponse;
import com.microsoft.socialplus.autorest.models.PostReportRequest;
import com.microsoft.socialplus.autorest.models.PostTopicRequest;
import com.microsoft.socialplus.autorest.models.PostTopicResponse;
import com.microsoft.socialplus.autorest.models.PublisherType;
import com.microsoft.socialplus.autorest.models.Reason;
import com.microsoft.socialplus.autorest.models.TopicView;
import com.microsoft.socialplus.autorest.models.UserCompactView;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.dao.query.LazyList;
import sg.edu.smu.livelabs.mobicom.App;
import sg.edu.smu.livelabs.mobicom.UIHelper;
import sg.edu.smu.livelabs.mobicom.busEvents.DeleteCommentEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.DeleteTopicEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.NewCommentEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.NewTopicEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.RefreshTopicEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateCommentEvent;
import sg.edu.smu.livelabs.mobicom.busEvents.UpdateTopicEvent;
import sg.edu.smu.livelabs.mobicom.models.User;
import sg.edu.smu.livelabs.mobicom.models.data.CommentEntity;
import sg.edu.smu.livelabs.mobicom.models.data.CommentEntityDao;
import sg.edu.smu.livelabs.mobicom.models.data.TopicEntity;
import sg.edu.smu.livelabs.mobicom.models.data.TopicEntityDao;
import sg.edu.smu.livelabs.mobicom.net.RestClient;
import sg.edu.smu.livelabs.mobicom.util.Util;

/**
 * Created by smu on 21/4/16.
 */
public class ForumService extends GeneralService {
    private static final ForumService instance = new ForumService();

    public static ForumService getInstance(){return instance;}
    private static final String FORUM_CATEGORY = "forum";
    public static final int LIMIT = 30;
    public static final int TYPE_MAIN = 1;
    public static final int TYPE_NORMAL = 2;

    private static String userHandler ;
    private static String sessionToken;
    public String topicCursor = null;
    private boolean resetData;
    private String commentCursor = null;
    private boolean topicSyncing = false;
    private TopicsOperations topicApis;
    private TopicCommentsOperations topicCommentsApis;
    private CommentsOperations commentsApis;
    private TopicLikesOperations topicLikesApis;
    private CommentLikesOperations commentLikesApis;
    private TopicReportsOperations topicReportsApis;
    private CommentReportsOperations commentReportsApis;
    private UserTopicsOperations userTopicsApis;
    private CommentEntityDao commentEntityDao;
    private TopicEntityDao topicEntityDao;

    private Handler handler;

    public void init(Context context, Bus bus, TopicsOperations topicApis,
                     TopicCommentsOperations topicCommentsApis, CommentsOperations commentsApis,
                     TopicLikesOperations topicLikesApis, CommentLikesOperations commentLikesApis,
                     TopicReportsOperations topicReportsApis, CommentReportsOperations commentReportsApis,
                     UserTopicsOperations userTopicsApis){
        this.context = context;
        this.bus = bus;
        this.topicApis = topicApis;
        this.topicCommentsApis = topicCommentsApis;
        this.commentsApis = commentsApis;
        this.topicLikesApis = topicLikesApis;
        this.commentLikesApis = commentLikesApis;
        this.topicReportsApis = topicReportsApis;
        this.commentReportsApis = commentReportsApis;
        this.userTopicsApis = userTopicsApis;
        this.commentEntityDao = DatabaseService.getInstance().getCommentEntityDao();
        this.topicEntityDao = DatabaseService.getInstance().getTopicEntityDao();
        topicCursor = null;
        commentCursor = null;
        handler = new Handler();
    }

    public String getUserHandler(){
        if (userHandler == null){
            userHandler = DatabaseService.getInstance().getMe().getUserHandle();

        }
        return userHandler;
    }

    public String getSessionToken(){
        if (sessionToken == null){
            sessionToken = "Bearer " + DatabaseService.getInstance().getMe().getSessionToken();
        }
        return sessionToken;
    }

    //DAO
    public List<CommentEntity> getComments(String topicHandle){
        if(topicHandle == null){
            return new ArrayList<>();
        }
        return commentEntityDao.queryBuilder()
                .where(CommentEntityDao.Properties.TopicHandle.eq(topicHandle),
                        CommentEntityDao.Properties.UserHandle.notEq("-1"))
                .orderDesc(CommentEntityDao.Properties.CreatedTime)
                .list();
    }

    //forum
    public LazyList<TopicEntity> getAllTopic(){
        return topicEntityDao.queryBuilder()
                .where(TopicEntityDao.Properties.UserHandle.notEq("-1"))
                .orderAsc(TopicEntityDao.Properties.Type)
                .orderDesc(TopicEntityDao.Properties.CreatedTime)
                .listLazy();
    }

    public LazyList<TopicEntity> searchTopic(String str){
        String key = "%" + str + "%";
        return topicEntityDao.queryBuilder()
                .whereOr(TopicEntityDao.Properties.Text.like(key),
                        TopicEntityDao.Properties.UserFullName.like(key))
                .listLazy();

    }

    public CommentEntity addComment(String topicHandle, String comment){
        CommentEntity entity = new CommentEntity();
        entity.setTopicHandle(topicHandle);
        entity.setCommentHandle("");
        entity.setCreatedTime(new Date());
        entity.setText(comment);
        entity.setLiked(false);
        entity.setSynced(true);
        User me = DatabaseService.getInstance().getMe();
        entity.setUserPhotoUrl(Util.getPhotoUrlFromId(me.getAvatar(), 96));
        entity.setUserFullName(me.getName());
        entity.setUserHandle(getUserHandler());
        commentEntityDao.insert(entity);
        syncTopicComments(topicHandle);
        return entity;
    }

    public TopicEntity addTopic(String topicText){
        TopicEntity topicEntity = new TopicEntity();
        topicEntity.setText(topicText);
        topicEntity.setTitle(topicText);
        topicEntity.setTopicHandle("");
        topicEntity.setTotalLikes(0l);
        topicEntity.setTotalComments(0l);
        topicEntity.setCreatedTime(new Date());
        topicEntity.setLiked(false);
        topicEntity.setType(TYPE_NORMAL);
        User me = DatabaseService.getInstance().getMe();
        topicEntity.setUserFullName(me.getName());
        topicEntity.setUserPhotoUrl(Util.getPhotoUrlFromId(me.getAvatar(), 96));
        topicEntity.setUserHandle(getUserHandler());
        topicEntityDao.insert(topicEntity);
        syncTopic();
        return topicEntity;
    }

    public void deleteTopic(TopicEntity topicEntity){
        topicEntity.setUserHandle("-1");
        topicEntityDao.update(topicEntity);
        syncTopic();
    }

    public void deleteComment(CommentEntity commentEntity){
        commentEntity.setUserHandle("-1");
        commentEntityDao.update(commentEntity);
        syncTopicComments(commentEntity.getTopicHandle());
    }

    //server S+ to client
    public void syncTopic(){
        if (topicCursor != null || topicSyncing){
            return;
        }
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    topicSyncing = true;
                    resetData = true;
                    List<TopicEntity> unSyncTopics = topicEntityDao
                            .queryBuilder()
                            .where(TopicEntityDao.Properties.TopicHandle.eq(""))
                            .list();
                    if (unSyncTopics != null && !unSyncTopics.isEmpty()){
                        for (TopicEntity entity : unSyncTopics){
                            PostTopicRequest postTopicRequest = new PostTopicRequest();
                            postTopicRequest.setText(entity.getText());
                            postTopicRequest.setTitle(entity.getTitle());
                            postTopicRequest.setPublisherType(PublisherType.USER);
                            postTopicRequest.setBlobType(BlobType.UNKNOWN);
                            postTopicRequest.setCategories(FORUM_CATEGORY);
                            ServiceResponse<PostTopicResponse> serviceResponse;
                            try {
                                serviceResponse = topicApis.postTopic(postTopicRequest, getSessionToken());
                                if (serviceResponse != null && serviceResponse.getBody() != null)
                                entity.setTopicHandle(serviceResponse.getBody().getTopicHandle());
                                TrackingService.getInstance().sendTracking("518", "more", "forum", entity.getTopicHandle(), "", "");
                            } catch (Exception e) {
                                topicSyncing = false;
                                post(new NewTopicEvent(false));
                                Log.d(App.APP_TAG, "Post topic exception " + e.toString());
                            }
                        }
                        topicEntityDao.updateInTx(unSyncTopics);
                        post(new NewTopicEvent(true));
                    }
                    List<TopicEntity> unSyncDeletedTopics = topicEntityDao
                            .queryBuilder()
                            .where(TopicEntityDao.Properties.UserHandle.eq("-1"))
                            .list();
                    if (unSyncDeletedTopics != null && !unSyncDeletedTopics.isEmpty()){
                        for (TopicEntity entity : unSyncDeletedTopics){
                            topicApis.deleteTopic(entity.getTopicHandle(), getSessionToken());
                        }
                    }
                    topicSyncing = false;
                    getTopics();

                }catch (Exception e){
                    topicCursor = null;
                    topicSyncing = false;
                    post(new RefreshTopicEvent(false));
                    Log.d(App.APP_TAG, "syncTopic: Exception" + e.toString());
                }
                return null;
            }
        }.execute();

    }

    public void getTopics(){
        topicApis.getTopicsAsync(topicCursor, LIMIT, null, getSessionToken(), null,
                new ServiceCallback<FeedResponseTopicView>() {
                    @Override
                    public void failure(Throwable t) {
                        topicCursor = null;
                        post(new RefreshTopicEvent(false));
                        Log.d(App.APP_TAG, "syncTopic: failure" + t.toString());
                    }

                    @Override
                    public void success(ServiceResponse<FeedResponseTopicView> result) {
                        if (result == null || result.getBody() == null) return;
                        List<TopicView> topicViews = result.getBody().getData();
                        if (topicViews != null && !topicViews.isEmpty()) {
                            List<TopicEntity> topicEntities = new ArrayList<TopicEntity>();
                            for (TopicView topicView : topicViews) {
                                if (!FORUM_CATEGORY.equals(topicView.getCategories()))
                                    continue;
                                TopicEntity topicEntity = new TopicEntity();
                                topicEntity.setText(topicView.getText());
                                topicEntity.setTitle(topicView.getTitle());
                                topicEntity.setTopicHandle(topicView.getTopicHandle());
                                topicEntity.setTotalLikes(topicView.getTotalLikes());
                                topicEntity.setTotalComments(topicView.getTotalComments());
                                topicEntity.setCreatedTime(topicView.getCreatedTime().toDate());
                                topicEntity.setUserFullName(getFullName(topicView.getUser()));
                                topicEntity.setUserPhotoUrl(topicView.getUser().getPhotoUrl());
                                topicEntity.setUserHandle(topicView.getUser().getUserHandle());
                                topicEntity.setLiked(topicView.getLiked());
                                topicEntity.setType(TYPE_NORMAL);
                                topicEntities.add(topicEntity);
                            }
                            if (topicCursor == null) {
                                List<TopicEntity> oldData = topicEntityDao.queryBuilder().
                                        where(TopicEntityDao.Properties.Type.eq(TYPE_NORMAL))
                                        .list();
                                topicEntityDao.deleteInTx(oldData);
                                topicEntityDao.insertInTx(topicEntities);
                                post(new RefreshTopicEvent(true));
                            } else {
                                topicEntityDao.insertInTx(topicEntities);
                            }
                            topicCursor = result.getBody().getCursor();
                            if (topicCursor == null) {
                                post(new RefreshTopicEvent(true));
                            } else {
                                getTopics();
                            }
                        }
                    }
                });
    }

    //server to client

    public void addTopicSP(final String topicText){
        PostTopicRequest postTopicRequest = new PostTopicRequest();
        postTopicRequest.setText(topicText);
        postTopicRequest.setTitle(topicText);
        postTopicRequest.setPublisherType(PublisherType.USER);
        postTopicRequest.setBlobType(BlobType.UNKNOWN);
        postTopicRequest.setCategories(FORUM_CATEGORY);
        topicApis.postTopicAsync(postTopicRequest, getSessionToken(), new ServiceCallback<PostTopicResponse>() {
            @Override
            public void failure(Throwable t) {
                post(new NewTopicEvent(false));
            }

            @Override
            public void success(ServiceResponse<PostTopicResponse> result) {
                TopicEntity topicEntity = new TopicEntity();
                topicEntity.setText(topicText);
                topicEntity.setTitle(topicText);
                topicEntity.setTopicHandle("");
                topicEntity.setTotalLikes(0l);
                topicEntity.setTotalComments(0l);
                topicEntity.setCreatedTime(new Date());
                topicEntity.setLiked(false);
                topicEntity.setType(TYPE_NORMAL);
                User me = DatabaseService.getInstance().getMe();
                topicEntity.setUserFullName(me.getName());
                topicEntity.setUserPhotoUrl(Util.getPhotoUrlFromId(me.getAvatar(), 96));
                topicEntity.setUserHandle(getUserHandler());
                topicEntityDao.insert(topicEntity);
                post(new NewTopicEvent(true));
                getTopics();
            }
        });
    }

    public void deleteTopicSP(final TopicEntity topicEntity){
        topicApis.deleteTopicAsync(topicEntity.getTopicHandle(), getSessionToken(), new ServiceCallback<Object>() {
            @Override
            public void failure(Throwable t) {
                post(new DeleteTopicEvent(false));
            }

            @Override
            public void success(ServiceResponse<Object> result) {
                topicEntityDao.delete(topicEntity);
                post(new DeleteTopicEvent(true));
            }
        });
    }

    public void deleteCommentSP(final CommentEntity commentEntity){
        commentsApis.deleteCommentAsync(commentEntity.getCommentHandle(), getSessionToken(), new ServiceCallback<Object>() {
            @Override
            public void failure(Throwable t) {
                post(new DeleteCommentEvent(false));
            }

            @Override
            public void success(ServiceResponse<Object> result) {
                AgendaService.getInstance().storeComment(commentEntity.getTopicHandle(),
                        commentEntity.getCommentHandle(), "delete");
                commentEntityDao.delete(commentEntity);
                post(new DeleteCommentEvent(true));
                getTopicComments(commentEntity.getTopicHandle());
            }
        });
    }
    public void addCommentToSP(final String topicHandle, final String comment){
        PostCommentRequest commentRequest = new PostCommentRequest();
        commentRequest.setText(comment);
        commentRequest.setBlobHandle(null);
        commentRequest.setBlobType(BlobType.UNKNOWN);
        topicCommentsApis.postCommentAsync(topicHandle, commentRequest, getSessionToken(), new ServiceCallback<PostCommentResponse>() {
            @Override
            public void failure(Throwable t) {
                post(new NewCommentEvent(false));
            }

            @Override
            public void success(ServiceResponse<PostCommentResponse> result) {
                CommentEntity entity = new CommentEntity();
                entity.setTopicHandle(topicHandle);
                entity.setCommentHandle(result.getBody().getCommentHandle());
                entity.setCreatedTime(new Date());
                entity.setText(comment);
                entity.setTotalLikeCount(0l);
                entity.setLiked(false);
                entity.setSynced(true);
                User me = DatabaseService.getInstance().getMe();
                entity.setUserPhotoUrl(Util.getPhotoUrlFromId(me.getAvatar(), 96));
                entity.setUserFullName(me.getName());
                entity.setUserHandle(getUserHandler());
                post(new NewCommentEvent(true));
                commentEntityDao.insert(entity);
                AgendaService.getInstance().storeComment(topicHandle,
                        result.getBody().getCommentHandle(), "insert");
                getTopicComments(topicHandle);
            }
        });

    }

    public void syncTopicComments(final String topicHandler){
        if (commentCursor != null){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    syncTopicComments(topicHandler);
                }
            }, 5000);
            return;
        }
        new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    commentCursor = null;
                    List<CommentEntity> unSyncAddedComments = commentEntityDao.queryBuilder()
                            .where(CommentEntityDao.Properties.CommentHandle.eq(""))
                            .list();
                    if (unSyncAddedComments!= null && !unSyncAddedComments.isEmpty()){
                        for (CommentEntity commentEntity : unSyncAddedComments){
                            PostCommentRequest commentRequest = new PostCommentRequest();
                            commentRequest.setText(commentEntity.getText());
                            commentRequest.setBlobHandle(null);
                            commentRequest.setBlobType(BlobType.UNKNOWN);
                            ServiceResponse<PostCommentResponse> response =
                                    topicCommentsApis.postComment(commentEntity.getTopicHandle(), commentRequest, getSessionToken());
                            AgendaService.getInstance().storeComment(commentEntity.getTopicHandle(),
                                    response.getBody().getCommentHandle(), "insert");

                        }
                    }
                    List<CommentEntity> unSyncDeletedComments = commentEntityDao.queryBuilder()
                            .where(CommentEntityDao.Properties.UserHandle.eq("-1"))
                            .list();
                    if (unSyncDeletedComments!= null && !unSyncDeletedComments.isEmpty()){
                        for (CommentEntity commentEntity : unSyncDeletedComments){
                            commentsApis.deleteComment(commentEntity.getCommentHandle(), getSessionToken());
                            AgendaService.getInstance().storeComment(commentEntity.getTopicHandle(),
                                    commentEntity.getCommentHandle(), "delete");
                        }
                    }
                    List<CommentEntity> unSyncLikedComments = commentEntityDao.queryBuilder()
                            .where(CommentEntityDao.Properties.Synced.eq(false))
                            .list();
                    if (unSyncLikedComments!= null && !unSyncLikedComments.isEmpty()){
                        for (CommentEntity commentEntity : unSyncLikedComments){
                            if (commentEntity.getLiked()){
                                commentLikesApis.postLike(commentEntity.getCommentHandle(), getSessionToken());
                            } else {
                                commentLikesApis.deleteLike(commentEntity.getCommentHandle(), getSessionToken());
                            }
                        }
                    }
                    getTopicComments(topicHandler);
                }catch(Exception e){
                    commentCursor = null;
                    post(new UpdateCommentEvent(false));
                    Log.d(App.APP_TAG, "syncTopicComments Exception" + e.toString());
                }
                return null;
            }
        }.execute();
    }

    public void getTopicComments(final String topicHandler){
        topicCommentsApis.getTopicCommentsAsync(topicHandler, commentCursor, LIMIT, null,
                getSessionToken(), null,
                new ServiceCallback<FeedResponseCommentView>() {
                    @Override
                    public void failure(Throwable t) {
                        commentCursor = null;
                        post(new UpdateCommentEvent(false));
                        Log.d(App.APP_TAG, "getTopicComments: failure" + t.toString());
                    }

                    @Override
                    public void success(ServiceResponse<FeedResponseCommentView> result) {
                        if (result == null) return;
                        if (result.getBody() != null) {
                            List<CommentView> commentViews = result.getBody().getData();
                            List<CommentEntity> entities = new ArrayList<CommentEntity>();
                            for (CommentView commentView : commentViews) {
                                CommentEntity entity = new CommentEntity();
                                entity.setTopicHandle(commentView.getTopicHandle());
                                entity.setCommentHandle(commentView.getCommentHandle());
                                entity.setCreatedTime(commentView.getCreatedTime().toDate());
                                entity.setText(commentView.getText());
                                entity.setUserPhotoUrl(commentView.getUser().getPhotoUrl());
                                entity.setUserFullName(getFullName(commentView.getUser()));
                                entity.setUserHandle(commentView.getUser().getUserHandle());
                                entity.setLiked(commentView.getLiked());
                                entity.setTotalLikeCount(commentView.getTotalLikes());
                                entity.setSynced(true);
                                entities.add(entity);
                            }
                            if (commentCursor == null) {
                                List<CommentEntity> oldData = commentEntityDao.queryBuilder()
                                        .where(CommentEntityDao.Properties.TopicHandle.eq(topicHandler))
                                        .list();
                                commentEntityDao.deleteInTx(oldData);
                            }
                            commentEntityDao.insertInTx(entities);
                            commentCursor = result.getBody().getCursor();
                            if (commentCursor == null) {
                                post(new UpdateCommentEvent(true));
                            } else {
                                getTopicComments(topicHandler);
                            }

                        }
                    }
                });
    }

    public void SyncATopic(String topicHandle) {
        if ("".equals(topicHandle)) return;
        try {
            topicApis.getTopicAsync(topicHandle, RestClient.APP_KEY, getSessionToken(), getUserHandler(),
                    new ServiceCallback<TopicView>() {
                @Override
                public void failure(Throwable t) {
                    Log.d(App.APP_TAG, "SyncATopic failure" + t.toString());
                }

                @Override
                public void success(ServiceResponse<TopicView> result) {
                    if (result == null || result.getBody() == null) return;
                    try {
                        TopicView topicView = result.getBody();
                        TopicEntity topicEntity = topicEntityDao
                                .queryBuilder()
                                .where(TopicEntityDao.Properties.TopicHandle.eq(topicView.getTopicHandle()))
                                .unique();
                        topicEntity.setTotalLikes(topicView.getTotalLikes());
                        topicEntity.setTotalComments(topicView.getTotalComments());
                        topicEntityDao.update(topicEntity);
                        post(new UpdateTopicEvent());
                    }catch (Exception e){
                        Log.d(App.APP_TAG, e.toString());
                    }

                }
            });
        } catch (Exception e){
            Log.d(App.APP_TAG, "SyncATopic Exception" + e.toString());
        }

    }

    private String getFullName(UserCompactView userCompactView){
        String fullName = "";
        if (userCompactView.getFirstName() != null){
            fullName += userCompactView.getFirstName();
            fullName += " ";
        }
//        if (userCompactView.getLastName() != null){
//            fullName += userCompactView.getLastName();
//        }
        return fullName;
    }

    public void likeAComment(final CommentEntity commentEntity){
        commentEntity.setLiked(true);
        if(commentEntity.getTotalLikeCount() == null)
            commentEntity.setTotalLikeCount(0l);
        else
            commentEntity.setTotalLikeCount(commentEntity.getTotalLikeCount() + 1);
        commentEntity.setSynced(false);
        commentEntityDao.update(commentEntity);
        commentLikesApis.postLikeAsync(commentEntity.getCommentHandle(), getSessionToken(), new ServiceCallback<Object>() {
            @Override
            public void failure(Throwable t) {
            }

            @Override
            public void success(ServiceResponse<Object> result) {
                commentEntity.setSynced(true);
                commentEntityDao.update(commentEntity);
                AgendaService.getInstance()
                        .storeLikeComment(commentEntity.getCommentHandle(), "like");
            }
        });
    }

    public void deleteLikeAComment(final CommentEntity commentEntity){
        commentEntity.setLiked(false);
        if(commentEntity.getTotalLikeCount() == null)
            commentEntity.setTotalLikeCount(0l);
        else
            commentEntity.setTotalLikeCount(commentEntity.getTotalLikeCount() - 1);
        commentEntity.setSynced(false);
        commentEntityDao.update(commentEntity);
        commentLikesApis.deleteLikeAsync(commentEntity.getCommentHandle(), getSessionToken(), new ServiceCallback<Object>() {
            @Override
            public void failure(Throwable t) {
            }

            @Override
            public void success(ServiceResponse<Object> result) {
                commentEntity.setSynced(true);
                commentEntityDao.update(commentEntity);
                AgendaService.getInstance()
                        .storeLikeComment(commentEntity.getCommentHandle(), "dislike");
            }
        });
    }

    public void getUserTopic(String userHandler){
        userTopicsApis.getTopicsAsync(userHandler, null, 10, null, getSessionToken(), getUserHandler(),
                new ServiceCallback<FeedResponseTopicView>() {
            @Override
            public void failure(Throwable t) {
                Log.e(App.APP_TAG, "getUserTopic",  t);
            }

            @Override
            public void success(ServiceResponse<FeedResponseTopicView> result) {
                if (result.getBody() != null && result.getBody().getData() != null){
                    List<TopicView> topics = result.getBody().getData();
                    List<TopicEntity> mainTopicEntities = new ArrayList<TopicEntity>();
                    for (TopicView topicView : topics){
                        TopicEntity topicEntity = new TopicEntity();
                        topicEntity.setText(topicView.getText());
                        topicEntity.setTitle(topicView.getTitle());
                        topicEntity.setTopicHandle(topicView.getTopicHandle());
                        topicEntity.setTotalLikes(topicView.getTotalLikes());
                        topicEntity.setTotalComments(topicView.getTotalComments());
                        topicEntity.setCreatedTime(topicView.getCreatedTime().toDate());
                        topicEntity.setUserFullName(getFullName(topicView.getUser()));
                        topicEntity.setUserPhotoUrl(topicView.getUser().getPhotoUrl());
                        topicEntity.setUserHandle(topicView.getUser().getUserHandle());
                        topicEntity.setLiked(topicView.getLiked());
                        topicEntity.setType(TYPE_MAIN);
                        mainTopicEntities.add(topicEntity);
                    }
                    List<TopicEntity> oldData = topicEntityDao.queryBuilder().
                            where(TopicEntityDao.Properties.Type.eq(TYPE_MAIN))
                            .list();
                    topicEntityDao.deleteInTx(oldData);
                    topicEntityDao.insertInTx(mainTopicEntities);
                    post(new RefreshTopicEvent(true));
                }
            }
        });
    }

    public void deleteAtopic(TopicEntity topicEntity){ // moderator deletes a topic

    }

//    public void likeATopic(final TopicEntity topicEntity){
//        topicEntity.setLiked(true);
//        topicEntityDao.update(topicEntity);
//        topicLikesApis.postLikeAsync(topicEntity.getTopicHandle(), getSessionToken(), new ServiceCallback<Object>() {
//            @Override
//            public void failure(Throwable t) {
//            }
//
//            @Override
//            public void success(ServiceResponse<Object> result) {
//            }
//        });
//    }
//
//    public void deleteLikeATopic(final TopicEntity topicEntity){
//        topicEntity.setLiked(false);
//        topicEntityDao.update(topicEntity);
//        topicLikesApis.deleteLikeAsync(topicEntity.getTopicHandle(), getSessionToken(), new ServiceCallback<Object>() {
//            @Override
//            public void failure(Throwable t) {
//            }
//
//            @Override
//            public void success(ServiceResponse<Object> result) {
//            }
//        });
//    }

    public void reportATopic(String topicHandle, Reason reason){
        PostReportRequest reportRequest = new PostReportRequest();
        reportRequest.setReason(reason);
        topicReportsApis.postReportAsync(topicHandle, reportRequest,
                getSessionToken(), RestClient.APP_KEY, null,
                new ServiceCallback<Object>() {
                    @Override
                    public void failure(Throwable t) {
                        UIHelper.getInstance().dismissProgressDialog();
                        Toast.makeText(context, "Sorry. We could not process your request. Please try again.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void success(ServiceResponse<Object> result) {
                        UIHelper.getInstance().dismissProgressDialog();
                        Toast.makeText(context, "Thank you for your feedback. Our team will look into the matter", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void reportComment(String commentHandle, Reason reason){
        PostReportRequest reportRequest = new PostReportRequest();
        reportRequest.setReason(reason);
        commentReportsApis.postReportAsync(commentHandle, reportRequest,
                getSessionToken(), RestClient.APP_KEY,null,
                new ServiceCallback<Object>() {
                    @Override
                    public void failure(Throwable t) {
                        UIHelper.getInstance().dismissProgressDialog();
                        Toast.makeText(context, "Sorry. We could not process your request. Please try again.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void success(ServiceResponse<Object> result) {
                        UIHelper.getInstance().dismissProgressDialog();
                        Toast.makeText(context, "Thank you for your feedback. Our team will look into the matter.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
