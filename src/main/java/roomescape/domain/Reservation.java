package roomescape.domain;

import java.util.Objects;
import lombok.Getter;

import java.time.LocalDate;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

@Getter
public class Reservation {

    private final Long id;
    private final PersonName name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    private Reservation(final Long id, final PersonName name, final LocalDate date, final ReservationTime time, final Theme theme) {
        this.id = id;
        this.name = name;
        validateDate(date);
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private static void validateId(final Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.RESERVATION_ID_NULL);
        }
    }

    private static void validateDate(final LocalDate date) {
        if (date == null) {
            throw new BusinessException(ErrorCode.DATE_NULL);
        }
    }

    public static Reservation create(final String name, final LocalDate date, final ReservationTime time, final Theme theme) {
        return new Reservation(
                null,
                new PersonName(name),
                date,
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
                date,
                time,
                theme
        );
    }

    public Reservation withId(final Long id) {
        validateId(id);
        return new Reservation(
                id,
                this.name,
                this.date,
                this.time,
                this.theme
        );
    }

    public Reservation modify(final LocalDate newDate, final ReservationTime newReservationTime) {
        return new Reservation(
                this.id,
                this.name,
                Objects.requireNonNullElse(newDate, this.date),
                Objects.requireNonNullElse(newReservationTime, this.time),
                this.theme
        );
    }

    public String getName() {
        return this.name.getName();
    }
}
