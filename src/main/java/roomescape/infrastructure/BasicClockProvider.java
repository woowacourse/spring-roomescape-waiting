package roomescape.infrastructure;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import roomescape.application.ClockProvider;

@Component
public class BasicClockProvider implements ClockProvider {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
