package roomescape.helper.fixture;

import java.time.LocalDate;

public class DateFixture {
    public static LocalDate today() {
        return LocalDate.now();
    }

    public static LocalDate tomorrow() {
        return LocalDate.now().plusDays(1);
    }

    public static LocalDate dayAfterTomorrow() {
        return LocalDate.now().plusDays(2);
    }
}
