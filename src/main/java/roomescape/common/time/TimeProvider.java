package roomescape.common.time;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@AllArgsConstructor
public class TimeProvider {

    private final Clock clock;

    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    public LocalTime nowTime() {
        return LocalTime.now(clock);
    }
}
