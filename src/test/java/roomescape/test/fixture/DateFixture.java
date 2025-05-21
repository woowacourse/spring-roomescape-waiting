package roomescape.test.fixture;

import java.time.LocalDate;

public class DateFixture {

    public static final LocalDate YESTERDAY = LocalDate.now().minusDays(1);
    public static final LocalDate TODAY = LocalDate.now();
    public static final LocalDate NEXT_DAY = LocalDate.now().plusDays(1);
}
