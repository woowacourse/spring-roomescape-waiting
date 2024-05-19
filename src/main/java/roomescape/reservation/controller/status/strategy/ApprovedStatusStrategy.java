package roomescape.reservation.controller.status.strategy;

import roomescape.reservation.controller.status.ReservationStatusStrategy;

public class ApprovedStatusStrategy implements ReservationStatusStrategy {
    @Override
    public String getStatus(int waitingNumber) {
        return "예약";
    }
}
