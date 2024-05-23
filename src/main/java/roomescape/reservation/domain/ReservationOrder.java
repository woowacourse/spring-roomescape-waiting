package roomescape.reservation.domain;

import jakarta.persistence.Embeddable;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ValidateException;

@Embeddable
public record ReservationOrder(Long reservationOrder) {

    public ReservationOrder(final Long reservationOrder) {
        this.reservationOrder = reservationOrder;

        validateNotNull(reservationOrder);
        validateRange(reservationOrder);
    }

    private void validateNotNull(final Long reservationOrder) {
        if (reservationOrder == null) {
            throw new ValidateException(ErrorType.INVALID_REQUEST_DATA, "예약 순서(ReservationOrder)는 null 일 수 없습니다.");
        }
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

    public boolean isReservationPossibleOrder() {
        return reservationOrder == 0;
    }

    public boolean isFirstWaitingOrder() {
        return reservationOrder == 1;
    }
}
