package roomescape.domain.reservation.model.exception;

// TODO : 추상화 수준 낮추기
public class ReservationException extends RuntimeException {

    public ReservationException(String message) {
        super(message);
    }

    public static final class InvalidReservationTimeException extends ReservationException {

        public InvalidReservationTimeException(String message) {
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
}
