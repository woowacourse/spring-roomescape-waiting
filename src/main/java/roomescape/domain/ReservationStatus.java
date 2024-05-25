package roomescape.domain;

public enum ReservationStatus {
    APPROVED,
    PENDING;

    public String makeStatusMessage(ReservationStatus reservationStatus, Long rank) {
        if (reservationStatus == APPROVED) {
            return "예약";
        }
        return rank + "번째 예약대기";
    }
}
