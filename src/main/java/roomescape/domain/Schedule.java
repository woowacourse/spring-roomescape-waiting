package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import roomescape.exception.RoomEscapeException;

@Getter
@EqualsAndHashCode(of = {"date", "time"})
@ToString
public class Schedule {

    private final LocalDate date;
    private final ReservationTime time;

    private Schedule(LocalDate date, ReservationTime time) {
        if (date == null || time == null) {
            throw new RoomEscapeException("예약 날짜 및 시간 정보는 비어있을 수 없습니다.");
        }
        this.date = date;
        this.time = time;
    }

    public static Schedule of(LocalDate date, ReservationTime time) {
        return new Schedule(date, time);
    }

    public boolean isPast(LocalDateTime now) {
        return toDateTime().isBefore(now);
    }

    public LocalTime getStartAt() {
        return time.getStartAt();
    }

    private LocalDateTime toDateTime() {
        return LocalDateTime.of(date, time.getStartAt());
    }
}
