package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.exception.ReservationErrorCode;
import roomescape.exception.ReservationSlotErrorCode;
import roomescape.exception.RoomEscapeException;

public class ReservationSlot {

    private final LocalDate date;

    private final ReservationTime time;

    private final Theme theme;

    private ReservationSlot(LocalDate date, ReservationTime time, Theme theme) {
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        this.date = date;
        this.time = time;
        this.theme = theme;
    }
    public static ReservationSlot of(LocalDate date, ReservationTime time, Theme theme) {
        return new ReservationSlot(date, time, theme);
    }

    private static void validateDate(LocalDate date) {
        if (date == null) {
            throw new RoomEscapeException(ReservationErrorCode.INVALID_DATE);
        }
    }

    private static void validateTime(ReservationTime time) {
        if (time == null) {
            throw new RoomEscapeException(ReservationErrorCode.INVALID_TIME);
        }
    }

    private static void validateTheme(Theme theme) {
        if (theme == null) {
            throw new RoomEscapeException(ReservationErrorCode.INVALID_THEME);
        }
    }

    public void validateNotPastTime(LocalDateTime now) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());

        if (reservationDateTime.isBefore(now)) {
            throw new RoomEscapeException(ReservationSlotErrorCode.SLOT_PAST_TIME);
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationSlot that = (ReservationSlot) o;
        return Objects.equals(date, that.date)
                && Objects.equals(time, that.time)
                && Objects.equals(theme, that.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time, theme);
    }
}
