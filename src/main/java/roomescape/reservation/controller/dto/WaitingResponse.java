package roomescape.reservation.controller.dto;

public class WaitingResponse {
    private final int waitingNumber;

    public WaitingResponse(int waitingNumber) {
        this.waitingNumber = waitingNumber;
    }

    public String getStatus() {
        if (waitingNumber > 1) {
            return waitingNumber + "번째 예약대기";
        }
        return "예약";
    }
}
