package roomescape.time.fixture;

import java.time.LocalDate;
import java.time.LocalTime;

public class DateTimeFixture {

    public static final LocalDate SEVEN_DAYS_AGO = LocalDate.now().minusDays(7);
    public static final LocalDate YESTERDAY = LocalDate.now().minusDays(1);
    public static final LocalDate TODAY = LocalDate.now();
    public static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    public static final LocalDate DAY_AFTER_TOMORROW = LocalDate.now().plusDays(2);

    public static final LocalTime TIME_10_00 = LocalTime.of(10, 0);
    public static final LocalTime TIME_11_00 = LocalTime.of(11, 0);
    public static final LocalTime TIME_12_00 = LocalTime.of(12, 0);
    public static final LocalTime TIME_13_00 = LocalTime.of(13, 0);
}
