package roomescape.reservation.domain.exception;

public class PastReservationException extends RuntimeException {

    public PastReservationException() {
        super("[ERROR] 지난 날짜에는 예약할 수 없습니다.");
    }
}
