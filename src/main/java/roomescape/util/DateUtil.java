package roomescape.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static final LocalDate TODAY = LocalDate.now();
    public static final LocalDate YESTERDAY = LocalDate.now().minusDays(1);
    public static final LocalDate A_WEEK_AGO = LocalDate.now().minusDays(7);
    public static final LocalTime CURRENT_TIME = LocalTime.now();

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
        boolean isPastDate = date.isBefore(TODAY);
        boolean isPastTime = date.isEqual(TODAY) && time.isBefore(CURRENT_TIME);
        return isPastDate || isPastTime;
    }
}
