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

    /**
     * 이 시점이 주어진 기준 시각보다 엄격히 이후인가. 같은 순간은 "이후"가 아니다(false). "동일 시각을 허용/거부할지"의 결정은 이 비교 위에서 정책이 내린다.
     */
    public boolean startsAfter(LocalDateTime moment) {
        return date.atTime(time).isAfter(moment);
    }
}
