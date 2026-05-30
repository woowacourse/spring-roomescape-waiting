package roomescape.reservation.domain;

import roomescape.date.domain.ReservationDate;
import roomescape.reservation.exception.ReservationException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static roomescape.reservation.exception.ReservationErrorInformation.RESERVATION_ALREADY_PAST;

public record ReservationSlot(
        ReservationDate date,
        ReservationTime time,
        Theme theme
) {
    public static ReservationSlot of(ReservationDate date, ReservationTime time, Theme theme) {
        return new ReservationSlot(date, time, theme);
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

    public void validateNotPast() {
        if (isPast(date.getDate(), time.getStartAt())) {
            throw new ReservationException(RESERVATION_ALREADY_PAST);
        }
    }

    private boolean isPast(LocalDate date, LocalTime time) {
        return LocalDateTime.of(date, time).isBefore(LocalDateTime.now());
    }

}
