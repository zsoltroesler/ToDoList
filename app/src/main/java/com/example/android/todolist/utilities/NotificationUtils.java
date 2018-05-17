package com.example.android.todolist.utilities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.example.android.todolist.ui.main.MainActivity;
import com.example.android.todolist.R;

/**
 * Created by Zsolt on 25.01.2018.
 */

public class NotificationUtils extends BroadcastReceiver {

    /*
    * This notification ID can be used to access our notification after we've displayed it.
    */
    private static final int TODO_REMINDER_NOTIFICATION_ID = 100;

    /**
     * This pending intent id is used to uniquely reference the pending intent
     */
    private static final int TODO_REMINDER_PENDING_INTENT_ID = 111;

    /**
     * This notification channel id is used to link notifications to this channel
     */
    private static final String REMINDER_NOTIFICATION_CHANNEL_ID = "reminder_notification_channel";

    @Override
    public void onReceive(Context context, Intent intent) {

        // Create an intent that opens up the MainActivity
        intent = new Intent(context, MainActivity.class);

        // Create a pending intent, which holds our intent and be passed to NotificationManager
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                TODO_REMINDER_PENDING_INTENT_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Get the NotificationManager using context.getSystemService
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Create a notification channel for Android O devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    REMINDER_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.main_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        // Use NotificationCompat.Builder to create a notification that:
        // - has a color - use ContextCompat.getColor to get a compatible color
        // - has a small icon
        // - uses icon returned by the showIcon helper method as the large icon
        // - sets the title
        // - sets the text
        // - sets the style to NotificationCompat.BigTextStyle().bigText(text)
        // - sets the notification defaults to vibrate
        // - sets the content intent - supplies a PendingIntent to send when the notification is clicked
        // - automatically cancels the notification when the notification is clicked
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, REMINDER_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_todo_list_notification_24dp)
                .setLargeIcon(showIcon(context))
                .setContentTitle(context.getString(R.string.todo_reminder_notification_title))
                .setContentText(context.getString(R.string.todo_reminder_notification_body))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        context.getString(R.string.todo_reminder_notification_body)))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // If the build version is greater than JELLY_BEAN and lower than OREO,
        // set the notification's priority to PRIORITY_HIGH.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        // Trigger the notification by calling notify on the NotificationManager.
        // Pass in a unique ID and notificationBuilder.build()
        notificationManager.notify(TODO_REMINDER_NOTIFICATION_ID, notificationBuilder.build());
    }

    // Create a helper method which takes in a Context as a parameter and
    // returns a Bitmap. This method is necessary to decode a bitmap needed for the notification.
    private static Bitmap showIcon(Context context) {
        // Get a Resources object from the context.
        Resources res = context.getResources();
        // Create and return a bitmap using BitmapFactory.decodeResource, passing in the
        // resources object and the vector icon
        Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.ic_todo_list_notification_24dp);
        return largeIcon;
    }
}
