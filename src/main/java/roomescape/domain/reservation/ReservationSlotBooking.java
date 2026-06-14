package roomescape.domain.reservation;

import java.time.LocalDateTime;
import roomescape.domain.reservationwaiting.ReservationWaiting;
import roomescape.domain.reservationwaiting.ReservationWaitingLine;

public class ReservationSlotBooking {
    public static final String DIFFERENT_SLOT_MESSAGE = "예약과 대기 줄의 슬롯이 일치하지 않습니다.";

    private final Reservation reservation;
    private final ReservationWaitingLine waitingLine;

    public ReservationSlotBooking(
            final Reservation reservation,
            final ReservationWaitingLine waitingLine
    ) {
        validate(reservation, waitingLine);
        this.reservation = reservation;
        this.waitingLine = waitingLine;
    }

    public ReservationCancellation cancel(final LocalDateTime requestedAt) {
        return waitingLine.first()
                .map(waiting -> promote(waiting, requestedAt))
                .orElseGet(() -> ReservationCancellation.withoutPromotion(reservation));
    }

    private ReservationCancellation promote(
            final ReservationWaiting waiting,
            final LocalDateTime requestedAt
    ) {
        return ReservationCancellation.withPromotion(
                reservation,
                waiting,
                waiting.toReservation(requestedAt)
        );
    }

    private void validate(
            final Reservation reservation,
            final ReservationWaitingLine waitingLine
    ) {
        if (reservation == null) {
            throw new IllegalArgumentException("예약은 비어있으면 안됩니다.");
        }
        if (waitingLine == null) {
            throw new IllegalArgumentException("대기 줄은 비어있으면 안됩니다.");
        }
        if (!waitingLine.isEmpty() && !waitingLine.isForSlot(reservation.getSlot())) {
            throw new IllegalArgumentException(DIFFERENT_SLOT_MESSAGE);
        }
    }
}
