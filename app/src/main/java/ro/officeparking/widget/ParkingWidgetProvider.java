package ro.officeparking.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import java.text.NumberFormat;
import java.time.Instant;
import java.util.Locale;

public final class ParkingWidgetProvider extends AppWidgetProvider {
    static final String DASHBOARD_URL =
            "https://office-parking-ro.github.io/parking-dashboard/";

    @Override
    public void onEnabled(Context context) {
        WorkScheduler.start(context);
        WorkScheduler.requestRefresh(context);
        BoundaryAlarm.schedule(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] widgetIds) {
        render(context, manager, widgetIds);
        if (widgetIds.length > 0) {
            WorkScheduler.start(context);
            WorkScheduler.requestRefresh(context);
            BoundaryAlarm.schedule(context);
        }
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        onUpdate(context, AppWidgetManager.getInstance(context), newWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        WorkScheduler.stop(context);
        BoundaryAlarm.cancel(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || Intent.ACTION_DATE_CHANGED.equals(action)
                || Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            if (hasWidgets(context)) {
                renderAll(context);
                WorkScheduler.start(context);
                BoundaryAlarm.schedule(context);
                if (ParkingHours.isOpen(Instant.now())) {
                    WorkScheduler.requestRefresh(context);
                }
            }
        }
    }

    static boolean hasWidgets(Context context) {
        return widgetIds(context).length > 0;
    }

    static void renderAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        render(context, manager, widgetIds(context));
    }

    private static int[] widgetIds(Context context) {
        return AppWidgetManager.getInstance(context).getAppWidgetIds(
                new ComponentName(context, ParkingWidgetProvider.class)
        );
    }

    private static void render(
            Context context, AppWidgetManager manager, int[] widgetIds) {
        if (widgetIds.length == 0) {
            return;
        }

        RemoteViews views =
                new RemoteViews(context.getPackageName(), R.layout.widget_parking);
        Intent dashboard = new Intent(Intent.ACTION_VIEW, Uri.parse(DASHBOARD_URL));
        dashboard.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent openDashboard = PendingIntent.getActivity(
                context, 0, dashboard,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_root, openDashboard);

        if (ParkingHours.isOpen(Instant.now())) {
            ParkingCache.Snapshot cache = ParkingCache.read(context);
            views.setViewVisibility(R.id.hours_message, View.GONE);
            views.setViewVisibility(R.id.available_count, View.VISIBLE);
            if (cache.available == null) {
                views.setTextViewText(R.id.available_count, "--");
                views.setViewVisibility(R.id.status_text, View.GONE);
            } else {
                String available =
                        NumberFormat.getNumberInstance(Locale.US).format(cache.available);
                views.setTextViewText(R.id.available_count, available);
                views.setTextViewTextSize(
                        R.id.available_count,
                        TypedValue.COMPLEX_UNIT_SP,
                        available.length() <= 3 ? 48
                                : available.length() <= 5 ? 38 : 28
                );
                String time = ParkingHours.formatTime(cache.updatedAt);
                views.setTextViewText(
                        R.id.status_text,
                        context.getString(
                                cache.isFresh(System.currentTimeMillis())
                                        ? R.string.updated_at : R.string.last_seen_at,
                                time
                        )
                );
                views.setViewVisibility(R.id.status_text, View.VISIBLE);
            }
        } else {
            views.setViewVisibility(R.id.available_count, View.GONE);
            views.setViewVisibility(R.id.status_text, View.GONE);
            views.setViewVisibility(R.id.hours_message, View.VISIBLE);
        }
        manager.updateAppWidget(widgetIds, views);
    }
}

