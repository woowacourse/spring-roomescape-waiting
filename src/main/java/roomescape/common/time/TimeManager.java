package roomescape.common.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

@Component
public class TimeManager {
    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    public LocalDate today() {
        return LocalDate.now(SERVICE_ZONE);
    }

    public LocalDateTime now() {
        return LocalDateTime.now(SERVICE_ZONE);
    }
}
