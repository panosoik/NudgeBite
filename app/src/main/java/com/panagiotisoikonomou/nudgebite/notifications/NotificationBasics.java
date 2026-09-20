package com.panagiotisoikonomou.nudgebite.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.panagiotisoikonomou.nudgebite.R;

/** What both of the app's notifications share: a channel, a text body, and broadcast buttons. */
final class NotificationBasics {

    private NotificationBasics() {
    }

    /** Re-creating a channel with the same id is a no-op, so this is safe to call every time. */
    static void ensureChannel(Context context, String channelId, String name, int importance, String description) {
        NotificationChannel channel = new NotificationChannel(channelId, name, importance);
        channel.setDescription(description);
        context.getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    /** False when the app's notifications are off or this channel has been muted on its own. */
    static boolean isChannelShown(Context context, String channelId) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return false;
        }
        NotificationChannel channel = context.getSystemService(NotificationManager.class).getNotificationChannel(channelId);
        return channel == null || channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
    }

    static NotificationCompat.Builder textNotification(Context context, String channelId, String body, int priority) {
        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(priority)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true);
    }

    static PendingIntent broadcast(Context context, int requestCode, Intent intent) {
        return PendingIntent.getBroadcast(context, requestCode, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
