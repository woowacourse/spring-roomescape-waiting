package roomescape.domain;

import roomescape.util.Validator;

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

    private void validateId(Long id) {
        Validator.positive(id, "Id는 0보다 작거나 같을 수 없습니다.");
    }

    private void validateReservationId(Long reservationId) {
        Validator.notNull(reservationId, "예약 ID는 null일 수 없습니다.");
        Validator.positive(reservationId, "예약 ID는 0보다 작거나 같을 수 없습니다.");
    }

    private void validateMemberId(Long memberId) {
        Validator.notNull(memberId, "회원 ID는 null일 수 없습니다.");
        Validator.positive(memberId, "회원 ID는 0보다 작거나 같을 수 없습니다.");
    }

    private void validateCreatedAt(LocalDateTime createdAt) {
        Validator.notNull(createdAt, "생성 시간은 null일 수 없습니다.");
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
}
