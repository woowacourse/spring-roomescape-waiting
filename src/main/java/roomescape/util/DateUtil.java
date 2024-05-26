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

    public static boolean isPastDateTime(LocalDate date, LocalTime time) {
        LocalDate nowDate = LocalDate.now();
        LocalTime nowTime = LocalTime.now();
        boolean isPastDate = date.isBefore(nowDate);
        boolean isPastTime = date.isEqual(nowDate) && time.isBefore(nowTime);

        return isPastDate || isPastTime;
    }

    public static LocalDate aWeekAgo() {
        return LocalDate.now().minusDays(7);
    }

    public static LocalDate yesterday() {
        return LocalDate.now().minusDays(1);
    }
}
