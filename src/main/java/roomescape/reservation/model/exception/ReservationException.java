package roomescape.reservation.model.exception;

// TODO : 추상화 수준 낮추기
sealed public class ReservationException extends RuntimeException {

    public ReservationException(String message) {
        super(message);
    }

    public static final class InvalidReservationTimeException extends ReservationException {

        public InvalidReservationTimeException(String message) {
            super(message);
        }
    }

    public static final class ReservationNotFoundException extends ReservationException {

        public ReservationNotFoundException(String message) {
            super(message);
        }
    }

    public static final class ReservationThemeInUseException extends ReservationException {

        public ReservationThemeInUseException(String message) {
            super(message);
        }
    }

    public static final class ReservationTimeInUseException extends ReservationException {

        public ReservationTimeInUseException(String message) {
            super(message);
        }
    }

    public static final class AlreadyDoneWaitingException extends ReservationException {

        private static final String message = "해당 테마 및 날짜에 이미 예약 대기를 진행한 상태입니다";

        public AlreadyDoneWaitingException() {
            super(message);
        }
    }
}
