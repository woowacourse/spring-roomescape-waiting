package roomescape.domain;

import lombok.Getter;

import java.time.LocalDate;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

@Getter
public class Reservation {

    private final Long id;
    private final PersonName name;

    private final ReservationDate reservationDate;
    private final ReservationTime time;
    private final Theme theme;

    private Reservation(final Long id, final PersonName name, final ReservationDate date, final ReservationTime time, final Theme theme) {
        this.id = id;
        this.name = name;
        this.reservationDate = date;
        this.time = time;
        this.theme = theme;
    }

    private static void validateId(final Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.RESERVATION_ID_NULL);
        }
    }

    public static Reservation create(final String name, final LocalDate date, final ReservationTime time, final Theme theme) {
        return new Reservation(
                null,
                new PersonName(name),
                new ReservationDate(date),
                time,
                theme
        );
    }

    public static Reservation createWithId(
            final Long id,
            final String name,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme) {

        validateId(id);
        return new Reservation(
                id,
                new PersonName(name),
                new ReservationDate(date),
                time,
                theme
        );
    }

    public Reservation withId(final Long id) {
        validateId(id);
        return new Reservation(
                id,
                this.name,
                this.reservationDate,
                this.time,
                this.theme
        );
    }

    public Reservation modify(final LocalDate newDate, final ReservationTime newReservationTime, final Theme theme) {
        return new Reservation(
                this.id,
                this.name,
                new ReservationDate(newDate),
                newReservationTime,
                theme
        );
    }

    public void validateNotPast() {
        if (reservationDate.isPast()) {
            throw new BusinessException(ErrorCode.DATE_ALREADY_PASSED);
        }
        if (reservationDate.isToday() && time.isBefore()) {
            throw new BusinessException(ErrorCode.TIME_ALREADY_PASSED);
        }
    }

    public boolean isSameSlot(final Reservation other) {
        return this.reservationDate.getDate().equals(other.reservationDate.getDate())
                && this.time.getId().equals(other.time.getId())
                && this.theme.getId().equals(other.theme.getId());
    }

    public void validateOwner(final String name) {
        if (!this.name.getName().equals(name)) {
            throw new BusinessException(ErrorCode.USER_NAME_NOT_MATCHED);
        }
    }

    public String getName() {
        return this.name.getName();
    }
}
