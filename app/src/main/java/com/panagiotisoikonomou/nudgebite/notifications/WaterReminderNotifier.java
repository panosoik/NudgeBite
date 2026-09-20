package com.panagiotisoikonomou.nudgebite.notifications;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.panagiotisoikonomou.nudgebite.notifications.InterventionActionReceiver.Action;

/** Posts the hydration reminder, on its own channel so it can be silenced without silencing the nudges. */
public final class WaterReminderNotifier {

    private static final String TAG = "WaterReminder";
    private static final String CHANNEL_ID = "hydration_reminder";

    /** A tag keeps this id from colliding with intervention ids, which share the same sequence. */
    private static final String NOTIFICATION_TAG = "hydration_reminder";
    private static final int NOTIFICATION_ID = 7101;

    private static final String BODY = "A glass of water might be good about now.";

    private WaterReminderNotifier() {
    }

    /** The wording, stored on the reminder's log row so the export carries it too. */
    public static String reminderBody() {
        return BODY;
    }

    public static boolean canPostReminder(Context context) {
        Context appContext = context.getApplicationContext();
        ensureChannel(appContext);
        return NotificationBasics.isChannelShown(appContext, CHANNEL_ID);
    }

    public static void notifyReminder(Context context, long interventionLogId) {
        Context appContext = context.getApplicationContext();
        // Checked inline because lint reads one method at a time.
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED || !canPostReminder(appContext)) {
            Log.w(TAG, "Hydration reminder skipped: it could not be shown.");
            return;
        }
        PendingIntent ok = NotificationBasics.broadcast(appContext, NOTIFICATION_ID,
                InterventionActionReceiver.intentFor(appContext, Action.WATER_OK, interventionLogId));
        PendingIntent skip = NotificationBasics.broadcast(appContext, NOTIFICATION_ID + 1,
                InterventionActionReceiver.intentFor(appContext, Action.WATER_SKIP, interventionLogId));
        NotificationManagerCompat.from(appContext).notify(NOTIFICATION_TAG, NOTIFICATION_ID, NotificationBasics
                .textNotification(appContext, CHANNEL_ID, BODY, NotificationCompat.PRIORITY_DEFAULT)
                .addAction(0, "OK", ok)
                .addAction(0, "Skip", skip)
                .setDeleteIntent(skip)
                .build());
        Log.i(TAG, "Hydration reminder posted, intervention_log id=" + interventionLogId);
    }

    public static void cancel(Context context) {
        NotificationManagerCompat.from(context.getApplicationContext()).cancel(NOTIFICATION_TAG, NOTIFICATION_ID);
    }

    private static void ensureChannel(Context context) {
        NotificationBasics.ensureChannel(context, CHANNEL_ID, "Hydration reminders",
                NotificationManager.IMPORTANCE_DEFAULT,
                "An occasional reminder to have a glass of water, if you asked for one.");
    }
}
