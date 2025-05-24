package roomescape.reservation.model.exception;

public class ReservationAuthException  extends IllegalArgumentException {

    public ReservationAuthException(String message) {
        super(message);
    }

    public static final class InvalidOwnerException extends ReservationAuthException {

        private static final String MESSAGE = "사용자 자신의 예약 혹은 예약대기만 취소할 수 있습니다.";

        public InvalidOwnerException() {
            super(MESSAGE);
        }
    }
}
