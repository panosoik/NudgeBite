package com.panagiotisoikonomou.nudgebite.notifications;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.panagiotisoikonomou.nudgebite.MainActivity;
import com.panagiotisoikonomou.nudgebite.data.enums.InterventionFormat;
import com.panagiotisoikonomou.nudgebite.notifications.InterventionActionReceiver.Action;
import com.panagiotisoikonomou.nudgebite.ui.intervention.MicroEmaActivity;
import com.panagiotisoikonomou.nudgebite.ui.intervention.SuggestionActivity;

import java.util.List;

/** Posts the JITAI intervention notification; its buttons come from InterventionActionPlan. */
public final class InterventionNotifier {

    private static final String TAG = "NudgeBiteEngine";
    private static final String CHANNEL_ID = "jitai_intervention";
    private static final int NOTIFICATION_ID_BASE = 5000;

    private InterventionNotifier() {
    }

    /** Checks the channel too: a muted channel silences nudges while the permission still looks granted. */
    public static boolean canPost(Context context) {
        Context appContext = context.getApplicationContext();
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        ensureChannel(appContext);
        return NotificationBasics.isChannelShown(appContext, CHANNEL_ID);
    }

    public static void notify(Context context, long interventionLogId, InterventionFormat format, String body) {
        Context appContext = context.getApplicationContext();
        // Checked inline because lint reads one method at a time.
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Intervention notification skipped: POST_NOTIFICATIONS not granted.");
            return;
        }
        ensureChannel(appContext);
        int notificationId = notificationIdFor(interventionLogId);
        NotificationManagerCompat.from(appContext).notify(notificationId,
                buildNotification(appContext, notificationId, interventionLogId, format, body));
        Log.i(TAG, "Intervention notification posted, id=" + notificationId);
    }

    /** Action buttons do not auto-cancel; a stale micro-EMA notification would record a second answer. */
    public static void cancel(Context context, long interventionLogId) {
        NotificationManagerCompat.from(context.getApplicationContext()).cancel(notificationIdFor(interventionLogId));
    }

    private static int notificationIdFor(long interventionLogId) {
        return NOTIFICATION_ID_BASE + (int) interventionLogId;
    }

    private static Notification buildNotification(Context context, int notificationId, long interventionLogId,
                                                  InterventionFormat format, String body) {
        NotificationCompat.Builder builder = NotificationBasics
                .textNotification(context, CHANNEL_ID, body, NotificationCompat.PRIORITY_HIGH)
                .setDeleteIntent(button(context, notificationId, Action.SWIPED_AWAY, interventionLogId));

        List<String> answerLabels = InterventionActionPlan.answerLabels(format);
        if (!answerLabels.isEmpty()) {
            for (int i = 0; i < answerLabels.size(); i++) {
                builder.addAction(0, answerLabels.get(i), button(context, notificationId,
                        InterventionActionReceiver.answerAction(format, i), interventionLogId));
            }
            return builder.build();
        }

        PendingIntent primary;
        Action secondary = Action.SKIP;
        if (InterventionActionPlan.opensScreen(format)) {
            primary = PendingIntent.getActivity(context, notificationId,
                    screenIntentFor(context, interventionLogId, format),
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(primary);
        } else if (format == InterventionFormat.NOTIFICATION_EMA) {
            primary = button(context, notificationId, Action.FOOD_AT_HOME_YES, interventionLogId);
            secondary = Action.FOOD_AT_HOME_NO;
        } else {
            primary = button(context, notificationId, Action.ACKNOWLEDGE, interventionLogId);
        }
        return builder
                .addAction(0, InterventionActionPlan.primaryLabel(format), primary)
                .addAction(0, InterventionActionPlan.secondaryLabel(format),
                        button(context, notificationId, secondary, interventionLogId))
                .build();
    }

    private static PendingIntent button(Context context, int notificationId, Action action, long interventionLogId) {
        return NotificationBasics.broadcast(context, notificationId,
                InterventionActionReceiver.intentFor(context, action, interventionLogId));
    }

    private static Intent screenIntentFor(Context context, long interventionLogId, InterventionFormat format) {
        if (format == InterventionFormat.WEEKLY_REVIEW) {
            return MainActivity.newIntentForWeeklyReview(context, interventionLogId);
        }
        if (format == InterventionFormat.MICRO_EMA) {
            return MicroEmaActivity.newIntent(context, interventionLogId);
        }
        return SuggestionActivity.newIntent(context, interventionLogId);
    }

    private static void ensureChannel(Context context) {
        NotificationBasics.ensureChannel(context, CHANNEL_ID, "Meal-time nudges", NotificationManager.IMPORTANCE_HIGH,
                "JITAI interventions — a quick suggestion right before you order.");
    }
}
