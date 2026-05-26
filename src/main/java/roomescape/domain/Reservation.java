package roomescape.domain;

import java.time.LocalDate;

import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

public class Reservation {
    private final Long id;
    private final LocalDate date;
    private final Time time;
    private final Theme theme;
    private final Waiting waiting;

    public Reservation(Long id, LocalDate date, Time time, Theme theme, Waiting waiting) {
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        validateWaiting(waiting);

        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.waiting = waiting;
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new CustomException(ErrorCode.RESERVATION_DATE_NULL);
        }
    }

    private void validateTime(Time time) {
        if (time == null) {
            throw new CustomException(ErrorCode.RESERVATION_TIME_NULL);
        }
    }

    private void validateTheme(Theme theme) {
        if (theme == null) {
            throw new CustomException(ErrorCode.RESERVATION_THEME_NULL);
        }
    }
    
    private void validateWaiting(Waiting waiting) {
        if (waiting == null) {
            throw new CustomException(ErrorCode.RESERVATION_THEME_NULL);
        }
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Time getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Waiting getWaiting() {
        return waiting;
    }
}
