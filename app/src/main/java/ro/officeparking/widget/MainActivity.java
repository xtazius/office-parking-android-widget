package ro.officeparking.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

public final class MainActivity extends Activity {
    private static final String ONBOARDING_PREFERENCES = "widget_onboarding";
    private static final String KEY_PIN_PROMPT_SHOWN = "pin_prompt_shown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.add_widget_button).setOnClickListener(view -> requestWidgetPin());

        findViewById(R.id.open_dashboard_button).setOnClickListener(view -> {
            Intent dashboard = new Intent(
                    Intent.ACTION_VIEW, Uri.parse(ParkingWidgetProvider.DASHBOARD_URL)
            );
            try {
                startActivity(dashboard);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.no_browser, Toast.LENGTH_LONG).show();
            }
        });

        if (!ParkingWidgetProvider.hasWidgets(this)
                && !getSharedPreferences(ONBOARDING_PREFERENCES, MODE_PRIVATE)
                        .getBoolean(KEY_PIN_PROMPT_SHOWN, false)) {
            getSharedPreferences(ONBOARDING_PREFERENCES, MODE_PRIVATE)
                    .edit()
                    .putBoolean(KEY_PIN_PROMPT_SHOWN, true)
                    .apply();
            findViewById(R.id.add_widget_button).post(this::requestWidgetPin);
        }
    }

    private void requestWidgetPin() {
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        ComponentName provider = new ComponentName(this, ParkingWidgetProvider.class);
        if (!manager.isRequestPinAppWidgetSupported()
                || !manager.requestPinAppWidget(provider, null, null)) {
            Toast.makeText(this, R.string.add_widget_manually, Toast.LENGTH_LONG).show();
        }
    }
}
