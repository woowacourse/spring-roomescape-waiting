package roomescape.reservation.controller.dto;

import roomescape.reservation.controller.status.ReservationStatusStrategy;
import roomescape.reservation.controller.status.ReservationStatusStrategyFactory;
import roomescape.reservation.domain.ReservationStatus;

public class WaitingResponse {
    private final ReservationStatus status;
    private final int waitingNumber;

    public WaitingResponse(ReservationStatus status, int waitingNumber) {
        this.status = status;
        this.waitingNumber = waitingNumber;
    }

    public String getStatus() {
        ReservationStatusStrategy strategy = ReservationStatusStrategyFactory.getStrategy(status);
        return strategy.getStatus(waitingNumber);
    }
}
