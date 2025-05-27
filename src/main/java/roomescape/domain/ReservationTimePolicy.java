package roomescape.domain;

import java.time.LocalTime;
import org.springframework.stereotype.Component;
import roomescape.exception.ReservationTimeException;

@Component
public class ReservationTimePolicy {
    public static final LocalTime RESERVATION_START_TIME = LocalTime.of(12, 0);
    public static final LocalTime RESERVATION_END_TIME = LocalTime.of(22, 0);

    public void validate(LocalTime time) {
        boolean isOpenTime = !time.isBefore(RESERVATION_START_TIME) && !time.isAfter(RESERVATION_END_TIME);

        if(!isOpenTime) {
            throw new ReservationTimeException("해당 시간은 예약 가능 시간이 아닙니다.");
        }
    }
}
