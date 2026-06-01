package roomescape.domain;

import java.time.LocalDate;

import static roomescape.domain.exception.DomainErrorCode.INVALID_INPUT;
import static roomescape.domain.exception.DomainPreconditions.requireNonNull;

public class Schedule {

    private final Long id;
    private final Theme theme;
    private final LocalDate date;
    private final ReservationTime reservationTime;

    public Schedule(Long id, Theme theme, LocalDate date, ReservationTime time) {
        this.id = id;
        this.theme = requireNonNull(theme, INVALID_INPUT, "예약 테마는 비어있을 수 없습니다.");
        this.date = requireNonNull(date, INVALID_INPUT, "예약 날짜는 비어있을 수 없습니다.");
        this.reservationTime = requireNonNull(time, INVALID_INPUT, "예약 시간은 비어있을 수 없습니다.");
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
