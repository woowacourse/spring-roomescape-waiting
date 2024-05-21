package roomescape.reservation.domain;

import jakarta.persistence.Embeddable;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ValidateException;

@Embeddable
public record ReservationOrder(Long reservationOrder) {

    public ReservationOrder(final Long reservationOrder) {
        this.reservationOrder = reservationOrder;

        validateRange(reservationOrder);
    }

    private void validateRange(final Long reservationOrder) {
        if (reservationOrder < 0) {
            System.out.println(reservationOrder);
            throw new ValidateException(ErrorType.INVALID_REQUEST_DATA, "예약 순서(ReservationOrder)는 음수일 수 없습니다.");
        }
    }

    public ReservationOrder increase() {
        return new ReservationOrder(reservationOrder - 1);
    }

    public boolean isFirst() {
        return reservationOrder == 0;
    }
}
