package ro.officeparking.widget;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class ParkingHours {
    static final ZoneId ZONE = ZoneId.of("Europe/Bucharest");
    private static final DateTimeFormatter CLOCK =
            DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
    private static final LocalTime OPENING = LocalTime.of(7, 0);
    private static final LocalTime CLOSING = LocalTime.of(16, 0);

    private ParkingHours() {
    }

    static boolean isOpen(Instant now) {
        ZonedDateTime local = now.atZone(ZONE);
        DayOfWeek day = local.getDayOfWeek();
        int minuteOfDay = local.getHour() * 60 + local.getMinute();
        return day.getValue() <= DayOfWeek.FRIDAY.getValue()
                && minuteOfDay >= 7 * 60
                && minuteOfDay < 16 * 60;
    }

    static String formatTime(long epochMillis) {
        return CLOCK.format(Instant.ofEpochMilli(epochMillis).atZone(ZONE));
    }

    static long nextBoundaryMillis(Instant now) {
        LocalDate today = now.atZone(ZONE).toLocalDate();
        for (int offset = 0; offset <= 7; offset++) {
            LocalDate date = today.plusDays(offset);
            if (date.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue()) {
                continue;
            }
            for (LocalTime time : new LocalTime[]{OPENING, CLOSING}) {
                Instant boundary = date.atTime(time).atZone(ZONE).toInstant();
                if (boundary.isAfter(now)) {
                    return boundary.toEpochMilli();
                }
            }
        }
        throw new IllegalStateException("Could not find the next office-hours boundary");
    }
}

