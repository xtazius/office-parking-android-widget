package ro.officeparking.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.time.Instant;

final class BoundaryAlarm {
    static final String ACTION = "ro.officeparking.widget.OFFICE_HOURS_BOUNDARY";
    private static final int REQUEST_CODE = 100;

    private BoundaryAlarm() {
    }

    static void schedule(Context context) {
        if (!ParkingWidgetProvider.hasWidgets(context)) {
            return;
        }
        AlarmManager manager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            manager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    ParkingHours.nextBoundaryMillis(Instant.now()),
                    pendingIntent(context, PendingIntent.FLAG_UPDATE_CURRENT)
            );
        }
    }

    static void cancel(Context context) {
        AlarmManager manager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent intent = pendingIntent(context, PendingIntent.FLAG_NO_CREATE);
        if (manager != null && intent != null) {
            manager.cancel(intent);
            intent.cancel();
        }
    }

    private static PendingIntent pendingIntent(Context context, int flags) {
        Intent intent = new Intent(context, ParkingBoundaryReceiver.class).setAction(ACTION);
        return PendingIntent.getBroadcast(
                context, REQUEST_CODE, intent, flags | PendingIntent.FLAG_IMMUTABLE
        );
    }
}

