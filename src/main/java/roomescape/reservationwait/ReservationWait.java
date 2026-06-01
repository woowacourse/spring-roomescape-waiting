package roomescape.reservationwait;

import java.time.LocalDateTime;
import roomescape.reservationwait.exception.PastReservationWaitNotAllowedException;
import roomescape.reservationwait.exception.SelfReservationWaitNotAllowedException;
import roomescape.reservation.Reservation;

public class ReservationWait {

    private final Long id;
    private final Long reservationId;
    private final Long memberId;
    private final LocalDateTime createdAt;

    public ReservationWait(Long id, Long reservationId, Long memberId, LocalDateTime createdAt) {
        validateReservationId(reservationId);
        validateMemberId(memberId);

        this.id = id;
        this.reservationId = reservationId;
        this.memberId = memberId;
        this.createdAt = createdAt;
    }

    public static ReservationWait create(Reservation reservation, Long memberId) {
        if (reservation.isPast()) {
            throw new PastReservationWaitNotAllowedException();
        }
        if (reservation.isReservedBy(memberId)) {
            throw new SelfReservationWaitNotAllowedException();
        }
        return new ReservationWait(null, reservation.getId(), memberId, null);
    }

    public Long getId() {
        return id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    private void validateReservationId(Long reservationId) {
        if (reservationId == null) {
            throw new IllegalArgumentException("예약 ID는 null일 수 없습니다.");
        }
    }

    private void validateMemberId(Long memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 null일 수 없습니다.");
        }
    }
}
