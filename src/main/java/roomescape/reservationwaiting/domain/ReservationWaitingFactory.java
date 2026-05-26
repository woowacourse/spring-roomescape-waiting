package roomescape.reservationwaiting.domain;

import roomescape.exception.ErrorCode;
import roomescape.exception.business.BusinessException;
import roomescape.reservation.domain.Reservation;

public class ReservationWaitingFactory {
    public ReservationWaiting create(String name, Reservation reservation) {
        validate(name, reservation);
        return ReservationWaiting.restore(null, name, reservation);
    }

    private void validate(String name, Reservation reservation) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("예약자 이름은 필수입니다.");
        }
        if (reservation.isPast()) {
            throw new BusinessException(ErrorCode.PAST_TIME_WAITING);
        }
        if (reservation.getId() == null) {
            throw new IllegalArgumentException("예약은 필수입니다.");
        }
    }
}
