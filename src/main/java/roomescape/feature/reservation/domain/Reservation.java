package roomescape.feature.reservation.domain;

import java.time.LocalDate;
import lombok.Getter;
import roomescape.feature.reservation.error.type.ReservationErrorType;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.time.domain.Time;
import roomescape.global.error.exception.GeneralException;

@Getter
public class Reservation {

    private final Long id;
    private final ReserverName name;
    private final Slot slot;
    private final ReservationStatus status;
    private final OrderStatus orderStatus;
    private final long version;

    private Reservation(
        Long id, ReserverName name, Slot slot, ReservationStatus status, OrderStatus orderStatus, long version) {
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.status = status;
        this.orderStatus = orderStatus;
        this.version = version;
    }

    public static Reservation create(ReserverName name, LocalDate date, Time time, Theme theme, ReservationStatus status) {
        Slot slot = new Slot(date, time, theme);
        validateFuture(slot);

        return new Reservation(null, name, slot, status, OrderStatus.PENDING, 0L);
    }

    public static Reservation reconstruct(
        Long id, ReserverName name, LocalDate date,
        Time time, Theme theme, ReservationStatus status) {
        return new Reservation(id, name, new Slot(date, time, theme), status, OrderStatus.PENDING, 0L);
    }

    public static Reservation reconstruct(
        Long id, ReserverName name, LocalDate date,
        Time time, Theme theme, ReservationStatus status, long version) {
        return new Reservation(id, name, new Slot(date, time, theme), status, OrderStatus.PENDING, version);
    }

    public static Reservation reconstruct(
        Long id, ReserverName name, LocalDate date,
        Time time, Theme theme, ReservationStatus status, OrderStatus orderStatus, long version) {
        return new Reservation(id, name, new Slot(date, time, theme), status, orderStatus, version);
    }

    public Reservation update(ReserverName requestName, LocalDate newDate, Time newTime, Theme newTheme) {
        Slot newSlot = new Slot(newDate, newTime, newTheme);
        validateUpdatable(requestName, newSlot);
        validateChanged(newSlot);

        return new Reservation(this.id, this.name, newSlot, this.status, this.orderStatus, this.version);
    }

    public Reservation delete() {
        if (this.status == ReservationStatus.DELETED) {
            throw new GeneralException(ReservationErrorType.ALREADY_DELETED);
        }

        return new Reservation(this.id, this.name, this.slot, ReservationStatus.DELETED, this.orderStatus, this.version);
    }

    public Reservation cancelActive(ReserverName requestName) {
        if (this.status != ReservationStatus.ACTIVE) {
            throw new GeneralException(ReservationErrorType.NOT_ACTIVE_RESERVATION);
        }
        validateCancelable(requestName);

        return new Reservation(this.id, this.name, this.slot, ReservationStatus.CANCELED, this.orderStatus, this.version);
    }

    public Reservation cancelWaiting(ReserverName requestName) {
        if (this.status != ReservationStatus.WAITING) {
            throw new GeneralException(ReservationErrorType.NOT_WAITING_RESERVATION);
        }
        validateCancelable(requestName);

        return new Reservation(this.id, this.name, this.slot, ReservationStatus.CANCELED, this.orderStatus, this.version);
    }

    public Reservation confirmWaiting() {
        if (this.status != ReservationStatus.WAITING) {
            throw new GeneralException(ReservationErrorType.NOT_WAITING_RESERVATION);
        }

        return new Reservation(this.id, this.name, this.slot, ReservationStatus.ACTIVE, this.orderStatus, this.version);
    }

    public Reservation confirmOrder() {
        if (this.status != ReservationStatus.ACTIVE) {
            throw new GeneralException(ReservationErrorType.NOT_ACTIVE_RESERVATION);
        }
        if (this.orderStatus != OrderStatus.PENDING) {
            throw new GeneralException(ReservationErrorType.ALREADY_CONFIRMED_ORDER);
        }

        return new Reservation(this.id, this.name, this.slot, this.status, OrderStatus.CONFIRMED, this.version);
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
