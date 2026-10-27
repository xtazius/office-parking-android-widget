package ro.officeparking.widget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

final class ParkingRepository {
    private static final String DATA_URL =
            "https://gist.githubusercontent.com/spielmannlaszlo11-dotcom/"
                    + "9cc975e2d6d46b1fbef3409729fbccab/raw/parking.json";
    private static final int TIMEOUT_MS = 10_000;
    private static final int MAX_RESPONSE_BYTES = 1_000_000;

    private ParkingRepository() {
    }

    static double fetchAvailable() throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(DATA_URL + "?t=" + System.currentTimeMillis());
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Cache-Control", "no-cache, no-store, max-age=0");
            connection.setRequestProperty("Pragma", "no-cache");

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new IOException("Parking request returned HTTP " + status);
            }
            return parseAvailable(readBody(connection.getInputStream()));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    static double parseAvailable(String json) throws IOException {
        final JSONArray groups;
        try {
            groups = new JSONArray(json);
        } catch (JSONException e) {
            throw new IOException("Parking response is not a JSON array", e);
        }

        double total = 0;
        int validGroups = 0;
        for (int i = 0; i < groups.length(); i++) {
            JSONObject group = groups.optJSONObject(i);
            if (group == null) {
                continue;
            }
            Double capacity = nonNegativeNumber(group.opt("capacity"));
            Double occupation = nonNegativeNumber(group.opt("occupation"));
            if (capacity == null || occupation == null) {
                continue;
            }
            total += Math.max(0, capacity - occupation);
            if (!Double.isFinite(total)) {
                throw new IOException("Parking total is not finite");
            }
            validGroups++;
        }
        if (validGroups == 0) {
            throw new IOException("Parking response has no valid groups");
        }
        return total;
    }

    private static Double nonNegativeNumber(Object raw) {
        if (!(raw instanceof Number) && !(raw instanceof String)) {
            return null;
        }
        try {
            String text = raw.toString().trim();
            if (text.isEmpty()) {
                return null;
            }
            double value = Double.parseDouble(text);
            return Double.isFinite(value) && value >= 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String readBody(InputStream input) throws IOException {
        try (InputStream stream = input;
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                if (output.size() + read > MAX_RESPONSE_BYTES) {
                    throw new IOException("Parking response is too large");
                }
                output.write(buffer, 0, read);
            }
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}

