package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Schedule {
    private final LocalDate date;
    private final ReservationTime time;

    private Schedule(LocalDate date, ReservationTime time) {
        validateFields(date, time);

        this.date = date;
        this.time = time;
    }

    public static Schedule from(LocalDate date, ReservationTime time) {
        return new Schedule(date, time);
    }

    private void validateFields(LocalDate date, ReservationTime time) {
        Objects.requireNonNull(date, "날짜는 필수입니다.");
        Objects.requireNonNull(time, "예약 시간은 필수입니다.");
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
