package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.domain.exception.InvalidDomainException;

public class ReservationDateTime {

    private final LocalDate date;
    private final LocalTime time;

    private ReservationDateTime(LocalDate date, LocalTime time) {
        if (date == null) {
            throw new InvalidDomainException("예약 시점의 날짜는 비어 있을 수 없습니다.");
        }
        if (time == null) {
            throw new InvalidDomainException("예약 시점의 시간은 비어 있을 수 없습니다.");
        }
        this.date = date;
        this.time = time;
    }

    public static ReservationDateTime of(LocalDate date, LocalTime time) {
        return new ReservationDateTime(date, time);
    }

    public boolean startsAtOrBefore(LocalDateTime moment) {
        return !date.atTime(time).isAfter(moment);
    }
}
