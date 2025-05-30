package roomescape.global.util;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.stereotype.Component;

@Component
public class DateTimeService {
    private final Clock clock;

    public DateTimeService(final Clock clock) {
        this.clock = clock;
    }

    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    public LocalTime currentTime() {
        return LocalTime.now(clock);
    }
}
