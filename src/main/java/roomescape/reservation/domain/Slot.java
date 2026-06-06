package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.common.exception.RoomEscapeException;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class Slot {

    private final Long id;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    private Slot(Long id, LocalDate date, ReservationTime time, Theme theme) {
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static Slot create(LocalDate date, ReservationTime time, Theme theme) {
        return new Slot(null, date, time, theme);
    }

    public static Slot of(Long id, LocalDate date, ReservationTime time, Theme theme) {
        validateId(id);
        return new Slot(id, date, time, theme);
    }

    private static void validateId(Long id) {
        if (id == null) {
            throw new IllegalStateException("ID는 필수값입니다.");
        }
        if (id < 1) {
            throw new IllegalStateException("ID는 1 이상의 숫자여야 합니다. (입력값: " + id + ")");
        }
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

    public boolean isPast(LocalDateTime now) {
        LocalDateTime slotDateTime = LocalDateTime.of(date, time.getStartAt());
        return slotDateTime.isBefore(now);
    }

    public Long getId() {
        return id;
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
}
