package roomescape.domain.policy;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import roomescape.domain.ReservationDateTime;
import roomescape.exception.client.BusinessRuleViolationException;

@Component
public class FutureOnlyPolicy implements ReservationPolicy {

    private final Clock clock;

    public FutureOnlyPolicy(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void validateCreatable(ReservationDateTime when) {
        rejectIfNotFuture(when, "지나간 날짜, 시간으로는 예약할 수 없습니다.");
    }

    @Override
    public void validateCancellable(ReservationDateTime when) {
        rejectIfNotFuture(when, "이미 지난 예약은 취소할 수 없습니다.");
    }

    @Override
    public void validateUpdatable(ReservationDateTime when) {
        rejectIfNotFuture(when, "이미 지난 예약은 변경할 수 없습니다.");
    }

    @Override
    public void validateUpdateTarget(ReservationDateTime when) {
        rejectIfNotFuture(when, "지나간 날짜, 시간으로는 변경할 수 없습니다.");
    }

    private void rejectIfNotFuture(ReservationDateTime when, String message) {
        if (when.startsAtOrBefore(LocalDateTime.now(clock))) {
            throw new BusinessRuleViolationException(message);
        }
    }
}
