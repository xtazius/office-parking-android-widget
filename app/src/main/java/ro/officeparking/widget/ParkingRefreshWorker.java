package ro.officeparking.widget;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.time.Instant;

public final class ParkingRefreshWorker extends Worker {
    private static final String TAG = "ParkingWidget";
    private static final Object FETCH_LOCK = new Object();

    public ParkingRefreshWorker(@NonNull Context context, @NonNull WorkerParameters parameters) {
        super(context, parameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        synchronized (FETCH_LOCK) {
            Context context = getApplicationContext();
            if (!ParkingWidgetProvider.hasWidgets(context)) {
                return Result.success();
            }
            if (ParkingHours.isOpen(Instant.now())) {
                try {
                    double available = ParkingRepository.fetchAvailable();
                    if (!ParkingCache.saveSuccess(
                            context, available, System.currentTimeMillis())) {
                        throw new IOException("Could not save the parking value");
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Parking refresh failed; showing the last successful value", e);
                    ParkingCache.recordFailure(context);
                }
            }
            ParkingWidgetProvider.renderAll(context);
            return Result.success();
        }
    }
}

