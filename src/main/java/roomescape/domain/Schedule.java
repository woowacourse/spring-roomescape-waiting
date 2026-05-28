package roomescape.domain;

import java.time.LocalDate;

import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

public class Schedule {

    private final Long id;
    private final LocalDate date;
    private final ReservationTime reservationTime;
    private final Theme theme;

    public Schedule(Long id, LocalDate date, ReservationTime reservationTime, Theme theme) {
        validateDate(date);
        validateTime(reservationTime);
        validateTheme(theme);
        this.id = id;
        this.date = date;
        this.reservationTime = reservationTime;
        this.theme = theme;
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new CustomException(ErrorCode.RESERVATION_DATE_NULL);
        }
    }

    private void validateTime(ReservationTime reservationTime) {
        if (reservationTime == null) {
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

    public ReservationTime getTime() {
        return reservationTime;
    }

    public Theme getTheme() {
        return theme;
    }
}
