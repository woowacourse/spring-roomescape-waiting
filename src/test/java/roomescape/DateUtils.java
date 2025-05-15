package roomescape;

import java.time.LocalDate;

public class DateUtils {

    public static LocalDate yesterday() {
        return LocalDate.now().minusDays(1);
    }

    public static LocalDate today() {
        return LocalDate.now();
    }

    public static LocalDate tomorrow() {
        return LocalDate.now().plusDays(1);
    }

    public static LocalDate afterNDay(final int days) {
        return LocalDate.now().plusDays(days);
    }
}
