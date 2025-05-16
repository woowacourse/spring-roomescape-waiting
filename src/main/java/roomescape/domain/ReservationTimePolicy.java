package roomescape.domain;

import java.time.LocalTime;
import org.springframework.stereotype.Component;

@Component
public class ReservationTimePolicy {
    public static final LocalTime RESERVATION_START_TIME = LocalTime.of(12, 0);
    public static final LocalTime RESERVATION_END_TIME = LocalTime.of(22, 0);

    public boolean canCreate(LocalTime time) {
        return !time.isBefore(RESERVATION_START_TIME) && !time.isAfter(RESERVATION_END_TIME);
    }
}
