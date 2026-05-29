package roomescape.domain;

import java.time.LocalDate;

import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;

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
            throw new RoomescapeException(DomainErrorCode.INVALID_INPUT, "예약 날짜는 null일 수 없습니다.");
        }
    }

    private void validateTime(ReservationTime reservationTime) {
        if (reservationTime == null) {
            throw new RoomescapeException(DomainErrorCode.INVALID_INPUT, "예약 시간은 null일 수 없습니다.");
        }
    }

    private void validateTheme(Theme theme) {
        if (theme == null) {
            throw new RoomescapeException(DomainErrorCode.INVALID_INPUT, "예약 테마는 null일 수 없습니다.");
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
