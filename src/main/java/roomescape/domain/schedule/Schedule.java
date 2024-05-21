package roomescape.domain.schedule;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.ManyToOne;
import roomescape.exception.InvalidReservationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Embeddable
public class Schedule {
    @Embedded
    private ReservationDate date;

    @ManyToOne
    private ReservationTime time;

    protected Schedule() {
    }

    public Schedule(ReservationDate date, ReservationTime time) {
        validateIfBefore(date, time);
        this.date = date;
        this.time = time;
    }

    private void validateIfBefore(ReservationDate date, ReservationTime time) {
        LocalDateTime value = LocalDateTime.of(date.getValue(), time.getStartAt());
        if (value.isBefore(LocalDateTime.now())) {
            throw new InvalidReservationException("현재보다 이전으로 일정을 설정할 수 없습니다.");
        }
    }

    public LocalDate getDate() {
        return date.getValue();
    }

    public LocalTime getTime() {
        return time.getStartAt();
    }

    public ReservationTime getReservationTime() {
        return time;
    }
}
