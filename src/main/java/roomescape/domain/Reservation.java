package roomescape.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

public class Reservation {
    private final Long id;
    private final String name;
    private long reservationSlotId;
    private Status status;
    private LocalDateTime updateAt;

    public Reservation(Long id, String name, Long reservationSlotId, Status status, LocalDateTime updateAt) {
        validateName(name);
        this.id = id;
        this.name = name;
        this.reservationSlotId = reservationSlotId;
        this.status = status;
        this.updateAt = updateAt;
    }

    public boolean isReserved() {
        return status == Status.RESERVED;
    }

    public boolean isWaiting() {
        return status == Status.WAITING;
    }

    public boolean isPaymentPending() {
        return status == Status.PAYMENT_PENDING;
    }

    public boolean isUpdatedAtBefore(Reservation other) {
        return updateAt.isBefore(other.updateAt);
    }

    public void promote() {
        if (!isWaiting()) {
            throw new CustomException(ErrorCode.RESERVATION_STATUS_UNAVAILABLE);
        }
        this.status = Status.RESERVED;
    }

    public void confirmPayment() {
        if (!isPaymentPending()) {
            throw new CustomException(ErrorCode.RESERVATION_STATUS_UNAVAILABLE);
        }
        this.status = Status.RESERVED;
    }

    public void update(LocalDateTime now, long reservationSlotId, Status status) {
        validateUpdateAt(now);
        validateNotCanceledStatus();
        this.reservationSlotId = reservationSlotId;
        this.status = status;
        this.updateAt = now;
    }

    public void cancel(LocalDateTime now) {
        validateUpdateAt(now);
        validateNotCanceledStatus();
        this.status = Status.CANCELED;
        this.updateAt = now;
    }

    private void validateName(String name) {
        if (name.length() > 255) {
            throw new CustomException(ErrorCode.RESERVATION_NAME_TOO_LONG);
        }
    }

    private void validateUpdateAt(LocalDateTime now) {
        if (now.isBefore(this.updateAt)) {
            throw new CustomException(ErrorCode.RESERVATION_DATE_UNAVAILABLE);
        }
    }

    private void validateNotCanceledStatus() {
        if (this.status == Status.CANCELED) {
            throw new CustomException(ErrorCode.RESERVATION_STATUS_UNAVAILABLE);
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getReservationSlotId() {
        return reservationSlotId;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return reservationSlotId == that.reservationSlotId && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, reservationSlotId);
    }
}
