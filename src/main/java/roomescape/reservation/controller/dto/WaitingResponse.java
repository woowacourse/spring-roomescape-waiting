package roomescape.reservation.controller.dto;

import roomescape.reservation.domain.ReservationStatus;

public class WaitingResponse {

    private final ReservationStatus status;

    private final int waitingNumber;

    public WaitingResponse(ReservationStatus status, int waitingNumber) {
        this.status = status;
        this.waitingNumber = waitingNumber;
    }

    public String getStatus() {
        return switch (status) {
            case APPROVED -> "예약";
            case PENDING -> waitingNumber + "번째 예약대기";
            case DENY -> "거절된 예약";
            default -> throw new IllegalStateException("존재하지 않는 예약 상태입니다. status = " + status);
        };
    }
}
