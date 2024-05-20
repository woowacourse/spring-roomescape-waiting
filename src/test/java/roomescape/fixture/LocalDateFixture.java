package roomescape.fixture;

import java.time.LocalDate;

public class LocalDateFixture {

    public static LocalDate TODAY = LocalDate.now();
    public static LocalDate AFTER_ONE_DAYS_DATE = LocalDate.now().plusDays(1L);
    public static LocalDate AFTER_TWO_DAYS_DATE = LocalDate.now().plusDays(2L);
    public static LocalDate AFTER_THREE_DAYS_DATE = LocalDate.now().plusDays(3L);
    public static LocalDate BEFORE_ONE_DAYS_DATE = LocalDate.now().minusDays(1L);
    public static LocalDate BEFORE_TWO_DAYS_DATE = LocalDate.now().minusDays(2L);
    public static LocalDate BEFORE_THREE_DAYS_DATE = LocalDate.now().minusDays(3L);

}
