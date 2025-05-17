package roomescape;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.stereotype.Component;

@Component
public class CurrentDateTimeGenerator implements CurrentDateTime {
    @Override
    public LocalDate getDate() {
        return LocalDate.now();
    }

    @Override
    public LocalTime getTime() {
        return LocalTime.now();
    }

    @Override
    public LocalDateTime getDateTime() {
        return LocalDateTime.now();
    }
}
