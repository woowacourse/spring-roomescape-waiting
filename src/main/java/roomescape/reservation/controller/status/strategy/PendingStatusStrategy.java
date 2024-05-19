package roomescape.reservation.controller.status.strategy;

import roomescape.reservation.controller.status.ReservationStatusStrategy;

public class PendingStatusStrategy implements ReservationStatusStrategy {
    @Override
    public String getStatus(int waitingNumber) {
        return waitingNumber + "번째 예약대기";
    }
}
