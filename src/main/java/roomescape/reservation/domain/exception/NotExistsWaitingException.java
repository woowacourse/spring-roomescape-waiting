package roomescape.reservation.domain.exception;

public class NotExistsWaitingException extends RuntimeException {
    public NotExistsWaitingException() {
        super("대기 중인 예약이 없습니다.");
    }
}
