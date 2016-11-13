package sg.edu.smu.livelabs.mobicom;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import java.util.List;

/**
 * Created by smu on 25/1/16.
 */
public class AppNotifications {
    public static final String NOTI_TYPE = "NOTI_TYPE";
    public static final String NOTI_ID = "NOTI_ID";
    //Notification id
    public static final int OTHER_NOTIFICATION_ID = 10000;
    public static final int SYSTEM_NOTIFICATION_ID = 10002;
    public static final int SIMPLE_NOTIFICATION_ID = 10003;
    public static final int CHAT_NOTIFICATION_ID = 10004;
    public static final int BEP_GENERAL_NOTIFICATION_ID = 10011;
    public static final int BEP_SURVEY_NOTIFICATION_ID = 10012;
    public static final int GAME_NOTIFICATION_ID = 10013;


    //Notification type
    public static final String SIMPLE_NOTIFICATION = "Simple";

    public static long[] vibrate = new long[] { 1000, 1000, 1000, 1000, 1000 };

    public static void sendSimpleNotification( Context context, String content, int notiID) {
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notifyIntent = new Intent(context, MainActivity.class);
        notifyIntent.putExtra(NOTI_TYPE, SIMPLE_NOTIFICATION);
        notifyIntent.putExtra(NOTI_ID, notiID);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(getNotificationIcon(context))
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),  R.drawable.logo))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                        .setContentTitle(context.getResources().getString(R.string.app_name)) //Should be name
                        .setSound(alarmSound)
                        .setVibrate(vibrate)
                        .setContentText(content);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(notiID, mBuilder.build());
    }

    public static void sendChatNotification(Context context, List<String> notiContents, String summaryText, long groupId, long fromUser) {
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notifyIntent = new Intent(context, MainActivity.class);
        notifyIntent.putExtra(NOTI_TYPE, "Message");
        notifyIntent.putExtra(NOTI_ID, CHAT_NOTIFICATION_ID);
        Bundle bundle = new Bundle();
        bundle.putLong("GROUP_ID", groupId);
        bundle.putLong("USER_ID", fromUser);
        notifyIntent.putExtras(bundle);

        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(App.appName);
        inboxStyle.setSummaryText(summaryText);
        for (String notiContent : notiContents) {
            inboxStyle.addLine(notiContent);
        }
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(getNotificationIcon(context))
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.logo))
                        .setContentTitle(App.appName)
                        .setContentText(notiContents.get(notiContents.size() - 1))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setStyle(inboxStyle)
                        .setSound(alarmSound)
                        .setVibrate(vibrate);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(CHAT_NOTIFICATION_ID, mBuilder.build());
    }

    private static int getNotificationIcon(Context context){
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        int imageId =  useWhiteIcon ? R.drawable.logo_small : R.drawable.logo;
        return imageId;
    }

    public static void sendGroupInvitedNotification(Context context, String content, int notiID, long groupId) {
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notifyIntent = new Intent(context, MainActivity.class);
        notifyIntent.putExtra("NOTI_TYPE", "Group Invited");
        notifyIntent.putExtra("NOTI_ID", notiID);
        Bundle bundle = new Bundle();
        bundle.putLong("GROUP_ID", groupId);
        bundle.putLong("USER_ID", 0);
        notifyIntent.putExtras(bundle);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, notiID,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(getNotificationIcon(context))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(content))
                        .setContentTitle(context.getResources().getString(R.string.app_name)) //Should be name
                        .setSound(alarmSound)
                        .setVibrate(vibrate)
                        .setContentText(content);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(notiID, mBuilder.build());
    }

    public static void sendOtherNotification(Context context,String title, String content,
                                             int notiID, String type) {
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notifyIntent = new Intent(context, MainActivity.class);
        if (title == null || title.isEmpty()){
            title = context.getResources().getString(R.string.app_name);
        }
        notifyIntent.putExtra("NOTI_TYPE", type);
        notifyIntent.putExtra("NOTI_ID", notiID);
        Bundle bundle = new Bundle();
        bundle.putString("CONTENT", content);
        bundle.putString("TITLE", title);
        notifyIntent.putExtras(bundle);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, notiID,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(getNotificationIcon(context))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(content))
                        .setContentTitle(title) //Should be name
                        .setSound(alarmSound)
                        .setVibrate(vibrate)
                        .setContentText(content);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(notiID, mBuilder.build());
    }

    public static void sendDataNotification(Context context,String title, String content,
                                             int notiID, String type, Bundle data) {
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notifyIntent = new Intent(context, MainActivity.class);
        if (title == null || title.isEmpty()){
            title = context.getResources().getString(R.string.app_name);
        }
        notifyIntent.putExtra("NOTI_TYPE", type);
        notifyIntent.putExtra("NOTI_ID", notiID);
        Bundle bundle = new Bundle();
        bundle.putString("CONTENT", content);
        bundle.putString("TITLE", title);
        bundle.putBundle("DATA", data);
        notifyIntent.putExtras(bundle);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, notiID,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(getNotificationIcon(context))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(content))
                        .setContentTitle(title) //Should be name
                        .setSound(alarmSound)
                        .setVibrate(vibrate)
                        .setContentText(content);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(notiID, mBuilder.build());
    }
}
