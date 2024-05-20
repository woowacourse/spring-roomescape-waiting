package roomescape.util;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@Primary
public class OldDateTimeFormatter implements DateTimeFormatter {
    public LocalDate getDate() {
        return LocalDate.parse("2000-01-01");
    }

    public LocalTime getTime() {
        return LocalTime.parse("10:00");
    }
}
