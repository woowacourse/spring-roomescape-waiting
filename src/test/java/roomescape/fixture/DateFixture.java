package roomescape.fixture;

import java.time.LocalDate;

public class DateFixture {
    public static LocalDate getNextDay() {
        return LocalDate.now().plusDays(1);
    }
}
