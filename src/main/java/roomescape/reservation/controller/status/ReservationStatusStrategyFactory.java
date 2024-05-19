package roomescape.reservation.controller.status;

import roomescape.reservation.controller.status.strategy.ApprovedStatusStrategy;
import roomescape.reservation.controller.status.strategy.DenyStatusStrategy;
import roomescape.reservation.controller.status.strategy.PendingStatusStrategy;
import roomescape.reservation.domain.ReservationStatus;

public class ReservationStatusStrategyFactory {
    public static ReservationStatusStrategy getStrategy(ReservationStatus status) {
        return switch (status) {
            case APPROVED -> new ApprovedStatusStrategy();
            case PENDING -> new PendingStatusStrategy();
            case DENY -> new DenyStatusStrategy();
            default -> throw new IllegalStateException("존재하지 않는 예약 상태입니다. status = " + status);
        };
    }
}

