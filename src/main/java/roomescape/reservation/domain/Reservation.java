package roomescape.reservation.domain;

import static roomescape.common.domain.DomainPreconditions.require;
import static roomescape.common.domain.DomainPreconditions.requireNonBlank;
import static roomescape.common.domain.DomainPreconditions.requireNonNull;
import static roomescape.reservation.exception.ReservationErrorCode.INVALID_RESERVATION_DATE;
import static roomescape.reservation.exception.ReservationErrorCode.INVALID_RESERVATION_GUEST_NAME;
import static roomescape.reservation.exception.ReservationErrorCode.RESERVATION_ALREADY_HAS_ID;

import java.time.LocalDate;
import java.util.Objects;
import lombok.Getter;
import roomescape.common.exception.DomainException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Getter
public class Reservation {
    private final Long id;
    private final String guestName;
    private final ReservationSlot slot;
    private final Status status;

    private Reservation(Long id, String guestName, ReservationSlot slot, Status status) {
        validateReservation(guestName, slot);
        this.id = id;
        this.guestName = guestName;
        this.slot = slot;
        this.status = status;
    }

    public static Reservation create(String guestName, LocalDate date, ReservationTime time, Theme theme,
                                     Status status) {
        return create(guestName, ReservationSlot.of(date, time, theme), status);
    }

    public static Reservation create(String guestName, ReservationSlot slot, Status status) {
        return new Reservation(null, guestName, slot, status);
    }

    public static Reservation of(
            long id,
            String guestName,
            LocalDate date,
            ReservationTime time,
            Theme theme,
            Status status
    ) {
        return of(id, guestName, ReservationSlot.of(date, time, theme), status);
    }

    public static Reservation of(long id, String guestName, ReservationSlot slot, Status status) {
        return new Reservation(id, guestName, slot, status);
    }

    public Reservation withId(long id) {
        require(this.id == null, new DomainException(RESERVATION_ALREADY_HAS_ID));
        return of(id, guestName, slot, status);
    }

    private void validateReservation(String guestName, ReservationSlot slot) {
        requireNonBlank(guestName, new DomainException(INVALID_RESERVATION_GUEST_NAME));
        requireNonNull(slot, new DomainException(INVALID_RESERVATION_DATE));
    }

    public LocalDate getDate() {
        return slot.date();
    }

    public ReservationTime getTime() {
        return slot.time();
    }

    public Theme getTheme() {
        return slot.theme();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reservation that)) {
            return false;
        }
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public boolean isSameGuest(String guestName) {
        return Objects.equals(this.guestName, guestName);
    }


    public boolean isConfirmed() {
        return status == Status.CONFIRMED;
    }

    public boolean isCanceled() {
        return status == Status.CANCELED;
    }

    public boolean hasSameSlotAs(Reservation other) {
        return slot.equals(other.slot);
    }

    public Reservation changeSlot(ReservationSlot changedSlot, Status status) {
        return of(id, guestName, changedSlot, status);
    }
}
