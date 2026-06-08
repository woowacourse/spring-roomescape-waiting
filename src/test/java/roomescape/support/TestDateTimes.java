package roomescape.support;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public final class TestDateTimes {

    public static final LocalDateTime FIXED = LocalDateTime.of(2025, 1, 1, 12, 0);

    private TestDateTimes() {
    }

    public static Clock fixedClock() {
        return Clock.fixed(FIXED.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
    }

    public static LocalDate today() {
        return LocalDate.now();
    }

    public static LocalDate tomorrow() {
        return LocalDate.now().plusDays(1);
    }

    public static LocalDate yesterday() {
        return LocalDate.now().minusDays(1);
    }

    public static LocalDate daysLater(int days) {
        return LocalDate.now().plusDays(days);
    }

    public static LocalTime defaultTime() {
        return LocalTime.of(10, 0);
    }

    public static LocalTime hour(int hour) {
        return LocalTime.of(hour, 0);
    }
}
