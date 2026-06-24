package roomescape.reservationwaiting.domain;

import java.time.Clock;
import org.springframework.stereotype.Component;
import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorCode;
import roomescape.reservation.domain.Reservation;

@Component
public class ReservationWaitingFactory {

    private final Clock clock;

    public ReservationWaitingFactory(Clock clock) {
        this.clock = clock;
    }

    public ReservationWaiting create(String name, Reservation reservation) {
        validate(name, reservation);
        return ReservationWaiting.restore(null, name, reservation.getDate(), reservation.getTime(), reservation.getTheme());
    }

    private void validate(String name, Reservation reservation) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("예약자 이름은 필수입니다.");
        }
        if (reservation.isPast(clock)) {
            throw new BusinessException(ErrorCode.PAST_TIME_WAITING);
        }
        if (reservation.getId() == null) {
            throw new IllegalArgumentException("예약은 필수입니다.");
        }
    }
}
