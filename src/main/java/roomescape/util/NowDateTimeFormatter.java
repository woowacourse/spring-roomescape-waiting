package roomescape.util;

import java.time.LocalDate;
import java.time.LocalTime;

public class NowDateTimeFormatter implements DateTimeFormatter {

    public LocalDate getDate() {
        return LocalDate.now();
    }

    public LocalTime getTime() {
        return LocalTime.now();
    }
}
