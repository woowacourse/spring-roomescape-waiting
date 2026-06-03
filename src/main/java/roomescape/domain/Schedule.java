package roomescape.domain;

import roomescape.domain.exception.DomainRuleViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Schedule {

    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Schedule(LocalDate date, ReservationTime time, Theme theme) {
        if (date == null) {
            throw new DomainRuleViolationException("예약 날짜는 비어 있을 수 없습니다.");
        }
        if (time == null) {
            throw new DomainRuleViolationException("예약 시간은 비어 있을 수 없습니다.");
        }
        if (theme == null) {
            throw new DomainRuleViolationException("예약 테마는 비어 있을 수 없습니다.");
        }
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public boolean isPast(LocalDateTime now) {
        return time.isPast(date, now);
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
        if (!(o instanceof Schedule other)) {
            return false;
        }
        return Objects.equals(date, other.date)
                && Objects.equals(time, other.time)
                && Objects.equals(theme, other.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time, theme);
    }
}
