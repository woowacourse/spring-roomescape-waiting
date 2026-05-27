package roomescape.reservationWaiting.exception;

public enum ReservationWaitingErrorCode {
    ALREADY_RESERVED("자신이 이미 예약한 시간입니다."),
    DUPLICATE_WAITING("이미 존재하는 예약 대기입니다."),
    WAITING_NOT_FOUND("예약 대기가 존재하지 않습니다."),
    TARGET_RESERVATION_NOT_FOUND("대기할 예약이 존재하지 않습니다.");

    private final String message;

    ReservationWaitingErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
