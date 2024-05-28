package roomescape.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static Date getCurrentTime() {
        return new Date();
    }

    public static Date getAfterTenMinutes() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 10);
        return calendar.getTime();
    }

    public static LocalDate getYesterday() {
        return LocalDate.now().minusDays(1);
    }

    public static LocalDate getaWeekAgo() {
        return LocalDate.now().minusDays(7);
    }

    public static boolean isPastDateTime(LocalDate date, LocalTime time) {
        boolean isPastDate = date.isBefore(LocalDate.now());
        boolean isPastTime = date.isEqual(LocalDate.now()) && time.isBefore(LocalTime.now());
        return isPastDate || isPastTime;
    }
}
