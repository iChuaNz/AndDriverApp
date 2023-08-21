package sg.com.commute_solutions.bustracker.util.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import me.leolin.shortcutbadger.ShortcutBadger;
import sg.com.commute_solutions.bustracker.R;
import sg.com.commute_solutions.bustracker.common.Constants;
import sg.com.commute_solutions.bustracker.common.Preferences;
import sg.com.commute_solutions.bustracker.util.StringUtil;

/**
 * Created by Kyle on 15/5/17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String LOG_TAG = MyFirebaseMessagingService.class.getSimpleName();
    private String clickAction, additionalInfo, busCharterId;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            SharedPreferences prefs = this.getSharedPreferences(Constants.SHARE_PREFERENCE_PACKAGE, Context.MODE_PRIVATE);
            int badgeCount = prefs.getInt(Preferences.BADGE_COUNT, 0);
            badgeCount += 1;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(Preferences.BADGE_COUNT, badgeCount);
//            ShortcutBadger.applyCount(this, badgeCount);

            Log.d(LOG_TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            clickAction = StringUtil.deNull(remoteMessage.getNotification().getClickAction());
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(LOG_TAG, "Message data payload: " + remoteMessage.getData());
            additionalInfo = remoteMessage.getData().get(Constants.EXTRA).toString();
            busCharterId = remoteMessage.getData().get(Constants.BUS_CHARTER_ID).toString();
        }

        if (!clickAction.equalsIgnoreCase("")) {
            sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), additionalInfo, busCharterId, clickAction);
        }
    }

    private void sendNotification(String title, String body, String extra, String charterId, String click_action) {
        Intent intent = new Intent(click_action);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.EXTRA, extra);
        intent.putExtra(Constants.BUS_CHARTER_ID, charterId);

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setLargeIcon(icon)
                .setSmallIcon(R.drawable.appicon)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }
}
