package roomescape.domain;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Schedule {
    private final LocalDate date;
    private final ReservationTime time;

    public static Schedule from(LocalDate date, ReservationTime time) {
        return new Schedule(date, time);
    }

    public boolean isPast(LocalDateTime now) {
        return date.atTime(time.getStartAt()).isBefore(now);
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schedule schedule = (Schedule) o;
        return Objects.equals(date, schedule.date) && Objects.equals(time, schedule.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, time);
    }
}
