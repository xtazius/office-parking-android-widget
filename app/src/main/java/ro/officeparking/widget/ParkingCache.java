package ro.officeparking.widget;

import android.content.Context;
import android.content.SharedPreferences;

final class ParkingCache {
    private static final String PREFERENCES = "parking_widget";
    private static final String KEY_AVAILABLE = "available";
    private static final String KEY_UPDATED_AT = "updated_at";
    private static final String KEY_LAST_FETCH_SUCCEEDED = "last_fetch_succeeded";
    private static final long STALE_AFTER_MS = 30 * 60 * 1000L;

    private ParkingCache() {
    }

    static Snapshot read(Context context) {
        SharedPreferences preferences =
                context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        try {
            String raw = preferences.getString(KEY_AVAILABLE, null);
            long updatedAt = preferences.getLong(KEY_UPDATED_AT, 0);
            if (raw == null || updatedAt <= 0) {
                return Snapshot.empty();
            }
            double available = Double.parseDouble(raw);
            if (!Double.isFinite(available) || available < 0) {
                return Snapshot.empty();
            }
            return new Snapshot(
                    available,
                    updatedAt,
                    preferences.getBoolean(KEY_LAST_FETCH_SUCCEEDED, false)
            );
        } catch (RuntimeException e) {
            return Snapshot.empty();
        }
    }

    static boolean saveSuccess(Context context, double available, long updatedAt) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_AVAILABLE, Double.toString(available))
                .putLong(KEY_UPDATED_AT, updatedAt)
                .putBoolean(KEY_LAST_FETCH_SUCCEEDED, true)
                .commit();
    }

    static void recordFailure(Context context) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_LAST_FETCH_SUCCEEDED, false)
                .apply();
    }

    static final class Snapshot {
        final Double available;
        final long updatedAt;
        private final boolean lastFetchSucceeded;

        private Snapshot(Double available, long updatedAt, boolean lastFetchSucceeded) {
            this.available = available;
            this.updatedAt = updatedAt;
            this.lastFetchSucceeded = lastFetchSucceeded;
        }

        static Snapshot empty() {
            return new Snapshot(null, 0, false);
        }

        boolean isFresh(long nowMillis) {
            long age = nowMillis - updatedAt;
            return available != null
                    && lastFetchSucceeded
                    && age >= 0
                    && age <= STALE_AFTER_MS;
        }
    }
}

