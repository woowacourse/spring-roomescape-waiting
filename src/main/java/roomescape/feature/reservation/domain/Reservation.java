package roomescape.feature.reservation.domain;

import java.time.LocalDate;
import lombok.Getter;
import roomescape.feature.reservation.error.type.ReservationErrorType;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.time.domain.Time;
import roomescape.global.error.exception.GeneralException;

@Getter
public class Reservation {

    // 금액이 관심사가 아닌 재구성(reconstruct) 편의 오버로드용 기본값
    private static final long UNDEFINED_AMOUNT = 0L;

    private final Long id;
    private final ReserverName name;
    private final Slot slot;
    private final ReservationStatus status;
    private final OrderStatus orderStatus;
    private final long amount;
    private final long version;

    private Reservation(
        Long id, ReserverName name, Slot slot, ReservationStatus status, OrderStatus orderStatus,
        long amount, long version) {
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.status = status;
        this.orderStatus = orderStatus;
        this.amount = amount;
        this.version = version;
    }

    public static Reservation create(
        ReserverName name, LocalDate date, Time time, Theme theme, ReservationStatus status, long amount) {
        Slot slot = new Slot(date, time, theme);
        validateFuture(slot);

        return new Reservation(null, name, slot, status, OrderStatus.PENDING, amount, 0L);
    }

    public static Reservation reconstruct(
        Long id, ReserverName name, LocalDate date,
        Time time, Theme theme, ReservationStatus status) {
        return new Reservation(id, name, new Slot(date, time, theme), status, OrderStatus.PENDING, UNDEFINED_AMOUNT, 0L);
    }

    public static Reservation reconstruct(
        Long id, ReserverName name, LocalDate date,
        Time time, Theme theme, ReservationStatus status, long version) {
        return new Reservation(
            id, name, new Slot(date, time, theme), status, OrderStatus.PENDING, UNDEFINED_AMOUNT, version);
    }

    public static Reservation reconstruct(
        Long id, ReserverName name, LocalDate date,
        Time time, Theme theme, ReservationStatus status, OrderStatus orderStatus, long version) {
        return new Reservation(id, name, new Slot(date, time, theme), status, orderStatus, UNDEFINED_AMOUNT, version);
    }

    public static Reservation reconstruct(
        Long id, ReserverName name, LocalDate date,
        Time time, Theme theme, ReservationStatus status, OrderStatus orderStatus, long amount, long version) {
        return new Reservation(id, name, new Slot(date, time, theme), status, orderStatus, amount, version);
    }

    public Reservation update(ReserverName requestName, LocalDate newDate, Time newTime, Theme newTheme) {
        Slot newSlot = new Slot(newDate, newTime, newTheme);
        validateUpdatable(requestName, newSlot);
        validateChanged(newSlot);

        return new Reservation(this.id, this.name, newSlot, this.status, this.orderStatus, this.amount, this.version);
    }

    public Reservation delete() {
        if (this.status == ReservationStatus.DELETED) {
            throw new GeneralException(ReservationErrorType.ALREADY_DELETED);
        }

        return new Reservation(
            this.id, this.name, this.slot, ReservationStatus.DELETED, this.orderStatus, this.amount, this.version);
    }

    public Reservation cancelActive(ReserverName requestName) {
        if (this.status != ReservationStatus.ACTIVE) {
            throw new GeneralException(ReservationErrorType.NOT_ACTIVE_RESERVATION);
        }
        validateCancelable(requestName);

        return new Reservation(
            this.id, this.name, this.slot, ReservationStatus.CANCELED, this.orderStatus, this.amount, this.version);
    }

    public Reservation cancelWaiting(ReserverName requestName) {
        if (this.status != ReservationStatus.WAITING) {
            throw new GeneralException(ReservationErrorType.NOT_WAITING_RESERVATION);
        }
        validateCancelable(requestName);

        return new Reservation(
            this.id, this.name, this.slot, ReservationStatus.CANCELED, this.orderStatus, this.amount, this.version);
    }

    public Reservation confirmWaiting() {
        if (this.status != ReservationStatus.WAITING) {
            throw new GeneralException(ReservationErrorType.NOT_WAITING_RESERVATION);
        }

        return new Reservation(
            this.id, this.name, this.slot, ReservationStatus.ACTIVE, this.orderStatus, this.amount, this.version);
    }

    public Reservation confirmOrder(long paidAmount) {
        if (this.status != ReservationStatus.ACTIVE) {
            throw new GeneralException(ReservationErrorType.NOT_ACTIVE_RESERVATION);
        }
        // PENDING(미결제)과 CONFIRMATION_REQUIRED(결과 불명)는 확정 가능. 이미 CONFIRMED 인 경우만 거절한다.
        if (this.orderStatus == OrderStatus.CONFIRMED) {
            throw new GeneralException(ReservationErrorType.ALREADY_CONFIRMED_ORDER);
        }
        if (this.amount != paidAmount) {
            throw new GeneralException(ReservationErrorType.PAYMENT_AMOUNT_MISMATCH);
        }

        return new Reservation(
            this.id, this.name, this.slot, this.status, OrderStatus.CONFIRMED, this.amount, this.version);
    }

    /**
     * 결제 결과가 불명확(read timeout 등)할 때 '확인 필요' 상태로 표시한다.
     * 결제 실패로 단정하지 않으며, 이후 재확인으로 확정될 수 있다.
     */
    public Reservation markConfirmationRequired() {
        if (this.status != ReservationStatus.ACTIVE) {
            throw new GeneralException(ReservationErrorType.NOT_ACTIVE_RESERVATION);
        }
        if (this.orderStatus != OrderStatus.PENDING) {
            throw new GeneralException(ReservationErrorType.ALREADY_CONFIRMED_ORDER);
        }

        return new Reservation(
            this.id, this.name, this.slot, this.status, OrderStatus.CONFIRMATION_REQUIRED, this.amount, this.version);
    }

    private void validateCancelable(ReserverName requestName) {
        if (!this.name.equals(requestName)) {
            throw new GeneralException(ReservationErrorType.RESERVATION_CANCEL_FORBIDDEN);
        }
        if (this.slot.isPast()) {
            throw new GeneralException(ReservationErrorType.PAST_RESERVATION_CANCEL);
        }
    }

    private static void validateFuture(Slot slot) {
        if (slot.isPast()) {
            throw new GeneralException(ReservationErrorType.PAST_RESERVATION_CREATE);
        }
    }

    private void validateUpdatable(ReserverName requestName, Slot newSlot) {
        if (!this.name.equals(requestName)) {
            throw new GeneralException(ReservationErrorType.RESERVATION_UPDATE_FORBIDDEN);
        }
        if (this.status != ReservationStatus.ACTIVE) {
            throw new GeneralException(ReservationErrorType.NOT_ACTIVE_RESERVATION);
        }

        validateFuture(this.slot);
        validateFuture(newSlot);
    }

    private void validateChanged(Slot newSlot) {
        if (this.slot.equals(newSlot)) {
            throw new GeneralException(ReservationErrorType.RESERVATION_NOT_CHANGED);
        }
    }

    public LocalDate getDate() {
        return slot.getDate();
    }

    public Time getTime() {
        return slot.getTime();
    }

    public Theme getTheme() {
        return slot.getTheme();
    }
}
