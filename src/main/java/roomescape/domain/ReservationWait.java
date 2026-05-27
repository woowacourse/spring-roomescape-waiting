package roomescape.domain;

import java.time.LocalDateTime;

public class ReservationWait {

    private final Long id;
    private final Long reservationId;
    private final Long memberId;
    private final LocalDateTime createdAt;

    public ReservationWait(Long id, Long reservationId, Long memberId, LocalDateTime createdAt) {
        validateId(id);
        validateReservationId(reservationId);
        validateMemberId(memberId);
        validateCreatedAt(createdAt);

        this.id = id;
        this.reservationId = reservationId;
        this.memberId = memberId;
        this.createdAt = createdAt;
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

    private void validateId(Long id) {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("Id는 0보다 작거나 같을 수 없습니다.");
        }
    }

    private void validateReservationId(Long reservationId) {
        if (reservationId == null) {
            throw new IllegalArgumentException("예약 ID는 null일 수 없습니다.");
        }
        if (reservationId <= 0) {
            throw new IllegalArgumentException("예약 ID는 0보다 작거나 같을 수 없습니다.");
        }
    }

    private void validateMemberId(Long memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 null일 수 없습니다.");
        }
        if (memberId <= 0) {
            throw new IllegalArgumentException("회원 ID는 0보다 작거나 같을 수 없습니다.");
        }
    }

    private void validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("생성 시간은 null일 수 없습니다.");
        }
    }
}
