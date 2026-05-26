package roomescape.domain.reservation;

import lombok.Getter;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.exception.BadRequestException;
import roomescape.support.exception.errors.ReservationErrors;
import roomescape.support.exception.errors.ReservationTimeErrors;
import roomescape.support.exception.errors.ThemeErrors;

@Getter
public class Reservation {

    private static final int MAX_NAME_LENGTH = 10;

    private final Long id;
    private final String name;
    private final ReservationDate date;
    private final ReservationTime time;
    private final Theme theme;

    private Reservation(
        Long id,
        String name,
        ReservationDate date,
        ReservationTime time,
        Theme theme
    ) {
        validate(name, date, time, theme);
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static Reservation createWithoutId(
        String name,
        ReservationDate date,
        ReservationTime time,
        Theme theme
    ) {
        return new Reservation(
            null,
            name,
            date,
            time,
            theme
        );
    }

    public static Reservation createWithId(long id, Reservation reservation) {
        return of(
            id,
            reservation.getName(),
            reservation.getDate(),
            reservation.getTime(),
            reservation.getTheme()
        );
    }

    public static Reservation of(
        long id,
        String name,
        ReservationDate date,
        ReservationTime time,
        Theme theme
    ) {
        return new Reservation(
            id,
            name,
            date,
            time,
            theme
        );
    }

    private static void validate(String name, ReservationDate date, ReservationTime time, Theme theme) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException(ReservationErrors.INVALID_RESERVATION_NAME);
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new BadRequestException(ReservationErrors.INVALID_RESERVATION_NAME_LENGTH);
        }
        if (date == null) {
            throw new BadRequestException(ReservationErrors.INVALID_RESERVATION_DATE);
        }
        if (time == null) {
            throw new BadRequestException(ReservationTimeErrors.INVALID_RESERVATION_TIME);
        }
        if (theme == null) {
            throw new BadRequestException(ThemeErrors.INVALID_THEME);
        }
    }
}
