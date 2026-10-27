# Office Parking Widget for Android

A native Android home-screen widget showing the number of available office
parking spaces. It uses the same parking data and dashboard as the former
iPhone Scriptable widget. Scriptable is not used on Android.

## Requirements

- An Android phone running Android 8.0 (API 26) or newer.
- Internet access for the first successful parking data refresh.

## Install the APK

1. Copy OfficeParking.apk to the phone and open the file. Android may ask you
   to allow installation from your file manager before it installs.
2. Open **Office Parking** once after installation. On launchers that support
   widget pinning, confirm the prompt to add the widget to your home screen.
3. If your launcher does not show that prompt, long-press an empty area of the
   home screen, choose **Widgets**, then drag **Office Parking** onto the screen.

Android requires a launcher action to place a home-screen widget. Installing
an APK alone cannot place one automatically.

## Build the APK with GitHub Actions

No Android build tools are needed on your PC. Push this folder to a GitHub
repository. The included [Build Android APK](./.github/workflows/build-apk.yml)
workflow runs on a GitHub-hosted Ubuntu runner after every push. You can also
start it from the repository's **Actions** tab with **Run workflow**.

When the run succeeds, download its **OfficeParking-Android-APK** artifact and
extract **OfficeParking.apk** from the ZIP. Copy that APK to the phone and
follow the installation steps above. The APK is debug-signed, so it is
installable without any signing setup. A later build may use a different debug
key; in that case, uninstall the earlier build before installing the new one.

## What the widget shows

- Monday to Friday, from 07:00 inclusive until 16:00 exclusive in the
  Europe/Bucharest time zone: **AVAILABLE SPACES** and the total.
- Outside those hours: **Shown Mon-Fri, 07:00-16:00**.
- After a successful refresh: **Updated HH:mm**.
- If a refresh fails: the saved value with **Last seen HH:mm**. On a later
  widget update, a saved value older than 30 minutes is also marked this way.
- If no successful value has ever been saved and a refresh fails: **--**.

The time in the status line is Romania time. Tapping the widget opens
[the parking dashboard](https://office-parking-ro.github.io/parking-dashboard/).

Android schedules a background refresh about every 15 minutes while a widget
is present. The launcher also requests widget updates, and an inexact alarm
requests a refresh at opening and closing times. Android may defer background
work to save battery, so a change can appear later than its scheduled time.
The business-hours rule itself is checked every time the widget renders.

## Data and failure handling

The app fetches the JSON array of parking groups from:

https://gist.githubusercontent.com/spielmannlaszlo11-dotcom/9cc975e2d6d46b1fbef3409729fbccab/raw/parking.json

It adds a timestamp query parameter and sends no-cache headers to reduce
cached HTTP responses. For each group with finite, nonnegative numeric
capacity and occupation, it adds max(0, capacity - occupation). Invalid
groups are skipped. An empty array, a response with no valid groups, an HTTP
error, or malformed JSON is treated as a failed refresh. A valid total of
zero is saved and displayed as zero; a failure never becomes zero.

The last successful total and its retrieval time are stored on the phone.
They survive app restarts and are replaced only after another successful
response. No sign-in or account is required.

## Native Android implementation

- [ParkingWidgetProvider.java](./app/src/main/java/ro/officeparking/widget/ParkingWidgetProvider.java)
  creates the Android home-screen widget using RemoteViews and handles taps.
- [ParkingRefreshWorker.java](./app/src/main/java/ro/officeparking/widget/ParkingRefreshWorker.java)
  refreshes in the background using AndroidX WorkManager.
- [ParkingRepository.java](./app/src/main/java/ro/officeparking/widget/ParkingRepository.java)
  retrieves and validates the JSON.
- [ParkingCache.java](./app/src/main/java/ro/officeparking/widget/ParkingCache.java)
  stores the last successful value in SharedPreferences.
- [ParkingHours.java](./app/src/main/java/ro/officeparking/widget/ParkingHours.java)
  applies the Romania business-hours rule.

The former Scriptable script depended on iOS-only widget, color, font,
networking, file, date formatting, and refresh APIs. These are replaced by
Android components, layouts, resources, and schedulers. The iPhone setup
steps involving the App Store, AirDrop, iCloud Drive, and Scriptable's
widget configurator do not apply.
