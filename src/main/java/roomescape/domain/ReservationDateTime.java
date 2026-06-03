package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.domain.exception.InvalidDomainException;

/**
 * 예약이 일어나는 "시점"(날짜 + 시작 시각)을 표현하는 값 객체.
 *
 * <p>존재 이유: "이 시점이 어떤 기준 시각보다 이후인가"라는 비교를 도메인이 자기 책임으로 갖기 위해.
 * 이전엔 이 비교가 정책(FutureOnlyPolicy) 안에 숨어 있었다(!date.atTime(time).isAfter(now)). 비교는 "입력만으로 결정되는 순수한 사실"이라 도메인의 것이고, "지금이 몇
 * 시인가(Clock)"와 "이후가 아니면 위반이다"라는 판정은 정책의 것이다.
 *
 * <p>의도적으로 Clock을 갖지 않는다. startsAfter는 기준 시각을 인자로 받는 순수 비교라
 * 시계 의존 없이 결정적으로 테스트된다. (값 동등성이 필요해지기 전까지 equals/hashCode는 두지 않는다.)
 */
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
