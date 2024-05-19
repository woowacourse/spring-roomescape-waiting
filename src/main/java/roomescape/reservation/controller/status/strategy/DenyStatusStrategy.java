package roomescape.reservation.controller.status.strategy;

import roomescape.reservation.controller.status.ReservationStatusStrategy;

public class DenyStatusStrategy implements ReservationStatusStrategy {
    @Override
    public String getStatus(int waitingNumber) {
        return "거절된 예약";
    }
}
