package roomescape.reservation.domain;

import lombok.Getter;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.common.exception.BadRequestException;
import roomescape.common.exception.errors.ReservationSlotErrors;
import roomescape.common.exception.errors.ReservationTimeErrors;
import roomescape.common.exception.errors.ThemeErrors;

@Getter
public class ReservationSlot {

    private final Long id;
    private final ReservationDate date;
    private final ReservationTime time;
    private final Theme theme;

    private ReservationSlot(
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

    public static ReservationSlot createWithoutId(
        ReservationDate date,
        ReservationTime time,
        Theme theme
    ) {
        return new ReservationSlot(
            null,
            date,
            time,
            theme
        );
    }

    public static ReservationSlot createWithId(long id, ReservationSlot reservation) {
        return of(
            id,
            reservation.getDate(),
            reservation.getTime(),
            reservation.getTheme()
        );
    }

    public static ReservationSlot of(
        long id,
        ReservationDate date,
        ReservationTime time,
        Theme theme
    ) {
        return new ReservationSlot(
            id,
            date,
            time,
            theme
        );
    }

    private static void validate(ReservationDate date, ReservationTime time, Theme theme) {
        if (date == null) {
            throw new BadRequestException(ReservationSlotErrors.INVALID_RESERVATION_DATE);
        }
        if (time == null) {
            throw new BadRequestException(ReservationTimeErrors.INVALID_RESERVATION_TIME);
        }
        if (theme == null) {
            throw new BadRequestException(ThemeErrors.INVALID_THEME);
        }
    }
}
