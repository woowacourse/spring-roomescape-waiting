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

    private final Long id;
    private final ReservationDate date;
    private final ReservationTime time;
    private final Theme theme;

    private Reservation(
        Long id,
        ReservationDate date,
        ReservationTime time,
        Theme theme
    ) {
        validate(date, time, theme);
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static Reservation createWithoutId(
        ReservationDate date,
        ReservationTime time,
        Theme theme
    ) {
        return new Reservation(
            null,
            date,
            time,
            theme
        );
    }

    public static Reservation createWithId(long id, Reservation reservation) {
        return of(
            id,
            reservation.getDate(),
            reservation.getTime(),
            reservation.getTheme()
        );
    }

    public static Reservation of(
        long id,
        ReservationDate date,
        ReservationTime time,
        Theme theme
    ) {
        return new Reservation(
            id,
            date,
            time,
            theme
        );
    }

    private static void validate(ReservationDate date, ReservationTime time, Theme theme) {
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
