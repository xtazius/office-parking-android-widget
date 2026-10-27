package ro.officeparking.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.time.Instant;

public final class ParkingBoundaryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!BoundaryAlarm.ACTION.equals(intent.getAction())) {
            return;
        }
        if (!ParkingWidgetProvider.hasWidgets(context)) {
            BoundaryAlarm.cancel(context);
            return;
        }
        ParkingWidgetProvider.renderAll(context);
        BoundaryAlarm.schedule(context);
        if (ParkingHours.isOpen(Instant.now())) {
            WorkScheduler.requestRefresh(context);
        }
    }
}

