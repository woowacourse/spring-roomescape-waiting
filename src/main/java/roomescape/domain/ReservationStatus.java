package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.Objects;

@Embeddable
public class ReservationStatus {

    private static final long FIRST_PRIORITY = 0L;
    private static final ReservationStatus FIRST_RESERVATION_STATUS =
            new ReservationStatus(Status.RESERVED, FIRST_PRIORITY);

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    @Column(nullable = false)
    private long priority;

    protected ReservationStatus() {
    }

    public ReservationStatus(Status status, long priority) {
        validate(status, priority);
        this.status = status;
        this.priority = priority;
    }

    private void validate(Status status, long priority) {
        if (priority < FIRST_PRIORITY) {
            throw new IllegalArgumentException("대기 번호는 0이상의 수 입니다.");
        }
        if (status.isReserved() && priority != FIRST_PRIORITY) {
            throw new IllegalArgumentException("예약 상태에서는 대기번호를 갖을 수 없습니다.");
        }
        if (status.isWaiting() && priority == FIRST_PRIORITY) {
            throw new IllegalArgumentException("대기 상태에서는 대기 번호가 존재해야 됩니다.");
        }
    }

    public static ReservationStatus getFirstReservationStatus() {
        return FIRST_RESERVATION_STATUS;
    }

    public ReservationStatus updateDecreasedPriorityStatus() {
        if (this.priority - 1 == FIRST_PRIORITY) {
            return new ReservationStatus(Status.RESERVED, FIRST_PRIORITY);
        }
        return new ReservationStatus(Status.WAITING, this.priority - 1);
    }

    public Status getStatus() {
        return status;
    }

    public Long getPriority() {
        return priority;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationStatus that = (ReservationStatus) o;
        return status == that.status && priority == that.priority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, priority);
    }
}
