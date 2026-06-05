package roomescape.domain.policy;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.stereotype.Component;
import roomescape.exception.client.BusinessRuleViolationException;

@Component
public class FutureOnlyPolicy implements ReservationPolicy {

    private final Clock clock;

    public FutureOnlyPolicy() {
        this.clock = Clock.systemDefaultZone();
    }

    @Override
    public void validateCreatable(LocalDate date, LocalTime time) {
        if (isPast(date.atTime(time))) {
            throw new BusinessRuleViolationException("지나간 날짜, 시간으로는 예약할 수 없습니다.");
        }
    }

    @Override
    public void validateCancellable(LocalDate date, LocalTime time) {
        if (isPast(date.atTime(time))) {
            throw new BusinessRuleViolationException("이미 지난 예약은 취소할 수 없습니다.");
        }
    }

    @Override
    public void validateUpdatable(LocalDate date, LocalTime time) {
        if (isPast(date.atTime(time))) {
            throw new BusinessRuleViolationException("이미 지난 예약은 변경할 수 없습니다.");
        }
    }

    @Override
    public void validateUpdateTarget(LocalDate date, LocalTime time) {
        if (isPast(date.atTime(time))) {
            throw new BusinessRuleViolationException("지나간 날짜, 시간으로는 변경할 수 없습니다.");
        }
    }

    private boolean isPast(LocalDateTime dateTime) {
        return !dateTime.isAfter(LocalDateTime.now(clock));
    }
}
