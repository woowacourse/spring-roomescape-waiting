package roomescape.domain;

import java.time.LocalDate;

import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

public class Reservation {
    private final Long id;
    private final LocalDate date;
    private final Time time;
    private final Theme theme;

    public Reservation(Long id, LocalDate date, Time time, Theme theme) {
        validateDate(date);
        validateTime(time);
        validateTheme(theme);

        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
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

}
