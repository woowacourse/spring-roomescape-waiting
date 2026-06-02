package roomescape.domain.reservation;

import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ExpiredDateTimeException;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservationSlot {

    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public ReservationSlot(LocalDate date, ReservationTime time, Theme theme) {
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Long getTimeId() {
        return time.getId();
    }

    public Long getThemeId() {
        return theme.getId();
    }

    public void validateNoPast() {
        if (LocalDateTime.of(date, time.getStartAt()).isBefore(LocalDateTime.now())) {
            throw new ExpiredDateTimeException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationSlot that)) return false;
        return java.util.Objects.equals(date, that.date)
                && java.util.Objects.equals(getTimeId(), that.getTimeId())
                && java.util.Objects.equals(getThemeId(), that.getThemeId());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(date, getTimeId(), getThemeId());
    }
}