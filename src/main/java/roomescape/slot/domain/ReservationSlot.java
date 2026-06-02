package roomescape.slot.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import roomescape.date.domain.ReservationDate;
import roomescape.reservation.exception.ReservationException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDateTime;

import static roomescape.reservation.exception.ReservationErrorInformation.*;
import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_THEME_IS_NULL;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReservationSlot {

    private Long id;
    private ReservationDate date;
    private ReservationTime time;
    private Theme theme;

    public static ReservationSlot of(ReservationDate date, ReservationTime time, Theme theme) {
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        return new ReservationSlot(null, date, time, theme);
    }

    public static ReservationSlot load(Long id, ReservationDate date, ReservationTime time, Theme theme) {
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        return new ReservationSlot(id, date, time, theme);
    }

    private static void validateDate(ReservationDate date) {
        if (date == null) {
            throw new ReservationException(RESERVATION_DATE_IS_NULL);
        }
    }

    private static void validateTime(ReservationTime time) {
        if (time == null) {
            throw new ReservationException(RESERVATION_TIME_IS_NULL);
        }
    }

    private static void validateTheme(Theme theme) {
        if (theme == null) {
            throw new ReservationException(RESERVATION_THEME_IS_NULL);
        }
    }

    public void validateNotPast(LocalDateTime reservedAt) {
        if (LocalDateTime.of(date.getDate(), time.getStartAt()).isBefore(reservedAt)) {
            throw new ReservationException(RESERVATION_ALREADY_PAST);
        }
    }

    public Long getDateId() {
        return date.getId();
    }

    public Long getTimeId() {
        return time.getId();
    }

    public Long getThemeId() {
        return theme.getId();
    }

}
