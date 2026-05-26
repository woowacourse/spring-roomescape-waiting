package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import roomescape.reservation.exception.PastReservationException;

public class ReservationTime {
    private final Long id;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;

    public ReservationTime(LocalDateTime startAt, LocalDateTime endAt) {
        this(null, startAt, endAt);
    }

    public ReservationTime(Long id, LocalDateTime startAt, LocalDateTime endAt) {
        validateTimesPresent(startAt, endAt);
        validateTimeOrder(startAt, endAt);
        this.id = id;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    private void validateTimesPresent(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null) {
            throw new IllegalArgumentException("시작 시간과 종료 시간은 비어있을 수 없습니다.");
        }
    }

    private void validateTimeOrder(LocalDateTime startAt, LocalDateTime endAt) {
        if (!endAt.isAfter(startAt)) {
            throw new IllegalArgumentException("종료 시간은 시작 시간 이후여야 합니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public LocalDate getDate() {
        return startAt.toLocalDate();
    }

    public void validateReservableSchedule() {
        if (isPast()) {
            throw PastReservationException.pastReservation();
        }
    }

    public void validateUpdatableReservation() {
        if (isPast()) {
            throw PastReservationException.pastUpdate();
        }
    }

    public void validateNotPastForCancel() {
        if (startAt.isBefore(LocalDateTime.now())) {
            throw PastReservationException.pastCancel();
        }
    }

    private boolean isPast() {
        return startAt.isBefore(LocalDateTime.now());
    }
}
