package ro.officeparking.widget;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

final class WorkScheduler {
    private static final String PERIODIC_NAME = "parking_periodic_refresh";
    private static final String IMMEDIATE_NAME = "parking_immediate_refresh";

    private WorkScheduler() {
    }

    static void start(Context context) {
        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(
                        ParkingRefreshWorker.class, 15, TimeUnit.MINUTES
                ).setInitialDelay(15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniquePeriodicWork(
                        PERIODIC_NAME, ExistingPeriodicWorkPolicy.KEEP, request
                );
    }

    static void requestRefresh(Context context) {
        OneTimeWorkRequest request =
                new OneTimeWorkRequest.Builder(ParkingRefreshWorker.class).build();
        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniqueWork(IMMEDIATE_NAME, ExistingWorkPolicy.KEEP, request);
    }

    static void stop(Context context) {
        WorkManager manager = WorkManager.getInstance(context.getApplicationContext());
        manager.cancelUniqueWork(PERIODIC_NAME);
        manager.cancelUniqueWork(IMMEDIATE_NAME);
    }
}

